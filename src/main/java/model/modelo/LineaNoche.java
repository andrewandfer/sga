package model.modelo;

import model.enums.Temporada;
import java.time.LocalDate;
/**
 * Representa el cobro correspondiente a una noche de alojamiento.
 * Almacena la fecha, temporada, cantidad de personas cobradas
 * y el valor de esa noche.
 */
public class LineaNoche {

    private final LocalDate noche;
    private final Temporada temporada;
    private final int personasCobradas;
    private final double valor;
    /**
     * Construye una línea de cobro para una noche de alojamiento.
     *
     * @param noche fecha de la noche cobrada
     * @param temporada temporada correspondiente a la noche
     * @param personasCobradas cantidad de personas cobradas
     * @param valor importe correspondiente a la noche
     */
    public LineaNoche(LocalDate noche, Temporada temporada, int personasCobradas, double valor) {
        this.noche = noche;
        this.temporada = temporada;
        this.personasCobradas = personasCobradas;
        this.valor = valor;
    }
    /**
     * Obtiene la fecha de la noche cobrada.
     *
     * @return fecha de la noche
     */
    public LocalDate getNoche() {
        return noche;
    }

    /**
     * Obtiene la temporada correspondiente a la noche.
     *
     * @return temporada de la noche
     */
    public Temporada getTemporada() {
        return temporada;
    }

    /**
     * Obtiene la cantidad de personas consideradas para el cobro.
     *
     * @return número de personas cobradas
     */
    public int getPersonasCobradas() {
        return personasCobradas;
    }
    /**
     * Obtiene el importe correspondiente a la noche.
     *
     * @return valor de la noche
     */
    public double getValor() {
        return valor;
    }

    /**
     * Devuelve una representación textual de la línea de cobro.
     *
     * @return fecha, temporada, cantidad de personas y valor
     */
    @Override
    public String toString() {
        return noche + " | " + temporada + " | " + personasCobradas + " pers. | " + valor;
    }
}