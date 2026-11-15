package servicios;

import enums.Temporada;
import estructuras.ListaDoblementeEnlazada;
import excepciones.FechasInvalidasException;
import modelo.Apartamento;
import modelo.PeriodoTemporada;

import java.time.LocalDate;

public class GestorTemporadas {

    private final ListaDoblementeEnlazada<PeriodoTemporada> periodos = new ListaDoblementeEnlazada<>();

    public void definirPeriodo(PeriodoTemporada nuevo) throws FechasInvalidasException {
        for (PeriodoTemporada existente : periodos) {
            if (existente.seSolapaCon(nuevo)) {
                throw new FechasInvalidasException("El periodo " + nuevo + " se cruza con " + existente);
            }
        }
        periodos.agregar(nuevo);
    }

    public boolean eliminarPeriodo(PeriodoTemporada periodo) {
        return periodos.eliminar(periodo);
    }

    public Temporada temporadaDe(LocalDate fecha) {
        for (PeriodoTemporada p : periodos) {
            if (p.contiene(fecha)) {
                return p.getTemporada();
            }
        }
        return Temporada.BAJA;
    }

    public void definirTarifa(Apartamento apartamento, Temporada temporada, double valorPersonaNoche) {
        if (apartamento == null || temporada == null) {
            throw new IllegalArgumentException("Apartamento y temporada son obligatorios");
        }
        apartamento.definirTarifa(temporada, valorPersonaNoche);
    }

    public double tarifaNoche(Apartamento apartamento, LocalDate noche) {
        return apartamento.tarifaPara(temporadaDe(noche));
    }

    public ListaDoblementeEnlazada<PeriodoTemporada> getPeriodos() {
        return periodos;
    }
}
