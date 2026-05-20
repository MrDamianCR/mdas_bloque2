package es.uco.pw.club_nautico.model.domain.Inscripcion;

import java.time.LocalDate;

public class Inscripcion {
    private Integer id;
    private InscripcionType tipo;
    private LocalDate fecha_creacion;
    private double cuota;
    private String dni_socio;

    
    public Inscripcion() {
    }

    public Inscripcion(Integer id, InscripcionType tipo, LocalDate fecha_creacion, double cuota, String dni_socio) {
        this.id = id;
        this.tipo = tipo;
        this.fecha_creacion = fecha_creacion;
        this.cuota = cuota;
        this.dni_socio = dni_socio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public InscripcionType getTipo() {
        return tipo;
    }

    public void setTipo(InscripcionType tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(LocalDate fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    public double getCuota() {
        return cuota;
    }

    public void setCuota(double cuota) {
        this.cuota = cuota;
    }

    public String getDni_socio() {
        return dni_socio;
    }

    public void setDni_socio(String dni_socio) {
        this.dni_socio = dni_socio;
    }

    

}
