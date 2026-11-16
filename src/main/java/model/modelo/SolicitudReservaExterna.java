package model.modelo;

import model.enums.EstadoSolicitudExterna;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Peticion recibida desde un tercero. Todavia no bloquea noches ni crea una Reserva:
 * eso solo sucede cuando la solicitud llega al frente de la cola FIFO y es aceptada.
 */
public class SolicitudReservaExterna {

    private final String id;
    private final Huesped titular;
    private final Apartamento apartamento;
    private final Estancia estancia;
    private final ListaDoblementeEnlazada<Ocupante> ocupantes;
    private final LocalDateTime recibidaEn;

    private EstadoSolicitudExterna estado;
    private Reserva reservaGenerada;
    private String motivoRechazo;

    public SolicitudReservaExterna(String id, Huesped titular, Apartamento apartamento,
                                   Estancia estancia, ListaDoblementeEnlazada<Ocupante> ocupantes,
                                   LocalDateTime recibidaEn) {
        if (id == null || id.isBlank() || titular == null || apartamento == null
                || estancia == null || ocupantes == null || ocupantes.estaVacia() || recibidaEn == null) {
            throw new IllegalArgumentException("Los datos de la solicitud externa son obligatorios");
        }
        this.id = id;
        this.titular = titular;
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.ocupantes = new ListaDoblementeEnlazada<>();
        for (Ocupante ocupante : ocupantes) {
            this.ocupantes.agregar(ocupante);
        }
        this.recibidaEn = recibidaEn;
        this.estado = EstadoSolicitudExterna.PENDIENTE;
    }

    public void aceptar(Reserva reserva) {
        Objects.requireNonNull(reserva, "La reserva generada es obligatoria");
        if (estado != EstadoSolicitudExterna.PENDIENTE) {
            throw new IllegalStateException("La solicitud " + id + " ya fue procesada");
        }
        this.reservaGenerada = reserva;
        this.estado = EstadoSolicitudExterna.ACEPTADA;
    }

    public void rechazar(String motivo) {
        if (estado != EstadoSolicitudExterna.PENDIENTE) {
            throw new IllegalStateException("La solicitud " + id + " ya fue procesada");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo del rechazo es obligatorio");
        }
        this.motivoRechazo = motivo;
        this.estado = EstadoSolicitudExterna.RECHAZADA;
    }

    public String getId() { return id; }
    public Huesped getTitular() { return titular; }
    public Apartamento getApartamento() { return apartamento; }
    public Estancia getEstancia() { return estancia; }
    public ListaDoblementeEnlazada<Ocupante> getOcupantes() {
        ListaDoblementeEnlazada<Ocupante> copia = new ListaDoblementeEnlazada<>();
        for (Ocupante ocupante : ocupantes) {
            copia.agregar(ocupante);
        }
        return copia;
    }
    public LocalDateTime getRecibidaEn() { return recibidaEn; }
    public EstadoSolicitudExterna getEstado() { return estado; }
    public Reserva getReservaGenerada() { return reservaGenerada; }
    public String getMotivoRechazo() { return motivoRechazo; }

    @Override
    public String toString() {
        return id + " [" + estado + ", apartamento " + apartamento.getCodigo()
                + ", " + estancia + "]";
    }
}
