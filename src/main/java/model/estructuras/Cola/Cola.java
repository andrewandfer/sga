
package model.estructuras.Cola;

import model.excepciones.EstructuraVaciaException;

import java.util.Iterator;
import java.util.Objects;

public class Cola<T> implements Iterable<T> {

    private NodoCola<T> frente;
    private NodoCola<T> finalCola;
    private int tamanio;

    public Cola() {
        frente = null;
        finalCola = null;
        tamanio = 0;
    }

    // Inserta un elemento al final de la cola.
    public void add(T dato) {
        NodoCola<T> nuevo = new NodoCola<>(dato);

        if (isEmpty()) {
            frente = nuevo;
            finalCola = nuevo;
        } else {
            finalCola.setSiguiente(nuevo);
            finalCola = nuevo;
        }

        tamanio++;
    }

    // Retira y devuelve el primer elemento.
    public T poll() {
        if (isEmpty()) {
            throw new EstructuraVaciaException("La cola está vacía");
        }

        T dato = frente.getDato();
        NodoCola<T> eliminado = frente;

        frente = frente.getSiguiente();
        eliminado.setSiguiente(null);

        tamanio--;

        if (frente == null) {
            finalCola = null;
        }

        return dato;
    }

    // Consulta el primer elemento sin retirarlo.
    public T peek() {
        if (isEmpty()) {
            throw new EstructuraVaciaException("La cola está vacía");
        }

        return frente.getDato();
    }

    public boolean isEmpty() {
        return tamanio == 0;
    }

    public int size() {
        return tamanio;
    }

    // Comprueba si un elemento está en la cola.
    public boolean contains(T dato) {
        for (T elemento : this) {
            if (Objects.equals(elemento, dato)) {
                return true;
            }
        }

        return false;
    }

    // Elimina todos los elementos.
    public void clear() {
        NodoCola<T> actual = frente;

        while (actual != null) {
            NodoCola<T> siguiente = actual.getSiguiente();
            actual.setSiguiente(null);
            actual = siguiente;
        }

        frente = null;
        finalCola = null;
        tamanio = 0;
    }

    // Agrega los elementos de otra cola sin vaciarla.
    public void merge(Cola<T> otra) {
        Objects.requireNonNull(otra, "La cola no puede ser null");

        if (otra == this) {
            throw new IllegalArgumentException(
                    "No se puede fusionar una cola consigo misma"
            );
        }

        for (T elemento : otra) {
            this.add(elemento);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new IteradorCola<>(frente);
    }

    @Override
    public String toString() {
        StringBuilder resultado = new StringBuilder("[");
        Iterator<T> iterador = iterator();

        while (iterador.hasNext()) {
            resultado.append(iterador.next());

            if (iterador.hasNext()) {
                resultado.append(", ");
            }
        }

        return resultado.append("]").toString();
    }
}
