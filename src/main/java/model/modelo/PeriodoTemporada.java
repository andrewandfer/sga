package model.modelo;

import model.enums.Temporada;
import model.excepciones.FechasInvalidasException;

import java.time.LocalDate;
/**
 * Representa un periodo de fechas asociado a una temporada.
 * Permite consultar si una fecha pertenece al periodo y si se solapa con otro.
 */
public class PeriodoTemporada {

    private final LocalDate inicio;
    private final LocalDate fin;
    private final Temporada temporada;
    /**
     * Crea un periodo asociado a una temporada.
     *
     * @param inicio fecha de inicio del periodo
     * @param fin fecha de finalización del periodo
     * @param temporada temporada asociada al periodo
     * @throws FechasInvalidasException si alguno de los parámetros es nulo o la fecha final es anterior a la inicial
     */
    public PeriodoTemporada(LocalDate inicio, LocalDate fin, Temporada temporada) throws FechasInvalidasException {
        if (inicio == null || fin == null || temporada == null) {
            throw new FechasInvalidasException("Inicio, fin y temporada son obligatorios");
        }
        if (fin.isBefore(inicio)) {
            throw new FechasInvalidasException("La fecha fin del periodo no puede ser anterior al inicio");
        }
        this.inicio = inicio;
        this.fin = fin;
        this.temporada = temporada;
    }
    /**
     * Comprueba si una fecha está dentro del periodo, incluyendo sus extremos.
     *
     * @param fecha fecha que se desea comprobar
     * @return {@code true} si la fecha pertenece al periodo; {@code false} en caso contrario
     */
    public boolean contiene(LocalDate fecha) {
        return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }
    /**
     * Comprueba si este periodo comparte al menos una fecha con otro periodo.
     *
     * @param otro periodo con el que se compara
     * @return {@code true} si ambos periodos se solapan; {@code false} en caso contrario
     */
    public boolean seSolapaCon(PeriodoTemporada otro) {
        return !inicio.isAfter(otro.fin) && !otro.inicio.isAfter(fin);
    }
    /**
     * Obtiene la fecha de inicio del periodo.
     *
     * @return fecha de inicio
     */
    public LocalDate getInicio() {
        return inicio;
    }
    /**
     * Obtiene la fecha de finalización del periodo.
     *
     * @return fecha de finalización
     */
    public LocalDate getFin() {
        return fin;
    }
    /**
     * Obtiene la temporada asociada al periodo.
     *
     * @return temporada del periodo
     */
    public Temporada getTemporada() {
        return temporada;
    }

    /**
     * Devuelve la temporada y el intervalo de fechas del periodo.
     *
     * @return representación textual del periodo
     */
    @Override
    public String toString() {
        return temporada + " [" + inicio + " a " + fin + "]";
    }
}

