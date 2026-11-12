package estructuras;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class ListaDoblementeEnlazada<T> implements Iterable<T> {

    public static class NodoDoble<T> {

        private T dato;
        private NodoDoble<T> anterior;
        private NodoDoble<T> siguiente;

        public NodoDoble(T dato) {
            this.dato = dato;
        }

        public T getDato() {
            return dato;
        }

        public void setDato(T dato) {
            this.dato = dato;
        }

        public NodoDoble<T> getAnterior() {
            return anterior;
        }

        public void setAnterior(NodoDoble<T> anterior) {
            this.anterior = anterior;
        }

        public NodoDoble<T> getSiguiente() {
            return siguiente;
        }

        public void setSiguiente(NodoDoble<T> siguiente) {
            this.siguiente = siguiente;
        }
    }

    private NodoDoble<T> cabeza;
    private NodoDoble<T> cola;
    private int tamano;

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

    public void insertarOrdenado(T dato, Comparator<? super T> comparador) {
        NodoDoble<T> actual = cabeza;
        int posicion = 0;
        while (actual != null && comparador.compare(dato, actual.getDato()) >= 0) {
            actual = actual.getSiguiente();
            posicion++;
        }
        insertarEn(posicion, dato);
    }

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

    public T eliminarEn(int indice) {
        validarIndice(indice);
        NodoDoble<T> nodo = nodoEn(indice);
        desenlazar(nodo);
        return nodo.getDato();
    }

    public T obtener(int indice) {
        validarIndice(indice);
        return nodoEn(indice).getDato();
    }

    public boolean contiene(T dato) {
        return indiceDe(dato) >= 0;
    }

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

    public void limpiar() {
        cabeza = null;
        cola = null;
        tamano = 0;
    }

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

    private void validarIndice(int indice) {
        if (indice < 0 || indice >= tamano) {
            throw new IndexOutOfBoundsException("Indice " + indice + " fuera de rango [0, " + (tamano - 1) + "]");
        }
    }

    /** Recorre desde la cabeza o desde la cola segun lo que quede mas cerca. */
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

    private class IteradorLista implements Iterator<T> {
        private NodoDoble<T> siguiente = cabeza;

        @Override
        public boolean hasNext() {
            return siguiente != null;
        }

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

    public NodoDoble<T> getCabeza() {
        return cabeza;
    }

    public void setCabeza(NodoDoble<T> cabeza) {
        this.cabeza = cabeza;
    }

    public NodoDoble<T> getCola() {
        return cola;
    }

    public void setCola(NodoDoble<T> cola) {
        this.cola = cola;
    }

    public int getTamano() {
        return tamano;
    }

    public void setTamano(int tamano) {
        this.tamano = tamano;
    }
}
