package model.estructuras.Grafo;
/**
 * Representa una arista que conecta dos nodos dentro de un grafo.
 *
 * Cada arista almacena un valor entero y referencias a los dos nodos
 * que conecta, identificados como anterior y siguiente.
 */
public class Arista{
    /**
     * Valor asociado a la arista, utilizado para representar
     * su peso o costo dentro del grafo.
     */
    Integer valor;
    /**
     * Referencia a uno de los nodos extremos de la arista.
     */
    NodoGrafo<?> siguiente;
    /**
     * Referencia al otro nodo extremo de la arista.
     */
    NodoGrafo<?> anterior;
    /**
     * Construye una arista con el valor especificado.
     * Inicialmente, sus extremos no están asociados a ningún nodo.
     *
     * @param valor Peso o costo asignado a la arista.
     */
    public Arista(Integer valor){
        this.valor=valor;
    }
    /**
     * Obtiene el valor asociado a la arista.
     *
     * @return Peso o costo de la arista.
     */
    public Integer getValor() {
        return valor;
    }
    /**
     * Modifica el valor asociado a la arista.
     *
     * @param valor Nuevo peso o costo de la arista.
     */
    public void setValor(Integer valor) {
        this.valor = valor;
    }
    /**
     * Obtiene uno de los nodos extremos de la arista.
     *
     * @return Nodo identificado como siguiente.
     */
    public NodoGrafo<?> getSiguiente() {
        return siguiente;
    }
    /**
     * Establece uno de los nodos extremos de la arista.
     *
     * @param siguiente Nodo que se establecerá como siguiente.
     */
    public void setSiguiente(NodoGrafo<?> siguiente) {
        this.siguiente = siguiente;
    }
    /**
     * Obtiene el otro nodo extremo de la arista.
     *
     * @return Nodo identificado como anterior.
     */
    public NodoGrafo<?> getAnterior() {
        return anterior;
    }
    /**
     * Establece el otro nodo extremo de la arista.
     *
     * @param anterior Nodo que se establecerá como anterior.
     */
    public void setAnterior(NodoGrafo<?> anterior) {
        this.anterior = anterior;
    }
    /**
     * Obtiene la representación textual del valor de la arista.
     *
     * @return Valor de la arista convertido a String.
     */
    public String toString(){
        return valor.toString();
    }
    /**
     * Obtiene el extremo opuesto al nodo proporcionado.
     *
     * Si el nodo recibido coincide con el extremo siguiente,
     * devuelve el extremo anterior. En caso contrario, devuelve
     * el extremo siguiente.
     *
     * @param nodo Nodo desde el cual se desea obtener el extremo opuesto.
     * @return Nodo ubicado en el extremo contrario de la arista.
     */
    public NodoGrafo<?> getExtremo(NodoGrafo<?> nodo){
        if (nodo.equals(siguiente)) return anterior;
        return siguiente;
    }
}
