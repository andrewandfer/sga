package model.modelo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/** Representa el turno de preparación de un apartamento después de un checkout. */
public class SolicitudPreparacion {

    private final Apartamento apartamento;
    private final LocalDateTime llegadaEstimada;
    private final LocalDateTime registradaEn;
    private final int prioridad;
    private LocalDateTime inicioPreparacion;
    private LocalDateTime finPreparacion;

    public SolicitudPreparacion(Apartamento apartamento, LocalDateTime llegadaEstimada,
                                LocalDateTime registradaEn, int prioridad) {
        this.apartamento = Objects.requireNonNull(apartamento, "El apartamento es obligatorio");
        this.registradaEn = Objects.requireNonNull(registradaEn, "La fecha de registro es obligatoria");
        if (prioridad != 1 && prioridad != 2) {
            throw new IllegalArgumentException("La prioridad debe ser 1 o 2");
        }
        this.llegadaEstimada = llegadaEstimada;
        this.prioridad = prioridad;
    }

    public void iniciar(LocalDateTime fecha) {
        if (inicioPreparacion != null || finPreparacion != null) {
            throw new IllegalStateException("La solicitud ya fue iniciada o finalizada");
        }
        if (fecha == null || fecha.isBefore(registradaEn)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser anterior al registro");
        }
        inicioPreparacion = fecha;
    }

    public void finalizar(LocalDateTime fecha) {
        if (inicioPreparacion == null || finPreparacion != null) {
            throw new IllegalStateException("La preparación no está en curso");
        }
        if (fecha == null || fecha.isBefore(inicioPreparacion)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior al inicio");
        }
        finPreparacion = fecha;
    }

    public long duracionMinutos() {
        if (inicioPreparacion == null || finPreparacion == null) {
            throw new IllegalStateException("La preparación todavía no ha finalizado");
        }
        return Duration.between(inicioPreparacion, finPreparacion).toMinutes();
    }

    public Apartamento getApartamento() { return apartamento; }
    public LocalDateTime getLlegadaEstimada() { return llegadaEstimada; }
    public LocalDateTime getRegistradaEn() { return registradaEn; }
    public int getPrioridad() { return prioridad; }
    public LocalDateTime getInicioPreparacion() { return inicioPreparacion; }
    public LocalDateTime getFinPreparacion() { return finPreparacion; }

    @Override
    public boolean equals(Object otro) {
        return otro instanceof SolicitudPreparacion
                && apartamento.equals(((SolicitudPreparacion) otro).apartamento);
    }

    @Override
    public int hashCode() { return apartamento.hashCode(); }
}
