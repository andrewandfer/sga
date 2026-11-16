package model.modelo;

import model.enums.Temporada;

/** Resultado de ocupacion de un apartamento dentro de una temporada. */
public class ReporteOcupacion {

    private final String codigoApartamento;
    private final Temporada temporada;
    private final int nochesDisponibles;
    private final int nochesOcupadas;

    public ReporteOcupacion(String codigoApartamento, Temporada temporada,
                            int nochesDisponibles, int nochesOcupadas) {
        this.codigoApartamento = codigoApartamento;
        this.temporada = temporada;
        this.nochesDisponibles = nochesDisponibles;
        this.nochesOcupadas = nochesOcupadas;
    }

    public String getCodigoApartamento() { return codigoApartamento; }
    public Temporada getTemporada() { return temporada; }
    public int getNochesDisponibles() { return nochesDisponibles; }
    public int getNochesOcupadas() { return nochesOcupadas; }

    public double getPorcentajeOcupacion() {
        return nochesDisponibles == 0 ? 0.0 : (double) nochesOcupadas / nochesDisponibles * 100.0;
    }

    @Override
    public String toString() {
        return codigoApartamento + " | " + temporada + " | " + nochesOcupadas + "/"
                + nochesDisponibles + " noches | " + String.format("%.2f%%", getPorcentajeOcupacion());
    }
}
