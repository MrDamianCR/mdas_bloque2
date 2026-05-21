package es.uco.pw.club_nautico.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.uco.pw.club_nautico.model.domain.Patron;
import es.uco.pw.club_nautico.model.domain.Inscripcion.Inscripcion;
import es.uco.pw.club_nautico.model.domain.Inscripcion.InscripcionType;
import es.uco.pw.club_nautico.model.domain.Socio.Socio;
import es.uco.pw.club_nautico.model.domain.Socio.SocioRol;
import es.uco.pw.club_nautico.model.repository.SocioRepository;
import es.uco.pw.club_nautico.model.repository.InscripcionRepository;
import es.uco.pw.club_nautico.model.repository.PatronRepository;
import es.uco.pw.club_nautico.service.CuotaService;

@Controller
@RequestMapping("/socios")
public class SocioController {

    private final SocioRepository socioRepository;
    private final InscripcionRepository inscripcionRepository;
    private final PatronRepository patronRepository;
    private final CuotaService cuotaService;

    public SocioController(SocioRepository socioRepository, InscripcionRepository inscripcionRepository,
            CuotaService cuotaService, PatronRepository patronRepository) {
        this.socioRepository = socioRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.patronRepository = patronRepository;
        this.cuotaService = cuotaService;
        String sqlQueriesFileName = "./src/main/resources/db/socios.properties";
        this.socioRepository.setSQLQueriesFileName(sqlQueriesFileName);
    }

    @GetMapping("/listar")
    public String listar(Model model) {
        List<Socio> socios = socioRepository.findAllSocios();
        List<Inscripcion> inscripciones = inscripcionRepository.findAllInscripciones();
        model.addAttribute("socios", socios);
        model.addAttribute("inscripciones", inscripciones);
        return "socios";
    }

    @GetMapping("/editar/{dni}")
    public String mostrarFormularioEdicion(@PathVariable("dni") String dni, Model model, RedirectAttributes redirectAttributes) {
        Socio socio = socioRepository.findSocioByDni(dni);
        if (socio == null) {
            redirectAttributes.addFlashAttribute("error", "El socio con DNI " + dni + " no existe.");
            return "redirect:/socios/listar";
        }

        List<Inscripcion> inscripciones = inscripcionRepository.findAllInscripciones();
        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("socio", socio);
        return "editar_socio";
    }

    @PostMapping("/registrar")
    public String registrar(@ModelAttribute Socio socio, RedirectAttributes ra) {
        
        // 1. Llamamos al método extraído para validar las reglas de negocio
        String errorValidacion = validarReglasInscripcion(socio);
        if (errorValidacion != null) {
            ra.addFlashAttribute("error", errorValidacion);
            return "redirect:/socios/listar";
        }

        // 2. Si todo es correcto, registramos
        boolean ok = socioRepository.addSocio(socio);
        if (ok) {
            if (socio.getId_inscripcion() != null && socio.getRol() == SocioRol.Titular) {
                Inscripcion inscripcion = inscripcionRepository.findInscripcionById(socio.getId_inscripcion());
                if (inscripcion != null && (inscripcion.getDni_socio() == null || inscripcion.getDni_socio().isBlank())) {
                    inscripcion.setDni_socio(socio.getDni_socio());
                    inscripcionRepository.updateInscripcion(inscripcion);
                }
            }
            if (socio.isEs_patron()) {
                patronRepository.addPatron(new Patron(
                        socio.getDni_socio(), socio.getNombre(), socio.getApellidos(),
                        socio.getFecha_nacimiento(), socio.getFecha_inscripcion()));
            }

            cuotaService.recalcularCuota(socio.getId_inscripcion());
            ra.addFlashAttribute("mensaje", "Socio registrado correctamente.");
        } else {
            ra.addFlashAttribute("error", "No se pudo registrar el socio (¿DNI duplicado?).");
        }
        return "redirect:/socios/listar";
    }

    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute("socio") Socio socio, RedirectAttributes ra) {
        try {
            Socio socioBD = socioRepository.findSocioByDni(socio.getDni_socio());
            boolean eraPatron = (socioBD != null && socioBD.isEs_patron());

            // 1. Llamamos al método extraído para validar las reglas de negocio
            String errorValidacion = validarReglasInscripcion(socio);
            if (errorValidacion != null) {
                ra.addFlashAttribute("error", errorValidacion);
                return "redirect:/socios/listar";
            }

            // 2. Actualizamos
            boolean actualizado = socioRepository.updateSocio(socio);
            if (actualizado) {
                if (socio.getId_inscripcion() != null && socio.getRol() == SocioRol.Titular) {
                    Inscripcion inscripcion = inscripcionRepository.findInscripcionById(socio.getId_inscripcion());
                    if (inscripcion != null && (inscripcion.getDni_socio() == null || inscripcion.getDni_socio().isBlank())) {
                        inscripcion.setDni_socio(socio.getDni_socio());
                        inscripcionRepository.updateInscripcion(inscripcion);
                    }
                }   
  
                boolean esPatronAhora = socio.isEs_patron();
                if (!eraPatron && esPatronAhora) {
                    patronRepository.addPatron(new Patron(socio.getDni_socio(), socio.getNombre(), socio.getApellidos(), socio.getFecha_nacimiento(), socio.getFecha_inscripcion()));
                } else if (eraPatron && !esPatronAhora) {
                    patronRepository.deletePatron(socio.getDni_socio());
                } else if (eraPatron && esPatronAhora) {
                    patronRepository.updatePatron(new Patron(socio.getDni_socio(), socio.getNombre(), socio.getApellidos(), socio.getFecha_nacimiento(), socio.getFecha_inscripcion()));
                }

                cuotaService.recalcularCuota(socio.getId_inscripcion());
                ra.addFlashAttribute("mensaje", "Socio actualizado correctamente.");
            } else {
                ra.addFlashAttribute("error", "No se pudo actualizar el socio con DNI " + socio.getDni_socio());
            }
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error interno al actualizar el socio.");
        }
        return "redirect:/socios/listar";
    }

    private String validarReglasInscripcion(Socio socio) {
        Integer idInscripcion = socio.getId_inscripcion();
        if (idInscripcion == null) return null;
        Inscripcion inscripcion = inscripcionRepository.findInscripcionById(idInscripcion);
        if (inscripcion == null) {
            return "La inscripción con ID " + idInscripcion + " no existe.";
        }
        List<Socio> sociosInscripcion = socioRepository.findByInscripcionId(idInscripcion);
        // Validar inscripción individual
        if (inscripcion.getTipo() == InscripcionType.Individual) {
            boolean yaHayOtro = sociosInscripcion.stream()
                    .anyMatch(s -> !s.getDni_socio().equals(socio.getDni_socio()));
            if (yaHayOtro) {
                return "La inscripción " + idInscripcion + " es INDIVIDUAL y ya tiene un socio asociado.";
            }
        }
        // Validar Titular único
        if (socio.getRol() == SocioRol.Titular) {
            boolean otroTitular = sociosInscripcion.stream()
                    .anyMatch(s -> s.getRol() == SocioRol.Titular && !s.getDni_socio().equals(socio.getDni_socio()));
            if (otroTitular) {
                return "La inscripción " + idInscripcion + " ya tiene un socio TITULAR. No se pueden añadir más.";
            }
        }

        if (socio.getRol() == SocioRol.Hijo || socio.getRol() == SocioRol.Adulto_Adicional) {
            boolean hayTitular = sociosInscripcion.stream().anyMatch(s -> s.getRol() == SocioRol.Titular);
            if (!hayTitular) {
                return "La inscripción " + idInscripcion + " aún no tiene Titular. Primero debes dar de alta un TITULAR.";
            }
        }
        
        return null;
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam("dni") String dni, RedirectAttributes redirectAttributes) {
        Socio socio = socioRepository.findSocioByDni(dni);
        if (socio != null) {
            boolean ok = socioRepository.deleteSocio(dni);
            if (ok) {
                cuotaService.recalcularCuota(socio.getId_inscripcion());
                redirectAttributes.addFlashAttribute("mensaje", "Socio eliminado correctamente.");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el socio con DNI: " + dni);
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el socio con DNI " + dni);
        }
        return "redirect:/socios/listar";
    }

    @GetMapping("/patrones")
    public String listarPatrones(Model model, RedirectAttributes redirectAttributes) {
        var lista = socioRepository.findSociosPatron();
        if (lista == null || lista.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "No hay socios con rol Patrón.");
            return "redirect:/socios/listar";
        }
        model.addAttribute("socios", lista);
        return "socios";
    }

    @GetMapping("/buscar")
    public String buscarPorDni(@RequestParam(name = "dni", required = false) String dni,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (dni == null || dni.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Introduce un DNI para buscar.");
            return "redirect:/socios/listar";
        }
        var socio = socioRepository.findSocioByDni(dni);
        if (socio == null) {
            redirectAttributes.addFlashAttribute("mensaje", "No se encontró ningún socio con DNI " + dni + ".");
            return "redirect:/socios/listar";
        }

        model.addAttribute("socios", java.util.List.of(socio));
        return "socios";
    }

    @GetMapping
    public String root() {
        return "redirect:/socios/listar";
    }

}