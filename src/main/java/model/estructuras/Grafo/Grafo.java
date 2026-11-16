package model.estructuras.Grafo;

import model.estructuras.Cola.Cola;
import model.estructuras.Cola.ColaPrioritaria;
import model.estructuras.Conjunto;
import model.estructuras.Grafo.Dijkstra.Paso;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.estructuras.Pila.Pila;

import java.util.Comparator;
import java.util.Objects;

public class Grafo {

    // Agregar una conexión bidireccional entre dos nodos.
    public void agregarNodo(
            NodoGrafo<?> origen,
            NodoGrafo<?> destino,
            Integer valor) {

        if (origen == null || destino == null) {
            throw new IllegalArgumentException(
                    "Los nodos no pueden ser null"
            );
        }

        if (valor == null || valor < 0) {
            throw new IllegalArgumentException(
                    "El peso debe ser un entero no negativo"
            );
        }

        if (origen == destino) {
            throw new IllegalArgumentException(
                    "No se puede conectar un nodo consigo mismo"
            );
        }

        if (origen.contieneAdyacente(destino)) {
            throw new IllegalArgumentException(
                    "La conexión entre los nodos ya existe"
            );
        }

        Arista arista = new Arista(valor, origen, destino);

        origen.agregarAdyacente(arista);
        destino.agregarAdyacente(arista);
    }

    // Imprimir las conexiones de un nodo.
    public String imprimir(NodoGrafo<?> nodo) {

        Objects.requireNonNull(nodo, "El nodo no puede ser null");

        StringBuilder resultado = new StringBuilder();

        resultado.append("Nodo actual: ")
                .append(nodo.getContenido())
                .append(".\n");

        for (Arista arista : nodo.getAdyacentes()) {

            resultado.append("[Valor: ")
                    .append(arista.getValor())
                    .append("] - ")
                    .append(arista.getExtremo(nodo))
                    .append("\n");
        }

        return resultado.toString();
    }

    // Obtener todos los nodos alcanzables mediante recursividad.
    public ListaDoblementeEnlazada<NodoGrafo<?>> obtenerGrafo(
            NodoGrafo<?> nodo,
            ListaDoblementeEnlazada<NodoGrafo<?>> visitados) {

        Objects.requireNonNull(nodo, "El nodo no puede ser null");
        Objects.requireNonNull(visitados, "La lista no puede ser null");

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

    // Recorrido en amplitud.
    public ListaDoblementeEnlazada<NodoGrafo<?>> BFS(
            NodoGrafo<?> nodoInicio,
            NodoGrafo<?> nodoFin) {

        Objects.requireNonNull(nodoInicio, "El nodo inicial no puede ser null");
        Objects.requireNonNull(nodoFin, "El nodo final no puede ser null");

        ListaDoblementeEnlazada<NodoGrafo<?>> visitados =
                new ListaDoblementeEnlazada<>();

        Cola<NodoGrafo<?>> cola = new Cola<>();

        cola.add(nodoInicio);
        visitados.agregar(nodoInicio);

        while (!cola.isEmpty()) {

            NodoGrafo<?> nodo = cola.poll();

            if (nodo == nodoFin) {
                return visitados;
            }

            for (Arista arista : nodo.getAdyacentes()) {

                NodoGrafo<?> vecino = arista.getExtremo(nodo);

                if (!visitados.contiene(vecino)) {

                    // Se marca al descubrirlo, no al extraerlo.
                    visitados.agregar(vecino);
                    cola.add(vecino);
                }
            }
        }

        return visitados;
    }

    // Recorrido en profundidad.
    public Conjunto<NodoGrafo<?>> dfs(NodoGrafo<?> nodoInicio) {

        Objects.requireNonNull(nodoInicio, "El nodo inicial no puede ser null");

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

    // Algoritmo de Dijkstra.
    private Paso dijkstra(
            NodoGrafo<?> inicio,
            NodoGrafo<?> fin) {

        Objects.requireNonNull(inicio, "El nodo inicial no puede ser null");
        Objects.requireNonNull(fin, "El nodo final no puede ser null");

        ColaPrioritaria<Paso> cola =
                new ColaPrioritaria<>(
                        Comparator.comparingInt(Paso::getDistancia)
                );

        Conjunto<NodoGrafo<?>> visitados = new Conjunto<>();

        cola.add(new Paso(inicio, 0, null));

        while (!cola.isEmpty()) {

            Paso actual = cola.poll();

            if (!visitados.contiene(actual.getNodo())) {

                visitados.agregar(actual.getNodo());

                if (actual.getNodo() == fin) {
                    return actual;
                }

                for (Arista arista : actual.getNodo().getAdyacentes()) {

                    NodoGrafo<?> vecino =
                            arista.getExtremo(actual.getNodo());

                    if (!visitados.contiene(vecino)) {

                        int nuevaDistancia = Math.addExact(
                                actual.getDistancia(),
                                arista.getValor()
                        );

                        cola.add(
                                new Paso(
                                        vecino,
                                        nuevaDistancia,
                                        actual
                                )
                        );
                    }
                }
            }
        }

        return null;
    }

    // Construcción del camino más corto.
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

    // Obtener el costo del camino más corto.
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