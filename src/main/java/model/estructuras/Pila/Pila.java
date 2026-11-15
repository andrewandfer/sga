package model.estructuras.Pila;
/**
 * Implementa una estructura de datos Pila genérica mediante nodos enlazados.
 *
 * Sigue el principio LIFO (Last In, First Out), donde el último elemento
 * en ingresar es el primero en salir.
 *
 * Permite insertar, extraer y consultar elementos desde el inicio
 * de la estructura.
 *
 * @param <T> Tipo de dato de los elementos almacenados en la pila.
 */
public class Pila<T>{
    /**
     * Referencia al nodo ubicado en la cima de la pila.
     * Es null cuando la estructura está vacía.
     */
    NodoPila<T> Raiz;
    /**
     * Construye una pila vacía.
     */
    public Pila(){
        Raiz=null;
    }
    /**
     * Construye una pila estableciendo un nodo como cima inicial.
     *
     * @param raiz Nodo que se establecerá como inicio de la pila.
     */
    public Pila(NodoPila<T> raiz){
        this.Raiz=raiz;
    }
    /**
     * Modifica la referencia al nodo ubicado en la cima de la pila.
     *
     * @param raiz Nueva referencia al nodo inicial.
     */
    public void setRaiz(NodoPila<T> raiz) {
        Raiz = raiz;
    }
    /**
     * Consulta el nodo ubicado en la cima de la pila sin extraerlo.
     *
     * @return Nodo ubicado en la cima, o null si la pila está vacía.
     */
    public NodoPila<T> peek() {
        return Raiz;
    }
    /**
     * Inserta un elemento en la cima de la pila.
     *
     * Crea un nuevo nodo, establece su referencia al nodo que
     * anteriormente ocupaba la cima y actualiza la raíz para
     * que apunte al nuevo nodo.
     *
     * @param dato Elemento que se desea insertar en la pila.
     */
    public void push(T dato){
        NodoPila<T> nuevo=new NodoPila<>(dato);
        nuevo.setSiguiente(Raiz);
        this.setRaiz(nuevo);
    }
    /**
     * Extrae y devuelve el elemento ubicado en la cima de la pila.
     *
     * Actualiza la raíz para que apunte al siguiente nodo y
     * desvincula el nodo extraído del resto de la estructura.
     *
     * @return Dato almacenado en el nodo extraído.
     */
    public T pop(){
        if (isEmpty()) return null;
        NodoPila<T> aux=Raiz;
        this.setRaiz(Raiz.getSiguiente());
        aux.setSiguiente(null);
        return aux.getDato();
    }
    /**
     * Comprueba si la pila está vacía.
     *
     * @return true si la raíz es null; false si existe al menos un nodo.
     */
    public boolean isEmpty(){
        return Raiz==null;
    }
    /**
     * Genera una representación textual de los elementos de la pila.
     *
     * Recorre los nodos desde la cima hasta el último elemento,
     * separando sus contenidos mediante flechas.
     *
     * @return Cadena con los elementos de la pila, o "[]" si está vacía.
     */
    public String toString(){
        if (isEmpty()) return "[]";
        NodoPila<T> aux=Raiz;
        StringBuilder cadena=new StringBuilder();
        while (aux!=null){
            cadena.append(aux.getDato());
            if (aux.nextExist()) {
                cadena.append("->");}
            aux=aux.getSiguiente();
        }
        return cadena.toString();
    }
}
