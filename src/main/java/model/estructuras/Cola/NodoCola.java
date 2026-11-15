package model.estructuras.Cola;

public class NodoCola<T>{
    T dato;

    NodoCola<T> siguiente;

    public NodoCola(T dato){
        this.dato=dato;
    }

    public T getDato() {
        return dato;
    }

    public void setDato(T dato) {
        this.dato = dato;
    }

    public NodoCola<T> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoCola<T> siguiente) {
        this.siguiente = siguiente;
    }
    public String toString(){
        StringBuilder sr=new StringBuilder();
        sr.append("[").append(dato.toString()).append("]");
        if (this.nextExist()) sr.append("->").append(this.siguiente.toString());
        return sr.toString();
    }
    public boolean nextExist(){
        return siguiente!=null;
    }
    public void add(T dato){
        if(this.nextExist()){
            this.siguiente.add(dato);
        }
        else{
            this.siguiente=new NodoCola<>(dato);
        }
    }
    public boolean contains(T dato){
        if(this.dato.equals(dato)) return true;
        if(this.nextExist()) return this.siguiente.contains(dato);
        return false;
    }


}
