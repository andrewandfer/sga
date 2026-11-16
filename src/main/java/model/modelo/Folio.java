
package model.modelo;

import model.enums.TipoMovimientoFolio;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;

import java.math.BigDecimal;
import java.util.Objects;

public class Folio {

    private final String id;
    private final String idReserva;

    private final ListaDoblementeEnlazada<MovimientoFolio> movimientos;

    public Folio(String id, String idReserva) {
        if (id == null || id.isBlank()
                || idReserva == null || idReserva.isBlank()) {
            throw new IllegalArgumentException(
                    "El folio y la reserva deben tener identificadores");
        }

        this.id = id;
        this.idReserva = idReserva;
        this.movimientos = new ListaDoblementeEnlazada<>();
    }

    public void registrarCargo(MovimientoFolio movimiento) {
        registrar(movimiento, TipoMovimientoFolio.CARGO);
    }

    public void registrarPago(MovimientoFolio movimiento) {
        Objects.requireNonNull(movimiento,
                "El movimiento no puede ser null");

        if (movimiento.getValor().compareTo(calcularSaldo()) > 0) {
            throw new IllegalArgumentException(
                    "El pago supera el saldo pendiente");
        }

        registrar(movimiento, TipoMovimientoFolio.PAGO);
    }

    public void registrarDevolucion(MovimientoFolio movimiento) {
        Objects.requireNonNull(movimiento,
                "El movimiento no puede ser null");

        BigDecimal dineroRecibido = calcularTotalPagos()
                .subtract(calcularTotalDevoluciones());

        if (movimiento.getValor().compareTo(dineroRecibido) > 0) {
            throw new IllegalArgumentException(
                    "La devolucion supera el dinero recibido");
        }

        registrar(movimiento, TipoMovimientoFolio.DEVOLUCION);
    }

    private void registrar(
            MovimientoFolio movimiento,
            TipoMovimientoFolio tipoEsperado) {

        Objects.requireNonNull(movimiento,
                "El movimiento no puede ser null");

        if (movimiento.getTipo() != tipoEsperado) {
            throw new IllegalArgumentException(
                    "El tipo de movimiento no corresponde a la operación");
        }

        for (MovimientoFolio existente : movimientos) {
            if (existente.getId().equals(movimiento.getId())) {
                throw new IllegalArgumentException(
                        "Ya existe un movimiento con ese identificador");
            }
        }

        movimientos.agregar(movimiento);
    }

    public BigDecimal calcularTotalCargos() {
        return sumarPorTipo(TipoMovimientoFolio.CARGO);
    }

    public BigDecimal calcularTotalPagos() {
        return sumarPorTipo(TipoMovimientoFolio.PAGO);
    }

    public BigDecimal calcularTotalDevoluciones() {
        return sumarPorTipo(TipoMovimientoFolio.DEVOLUCION);
    }

    private BigDecimal sumarPorTipo(TipoMovimientoFolio tipo) {
        BigDecimal total = BigDecimal.ZERO;

        for (MovimientoFolio movimiento : movimientos) {
            if (movimiento.getTipo() == tipo) {
                total = total.add(movimiento.getValor());
            }
        }

        return total;
    }

    public BigDecimal calcularSaldo() {
        return calcularTotalCargos()
                .add(calcularTotalDevoluciones())
                .subtract(calcularTotalPagos());
    }

    public String getId() {
        return id;
    }

    public String getIdReserva() {
        return idReserva;
    }

    public int numeroMovimientos() {
        return movimientos.tamano();
    }

    public boolean estaVacio() {
        return movimientos.estaVacia();
    }

    public MovimientoFolio obtenerMovimiento(int indice) {
        return movimientos.obtener(indice);
    }

    @Override
    public String toString() {
        return "Folio " + id
                + " | Reserva: " + idReserva
                + " | Saldo: $" + calcularSaldo();
    }
}
