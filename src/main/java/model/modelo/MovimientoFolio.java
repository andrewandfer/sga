package model.modelo;

import model.enums.TipoMovimientoFolio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class MovimientoFolio {

    private final String id;
    private final TipoMovimientoFolio tipo;
    private final String concepto;
    private final BigDecimal valor;
    private final LocalDateTime fecha;
    private final String idMovimientoRelacionado;

    public MovimientoFolio(
            String id,
            TipoMovimientoFolio tipo,
            String concepto,
            BigDecimal valor,
            LocalDateTime fecha,
            String idMovimientoRelacionado) {

        if (id == null || tipo == null || concepto == null
                || concepto.isBlank() || valor == null || fecha == null) {
            throw new IllegalArgumentException(
                    "Los datos del movimiento son obligatorios");
        }

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El valor debe ser mayor que cero");
        }

        this.id = id;
        this.tipo = tipo;
        this.concepto = concepto;
        this.valor = valor;
        this.fecha = fecha;
        this.idMovimientoRelacionado = idMovimientoRelacionado;
    }

    public String getId() {
        return id;
    }

    public TipoMovimientoFolio getTipo() {
        return tipo;
    }

    public String getConcepto() {
        return concepto;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getIdMovimientoRelacionado() {
        return idMovimientoRelacionado;
    }

    @Override
    public String toString() {
        return tipo + " | " + concepto + " | $" + valor;
    }
}