package model.estructuras.Grafo;

import model.estructuras.ListaDoblementeEnlazada;

public class NodoGrafo<T> {
    T contenido;
    ListaDoblementeEnlazada<Arista> adyacentes;

    public NodoGrafo(T contenido){
        this.contenido=contenido;
        adyacentes=new ListaDoblementeEnlazada<>();
    }
    public T getContenido() {
        return contenido;
    }
    public ListaDoblementeEnlazada<Arista> getAdyacentes() {
        return adyacentes;
    }
    public void agregarAdyacente(Arista arista){
        adyacentes.agregar(arista);
    }
    public void quitarAdyacente(Arista arista){
        adyacentes.eliminar(arista);
    }
    public boolean contieneAdyacente(NodoGrafo<?> nodo){
        for (Arista arista : adyacentes) {
            if (arista.getSiguiente().equals(nodo) || arista.getAnterior().equals(nodo)) {
                return true;
            }
        }
        return false;
    }

    public void setContenido(T contenido) {
        this.contenido = contenido;
    }

    public String toString(){
        return contenido.toString();
    }
}
