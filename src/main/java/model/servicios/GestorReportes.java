package model.servicios;

import model.enums.CanalReserva;
import model.enums.EstadoReserva;
import model.enums.Temporada;
import model.estructuras.ArbolBinarioBusqueda;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.modelo.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Genera los reportes administrativos recorriendo los arboles y listas propias del sistema. */
public class GestorReportes {

    private final ArbolBinarioBusqueda<Apartamento> apartamentos;
    private final ArbolBinarioBusqueda<Huesped> huespedes;
    private final GestorTemporadas temporadas;

    public GestorReportes(ArbolBinarioBusqueda<Apartamento> apartamentos,
                          ArbolBinarioBusqueda<Huesped> huespedes,
                          GestorTemporadas temporadas) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.huespedes = Objects.requireNonNull(huespedes);
        this.temporadas = Objects.requireNonNull(temporadas);
    }

    /**
     * Calcula noches ocupadas por apartamento y temporada en el intervalo [desde, hasta).
     * Las reservas canceladas y no-show no ocupan noches; las finalizadas sí cuentan como historial.
     */
    public ListaDoblementeEnlazada<ReporteOcupacion> generarOcupacion(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        int[] nochesPorTemporada = contarNochesPorTemporada(desde, hasta);
        ListaDoblementeEnlazada<ReporteOcupacion> resultado = new ListaDoblementeEnlazada<>();

        for (Apartamento apartamento : apartamentos) {
            for (Temporada temporada : Temporada.values()) {
                int disponibles = nochesPorTemporada[temporada.ordinal()];
                if (disponibles == 0) continue;
                int ocupadas = contarNochesOcupadas(apartamento, temporada, desde, hasta);
                resultado.agregar(new ReporteOcupacion(apartamento.getCodigo(), temporada,
                        disponibles, ocupadas));
            }
        }
        return resultado;
    }

    private int[] contarNochesPorTemporada(LocalDate desde, LocalDate hasta) {
        int[] cantidades = new int[Temporada.values().length];
        for (LocalDate noche = desde; noche.isBefore(hasta); noche = noche.plusDays(1)) {
            cantidades[temporadas.temporadaDe(noche).ordinal()]++;
        }
        return cantidades;
    }

    private int contarNochesOcupadas(Apartamento apartamento, Temporada temporada,
                                     LocalDate desde, LocalDate hasta) {
        int total = 0;
        for (LocalDate noche = desde; noche.isBefore(hasta); noche = noche.plusDays(1)) {
            if (temporadas.temporadaDe(noche) != temporada) continue;
            boolean ocupada = false;
            for (Huesped huesped : huespedes) {
                for (Reserva reserva : huesped.getHistorial()) {
                    if (reserva.getApartamento().equals(apartamento)
                            && reserva.ocupaNoches() && reserva.getEstancia().incluyeNoche(noche)) {
                        ocupada = true;
                        break;
                    }
                }
                if (ocupada) break;
            }
            if (ocupada) total++;
        }
        return total;
    }

    /**
     * Suma los pagos recibidos en [desde, hasta), descontando devoluciones y anulaciones de pagos
     * registradas en ese mismo periodo. Los cargos no se consideran ingresos recibidos.
     */
    public ReporteIngresos generarIngresos(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        BigDecimal pagos = BigDecimal.ZERO;
        BigDecimal devoluciones = BigDecimal.ZERO;
        BigDecimal anulaciones = BigDecimal.ZERO;

        for (Huesped huesped : huespedes) {
            for (Reserva reserva : huesped.getHistorial()) {
                Folio folio = reserva.getFolio();
                for (int i = 0; i < folio.numeroMovimientos(); i++) {
                    MovimientoFolio movimiento = folio.obtenerMovimiento(i);
                    LocalDate fecha = movimiento.getFecha().toLocalDate();
                    if (fecha.isBefore(desde) || !fecha.isBefore(hasta)) continue;
                    switch (movimiento.getTipo()) {
                        case PAGO -> pagos = pagos.add(movimiento.getValor());
                        case DEVOLUCION -> devoluciones = devoluciones.add(movimiento.getValor());
                        case ANULACION_PAGO -> anulaciones = anulaciones.add(movimiento.getValor());
                        default -> { }
                    }
                }
            }
        }
        return new ReporteIngresos(pagos, devoluciones, anulaciones);
    }

    /** Cuenta todos los estados, incluidos CANCELADA y NO_SHOW, a partir del historial de titulares. */
    public ListaDoblementeEnlazada<ReporteReservasCanal> generarReservasPorCanal() {
        ListaDoblementeEnlazada<ReporteReservasCanal> resultado = new ListaDoblementeEnlazada<>();
        for (CanalReserva canal : CanalReserva.values()) {
            int[] cantidades = new int[EstadoReserva.values().length];
            for (Huesped huesped : huespedes) {
                for (Reserva reserva : huesped.getHistorial()) {
                    if (reserva.getCanal() == canal) {
                        cantidades[reserva.getEstado().ordinal()]++;
                    }
                }
            }
            resultado.agregar(new ReporteReservasCanal(canal, cantidades));
        }
        return resultado;
    }

    private void validarPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || !hasta.isAfter(desde)) {
            throw new IllegalArgumentException("El periodo debe ser valido y la fecha final posterior a la inicial");
        }
    }
}
