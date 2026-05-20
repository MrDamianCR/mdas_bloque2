package es.uco.pw.club_nautico.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.uco.pw.club_nautico.model.repository.EmbarcacionRepository;
import es.uco.pw.club_nautico.model.domain.Alquiler;
import es.uco.pw.club_nautico.model.domain.Embarcacion;
import es.uco.pw.club_nautico.model.repository.AlquilerRepository;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



@Service
public class AlquilerService {

    private final EmbarcacionRepository embarcacionRepo;
    private final AlquilerRepository alquilerRepo;

    public AlquilerService(EmbarcacionRepository embarcacionRepo, AlquilerRepository alquilerRepo) {
        this.embarcacionRepo = embarcacionRepo;
        this.alquilerRepo = alquilerRepo;
    }



    // Método para buscar embarcaciones disponibles en un rango de fechas
    public List<Embarcacion> buscarEmbarcacionesPorFecha(LocalDate inicio, LocalDate fin) {

        System.out.println("[DEBUG] buscarEmbarcacionesPorFecha inicio=" + inicio + " fin=" + fin);

        LocalDate finExclusiva = fin.plusDays(1);

        // 1) Traemos todas las embarcaciones de BD
        List<Embarcacion> todas = embarcacionRepo.findAllEmbarcaciones();
        List<Embarcacion> disponibles = new ArrayList<>();

        // 2) Por cada embarcación, preguntamos cuántos solapes tiene
        for (Embarcacion e : todas) {
            int solapes = alquilerRepo.contarSolapes(
                    e.getMatricula(),    
                    inicio,
                    finExclusiva
            );

            if (solapes == 0) {
                disponibles.add(e);
            }
        }

        System.out.println("[DEBUG] embarcaciones disponibles: " + disponibles.size());
        return disponibles;
    }



    public List<Alquiler> listarAlquileresFuturos() {
    LocalDate hoy = LocalDate.now();
    return alquilerRepo.findAll().stream()
            .filter(a -> a.getFecha_fin().isAfter(hoy))
            .collect(Collectors.toList());
    }

    /** 
     * Crea un alquiler cumpliendo las reglas:
     * - Oct–Abr: exactamente 3 días
     * - May–Sep: exactamente 7 o 14 días
     * - Capacidad suficiente
     * - Sin solapes con otros alquileres
     * 
     * @param dniSocio          socio titular
     * @param matricula         matrícula de la embarcación
     * @param plazasDeseadas    plazas solicitadas (>0)
     * @param inicio            fecha inicio (inclusive)
     * @param finInclusiva      fecha fin (inclusive)
     * @return id generado del alquiler
     */
@Transactional
public void alquilar(String matricula, String dniSocio, int plazas,
                     LocalDate inicio, LocalDate fin) {

    LocalDate finExclusiva = fin.plusDays(1);

    validarDuracionPorTemporada(inicio, finExclusiva);

    var emb = embarcacionRepo.findEmbarcacionByMatricula(matricula);
    if (emb == null) throw new IllegalArgumentException("No existe la embarcación " + matricula);
    if (plazas <= 0 || plazas > emb.getNumeroPlazas())
        throw new IllegalArgumentException("Capacidad insuficiente");

    if (alquilerRepo.contarSolapes(matricula, inicio, finExclusiva) > 0)
        throw new IllegalStateException("No disponible en esas fechas");

    alquilerRepo.insertar(inicio, finExclusiva, plazas, matricula, dniSocio);
}




    private void validarDuracionPorTemporada(LocalDate inicio, LocalDate finExcl) {
        long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, finExcl);

        
        if (!mismaTemporada(inicio, finExcl.minusDays(1))) {
            throw new IllegalArgumentException("El rango cruza temporadas. Divide la reserva por temporada.");
        }

        int mes = inicio.getMonthValue();
        boolean baja = (mes >= 10 || mes <= 4); 
        if (baja) {
            if (dias != 3) {
                throw new IllegalArgumentException("De octubre a abril el alquiler debe ser de exactamente 3 días.");
            }
        } else { // May–Sep
            if (!(dias == 7 || dias == 14)) {
                throw new IllegalArgumentException("De mayo a septiembre solo se permiten 7 o 14 días.");
            }
        }
    }

    private boolean mismaTemporada(LocalDate a, LocalDate b) {
            return esBaja(a) == esBaja(b);
        }

        private boolean esBaja(LocalDate d) {
            int m = d.getMonthValue();
            return (m >= 10 || m <= 4); 
        }
    }



