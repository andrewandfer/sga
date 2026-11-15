package model.estructuras.Grafo;

import model.estructuras.ListaDoblementeEnlazada;

/**
 * Representa un nodo perteneciente a una estructura de datos Grafo.
 *
 * Cada nodo almacena un contenido genérico y mantiene una lista
 * doblemente enlazada de las aristas que lo conectan con otros nodos.
 *
 * @param <T> Tipo de dato almacenado como contenido del nodo.
 */
public class NodoGrafo<T> {
    /**
     * Contenido o valor almacenado en el nodo.
     */
    T contenido;
    /**
     * Lista de aristas que representan las conexiones del nodo
     * con otros nodos del grafo.
     */
    ListaDoblementeEnlazada<Arista> adyacentes;
    /**
     * Construye un nodo con el contenido especificado e inicializa
     * su lista de aristas adyacentes vacía.
     *
     * @param contenido Valor que almacenará el nodo.
     */
    public NodoGrafo(T contenido){
        this.contenido=contenido;
        adyacentes=new ListaDoblementeEnlazada<>();
    }
    /**
     * Obtiene el contenido almacenado en el nodo.
     *
     * @return Contenido del nodo.
     */
    public T getContenido() {
        return contenido;
    }
    /**
     * Obtiene la lista de aristas adyacentes al nodo.
     *
     * @return Lista doblemente enlazada de aristas.
     */
    public ListaDoblementeEnlazada<Arista> getAdyacentes() {
        return adyacentes;
    }
    /**
     * Agrega una arista a la lista de adyacencias del nodo.
     *
     * @param arista Arista que se desea incorporar a la lista.
     */
    public void agregarAdyacente(Arista arista){
        adyacentes.agregar(arista);
    }
    /**
     * Elimina una arista de la lista de adyacencias del nodo.
     *
     * @param arista Arista que se desea eliminar.
     */
    public void quitarAdyacente(Arista arista){
        adyacentes.eliminar(arista);
    }
    /**
     * Comprueba si existe una arista que conecte este nodo
     * con el nodo especificado.
     *
     * Recorre las aristas adyacentes y verifica si el nodo buscado
     * corresponde al extremo anterior o siguiente de alguna de ellas.
     *
     * @param nodo Nodo cuya conexión se desea comprobar.
     * @return true si existe una arista que conecta ambos nodos;
     *         false en caso contrario.
     */
    public boolean contieneAdyacente(NodoGrafo<?> nodo){
        for (Arista arista : adyacentes) {
            if (arista.getSiguiente().equals(nodo) || arista.getAnterior().equals(nodo)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Modifica el contenido almacenado en el nodo.
     *
     * @param contenido Nuevo contenido del nodo.
     */
    public void setContenido(T contenido) {
        this.contenido = contenido;
    }
    /**
     * Obtiene la representación textual del nodo a partir
     * de su contenido.
     *
     * @return Representación textual del contenido almacenado.
     */
    public String toString(){
        return contenido.toString();
    }
}
