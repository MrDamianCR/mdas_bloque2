package es.uco.pw.club_nautico.model.repository;

import es.uco.pw.club_nautico.model.domain.Socio.Socio;
import es.uco.pw.club_nautico.model.domain.Socio.SocioRol;

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
import java.util.List;
import java.util.Properties;

@Repository
public class SocioRepository {

    private final JdbcTemplate jdbcTemplate;
    private Properties sqlQueries;
    private String sqlQueriesFileName;

    public SocioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setSQLQueriesFileName(String sqlQueriesFileName) {
        this.sqlQueriesFileName = sqlQueriesFileName;
        createProperties();
    }

    public List<Socio> findAllSocios() {
        try {
            String query = sqlQueries.getProperty("select-findAllSocios");
            if (query != null) {
                List<Socio> result = jdbcTemplate.query(query, new RowMapper<Socio>() {
                    public Socio mapRow(ResultSet rs, int rowNumber) throws SQLException {
                        return new Socio(
                                rs.getString("dni_socio"),
                                rs.getString("nombre"),
                                rs.getString("apellidos"),
                                Date.valueOf(rs.getString("fecha_nacimiento")).toLocalDate(),
                                rs.getString("direccion"),
                                Date.valueOf(rs.getString("fecha_inscripcion")).toLocalDate(),
                                rs.getBoolean("es_patron"),
                                rs.getInt("id_inscripcion"),
                                SocioRol.valueOf(rs.getString("rol")));
                    };
                });
                return result;
            } else
                return null;
        } catch (DataAccessException exception) {
            System.err.println("Incapaz de encontrar Socios");
            exception.printStackTrace();
            return null;
        }
    }

    public Socio findSocioByDni(String dni) {
        try {
            String query = sqlQueries.getProperty("select-findSocioByDni");
            Socio result = jdbcTemplate.query(query, this::mapRowToSocio, dni);
            if (result != null)
                return result;
            else
                return null;
        } catch (DataAccessException exception) {
            System.err.println("No se puede encontrar Socio con DNI=" + dni);
            exception.printStackTrace();
            return null;
        }
    }

    private Socio mapRowToSocio(ResultSet row) throws SQLException {
        try {
            if (row.first()) { 
                String dni_socio = row.getString("dni_socio");
                String nombre = row.getString("nombre");
                String apellido = row.getString("apellidos");
                Date fecha_nacimiento = Date.valueOf(row.getString("fecha_nacimiento"));
                String direccion = row.getString("direccion");
                Date fecha_inscripcion = Date.valueOf(row.getString("fecha_inscripcion"));
                Boolean es_patron = row.getBoolean("es_patron");
                Integer id_inscripcion = row.getInt("id_inscripcion");
                SocioRol rol = SocioRol.valueOf(row.getString("rol"));

                return new Socio(dni_socio, nombre, apellido, fecha_nacimiento.toLocalDate(),
                        direccion, fecha_inscripcion.toLocalDate(), es_patron,
                        id_inscripcion, rol);
            } else {
                return null;
            }
        } catch (SQLException exception) {
            System.err.println("No se pueden recuperar resultados de la base de datos");
            exception.printStackTrace();
            return null;
        }
    }

    public List<Socio> findSociosPatron() {
        try {
            String query = sqlQueries.getProperty("select-findSociosPatron");
            if (query != null) {
                List<Socio> result = jdbcTemplate.query(query, new RowMapper<Socio>() {
                    public Socio mapRow(ResultSet rs, int rowNumber) throws SQLException {
                        return new Socio(
                                rs.getString("dni_socio"),
                                rs.getString("nombre"),
                                rs.getString("apellidos"),
                                Date.valueOf(rs.getString("fecha_nacimiento")).toLocalDate(),
                                rs.getString("direccion"),
                                Date.valueOf(rs.getString("fecha_inscripcion")).toLocalDate(),
                                rs.getBoolean("es_patron"),
                                rs.getInt("id_inscripcion"),
                                SocioRol.valueOf(rs.getString("rol")));
                    };
                });
                return result;
            } else
                return null;
        } catch (DataAccessException exception) {
            System.err.println("Incapaz de meter un Usuario en la base de datos");
            exception.printStackTrace();
            return null;
        }
    }

    public boolean addSocio(Socio socio) {
        if (existsByDni(socio.getDni())) {
            System.err.println("El usuario ya existe");
            return false;
        }

        try {
            String query = sqlQueries.getProperty("insert-addSocio");
            if (query != null) {
                int result = jdbcTemplate.update(query,
                        socio.getDni(),
                        socio.getNombre(),
                        socio.getApellidos(),
                        socio.getFecha_nacimiento().toString(),
                        socio.getDireccion(),
                        socio.getFecha_inscripcion().toString(),
                        socio.isEs_patron(),
                        socio.getId_inscripcion(),
                        socio.getRol().toString());
                if (result > 0) {
                    return true;
                } else {
                    return false;
                }
            } else
                return false;
        } catch (DataAccessException exception) {
            System.err.println("Incapaz de meter un Usuario en la base de datos");
            exception.printStackTrace();
            return false;
        }
    }

    public boolean existsByDni(String dni) {
        String sql = "SELECT COUNT(*) FROM Socio WHERE dni_socio = ?";
        Integer n = jdbcTemplate.queryForObject(sql, Integer.class, dni);
        return n != null && n > 0;
    }

    public boolean updateSocio(Socio socio) {
        try {
            String query = sqlQueries.getProperty("update-updateSocio");
            if (query != null) {
                int result = jdbcTemplate.update(query,
                        socio.getNombre(),
                        socio.getApellidos(),
                        socio.getFecha_nacimiento().toString(),
                        socio.getDireccion(),
                        socio.getFecha_inscripcion().toString(),
                        socio.isEs_patron(),
                        socio.getId_inscripcion(),
                        socio.getRol().toString(),
                        socio.getDni());
                if (result > 0) {
                    System.out.println("Socio actualizado correctamente: " + socio.getDni());
                    return true;
                }
                return true;
            } else {
                System.err.println("No se encontró socio con DNI " + socio.getDni() + " para actualizar.");
                return false;
            }

        } catch (DataAccessException e) {
            System.err.println("Error al actualizar socio con DNI=" + socio.getDni());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSocio(String dni) {
        try {
            String sqlTitular = "UPDATE Inscripcion SET dni_socio = NULL WHERE dni_socio = ?";
            jdbcTemplate.update(sqlTitular, dni);
            List<Integer> inscripciones = jdbcTemplate.queryForList(
                    "SELECT id_inscripcion FROM Socio WHERE dni_socio = ?",
                    Integer.class, dni);

            for (Integer id : inscripciones) {
                if (id != null)
                    desvincularSociosDeInscripcion(id);
            }
            String query = sqlQueries.getProperty("delete-deleteSocioByDni");
            if (query == null) {
                System.err.println("ERROR: Falta la clave 'delete-deleteSocioByDni' en el fichero SQL.");
                return false;
            }

            int result = jdbcTemplate.update(query, dni);

            if (result > 0) {
                System.out.println("Socio con DNI " + dni + " eliminado correctamente.");
                return true;
            } else {
                System.err.println("No se encontró el socio con DNI " + dni + " para eliminar.");
                return false;
            }

        } catch (DataAccessException exception) {
            System.err.println("Error al intentar eliminar el socio con DNI=" + dni);
            exception.printStackTrace();
            return false;
        }
    }

    public List<Socio> findByInscripcionId(int idInscripcion) {
        try {
            String query = sqlQueries.getProperty("select-findByInscripcionId");
            if (query != null) {
                return jdbcTemplate.query(query, new RowMapper<Socio>() {
                    @Override
                    public Socio mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Socio(
                                rs.getString("dni_socio"),
                                rs.getString("nombre"),
                                rs.getString("apellidos"),
                                Date.valueOf(rs.getString("fecha_nacimiento")).toLocalDate(),
                                rs.getString("direccion"),
                                Date.valueOf(rs.getString("fecha_inscripcion")).toLocalDate(),
                                rs.getBoolean("es_patron"),
                                rs.getInt("id_inscripcion"),
                                SocioRol.valueOf(rs.getString("rol")));
                    }
                }, idInscripcion);
            } else {
                System.err.println("No se encontró la query 'select-findByInscripcionId'");
                return java.util.Collections.emptyList();
            }
        } catch (DataAccessException e) {
            System.err.println("Error al buscar socios por inscripción ID=" + idInscripcion);
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public boolean desvincularSociosDeInscripcion(int idInscripcion) {
        try {
            String sql = sqlQueries.getProperty("update-desvincularSocio");
            int rows = jdbcTemplate.update(sql, idInscripcion);
            System.out.println("Socios desvinculados de la inscripción " + idInscripcion + ": " + rows);
            return true;
        } catch (DataAccessException e) {
            System.err.println("Error al desvincular socios de la inscripción " + idInscripcion);
            e.printStackTrace();
            return false;
        }
    }

    private void createProperties() {
        sqlQueries = new Properties();
        try {
            BufferedReader reader;
            File f = new File(sqlQueriesFileName);
            reader = new BufferedReader(new FileReader(f));
            sqlQueries.load(reader);
        } catch (IOException e) {
            System.err.println("Error creando propiedades objetos para las consultas SQL");
            e.printStackTrace();
        }
    }

}
