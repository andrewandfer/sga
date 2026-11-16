
package model.estructuras;

import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.estructuras.listaDoble.NodoDoble;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Conjunto<T> implements Iterable<T> {

    private final ListaDoblementeEnlazada<T> elementos =
            new ListaDoblementeEnlazada<>();

    public boolean agregar(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException(
                    "El conjunto no admite elementos nulos"
            );
        }

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

    /**
     * Recorre los elementos en orden de inserción.
     */
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
            if (!hasNext()) {
                throw new NoSuchElementException(
                        "No hay más elementos en el conjunto"
                );
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
