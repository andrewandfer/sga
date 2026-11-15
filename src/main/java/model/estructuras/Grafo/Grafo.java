package model.estructuras.Grafo;

import model.estructuras.Cola.Cola;
import model.estructuras.Cola.ColaPrioritaria;
import model.estructuras.Conjunto;
import model.estructuras.Grafo.Dijkstra.Paso;
import model.estructuras.ListaDoblementeEnlazada;
import model.estructuras.Pila.Pila;

public class Grafo{

    public void agregarNodo(NodoGrafo<?> origen,NodoGrafo<?> destino, Integer valor) {

        Boolean existe_origin=false;
        Boolean existe_destino=false;
        if (origen.contieneAdyacente(destino)) {
            existe_origin=true;
        }

        if (destino.contieneAdyacente(origen)) {
            existe_destino=true;
        }
        if (existe_origin && existe_destino) {
            throw new IllegalArgumentException("El nodo ya existe en el grafo");
        }

        Arista arista=new Arista(valor);
        origen.agregarAdyacente(arista);
        destino.agregarAdyacente(arista);
        arista.setSiguiente(destino);
        arista.setAnterior(origen);
    }
    public String imprimir(NodoGrafo<?> nodo){
        StringBuilder sr=new StringBuilder();
        ListaDoblementeEnlazada<Arista> adyacentes=nodo.getAdyacentes();
        sr.append("Nodo actual: " + nodo.getContenido() + ".");
        sr.append("\n");
        for (Arista arista : adyacentes) {
            sr.append("[Valor: ").append(arista.getValor()).append("] -");
            sr.append(arista.getExtremo(nodo));
            sr.append("]");
            sr.append("\n");
        }
        return sr.toString();
    }
    public ListaDoblementeEnlazada<NodoGrafo<?>> obtenerGrafo(NodoGrafo<?> nodo, ListaDoblementeEnlazada<NodoGrafo<?>> grafos_visitados){
        if (!grafos_visitados.contiene(nodo)) {
        grafos_visitados.agregar(nodo);}
        for (Arista arista : nodo.getAdyacentes()) {
            NodoGrafo<?> aux;
            aux = arista.getExtremo(nodo);
            if (!grafos_visitados.contiene(aux)) {
                grafos_visitados.agregar(aux);
                ListaDoblementeEnlazada<NodoGrafo<?>> aux_grafos_visitados= this.obtenerGrafo(aux,grafos_visitados);
                grafos_visitados.merge( aux_grafos_visitados);
            }
        }
        return grafos_visitados;
    }
    public ListaDoblementeEnlazada<NodoGrafo<?>> BFS(NodoGrafo<?> nodo_inicio, NodoGrafo<?> nodo_fin){
        ListaDoblementeEnlazada<NodoGrafo<?>> grafos_visitados=new ListaDoblementeEnlazada<>();
        Cola<NodoGrafo<?>> cola=new Cola<>();

        cola.add(nodo_inicio);
        while (!cola.isEmpty()) {
            NodoGrafo<?> nodo=cola.poll();
            if (!grafos_visitados.contiene(nodo)) {
                grafos_visitados.agregar(nodo);}
            if (nodo.equals(nodo_fin)) {
                return grafos_visitados;
            }
            for (Arista arista : nodo.getAdyacentes()) {
                cola.add(arista.getExtremo(nodo));
            }

        }
        return grafos_visitados;
    }
    public Conjunto<NodoGrafo<?>> dfs(NodoGrafo<?> nodo_inicio){
        Conjunto<NodoGrafo<?>> visitados=new Conjunto<>();
        dfs(nodo_inicio,visitados);
        return visitados;

    }
    private void dfs (NodoGrafo<?> nodo, Conjunto<NodoGrafo<?>> visitados){
        visitados.agregar(nodo);
        for (Arista arista : nodo.getAdyacentes()) {
            NodoGrafo<?> aux = arista.getExtremo(nodo);
            if (!visitados.contiene(aux)) {
                dfs(aux,visitados);
            }
        }
    }
    private Paso dijkstra(NodoGrafo<?> inicio, NodoGrafo<?> fin) {
        ColaPrioritaria<Paso> cola = new ColaPrioritaria<>();
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
                    NodoGrafo<?> vecino = arista.getExtremo(actual.getNodo());
                    if (!visitados.contiene(vecino)) {
                        int nueva = actual.getDistancia() + arista.getValor();
                        cola.add(new Paso(vecino, nueva, actual));
                    }
                }
            }
        }
        return null;
    }
    public ListaDoblementeEnlazada<NodoGrafo<?>> armarCamino(NodoGrafo<?> inicio, NodoGrafo<?> fin) {
        return armarCamino(dijkstra(inicio, fin));
    }

    private ListaDoblementeEnlazada<NodoGrafo<?>> armarCamino(Paso fin) {
        Pila<NodoGrafo<?>> invertido = new Pila<>();
        Paso actual = fin;
        while (actual != null) {
            invertido.push(actual.getNodo());
            actual = actual.getAnterior();
        }

        ListaDoblementeEnlazada<NodoGrafo<?>> camino = new ListaDoblementeEnlazada<>();
        while (!invertido.isEmpty()) {
            camino.agregar(invertido.pop());
        }
        return camino;
    }
    public Integer costoCamino(NodoGrafo<?> inicio, NodoGrafo<?> fin) {
        Paso paso = dijkstra(inicio, fin);
        if (paso == null) {
            return null;
        }
        return paso.getDistancia();
    }
}
