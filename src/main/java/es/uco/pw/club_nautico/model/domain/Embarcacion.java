package es.uco.pw.club_nautico.model.domain;

public class Embarcacion {
    private String matricula;
    private String tipo;
    private String nombre;
    private int numeroPlazas;
    private String dimensiones;
    private String dni_patron;

    public Embarcacion(String matricula, String tipo, String nombre, int numeroPlazas, String dimensiones,
            String dni_patron) {
        this.matricula = matricula;
        this.tipo = tipo;
        this.nombre = nombre;
        this.numeroPlazas = numeroPlazas;
        this.dimensiones = dimensiones;
        this.dni_patron = dni_patron;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNumeroPlazas() {
        return numeroPlazas;
    }

    public void setNumeroPlazas(int numeroPlazas) {
        this.numeroPlazas = numeroPlazas;
    }

    public String getDimensiones() {
        return dimensiones;
    }

    public void setDimensiones(String dimensiones) {
        this.dimensiones = dimensiones;
    }

    public String getDni_patron() {
        return dni_patron;
    }

    public void setDni_patron(String dni_patron) {
        this.dni_patron = dni_patron;
    }

}
