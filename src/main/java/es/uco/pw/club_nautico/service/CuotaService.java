package es.uco.pw.club_nautico.service;

import org.springframework.stereotype.Service;

import es.uco.pw.club_nautico.model.repository.InscripcionRepository;
import es.uco.pw.club_nautico.model.repository.SocioRepository;
import es.uco.pw.club_nautico.model.domain.Inscripcion.Inscripcion;
import es.uco.pw.club_nautico.model.domain.Inscripcion.InscripcionType;
import es.uco.pw.club_nautico.model.domain.Socio.*;

import java.util.List;

@Service
public class CuotaService {

    private final SocioRepository socioRepository;
    private final InscripcionRepository inscripcionRepository;

    public CuotaService(SocioRepository socioRepository, InscripcionRepository inscripcionRepository) {
        this.socioRepository = socioRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    public void recalcularCuota(Integer idInscripcion) {
        if (idInscripcion == null) return;

        List<Socio> socios = socioRepository.findByInscripcionId(idInscripcion);

        long numTitulares = socios.stream().filter(s -> s.getRol() == SocioRol.Titular).count();
        long numAdultosAdicionales = socios.stream().filter(s -> s.getRol() == SocioRol.Adulto_Adicional).count();
        long numHijos = socios.stream().filter(s -> s.getRol() == SocioRol.Hijo).count();

        if (numTitulares == 0) {
            inscripcionRepository.updateCuotaYDniSocio(idInscripcion, 0.0, null);
            return;
        }

        double cuota = 0.0;
        Inscripcion inscripcion = inscripcionRepository.findInscripcionById(idInscripcion);
        
        if (inscripcion != null && inscripcion.getTipo() == InscripcionType.Individual) {
            cuota = 300.0;
        } else {
            cuota += 300.0;
            if (numAdultosAdicionales >= 1) {
                cuota += 250.0;
            }
            cuota += numHijos * 100.0;
        }

        String dniTitular = socios.stream()
                .filter(s -> s.getRol() == SocioRol.Titular)
                .map(Socio::getDni_socio)
                .findFirst()
                .orElse(null);

        inscripcionRepository.updateCuotaYDniSocio(idInscripcion, cuota, dniTitular);
    }
}