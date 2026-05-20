package es.uco.pw.club_nautico.controller;

import es.uco.pw.club_nautico.model.domain.Reserva;
import es.uco.pw.club_nautico.service.ActividadService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/actividades")
public class ActividadController {

    private final ActividadService actividadService;

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    /* ===== LISTAR ACTIVIDADES (GET /actividades/listar) ===== */
    @GetMapping("/listar")
    public String listar(Model model) {
        List<Reserva> reservas = actividadService.listarReservasFuturas();
        model.addAttribute("reservas", reservas);
        // -> src/main/resources/templates/lista_actividades.html
        return "lista_actividades";
    }

    /* ===== MOSTRAR FORMULARIO (GET /actividades/crear) ===== */
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("hoy", LocalDate.now());
        // -> src/main/resources/templates/formulario_actividad.html
        return "formulario_actividad";
    }

    /* Alias por si en algún sitio llamas a /actividades/nuevo */
    @GetMapping("/nuevo")
    public String redirigirNuevoACrear() {
        return "redirect:/actividades/crear";
    }

    /* ===== PROCESAR FORMULARIO (POST /actividades/crear) ===== */
    @PostMapping("/crear")
    public String crearActividad(
            // Mejora de codigo
            @RequestParam("matricula") String matricula,
            @RequestParam("dniSocio") String dniSocio,
            @RequestParam("plazas") int plazas,
            @RequestParam("fecha") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha,
            @RequestParam("descripcion") String descripcion,
            RedirectAttributes redirectAttributes) {
        try {
            actividadService.crearActividad(dniSocio, matricula, fecha, plazas, descripcion);
            redirectAttributes.addFlashAttribute("ok", "Actividad creada correctamente.");
            return "redirect:/actividades/listar";
        } catch (Exception ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error creando la actividad: " + ex.getMessage());
            return "redirect:/actividades/crear";
        }
    }

    /* ===== ROOT → REDIRIGE A LISTA ===== */
    @GetMapping
    public String root() {
        return "redirect:/actividades/listar";
    }
}
