package model.modelo;
import model.estructuras.ListaDoblementeEnlazada;
/**
 * Representa una cotización de alojamiento.
 * Almacena el importe total y el desglose de las noches cotizadas.
 */
public class Cotizacion {

    private final double total;

    private final ListaDoblementeEnlazada<LineaNoche> desglose;

    /**
     * Construye una cotización con su importe total y desglose.
     *
     * @param total importe total de la cotización
     * @param desglose lista con el detalle de cada noche cotizada
     */
    public Cotizacion(double total, ListaDoblementeEnlazada<LineaNoche> desglose) {
        this.total = total;
        this.desglose = desglose;
    }
    /**
     * Obtiene el importe total de la cotización.
     *
     * @return importe total
     */
    public double getTotal() {
        return total;
    }
    /**
     * Obtiene el desglose de las noches cotizadas.
     *
     * @return lista con las líneas de detalle
     */
    public ListaDoblementeEnlazada<LineaNoche> getDesglose() {
        return desglose;
    }
    /**
     * Genera una representación textual de la cotización,
     * incluyendo cada línea del desglose y el importe total.
     *
     * @return representación textual de la cotización
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (LineaNoche linea : desglose) {
            sb.append(linea).append(System.lineSeparator());
        }
        return sb.append("TOTAL: ").append(total).toString();
    }
}
