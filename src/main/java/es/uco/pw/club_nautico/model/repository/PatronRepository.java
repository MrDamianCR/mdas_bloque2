package es.uco.pw.club_nautico.model.repository;

import es.uco.pw.club_nautico.model.domain.Patron;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Repository
public class PatronRepository {

    private final JdbcTemplate jdbcTemplate;
    private Properties sqlQueries;
    private String sqlQueriesFileName;

    public PatronRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setSQLQueriesFileName(String sqlQueriesFileName) {
        this.sqlQueriesFileName = sqlQueriesFileName;
        createProperties();
    }

    /**
     * Listar todos los patrones
     */
    public List<Patron> findAllPatrones() {
        try {
            String query = sqlQueries.getProperty("select-findAllPatrones");
            if (query != null) {
                List<Patron> result = jdbcTemplate.query(query, new RowMapper<Patron>() {
                    @Override
                    public Patron mapRow(ResultSet rs, int rowNumber) throws SQLException {
                        return new Patron(
                                rs.getString("dni_patron"),
                                rs.getString("nombre"),
                                rs.getString("apellidos"),
                                Date.valueOf(rs.getString("fecha_nacimiento")).toLocalDate(),
                                Date.valueOf(rs.getString("fecha_exp_patron")).toLocalDate());
                    }
                });
                return result;
            } else {
                return Collections.emptyList();
            }
        } catch (DataAccessException exception) {
            System.err.println("Incapaz de encontrar Patrones");
            exception.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 🔹 NUEVO: Listar patrones libres (no asociados a ninguna embarcación)
     */
    public List<Patron> findPatronesLibres() {
        try {
            String query = sqlQueries.getProperty("select-findPatronesLibres");
            if (query == null) {
                System.err.println("No se encontró la query 'select-findPatronesLibres' en patrones.properties");
                return Collections.emptyList();
            }

            return jdbcTemplate.query(query, (rs, rowNum) -> new Patron(
                    rs.getString("dni_patron"),
                    rs.getString("nombre"),
                    rs.getString("apellidos"),
                    Date.valueOf(rs.getString("fecha_nacimiento")).toLocalDate(),
                    Date.valueOf(rs.getString("fecha_exp_patron")).toLocalDate()));

        } catch (DataAccessException e) {
            System.err.println("Error al recuperar patrones libres");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Buscar patrón por DNI
     */
    public Patron findPatronByDni(String dni) {
        try {
            String query = sqlQueries.getProperty("select-findPatronByDni");
            Patron result = jdbcTemplate.query(query, this::mapRowToPatron, dni);
            if (result != null) {
                return result;
            } else {
                return null;
            }
        } catch (DataAccessException exception) {
            System.err.println("No se puede encontrar Patrón con DNI=" + dni);
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Método auxiliar para mapear un ResultSet a un Patron.
     * Igual estilo que mapRowToSocio en SocioRepository.
     */
    private Patron mapRowToPatron(ResultSet row) throws SQLException {
        try {
            if (row.first()) { // Verificamos si hay filas
                String dni_patron = row.getString("dni_patron");
                String nombre = row.getString("nombre");
                String apellidos = row.getString("apellidos");
                Date fecha_nacimiento = Date.valueOf(row.getString("fecha_nacimiento"));
                Date fecha_exp_patron = Date.valueOf(row.getString("fecha_exp_patron"));

                return new Patron(
                        dni_patron,
                        nombre,
                        apellidos,
                        fecha_nacimiento.toLocalDate(),
                        fecha_exp_patron.toLocalDate());
            } else {
                return null;
            }
        } catch (SQLException exception) {
            System.err.println("No se pueden recuperar resultados de la base de datos (Patron)");
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Insertar un nuevo patrón
     */
    public boolean addPatron(Patron patron) {
        if (existsByDni(patron.getDni_patron())) {
            System.err.println("El patrón ya existe");
            return false;
        }

        try {
            String query = sqlQueries.getProperty("insert-addPatron");
            if (query != null) {
                int result = jdbcTemplate.update(query,
                        patron.getDni_patron(),
                        patron.getNombre(),
                        patron.getApellidos(),
                        patron.getFecha_nacimiento().toString(),
                        patron.getFecha_exp_patron().toString());
                return result > 0;
            } else {
                return false;
            }
        } catch (DataAccessException exception) {
            System.err.println("Incapaz de meter un Patrón en la base de datos");
            exception.printStackTrace();
            return false;
        }
    }

    /**
     * Comprobar si existe un patrón por DNI
     */
    public boolean existsByDni(String dni) {
        String sql = "SELECT COUNT(*) FROM Patron WHERE dni_patron = ?";
        Integer n = jdbcTemplate.queryForObject(sql, Integer.class, dni);
        return n != null && n > 0;
    }

    /**
     * Modificar patrón
     */
    public boolean updatePatron(Patron patron) {
        try {
            String query = sqlQueries.getProperty("update-updatePatron");
            if (query != null) {
                int result = jdbcTemplate.update(query,
                        patron.getNombre(),
                        patron.getApellidos(),
                        patron.getFecha_nacimiento().toString(),
                        patron.getFecha_exp_patron().toString(),
                        patron.getDni_patron());

                if (result > 0) {
                    System.out.println("Patrón actualizado correctamente: " + patron.getDni_patron());
                    return true;
                } else {
                    System.err.println("No se encontró patrón con DNI " + patron.getDni_patron() + " para actualizar.");
                    return false;
                }
            } else {
                System.err.println("No se encontró la query 'update-updatePatron' en patrones.properties");
                return false;
            }

        } catch (DataAccessException e) {
            System.err.println("Error al actualizar patrón con DNI=" + patron.getDni_patron());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Eliminar patrón por DNI
     */
    public boolean deletePatron(String dni) {
        try {
            String query = sqlQueries.getProperty("delete-deletePatronByDni");

            int result = jdbcTemplate.update(query, dni);

            if (result > 0) {
                System.out.println("Patrón con DNI " + dni + " eliminado correctamente.");
                return true;
            } else {
                System.err.println("No se encontró el patrón con DNI " + dni + " para eliminar.");
                return false;
            }

        } catch (DataAccessException exception) {
            System.err.println("Error al intentar eliminar el patrón con DNI=" + dni);
            exception.printStackTrace();
            return false;
        }
    }

    /**
     * Buscar patrones cuya fecha de expiración sea menor o igual que una fecha
     * (usa la query select-findByExpiracion)
     */
    public List<Patron> findByExpiracion(LocalDate fechaExpiracion) {
        try {
            String query = sqlQueries.getProperty("select-findByExpiracion");
            if (query != null) {
                return jdbcTemplate.query(query, (rs, rowNum) -> new Patron(
                        rs.getString("dni_patron"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        Date.valueOf(rs.getString("fecha_nacimiento")).toLocalDate(),
                        Date.valueOf(rs.getString("fecha_exp_patron")).toLocalDate()), fechaExpiracion.toString());
            } else {
                System.err.println("No se encontró la query 'select-findByExpiracion'");
                return Collections.emptyList();
            }
        } catch (DataAccessException e) {
            System.err.println("Error al buscar patrones con fecha de expiración <= " + fechaExpiracion);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Carga del fichero patrones.properties
     */
    private void createProperties() {
        sqlQueries = new Properties();
        try {
            BufferedReader reader;
            File f = new File(sqlQueriesFileName);
            reader = new BufferedReader(new FileReader(f));
            sqlQueries.load(reader);
        } catch (IOException e) {
            System.err.println("Error creando propiedades objetos para las consultas SQL de Patron");
            e.printStackTrace();
        }
    }

}
