package model.estructuras.Grafo.Dijkstra;

import model.estructuras.Grafo.NodoGrafo;

public class Paso {

    private final NodoGrafo<?> nodo;
    private final int distancia;
    private final Paso anterior;

    public Paso(NodoGrafo<?> nodo, int distancia, Paso anterior) {

        if (nodo == null) {
            throw new IllegalArgumentException(
                    "El nodo del paso no puede ser null"
            );
        }

        if (distancia < 0) {
            throw new IllegalArgumentException(
                    "La distancia no puede ser negativa"
            );
        }

        this.nodo = nodo;
        this.distancia = distancia;
        this.anterior = anterior;
    }

    public NodoGrafo<?> getNodo() {
        return nodo;
    }

    public int getDistancia() {
        return distancia;
    }

    public Paso getAnterior() {
        return anterior;
    }
}