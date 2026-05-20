package es.uco.pw.club_nautico.model.domain;

import java.time.LocalDate;

public class Patron {
    private String dni_patron;
    private String nombre;
    private String apellidos;
    private LocalDate fecha_nacimiento;
    private LocalDate fecha_exp_patron;

    // Constructor
    public Patron(String dni_patron, String nombre, String apellidos, LocalDate fecha_nacimiento,
            LocalDate fecha_exp_patron) {
        this.dni_patron = dni_patron;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fecha_nacimiento = fecha_nacimiento;
        this.fecha_exp_patron = fecha_exp_patron;
    }

    // Getters y Setters
    public String getDni_patron() {
        return dni_patron;
    }

    public void setDni_patron(String dni_patron) {
        this.dni_patron = dni_patron;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public LocalDate getFecha_nacimiento() {
        return fecha_nacimiento;
    }

    public void setFecha_nacimiento(LocalDate fecha_nacimiento) {
        this.fecha_nacimiento = fecha_nacimiento;
    }

    public LocalDate getFecha_exp_patron() {
        return fecha_exp_patron;
    }

    public void setFecha_exp_patron(LocalDate fecha_exp_patron) {
        this.fecha_exp_patron = fecha_exp_patron;
    }
}
