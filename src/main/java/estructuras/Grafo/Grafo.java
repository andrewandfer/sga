package estructuras.Grafo;

import estructuras.ListaDoblementeEnlazada;

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
            NodoGrafo<?> anterior = arista.getAnterior();
            NodoGrafo<?> siguiente = arista.getSiguiente();
            sr.append("[");
            if (anterior.equals(nodo)) {
                sr.append(siguiente.getContenido());
            }
            else{
                sr.append(anterior.getContenido());
            }
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
            if (arista.getSiguiente().equals(nodo)) {
                aux = arista.getAnterior();
            }
            else{
                aux = arista.getSiguiente();
            }
            if (!grafos_visitados.contiene(aux)) {
                grafos_visitados.agregar(aux);
                ListaDoblementeEnlazada<NodoGrafo<?>> aux_grafos_visitados= this.obtenerGrafo(aux,grafos_visitados);
                grafos_visitados.merge( aux_grafos_visitados);
            }
        }
        return grafos_visitados;
    }

}
