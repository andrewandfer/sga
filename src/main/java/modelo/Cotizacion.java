package modelo;
import estructuras.ListaDoblementeEnlazada;

public class Cotizacion {

    private final double total;
    private final ListaDoblementeEnlazada<LineaNoche> desglose;

    public Cotizacion(double total, ListaDoblementeEnlazada<LineaNoche> desglose) {
        this.total = total;
        this.desglose = desglose;
    }

    public double getTotal() {
        return total;
    }

    public ListaDoblementeEnlazada<LineaNoche> getDesglose() {
        return desglose;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (LineaNoche linea : desglose) {
            sb.append(linea).append(System.lineSeparator());
        }
        return sb.append("TOTAL: ").append(total).toString();
    }
}
