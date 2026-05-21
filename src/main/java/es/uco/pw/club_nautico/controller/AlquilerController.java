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

// En este archivo se han hecho cambios en los comentarios, se han eliminado unos y modificado otros.

@Controller
@RequestMapping("/alquiler")
public class AlquilerController {

    private final AlquilerRepository alquilerRepo;
    private final EmbarcacionRepository embarcacionRepo;
    private final AlquilerService alquilerService;

<<<<<<< Updated upstream
    public AlquilerController(AlquilerRepository alquilerRepo, EmbarcacionRepository embarcacionRepo, AlquilerService alquilerService) {
=======
    public AlquilerController(AlquilerRepository alquilerRepo,
            EmbarcacionRepository embarcacionRepo,
            AlquilerService alquilerService) {
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
=======
            System.out.println("[DEBUG] disponibles.size() = " + disponibles.size());
>>>>>>> Stashed changes
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
            @RequestParam("fin") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha_fin,
            @RequestParam("plazas") int plazas,
            @RequestParam("matricula") String matricula,
            @RequestParam("dniSocio") String dniSocio,
            RedirectAttributes redirectAttributes) {

        try {
<<<<<<< Updated upstream
            alquilerService.alquilar(matricula, dniSocio, plazas, fecha_inicio, fecha_fin);
            redirectAttributes.addFlashAttribute("ok", "Alquiler creado correctamente.");
=======
            // fin exclusiva
            LocalDate finExclusiva = fecha_fin.plusDays(1);

            // Comprueba si se solapan las fechas con otro alquiler
            int solapes = alquilerRepo.contarSolapes(matricula, fecha_inicio, fecha_fin);
            if (solapes > 0) {
                ra.addFlashAttribute("error",
                        "La embarcación con matrícula " + matricula +
                                " ya está reservada en las fechas seleccionadas.");
                return "redirect:/alquiler/crear";
            }

            // Comprueba que no se pase del numero de plazas
            Embarcacion embarcacion = embarcacionRepo.findEmbarcacionByMatricula(matricula);
            if (plazas > embarcacion.getNumeroPlazas()) {
                ra.addFlashAttribute("error",
                        "La embarcación solo tiene " + embarcacion.getNumeroPlazas() +
                                " plazas, no se pueden reservar " + plazas + ".");
                return "redirect:/alquiler/crear";
            }

            alquilerRepo.insertar(
                    fecha_inicio,
                    finExclusiva,
                    plazas,
                    matricula,
                    dniSocio);

            ra.addFlashAttribute("ok", "Alquiler creado correctamente.");
            // después de crear, vamos a la lista
>>>>>>> Stashed changes
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