
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
                .subtract(calcularTotalDevoluciones())
                .subtract(calcularTotalAnulacionesPagos());

        if (movimiento.getValor().compareTo(dineroRecibido) > 0) {
            throw new IllegalArgumentException(
                    "La devolucion supera el dinero recibido");
        }

        registrar(movimiento, TipoMovimientoFolio.DEVOLUCION);
    }

    /** Registra la compensación de un cargo sin borrar el movimiento original. */
    public void registrarAnulacionCargo(MovimientoFolio movimiento) {
        validarAnulacion(movimiento, TipoMovimientoFolio.CARGO);
        if (movimiento.getValor().compareTo(calcularSaldo()) > 0) {
            throw new IllegalArgumentException(
                    "No se puede anular el cargo porque ya fue cubierto total o parcialmente por pagos");
        }
        registrar(movimiento, TipoMovimientoFolio.ANULACION_CARGO);
    }

    /** Registra la compensación de un pago sin confundirla con una devolución real. */
    public void registrarAnulacionPago(MovimientoFolio movimiento) {
        validarAnulacion(movimiento, TipoMovimientoFolio.PAGO);
        registrar(movimiento, TipoMovimientoFolio.ANULACION_PAGO);
    }

    private void validarAnulacion(MovimientoFolio anulacion,
                                  TipoMovimientoFolio tipoOriginal) {
        Objects.requireNonNull(anulacion, "El movimiento no puede ser null");
        TipoMovimientoFolio tipoAnulacion = tipoOriginal == TipoMovimientoFolio.CARGO
                ? TipoMovimientoFolio.ANULACION_CARGO
                : TipoMovimientoFolio.ANULACION_PAGO;

        if (anulacion.getTipo() != tipoAnulacion
                || anulacion.getIdMovimientoRelacionado() == null
                || anulacion.getIdMovimientoRelacionado().isBlank()) {
            throw new IllegalArgumentException(
                    "La anulación debe indicar el tipo y el identificador del movimiento original");
        }

        MovimientoFolio original = null;
        for (MovimientoFolio existente : movimientos) {
            if (existente.getId().equals(anulacion.getIdMovimientoRelacionado())) {
                original = existente;
                break;
            }
        }

        if (original == null || original.getTipo() != tipoOriginal) {
            throw new IllegalArgumentException("No existe el movimiento original que se desea anular");
        }
        if (original.getValor().compareTo(anulacion.getValor()) != 0) {
            throw new IllegalArgumentException("La anulación debe compensar el valor completo del movimiento original");
        }
        if (tipoOriginal == TipoMovimientoFolio.PAGO
                && calcularTotalDevoluciones().compareTo(
                calcularTotalPagos().subtract(calcularTotalAnulacionesPagos())
                        .subtract(anulacion.getValor())) > 0) {
            throw new IllegalArgumentException(
                    "No se puede anular el pago porque ya existe una devolución asociada al dinero recibido");
        }

        for (MovimientoFolio existente : movimientos) {
            if ((existente.getTipo() == TipoMovimientoFolio.ANULACION_CARGO
                    || existente.getTipo() == TipoMovimientoFolio.ANULACION_PAGO)
                    && anulacion.getIdMovimientoRelacionado().equals(
                    existente.getIdMovimientoRelacionado())) {
                throw new IllegalArgumentException("El movimiento original ya fue anulado");
            }
        }
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

    public BigDecimal calcularTotalAnulacionesCargos() {
        return sumarPorTipo(TipoMovimientoFolio.ANULACION_CARGO);
    }

    public BigDecimal calcularTotalAnulacionesPagos() {
        return sumarPorTipo(TipoMovimientoFolio.ANULACION_PAGO);
    }

    /** Saldo = cargos - anulaciones de cargos + devoluciones - pagos + anulaciones de pagos. */
    public BigDecimal calcularSaldo() {
        return calcularTotalCargos()
                .subtract(calcularTotalAnulacionesCargos())
                .add(calcularTotalDevoluciones())
                .subtract(calcularTotalPagos())
                .add(calcularTotalAnulacionesPagos());
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
