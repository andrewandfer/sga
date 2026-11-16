package model.estructuras.Grafo;

import model.estructuras.listaDoble.ListaDoblementeEnlazada;

public class NodoGrafo<T> {

    private T contenido;
    private final ListaDoblementeEnlazada<Arista> adyacentes;

    public NodoGrafo(T contenido) {
        this.contenido = contenido;
        this.adyacentes = new ListaDoblementeEnlazada<>();
    }

    public T getContenido() {
        return contenido;
    }

    public void setContenido(T contenido) {
        this.contenido = contenido;
    }

    public ListaDoblementeEnlazada<Arista> getAdyacentes() {
        return adyacentes;
    }

    public void agregarAdyacente(Arista arista) {

        if (arista == null) {
            throw new IllegalArgumentException(
                    "La arista no puede ser null"
            );
        }

        adyacentes.agregar(arista);
    }

    public void quitarAdyacente(Arista arista) {
        adyacentes.eliminar(arista);
    }

    public boolean contieneAdyacente(NodoGrafo<?> nodo) {

        if (nodo == null) {
            return false;
        }

        for (Arista arista : adyacentes) {

            if (arista.getExtremo(this) == nodo) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(contenido);
    }
}