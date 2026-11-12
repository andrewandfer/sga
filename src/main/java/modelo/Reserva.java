package modelo;
import enums.CanalReserva;
import enums.EstadoReserva;
import estructuras.ListaDoblementeEnlazada;
import modelo.Apartamento;

import java.time.LocalDateTime;
import java.util.Objects;


public class Reserva implements Comparable<Reserva> {

    public static final int HORAS_VENCIMIENTO = 24;

    private final String id;
    private final CanalReserva canal;
    private EstadoReserva estado;
    private final Estancia estancia;
    private final Huesped titular;
    private final Apartamento apartamento;
    private final ListaDoblementeEnlazada<Ocupante> ocupantes;
    private final LocalDateTime creadaEn;
    private final LocalDateTime venceEn;

    public Reserva(String id, CanalReserva canal, Huesped titular, Apartamento apartamento,
                   Estancia estancia, ListaDoblementeEnlazada<Ocupante> ocupantes, LocalDateTime creadaEn) {
        if (id == null || canal == null || titular == null || apartamento == null
                || estancia == null || ocupantes == null || creadaEn == null) {
            throw new IllegalArgumentException("Todos los datos de la reserva son obligatorios");
        }
        this.id = id;
        this.canal = canal;
        this.titular = titular;
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.ocupantes = ocupantes;
        this.creadaEn = creadaEn;
        this.venceEn = creadaEn.plusHours(HORAS_VENCIMIENTO);
        this.estado = EstadoReserva.PENDIENTE;
    }


    public void confirmar() {
        transicion(EstadoReserva.CONFIRMADA, EstadoReserva.PENDIENTE);
    }

    public void iniciar() {
        transicion(EstadoReserva.EN_CURSO, EstadoReserva.CONFIRMADA);
    }

    public void finalizar() {
        transicion(EstadoReserva.FINALIZADA, EstadoReserva.EN_CURSO);
    }

    public void cancelar() {
        transicion(EstadoReserva.CANCELADA, EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);
    }

    public void marcarNoShow() {
        transicion(EstadoReserva.NO_SHOW, EstadoReserva.CONFIRMADA);
    }

    private void transicion(EstadoReserva destino, EstadoReserva... permitidos) {
        for (EstadoReserva p : permitidos) {
            if (estado == p) {
                estado = destino;
                return;
            }
        }
        throw new IllegalStateException("Transicion invalida en la reserva " + id + ": " + estado + " -> " + destino);
    }

    public boolean ocupaNoches() {
        return estado != EstadoReserva.CANCELADA && estado != EstadoReserva.NO_SHOW;
    }

    public boolean esActiva() {
        return estado == EstadoReserva.PENDIENTE || estado == EstadoReserva.CONFIRMADA
                || estado == EstadoReserva.EN_CURSO;
    }

    public boolean estaVencida(LocalDateTime ahora) {
        return estado == EstadoReserva.PENDIENTE && !ahora.isBefore(venceEn);
    }

    public int numeroPersonas() {
        return ocupantes.tamano();
    }

    public String getId() {
        return id;
    }

    public CanalReserva getCanal() {
        return canal;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public Estancia getEstancia() {
        return estancia;
    }

    public Huesped getTitular() {
        return titular;
    }

    public Apartamento getApartamento() {
        return apartamento;
    }

    public ListaDoblementeEnlazada<Ocupante> getOcupantes() {
        return ocupantes;
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }

    public LocalDateTime getVenceEn() {
        return venceEn;
    }

    @Override
    public int compareTo(Reserva otra) {
        return estancia.getEntrada().compareTo(otra.estancia.getEntrada());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reserva)) {
            return false;
        }
        return id.equals(((Reserva) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id + " [" + canal + ", " + estado + ", apto " + apartamento.getCodigo() + ", " + estancia + "]";
    }
}
