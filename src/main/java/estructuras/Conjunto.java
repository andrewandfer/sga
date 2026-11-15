package estructuras;

import estructuras.ListaDoblementeEnlazada.NodoDoble;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class Conjunto<T> implements Iterable<T> {

    private final ListaDoblementeEnlazada<T> elementos = new ListaDoblementeEnlazada<>();

    public boolean agregar(T dato) {
        Objects.requireNonNull(dato, "El conjunto no admite elementos nulos");
        if (contiene(dato)) {
            return false;
        }
        elementos.agregar(dato);
        return true;
    }

    public boolean contiene(T dato) {
        return elementos.contiene(dato);
    }

    public boolean eliminar(T dato) {
        return elementos.eliminar(dato);
    }

    public int tamano() {
        return elementos.tamano();
    }

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

    @Override
    public String toString() {
        return elementos.toString();
    }
}