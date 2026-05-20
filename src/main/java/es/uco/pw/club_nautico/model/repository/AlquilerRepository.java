package es.uco.pw.club_nautico.model.repository;

import es.uco.pw.club_nautico.model.domain.Alquiler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Repository
public class AlquilerRepository {

    private final JdbcTemplate jdbcTemplate;
    private Properties sqlQueries;

    public AlquilerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        cargarSqlQueries();  
    }

    private void cargarSqlQueries() {
        sqlQueries = new Properties();
        try {
            ClassPathResource resource = new ClassPathResource("db/alquiler.properties");
            try (InputStream is = resource.getInputStream()) {
                sqlQueries.load(is);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error cargando db/alquiler.properties", e);
        }
    }

    private Alquiler mapRowToAlquiler(ResultSet row) throws SQLException {
        int id_alquiler = row.getInt("id_alquiler");
        Date fecha_inicio = row.getDate("fecha_inicio");
        Date fecha_fin = row.getDate("fecha_fin");
        int plazas_reservadas = row.getInt("plazas_reservadas");
        String matricula = row.getString("matricula");
        String dni_socio = row.getString("dni_socio");

        return new Alquiler(
                id_alquiler,
                fecha_inicio.toLocalDate(),
                fecha_fin.toLocalDate(),
                plazas_reservadas,
                matricula,
                dni_socio
        );
    }

    public List<Alquiler> findAll() {
        String sql = sqlQueries.getProperty("ALQUILER_SELECT_ALL");
        return jdbcTemplate.query(sql, rs -> {
            List<Alquiler> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapRowToAlquiler(rs));
            }
            return lista;
        });
    }

public int contarSolapes(String matricula,
                         LocalDate inicio,
                         LocalDate finExclusiva) {

    String sql = sqlQueries.getProperty("count-solapes");

    Integer count = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            matricula,
            java.sql.Date.valueOf(finExclusiva),
            java.sql.Date.valueOf(inicio)
    );

    return (count != null) ? count : 0;
}

    public long insertar(LocalDate inicio, LocalDate finExclusiva, int plazas,
                         String matricula, String dniSocio) {
                            
        String sql = sqlQueries.getProperty("insert");
        jdbcTemplate.update(
                sql,
                Timestamp.valueOf(inicio.atStartOfDay()),
                Timestamp.valueOf(finExclusiva.atStartOfDay()),
                plazas,
                matricula,
                dniSocio
        );
        return 0L;
    }
}
