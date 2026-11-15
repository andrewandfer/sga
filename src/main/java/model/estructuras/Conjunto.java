package model.estructuras;

import model.estructuras.ListaDoblementeEnlazada.NodoDoble;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
/**
 * Representa un conjunto de elementos genéricos que no admite valores nulos
 * ni elementos duplicados.
 *
 * <p>Almacena sus elementos mediante una {@link ListaDoblementeEnlazada},
 * utilizando dicha estructura para realizar las operaciones de búsqueda,
 * eliminación e iteración.</p>
 *
 * @param <T> tipo de elementos almacenados en el conjunto.
 */
public class Conjunto<T> implements Iterable<T> {
    /**
     * Lista doblemente enlazada que almacena los elementos del conjunto.
     */
    private final ListaDoblementeEnlazada<T> elementos = new ListaDoblementeEnlazada<>();
    /**
     * Agrega un elemento al conjunto si todavía no está presente.
     *
     * @param dato elemento que se desea agregar.
     * @return true si el elemento fue agregado; false si ya existía.
     * @throws NullPointerException si el elemento es null.
     */
    public boolean agregar(T dato) {
        Objects.requireNonNull(dato, "El conjunto no admite elementos nulos");
        if (contiene(dato)) {
            return false;
        }
        elementos.agregar(dato);
        return true;
    }
    /**
     * Comprueba si el conjunto contiene un elemento.
     *
     * @param dato elemento cuya presencia se desea comprobar.
     * @return true si el elemento está presente; false en caso contrario.
     */
    public boolean contiene(T dato) {
        return elementos.contiene(dato);
    }
    /**
     * Elimina un elemento del conjunto.
     *
     * @param dato elemento que se desea eliminar.
     * @return true si el elemento fue encontrado y eliminado; false si no existe.
     */
    public boolean eliminar(T dato) {
        return elementos.eliminar(dato);
    }
    /**
     * Obtiene la cantidad de elementos almacenados en el conjunto.
     *
     * @return número de elementos del conjunto.
     */
    public int tamano() {
        return elementos.tamano();
    }
    /**
     * Comprueba si el conjunto no contiene elementos.
     *
     * @return true si el conjunto está vacío; false en caso contrario.
     */
    public boolean estaVacio() {
        return elementos.estaVacia();
    }

    /** Iterador propio del conjunto: recorre los nodos internos en orden de insercion. */
    @Override
    public Iterator<T> iterator() {
        return new IteradorConjunto();
    }

    private class IteradorConjunto implements Iterator<T> {

        private NodoDoble<T> actual = elementos.getCabeza();

        @Override
        public boolean hasNext() {
            return actual != null;
        }

        @Override
        public T next() {
            if (actual == null) {
                throw new NoSuchElementException("No hay mas elementos en el conjunto");
            }
            T dato = actual.getDato();
            actual = actual.getSiguiente();
            return dato;
        }
    }
    /**
     * Obtiene la representación textual de los elementos del conjunto.
     *
     * @return representación textual de la lista que almacena los elementos.
     */
    @Override
    public String toString() {
        return elementos.toString();
    }
}