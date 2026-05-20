package es.uco.pw.club_nautico.model.repository;

import es.uco.pw.club_nautico.model.domain.Reserva;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Repository
public class ReservaRepository {

    private final JdbcTemplate jdbcTemplate;
    private Properties sqlQueries;

    public ReservaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        cargarSqlQueries();
    }

    private void cargarSqlQueries() {
        sqlQueries = new Properties();
        try {
            ClassPathResource resource = new ClassPathResource("db/reserva.properties");
            try (InputStream is = resource.getInputStream()) {
                sqlQueries.load(is);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error cargando db/reserva.properties", e);
        }
    }

    private Reserva mapRowToReserva(ResultSet row) throws SQLException {
        int id = row.getInt("id_reserva");
        Timestamp fechaTs = row.getTimestamp("fecha");
        LocalDateTime fecha = (fechaTs != null) ? fechaTs.toLocalDateTime() : null;
        int plazas_reservadas = row.getInt("plazas_reservadas");
        String descripcion = row.getString("descripcion");
        float precio_total = row.getFloat("precio_total");
        String dni_socio = row.getString("dni_socio");
        String matricula = row.getString("matricula");

        return new Reserva(
                id,
                fecha,
                plazas_reservadas,
                descripcion,
                precio_total,
                dni_socio,
                matricula
        );
    }

    public List<Reserva> findAll() {
        String sql = sqlQueries.getProperty("select-all");
        return jdbcTemplate.query(sql, rs -> {
            List<Reserva> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapRowToReserva(rs));
            }
            return lista;
        });
    }

    public int countByBoatAndDate(String matricula, LocalDate fecha) {
        String sql = sqlQueries.getProperty("count-by-boat-and-date");

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                matricula,
                java.sql.Date.valueOf(fecha)
        );

        return (count != null) ? count : 0;
    }

    public long insertar(Reserva reserva) {
        String sql = sqlQueries.getProperty("insert");
        jdbcTemplate.update(
                sql,
                Timestamp.valueOf(reserva.getFecha()),
                reserva.getPlazas_reservadas(),
                reserva.getDescripcion(),
                reserva.getPrecio_total(),
                reserva.getDni_socio(),
                reserva.getMatricula()
        );
        return 0L;
    }
}
