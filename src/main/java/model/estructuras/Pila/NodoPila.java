package model.estructuras.Pila;

public class NodoPila<T> {
    T dato;
    NodoPila<T> siguiente;
    public NodoPila(T dato){
        this.dato=dato;
    }
    public T getDato() {
        return dato;
    }
    public void setDato(T dato) {
        this.dato = dato;
    }
    public NodoPila<T> getSiguiente() {
        return siguiente;
    }
    public void setSiguiente(NodoPila<T> siguiente) {
        this.siguiente = siguiente;
    }
    public String toString(){
        return dato.toString();
    }
    public boolean nextExist(){
        return siguiente!=null;
    }

}
