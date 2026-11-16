package model.estructuras;
import java.util.Comparator;
import model.excepciones.EstructuraVaciaException;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class ArbolBinarioBusqueda<T extends Comparable<T>> implements Iterable<T> {
    /**
     * Representa un nodo individual del árbol binario de búsqueda.
     *
     * Cada nodo almacena un dato y referencias a sus dos posibles
     * subárboles: izquierdo y derecho.
     *
     * @param <T> Tipo de dato almacenado en el nodo.
     */
    public static class NodoArbol<T> {
        /**
         * Dato almacenado en el nodo.
         */
        private T dato;
        /**
         * Referencia al hijo izquierdo del nodo.
         */
        private NodoArbol<T> izquierdo;
        /**
         * Referencia al hijo derecho del nodo.
         */
        private NodoArbol<T> derecho;
        /**
         * Construye un nodo con el dato especificado.
         *
         * @param dato Elemento que se almacenará en el nodo.
         */
        public NodoArbol(T dato) {
            this.dato = dato;
        }
        /**
         * Obtiene el dato almacenado en el nodo.
         *
         * @return Dato del nodo.
         */
        public T getDato() {
            return dato;
        }
        /**
         * Modifica el dato almacenado en el nodo.
         *
         * @param dato Nuevo dato del nodo.
         */
        public void setDato(T dato) {
            this.dato = dato;
        }
        /**
         * Obtiene la referencia al hijo izquierdo.
         *
         * @return Nodo hijo izquierdo, o null si no existe.
         */
        public NodoArbol<T> getIzquierdo() {
            return izquierdo;
        }
        /**
         * Establece el hijo izquierdo del nodo.
         *
         * @param izquierdo Nodo que se establecerá como hijo izquierdo.
         */
        public void setIzquierdo(NodoArbol<T> izquierdo) {
            this.izquierdo = izquierdo;
        }
        /**
         * Obtiene la referencia al hijo derecho.
         *
         * @return Nodo hijo derecho, o null si no existe.
         */
        public NodoArbol<T> getDerecho() {
            return derecho;
        }
        /**
         * Establece el hijo derecho del nodo.
         *
         * @param derecho Nodo que se establecerá como hijo derecho.
         */
        public void setDerecho(NodoArbol<T> derecho) {
            this.derecho = derecho;
        }
    }
    /**
     * Referencia al nodo raíz del árbol.
     */
    private NodoArbol<T> raiz;
    /**
     * Cantidad de elementos almacenados en el árbol.
     */
    private int tamano;
    /**
     * Comparador utilizado para determinar el orden de los elementos.
     * Si es null, se utiliza el orden natural definido por Comparable.
     */
    private final Comparator<T> comparador;
    /**
     * Construye un árbol binario de búsqueda vacío utilizando
     * el orden natural de los elementos.
     */
    public ArbolBinarioBusqueda() {
        this(null);
    }
    /**
     * Construye un árbol binario de búsqueda vacío utilizando
     * el comparador especificado.
     *
     * @param comparador Criterio de comparación de los elementos.
     *                   Si es null, se utiliza el orden natural.
     */
    public ArbolBinarioBusqueda(Comparator<T> comparador) {
        this.comparador = comparador;
        this.raiz = null;
        this.tamano = 0;
    }
    /**
     * Compara dos elementos utilizando el comparador personalizado
     * o el orden natural de Comparable.
     *
     * @param a Primer elemento de la comparación.
     * @param b Segundo elemento de la comparación.
     * @return Valor negativo si a es menor que b, cero si son
     *         equivalentes según el criterio de comparación
     *         o positivo si a es mayor que b.
     */
    private int comparar(T a, T b) {
        return comparador != null
                ? comparador.compare(a, b)
                : a.compareTo(b);
    }

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

        if (clave == null) {
            throw new IllegalArgumentException(
                    "La clave de búsqueda no puede ser null"
            );
        }

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

        return c < 0
                ? buscar(nodo.getIzquierdo(), clave)
                : buscar(nodo.getDerecho(), clave);
    }
    /**
     * Comprueba si un elemento se encuentra almacenado en el árbol.
     *
     * @param clave Elemento cuya existencia se desea verificar.
     * @return true si el elemento está presente; false en caso contrario.
     */
    public boolean contiene(T clave) {
        return buscar(clave) != null;
    }

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

    private NodoArbol<T> eliminar(NodoArbol<T> nodo, T clave) {
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

    private NodoArbol<T> minimo(NodoArbol<T> nodo) {

        if (nodo.getIzquierdo() == null) {
            return nodo;
        }

        return minimo(nodo.getIzquierdo());
    }

    private NodoArbol<T> eliminarMinimo(NodoArbol<T> nodo) {

        if (nodo.getIzquierdo() == null) {
            return nodo.getDerecho();
        }

        nodo.setIzquierdo(
                eliminarMinimo(nodo.getIzquierdo())
        );

        return nodo;
    }

    public int altura() {
        return altura(raiz);
    }
    /**
     * Calcula recursivamente la altura de un subárbol.
     *
     * Obtiene la mayor altura entre los subárboles izquierdo y
     * derecho y agrega un nivel correspondiente al nodo actual.
     *
     * @param nodo Raíz del subárbol cuya altura se desea calcular.
     * @return Altura del subárbol; 0 si el nodo es null.
     */
    private int altura(NodoArbol<T> nodo) {

        if (nodo == null) {
            return 0;
        }

        return 1 + Math.max(
                altura(nodo.getIzquierdo()),
                altura(nodo.getDerecho())
        );
    }

    public ListaDoblementeEnlazada<T> inOrden() {

        ListaDoblementeEnlazada<T> resultado =
                new ListaDoblementeEnlazada<>();

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