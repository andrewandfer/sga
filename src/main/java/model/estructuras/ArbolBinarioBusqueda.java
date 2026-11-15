package model.estructuras;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
/**
 * Implementa un árbol binario de búsqueda genérico.
 *
 * Organiza los elementos de acuerdo con su orden natural o con un
 * comparador personalizado. Para cada nodo, los elementos menores
 * se ubican en el subárbol izquierdo y los mayores en el derecho.
 *
 * No admite elementos duplicados según el criterio de comparación.
 * Implementa Iterable para permitir recorrer sus elementos en orden.
 *
 * @param <T> Tipo de elementos almacenados. Debe implementar Comparable.
 */
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
        return comparador != null ? comparador.compare(a, b) : a.compareTo(b);
    }
    /**
     * Inserta un elemento en el árbol binario de búsqueda.
     *
     * La ubicación del elemento se determina mediante el criterio
     * de comparación establecido. Si el elemento ya existe según
     * dicho criterio, no se realiza ninguna inserción.
     *
     * @param dato Elemento que se desea insertar.
     * @return true si el elemento fue insertado; false si ya existía.
     * @throws NullPointerException Si el elemento recibido es null.
     */
    public boolean insertar(T dato) {
        Objects.requireNonNull(dato, "El arbol no admite elementos nulos");
        int antes = tamano;
        raiz = insertar(raiz, dato);
        return tamano > antes;
    }
    /**
     * Realiza recursivamente la inserción de un elemento a partir
     * de un nodo determinado.
     *
     * Si encuentra una posición vacía, crea un nuevo nodo.
     * En caso contrario, continúa la búsqueda por el subárbol
     * izquierdo o derecho según el resultado de la comparación.
     *
     * @param nodo Nodo desde el cual comienza la búsqueda de posición.
     * @param dato Elemento que se desea insertar.
     * @return Nodo raíz del subárbol después de la inserción.
     */
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
    /**
     * Busca un elemento dentro del árbol utilizando el criterio
     * de comparación establecido.
     *
     * @param clave Elemento que se desea localizar.
     * @return Elemento almacenado que coincide con la clave,
     *         o null si no se encuentra.
     */
    public T buscar(T clave) {
        return buscar(raiz, clave);
    }
    /**
     * Realiza recursivamente la búsqueda de un elemento a partir
     * de un nodo determinado.
     *
     * Compara la clave con el dato actual y continúa la búsqueda
     * por el subárbol correspondiente según el resultado.
     *
     * @param nodo Nodo desde el cual continúa la búsqueda.
     * @param clave Elemento que se desea localizar.
     * @return Elemento encontrado, o null si no existe en el subárbol.
     */
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
    /**
     * Comprueba si un elemento se encuentra almacenado en el árbol.
     *
     * @param clave Elemento cuya existencia se desea verificar.
     * @return true si el elemento está presente; false en caso contrario.
     */
    public boolean contiene(T clave) {
        return buscar(clave) != null;
    }
    /**
     * Elimina un elemento del árbol.
     *
     * Si el elemento tiene dos hijos, su posición es ocupada por
     * el menor elemento de su subárbol derecho. Si tiene uno o
     * ningún hijo, se reorganizan las referencias correspondientes.
     *
     * @param clave Elemento que se desea eliminar.
     * @return true si el elemento fue eliminado; false si no existía.
     */
    public boolean eliminar(T clave) {
        int antes = tamano;
        raiz = eliminar(raiz, clave);
        return tamano < antes;
    }
    /**
     * Realiza recursivamente la eliminación de un elemento.
     *
     * Busca el nodo correspondiente y contempla tres situaciones:
     * ausencia de hijos, presencia de un único hijo o presencia
     * de dos hijos. En el último caso, sustituye el dato por el
     * sucesor inmediato obtenido del subárbol derecho.
     *
     * @param nodo Nodo desde el cual comienza la búsqueda.
     * @param clave Elemento que se desea eliminar.
     * @return Nueva raíz del subárbol después de la eliminación.
     */
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
    /**
     * Encuentra el nodo con el menor elemento de un subárbol.
     *
     * Busca recursivamente el nodo más a la izquierda,
     * ya que contiene el menor elemento según el orden del árbol.
     *
     * @param nodo Raíz del subárbol que se desea examinar.
     * @return Nodo que contiene el menor elemento del subárbol.
     */
    private NodoArbol<T> minimo(NodoArbol<T> nodo) {
        return nodo.getIzquierdo() == null ? nodo : minimo(nodo.getIzquierdo());
    }
    /**
     * Elimina el nodo que contiene el menor elemento de un subárbol.
     *
     * Si el nodo mínimo tiene un hijo derecho, este ocupa su lugar.
     * En caso contrario, se continúa la búsqueda recursivamente
     * por el subárbol izquierdo.
     *
     * Este método no modifica el contador tamano.
     *
     * @param nodo Raíz del subárbol del cual se eliminará el mínimo.
     * @return Nueva raíz del subárbol después de eliminar el mínimo.
     */
    private NodoArbol<T> eliminarMinimo(NodoArbol<T> nodo) {
        if (nodo.getIzquierdo() == null) {
            return nodo.getDerecho();
        }
        nodo.setIzquierdo(eliminarMinimo(nodo.getIzquierdo()));
        return nodo;
    }
    /**
     * Calcula la altura del árbol.
     *
     * La altura corresponde a la cantidad de niveles existentes
     * desde la raíz hasta la hoja más profunda.
     *
     * @return Altura del árbol. Devuelve 0 si está vacío.
     */
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
        return 1 + Math.max(altura(nodo.getIzquierdo()), altura(nodo.getDerecho()));
    }
    /**
     * Obtiene los elementos del árbol mediante un recorrido inorden.
     *
     * El recorrido visita primero el subárbol izquierdo, después
     * el nodo actual y finalmente el subárbol derecho.
     *
     * @return Lista doblemente enlazada con los elementos ordenados.
     */
    public ListaDoblementeEnlazada<T> inOrden() {
        ListaDoblementeEnlazada<T> resultado = new ListaDoblementeEnlazada<>();
        inOrden(raiz, resultado);
        return resultado;
    }
    /**
     * Realiza recursivamente el recorrido inorden de un subárbol.
     *
     * Agrega los elementos a la lista siguiendo el orden:
     * subárbol izquierdo, nodo actual y subárbol derecho.
     *
     * @param nodo Nodo desde el cual comienza el recorrido.
     * @param resultado Lista en la que se almacenan los elementos.
     */
    private void inOrden(NodoArbol<T> nodo, ListaDoblementeEnlazada<T> resultado) {
        if (nodo == null) {
            return;
        }
        inOrden(nodo.getIzquierdo(), resultado);
        resultado.agregar(nodo.getDato());
        inOrden(nodo.getDerecho(), resultado);
    }
    /**
     * Obtiene la cantidad de elementos almacenados en el árbol.
     *
     * @return Número de elementos presentes.
     */
    public int tamano() {
        return tamano;
    }
    /**
     * Comprueba si el árbol no contiene elementos.
     *
     * @return true si el tamaño es cero; false en caso contrario.
     */
    public boolean estaVacio() {
        return tamano == 0;
    }

    /**
     * Obtiene el nodo raíz del árbol.
     *
     * @return Nodo raíz, o null si el árbol está vacío.
     */
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
        return inOrden().iterator();
    }
}