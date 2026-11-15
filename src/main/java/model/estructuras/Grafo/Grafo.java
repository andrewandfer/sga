package model.estructuras.Grafo;

import model.estructuras.Cola.Cola;
import model.estructuras.Cola.ColaPrioritaria;
import model.estructuras.Conjunto;
import model.estructuras.Grafo.Dijkstra.Paso;
import model.estructuras.ListaDoblementeEnlazada;
import model.estructuras.Pila.Pila;
/**
 * Implementa operaciones sobre una estructura de datos Grafo.
 *
 * Permite establecer conexiones entre nodos mediante aristas con
 * valores asociados, consultar sus adyacencias y realizar recorridos
 * utilizando algoritmos de búsqueda en profundidad, búsqueda en anchura
 * y Dijkstra.
 *
 * Las conexiones se representan mediante objetos Arista compartidos
 * por las listas de adyacencia de los dos nodos que conectan.
 */
public class Grafo{
    /**
     * Establece una conexión entre dos nodos mediante una nueva arista.
     *
     * La arista creada se incorpora a las listas de adyacencia de ambos
     * nodos y almacena las referencias a sus extremos.
     *
     * Antes de establecer la conexión, verifica si ambos nodos ya
     * contienen una referencia de adyacencia entre sí.
     *
     * @param origen Nodo desde el cual se establece la conexión.
     * @param destino Nodo con el cual se establece la conexión.
     * @param valor Peso o costo asociado a la arista.
     * @throws IllegalArgumentException Si ambos nodos ya contienen
     *                                  la conexión entre sí.
     */
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
    /**
     * Genera una representación textual de un nodo y sus conexiones.
     *
     * Incluye el contenido del nodo y, para cada arista adyacente,
     * su valor y el nodo ubicado en el extremo opuesto.
     *
     * @param nodo Nodo cuyas conexiones se desean representar.
     * @return Cadena con la información del nodo y sus adyacencias.
     */
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
    /**
     * Obtiene recursivamente los nodos alcanzables desde un nodo inicial.
     *
     * Recorre las aristas adyacentes y agrega a la lista los nodos
     * que todavía no han sido visitados. La exploración continúa
     * recursivamente desde cada nuevo nodo encontrado.
     *
     * La lista recibida permite conservar los nodos visitados durante
     * todo el recorrido y evitar procesar nuevamente los ya registrados.
     *
     * @param nodo Nodo desde el cual continúa la exploración.
     * @param grafos_visitados Lista de nodos registrados durante el recorrido.
     * @return Lista de nodos alcanzables sin repetir los ya registrados.
     */
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
    /**
     * Realiza un recorrido del grafo utilizando búsqueda en anchura (BFS).
     *
     * Emplea una cola para explorar los nodos por niveles, comenzando
     * desde el nodo inicial. Los nodos extraídos se incorporan a la
     * lista de visitados si todavía no se encuentran registrados.
     *
     * El recorrido finaliza cuando se alcanza el nodo de destino
     * o cuando se agotan los elementos de la cola.
     *
     * @param nodo_inicio Nodo desde el cual comienza el recorrido.
     * @param nodo_fin Nodo que determina la finalización del recorrido.
     * @return Lista de nodos visitados hasta alcanzar el destino
     *         o agotar las conexiones disponibles.
     */
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
    /**
     * Realiza un recorrido del grafo utilizando búsqueda en profundidad
     * (DFS).
     *
     * Inicializa un conjunto de nodos visitados y comienza la exploración
     * recursiva desde el nodo indicado.
     *
     * @param nodo_inicio Nodo desde el cual comienza la exploración.
     * @return Conjunto de nodos alcanzables desde el nodo inicial.
     */
    public Conjunto<NodoGrafo<?>> dfs(NodoGrafo<?> nodo_inicio){
        Conjunto<NodoGrafo<?>> visitados=new Conjunto<>();
        dfs(nodo_inicio,visitados);
        return visitados;

    }
    /**
     * Explora recursivamente los nodos adyacentes mediante DFS.
     *
     * Registra el nodo actual como visitado y continúa la exploración
     * por cada nodo adyacente que todavía no pertenezca al conjunto
     * de visitados.
     *
     * @param nodo Nodo que se está explorando.
     * @param visitados Conjunto que almacena los nodos ya visitados.
     */
    private void dfs (NodoGrafo<?> nodo, Conjunto<NodoGrafo<?>> visitados){
        visitados.agregar(nodo);
        for (Arista arista : nodo.getAdyacentes()) {
            NodoGrafo<?> aux = arista.getExtremo(nodo);
            if (!visitados.contiene(aux)) {
                dfs(aux,visitados);
            }
        }
    }
    /**
     * Calcula el camino de menor costo entre dos nodos mediante
     * el algoritmo de Dijkstra.
     *
     * Utiliza una cola prioritaria para procesar primero los pasos
     * con menor distancia acumulada y un conjunto para registrar
     * los nodos cuyo costo mínimo ya ha sido determinado.
     *
     * Cada paso conserva una referencia al paso anterior, permitiendo
     * reconstruir posteriormente el camino encontrado.
     *
     * @param inicio Nodo desde el cual comienza la búsqueda.
     * @param fin Nodo cuyo camino mínimo se desea encontrar.
     * @return Paso correspondiente al nodo de destino, con su distancia
     *         acumulada y referencias al recorrido; null si no existe
     *         un camino entre ambos nodos.
     */
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
    /**
     * Construye el camino de menor costo entre dos nodos utilizando
     * el algoritmo de Dijkstra.
     *
     * Obtiene el paso final de la búsqueda y delega la reconstrucción
     * del recorrido al método privado correspondiente.
     *
     * @param inicio Nodo desde el cual comienza el camino.
     * @param fin Nodo al cual se desea llegar.
     * @return Lista de nodos que conforman el camino desde el origen
     *         hasta el destino. Si no existe un camino, devuelve
     *         una lista vacía.
     */
    public ListaDoblementeEnlazada<NodoGrafo<?>> armarCamino(NodoGrafo<?> inicio, NodoGrafo<?> fin) {
        return armarCamino(dijkstra(inicio, fin));
    }
    /**
     * Reconstruye el camino a partir de un paso final de Dijkstra.
     *
     * Recorre las referencias a los pasos anteriores, almacenando
     * los nodos en una pila para invertir el orden de recuperación.
     * Posteriormente, extrae los nodos de la pila y los incorpora
     * a una lista doblemente enlazada, obteniendo el recorrido
     * desde el origen hasta el destino.
     *
     * @param fin Paso correspondiente al nodo de destino.
     * @return Lista de nodos que conforman el camino reconstruido.
     *         Devuelve una lista vacía si el paso recibido es null.
     */
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
    /**
     * Calcula el costo mínimo del camino entre dos nodos mediante
     * el algoritmo de Dijkstra.
     *
     * Ejecuta el algoritmo para obtener el paso correspondiente
     * al destino y consulta su distancia acumulada.
     *
     * @param inicio Nodo desde el cual comienza el cálculo.
     * @param fin Nodo cuyo costo mínimo se desea determinar.
     * @return Costo mínimo acumulado desde el origen hasta el destino,
     *         o null si no existe un camino entre ambos nodos.
     */
    public Integer costoCamino(NodoGrafo<?> inicio, NodoGrafo<?> fin) {
        Paso paso = dijkstra(inicio, fin);
        if (paso == null) {
            return null;
        }
        return paso.getDistancia();
    }
}
