package model.estructuras.Grafo;

public class Arista{
    Integer valor;
    NodoGrafo<?> siguiente;
    NodoGrafo<?> anterior;
    public Arista(Integer valor){
        this.valor=valor;
    }
    public Integer getValor() {
        return valor;
    }
    public void setValor(Integer valor) {
        this.valor = valor;
    }
    public NodoGrafo<?> getSiguiente() {
        return siguiente;
    }
    public void setSiguiente(NodoGrafo<?> siguiente) {
        this.siguiente = siguiente;
    }
    public NodoGrafo<?> getAnterior() {
        return anterior;
    }
    public void setAnterior(NodoGrafo<?> anterior) {
        this.anterior = anterior;
    }
    public String toString(){
        return valor.toString();
    }
    public NodoGrafo<?> getExtremo(NodoGrafo<?> nodo){
        if (nodo.equals(siguiente)) return anterior;
        return siguiente;
    }
}
