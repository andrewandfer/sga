
package model.estructuras.Grafo.Dijkstra;

import model.estructuras.Grafo.NodoGrafo;

public class Paso {

    private NodoGrafo<?> nodo;
    private int distancia;
    private Paso anterior;

    public Paso(NodoGrafo<?> nodo, int distancia, Paso anterior) {
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
