package es.uco.pw.club_nautico.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.uco.pw.club_nautico.model.domain.Embarcacion;
import es.uco.pw.club_nautico.model.domain.Patron;
import es.uco.pw.club_nautico.model.repository.EmbarcacionRepository;
import es.uco.pw.club_nautico.model.repository.PatronRepository;

// En este archivo se han hecho cambios en los comentarios, se han eliminado unos y modificado otros.

@Controller
@RequestMapping("/embarcaciones")
public class EmbarcacionController {

    private final EmbarcacionRepository embarcacionRepository;
    private final PatronRepository patronRepository;

    public EmbarcacionController(EmbarcacionRepository embarcacionRepository,
            PatronRepository patronRepository) {
        this.embarcacionRepository = embarcacionRepository;
        this.patronRepository = patronRepository;

        // Carga de los .properties con control de errores básico
        try {
            String sqlQueriesFileNameEmbarcaciones = "./src/main/resources/db/embarcacion.properties";
            this.embarcacionRepository.setSQLQueriesFileName(sqlQueriesFileNameEmbarcaciones);
        } catch (Exception e) {
            System.err.println("Error cargando consultas SQL de Embarcacion");
            e.printStackTrace();
        }

        try {
            String sqlQueriesFileNamePatrones = "./src/main/resources/db/patrones.properties";
            this.patronRepository.setSQLQueriesFileName(sqlQueriesFileNamePatrones);
        } catch (Exception e) {
            System.err.println("Error cargando consultas SQL de Patron");
            e.printStackTrace();
        }
    }

    @GetMapping("/listar")
    public String listar(Model model, RedirectAttributes ra) {
        try {
            List<Embarcacion> embarcaciones = embarcacionRepository.findAllEmbarcaciones();
            if (embarcaciones == null) {
                embarcaciones = Collections.emptyList();
            }
            model.addAttribute("embarcaciones", embarcaciones);
        } catch (DataAccessException e) {
            System.err.println("Error al listar embarcaciones");
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error al obtener la lista de embarcaciones.");
            model.addAttribute("embarcaciones", Collections.emptyList());
        }
        return "embarcaciones";
    }

    @GetMapping("/editar/{matricula}")
    public String mostrarFormularioEdicion(@PathVariable("matricula") String matricula,
            Model model,
            RedirectAttributes ra) {
        if (matricula == null || matricula.isBlank()) {
            ra.addFlashAttribute("error", "Matrícula no válida.");
            return "redirect:/embarcaciones/listar";
        }

        try {
            Embarcacion embarcacion = embarcacionRepository.findEmbarcacionByMatricula(matricula);
            if (embarcacion == null) {
                ra.addFlashAttribute("error", "La embarcación con matrícula " + matricula + " no existe.");
                return "redirect:/embarcaciones/listar";
            }
            model.addAttribute("embarcacion", embarcacion);
            return "editar_embarcacion";
        } catch (DataAccessException e) {
            System.err.println("Error al buscar embarcación con matrícula=" + matricula);
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error al cargar la embarcación para editar.");
            return "redirect:/embarcaciones/listar";
        }
    }

    @PostMapping("/registrar")
    public String registrar(@ModelAttribute Embarcacion embarcacion, RedirectAttributes ra) {
        if (embarcacion == null || embarcacion.getMatricula() == null
                || embarcacion.getMatricula().isBlank()) {
            ra.addFlashAttribute("error", "La matrícula de la embarcación es obligatoria.");
            return "redirect:/embarcaciones/listar";
        }

        try {
            boolean ok = embarcacionRepository.addEmbarcacion(embarcacion);
            if (ok) {
                ra.addFlashAttribute("mensaje", "Embarcación registrada correctamente.");
            } else {
                ra.addFlashAttribute("error",
                        "No se pudo registrar la embarcación (¿matrícula duplicada o datos inválidos?).");
            }
        } catch (DataAccessException e) {
            System.err.println("Error al registrar embarcación con matrícula=" + embarcacion.getMatricula());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error interno al registrar la embarcación.");
        }

        return "redirect:/embarcaciones/listar";
    }

    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute("embarcacion") Embarcacion embarcacion,
            RedirectAttributes ra) {
        if (embarcacion == null || embarcacion.getMatricula() == null
                || embarcacion.getMatricula().isBlank()) {
            ra.addFlashAttribute("error", "La matrícula de la embarcación es obligatoria.");
            return "redirect:/embarcaciones/listar";
        }

        try {
            boolean actualizado = embarcacionRepository.updateEmbarcacion(embarcacion);
            if (actualizado) {
                ra.addFlashAttribute("mensaje", "Embarcación actualizada correctamente.");
            } else {
                ra.addFlashAttribute("error",
                        "No se pudo actualizar la embarcación con matrícula " + embarcacion.getMatricula());
            }
        } catch (DataAccessException e) {
            System.err.println("Error al actualizar la embarcación con matrícula=" + embarcacion.getMatricula());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error interno al actualizar la embarcación.");
        }

        return "redirect:/embarcaciones/listar";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam("matricula") String matricula, RedirectAttributes ra) {
        if (matricula == null || matricula.isBlank()) {
            ra.addFlashAttribute("error", "Matrícula no válida.");
            return "redirect:/embarcaciones/listar";
        }

        try {
            Embarcacion embarcacion = embarcacionRepository.findEmbarcacionByMatricula(matricula);
            if (embarcacion != null) {
                boolean ok = embarcacionRepository.deleteEmbarcacion(matricula);
                if (ok) {
                    ra.addFlashAttribute("mensaje", "Embarcación eliminada correctamente.");
                } else {
                    ra.addFlashAttribute("error",
                            "No se pudo eliminar la embarcación con matrícula: " + matricula);
                }
            } else {
                ra.addFlashAttribute("error",
                        "No se encontró la embarcación con matrícula " + matricula + " para eliminar.");
            }
        } catch (DataAccessException e) {
            System.err.println("Error al eliminar embarcación con matrícula=" + matricula);
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error interno al intentar eliminar la embarcación.");
        }

        return "redirect:/embarcaciones/listar";
    }

    @GetMapping("/buscar")
    public String buscarPorMatricula(@RequestParam(name = "matricula", required = false) String matricula,
            Model model,
            RedirectAttributes ra) {
        if (matricula == null || matricula.isBlank()) {
            ra.addFlashAttribute("error", "Introduce una matrícula para buscar.");
            return "redirect:/embarcaciones/listar";
        }

        try {
            Embarcacion embarcacion = embarcacionRepository.findEmbarcacionByMatricula(matricula);
            if (embarcacion == null) {
                ra.addFlashAttribute("mensaje",
                        "No se encontró ninguna embarcación con matrícula " + matricula + ".");
                return "redirect:/embarcaciones/listar";
            }

            model.addAttribute("embarcaciones", java.util.List.of(embarcacion));
            return "embarcaciones";
        } catch (DataAccessException e) {
            System.err.println("Error al buscar embarcación por matrícula=" + matricula);
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error interno al buscar la embarcación.");
            return "redirect:/embarcaciones/listar";
        }
    }

    @GetMapping("/buscar-tipo")
    public String buscarPorTipo(@RequestParam(name = "tipo", required = false) String tipo,
            Model model,
            RedirectAttributes ra) {
        if (tipo == null || tipo.isBlank()) {
            ra.addFlashAttribute("error", "Introduce un tipo de embarcación para buscar.");
            return "redirect:/embarcaciones/listar";
        }

        try {
            List<Embarcacion> lista = embarcacionRepository.findEmbarcacionesByTipo(tipo);
            if (lista == null || lista.isEmpty()) {
                ra.addFlashAttribute("mensaje", "No se encontraron embarcaciones de tipo " + tipo + ".");
                return "redirect:/embarcaciones/listar";
            }

            model.addAttribute("embarcaciones", lista);
            return "embarcaciones";
        } catch (DataAccessException e) {
            System.err.println("Error al buscar embarcaciones por tipo=" + tipo);
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error interno al buscar por tipo.");
            return "redirect:/embarcaciones/listar";
        }
    }

    // Mostrar formulario para asignar / cambiar patron
    @GetMapping("/asignar-patron/{matricula}")
    public String mostrarFormularioAsignarPatron(@PathVariable("matricula") String matricula,
            Model model,
            RedirectAttributes ra) {
        if (matricula == null || matricula.isBlank()) {
            ra.addFlashAttribute("error", "Matrícula no válida.");
            return "redirect:/embarcaciones/listar";
        }

        try {
            Embarcacion embarcacion = embarcacionRepository.findEmbarcacionByMatricula(matricula);
            if (embarcacion == null) {
                ra.addFlashAttribute("error", "La embarcación con matrícula " + matricula + " no existe.");
                return "redirect:/embarcaciones/listar";
            }

            List<Patron> patronesLibres = patronRepository.findPatronesLibres();
            if (patronesLibres == null) {
                patronesLibres = Collections.emptyList();
            }

            model.addAttribute("embarcacion", embarcacion);
            model.addAttribute("patronesLibres", patronesLibres);
            return "asignar_patron_embarcacion";
        } catch (DataAccessException e) {
            System.err.println("Error al cargar datos para asignar patrón a la embarcación " + matricula);
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error interno al cargar los datos de asignación de patrón.");
            return "redirect:/embarcaciones/listar";
        }
    }

    // Procesar asignacion / cambio de patron
    @PostMapping("/asignar-patron")
    public String asignarPatron(@RequestParam("matricula") String matricula,
            @RequestParam("dniPatronNuevo") String dniPatronNuevo,
            @RequestParam(value = "confirmarReemplazo", required = false) Boolean confirmarReemplazo,
            RedirectAttributes ra) {

        if (matricula == null || matricula.isBlank() ||
                dniPatronNuevo == null || dniPatronNuevo.isBlank()) {
            ra.addFlashAttribute("error", "Matrícula y DNI del patrón son obligatorios.");
            return "redirect:/embarcaciones/listar";
        }

        try {
            Embarcacion embarcacion = embarcacionRepository.findEmbarcacionByMatricula(matricula);
            if (embarcacion == null) {
                ra.addFlashAttribute("error", "La embarcación con matrícula " + matricula + " no existe.");
                return "redirect:/embarcaciones/listar";
            }

            String dniPatronActual = embarcacion.getDni_patron(); // puede ser null

            // Si ya tiene patrón y no se ha confirmado el reemplazo, pedir confirmación
            if (dniPatronActual != null && (confirmarReemplazo == null || !confirmarReemplazo)) {
                ra.addFlashAttribute("error",
                        "La embarcación ya tiene asignado el patrón con DNI " + dniPatronActual +
                                ". Marca la casilla de confirmación para sustituirlo.");
                return "redirect:/embarcaciones/asignar-patron/" + matricula;
            }

            // Comprobar que el patrón nuevo NO está asignado a ninguna otra embarcación
            List<Embarcacion> embarcacionesDelPatron = embarcacionRepository
                    .findEmbarcacionesByDniPatron(dniPatronNuevo);

            if (embarcacionesDelPatron != null && !embarcacionesDelPatron.isEmpty()) {
                ra.addFlashAttribute("error",
                        "El patrón con DNI " + dniPatronNuevo +
                                " ya está asignado a otra embarcación. Debes elegir un patrón libre.");
                return "redirect:/embarcaciones/asignar-patron/" + matricula;
            }

            // Asignar patrón nuevo
            embarcacion.setDni_patron(dniPatronNuevo);
            boolean ok = embarcacionRepository.updateEmbarcacion(embarcacion);

            if (ok) {
                if (dniPatronActual != null) {
                    ra.addFlashAttribute("mensaje",
                            "Patrón cambiado correctamente. El patrón anterior (" + dniPatronActual +
                                    ") queda libre para ser asociado a otra embarcación.");
                } else {
                    ra.addFlashAttribute("mensaje",
                            "Patrón asignado correctamente a la embarcación " + matricula + ".");
                }
            } else {
                ra.addFlashAttribute("error",
                        "No se pudo asignar el patrón a la embarcación " + matricula + ".");
            }

        } catch (DataAccessException e) {
            System.err.println("Error al asignar patrón " + dniPatronNuevo +
                    " a la embarcación " + matricula);
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error interno al asignar el patrón.");
        }

        return "redirect:/embarcaciones/listar";
    }

    @GetMapping
    public String root() {
        return "redirect:/embarcaciones/listar";
    }
}
