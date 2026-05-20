package es.uco.pw.club_nautico.model.repository;

import es.uco.pw.club_nautico.model.domain.Embarcacion;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Repository
public class EmbarcacionRepository {

    private final JdbcTemplate jdbcTemplate;
    private Properties sqlQueries;
    private String sqlQueriesFileName;

    public EmbarcacionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setSQLQueriesFileName(String sqlQueriesFileName) {
        this.sqlQueriesFileName = sqlQueriesFileName;
        createProperties();
    }

    /*
     * 
     * CONSULTAS BÁSICAS
     * 
     */

    public List<Embarcacion> findAllEmbarcaciones() {
        try {
            String query = sqlQueries.getProperty("select-findAllEmbarcaciones");
            if (query != null) {
                List<Embarcacion> result = jdbcTemplate.query(query, new RowMapper<Embarcacion>() {
                    @Override
                    public Embarcacion mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Embarcacion(
                                rs.getString("matricula"),
                                rs.getString("tipo"),
                                rs.getString("nombre"),
                                rs.getInt("numero_plazas"),
                                rs.getString("dimensiones"),
                                rs.getString("dni_patron"));
                    }
                });
                return result;
            } else {
                return null;
            }
        } catch (DataAccessException ex) {
            System.err.println("Incapaz de encontrar Embarcaciones");
            ex.printStackTrace();
            return null;
        }
    }

    public Embarcacion findEmbarcacionByMatricula(String matricula) {
        try {
            String query = sqlQueries.getProperty("select-findEmbarcacionByMatricula");
            Embarcacion result = jdbcTemplate.query(query, this::mapRowToEmbarcacion, matricula);
            if (result != null) {
                return result;
            } else {
                return null;
            }
        } catch (DataAccessException ex) {
            System.err.println("No se puede encontrar Embarcacion con matrícula=" + matricula);
            ex.printStackTrace();
            return null;
        }
    }

    public List<Embarcacion> findEmbarcacionesByTipo(String tipo) {
        try {
            String query = sqlQueries.getProperty("select-findEmbarcacionesByTipo");
            if (query != null) {
                return jdbcTemplate.query(query, (rs, rowNum) -> new Embarcacion(
                        rs.getString("matricula"),
                        rs.getString("tipo"),
                        rs.getString("nombre"),
                        rs.getInt("numero_plazas"),
                        rs.getString("dimensiones"),
                        rs.getString("dni_patron")), tipo);
            } else {
                System.err.println("No se encontró la query 'select-findEmbarcacionesByTipo'");
                return Collections.emptyList();
            }
        } catch (DataAccessException ex) {
            System.err.println("Error al buscar embarcaciones por tipo: " + tipo);
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    private Embarcacion mapRowToEmbarcacion(ResultSet row) throws SQLException {
        try {
            if (row.first()) {
                String matricula = row.getString("matricula");
                String tipo = row.getString("tipo");
                String nombre = row.getString("nombre");
                int numeroPlazas = row.getInt("numero_plazas");
                String dimensiones = row.getString("dimensiones");
                String dniPatron = row.getString("dni_patron");

                return new Embarcacion(matricula, tipo, nombre, numeroPlazas, dimensiones, dniPatron);
            } else {
                return null;
            }
        } catch (SQLException ex) {
            System.err.println("No se pueden recuperar resultados de la base de datos (Embarcacion)");
            ex.printStackTrace();
            return null;
        }
    }

    public List<Embarcacion> findDisponiblesEntreFechas(LocalDate inicio, LocalDate finExclusiva) {

        String sql = "SELECT e.* " +
                "FROM Embarcacion e " +
                "WHERE NOT EXISTS ( " +
                "   SELECT 1 " +
                "   FROM Alquiler a " +
                "   WHERE a.matricula = e.matricula " +
                "     AND a.fecha_inicio < ? " +
                "     AND a.fecha_fin    > ? " +
                ")";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapRowToEmbarcacion(rs),
                java.sql.Date.valueOf(finExclusiva),
                java.sql.Date.valueOf(inicio));
    }

    public List<Embarcacion> findEmbarcacionesByDniPatron(String dniPatron) {
        try {
            String query = sqlQueries.getProperty("select-findEmbarcacionesByDniPatron");
            if (query != null) {
                return jdbcTemplate.query(query, (rs, rowNum) -> new Embarcacion(
                        rs.getString("matricula"),
                        rs.getString("tipo"),
                        rs.getString("nombre"),
                        rs.getInt("numero_plazas"),
                        rs.getString("dimensiones"),
                        rs.getString("dni_patron")), dniPatron);
            } else {
                System.err.println("No se encontró la query 'select-findEmbarcacionesByDniPatron'");
                return Collections.emptyList();
            }
        } catch (DataAccessException ex) {
            System.err.println("Error al buscar embarcaciones del patrón con DNI " + dniPatron);
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    /*
     * 
     * INSERTAR
     * 
     */

    public boolean addEmbarcacion(Embarcacion embarcacion) {
        if (existsByMatricula(embarcacion.getMatricula())) {
            System.err.println("La embarcación ya existe");
            return false;
        }

        try {
            String query = sqlQueries.getProperty("insert-addEmbarcacion");
            if (query != null) {
                int result = jdbcTemplate.update(query,
                        embarcacion.getMatricula(),
                        embarcacion.getTipo(),
                        embarcacion.getNombre(),
                        embarcacion.getNumeroPlazas(),
                        embarcacion.getDimensiones(),
                        embarcacion.getDni_patron());

                return result > 0;
            } else {
                return false;
            }
        } catch (DataAccessException ex) {
            System.err.println("Incapaz de meter una Embarcación en la base de datos");
            ex.printStackTrace();
            return false;
        }
    }

    public boolean existsByMatricula(String matricula) {
        String sql = "SELECT COUNT(*) FROM Embarcacion WHERE matricula = ?";
        Integer n = jdbcTemplate.queryForObject(sql, Integer.class, matricula);
        return n != null && n > 0;
    }

    /*
     * 
     * ACTUALIZAR
     * 
     */

    public boolean updateEmbarcacion(Embarcacion embarcacion) {
        try {
            String query = sqlQueries.getProperty("update-updateEmbarcacion");
            if (query != null) {
                int result = jdbcTemplate.update(query,
                        embarcacion.getTipo(),
                        embarcacion.getNombre(),
                        embarcacion.getNumeroPlazas(),
                        embarcacion.getDimensiones(),
                        embarcacion.getDni_patron(),
                        embarcacion.getMatricula());

                if (result > 0) {
                    System.out.println("Embarcación actualizada correctamente: " + embarcacion.getMatricula());
                    return true;
                }
                return false;
            } else {
                System.err.println("No se encontró embarcación con matrícula "
                        + embarcacion.getMatricula() + " para actualizar.");
                return false;
            }

        } catch (DataAccessException ex) {
            System.err.println("Error al actualizar embarcación con matrícula=" + embarcacion.getMatricula());
            ex.printStackTrace();
            return false;
        }
    }

    /*
     * 
     * ELIMINAR
     * 
     */

    public boolean deleteEmbarcacion(String matricula) {
        try {
            String query = sqlQueries.getProperty("delete-deleteEmbarcacionByMatricula");
            int result = jdbcTemplate.update(query, matricula);

            if (result > 0) {
                System.out.println("Embarcación con matrícula " + matricula + " eliminada correctamente.");
                return true;
            } else {
                System.err.println("No se encontró la embarcación con matrícula "
                        + matricula + " para eliminar.");
                return false;
            }

        } catch (DataAccessException ex) {
            System.err.println("Error al intentar eliminar la embarcación con matrícula=" + matricula);
            ex.printStackTrace();
            return false;
        }
    }

    /*
     * 
     * PROPIEDADES (fichero .properties)
     * 
     */

    private void createProperties() {
        sqlQueries = new Properties();
        try {
            BufferedReader reader;
            File f = new File(sqlQueriesFileName);
            reader = new BufferedReader(new FileReader(f));
            sqlQueries.load(reader);
        } catch (IOException e) {
            System.err.println("Error creando propiedades objetos para las consultas SQL (Embarcacion)");
            e.printStackTrace();
        }
    }
}
