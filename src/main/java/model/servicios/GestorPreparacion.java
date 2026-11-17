package model.servicios;

import model.enums.EstadoApartamento;
import model.estructuras.Cola.ColaPrioritaria;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.modelo.Apartamento;
import model.modelo.SolicitudPreparacion;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

/** Administra la cola prioritaria de limpieza y alistamiento de apartamentos. */
public class GestorPreparacion {

    private final ColaPrioritaria<SolicitudPreparacion> pendientes;
    private SolicitudPreparacion actual;
    private long minutosAcumulados;
    private int preparacionesFinalizadas;

    public GestorPreparacion() {
        this.pendientes = new ColaPrioritaria<>(Comparator
                .comparingInt(SolicitudPreparacion::getPrioridad)
                .thenComparing(SolicitudPreparacion::getLlegadaEstimada,
                        Comparator.nullsLast(Comparator.naturalOrder())));
    }

    /**
     * Registra un apartamento pendiente. Prioridad 1: llegada estimada hoy;
     * prioridad 2: llegada posterior o todavía desconocida.
     */
    public SolicitudPreparacion registrarPendiente(Apartamento apartamento,
                                                   LocalDateTime llegadaEstimada,
                                                   LocalDateTime ahora) {
        Objects.requireNonNull(apartamento, "El apartamento es obligatorio");
        Objects.requireNonNull(ahora, "La fecha de registro es obligatoria");
        if (apartamento.getEstadoOperativo() != EstadoApartamento.PENDIENTE_PREPARACION) {
            throw new IllegalStateException("El apartamento debe estar PENDIENTE_PREPARACION");
        }
        SolicitudPreparacion solicitud = new SolicitudPreparacion(
                apartamento, llegadaEstimada, ahora,
                llegadaEstimada != null && llegadaEstimada.toLocalDate().equals(ahora.toLocalDate()) ? 1 : 2);
        if (pendientes.contains(solicitud) || (actual != null && actual.equals(solicitud))) {
            throw new IllegalStateException("El apartamento ya tiene una preparación registrada");
        }
        pendientes.add(solicitud);
        return solicitud;
    }

    /** Retira la siguiente solicitud de la cola y comienza su preparación. */
    public SolicitudPreparacion iniciarSiguiente(LocalDateTime ahora) {
        Objects.requireNonNull(ahora, "La fecha de inicio es obligatoria");
        if (actual != null) {
            throw new IllegalStateException("Ya hay un apartamento en preparación");
        }
        if (pendientes.isEmpty()) {
            return null;
        }
        SolicitudPreparacion siguiente = pendientes.poll();
        if (siguiente.getApartamento().getEstadoOperativo() != EstadoApartamento.PENDIENTE_PREPARACION) {
            throw new IllegalStateException("El apartamento dejó de estar pendiente de preparación");
        }
        siguiente.iniciar(ahora);
        siguiente.getApartamento().cambiarEstado(EstadoApartamento.EN_PREPARACION);
        actual = siguiente;
        return actual;
    }

    /** Finaliza el trabajo actual, libera el apartamento y registra su duración. */
    public SolicitudPreparacion finalizarActual(LocalDateTime ahora) {
        if (actual == null) {
            throw new IllegalStateException("No hay una preparación en curso");
        }
        actual.finalizar(ahora);
        actual.getApartamento().cambiarEstado(EstadoApartamento.PREPARADO);
        minutosAcumulados += actual.duracionMinutos();
        preparacionesFinalizadas++;
        SolicitudPreparacion finalizada = actual;
        actual = null;
        return finalizada;
    }

    public ListaDoblementeEnlazada<SolicitudPreparacion> listarPendientes() {
        ListaDoblementeEnlazada<SolicitudPreparacion> resultado = new ListaDoblementeEnlazada<>();
        for (SolicitudPreparacion solicitud : pendientes) resultado.agregar(solicitud);
        return resultado;
    }

    public int cantidadPendientes() { return pendientes.size(); }
    public SolicitudPreparacion getActual() { return actual; }
    public int getPreparacionesFinalizadas() { return preparacionesFinalizadas; }

    public double promedioMinutosPreparacion() {
        return preparacionesFinalizadas == 0 ? 0.0 : (double) minutosAcumulados / preparacionesFinalizadas;
    }
}
