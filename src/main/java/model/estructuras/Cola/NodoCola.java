package model.estructuras.Cola;
/**
 * Representa un nodo de una estructura de datos enlazada para la
 * implementación de colas.
 *
 * Cada nodo almacena un dato genérico y una referencia al siguiente
 * nodo de la estructura. Permite recorrer, agregar y buscar elementos
 * mediante enlaces entre nodos.
 *
 * @param <T> Tipo de dato almacenado en el nodo.
 */
public class NodoCola<T>{
    /**
     * Dato almacenado en el nodo.
     */
    T dato;
    /**
     * Referencia al siguiente nodo de la estructura.
     * Es null cuando el nodo es el último de la cadena.
     */
    NodoCola<T> siguiente;
    /**
     * Construye un nodo con el dato especificado.
     * Inicialmente, el nodo no apunta a ningún otro nodo.
     *
     * @param dato Dato que se almacenará en el nodo.
     */
    public NodoCola(T dato){
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
     * Obtiene la referencia al siguiente nodo.
     *
     * @return Siguiente nodo, o null si no existe.
     */
    public NodoCola<T> getSiguiente() {
        return siguiente;
    }
    /**
     * Establece el siguiente nodo de la estructura.
     *
     * @param siguiente Nodo que quedará enlazado a continuación.
     */
    public void setSiguiente(NodoCola<T> siguiente) {
        this.siguiente = siguiente;
    }
    /**
     * Genera una representación textual recursiva del nodo y de
     * todos los nodos enlazados a continuación.
     *
     * Cada dato se representa entre corchetes y los nodos se separan
     * mediante una flecha.
     *
     * @return Representación textual de la cadena de nodos.
     */
    public String toString(){
        StringBuilder sr=new StringBuilder();
        sr.append("[").append(dato.toString()).append("]");
        if (this.nextExist()) sr.append("->").append(this.siguiente.toString());
        return sr.toString();
    }
    /**
     * Comprueba si existe un nodo enlazado a continuación del actual.
     *
     * @return true si existe un siguiente nodo; false en caso contrario.
     */
    public boolean nextExist(){
        return siguiente!=null;
    }
    /**
     * Agrega un nuevo nodo al final de la cadena.
     * Si ya existe un siguiente nodo, continúa recursivamente
     * hasta encontrar el último.
     *
     * @param dato Dato que almacenará el nuevo nodo.
     */
    public void add(T dato){
        if(this.nextExist()){
            this.siguiente.add(dato);
        }
        else{
            this.siguiente=new NodoCola<>(dato);
        }
    }
    /**
     * Busca recursivamente un dato en la cadena de nodos.
     * La comparación se realiza mediante equals().
     *
     * @param dato Dato que se desea buscar.
     * @return true si el dato se encuentra en algún nodo;
     *         false si no está presente.
     */
    public boolean contains(T dato){
        if(this.dato.equals(dato)) return true;
        if(this.nextExist()) return this.siguiente.contains(dato);
        return false;
    }


}
