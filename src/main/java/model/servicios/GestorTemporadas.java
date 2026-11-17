package model.servicios;

import model.enums.Temporada;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.excepciones.FechasInvalidasException;
import model.modelo.Apartamento;
import model.modelo.PeriodoTemporada;
import java.time.LocalDate;

public class GestorTemporadas {
    private final ListaDoblementeEnlazada<PeriodoTemporada> periodos = new ListaDoblementeEnlazada<>();
    public void definirPeriodo(PeriodoTemporada nuevo) throws FechasInvalidasException {
        if (nuevo == null) throw new FechasInvalidasException("El periodo es obligatorio");
        if (nuevo.getTemporada() == Temporada.BAJA) {
            throw new FechasInvalidasException("La temporada baja se asigna automáticamente a los meses no configurados como alta o media.");
        }
        for (PeriodoTemporada existente : periodos)
            if (existente.seSolapaCon(nuevo))
                throw new FechasInvalidasException("Los meses seleccionados se cruzan con otro periodo: " + existente);
        periodos.agregar(nuevo);
    }
    public boolean eliminarPeriodo(PeriodoTemporada periodo) { return periodos.eliminar(periodo); }
    /** Determina automáticamente la temporada de cada noche según el mes, todos los años. */
    public Temporada temporadaDe(LocalDate fecha) {
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria");
        for (PeriodoTemporada p : periodos) if (p.contiene(fecha)) return p.getTemporada();
        return Temporada.BAJA;
    }
    public void definirTarifa(Apartamento apartamento, Temporada temporada, double valorPersonaNoche) {
        if (apartamento == null || temporada == null) throw new IllegalArgumentException("Apartamento y temporada son obligatorios");
        apartamento.definirTarifa(temporada, valorPersonaNoche);
    }
    public double tarifaNoche(Apartamento apartamento, LocalDate noche) { return apartamento.tarifaPara(temporadaDe(noche)); }
    public ListaDoblementeEnlazada<PeriodoTemporada> getPeriodos() { return periodos; }
}
