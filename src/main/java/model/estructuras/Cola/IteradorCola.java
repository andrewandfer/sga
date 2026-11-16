
package model.estructuras.Cola;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteradorCola<T> implements Iterator<T> {

    private NodoCola<T> actual;

    public IteradorCola(NodoCola<T> primero) {
        this.actual = primero;
    }

    @Override
    public boolean hasNext() {
        return actual != null;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException(
                    "No hay más elementos en la cola"
            );
        }

        T dato = actual.getDato();
        actual = actual.getSiguiente();

        return dato;
    }
}
