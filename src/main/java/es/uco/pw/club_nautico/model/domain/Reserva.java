package es.uco.pw.club_nautico.model.domain;

import java.time.LocalDateTime;

public class Reserva {
    private int id;
    private LocalDateTime fecha;
    private int plazas_reservadas;
    private String descripcion;
    private float precio_total;
    private String dni_socio;
    private String matricula;

    public Reserva(int id, LocalDateTime fecha, int plazas_reservadas, String descripcion, float precio_total,
            String dni_socio, String matricula) {
        this.id = id;
        this.fecha = fecha;
        this.plazas_reservadas = plazas_reservadas;
        this.descripcion = descripcion;
        this.precio_total = precio_total;
        this.dni_socio = dni_socio;
        this.matricula = matricula;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public int getPlazas_reservadas() {
        return plazas_reservadas;
    }

    public void setPlazas_reservadas(int plazas_reservadas) {
        this.plazas_reservadas = plazas_reservadas;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public float getPrecio_total() {
        return precio_total;
    }

    public void setPrecio_total(float precio_total) {
        this.precio_total = precio_total;
    }

    public String getDni_socio() {
        return dni_socio;
    }

    public void setDni_socio(String dni_socio) {
        this.dni_socio = dni_socio;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

}
