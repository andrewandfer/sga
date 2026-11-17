package model.estructuras.Grafo;

import model.estructuras.listaDoble.ListaDoblementeEnlazada;

public class NodoGrafo<T> {

    private T contenido;
    private final ListaDoblementeEnlazada<Arista<T>> adyacentes;

    public NodoGrafo(T contenido) {
        if (contenido == null) throw new IllegalArgumentException("El contenido no puede ser null");
        this.contenido = contenido;
        this.adyacentes = new ListaDoblementeEnlazada<>();
    }

    public T getContenido() { return contenido; }

    public void setContenido(T contenido) {
        if (contenido == null) throw new IllegalArgumentException("El contenido no puede ser null");
        this.contenido = contenido;
    }

    public ListaDoblementeEnlazada<Arista<T>> getAdyacentes() { return adyacentes; }

    public void agregarAdyacente(Arista<T> arista) {
        if (arista == null) throw new IllegalArgumentException("La arista no puede ser null");
        adyacentes.agregar(arista);
    }

    public void quitarAdyacente(Arista<T> arista) { adyacentes.eliminar(arista); }

    public boolean contieneAdyacente(NodoGrafo<T> nodo) {
        if (nodo == null) return false;
        for (Arista<T> arista : adyacentes) {
            if (arista.getExtremo(this) == nodo) return true;
        }
        return false;
    }

    @Override
    public String toString() { return String.valueOf(contenido); }
}
