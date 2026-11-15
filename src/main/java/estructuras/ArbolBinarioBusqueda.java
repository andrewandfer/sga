package estructuras;
import java.util.Comparator;
import excepciones.EstructuraVaciaException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class ArbolBinarioBusqueda<T extends Comparable<T>> implements Iterable<T> {

    public static class NodoArbol<T> {

        private T dato;
        private NodoArbol<T> izquierdo;
        private NodoArbol<T> derecho;

        public NodoArbol(T dato) {
            this.dato = dato;
        }

        public T getDato() {
            return dato;
        }

        public void setDato(T dato) {
            this.dato = dato;
        }

        public NodoArbol<T> getIzquierdo() {
            return izquierdo;
        }

        public void setIzquierdo(NodoArbol<T> izquierdo) {
            this.izquierdo = izquierdo;
        }

        public NodoArbol<T> getDerecho() {
            return derecho;
        }

        public void setDerecho(NodoArbol<T> derecho) {
            this.derecho = derecho;
        }
    }

    private NodoArbol<T> raiz;
    private int tamano;
    private final Comparator<T> comparador;

    public ArbolBinarioBusqueda() {
        this(null);
    }

    public ArbolBinarioBusqueda(Comparator<T> comparador) {
        this.comparador = comparador;
    }

    private int comparar(T a, T b) {
        return comparador != null ? comparador.compare(a, b) : a.compareTo(b);
    }

    public boolean insertar(T dato) {
        Objects.requireNonNull(dato, "El arbol no admite elementos nulos");
        int antes = tamano;
        raiz = insertar(raiz, dato);
        return tamano > antes;
    }

    private NodoArbol<T> insertar(NodoArbol<T> nodo, T dato) {
        if (nodo == null) {
            tamano++;
            return new NodoArbol<>(dato);
        }
        int c = comparar(dato, nodo.getDato());
        if (c < 0) {
            nodo.setIzquierdo(insertar(nodo.getIzquierdo(), dato));
        } else if (c > 0) {
            nodo.setDerecho(insertar(nodo.getDerecho(), dato));
        }
        return nodo;
    }

    public T buscar(T clave) {
        return buscar(raiz, clave);
    }

    private T buscar(NodoArbol<T> nodo, T clave) {
        if (nodo == null) {
            return null;
        }
        int c = comparar(clave, nodo.getDato());
        if (c == 0) {
            return nodo.getDato();
        }
        return c < 0 ? buscar(nodo.getIzquierdo(), clave) : buscar(nodo.getDerecho(), clave);
    }

    public boolean contiene(T clave) {
        return buscar(clave) != null;
    }

    public boolean eliminar(T clave) {
        int antes = tamano;
        raiz = eliminar(raiz, clave);
        return tamano < antes;
    }

    private NodoArbol<T> eliminar(NodoArbol<T> nodo, T clave) {
        if (nodo == null) {
            return null;
        }
        int c = comparar(clave, nodo.getDato());
        if (c < 0) {
            nodo.setIzquierdo(eliminar(nodo.getIzquierdo(), clave));
        } else if (c > 0) {
            nodo.setDerecho(eliminar(nodo.getDerecho(), clave));
        } else {
            tamano--;
            if (nodo.getIzquierdo() == null) {
                return nodo.getDerecho();
            }
            if (nodo.getDerecho() == null) {
                return nodo.getIzquierdo();
            }
            nodo.setDato(minimo(nodo.getDerecho()).getDato());
            nodo.setDerecho(eliminarMinimo(nodo.getDerecho()));
        }
        return nodo;
    }

    private NodoArbol<T> minimo(NodoArbol<T> nodo) {
        return nodo.getIzquierdo() == null ? nodo : minimo(nodo.getIzquierdo());
    }

    private NodoArbol<T> eliminarMinimo(NodoArbol<T> nodo) {
        if (nodo.getIzquierdo() == null) {
            return nodo.getDerecho();
        }
        nodo.setIzquierdo(eliminarMinimo(nodo.getIzquierdo()));
        return nodo;
    }

    public int altura() {
        return altura(raiz);
    }

    private int altura(NodoArbol<T> nodo) {
        if (nodo == null) {
            return 0;
        }
        return 1 + Math.max(altura(nodo.getIzquierdo()), altura(nodo.getDerecho()));
    }

    public ListaDoblementeEnlazada<T> inOrden() {
        ListaDoblementeEnlazada<T> resultado = new ListaDoblementeEnlazada<>();
        inOrden(raiz, resultado);
        return resultado;
    }

    private void inOrden(NodoArbol<T> nodo, ListaDoblementeEnlazada<T> resultado) {
        if (nodo == null) {
            return;
        }
        inOrden(nodo.getIzquierdo(), resultado);
        resultado.agregar(nodo.getDato());
        inOrden(nodo.getDerecho(), resultado);
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacio() {
        return tamano == 0;
    }


    public NodoArbol<T> getRaiz() {
        return raiz;
    }

    /** Recorrido raiz - izquierdo - derecho (recursivo). */
    public ListaDoblementeEnlazada<T> preOrden() {
        ListaDoblementeEnlazada<T> resultado = new ListaDoblementeEnlazada<>();
        preOrden(raiz, resultado);
        return resultado;
    }

    private void preOrden(NodoArbol<T> nodo, ListaDoblementeEnlazada<T> resultado) {
        if (nodo == null) {
            return;
        }
        resultado.agregar(nodo.getDato());
        preOrden(nodo.getIzquierdo(), resultado);
        preOrden(nodo.getDerecho(), resultado);
    }

    /** Recorrido izquierdo - derecho - raiz (recursivo). */
    public ListaDoblementeEnlazada<T> postOrden() {
        ListaDoblementeEnlazada<T> resultado = new ListaDoblementeEnlazada<>();
        postOrden(raiz, resultado);
        return resultado;
    }

    private void postOrden(NodoArbol<T> nodo, ListaDoblementeEnlazada<T> resultado) {
        if (nodo == null) {
            return;
        }
        postOrden(nodo.getIzquierdo(), resultado);
        postOrden(nodo.getDerecho(), resultado);
        resultado.agregar(nodo.getDato());
    }

    /** Menor elemento segun el orden del arbol. Lanza EstructuraVaciaException si no hay elementos. */
    public T obtenerMinimo() {
        if (raiz == null) {
            throw new EstructuraVaciaException("El arbol esta vacio");
        }
        return minimo(raiz).getDato();
    }

    /** Mayor elemento segun el orden del arbol. Lanza EstructuraVaciaException si no hay elementos. */
    public T obtenerMaximo() {
        if (raiz == null) {
            throw new EstructuraVaciaException("El arbol esta vacio");
        }
        return maximo(raiz).getDato();
    }

    private NodoArbol<T> maximo(NodoArbol<T> nodo) {
        return nodo.getDerecho() == null ? nodo : maximo(nodo.getDerecho());
    }

    /**
     * Iterador propio en in-orden (de menor a mayor). Es perezoso: no copia el arbol,
     * mantiene una pila de nodos pendientes. No se debe modificar el arbol mientras se recorre.
     */
    @Override
    public Iterator<T> iterator() {
        return new IteradorInOrden();
    }

    private class IteradorInOrden implements Iterator<T> {

        private final ListaDoblementeEnlazada<NodoArbol<T>> pendientes = new ListaDoblementeEnlazada<>();

        IteradorInOrden() {
            apilarRamaIzquierda(raiz);
        }

        private void apilarRamaIzquierda(NodoArbol<T> nodo) {
            while (nodo != null) {
                pendientes.agregar(nodo);
                nodo = nodo.getIzquierdo();
            }
        }

        @Override
        public boolean hasNext() {
            return !pendientes.estaVacia();
        }

        @Override
        public T next() {
            if (pendientes.estaVacia()) {
                throw new NoSuchElementException("No hay mas elementos en el arbol");
            }
            NodoArbol<T> nodo = pendientes.eliminarEn(pendientes.tamano() - 1);
            apilarRamaIzquierda(nodo.getDerecho());
            return nodo.getDato();
        }
    }
}