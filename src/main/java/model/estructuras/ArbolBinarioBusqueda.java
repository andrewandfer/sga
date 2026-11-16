package model.estructuras;

import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.estructuras.Pila.Pila;
import model.excepciones.EstructuraVaciaException;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

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
        this.raiz = null;
        this.tamano = 0;
    }

    private int comparar(T a, T b) {
        return comparador != null
                ? comparador.compare(a, b)
                : a.compareTo(b);
    }

    // Insertar un elemento.
    public boolean insertar(T dato) {

        if (dato == null) {
            throw new IllegalArgumentException(
                    "El árbol no admite elementos nulos"
            );
        }

        int antes = tamano;

        raiz = insertar(raiz, dato);

        return tamano > antes;
    }

    private NodoArbol<T> insertar(
            NodoArbol<T> nodo,
            T dato) {

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

    // Buscar un elemento.
    public T buscar(T clave) {

        if (clave == null) {
            throw new IllegalArgumentException(
                    "La clave de búsqueda no puede ser null"
            );
        }

        return buscar(raiz, clave);
    }

    private T buscar(
            NodoArbol<T> nodo,
            T clave) {

        if (nodo == null) {
            return null;
        }

        int c = comparar(clave, nodo.getDato());

        if (c == 0) {
            return nodo.getDato();
        }

        return c < 0
                ? buscar(nodo.getIzquierdo(), clave)
                : buscar(nodo.getDerecho(), clave);
    }

    public boolean contiene(T clave) {
        return buscar(clave) != null;
    }

    // Eliminar un elemento.
    public boolean eliminar(T clave) {

        if (clave == null) {
            throw new IllegalArgumentException(
                    "La clave de eliminación no puede ser null"
            );
        }

        int antes = tamano;

        raiz = eliminar(raiz, clave);

        return tamano < antes;
    }

    private NodoArbol<T> eliminar(
            NodoArbol<T> nodo,
            T clave) {

        if (nodo == null) {
            return null;
        }

        int c = comparar(clave, nodo.getDato());

        if (c < 0) {

            nodo.setIzquierdo(
                    eliminar(nodo.getIzquierdo(), clave)
            );

        } else if (c > 0) {

            nodo.setDerecho(
                    eliminar(nodo.getDerecho(), clave)
            );

        } else {

            tamano--;

            if (nodo.getIzquierdo() == null) {
                return nodo.getDerecho();
            }

            if (nodo.getDerecho() == null) {
                return nodo.getIzquierdo();
            }

            NodoArbol<T> sucesor = minimo(nodo.getDerecho());

            nodo.setDato(sucesor.getDato());

            nodo.setDerecho(
                    eliminarMinimo(nodo.getDerecho())
            );
        }

        return nodo;
    }

    // Encontrar el menor elemento de un subárbol.
    private NodoArbol<T> minimo(NodoArbol<T> nodo) {

        if (nodo.getIzquierdo() == null) {
            return nodo;
        }

        return minimo(nodo.getIzquierdo());
    }

    // Eliminar el menor elemento de un subárbol.
    private NodoArbol<T> eliminarMinimo(NodoArbol<T> nodo) {

        if (nodo.getIzquierdo() == null) {
            return nodo.getDerecho();
        }

        nodo.setIzquierdo(
                eliminarMinimo(nodo.getIzquierdo())
        );

        return nodo;
    }

    // Calcular altura.
    public int altura() {
        return altura(raiz);
    }

    private int altura(NodoArbol<T> nodo) {

        if (nodo == null) {
            return 0;
        }

        return 1 + Math.max(
                altura(nodo.getIzquierdo()),
                altura(nodo.getDerecho())
        );
    }

    // Recorrido inorden: izquierdo - raíz - derecho.
    public ListaDoblementeEnlazada<T> inOrden() {

        ListaDoblementeEnlazada<T> resultado =
                new ListaDoblementeEnlazada<>();

        inOrden(raiz, resultado);

        return resultado;
    }

    private void inOrden(
            NodoArbol<T> nodo,
            ListaDoblementeEnlazada<T> resultado) {

        if (nodo == null) {
            return;
        }

        inOrden(nodo.getIzquierdo(), resultado);

        resultado.agregar(nodo.getDato());

        inOrden(nodo.getDerecho(), resultado);
    }

    // Recorrido preorden: raíz - izquierdo - derecho.
    public ListaDoblementeEnlazada<T> preOrden() {

        ListaDoblementeEnlazada<T> resultado =
                new ListaDoblementeEnlazada<>();

        preOrden(raiz, resultado);

        return resultado;
    }

    private void preOrden(
            NodoArbol<T> nodo,
            ListaDoblementeEnlazada<T> resultado) {

        if (nodo == null) {
            return;
        }

        resultado.agregar(nodo.getDato());

        preOrden(nodo.getIzquierdo(), resultado);
        preOrden(nodo.getDerecho(), resultado);
    }

    // Recorrido postorden: izquierdo - derecho - raíz.
    public ListaDoblementeEnlazada<T> postOrden() {

        ListaDoblementeEnlazada<T> resultado =
                new ListaDoblementeEnlazada<>();

        postOrden(raiz, resultado);

        return resultado;
    }

    private void postOrden(
            NodoArbol<T> nodo,
            ListaDoblementeEnlazada<T> resultado) {

        if (nodo == null) {
            return;
        }

        postOrden(nodo.getIzquierdo(), resultado);
        postOrden(nodo.getDerecho(), resultado);

        resultado.agregar(nodo.getDato());
    }

    // Obtener el menor elemento.
    public T obtenerMinimo() {

        if (estaVacio()) {
            throw new EstructuraVaciaException(
                    "El árbol está vacío"
            );
        }

        return minimo(raiz).getDato();
    }

    // Obtener el mayor elemento.
    public T obtenerMaximo() {

        if (estaVacio()) {
            throw new EstructuraVaciaException(
                    "El árbol está vacío"
            );
        }

        return maximo(raiz).getDato();
    }

    private NodoArbol<T> maximo(NodoArbol<T> nodo) {

        if (nodo.getDerecho() == null) {
            return nodo;
        }

        return maximo(nodo.getDerecho());
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

    // Iterador propio en inorden.
    @Override
    public Iterator<T> iterator() {
        return new IteradorInOrden();
    }

    private class IteradorInOrden implements Iterator<T> {

        private final Pila<NodoArbol<T>> pendientes =
                new Pila<>();

        IteradorInOrden() {
            apilarRamaIzquierda(raiz);
        }

        private void apilarRamaIzquierda(NodoArbol<T> nodo) {

            while (nodo != null) {
                pendientes.push(nodo);
                nodo = nodo.getIzquierdo();
            }
        }

        @Override
        public boolean hasNext() {
            return !pendientes.isEmpty();
        }

        @Override
        public T next() {

            if (!hasNext()) {
                throw new NoSuchElementException(
                        "No hay más elementos en el árbol"
                );
            }

            NodoArbol<T> nodo = pendientes.pop();

            apilarRamaIzquierda(nodo.getDerecho());

            return nodo.getDato();
        }
    }
}