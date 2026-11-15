
package model.estructuras.Grafo;

import model.estructuras.Cola.Cola;
import model.estructuras.Cola.ColaPrioritaria;
import model.estructuras.Conjunto;
import model.estructuras.Grafo.Dijkstra.Paso;
import model.estructuras.ListaDoblementeEnlazada;
import model.estructuras.Pila.Pila;

import java.util.Comparator;

public class Grafo {

    public void agregarNodo(NodoGrafo<?> origen, NodoGrafo<?> destino, Integer valor) {

        if (origen.contieneAdyacente(destino)
                || destino.contieneAdyacente(origen)) {
            throw new IllegalArgumentException("La conexión entre los nodos ya existe");
        }

        Arista arista = new Arista(valor);

        origen.agregarAdyacente(arista);
        destino.agregarAdyacente(arista);

        arista.setSiguiente(destino);
        arista.setAnterior(origen);
    }

    public String imprimir(NodoGrafo<?> nodo) {

        StringBuilder resultado = new StringBuilder();

        ListaDoblementeEnlazada<Arista> adyacentes = nodo.getAdyacentes();

        resultado.append("Nodo actual: ")
                .append(nodo.getContenido())
                .append(".\n");

        for (Arista arista : adyacentes) {
            resultado.append("[Valor: ")
                    .append(arista.getValor())
                    .append("] - ")
                    .append(arista.getExtremo(nodo))
                    .append("\n");
        }

        return resultado.toString();
    }

    public ListaDoblementeEnlazada<NodoGrafo<?>> obtenerGrafo(
            NodoGrafo<?> nodo,
            ListaDoblementeEnlazada<NodoGrafo<?>> visitados) {

        if (!visitados.contiene(nodo)) {
            visitados.agregar(nodo);
        }

        for (Arista arista : nodo.getAdyacentes()) {

            NodoGrafo<?> vecino = arista.getExtremo(nodo);

            if (!visitados.contiene(vecino)) {
                obtenerGrafo(vecino, visitados);
            }
        }

        return visitados;
    }

    // Recorrido en amplitud
    public ListaDoblementeEnlazada<NodoGrafo<?>> BFS(
            NodoGrafo<?> nodoInicio,
            NodoGrafo<?> nodoFin) {

        ListaDoblementeEnlazada<NodoGrafo<?>> visitados =
                new ListaDoblementeEnlazada<>();

        Cola<NodoGrafo<?>> cola = new Cola<>();

        cola.add(nodoInicio);
        visitados.agregar(nodoInicio);

        while (!cola.isEmpty()) {

            NodoGrafo<?> nodo = cola.poll();

            if (nodo.equals(nodoFin)) {
                return visitados;
            }

            for (Arista arista : nodo.getAdyacentes()) {

                NodoGrafo<?> vecino = arista.getExtremo(nodo);

                if (!visitados.contiene(vecino)) {
                    visitados.agregar(vecino);
                    cola.add(vecino);
                }
            }
        }

        return visitados;
    }

    // Recorrido en profundidad
    public Conjunto<NodoGrafo<?>> dfs(NodoGrafo<?> nodoInicio) {

        Conjunto<NodoGrafo<?>> visitados = new Conjunto<>();

        dfs(nodoInicio, visitados);

        return visitados;
    }

    private void dfs(
            NodoGrafo<?> nodo,
            Conjunto<NodoGrafo<?>> visitados) {

        visitados.agregar(nodo);

        for (Arista arista : nodo.getAdyacentes()) {

            NodoGrafo<?> vecino = arista.getExtremo(nodo);

            if (!visitados.contiene(vecino)) {
                dfs(vecino, visitados);
            }
        }
    }

    // Algoritmo de Dijkstra
    private Paso dijkstra(NodoGrafo<?> inicio, NodoGrafo<?> fin) {

        ColaPrioritaria<Paso> cola =
                new ColaPrioritaria<>(Comparator.comparingInt(Paso::getDistancia));

        Conjunto<NodoGrafo<?>> visitados = new Conjunto<>();

        cola.add(new Paso(inicio, 0, null));

        while (!cola.isEmpty()) {

            Paso actual = cola.poll();

            if (!visitados.contiene(actual.getNodo())) {

                visitados.agregar(actual.getNodo());

                if (actual.getNodo().equals(fin)) {
                    return actual;
                }

                for (Arista arista : actual.getNodo().getAdyacentes()) {

                    NodoGrafo<?> vecino =
                            arista.getExtremo(actual.getNodo());

                    if (!visitados.contiene(vecino)) {

                        int nuevaDistancia =
                                actual.getDistancia() + arista.getValor();

                        cola.add(new Paso(
                                vecino,
                                nuevaDistancia,
                                actual
                        ));
                    }
                }
            }
        }

        return null;
    }

    // Construcción del camino más corto
    public ListaDoblementeEnlazada<NodoGrafo<?>> armarCamino(
            NodoGrafo<?> inicio,
            NodoGrafo<?> fin) {

        return armarCamino(dijkstra(inicio, fin));
    }

    private ListaDoblementeEnlazada<NodoGrafo<?>> armarCamino(Paso fin) {

        Pila<NodoGrafo<?>> invertido = new Pila<>();

        Paso actual = fin;

        while (actual != null) {
            invertido.push(actual.getNodo());
            actual = actual.getAnterior();
        }

        ListaDoblementeEnlazada<NodoGrafo<?>> camino =
                new ListaDoblementeEnlazada<>();

        while (!invertido.isEmpty()) {
            camino.agregar(invertido.pop());
        }

        return camino;
    }

    public Integer costoCamino(
            NodoGrafo<?> inicio,
            NodoGrafo<?> fin) {

        Paso resultado = dijkstra(inicio, fin);

        if (resultado == null) {
            return null;
        }

        return resultado.getDistancia();
    }
}
