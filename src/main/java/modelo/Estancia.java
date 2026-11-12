package modelo;


import excepciones.FechasInvalidasException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class Estancia implements Iterable<LocalDate> {

    private final LocalDate entrada;
    private final LocalDate salida;

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

    public LocalDate getEntrada() {
        return entrada;
    }

    public LocalDate getSalida() {
        return salida;
    }

    public int noches() {
        return (int) ChronoUnit.DAYS.between(entrada, salida);
    }

    public boolean seSolapaCon(Estancia otra) {
        return entrada.isBefore(otra.salida) && otra.entrada.isBefore(salida);
    }

    public boolean incluyeNoche(LocalDate noche) {
        return !noche.isBefore(entrada) && noche.isBefore(salida);
    }

    @Override
    public Iterator<LocalDate> iterator() {
        return new IteradorNoches();
    }

    @Override
    public String toString() {
        return entrada + " -> " + salida + " (" + noches() + " noches)";
    }

    private class IteradorNoches implements Iterator<LocalDate> {
        private LocalDate actual = entrada;

        @Override
        public boolean hasNext() {
            return actual.isBefore(salida);
        }

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
