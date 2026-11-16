package model.servicios;

import model.enums.TipoMovimientoFolio;
import model.estructuras.Pila.Pila;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.excepciones.EstructuraVaciaException;
import model.modelo.Folio;
import model.modelo.MovimientoFolio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Centraliza las operaciones de los folios y conserva una pila de deshacer
 * independiente para cada folio. Los movimientos originales nunca se eliminan:
 * deshacer agrega un movimiento compensatorio relacionado con el original.
 */
public class GestorFolios {

    private final ListaDoblementeEnlazada<HistorialFolio> historiales;
    private long consecutivoReversiones;

    public GestorFolios() {
        historiales = new ListaDoblementeEnlazada<>();
        consecutivoReversiones = 0;
    }

    public void registrarCargo(Folio folio, MovimientoFolio movimiento) {
        validarEntrada(folio, movimiento, TipoMovimientoFolio.CARGO);
        folio.registrarCargo(movimiento);
        historialDe(folio).operaciones.push(new OperacionFolio(movimiento));
    }

    public void registrarPago(Folio folio, MovimientoFolio movimiento) {
        validarEntrada(folio, movimiento, TipoMovimientoFolio.PAGO);
        folio.registrarPago(movimiento);
        historialDe(folio).operaciones.push(new OperacionFolio(movimiento));
    }

    /**
     * Una devolución real se conserva en el folio, pero no se puede deshacer
     * con la operación de recepción destinada a cargos y pagos.
     * Se deja como barrera en la pila para no deshacer movimientos anteriores
     * ignorando una operación posterior.
     */
    public void registrarDevolucion(Folio folio, MovimientoFolio movimiento) {
        validarEntrada(folio, movimiento, TipoMovimientoFolio.DEVOLUCION);
        folio.registrarDevolucion(movimiento);
        historialDe(folio).operaciones.push(new OperacionFolio(movimiento));
    }

    /**
     * Deshace la última operación registrada para el folio indicado.
     * Solo se pueden deshacer cargos y pagos. La operación contraria queda
     * registrada como ANULACION_CARGO o ANULACION_PAGO.
     */
    public MovimientoFolio deshacerUltimaOperacion(Folio folio, LocalDateTime fecha) {
        Objects.requireNonNull(folio, "El folio es obligatorio");
        Objects.requireNonNull(fecha, "La fecha de anulación es obligatoria");

        HistorialFolio historial = buscarHistorial(folio);
        if (historial == null || historial.operaciones.isEmpty()) {
            throw new EstructuraVaciaException(
                    "No hay operaciones para deshacer en el folio " + folio.getId());
        }

        OperacionFolio ultima = historial.operaciones.peek();
        MovimientoFolio original = ultima.movimiento;
        TipoMovimientoFolio tipoReversion;
        String concepto;

        if (original.getTipo() == TipoMovimientoFolio.CARGO) {
            tipoReversion = TipoMovimientoFolio.ANULACION_CARGO;
            concepto = "Anulación del cargo " + original.getId();
        } else if (original.getTipo() == TipoMovimientoFolio.PAGO) {
            tipoReversion = TipoMovimientoFolio.ANULACION_PAGO;
            concepto = "Anulación del pago " + original.getId();
        } else {
            throw new IllegalStateException(
                    "La última operación es una devolución; no se puede deshacer desde esta función");
        }

        MovimientoFolio reversion = new MovimientoFolio(
                generarIdReversion(folio),
                tipoReversion,
                concepto,
                original.getValor(),
                fecha,
                original.getId()
        );

        // Primero se registra la compensación. Si falla una validación,
        // la pila conserva la operación original para permitir reintentar.
        if (tipoReversion == TipoMovimientoFolio.ANULACION_CARGO) {
            folio.registrarAnulacionCargo(reversion);
        } else {
            folio.registrarAnulacionPago(reversion);
        }

        historial.operaciones.pop();
        return reversion;
    }

    public boolean puedeDeshacer(Folio folio) {
        if (folio == null) {
            return false;
        }
        HistorialFolio historial = buscarHistorial(folio);
        if (historial == null || historial.operaciones.isEmpty()) {
            return false;
        }
        TipoMovimientoFolio tipo = historial.operaciones.peek().movimiento.getTipo();
        return tipo == TipoMovimientoFolio.CARGO || tipo == TipoMovimientoFolio.PAGO;
    }

    public int operacionesPendientesDeDeshacer(Folio folio) {
        if (folio == null) {
            throw new IllegalArgumentException("El folio es obligatorio");
        }
        HistorialFolio historial = buscarHistorial(folio);
        return historial == null ? 0 : historial.operaciones.size();
    }

    private void validarEntrada(Folio folio, MovimientoFolio movimiento,
                                TipoMovimientoFolio tipoEsperado) {
        Objects.requireNonNull(folio, "El folio es obligatorio");
        Objects.requireNonNull(movimiento, "El movimiento es obligatorio");
        if (movimiento.getTipo() != tipoEsperado) {
            throw new IllegalArgumentException("Se esperaba un movimiento de tipo " + tipoEsperado);
        }
    }

    private HistorialFolio historialDe(Folio folio) {
        HistorialFolio historial = buscarHistorial(folio);
        if (historial == null) {
            historial = new HistorialFolio(folio);
            historiales.agregar(historial);
        }
        return historial;
    }

    private HistorialFolio buscarHistorial(Folio folio) {
        for (HistorialFolio historial : historiales) {
            if (historial.folio == folio) {
                return historial;
            }
        }
        return null;
    }

    private String generarIdReversion(Folio folio) {
        consecutivoReversiones++;
        return "REV-" + folio.getId() + "-" + consecutivoReversiones;
    }

    private static class HistorialFolio {
        private final Folio folio;
        private final Pila<OperacionFolio> operaciones;

        private HistorialFolio(Folio folio) {
            this.folio = folio;
            this.operaciones = new Pila<>();
        }
    }

    private static class OperacionFolio {
        private final MovimientoFolio movimiento;

        private OperacionFolio(MovimientoFolio movimiento) {
            this.movimiento = movimiento;
        }
    }
}
