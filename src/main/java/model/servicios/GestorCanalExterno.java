package model.servicios;

import model.estructuras.Cola.Cola;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.excepciones.CapacidadExcedidaException;
import model.excepciones.ReservaSolapadaException;
import model.modelo.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Simula la recepcion de reservas de un tercero mediante una cola FIFO.
 * Cada solicitud se procesa una sola vez, respetando estrictamente su orden de llegada.
 */
public class GestorCanalExterno {

    private final Cola<SolicitudReservaExterna> solicitudes;
    private final ListaDoblementeEnlazada<SolicitudReservaExterna> historial;
    private final ListaDoblementeEnlazada<SolicitudReservaExterna> conflictos;
    private final GestorReservas gestorReservas;
    private int consecutivo;

    public GestorCanalExterno(GestorReservas gestorReservas) {
        this.gestorReservas = Objects.requireNonNull(gestorReservas,
                "El gestor de reservas es obligatorio");
        this.solicitudes = new Cola<>();
        this.historial = new ListaDoblementeEnlazada<>();
        this.conflictos = new ListaDoblementeEnlazada<>();
    }

    /** Recibe una solicitud y la agrega al final de la cola. No reserva noches todavia. */
    public SolicitudReservaExterna recibirSolicitud(Huesped titular, Apartamento apartamento,
                                                    Estancia estancia,
                                                    ListaDoblementeEnlazada<Ocupante> ocupantes) {
        return recibirSolicitud(titular, apartamento, estancia, ocupantes, LocalDateTime.now());
    }

    public SolicitudReservaExterna recibirSolicitud(Huesped titular, Apartamento apartamento,
                                                    Estancia estancia,
                                                    ListaDoblementeEnlazada<Ocupante> ocupantes,
                                                    LocalDateTime recibidaEn) {
        consecutivo++;
        SolicitudReservaExterna solicitud = new SolicitudReservaExterna(
                String.format("EXT-%04d", consecutivo), titular, apartamento,
                estancia, ocupantes, recibidaEn);
        solicitudes.add(solicitud);
        return solicitud;
    }

    /**
     * Procesa exclusivamente la solicitud mas antigua.
     * Retorna null cuando la cola esta vacia.
     */
    public SolicitudReservaExterna procesarSiguiente() {
        if (solicitudes.isEmpty()) {
            return null;
        }

        SolicitudReservaExterna solicitud = solicitudes.poll();
        try {
            Reserva reserva = gestorReservas.crearReservaExternaConfirmada(
                    solicitud.getTitular(), solicitud.getApartamento(), solicitud.getEstancia(),
                    solicitud.getOcupantes(), solicitud.getRecibidaEn());
            solicitud.aceptar(reserva);
        } catch (ReservaSolapadaException | CapacidadExcedidaException | IllegalStateException ex) {
            solicitud.rechazar(ex.getMessage());
            conflictos.agregar(solicitud);
        }

        historial.agregar(solicitud);
        return solicitud;
    }

    /** Procesa todas las solicitudes que estaban en cola, conservando el orden FIFO. */
    public ListaDoblementeEnlazada<SolicitudReservaExterna> procesarTodas() {
        ListaDoblementeEnlazada<SolicitudReservaExterna> procesadas = new ListaDoblementeEnlazada<>();
        while (!solicitudes.isEmpty()) {
            procesadas.agregar(procesarSiguiente());
        }
        return procesadas;
    }

    public int cantidadPendientes() {
        return solicitudes.size();
    }

    public boolean tienePendientes() {
        return !solicitudes.isEmpty();
    }

    public ListaDoblementeEnlazada<SolicitudReservaExterna> listarPendientes() {
        ListaDoblementeEnlazada<SolicitudReservaExterna> copia = new ListaDoblementeEnlazada<>();
        for (SolicitudReservaExterna solicitud : solicitudes) {
            copia.agregar(solicitud);
        }
        return copia;
    }

    public ListaDoblementeEnlazada<SolicitudReservaExterna> getHistorial() {
        return historial;
    }

    /** Solicitudes rechazadas por conflicto de fechas, capacidad o estado operativo. */
    public ListaDoblementeEnlazada<SolicitudReservaExterna> getConflictos() {
        return conflictos;
    }
}
