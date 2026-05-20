package es.uco.pw.club_nautico.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.uco.pw.club_nautico.model.domain.Inscripcion.Inscripcion;
import es.uco.pw.club_nautico.model.domain.Inscripcion.InscripcionType;
import es.uco.pw.club_nautico.model.domain.Socio.Socio;
import es.uco.pw.club_nautico.model.repository.InscripcionRepository;
import es.uco.pw.club_nautico.model.repository.SocioRepository;
import es.uco.pw.club_nautico.service.CuotaService;

@Controller
@RequestMapping("/inscripciones")
public class InscripcionController {
    private final InscripcionRepository inscripcionRepository;
    private final SocioRepository socioRepository;
    private final CuotaService cuotaService;

    public InscripcionController(InscripcionRepository inscripcionRepository, SocioRepository socioRepository,
            CuotaService cuotaService) {
        this.inscripcionRepository = inscripcionRepository;
        this.socioRepository = socioRepository;
        this.cuotaService = cuotaService;
        String sqlQueriesFileName = "./src/main/resources/db/inscripcion.properties";
        this.inscripcionRepository.setSQLQueriesFileName(sqlQueriesFileName);
    }

    @ModelAttribute("numSociosPorInscripcion")
    public Map<Integer, Integer> cargarNumSociosPorInscripcion() {
        List<Inscripcion> inscripciones = inscripcionRepository.findAllInscripciones();
        Map<Integer, Integer> mapa = new HashMap<>();
        for (Inscripcion ins : inscripciones) {
            int id = ins.getId();
            int numSocios = socioRepository.findByInscripcionId(id).size();
            mapa.put(id, numSocios);
        }
        return mapa;
    }

    @GetMapping("/listar")
    public String listar(Model model) {
        List<Inscripcion> inscripciones = inscripcionRepository.findAllInscripciones();
        List<Socio> socios = socioRepository.findAllSocios();

        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("socios", socios);
        return "inscripciones";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") int id, Model model, RedirectAttributes ra) {
        Inscripcion inscripcion = inscripcionRepository.findInscripcionById(id);
        List<Socio> socios = socioRepository.findAllSocios();
        if (inscripcion == null) {
            ra.addFlashAttribute("error", "La inscripcion con id " + id + " no existe.");
            return "redirect:/inscripciones/listar";
        }
        model.addAttribute("inscripcion", inscripcion);
        model.addAttribute("socios", socios);
        return "editar_inscripcion";// templates/editar_inscripcion.html
    }

    @PostMapping("/nuevo")
    public String registrar(@ModelAttribute Inscripcion inscripcion,
            RedirectAttributes ra) {

        if (inscripcion.getDni_socio() != null && inscripcion.getDni_socio().isBlank()) {
            inscripcion.setDni_socio(null);
        }

        boolean ok = inscripcionRepository.addInscripcion(inscripcion);
        if (ok) {
            cuotaService.recalcularCuota(inscripcion.getId());
            ra.addFlashAttribute("mensaje", "Inscripción registrada correctamente.");
        } else {
            ra.addFlashAttribute("error", "No se pudo añadir la nueva inscripción al sistema.");
        }
        return "redirect:/inscripciones/listar";
    }

    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute("inscripcion") Inscripcion inscripcion, RedirectAttributes ra) {
        try {
            Inscripcion original = inscripcionRepository.findInscripcionById(inscripcion.getId());
            if (original == null) {
                ra.addFlashAttribute("error", "La inscripción con ID " + inscripcion.getId() + " no existe.");
                return "redirect:/inscripciones/listar";
            }
            if (inscripcion.getTipo() == InscripcionType.Individual) {
                inscripcion.setCuota(300.0);
                socioRepository.desvincularSociosDeInscripcion(inscripcion.getId());
            }
            boolean actualizado = inscripcionRepository.updateInscripcion(inscripcion);
            if (actualizado) {
                ra.addFlashAttribute("mensaje", "Inscripcion actualizado correctamente");
            } else
                ra.addFlashAttribute("error", "No se pudo actualizar la inscripcion con ID " + inscripcion.getId());
        } catch (Exception e) {
            System.err.println("Error al actualizar la inscripcion con ID=" + inscripcion.getId());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error interno al actualizar la inscripcion.");
        }

        return "redirect:/inscripciones/listar";
    }

    @PostMapping("/eliminar")
    public String elimina(@RequestParam("id") int id, RedirectAttributes ra) {
        boolean ok = inscripcionRepository.deleteInscripcionById(id);
        if (ok) {
            ra.addFlashAttribute("mensaje", "Inscripcion con ID " + id + " eliminada correctamente");
        } else {
            ra.addFlashAttribute("error", "No se pudo eliminar la inscripcion con ID " + id);
        }

        return "redirect:/inscripciones/listar";
    }

    @GetMapping("/buscar")
    public String buscarPorID(@RequestParam(name = "id", required = false) Integer id,
            Model model,
            RedirectAttributes ra) {
        if (id == null || id == 0) {
            ra.addFlashAttribute("error", "Introduce el ID para buscar");
            return "redirect:/inscripciones/listar";
        }

        var inscripcion = inscripcionRepository.findInscripcionById(id);
        if (inscripcion == null) {
            ra.addFlashAttribute("mensaje", "No se encontró ninguna inscripción con ID " + id);
            return "redirect:/inscripciones/listar";
        }
        model.addAttribute("inscripciones", java.util.List.of(inscripcion));
        return "inscripciones";
    }

    @GetMapping
    public String root() {
        return "redirect:/inscripciones/listar";
    }

}
