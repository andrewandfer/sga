package model.modelo;

import java.math.BigDecimal;

/** Resumen de dinero efectivamente recibido en un periodo. */
public class ReporteIngresos {

    private final BigDecimal pagos;
    private final BigDecimal devoluciones;
    private final BigDecimal anulacionesPagos;

    public ReporteIngresos(BigDecimal pagos, BigDecimal devoluciones, BigDecimal anulacionesPagos) {
        this.pagos = pagos;
        this.devoluciones = devoluciones;
        this.anulacionesPagos = anulacionesPagos;
    }

    public BigDecimal getPagos() { return pagos; }
    public BigDecimal getDevoluciones() { return devoluciones; }
    public BigDecimal getAnulacionesPagos() { return anulacionesPagos; }

    public BigDecimal getIngresosNetos() {
        return pagos.subtract(devoluciones).subtract(anulacionesPagos);
    }

    @Override
    public String toString() {
        return "Pagos: $" + pagos + " | Devoluciones: $" + devoluciones
                + " | Anulaciones: $" + anulacionesPagos + " | Neto: $" + getIngresosNetos();
    }
}
