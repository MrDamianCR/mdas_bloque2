package es.uco.pw.club_nautico.controller;

import es.uco.pw.club_nautico.model.domain.Alquiler;
import es.uco.pw.club_nautico.model.domain.Embarcacion;
import es.uco.pw.club_nautico.model.repository.AlquilerRepository;
import es.uco.pw.club_nautico.model.repository.EmbarcacionRepository;
import es.uco.pw.club_nautico.service.AlquilerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/alquiler")
public class AlquilerController {

    private final AlquilerRepository alquilerRepo;
    private final EmbarcacionRepository embarcacionRepo;
    private final AlquilerService alquilerService;

    public AlquilerController(AlquilerRepository alquilerRepo, EmbarcacionRepository embarcacionRepo, AlquilerService alquilerService) {
        this.alquilerRepo = alquilerRepo;
        this.embarcacionRepo = embarcacionRepo;
        this.alquilerService = alquilerService;
    }

    @GetMapping("/listar")
    public String listar(Model model) {
        List<Alquiler> lista = alquilerRepo.findAll();
        model.addAttribute("alquileres", lista);
        return "lista_alquileres";
    }

    @GetMapping("/crear")
    public String mostrarFormularioCrear(
            @RequestParam(value = "inicioDisp", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicioDisp,
            @RequestParam(value = "finDisp", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate finDisp,
            Model model) {

        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("inicioDisp", inicioDisp);
        model.addAttribute("finDisp", finDisp);

        if (inicioDisp != null && finDisp != null) {
            List<Embarcacion> disponibles = alquilerService.buscarEmbarcacionesPorFecha(inicioDisp, finDisp);
            model.addAttribute("disponibles", disponibles);
        }

        return "formulario_alquiler";
    }

    @GetMapping("/nuevo")
    public String redirigirNuevoACrear() {
        return "redirect:/alquiler/crear";
    }

    @PostMapping("/crear")
    public String crearAlquiler(
            @RequestParam("inicio") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha_inicio,
            @RequestParam("fin")    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha_fin,
            @RequestParam("plazas") int plazas,
            @RequestParam("matricula") String matricula,
            @RequestParam("dniSocio") String dniSocio,
            RedirectAttributes redirectAttributes) {

        try {
            alquilerService.alquilar(matricula, dniSocio, plazas, fecha_inicio, fecha_fin);
            redirectAttributes.addFlashAttribute("ok", "Alquiler creado correctamente.");
            return "redirect:/alquiler/listar";

        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", "No se pudo realizar el alquiler: " + ex.getMessage());
            return "redirect:/alquiler/crear";
        } catch (Exception ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error interno creando el alquiler: " + ex.getMessage());
            return "redirect:/alquiler/crear";
        }
    }

    @GetMapping
    public String root() {
        return "redirect:/alquiler/listar";
    }
}