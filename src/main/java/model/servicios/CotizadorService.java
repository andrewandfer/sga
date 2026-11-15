package model.servicios;

import model.enums.Temporada;
import model.estructuras.ListaDoblementeEnlazada;
import model.excepciones.CapacidadExcedidaException;
import model.modelo.*;

import java.time.LocalDate;
import java.util.Iterator;

/**
 * Servicio encargado de calcular el costo de una estancia
 * según las tarifas del apartamento, las temporadas y los ocupantes.
 */
public class CotizadorService {

    private final GestorTemporadas temporadas;
    /**
     * Crea el servicio de cotización asociado al gestor de temporadas.
     *
     * @param temporadas gestor que determina la temporada correspondiente a cada fecha
     */
    public CotizadorService(GestorTemporadas temporadas) {
        this.temporadas = temporadas;
    }
    /**
     * Calcula el costo de una estancia y genera un desglose por noche.
     * Solo se cobra la tarifa a los ocupantes que cumplen la edad mínima establecida.
     *
     * @param apartamento apartamento cuya tarifa se utilizará
     * @param estancia periodo que se desea cotizar
     * @param ocupantes lista de personas que ocuparán el apartamento
     * @return cotización con el valor total y el desglose de cada noche
     * @throws IllegalArgumentException si algún parámetro es nulo o no hay ocupantes
     * @throws CapacidadExcedidaException si la cantidad de ocupantes supera la capacidad máxima del apartamento
     */
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
    /**
     * Cuenta los ocupantes que deben pagar la tarifa en una noche determinada.
     *
     * @param ocupantes lista de ocupantes de la estancia
     * @param noche fecha de la noche que se evalúa
     * @return cantidad de ocupantes que pagan
     */
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
