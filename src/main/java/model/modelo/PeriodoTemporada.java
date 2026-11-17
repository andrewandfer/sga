package model.modelo;

import model.enums.Temporada;
import model.excepciones.FechasInvalidasException;
import java.time.LocalDate;
import java.time.Month;

/** Periodo anual recurrente de una temporada, definido sin año calendario. */
public class PeriodoTemporada {
    private final Month mesInicio;
    private final Month mesFin;
    private final Temporada temporada;

    public PeriodoTemporada(Month mesInicio, Month mesFin, Temporada temporada) throws FechasInvalidasException {
        if (mesInicio == null || mesFin == null || temporada == null)
            throw new FechasInvalidasException("Mes inicial, mes final y temporada son obligatorios");
        this.mesInicio = mesInicio;
        this.mesFin = mesFin;
        this.temporada = temporada;
    }

    /** Compatibilidad con datos anteriores: se conservan únicamente los meses. */
    public PeriodoTemporada(LocalDate inicio, LocalDate fin, Temporada temporada) throws FechasInvalidasException {
        this(inicio == null ? null : inicio.getMonth(), fin == null ? null : fin.getMonth(), temporada);
    }

    public boolean contiene(LocalDate fecha) {
        if (fecha == null) return false;
        int mes = fecha.getMonthValue(), inicio = mesInicio.getValue(), fin = mesFin.getValue();
        return inicio <= fin ? mes >= inicio && mes <= fin : mes >= inicio || mes <= fin;
    }

    public boolean seSolapaCon(PeriodoTemporada otro) {
        if (otro == null) return false;
        for (int mes = 1; mes <= 12; mes++) {
            LocalDate referencia = LocalDate.of(2000, mes, 1);
            if (contiene(referencia) && otro.contiene(referencia)) return true;
        }
        return false;
    }

    public Month getMesInicio() { return mesInicio; }
    public Month getMesFin() { return mesFin; }
    public Temporada getTemporada() { return temporada; }
    @Override public String toString() { return temporada + " [" + mesInicio + " a " + mesFin + "]"; }
}
