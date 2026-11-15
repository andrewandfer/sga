package model.estructuras;

import model.estructuras.ListaDoblementeEnlazada.NodoDoble;
import model.excepciones.EstructuraVaciaException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
/**
 * Representa una lista circular doblemente enlazada de elementos genéricos.
 *
 * <p>La lista mantiene una referencia al nodo actual, desde el cual se puede
 * avanzar circularmente. Cada nodo está enlazado con su anterior y siguiente,
 * incluyendo los extremos de la lista.</p>
 *
 * @param <T> tipo de elementos almacenados en la lista.
 */
public class ListaCircular<T> implements Iterable<T> {
    /**
     * Nodo desde el cual se realizan las operaciones de consulta y recorrido.
     */
    private NodoDoble<T> actual;
    /**
     * Cantidad de elementos almacenados en la lista.
     */
    private int tamano;
    /**
     * Inicializa una lista circular vacía.
     */
    public ListaCircular() {
        this.actual = null;
        this.tamano = 0;
    }
    /**
     * Agrega un elemento al final de la lista, justo antes del nodo actual.
     *
     * <p>Si la lista está vacía, el nuevo nodo se enlaza consigo mismo
     * y pasa a ser el nodo actual.</p>
     *
     * @param dato elemento que se desea agregar.
     */
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
    /**
     * Obtiene el dato del nodo actual y avanza la referencia al siguiente nodo.
     *
     * @return dato del nodo que era actual antes de avanzar.
     * @throws NoSuchElementException si la lista está vacía.
     */
    public T siguiente() {
        if (actual == null) {
            throw new EstructuraVaciaException("La lista circular esta vacia");
        }
        T dato = actual.getDato();
        actual = actual.getSiguiente();
        return dato;
    }
    /**
     * Consulta el dato del nodo actual sin avanzar la referencia.
     *
     * @return dato almacenado en el nodo actual.
     * @throws NoSuchElementException si la lista está vacía.
     */
    public T actual() {
        if (actual == null) {
            throw new EstructuraVaciaException("La lista circular esta vacia");
        }
        return actual.getDato();
    }
    /**
     * Busca y elimina la primera aparición de un dato en la lista.
     *
     * <p>La búsqueda comienza en el nodo actual y recorre como máximo
     * {@code tamano} nodos. Si se elimina el nodo actual, la referencia
     * avanza al nodo siguiente.</p>
     *
     * @param dato elemento que se desea eliminar.
     * @return true si el elemento fue encontrado y eliminado; false si no existe.
     */
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
    /**
     * Obtiene la cantidad de elementos almacenados en la lista.
     *
     * @return número de elementos de la lista.
     */
    public int tamano() {
        return tamano;
    }
    /**
     * Comprueba si la lista no contiene elementos.
     *
     * @return true si la lista está vacía; false en caso contrario.
     */
    public boolean estaVacia() {
        return tamano == 0;
    }
    /**
     * Elimina todos los elementos de la lista restableciendo su estado inicial.
     */
    public void limpiar() {
        actual = null;
        tamano = 0;
    }
    /**
     * Obtiene un iterador que recorre los elementos desde el nodo actual.
     *
     * @return iterador de la lista circular.
     */
    @Override
    public Iterator<T> iterator() {
        return new IteradorCircular();
    }
    /**
     * Genera una representación textual de los elementos de la lista,
     * comenzando por el nodo actual y recorriendo una vuelta completa.
     *
     * @return cadena con los elementos separados por comas y delimitados
     *         por corchetes.
     */
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
    /**
     * Iterador que recorre la lista circular desde el nodo actual
     * y se detiene después de entregar todos los elementos existentes
     * al momento de consultar el tamaño.
     */
    private class IteradorCircular implements Iterator<T> {
        /**
         * Nodo que se entregará en la siguiente llamada a {@code next()}.
         */
        private NodoDoble<T> nodo = actual;
        /**
         * Cantidad de elementos que el iterador ya ha entregado.
         */
        private int entregados = 0;
        /**
         * Comprueba si quedan elementos por recorrer en esta vuelta.
         *
         * @return true si todavía no se han entregado tantos elementos
         *         como indica el tamaño actual de la lista.
         */
        @Override
        public boolean hasNext() {
            return entregados < tamano;
        }
        /**
         * Obtiene el siguiente elemento y avanza al nodo siguiente.
         *
         * @return dato del nodo actual del iterador.
         * @throws NoSuchElementException si ya se recorrieron todos los
         *         elementos de la lista.
         */
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