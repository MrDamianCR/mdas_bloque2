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

    /**
     * @param dniSocio      DNI del socio que solicita la actividad
     * @param matricula     matrícula de la embarcación
     * @param fechaActividad fecha de la actividad (solo día)
     * @param plazas        plazas solicitadas (personas, sin contar al patrón)
     * @param descripcion   descripción corta de la actividad
     */
    @Transactional
    public void crearActividad(String dniSocio,
                               String matricula,
                               LocalDate fechaActividad,
                               int plazas,
                               String descripcion) {

        // 1) Validar socio y mayoría de edad
        Socio socio = socioRepository.findSocioByDni(dniSocio);
        if (socio == null) {
            throw new IllegalArgumentException("No existe el socio con DNI " + dniSocio);
        }
        if (!esMayorDeEdadEnFecha(socio, fechaActividad)) {
            throw new IllegalArgumentException("El socio debe ser mayor de edad en la fecha de la actividad.");
        }

        // 2) Validar embarcación y patrón
        Embarcacion embarcacion = embarcacionRepository.findEmbarcacionByMatricula(matricula);
        if (embarcacion == null) {
            throw new IllegalArgumentException("No existe la embarcación con matrícula " + matricula);
        }
        String dniPatron = embarcacion.getDni_patron();
        if (dniPatron == null || dniPatron.isBlank()) {
            throw new IllegalStateException("La embarcación no tiene patrón asignado.");
        }

        // 3) Comprobar plazas (recordar que el patrón ocupa una plaza)
        int capacidadTotal = embarcacion.getNumeroPlazas();
        int capacidadSocios = capacidadTotal - 1; // una plaza es para el patrón empleado

        if (capacidadSocios <= 0) {
            throw new IllegalStateException("La embarcación no admite plazas para actividades.");
        }
        if (plazas <= 0 || plazas > capacidadSocios) {
            throw new IllegalArgumentException(
                    "Número de plazas inválido. Máximo permitido para esta actividad: " + capacidadSocios
            );
        }

        // 4) Comprobar que el barco está libre ese día (alquileres)
        LocalDate inicio = fechaActividad;
        LocalDate finExclusiva = fechaActividad.plusDays(1);
        int solapesAlquiler = alquilerRepository.contarSolapes(matricula, inicio, finExclusiva);
        if (solapesAlquiler > 0) {
            throw new IllegalStateException("La embarcación está alquilada ese día.");
        }

        // 5) Comprobar que no hay otra actividad ese mismo día
        int reservasMismoDia = reservaRepository.countByBoatAndDate(matricula, fechaActividad);
        if (reservasMismoDia > 0) {
            throw new IllegalStateException("Ya existe una actividad para esa embarcación en esa fecha.");
        }

        // 6) Calcular precio total (40 € por persona para el evento completo)
        float precioTotal = plazas * 40.0f;

        // 7) Crear Reserva y guardarla
        LocalDateTime fechaConHora = fechaActividad.atStartOfDay();
        Reserva reserva = new Reserva(
                0,               // id (lo gestionará la BD si es autoincremental)
                fechaConHora,
                plazas,
                descripcion,
                precioTotal,
                dniSocio,
                matricula
        );

        reservaRepository.insertar(reserva);
    }

    /**
     * Lista solo las reservas futuras (útil para un listado de próximas actividades).
     */
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
