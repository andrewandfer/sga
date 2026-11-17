package model.modelo;

import model.enums.CanalReserva;
import model.enums.EstadoReserva;

/** Conteo de reservas de un canal, discriminado por estado. */
public class ReporteReservasCanal {

    private final CanalReserva canal;
    private final int[] cantidades;

    public ReporteReservasCanal(CanalReserva canal, int[] cantidades) {
        this.canal = canal;
        this.cantidades = cantidades.clone();
    }

    public CanalReserva getCanal() { return canal; }

    public int getCantidad(EstadoReserva estado) {
        return cantidades[estado.ordinal()];
    }

    public int getTotal() {
        int total = 0;
        for (int cantidad : cantidades) total += cantidad;
        return total;
    }

    @Override
    public String toString() {
        StringBuilder texto = new StringBuilder(canal + " | total=" + getTotal());
        for (EstadoReserva estado : EstadoReserva.values()) {
            texto.append(" | ").append(estado).append('=').append(getCantidad(estado));
        }
        return texto.toString();
    }
}
