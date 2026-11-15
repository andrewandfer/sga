package model.modelo;


import model.excepciones.FechasInvalidasException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Representa el periodo de alojamiento de una reserva,
 * definido por una fecha de entrada y una fecha de salida.
 * Permite consultar las noches de la estancia e iterar sobre ellas.
 */
public class Estancia implements Iterable<LocalDate> {

    private final LocalDate entrada;
    private final LocalDate salida;
    /**
     * Construye una estancia a partir de las fechas de entrada y salida.
     *
     * @param entrada fecha de inicio de la estancia
     * @param salida fecha de finalización de la estancia
     * @throws FechasInvalidasException si alguna fecha es nula o la salida
     *                                  no es posterior a la entrada
     */
    public Estancia(LocalDate entrada, LocalDate salida) throws FechasInvalidasException {
        if (entrada == null || salida == null) {
            throw new FechasInvalidasException("Las fechas de entrada y salida son obligatorias");
        }
        if (!salida.isAfter(entrada)) {
            throw new FechasInvalidasException("La salida debe ser posterior a la entrada (minimo una noche)");
        }
        this.entrada = entrada;
        this.salida = salida;
    }
    /**
     * Obtiene la fecha de entrada de la estancia.
     *
     * @return fecha de entrada
     */
    public LocalDate getEntrada() {
        return entrada;
    }
    /**
     * Obtiene la fecha de salida de la estancia.
     *
     * @return fecha de salida
     */
    public LocalDate getSalida() {
        return salida;
    }
    /**
     * Calcula la cantidad de noches comprendidas en la estancia.
     *
     * @return número de noches
     */
    public int noches() {
        return (int) ChronoUnit.DAYS.between(entrada, salida);
    }
    /**
     * Comprueba si esta estancia se solapa con otra.
     *
     * @param otra estancia con la que se desea comparar
     * @return {@code true} si ambas estancias comparten al menos una noche
     */
    public boolean seSolapaCon(Estancia otra) {
        return entrada.isBefore(otra.salida) && otra.entrada.isBefore(salida);
    }
    /**
     * Comprueba si una fecha corresponde a una noche incluida en la estancia.
     * La fecha de entrada está incluida, pero la fecha de salida no.
     *
     * @param noche fecha que se desea consultar
     * @return {@code true} si la fecha pertenece al periodo de alojamiento
     */
    public boolean incluyeNoche(LocalDate noche) {
        return !noche.isBefore(entrada) && noche.isBefore(salida);
    }

    /**
     * Obtiene un iterador para recorrer las noches de la estancia.
     *
     * @return iterador de fechas desde la entrada hasta el día anterior a la salida
     */
    @Override
    public Iterator<LocalDate> iterator() {
        return new IteradorNoches();
    }
    /**
     * Devuelve una representación textual de la estancia con sus fechas
     * y la cantidad de noches.
     *
     * @return descripción de la estancia
     */
    @Override
    public String toString() {
        return entrada + " -> " + salida + " (" + noches() + " noches)";
    }

    /**
     * Iterador que recorre las noches de una estancia en orden cronológico.
     */
    private class IteradorNoches implements Iterator<LocalDate> {
        private LocalDate actual = entrada;
        /**
         * Comprueba si quedan noches por recorrer.
         *
         * @return {@code true} si la fecha actual es anterior a la salida
         */
        @Override
        public boolean hasNext() {
            return actual.isBefore(salida);
        }
        /**
         * Obtiene la noche actual y avanza a la siguiente.
         *
         * @return fecha de la noche actual
         * @throws NoSuchElementException si no quedan noches por recorrer
         */
        @Override
        public LocalDate next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No hay mas noches en la estancia");
            }
            LocalDate noche = actual;
            actual = actual.plusDays(1);
            return noche;
        }
    }
}
