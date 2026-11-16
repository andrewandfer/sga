
package model.estructuras.listaDoble;

import model.excepciones.EstructuraVaciaException;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class ListaDoblementeEnlazada<T> implements Iterable<T> {

    private NodoDoble<T> cabeza;
    private NodoDoble<T> cola;
    private int tamano;

    public ListaDoblementeEnlazada() {
        cabeza = null;
        cola = null;
        tamano = 0;
    }

    // Agrega un elemento al final.
    public void agregar(T dato) {
        NodoDoble<T> nuevo = new NodoDoble<>(dato);

        if (estaVacia()) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            cola.setSiguiente(nuevo);
            nuevo.setAnterior(cola);
            cola = nuevo;
        }

        tamano++;
    }

    // Inserta un elemento en una posición determinada.
    public void insertarEn(int indice, T dato) {
        if (indice < 0 || indice > tamano) {
            throw new IndexOutOfBoundsException(
                    "Indice " + indice + " fuera de rango [0, " + tamano + "]"
            );
        }

        if (indice == tamano) {
            agregar(dato);
            return;
        }

        NodoDoble<T> actual = nodoEn(indice);
        NodoDoble<T> previo = actual.getAnterior();

        NodoDoble<T> nuevo = new NodoDoble<>(dato);

        nuevo.setAnterior(previo);
        nuevo.setSiguiente(actual);
        actual.setAnterior(nuevo);

        if (previo == null) {
            cabeza = nuevo;
        } else {
            previo.setSiguiente(nuevo);
        }

        tamano++;
    }

    // Inserta manteniendo el orden definido por el comparador.
    public void insertarOrdenado(T dato, Comparator<? super T> comparador) {
        Objects.requireNonNull(comparador, "El comparador no puede ser null");

        NodoDoble<T> actual = cabeza;
        int posicion = 0;

        while (actual != null &&
                comparador.compare(dato, actual.getDato()) >= 0) {

            actual = actual.getSiguiente();
            posicion++;
        }

        insertarEn(posicion, dato);
    }

    // Elimina la primera aparición del elemento.
    public boolean eliminar(T dato) {
        NodoDoble<T> actual = cabeza;

        while (actual != null) {
            if (Objects.equals(actual.getDato(), dato)) {
                desenlazar(actual);
                return true;
            }

            actual = actual.getSiguiente();
        }

        return false;
    }

    // Elimina y devuelve el elemento de una posición.
    public T eliminarEn(int indice) {
        if (estaVacia()) {
            throw new EstructuraVaciaException(
                    "No se puede eliminar: la lista esta vacia"
            );
        }

        validarIndice(indice);

        NodoDoble<T> nodo = nodoEn(indice);
        T dato = nodo.getDato();

        desenlazar(nodo);

        return dato;
    }

    // Obtiene el elemento de una posición.
    public T obtener(int indice) {
        if (estaVacia()) {
            throw new EstructuraVaciaException(
                    "No se puede obtener: la lista esta vacia"
            );
        }

        validarIndice(indice);

        return nodoEn(indice).getDato();
    }

    // Obtiene el primer elemento.
    public T primero() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La lista esta vacia");
        }

        return cabeza.getDato();
    }

    // Obtiene el último elemento.
    public T ultimo() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La lista esta vacia");
        }

        return cola.getDato();
    }

    // Comprueba si contiene un elemento.
    public boolean contiene(T dato) {
        return indiceDe(dato) >= 0;
    }

    // Devuelve la posición de un elemento.
    public int indiceDe(T dato) {
        int indice = 0;
        NodoDoble<T> actual = cabeza;

        while (actual != null) {
            if (Objects.equals(actual.getDato(), dato)) {
                return indice;
            }

            actual = actual.getSiguiente();
            indice++;
        }

        return -1;
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    // Elimina todos los elementos.
    public void limpiar() {
        NodoDoble<T> actual = cabeza;

        while (actual != null) {
            NodoDoble<T> siguiente = actual.getSiguiente();
            actual.setAnterior(null);
            actual.setSiguiente(null);
            actual = siguiente;
        }

        cabeza = null;
        cola = null;
        tamano = 0;
    }

    // Une otra lista sin duplicar elementos.
    public void merge(ListaDoblementeEnlazada<T> lista) {
        Objects.requireNonNull(lista, "La lista no puede ser null");

        if (lista == this) {
            return;
        }

        for (T elemento : lista) {
            if (!contiene(elemento)) {
                agregar(elemento);
            }
        }
    }

    // Recorre la lista desde la cabeza.
    @Override
    public Iterator<T> iterator() {
        return new IteradorLista();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        NodoDoble<T> actual = cabeza;

        while (actual != null) {
            sb.append(actual.getDato());

            if (actual.getSiguiente() != null) {
                sb.append(", ");
            }

            actual = actual.getSiguiente();
        }

        return sb.append("]").toString();
    }

    // Comprueba que el índice corresponda a un elemento existente.
    private void validarIndice(int indice) {
        if (indice < 0 || indice >= tamano) {
            throw new IndexOutOfBoundsException(
                    "Indice " + indice + " fuera de rango [0, " + (tamano - 1) + "]"
            );
        }
    }

    // Busca desde el extremo más cercano.
    private NodoDoble<T> nodoEn(int indice) {
        NodoDoble<T> actual;

        if (indice < tamano / 2) {
            actual = cabeza;

            for (int i = 0; i < indice; i++) {
                actual = actual.getSiguiente();
            }
        } else {
            actual = cola;

            for (int i = tamano - 1; i > indice; i--) {
                actual = actual.getAnterior();
            }
        }

        return actual;
    }

    // Desconecta un nodo y actualiza los extremos.
    private void desenlazar(NodoDoble<T> nodo) {
        NodoDoble<T> previo = nodo.getAnterior();
        NodoDoble<T> siguiente = nodo.getSiguiente();

        if (previo == null) {
            cabeza = siguiente;
        } else {
            previo.setSiguiente(siguiente);
        }

        if (siguiente == null) {
            cola = previo;
        } else {
            siguiente.setAnterior(previo);
        }

        nodo.setAnterior(null);
        nodo.setSiguiente(null);

        tamano--;
    }

    private class IteradorLista implements Iterator<T> {

        private NodoDoble<T> siguiente = cabeza;

        @Override
        public boolean hasNext() {
            return siguiente != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException(
                        "No hay mas elementos en la lista"
                );
            }

            T dato = siguiente.getDato();
            siguiente = siguiente.getSiguiente();

            return dato;
        }
    }

    public NodoDoble<T> getCabeza() {
        return cabeza;
    }

    public NodoDoble<T> getCola() {
        return cola;
    }

    public int getTamano() {
        return tamano;
    }
}
