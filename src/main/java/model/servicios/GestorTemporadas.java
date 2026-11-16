package model.servicios;

import model.enums.Temporada;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.excepciones.FechasInvalidasException;
import model.modelo.Apartamento;
import model.modelo.PeriodoTemporada;

import java.time.LocalDate;
/**
 * Gestiona los periodos de temporada y las tarifas de los apartamentos.
 * Permite definir y eliminar periodos, consultar la temporada de una fecha
 * y establecer o consultar tarifas según la temporada correspondiente.
 */
public class GestorTemporadas {

    private final ListaDoblementeEnlazada<PeriodoTemporada> periodos = new ListaDoblementeEnlazada<>();
    /**
     * Registra un nuevo periodo de temporada si no se solapa con alguno existente.
     *
     * @param nuevo periodo de temporada que se desea registrar
     * @throws FechasInvalidasException si el nuevo periodo se cruza con uno existente
     */
    public void definirPeriodo(PeriodoTemporada nuevo) throws FechasInvalidasException {
        for (PeriodoTemporada existente : periodos) {
            if (existente.seSolapaCon(nuevo)) {
                throw new FechasInvalidasException("El periodo " + nuevo + " se cruza con " + existente);
            }
        }
        periodos.agregar(nuevo);
    }
    /**
     * Elimina un periodo de temporada de la lista.
     *
     * @param periodo periodo que se desea eliminar
     * @return {@code true} si se eliminó el periodo; {@code false} si no se encontró
     */
    public boolean eliminarPeriodo(PeriodoTemporada periodo) {
        return periodos.eliminar(periodo);
    }
    /**
     * Busca el periodo que contiene una fecha y obtiene su temporada.
     * Si la fecha no pertenece a ningún periodo definido, retorna la temporada BAJA.
     *
     * @param fecha fecha cuya temporada se desea consultar
     * @return temporada correspondiente a la fecha
     */
    public Temporada temporadaDe(LocalDate fecha) {
        for (PeriodoTemporada p : periodos) {
            if (p.contiene(fecha)) {
                return p.getTemporada();
            }
        }
        return Temporada.BAJA;
    }
    /**
     * Define la tarifa por persona y noche de un apartamento para una temporada.
     *
     * @param apartamento apartamento al que se le asignará la tarifa
     * @param temporada temporada para la cual se establece la tarifa
     * @param valorPersonaNoche valor que se cobrará por persona y noche
     * @throws IllegalArgumentException si el apartamento o la temporada son nulos
     */
    public void definirTarifa(Apartamento apartamento, Temporada temporada, double valorPersonaNoche) {
        if (apartamento == null || temporada == null) {
            throw new IllegalArgumentException("Apartamento y temporada son obligatorios");
        }
        apartamento.definirTarifa(temporada, valorPersonaNoche);
    }
    /**
     * Obtiene la tarifa por persona y noche de un apartamento según la temporada
     * correspondiente a una fecha.
     *
     * @param apartamento apartamento cuya tarifa se consulta
     * @param noche fecha de la noche que se desea cotizar
     * @return tarifa por persona y noche
     */
    public double tarifaNoche(Apartamento apartamento, LocalDate noche) {
        return apartamento.tarifaPara(temporadaDe(noche));
    }

    /**
     * Retorna la lista de periodos de temporada registrados.
     *
     * @return lista de periodos definidos
     */
    public ListaDoblementeEnlazada<PeriodoTemporada> getPeriodos() {
        return periodos;
    }
}
