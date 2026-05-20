package es.uco.pw.club_nautico.model.domain.Socio;

import java.time.LocalDate;

public class Socio {
    private String dni_socio;
    private String nombre;
    private String apellidos;
    private LocalDate fecha_nacimiento;
    private String direccion;
    private LocalDate fecha_inscripcion;
    private Boolean es_patron;
    private Integer id_inscripcion;
    private SocioRol rol;

    public Socio(String dni_socio, String nombre, String apellidos, LocalDate fecha_nacimiento, String direccion,
            LocalDate fecha_inscripcion, Boolean es_patron, Integer id_inscripcion, SocioRol rol) {
        this.dni_socio = dni_socio;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fecha_nacimiento = fecha_nacimiento;
        this.direccion = direccion;
        this.fecha_inscripcion = fecha_inscripcion;
        this.es_patron = es_patron;
        this.id_inscripcion = id_inscripcion;
        this.rol = rol;
    }

    public String getDni() {
        return dni_socio;
    }

    public void setDni(String dni) {
        this.dni_socio = dni;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDate getFecha_inscripcion() {
        return fecha_inscripcion;
    }

    public void setFecha_inscripcion(LocalDate fecha_inscripcion) {
        this.fecha_inscripcion = fecha_inscripcion;
    }

    public Boolean isEs_patron() {
        return es_patron;
    }

    public Boolean getEs_patron() {
        return es_patron;
    }

    public void setEs_patron(Boolean es_patron) {
        this.es_patron = es_patron;
    }

    public String getDni_socio() {
        return dni_socio;
    }

    public void setDni_socio(String dni_socio) {
        this.dni_socio = dni_socio;
    }

    public Integer getId_inscripcion() {
        return id_inscripcion;
    }

    public void setId_inscripcion(Integer id_inscripcion) {
        this.id_inscripcion = id_inscripcion;
    }

    public SocioRol getRol() {
        return rol;
    }

    public void setRol(SocioRol rol) {
        this.rol = rol;
    }

}
