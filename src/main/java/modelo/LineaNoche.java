package modelo;

import enums.Temporada;
import java.time.LocalDate;

public class LineaNoche {

    private final LocalDate noche;
    private final Temporada temporada;
    private final int personasCobradas;
    private final double valor;

    public LineaNoche(LocalDate noche, Temporada temporada, int personasCobradas, double valor) {
        this.noche = noche;
        this.temporada = temporada;
        this.personasCobradas = personasCobradas;
        this.valor = valor;
    }

    public LocalDate getNoche() {
        return noche;
    }

    public Temporada getTemporada() {
        return temporada;
    }

    public int getPersonasCobradas() {
        return personasCobradas;
    }

    public double getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return noche + " | " + temporada + " | " + personasCobradas + " pers. | " + valor;
    }
}