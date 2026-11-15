package model.modelo;


import model.enums.Temporada;
/**
 * Representa la tarifa por persona y noche correspondiente a una temporada.
 * Permite consultar y modificar el valor de la tarifa.
 */
public class Tarifa {

    private final Temporada temporada;
    private double valorPersonaNoche;
    /**
     * Crea una tarifa para una temporada con un valor por persona y noche.
     *
     * @param temporada temporada a la que corresponde la tarifa
     * @param valorPersonaNoche valor de la tarifa por persona y noche
     * @throws IllegalArgumentException si la temporada es nula o el valor es negativo
     */
    public Tarifa(Temporada temporada, double valorPersonaNoche) {
        if (temporada == null) {
            throw new IllegalArgumentException("La temporada es obligatoria");
        }
        if (valorPersonaNoche < 0) {
            throw new IllegalArgumentException("La tarifa no puede ser negativa");
        }
        this.temporada = temporada;
        this.valorPersonaNoche = valorPersonaNoche;
    }

    /**
     * Obtiene la temporada asociada a la tarifa.
     *
     * @return temporada de la tarifa
     */
    public Temporada getTemporada() {
        return temporada;
    }
    /**
     * Obtiene el valor de la tarifa por persona y noche.
     *
     * @return valor por persona y noche
     */
    public double getValorPersonaNoche() {
        return valorPersonaNoche;
    }

    /**
     * Modifica el valor de la tarifa por persona y noche.
     *
     * @param valorPersonaNoche nuevo valor de la tarifa
     * @throws IllegalArgumentException si el valor es negativo
     */
    public void setValorPersonaNoche(double valorPersonaNoche) {
        if (valorPersonaNoche < 0) {
            throw new IllegalArgumentException("La tarifa no puede ser negativa");
        }
        this.valorPersonaNoche = valorPersonaNoche;
    }
    /**
     * Devuelve la temporada y el valor de la tarifa.
     *
     * @return representación textual de la tarifa
     */
    @Override
    public String toString() {
        return temporada + ": " + valorPersonaNoche;
    }
}