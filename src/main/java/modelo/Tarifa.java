package modelo;


import enums.Temporada;

public class Tarifa {

    private final Temporada temporada;
    private double valorPersonaNoche;

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

    public Temporada getTemporada() {
        return temporada;
    }

    public double getValorPersonaNoche() {
        return valorPersonaNoche;
    }

    public void setValorPersonaNoche(double valorPersonaNoche) {
        if (valorPersonaNoche < 0) {
            throw new IllegalArgumentException("La tarifa no puede ser negativa");
        }
        this.valorPersonaNoche = valorPersonaNoche;
    }

    @Override
    public String toString() {
        return temporada + ": " + valorPersonaNoche;
    }
}