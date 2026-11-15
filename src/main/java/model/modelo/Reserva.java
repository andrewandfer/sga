package model.modelo;
import model.enums.CanalReserva;
import model.enums.EstadoReserva;
import model.estructuras.ListaDoblementeEnlazada;
import model.excepciones.TransicionInvalidaException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa una reserva de un apartamento, incluyendo sus datos,
 * ocupantes, fechas y estado dentro del proceso de reserva.
 * Permite gestionar las transiciones entre estados y consultar su vigencia.
 */
public class Reserva implements Comparable<Reserva> {
    /** Número de horas antes del vencimiento de una reserva pendiente. */
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
    /**
     * Crea una reserva en estado pendiente y establece su fecha de vencimiento.
     *
     * @param id identificador de la reserva
     * @param canal canal por el que se realizó la reserva
     * @param titular huésped responsable de la reserva
     * @param apartamento apartamento reservado
     * @param estancia periodo de la reserva
     * @param ocupantes lista de personas que ocuparán el apartamento
     * @param creadaEn fecha y hora de creación de la reserva
     * @throws IllegalArgumentException si alguno de los datos obligatorios es nulo
     */
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

    /**
     * Confirma la reserva si se encuentra pendiente.
     */
    public void confirmar() {
        transicion(EstadoReserva.CONFIRMADA, EstadoReserva.PENDIENTE);
    }
    /**
     * Inicia la reserva si se encuentra confirmada.
     */
    public void iniciar() {
        transicion(EstadoReserva.EN_CURSO, EstadoReserva.CONFIRMADA);
    }
    /**
     * Finaliza la reserva si se encuentra en curso.
     */
    public void finalizar() {
        transicion(EstadoReserva.FINALIZADA, EstadoReserva.EN_CURSO);
    }
    /**
     * Cancela la reserva si se encuentra pendiente o confirmada.
     */
    public void cancelar() {
        transicion(EstadoReserva.CANCELADA, EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);
    }
    /**
     * Marca la reserva como ausencia del huésped si estaba confirmada.
     */
    public void marcarNoShow() {
        transicion(EstadoReserva.NO_SHOW, EstadoReserva.CONFIRMADA);
    }
    /**
     * Cambia el estado de la reserva si su estado actual está entre los permitidos.
     *
     * @param destino nuevo estado de la reserva
     * @param permitidos estados desde los que se permite realizar la transición
     * @throws IllegalStateException si el estado actual no permite la transición
     */
    private void transicion(EstadoReserva destino, EstadoReserva... permitidos) {
        for (EstadoReserva p : permitidos) {
            if (estado == p) {
                estado = destino;
                return;
            }
        }
        throw new TransicionInvalidaException("Transicion invalida en la reserva " + id + ": " + estado + " -> " + destino);
    }
    /**
     * Comprueba si la reserva debe considerarse para la ocupación de noches.
     *
     * @return {@code true} si no está cancelada ni marcada como ausencia; {@code false} en caso contrario
     */
    public boolean ocupaNoches() {
        return estado != EstadoReserva.CANCELADA && estado != EstadoReserva.NO_SHOW;
    }
    /**
     * Comprueba si la reserva se encuentra en un estado activo.
     *
     * @return {@code true} si está pendiente, confirmada o en curso
     */
    public boolean esActiva() {
        return estado == EstadoReserva.PENDIENTE || estado == EstadoReserva.CONFIRMADA
                || estado == EstadoReserva.EN_CURSO;
    }
    /**
     * Comprueba si una reserva pendiente ha alcanzado su fecha de vencimiento.
     *
     * @param ahora fecha y hora con la que se compara el vencimiento
     * @return {@code true} si está pendiente y ya venció; {@code false} en caso contrario
     */
    public boolean estaVencida(LocalDateTime ahora) {
        return estado == EstadoReserva.PENDIENTE && !ahora.isBefore(venceEn);
    }
    /**
     * Obtiene la cantidad de ocupantes registrados en la reserva.
     *
     * @return número de ocupantes
     */
    public int numeroPersonas() {
        return ocupantes.tamano();
    }
    /**
     * Obtiene el identificador de la reserva.
     *
     * @return identificador de la reserva
     */
    public String getId() {
        return id;
    }

    /**
     * Obtiene el canal por el que se realizó la reserva.
     *
     * @return canal de reserva
     */
    public CanalReserva getCanal() {
        return canal;
    }
    /**
     * Obtiene el estado actual de la reserva.
     *
     * @return estado de la reserva
     */
    public EstadoReserva getEstado() {
        return estado;
    }
    /**
     * Obtiene la estancia asociada a la reserva.
     *
     * @return estancia reservada
     */
    public Estancia getEstancia() {
        return estancia;
    }
    /**
     * Obtiene el huésped titular de la reserva.
     *
     * @return huésped titular
     */
    public Huesped getTitular() {
        return titular;
    }
    /**
     * Obtiene el apartamento reservado.
     *
     * @return apartamento de la reserva
     */
    public Apartamento getApartamento() {
        return apartamento;
    }
    /**
     * Obtiene la lista de ocupantes de la reserva.
     *
     * @return lista de ocupantes
     */
    public ListaDoblementeEnlazada<Ocupante> getOcupantes() {
        return ocupantes;
    }
    /**
     * Obtiene la fecha y hora de creación de la reserva.
     *
     * @return fecha y hora de creación
     */
    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }
    /**
     * Obtiene la fecha y hora de vencimiento de la reserva.
     *
     * @return fecha y hora de vencimiento
     */
    public LocalDateTime getVenceEn() {
        return venceEn;
    }
    /**
     * Compara las reservas según la fecha de entrada de sus estancias.
     *
     * @param otra reserva con la que se compara
     * @return resultado de comparar las fechas de entrada
     */
    @Override
    public int compareTo(Reserva otra) {
        return estancia.getEntrada().compareTo(otra.estancia.getEntrada());
    }
    /**
     * Comprueba si dos reservas tienen el mismo identificador.
     *
     * @param o objeto con el que se compara
     * @return {@code true} si ambos objetos son la misma reserva; {@code false} en caso contrario
     */
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
    /**
     * Genera el código hash de la reserva a partir de su identificador.
     *
     * @return código hash de la reserva
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    /**
     * Devuelve una representación textual de la reserva con su identificador,
     * canal, estado, apartamento y estancia.
     *
     * @return representación textual de la reserva
     */
    @Override
    public String toString() {
        return id + " [" + canal + ", " + estado + ", apto " + apartamento.getCodigo() + ", " + estancia + "]";
    }
}
