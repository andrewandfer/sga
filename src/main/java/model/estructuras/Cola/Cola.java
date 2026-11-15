package model.estructuras.Cola;
/**
 * Representa una cola genérica implementada mediante nodos enlazados.
 * Permite almacenar, consultar, insertar y extraer elementos siguiendo
 * el principio FIFO (First In, First Out).
 *
 * @param <T> Tipo de dato de los elementos almacenados.
 */
public class Cola<T> {
    /**
     * Referencia al primer nodo de la cola.
     * Desde este nodo se accede secuencialmente a los demás elementos.
     */
    private NodoCola<T> Raiz;

    /**
     * Inicializa una cola vacía.
     */
    public Cola(){
        Raiz=null;
    }
    /**
     * Inicializa la cola estableciendo un nodo como raíz.
     *
     * @param Raiz Nodo que se utilizará como inicio de la cola.
     */
    public Cola(NodoCola<T> Raiz){
        this.Raiz=Raiz;
    }
    /**
     * Modifica la referencia al primer nodo de la cola.
     *
     * @param Raiz Nueva referencia al nodo inicial.
     */
    public void setRaiz(NodoCola<T> Raiz){
        this.Raiz=Raiz;
    }
    /**
     * Consulta el primer nodo de la cola sin modificar su contenido.
     *
     * @return Referencia al nodo inicial, o null si la cola está vacía.
     */
    public NodoCola<T> peek(){
        return Raiz;
    }
    /**
     * Extrae el primer elemento de la cola y actualiza la raíz
     * para que apunte al siguiente nodo.
     *
     * @return Dato almacenado en el nodo extraído, o null si está vacía.
     */
    public T poll(){
        if (isEmpty()) return null;
        NodoCola<T> aux=Raiz;
        Raiz=Raiz.getSiguiente();
        return aux.getDato();
    }
    /**
     * Agrega un nuevo elemento al final de la cola.
     * Si la cola está vacía, el nuevo nodo se establece como raíz.
     *
     * @param dato Elemento que se desea agregar.
     */
    public void add(T dato){
        if (Raiz==null) this.Raiz = new NodoCola<>(dato);
        else {NodoCola<T> aux= new NodoCola<>(dato);
        Raiz.add(dato);}
    }
    /**
     * Verifica si la cola contiene elementos.
     *
     * @return true si la raíz es null; false si existe al menos un nodo.
     */
    public boolean isEmpty(){
        return Raiz==null;
    }
    /**
     * Busca un elemento dentro de la cola.
     *
     * @param dato Elemento que se desea localizar.
     * @return true si el elemento existe en la cola; false en caso contrario.
     */
    public boolean contains(T dato){
        if (isEmpty()) return false;
        return Raiz.contains(dato);
    }
    /**
     * Transfiere los elementos de otra cola al final de la cola actual,
     * extrayéndolos uno por uno de la cola recibida.
     *
     * @param cola Cola de la cual se transferirán los elementos.
     * @throws IllegalArgumentException Si la cola recibida está vacía.
     */
    public void merge(Cola<T> cola){
        if (cola.isEmpty()) throw new IllegalArgumentException("La cola a fusionar esta vacia");
        while (!cola.isEmpty()){
            T aux=cola.poll();
            this.add(aux);
        }
    }
    /**
     * Genera una representación textual de la cola.
     *
     * @return Cadena con los elementos de la cola, o "[]" si está vacía.
     */
    public String toString() {
        if (isEmpty()) return "[]";
        return Raiz.toString();
    }
}
