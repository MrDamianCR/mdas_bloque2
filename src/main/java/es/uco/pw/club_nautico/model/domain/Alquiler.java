package es.uco.pw.club_nautico.model.domain;

import java.time.LocalDate;

public class Alquiler {
    private int id;
    private LocalDate fehca_inicio;
    private LocalDate fecha_fin;
    private int plazas_reservadas;
    private String matricula;
    private String dni_socio;

    public Alquiler(int id, LocalDate fehca_inicio, LocalDate fecha_fin, int plazas_reservadas, String matricula,
            String dni_socio) {
        this.id = id;
        this.fehca_inicio = fehca_inicio;
        this.fecha_fin = fecha_fin;
        this.plazas_reservadas = plazas_reservadas;
        this.matricula = matricula;
        this.dni_socio = dni_socio;
    }

    private Embarcacion embarcacion;

    public Embarcacion getEmbarcacion()
    {
        return embarcacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getFehca_inicio() {
        return fehca_inicio;
    }

    public void setFehca_inicio(LocalDate fehca_inicio) {
        this.fehca_inicio = fehca_inicio;
    }

    public LocalDate getFecha_fin() {
        return fecha_fin;
    }

    public void setFecha_fin(LocalDate fecha_fin) {
        this.fecha_fin = fecha_fin;
    }

    public int getPlazas_reservadas() {
        return plazas_reservadas;
    }

    public void setPlazas_reservadas(int plazas_reservadas) {
        this.plazas_reservadas = plazas_reservadas;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getDni_socio() {
        return dni_socio;
    }

    public void setDni_socio(String dni_socio) {
        this.dni_socio = dni_socio;
    }


}
