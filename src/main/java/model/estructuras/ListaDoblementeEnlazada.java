package model.estructuras;
import model.excepciones.EstructuraVaciaException;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
/**
 * Representa una lista doblemente enlazada de elementos genéricos.
 *
 * <p>Cada nodo mantiene referencias a su nodo anterior y siguiente.
 * La lista conserva referencias al primer y último nodo, permitiendo
 * recorrerla en ambas direcciones.</p>
 *
 * @param <T> tipo de elementos almacenados en la lista.
 */
public class ListaDoblementeEnlazada<T> implements Iterable<T> {
    /**
     * Representa un nodo de la lista doblemente enlazada.
     *
     * @param <T> tipo de dato almacenado en el nodo.
     */
    public static class NodoDoble<T> {
        /** Dato almacenado en el nodo. */
        private T dato;
        /** Referencia al nodo anterior. */
        private NodoDoble<T> anterior;
        /** Referencia al nodo siguiente. */
        private NodoDoble<T> siguiente;
        /**
         * Crea un nodo con el dato indicado.
         *
         * @param dato elemento que se almacenará en el nodo.
         */
        public NodoDoble(T dato) {
            this.dato = dato;
        }
        /**
         * Obtiene el dato almacenado en el nodo.
         *
         * @return dato del nodo.
         */
        public T getDato() {
            return dato;
        }
        /**
         * Modifica el dato almacenado en el nodo.
         *
         * @param dato nuevo dato del nodo.
         */
        public void setDato(T dato) {
            this.dato = dato;
        }
        /**
         * Obtiene la referencia al nodo anterior.
         *
         * @return nodo anterior, o null si no existe.
         */
        public NodoDoble<T> getAnterior() {
            return anterior;
        }
        /**
         * Modifica la referencia al nodo anterior.
         *
         * @param anterior nodo que se establecerá como anterior.
         */
        public void setAnterior(NodoDoble<T> anterior) {
            this.anterior = anterior;
        }
        /**
         * Obtiene la referencia al nodo siguiente.
         *
         * @return nodo siguiente, o null si no existe.
         */
        public NodoDoble<T> getSiguiente() {
            return siguiente;
        }
        /**
         * Modifica la referencia al nodo siguiente.
         *
         * @param siguiente nodo que se establecerá como siguiente.
         */
        public void setSiguiente(NodoDoble<T> siguiente) {
            this.siguiente = siguiente;
        }
    }
    /** Primer nodo de la lista. */
    private NodoDoble<T> cabeza;
    /** Último nodo de la lista. */
    private NodoDoble<T> cola;
    /** Cantidad de elementos almacenados en la lista. */
    private int tamano;
    /**
     * Agrega un elemento al final de la lista.
     *
     * <p>Si la lista está vacía, el nuevo nodo se establece como cabeza
     * y cola. En caso contrario, se enlaza después del último nodo.</p>
     *
     * @param dato elemento que se desea agregar.
     */
    public void agregar(T dato) {
        NodoDoble<T> nuevo = new NodoDoble<>(dato);
        if (cola == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            cola.setSiguiente(nuevo);
            nuevo.setAnterior(cola);
            cola = nuevo;
        }
        tamano++;
    }
    /**
     * Inserta un elemento en la posición indicada.
     *
     * <p>Los elementos posteriores a la posición se desplazan una posición
     * hacia el final de la lista. Si el índice coincide con el tamaño,
     * el elemento se agrega al final.</p>
     *
     * @param indice posición en la que se insertará el elemento,
     *               comprendida entre 0 y el tamaño de la lista.
     * @param dato elemento que se desea insertar.
     * @throws IndexOutOfBoundsException si el índice es negativo o mayor
     *         que el tamaño de la lista.
     */
    public void insertarEn(int indice, T dato) {
        if (indice < 0 || indice > tamano) {
            throw new IndexOutOfBoundsException("Indice " + indice + " fuera de rango [0, " + tamano + "]");
        }
        if (indice == tamano) {
            agregar(dato);
            return;
        }
        NodoDoble<T> actual = nodoEn(indice);
        NodoDoble<T> previo = actual.getAnterior();
        NodoDoble<T> nuevo = new NodoDoble<>(dato);
        nuevo.setSiguiente(actual);
        nuevo.setAnterior(previo);
        actual.setAnterior(nuevo);
        if (previo == null) {
            cabeza = nuevo;
        } else {
            previo.setSiguiente(nuevo);
        }
        tamano++;
    }
    /**
     * Inserta un elemento manteniendo el orden definido por el comparador.
     *
     * <p>El elemento se inserta antes del primer nodo cuyo dato sea
     * estrictamente mayor según el comparador. Si existen elementos
     * equivalentes, se inserta después de ellos.</p>
     *
     * @param dato elemento que se desea insertar.
     * @param comparador criterio utilizado para determinar el orden
     *                   entre los elementos.
     */
    public void insertarOrdenado(T dato, Comparator<? super T> comparador) {
        NodoDoble<T> actual = cabeza;
        int posicion = 0;
        while (actual != null && comparador.compare(dato, actual.getDato()) >= 0) {
            actual = actual.getSiguiente();
            posicion++;
        }
        insertarEn(posicion, dato);
    }
    /**
     * Busca y elimina la primera aparición de un elemento en la lista.
     *
     * @param dato elemento que se desea eliminar.
     * @return true si el elemento fue encontrado y eliminado;
     *         false si no existe.
     */
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
    /**
     * Elimina el elemento ubicado en la posición indicada.
     *
     * @param indice posición del elemento que se desea eliminar.
     * @return dato del nodo eliminado.
     * @throws IndexOutOfBoundsException si el índice es negativo o mayor
     *         o igual que el tamaño de la lista.
     */
    public T eliminarEn(int indice) {
        validarIndice(indice);
        NodoDoble<T> nodo = nodoEn(indice);
        desenlazar(nodo);
        return nodo.getDato();
    }

    /**
     * Obtiene el elemento ubicado en la posición indicada.
     *
     * @param indice posición del elemento que se desea consultar.
     * @return dato almacenado en la posición indicada.
     * @throws IndexOutOfBoundsException si el índice es negativo o mayor
     *         o igual que el tamaño de la lista.
     */
    public T obtener(int indice) {
        validarIndice(indice);
        return nodoEn(indice).getDato();
    }

    /** Primer elemento. Lanza EstructuraVaciaException si la lista esta vacia. */
    public T primero() {
        if (cabeza == null) {
            throw new EstructuraVaciaException("La lista esta vacia");
        }
        return cabeza.getDato();
    }

    /** Ultimo elemento. Lanza EstructuraVaciaException si la lista esta vacia. */
    public T ultimo() {
        if (cola == null) {
            throw new EstructuraVaciaException("La lista esta vacia");
        }
        return cola.getDato();
    }

    /**
     * Comprueba si la lista contiene un elemento.
     *
     * @param dato elemento cuya presencia se desea comprobar.
     * @return true si el elemento está presente; false en caso contrario.
     */
    public boolean contiene(T dato) {
        return indiceDe(dato) >= 0;
    }
    /**
     * Busca la primera aparición de un elemento en la lista.
     *
     * @param dato elemento cuya posición se desea encontrar.
     * @return índice de la primera aparición del elemento, o -1 si no existe.
     */
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
     * Elimina todos los elementos de la lista restableciendo sus referencias
     * y su tamaño.
     */
    public void limpiar() {
        cabeza = null;
        cola = null;
        tamano = 0;
    }
    /**
     * Obtiene un iterador para recorrer los elementos desde la cabeza
     * hasta la cola de la lista.
     *
     * @return iterador de los elementos almacenados.
     */
    @Override
    public Iterator<T> iterator() {
        return new IteradorLista();
    }
    /**
     * Genera una representación textual de los elementos de la lista,
     * desde la cabeza hasta la cola.
     *
     * @return cadena con los elementos separados por comas y delimitados
     *         por corchetes.
     */
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
    /**
     * Comprueba que un índice corresponda a una posición existente
     * dentro de la lista.
     *
     * @param indice posición que se desea validar.
     * @throws IndexOutOfBoundsException si el índice es negativo o mayor
     *         o igual que el tamaño de la lista.
     */
    private void validarIndice(int indice) {
        if (indice < 0 || indice >= tamano) {
            throw new IndexOutOfBoundsException("Indice " + indice + " fuera de rango [0, " + (tamano - 1) + "]");
        }
    }
    /**
     * Obtiene el nodo ubicado en el índice indicado.
     *
     * <p>Inicia el recorrido desde la cabeza o desde la cola, dependiendo
     * de cuál se encuentre más cerca de la posición buscada.</p>
     *
     * @param indice posición del nodo que se desea obtener.
     * @return nodo ubicado en la posición indicada.
     */
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
    /**
     * Desconecta un nodo de la lista y actualiza las referencias de sus
     * nodos vecinos, así como la cabeza, la cola y el tamaño.
     *
     * @param nodo nodo que se desea desconectar.
     */
    private void desenlazar(NodoDoble<T> nodo) {
        NodoDoble<T> previo = nodo.getAnterior();
        NodoDoble<T> sig = nodo.getSiguiente();
        if (previo == null) {
            cabeza = sig;
        } else {
            previo.setSiguiente(sig);
        }
        if (sig == null) {
            cola = previo;
        } else {
            sig.setAnterior(previo);
        }
        nodo.setAnterior(null);
        nodo.setSiguiente(null);
        tamano--;
    }

    /**
     * Iterador que recorre la lista desde la cabeza hasta el último nodo.
     */
    private class IteradorLista implements Iterator<T> {
        /** Nodo que se entregará en la siguiente llamada a {@code next()}. */
        private NodoDoble<T> siguiente = cabeza;
        /**
         * Comprueba si quedan elementos por recorrer.
         *
         * @return true si existe un siguiente nodo; false en caso contrario.
         */
        @Override
        public boolean hasNext() {
            return siguiente != null;
        }

        /**
         * Obtiene el dato del siguiente nodo y avanza el iterador.
         *
         * @return dato del nodo actual del iterador.
         * @throws NoSuchElementException si no quedan elementos por recorrer.
         */
        @Override
        public T next() {
            if (siguiente == null) {
                throw new NoSuchElementException("No hay mas elementos en la lista");
            }
            T dato = siguiente.getDato();
            siguiente = siguiente.getSiguiente();
            return dato;
        }
    }
    /**
     * Obtiene la referencia al primer nodo de la lista.
     *
     * @return nodo cabeza, o null si la lista está vacía.
     */
    public NodoDoble<T> getCabeza() {
        return cabeza;
    }

    /**
     * Modifica la referencia al primer nodo de la lista.
     *
     * @param cabeza nodo que se establecerá como cabeza.
     */
    public void setCabeza(NodoDoble<T> cabeza) {
        this.cabeza = cabeza;
    }

    /**
     * Obtiene la referencia al último nodo de la lista.
     *
     * @return nodo cola, o null si la lista está vacía.
     */
    public NodoDoble<T> getCola() {
        return cola;
    }

    /**
     * Modifica la referencia al último nodo de la lista.
     *
     * @param cola nodo que se establecerá como cola.
     */
    public void setCola(NodoDoble<T> cola) {
        this.cola = cola;
    }
    /**
     * Obtiene el tamaño registrado de la lista.
     *
     * @return cantidad de elementos registrada.
     */
    public int getTamano() {
        return tamano;
    }
    /**
     * Modifica directamente el tamaño registrado de la lista.
     *
     * @param tamano nuevo tamaño que se establecerá.
     */
    public void setTamano(int tamano) {
        this.tamano = tamano;
    }
    /**
     * Agrega a esta lista los elementos de otra lista que todavía no contiene.
     *
     * <p>Los elementos se recorren en el orden de la lista recibida.
     * Cada elemento se agrega al final únicamente si no existe previamente
     * en esta lista.</p>
     *
     * @param lista lista cuyos elementos se desean incorporar.
     */
    public void merge( ListaDoblementeEnlazada<T> lista){
        for (T elemento: lista){
            if (!this.contiene(elemento)) {
                agregar(elemento);
            }
        }
    }
}
