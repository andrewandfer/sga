package model.estructuras;

import model.estructuras.listaDoble.NodoDoble;
import model.excepciones.EstructuraVaciaException;

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

        if (estaVacia()) {

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

        if (estaVacia()) {
            throw new EstructuraVaciaException(
                    "La lista circular está vacía"
            );
        }

        T dato = actual.getDato();
        actual = actual.getSiguiente();

        return dato;
    }

    public T actual() {

        if (estaVacia()) {
            throw new EstructuraVaciaException(
                    "La lista circular está vacía"
            );
        }

        return actual.getDato();
    }

    public boolean eliminar(T dato) {

        if (estaVacia()) {
            return false;
        }

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

        if (estaVacia()) {
            return;
        }

        NodoDoble<T> nodo = actual;

        for (int i = 0; i < tamano; i++) {

            NodoDoble<T> siguiente = nodo.getSiguiente();

            nodo.setSiguiente(null);
            nodo.setAnterior(null);

            nodo = siguiente;
        }

        actual = null;
        tamano = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new IteradorCircular(actual, tamano);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("[");

        NodoDoble<T> nodo = actual;

        for (int i = 0; i < tamano; i++) {

            sb.append(nodo.getDato());

            if (i < tamano - 1) {
                sb.append(", ");
            }

            nodo = nodo.getSiguiente();
        }

        return sb.append("]").toString();
    }

    private class IteradorCircular implements Iterator<T> {

        private NodoDoble<T> nodo;
        private int entregados;
        private final int limite;

        public IteradorCircular(
                NodoDoble<T> inicio,
                int limite) {

            this.nodo = inicio;
            this.entregados = 0;
            this.limite = limite;
        }

        @Override
        public boolean hasNext() {
            return entregados < limite;
        }

        @Override
        public T next() {

            if (!hasNext()) {
                throw new NoSuchElementException(
                        "Se completó la vuelta de la lista circular"
                );
            }

            T dato = nodo.getDato();

            nodo = nodo.getSiguiente();
            entregados++;

            return dato;
        }
    }
}