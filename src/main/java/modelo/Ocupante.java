package modelo;

import java.time.LocalDate;
import java.time.Period;

public class Ocupante {

    public static final int EDAD_MINIMA_PARA_PAGAR = 6;

    private final String nombre;
    private final LocalDate fechaNacimiento;

    public Ocupante(String nombre, LocalDate fechaNacimiento) {
        if (nombre == null || nombre.isBlank() || fechaNacimiento == null) {
            throw new IllegalArgumentException("Nombre y fecha de nacimiento son obligatorios");
        }
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
    }

    public int edadEn(LocalDate fecha) {
        return Period.between(fechaNacimiento, fecha).getYears();
    }

    public boolean pagaCargo(LocalDate noche) {
        return edadEn(noche) >= EDAD_MINIMA_PARA_PAGAR;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    @Override
    public String toString() {
        return nombre + " (" + fechaNacimiento + ")";
    }
}
