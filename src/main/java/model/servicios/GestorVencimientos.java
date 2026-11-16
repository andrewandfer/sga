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
     * Registra una reserva pendiente en la cola de vencimientos.
     * Evita registrar dos veces la misma reserva.
     *
     * @return true si se agregó; false si ya estaba registrada.
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
     * Consulta la reserva con el vencimiento más próximo.
     * No la retira de la cola.
     */
    public Reserva consultarProximoVencimiento() {

        if (reservasPendientes.isEmpty()) {
            return null;
        }

        return reservasPendientes.peek();
    }

    /**
     * Procesa las reservas cuyo plazo de vencimiento ya llegó.
     *
     * Retira de la cola las reservas vencidas, independientemente
     * de su estado actual. Solo cancela las que siguen pendientes.
     *
     * @return cantidad de reservas que fueron canceladas.
     */
    public int procesarVencimientos(LocalDateTime ahora) {

        Objects.requireNonNull(
                ahora,
                "La fecha actual no puede ser null"
        );

        int cantidadCanceladas = 0;

        while (!reservasPendientes.isEmpty()) {

            Reserva reserva = reservasPendientes.peek();

            /*
             * La cola está ordenada por fecha de vencimiento.
             * Si la primera todavía no vence, las siguientes tampoco.
             */
            if (ahora.isBefore(reserva.getVenceEn())) {
                break;
            }

            // Ya llegó el vencimiento: se retira de la cola.
            reservasPendientes.poll();

            /*
             * Puede que la reserva haya sido confirmada o cancelada
             * manualmente antes de llegar a su vencimiento.
             */
            if (reserva.getEstado() == EstadoReserva.PENDIENTE) {

                reserva.cancelar();

                // Libera las noches del apartamento.
                reserva.getApartamento().quitarReserva(reserva);

                cantidadCanceladas++;
            }
        }

        return cantidadCanceladas;
    }

    /**
     * Cuenta únicamente las reservas que siguen pendientes.
     * No cuenta las confirmadas ni las canceladas que aún esperan
     * a que llegue su fecha de vencimiento para salir de la cola.
     */
    public int cantidadEnCola() {

        int cantidad = 0;

        for (Reserva reserva : reservasPendientes) {

            if (reserva.getEstado() == EstadoReserva.PENDIENTE) {
                cantidad++;
            }
        }

        return cantidad;
    }

    public boolean estaVacio() {
        return cantidadEnCola() == 0;
    }
}