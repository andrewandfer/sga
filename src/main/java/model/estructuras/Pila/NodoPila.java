package model.estructuras.Pila;

/**
 * Representa un nodo de una estructura de datos Pila implementada
 * mediante nodos enlazados.
 *
 * Cada nodo almacena un dato genérico y una referencia al siguiente
 * nodo de la estructura.
 *
 * @param <T> Tipo de dato almacenado en el nodo.
 */
public class NodoPila<T> {
    /**
     * Dato almacenado en el nodo.
     */
    T dato;
    /**
     * Referencia al siguiente nodo de la pila.
     * Es null cuando no existe un nodo enlazado a continuación.
     */
    NodoPila<T> siguiente;
    /**
     * Construye un nodo con el dato especificado.
     *
     * @param dato Elemento que se almacenará en el nodo.
     */
    public NodoPila(T dato){
        this.dato=dato;
    }

    /**
     * Obtiene el dato almacenado en el nodo.
     *
     * @return Dato contenido en el nodo.
     */
    public T getDato() {
        return dato;
    }
    /**
     * Modifica el dato almacenado en el nodo.
     *
     * @param dato Nuevo dato que se almacenará.
     */
    public void setDato(T dato) {
        this.dato = dato;
    }
    /**
     * Obtiene la referencia al siguiente nodo de la pila.
     *
     * @return Siguiente nodo, o null si no existe.
     */
    public NodoPila<T> getSiguiente() {
        return siguiente;
    }
    /**
     * Establece el siguiente nodo de la estructura.
     *
     * @param siguiente Nodo que quedará enlazado a continuación.
     */
    public void setSiguiente(NodoPila<T> siguiente) {
        this.siguiente = siguiente;
    }
    /**
     * Obtiene la representación textual del dato almacenado.
     *
     * @return Representación textual del dato.
     */
    public String toString(){
        return dato.toString();
    }
    /**
     * Comprueba si existe un nodo enlazado a continuación del actual.
     *
     * @return true si existe un siguiente nodo; false en caso contrario.
     */
    public boolean nextExist(){
        return siguiente!=null;
    }

}
