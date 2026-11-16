package model.estructuras.Grafo.Dijkstra;

import model.estructuras.Grafo.NodoGrafo;

public class Paso<T> {

    private final NodoGrafo<T> nodo;
    private final int distancia;
    private final Paso<T> anterior;

    public Paso(NodoGrafo<T> nodo, int distancia, Paso<T> anterior) {
        if (nodo == null) throw new IllegalArgumentException("El nodo no puede ser null");
        if (distancia < 0) throw new IllegalArgumentException("La distancia no puede ser negativa");
        this.nodo = nodo;
        this.distancia = distancia;
        this.anterior = anterior;
    }

    public NodoGrafo<T> getNodo() { return nodo; }
    public int getDistancia() { return distancia; }
    public Paso<T> getAnterior() { return anterior; }
}
