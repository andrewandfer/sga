package model.estructuras.Grafo;

import model.estructuras.Cola.Cola;
import model.estructuras.Cola.ColaPrioritaria;
import model.estructuras.Conjunto;
import model.estructuras.Grafo.Dijkstra.Paso;
import model.estructuras.Pila.Pila;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public class Grafo<T> implements Iterable<NodoGrafo<T>> {

    private final ListaDoblementeEnlazada<NodoGrafo<T>> nodos =
            new ListaDoblementeEnlazada<>();

    public void registrarNodo(NodoGrafo<T> nodo) {
        Objects.requireNonNull(nodo, "El nodo no puede ser null");
        if (!nodos.contiene(nodo)) nodos.agregar(nodo);
    }

    public void registrarNodo(T contenido) {
        registrarNodo(new NodoGrafo<>(contenido));
    }

    public void agregarNodo(NodoGrafo<T> origen, NodoGrafo<T> destino, Integer valor) {
        agregarNodo(origen, destino, valor, null);
    }

    /** Agrega una conexion y conserva una etiqueta opcional asociada a la arista. */
    public void agregarNodo(NodoGrafo<T> origen, NodoGrafo<T> destino,
                            Integer valor, Object etiqueta) {
        Objects.requireNonNull(origen, "El nodo origen no puede ser null");
        Objects.requireNonNull(destino, "El nodo destino no puede ser null");
        if (valor == null || valor < 0) throw new IllegalArgumentException("El peso debe ser no negativo");
        if (origen == destino) throw new IllegalArgumentException("No se permiten lazos");
        if (origen.contieneAdyacente(destino)) throw new IllegalArgumentException("La conexión ya existe");

        Arista<T> arista = new Arista<>(valor, origen, destino, etiqueta);
        origen.agregarAdyacente(arista);
        destino.agregarAdyacente(arista);
        registrarNodo(origen);
        registrarNodo(destino);
    }

    public void cerrarConexion(NodoGrafo<T> a, NodoGrafo<T> b) {
        buscarArista(a, b).cerrar();
    }

    public void abrirConexion(NodoGrafo<T> a, NodoGrafo<T> b) {
        buscarArista(a, b).abrir();
    }

    private Arista<T> buscarArista(NodoGrafo<T> a, NodoGrafo<T> b) {
        Objects.requireNonNull(a, "Nodo A obligatorio");
        Objects.requireNonNull(b, "Nodo B obligatorio");
        for (Arista<T> arista : a.getAdyacentes()) {
            if (arista.getExtremo(a) == b) return arista;
        }
        throw new IllegalArgumentException("No existe conexión entre los nodos");
    }

    public String imprimir(NodoGrafo<T> nodo) {
        Objects.requireNonNull(nodo, "El nodo no puede ser null");
        StringBuilder resultado = new StringBuilder("Nodo actual: ")
                .append(nodo.getContenido()).append(".\n");
        for (Arista<T> arista : nodo.getAdyacentes()) {
            resultado.append("[Valor: ").append(arista.getValor())
                    .append(arista.isActiva() ? ", abierta" : ", cerrada");
            if (arista.getEtiqueta() != null) {
                resultado.append(", conexión: ").append(arista.getEtiqueta());
            }
            resultado.append("] - ").append(arista.getExtremo(nodo)).append("\n");
        }
        return resultado.toString();
    }

    public ListaDoblementeEnlazada<NodoGrafo<T>> obtenerGrafo(
            NodoGrafo<T> nodo, ListaDoblementeEnlazada<NodoGrafo<T>> visitados) {
        Objects.requireNonNull(nodo, "El nodo no puede ser null");
        Objects.requireNonNull(visitados, "La lista no puede ser null");
        if (!visitados.contiene(nodo)) visitados.agregar(nodo);
        for (Arista<T> arista : nodo.getAdyacentes()) {
            if (arista.isActiva()) {
                NodoGrafo<T> vecino = arista.getExtremo(nodo);
                if (!visitados.contiene(vecino)) obtenerGrafo(vecino, visitados);
            }
        }
        return visitados;
    }

    public ListaDoblementeEnlazada<NodoGrafo<T>> BFS(
            NodoGrafo<T> inicio, NodoGrafo<T> fin) {
        Objects.requireNonNull(inicio, "El nodo inicial no puede ser null");
        Objects.requireNonNull(fin, "El nodo final no puede ser null");
        ListaDoblementeEnlazada<NodoGrafo<T>> visitados = new ListaDoblementeEnlazada<>();
        Cola<NodoGrafo<T>> cola = new Cola<>();
        cola.add(inicio);
        visitados.agregar(inicio);
        while (!cola.isEmpty()) {
            NodoGrafo<T> actual = cola.poll();
            if (actual == fin) return visitados;
            for (Arista<T> arista : actual.getAdyacentes()) {
                if (!arista.isActiva()) continue;
                NodoGrafo<T> vecino = arista.getExtremo(actual);
                if (!visitados.contiene(vecino)) {
                    visitados.agregar(vecino);
                    cola.add(vecino);
                }
            }
        }
        return visitados;
    }

    public Conjunto<NodoGrafo<T>> dfs(NodoGrafo<T> inicio) {
        Objects.requireNonNull(inicio, "El nodo inicial no puede ser null");
        Conjunto<NodoGrafo<T>> visitados = new Conjunto<>();
        dfsRecursivo(inicio, visitados);
        return visitados;
    }

    private void dfsRecursivo(NodoGrafo<T> actual, Conjunto<NodoGrafo<T>> visitados) {
        visitados.agregar(actual);
        for (Arista<T> arista : actual.getAdyacentes()) {
            if (!arista.isActiva()) continue;
            NodoGrafo<T> vecino = arista.getExtremo(actual);
            if (!visitados.contiene(vecino)) dfsRecursivo(vecino, visitados);
        }
    }

    private Paso<T> dijkstra(NodoGrafo<T> inicio, NodoGrafo<T> fin) {
        Objects.requireNonNull(inicio, "El nodo inicial no puede ser null");
        Objects.requireNonNull(fin, "El nodo final no puede ser null");
        ColaPrioritaria<Paso<T>> cola = new ColaPrioritaria<>(
                Comparator.comparingInt(Paso::getDistancia));
        Conjunto<NodoGrafo<T>> visitados = new Conjunto<>();
        cola.add(new Paso<>(inicio, 0, null));
        while (!cola.isEmpty()) {
            Paso<T> paso = cola.poll();
            NodoGrafo<T> actual = paso.getNodo();
            if (visitados.contiene(actual)) continue;
            visitados.agregar(actual);
            if (actual == fin) return paso;
            for (Arista<T> arista : actual.getAdyacentes()) {
                if (!arista.isActiva()) continue;
                NodoGrafo<T> vecino = arista.getExtremo(actual);
                if (!visitados.contiene(vecino)) {
                    int distancia = Math.addExact(paso.getDistancia(), arista.getValor());
                    cola.add(new Paso<>(vecino, distancia, paso));
                }
            }
        }
        return null;
    }

    public ListaDoblementeEnlazada<NodoGrafo<T>> armarCamino(
            NodoGrafo<T> inicio, NodoGrafo<T> fin) {
        Paso<T> resultado = dijkstra(inicio, fin);
        Pila<NodoGrafo<T>> invertido = new Pila<>();
        while (resultado != null) {
            invertido.push(resultado.getNodo());
            resultado = resultado.getAnterior();
        }
        ListaDoblementeEnlazada<NodoGrafo<T>> camino = new ListaDoblementeEnlazada<>();
        while (!invertido.isEmpty()) camino.agregar(invertido.pop());
        return camino;
    }

    public Integer costoCamino(NodoGrafo<T> inicio, NodoGrafo<T> fin) {
        Paso<T> resultado = dijkstra(inicio, fin);
        return resultado == null ? null : resultado.getDistancia();
    }

    public int cantidadNodos() { return nodos.tamano(); }

    @Override
    public Iterator<NodoGrafo<T>> iterator() { return nodos.iterator(); }
}
