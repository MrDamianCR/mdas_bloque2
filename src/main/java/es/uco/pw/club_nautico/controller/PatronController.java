package es.uco.pw.club_nautico.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.uco.pw.club_nautico.model.domain.Patron;
import es.uco.pw.club_nautico.model.repository.PatronRepository;

@Controller
@RequestMapping("/patrones")
public class PatronController {

    private final PatronRepository patronRepository;

    public PatronController(PatronRepository patronRepository) {
        this.patronRepository = patronRepository;

        // Cargar fichero patrones.properties con control de errores
        try {
            String sqlQueriesFileName = "./src/main/resources/db/patrones.properties";
            this.patronRepository.setSQLQueriesFileName(sqlQueriesFileName);
        } catch (Exception e) {
            System.err.println("Error cargando consultas SQL de Patron");
            e.printStackTrace();
        }
    }

    @GetMapping("/listar")
    public String listar(Model model, RedirectAttributes ra) {
        try {
            List<Patron> patrones = patronRepository.findAllPatrones();
            if (patrones == null) {
                patrones = Collections.emptyList();
            }
            model.addAttribute("patrones", patrones);
        } catch (DataAccessException e) {
            System.err.println("Error al listar patrones");
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error al obtener la lista de patrones.");
            model.addAttribute("patrones", Collections.emptyList());
        }
        return "patrones"; // templates/patrones.html
    }

    @GetMapping("/editar/{dni}")
    public String mostrarFormularioEdicion(@PathVariable("dni") String dni,
            Model model,
            RedirectAttributes ra) {
        if (dni == null || dni.isBlank()) {
            ra.addFlashAttribute("error", "DNI no válido.");
            return "redirect:/patrones/listar";
        }

        try {
            Patron patron = patronRepository.findPatronByDni(dni);
            if (patron == null) {
                ra.addFlashAttribute("error", "El patrón con DNI " + dni + " no existe.");
                return "redirect:/patrones/listar";
            }
            model.addAttribute("patron", patron);
            return "editar_patron"; // templates/editar_patron.html
        } catch (DataAccessException e) {
            System.err.println("Error al buscar patrón con DNI=" + dni);
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error al cargar el patrón para editar.");
            return "redirect:/patrones/listar";
        }
    }

    @PostMapping("/registrar")
    public String registrar(@ModelAttribute Patron patron, RedirectAttributes ra) {
        if (patron == null || patron.getDni_patron() == null || patron.getDni_patron().isBlank()) {
            ra.addFlashAttribute("error", "El DNI del patrón es obligatorio.");
            return "redirect:/patrones/listar";
        }

        try {
            boolean ok = patronRepository.addPatron(patron);
            if (ok) {
                ra.addFlashAttribute("mensaje", "Patrón registrado correctamente.");
            } else {
                ra.addFlashAttribute("error", "No se pudo registrar el patrón (¿DNI duplicado o datos inválidos?).");
            }
        } catch (DataAccessException e) {
            System.err.println("Error al registrar el patrón con DNI=" + patron.getDni_patron());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error interno al registrar el patrón.");
        }

        return "redirect:/patrones/listar";
    }

    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute("patron") Patron patron, RedirectAttributes ra) {
        if (patron == null || patron.getDni_patron() == null || patron.getDni_patron().isBlank()) {
            ra.addFlashAttribute("error", "El DNI del patrón es obligatorio.");
            return "redirect:/patrones/listar";
        }

        try {
            boolean actualizado = patronRepository.updatePatron(patron);
            if (actualizado) {
                ra.addFlashAttribute("mensaje", "Patrón actualizado correctamente.");
            } else {
                ra.addFlashAttribute("error",
                        "No se pudo actualizar el patrón con DNI " + patron.getDni_patron());
            }
        } catch (DataAccessException e) {
            System.err.println("Error al actualizar el patrón con DNI=" + patron.getDni_patron());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error interno al actualizar el patrón.");
        }

        return "redirect:/patrones/listar";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam("dni") String dni, RedirectAttributes ra) {
        if (dni == null || dni.isBlank()) {
            ra.addFlashAttribute("error", "DNI no válido.");
            return "redirect:/patrones/listar";
        }

        try {
            Patron patron = patronRepository.findPatronByDni(dni);
            if (patron != null) {
                boolean ok = patronRepository.deletePatron(dni);
                if (ok) {
                    ra.addFlashAttribute("mensaje", "Patrón eliminado correctamente.");
                } else {
                    ra.addFlashAttribute("error",
                            "No se pudo eliminar el patrón con DNI: " + dni);
                }
            } else {
                ra.addFlashAttribute("error",
                        "No se encontró el patrón con DNI " + dni + " para eliminar.");
            }
        } catch (DataAccessException e) {
            System.err.println("Error al eliminar el patrón con DNI=" + dni);
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error interno al intentar eliminar el patrón.");
        }

        return "redirect:/patrones/listar";
    }

    @GetMapping("/buscar")
    public String buscarPorDni(@RequestParam(name = "dni", required = false) String dni,
            Model model,
            RedirectAttributes ra) {
        if (dni == null || dni.isBlank()) {
            ra.addFlashAttribute("error", "Introduce un DNI para buscar.");
            return "redirect:/patrones/listar";
        }

        try {
            Patron patron = patronRepository.findPatronByDni(dni);
            if (patron == null) {
                ra.addFlashAttribute("mensaje", "No se encontró ningún patrón con DNI " + dni + ".");
                return "redirect:/patrones/listar";
            }

            model.addAttribute("patrones", java.util.List.of(patron));
            return "patrones";
        } catch (DataAccessException e) {
            System.err.println("Error al buscar patrón con DNI=" + dni);
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error interno al buscar el patrón.");
            return "redirect:/patrones/listar";
        }
    }

    @GetMapping
    public String root() {
        return "redirect:/patrones/listar";
    }
}
