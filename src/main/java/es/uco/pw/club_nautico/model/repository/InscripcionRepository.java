package es.uco.pw.club_nautico.model.repository;

import es.uco.pw.club_nautico.model.domain.Inscripcion.Inscripcion;
import es.uco.pw.club_nautico.model.domain.Inscripcion.InscripcionType;

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
import java.util.Map;
import java.util.Properties;

@Repository
public class InscripcionRepository {

    private final JdbcTemplate jdbcTemplate;
    private Properties sqlQueries;
    private String sqlQueriesFileName;
    private final SocioRepository socioRepository;

    public InscripcionRepository(JdbcTemplate jdbcTemplate, SocioRepository socioRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.socioRepository = socioRepository;
    }

    /* Igual que en SocioRepository: establecer el .properties y cargarlo */
    public void setSQLQueriesFileName(String sqlQueriesFileName) {
        this.sqlQueriesFileName = sqlQueriesFileName;
        createProperties();
    }

    public List<Inscripcion> findAllInscripciones() {
        try {
            String query = sqlQueries.getProperty("select-findAllInscripciones");
            if (query != null) {
                List<Inscripcion> result = jdbcTemplate.query(query, new RowMapper<Inscripcion>() {
                    public Inscripcion mapRow(ResultSet rs, int rowNumber) throws SQLException {
                        return new Inscripcion(
                                rs.getInt("id_inscripcion"),
                                InscripcionType.valueOf(rs.getString("tipo")),
                                Date.valueOf(rs.getString("fecha_creacion")).toLocalDate(),
                                rs.getDouble("cuota"),
                                rs.getString("dni_socio"));
                    };
                });
                return result;
            } else {
                return null;
            }
        } catch (DataAccessException e) {
            System.err.println("Incapaz de encontrar Inscripciones");
            e.printStackTrace();
            return null;
        }
    }

    public Inscripcion findInscripcionById(int id) {
        try {
            String query = sqlQueries.getProperty("select-findInscripcionById");
            Inscripcion result = jdbcTemplate.query(query, this::mapRowTOInscripcion, id);
            if (result != null)
                return result;
            else
                return null;
        } catch (DataAccessException exception) {
            System.err.println("No se puede encontrar Inscripcion con ID=" + id);
            exception.printStackTrace();
            return null;
        }
    }

    public boolean addInscripcion(Inscripcion i) {
        try {
            String query = sqlQueries.getProperty("insert-addInscripcion");
            if (query != null) {
                int result = jdbcTemplate.update(query,
                        i.getTipo().toString(),
                        i.getFecha_creacion().toString());
                if (result > 0) {
                    return true;
                } else {
                    return false;
                }
            } else
                return false;

        } catch (DataAccessException e) {
            System.err.println("Incapaz de meter una Inscripcion en la base de datos");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateInscripcion(Inscripcion i) {
        try {
            String query = sqlQueries.getProperty("update-updateInscripcion");
            if (query != null) {
                int result = jdbcTemplate.update(query,
                        i.getTipo().toString(),
                        i.getDni_socio(),
                        i.getFecha_creacion().toString(),
                        i.getId());
                return result > 0;
            } else {
                return false;
            }

        } catch (DataAccessException e) {
            System.err.println("Error al actualizar inscripción con ID " + i.getId());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteInscripcionById(int id) {
        try {
            socioRepository.desvincularSociosDeInscripcion(id);

            String clearDniSql = "UPDATE Inscripcion SET dni_socio = NULL WHERE id_inscripcion = ?";
            jdbcTemplate.update(clearDniSql, id);
            String query = sqlQueries.getProperty("delete-deleteInscripcionById");
            int result = jdbcTemplate.update(query, id);
            if (result > 0) {
                System.out.println("Inscripción con ID " + id + " eliminada correctamente.");
                return true;
            } else {
                System.err.println("No se encontró la inscripción con ID " + id + " para eliminar.");
                return false;
            }
        } catch (DataAccessException e) {
            System.err.println("Error al intentar eliminar la inscripción con ID=" + id);
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> listarInscripciones() {
        try {
            String query = sqlQueries.getProperty("select-listarInscripciones");
            if (query != null) {
                List<Map<String, Object>> result = jdbcTemplate.queryForList(query);
                return result;
            } else {
                return null;
            }
        } catch (DataAccessException e) {
            System.err.println("Incapaz de listar Inscripciones");
            e.printStackTrace();
            return null;
        }
    }

    private Inscripcion mapRowTOInscripcion(ResultSet row) {
        try {
            if (row.first()) {
                int id = row.getInt("id_inscripcion");
                InscripcionType tipo = InscripcionType.valueOf(row.getString("tipo"));
                Date fecha_creacion = Date.valueOf(row.getString("fecha_creacion"));
                double cuota = row.getDouble("cuota");
                String dni_socio = row.getString("dni_socio");
                Inscripcion insc = new Inscripcion(id, tipo, fecha_creacion.toLocalDate(), cuota, dni_socio);
                return insc;
            } else
                return null;
        } catch (SQLException exception) {
            System.err.println("No se pueden recuperar resultados de la base de datos");
            exception.printStackTrace();
            return null;
        }
    }

    public boolean updateCuota(int idInscripcion, double nuevaCuota) {
        try {
            String query = sqlQueries.getProperty("update-updateCuota");
            if (query != null) {
                int rows = jdbcTemplate.update(query, nuevaCuota, idInscripcion);
                return rows > 0;
            } else {
                return false;
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCuotaYDniSocio(int idInscripcion, double cuota, String dniSocio) {
        try {
            String sql = sqlQueries.getProperty("update-cuotaDni");
            int rows = jdbcTemplate.update(sql, cuota, dniSocio, idInscripcion);
            return rows > 0;
        } catch (DataAccessException e) {
            System.err.println("Error al actualizar cuota y dni_socio de inscripción " + idInscripcion);
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
