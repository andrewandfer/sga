package model.servicios;

import model.estructuras.ListaDoblementeEnlazada;
import model.excepciones.CapacidadExcedidaException;
import model.modelo.Apartamento;
/**
 * Asigna un grupo de personas a un conjunto de apartamentos disponibles.
 * Busca una combinación que minimice la cantidad de apartamentos utilizados
 * y, en caso de empate, la capacidad total asignada.
 */
public class AsignadorGrupos {

    /**
     * Selecciona los apartamentos necesarios para alojar a un grupo de personas.
     *
     * @param personas cantidad de personas que deben alojarse
     * @param libres lista de apartamentos disponibles
     * @return lista de apartamentos seleccionados
     * @throws IllegalArgumentException si la cantidad de personas es menor o igual a cero
     * @throws CapacidadExcedidaException si la capacidad total de los apartamentos disponibles es insuficiente
     */
    public ListaDoblementeEnlazada<Apartamento> asignar(int personas, ListaDoblementeEnlazada<Apartamento> libres)
            throws CapacidadExcedidaException {
        if (personas <= 0) {
            throw new IllegalArgumentException("El grupo debe tener al menos una persona");
        }
        int n = libres.tamano();
        Apartamento[] candidatos = new Apartamento[n];
        int i = 0;
        for (Apartamento a : libres) {
            candidatos[i++] = a;
        }
        ordenarPorCapacidadDescendente(candidatos);

        int[] sufijo = new int[n + 1];
        for (int k = n - 1; k >= 0; k--) {
            sufijo[k] = sufijo[k + 1] + candidatos[k].getCapacidadMaxima();
        }
        if (sufijo[0] < personas) {
            throw new CapacidadExcedidaException("Los apartamentos libres suman " + sufijo[0]
                    + " cupos y el grupo es de " + personas + " personas");
        }

        Busqueda busqueda = new Busqueda(candidatos, sufijo, personas);
        busqueda.retroceder(0, 0, 0);

        ListaDoblementeEnlazada<Apartamento> resultado = new ListaDoblementeEnlazada<>();
        for (int k = 0; k < busqueda.mejorCantidad; k++) {
            resultado.agregar(busqueda.mejor[k]);
        }
        return resultado;
    }
    /**
     * Ordena los apartamentos por capacidad máxima descendente.
     * En caso de empate, utiliza el orden natural de los apartamentos.
     *
     * @param v arreglo de apartamentos que se desea ordenar
     */
    private void ordenarPorCapacidadDescendente(Apartamento[] v) {
        for (int i = 1; i < v.length; i++) {
            Apartamento clave = v[i];
            int j = i - 1;
            while (j >= 0 && vaDespues(v[j], clave)) {
                v[j + 1] = v[j];
                j--;
            }
            v[j + 1] = clave;
        }
    }
    /**
     * Determina si un apartamento debe ubicarse después de otro
     * según los criterios de ordenamiento establecidos.
     *
     * @param a primer apartamento
     * @param b segundo apartamento
     * @return {@code true} si el primer apartamento debe ir después del segundo
     */
    private boolean vaDespues(Apartamento a, Apartamento b) {
        if (a.getCapacidadMaxima() != b.getCapacidadMaxima()) {
            return a.getCapacidadMaxima() < b.getCapacidadMaxima();
        }
        return a.compareTo(b) > 0;
    }
    /**
     * Realiza una búsqueda por retroceso para encontrar la combinación
     * de apartamentos que mejor satisface la capacidad requerida.
     */
    private static final class Busqueda {
        private final Apartamento[] candidatos;
        private final int[] sufijo;
        private final int objetivo;
        private final Apartamento[] actual;
        private final Apartamento[] mejor;
        private int mejorCantidad = Integer.MAX_VALUE;
        private int mejorCapacidad = Integer.MAX_VALUE;
        /**
         * Inicializa la búsqueda con los apartamentos candidatos,
         * las capacidades acumuladas y la cantidad de personas requerida.
         *
         * @param candidatos apartamentos disponibles para la búsqueda
         * @param sufijo capacidad acumulada desde cada posición del arreglo
         * @param objetivo capacidad mínima requerida
         */
        Busqueda(Apartamento[] candidatos, int[] sufijo, int objetivo) {
            this.candidatos = candidatos;
            this.sufijo = sufijo;
            this.objetivo = objetivo;
            this.actual = new Apartamento[candidatos.length];
            this.mejor = new Apartamento[candidatos.length];
        }

        /**
         * Explora las combinaciones posibles mediante retroceso.
         * Descarta las ramas que no pueden mejorar la solución encontrada
         * o que no cuentan con capacidad suficiente para alcanzar el objetivo.
         *
         * @param indice posición del siguiente candidato por evaluar
         * @param cantidad cantidad de apartamentos seleccionados
         * @param capacidad capacidad acumulada de los apartamentos seleccionados
         */
        void retroceder(int indice, int cantidad, int capacidad) {
            if (capacidad >= objetivo) {
                registrar(cantidad, capacidad);
                return;
            }
            if (indice == candidatos.length) {
                return;
            }
            if (cantidad >= mejorCantidad) {
                return;
            }
            if (capacidad + sufijo[indice] < objetivo) {
                return;
            }
            actual[cantidad] = candidatos[indice];
            retroceder(indice + 1, cantidad + 1, capacidad + candidatos[indice].getCapacidadMaxima());
            actual[cantidad] = null;
            retroceder(indice + 1, cantidad, capacidad);
        }
        /**
         * Registra una combinación si utiliza menos apartamentos que la mejor
         * encontrada o, en caso de empate, si tiene menor capacidad total.
         *
         * @param cantidad cantidad de apartamentos de la combinación
         * @param capacidad capacidad total de la combinación
         */
        private void registrar(int cantidad, int capacidad) {
            if (cantidad < mejorCantidad || (cantidad == mejorCantidad && capacidad < mejorCapacidad)) {
                mejorCantidad = cantidad;
                mejorCapacidad = capacidad;
                for (int k = 0; k < cantidad; k++) {
                                mejor[k] = actual[k];
                }
            }
        }
    }
}
