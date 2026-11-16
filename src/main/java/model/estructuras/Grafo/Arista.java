package model.estructuras.Grafo;

import java.util.Objects;

public class Arista {

    private final int valor;
    private final NodoGrafo<?> origen;
    private final NodoGrafo<?> destino;

    public Arista(int valor, NodoGrafo<?> origen, NodoGrafo<?> destino) {

        if (origen == null || destino == null) {
            throw new IllegalArgumentException(
                    "Los extremos de una arista no pueden ser null"
            );
        }

        if (origen == destino) {
            throw new IllegalArgumentException(
                    "Una arista no puede conectar un nodo consigo mismo"
            );
        }

        if (valor < 0) {
            throw new IllegalArgumentException(
                    "El peso de una arista no puede ser negativo"
            );
        }

        this.valor = valor;
        this.origen = origen;
        this.destino = destino;
    }

    public int getValor() {
        return valor;
    }

    public NodoGrafo<?> getOrigen() {
        return origen;
    }

    public NodoGrafo<?> getDestino() {
        return destino;
    }

    public NodoGrafo<?> getExtremo(NodoGrafo<?> nodo) {

        if (nodo == null) {
            throw new IllegalArgumentException(
                    "El nodo no puede ser null"
            );
        }

        if (nodo == origen) {
            return destino;
        }

        if (nodo == destino) {
            return origen;
        }

        throw new IllegalArgumentException(
                "El nodo no pertenece a esta arista"
        );
    }

    @Override
    public String toString() {
        return String.valueOf(valor);
    }
}