package model.estructuras.Grafo.Dijkstra;


import model.estructuras.Grafo.NodoGrafo;

/**
 * Representa un paso dentro del proceso de búsqueda de caminos
 * mediante el algoritmo de Dijkstra.
 *
 * Almacena un nodo del grafo, la distancia acumulada desde el nodo
 * de origen hasta él y una referencia al paso anterior, permitiendo
 * reconstruir posteriormente el camino recorrido.
 *
 * Implementa Comparable para permitir ordenar los pasos según
 * su distancia acumulada.
 */
public class Paso implements Comparable<Paso> {
    /**
     * Nodo del grafo asociado al paso actual.
     */
    private NodoGrafo<?> nodo;
    /**
     * Distancia acumulada desde el nodo de origen hasta el nodo actual.
     */
    private int distancia;
    /**
     * Referencia al paso anterior del recorrido.
     * Permite reconstruir la secuencia de nodos que conforman el camino.
     */
    private Paso anterior;
    /**
     * Construye un paso con el nodo, la distancia acumulada
     * y el paso anterior especificados.
     *
     * @param nodo Nodo del grafo correspondiente al paso.
     * @param distancia Distancia acumulada desde el origen hasta el nodo.
     * @param anterior Paso previo en el recorrido, o null si es el inicial.
     */
    public Paso(NodoGrafo<?> nodo, int distancia, Paso anterior) {
        this.nodo = nodo;
        this.distancia = distancia;
        this.anterior = anterior;
    }
    /**
     * Obtiene el nodo asociado al paso.
     *
     * @return Nodo del grafo.
     */
    public NodoGrafo<?> getNodo() { return nodo; }
    /**
     * Obtiene la distancia acumulada hasta el nodo actual.
     *
     * @return Distancia acumulada desde el origen.
     */
    public int getDistancia() { return distancia; }
    /**
     * Obtiene el paso anterior en el recorrido.
     *
     * @return Paso anterior, o null si es el paso inicial.
     */
    public Paso getAnterior() { return anterior; }
    /**
     * Compara dos pasos según sus distancias acumuladas.
     *
     * Permite establecer un orden natural en el que los pasos
     * con menor distancia tienen mayor prioridad.
     *
     * @param otro Paso con el que se realizará la comparación.
     * @return Valor negativo si este paso tiene menor distancia;
     *         cero si ambas distancias son iguales;
     *         valor positivo si este paso tiene mayor distancia.
     */
    @Override
    public int compareTo(Paso otro) {
        return this.distancia - otro.distancia;
    }
}