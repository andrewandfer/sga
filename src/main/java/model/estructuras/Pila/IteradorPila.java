package model.estructuras.Pila;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteradorPila<T> implements Iterator<T> {
    private NodoPila<T> actual;

    public IteradorPila(NodoPila<T> tope) {
        this.actual = tope;
    }

    @Override
    public boolean hasNext() {
        return actual != null;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No hay más elementos en la pila");
        }

        T dato = actual.getDato();
        actual = actual.getSiguiente();

        return dato;
    }
}
}
