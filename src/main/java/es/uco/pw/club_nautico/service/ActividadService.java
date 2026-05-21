package es.uco.pw.club_nautico.service;

import es.uco.pw.club_nautico.model.domain.Embarcacion;
import es.uco.pw.club_nautico.model.domain.Reserva;
import es.uco.pw.club_nautico.model.domain.Socio.Socio;
import es.uco.pw.club_nautico.model.repository.AlquilerRepository;
import es.uco.pw.club_nautico.model.repository.EmbarcacionRepository;
import es.uco.pw.club_nautico.model.repository.ReservaRepository;
import es.uco.pw.club_nautico.model.repository.SocioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActividadService {

    private static final float PRECIO_POR_PERSONA_ACTIVIDAD = 40.0f;

    private final SocioRepository socioRepository;
    private final EmbarcacionRepository embarcacionRepository;
    private final AlquilerRepository alquilerRepository;
    private final ReservaRepository reservaRepository;

    public ActividadService(SocioRepository socioRepository,
                            EmbarcacionRepository embarcacionRepository,
                            AlquilerRepository alquilerRepository,
                            ReservaRepository reservaRepository) {
        this.socioRepository = socioRepository;
        this.embarcacionRepository = embarcacionRepository;
        this.alquilerRepository = alquilerRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional
    public void crearActividad(String dniSocio, String matricula, LocalDate fechaActividad, int plazas, String descripcion) {

        Socio socio = socioRepository.findSocioByDni(dniSocio);
        if (socio == null) {
            throw new IllegalArgumentException("No existe el socio con DNI " + dniSocio);
        }
        if (!esMayorDeEdadEnFecha(socio, fechaActividad)) {
            throw new IllegalArgumentException("El socio debe ser mayor de edad en la fecha de la actividad.");
        }

        Embarcacion embarcacion = embarcacionRepository.findEmbarcacionByMatricula(matricula);
        if (embarcacion == null) {
            throw new IllegalArgumentException("No existe la embarcación con matrícula " + matricula);
        }
        
        String dniPatron = embarcacion.getDni_patron();
        if (dniPatron == null || dniPatron.isBlank()) {
            throw new IllegalStateException("La embarcación no tiene patrón asignado.");
        }

        int capacidadTotal = embarcacion.getNumeroPlazas();
        int capacidadSocios = capacidadTotal - 1; 

        if (capacidadSocios <= 0) {
            throw new IllegalStateException("La embarcación no admite plazas para actividades.");
        }
        if (plazas <= 0 || plazas > capacidadSocios) {
            throw new IllegalArgumentException("Número de plazas inválido. Máximo permitido: " + capacidadSocios);
        }

        LocalDate inicio = fechaActividad;
        LocalDate finExclusiva = fechaActividad.plusDays(1);
        int solapesAlquiler = alquilerRepository.contarSolapes(matricula, inicio, finExclusiva);
        if (solapesAlquiler > 0) {
            throw new IllegalStateException("La embarcación está alquilada ese día.");
        }

        int reservasMismoDia = reservaRepository.countByBoatAndDate(matricula, fechaActividad);
        if (reservasMismoDia > 0) {
            throw new IllegalStateException("Ya existe una actividad para esa embarcación en esa fecha.");
        }

        float precioTotal = plazas * PRECIO_POR_PERSONA_ACTIVIDAD;

        LocalDateTime fechaConHora = fechaActividad.atStartOfDay();
        Reserva reserva = new Reserva(0, fechaConHora, plazas, descripcion, precioTotal, dniSocio, matricula);
        reservaRepository.insertar(reserva);
    }

    public List<Reserva> listarReservasFuturas() {
        LocalDate hoy = LocalDate.now();
        return reservaRepository.findAll().stream()
                .filter(r -> r.getFecha() != null && !r.getFecha().toLocalDate().isBefore(hoy))
                .collect(Collectors.toList());
    }

    private boolean esMayorDeEdadEnFecha(Socio socio, LocalDate fechaReferencia) {
        if (socio.getFecha_nacimiento() == null) {
            return false;
        }
        return Period.between(socio.getFecha_nacimiento(), fechaReferencia).getYears() >= 18;
    }
}