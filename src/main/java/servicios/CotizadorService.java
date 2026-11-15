package servicios;

import enums.Temporada;
import estructuras.ListaDoblementeEnlazada;
import excepciones.CapacidadExcedidaException;
import modelo.*;

import java.time.LocalDate;
import java.util.Iterator;

public class CotizadorService {

    private final GestorTemporadas temporadas;

    public CotizadorService(GestorTemporadas temporadas) {
        this.temporadas = temporadas;
    }

    public Cotizacion cotizar(Apartamento apartamento, Estancia estancia,
                              ListaDoblementeEnlazada<Ocupante> ocupantes) throws CapacidadExcedidaException {
        if (apartamento == null || estancia == null || ocupantes == null || ocupantes.estaVacia()) {
            throw new IllegalArgumentException("Apartamento, estancia y al menos un ocupante son obligatorios");
        }
        if (ocupantes.tamano() > apartamento.getCapacidadMaxima()) {
            throw new CapacidadExcedidaException("El apartamento " + apartamento.getCodigo() + " admite maximo "
                    + apartamento.getCapacidadMaxima() + " personas y se indicaron " + ocupantes.tamano());
        }

        ListaDoblementeEnlazada<LineaNoche> desglose = new ListaDoblementeEnlazada<>();
        for (LocalDate noche : estancia) {
            Temporada temporada = temporadas.temporadaDe(noche);
            int pagan = contarQuienesPagan(ocupantes, noche);
            double valor = pagan * apartamento.tarifaPara(temporada);
            desglose.agregar(new LineaNoche(noche, temporada, pagan, valor));
        }
        return new Cotizacion(sumarRecursivo(desglose.iterator()), desglose);
    }

    private int contarQuienesPagan(ListaDoblementeEnlazada<Ocupante> ocupantes, LocalDate noche) {
        int pagan = 0;
        for (Ocupante o : ocupantes) {
            if (o.pagaCargo(noche)) {
                pagan++;
            }
        }
        return pagan;
    }

    /** Liquidacion recursiva: valor de la noche actual + valor de las noches restantes. */
    private double sumarRecursivo(Iterator<LineaNoche> noches) {
        if (!noches.hasNext()) {
            return 0;
        }
        double actual = noches.next().getValor();
        return actual + sumarRecursivo(noches);
    }
}
