package model.servicios;

import model.enums.EstadoReserva;
import model.estructuras.Cola.ColaPrioritaria;
import model.estructuras.Conjunto;
import model.modelo.Reserva;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

public class GestorVencimientos {

    private final ColaPrioritaria<Reserva> reservasPendientes;
    private final Conjunto<String> idsRegistrados;

    public GestorVencimientos() {

        Comparator<Reserva> comparador =
                Comparator.comparing(Reserva::getVenceEn);

        reservasPendientes = new ColaPrioritaria<>(comparador);
        idsRegistrados = new Conjunto<>();
    }

    /**
     * Agrega una reserva pendiente a la cola de vencimientos.
     * Evita registrar dos veces la misma reserva.
     */
    public boolean agregarReserva(Reserva reserva) {

        Objects.requireNonNull(reserva, "La reserva no puede ser null");

        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalArgumentException(
                    "Solo se pueden registrar reservas pendientes"
            );
        }

        if (!idsRegistrados.agregar(reserva.getId())) {
            return false;
        }

        reservasPendientes.add(reserva);
        return true;
    }

    /**
     * Consulta la reserva que tiene el vencimiento más próximo.
     */
    public Reserva consultarProximoVencimiento() {

        if (reservasPendientes.isEmpty()) {
            return null;
        }

        return reservasPendientes.peek();
    }

    /**
     * Cancela las reservas pendientes cuyo plazo ya venció.
     *
     * @return cantidad de reservas canceladas.
     */
    public int procesarVencimientos(LocalDateTime ahora) {

        Objects.requireNonNull(ahora, "La fecha actual no puede ser null");

        int cantidadCanceladas = 0;

        while (!reservasPendientes.isEmpty()) {

            Reserva reserva = reservasPendientes.peek();

            // La cola esta ordenada por hora de vencimiento: si el plazo de la primera
            // aun no llega, el de las siguientes tampoco. Se compara solo por tiempo (no por
            // estado) para que una reserva ya confirmada o cancelada no bloquee la cola.
            if (ahora.isBefore(reserva.getVenceEn())) {
                break;
            }

            reservasPendientes.poll();

            // Puede haber sido confirmada o cancelada previamente.
            if (reserva.getEstado() == EstadoReserva.PENDIENTE) {
                reserva.cancelar();
                // Libera las noches del apartamento.
                reserva.getApartamento().quitarReserva(reserva);
                cantidadCanceladas++;
            }
        }

        return cantidadCanceladas;
    }

    public int cantidadEnCola() {
        return reservasPendientes.size();
    }

    public boolean estaVacio() {
        return reservasPendientes.isEmpty();
    }
}