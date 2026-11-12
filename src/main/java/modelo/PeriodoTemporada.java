package modelo;

import enums.Temporada;
import excepciones.FechasInvalidasException;

import java.time.LocalDate;

public class PeriodoTemporada {

    private final LocalDate inicio;
    private final LocalDate fin;
    private final Temporada temporada;

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

    public boolean contiene(LocalDate fecha) {
        return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }

    public boolean seSolapaCon(PeriodoTemporada otro) {
        return !inicio.isAfter(otro.fin) && !otro.inicio.isAfter(fin);
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFin() {
        return fin;
    }

    public Temporada getTemporada() {
        return temporada;
    }

    @Override
    public String toString() {
        return temporada + " [" + inicio + " a " + fin + "]";
    }
}

