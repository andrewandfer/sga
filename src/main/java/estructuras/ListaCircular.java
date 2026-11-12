package estructuras;

import estructuras.ListaDoblementeEnlazada.NodoDoble;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class ListaCircular<T> implements Iterable<T> {

    private NodoDoble<T> actual;
    private int tamano;

    public ListaCircular() {
        this.actual = null;
        this.tamano = 0;
    }

    public void agregar(T dato) {
        NodoDoble<T> nuevo = new NodoDoble<>(dato);
        if (actual == null) {
            nuevo.setSiguiente(nuevo);
            nuevo.setAnterior(nuevo);
            actual = nuevo;
        } else {
            NodoDoble<T> ultimo = actual.getAnterior();
            ultimo.setSiguiente(nuevo);
            nuevo.setAnterior(ultimo);
            nuevo.setSiguiente(actual);
            actual.setAnterior(nuevo);
        }
        tamano++;
    }

    public T siguiente() {
        if (actual == null) {
            throw new NoSuchElementException("La lista circular esta vacia");
        }
        T dato = actual.getDato();
        actual = actual.getSiguiente();
        return dato;
    }

    public T actual() {
        if (actual == null) {
            throw new NoSuchElementException("La lista circular esta vacia");
        }
        return actual.getDato();
    }

    public boolean eliminar(T dato) {
        if (actual == null) return false;

        NodoDoble<T> nodo = actual;
        for (int i = 0; i < tamano; i++) {
            if (Objects.equals(nodo.getDato(), dato)) {
                if (tamano == 1) {
                    actual = null;
                } else {
                    NodoDoble<T> anterior = nodo.getAnterior();
                    NodoDoble<T> siguiente = nodo.getSiguiente();
                    anterior.setSiguiente(siguiente);
                    siguiente.setAnterior(anterior);
                    if (nodo == actual) {
                        actual = siguiente;
                    }
                }
                nodo.setAnterior(null);
                nodo.setSiguiente(null);
                tamano--;
                return true;
            }
            nodo = nodo.getSiguiente();
        }
        return false;
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public void limpiar() {
        actual = null;
        tamano = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new IteradorCircular();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        if (actual != null) {
            NodoDoble<T> temp = actual;
            for (int i = 0; i < tamano; i++) {
                sb.append(temp.getDato());
                if (i < tamano - 1) {
                    sb.append(", ");
                }
                temp = temp.getSiguiente();
            }
        }
        return sb.append("]").toString();
    }

    private class IteradorCircular implements Iterator<T> {
        private NodoDoble<T> nodo = actual;
        private int entregados = 0;

        @Override
        public boolean hasNext() {
            return entregados < tamano;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException("Se completo la vuelta de la lista circular");
            }
            T dato = nodo.getDato();
            nodo = nodo.getSiguiente();
            entregados++;
            return dato;
        }
    }
}