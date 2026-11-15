package model.modelo;

import java.time.LocalDate;
import java.time.Period;
/**
 * Representa a una persona que se alojará en un apartamento.
 * Permite calcular su edad en una fecha determinada y
 * determinar si debe pagar el cargo por alojamiento.
 */
public class Ocupante {
    /**
     * Edad mínima a partir de la cual se cobra el cargo por alojamiento.
     */
    public static final int EDAD_MINIMA_PARA_PAGAR = 6;

    private final String nombre;
    private final LocalDate fechaNacimiento;

    /**
     * Construye un ocupante con su nombre y fecha de nacimiento.
     *
     * @param nombre nombre del ocupante
     * @param fechaNacimiento fecha de nacimiento del ocupante
     * @throws IllegalArgumentException si el nombre o la fecha de nacimiento
     *                                  son nulos o el nombre está vacío
     */
    public Ocupante(String nombre, LocalDate fechaNacimiento) {
        if (nombre == null || nombre.isBlank() || fechaNacimiento == null) {
            throw new IllegalArgumentException("Nombre y fecha de nacimiento son obligatorios");
        }
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
    }
    /**
     * Calcula la edad del ocupante en una fecha determinada.
     *
     * @param fecha fecha en la que se desea calcular la edad
     * @return edad del ocupante en años
     */
    public int edadEn(LocalDate fecha) {
        return Period.between(fechaNacimiento, fecha).getYears();
    }

    /**
     * Comprueba si el ocupante debe pagar el cargo correspondiente
     * según su edad durante una noche determinada.
     *
     * @param noche fecha de la noche que se desea evaluar
     * @return {@code true} si su edad es igual o superior a la edad mínima de pago
     */
    public boolean pagaCargo(LocalDate noche) {
        return edadEn(noche) >= EDAD_MINIMA_PARA_PAGAR;
    }
    /**
     * Obtiene el nombre del ocupante.
     *
     * @return nombre del ocupante
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene la fecha de nacimiento del ocupante.
     *
     * @return fecha de nacimiento
     */
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    /**
     * Devuelve una representación textual del ocupante con su nombre
     * y fecha de nacimiento.
     *
     * @return descripción del ocupante
     */
    @Override
    public String toString() {
        return nombre + " (" + fechaNacimiento + ")";
    }
}
