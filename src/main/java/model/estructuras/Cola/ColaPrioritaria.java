package model.estructuras.Cola;
/**
 * Implementa una cola prioritaria genérica mediante una cola enlazada.
 * Los elementos se mantienen ordenados de acuerdo con su orden natural,
 * determinado por la implementación de Comparable en el tipo T.
 *
 * La prioridad de los elementos se establece mediante compareTo().
 *
 * @param <T> Tipo de elementos almacenados. Debe implementar Comparable
 *            para permitir la comparación entre elementos.
 */
public class ColaPrioritaria<T extends Comparable<T>> extends Cola<T>{
    /**
     * Ordena los elementos de una cola utilizando un procedimiento
     * recursivo que selecciona el menor elemento en cada iteración.
     *
     * Extrae los elementos de la cola, identifica el de menor valor
     * mediante compareTo() y almacena los restantes en una cola auxiliar.
     * Posteriormente, ordena recursivamente los elementos restantes
     * y concatena ambas colas.
     *
     * @param cola Cola cuyos elementos se desean ordenar.
     * @return Nueva cola con los elementos ordenados de menor a mayor.
     */
    private Cola<T> ordenar(Cola<T> cola){
        Cola<T> cola_vieja=new Cola<>();
        Cola<T> cola_mayor=new Cola<>();
        T dato_mayor = cola.poll();
        if (cola.isEmpty()) {
            cola_mayor.add(dato_mayor);
            return cola_mayor;
        }
        T dato_actual = cola.poll();
        while (true){
            if (dato_actual==null) break;
            if (dato_mayor.compareTo(dato_actual) > 0){
                cola_vieja.add(dato_mayor);
                dato_mayor=dato_actual;
            }
            else{
                cola_vieja.add(dato_actual);
            }
            dato_actual = cola.poll();
        }
        cola_mayor.add(dato_mayor);
        Cola<T> aux= this.ordenar(cola_vieja);
        cola_mayor.merge(aux);
        return cola_mayor;
    }

    /**
     * Comprueba recursivamente si los elementos de la cola están
     * ordenados de menor a mayor.
     *
     * Compara el dato del nodo inicial con los datos de los nodos
     * siguientes. Si encuentra un elemento menor que el inicial,
     * determina que la cola no está ordenada. Si no encuentra
     * inconsistencias, continúa la comprobación desde el siguiente nodo.
     *
     * @param nodo Nodo desde el cual comienza la comprobación.
     * @return true si los elementos están ordenados; false en caso contrario.
     */
    private boolean ordenado(NodoCola<T> nodo){
        boolean ordenado=true;
        NodoCola<T> aux = nodo;
        while (true){
            if (aux.nextExist()) {
                aux = aux.getSiguiente();
                if (nodo.getDato().compareTo(aux.getDato()) > 0) {
                    ordenado=false;
                    break;
                }
            }
            else{
                if (nodo.nextExist()) return this.ordenado(nodo.getSiguiente());
                break;
            }
        }
        return ordenado;
    }
    /**
     * Inserta un elemento en la cola prioritaria.
     *
     * Primero agrega el elemento utilizando la implementación de la
     * clase padre. Posteriormente, verifica si la cola conserva el orden
     * establecido y, si es necesario, reorganiza sus elementos.
     *
     * @param dato Elemento que se desea insertar en la cola.
     */
    @Override
    public void add(T dato){
        super.add(dato);
        if (!ordenado(this.peek())) {
            this.merge(this.ordenar(this));
        }
    }
    /**
     * Incorpora todos los elementos de otra cola a la cola prioritaria.
     *
     * Los elementos se extraen de la cola recibida y se agregan a la cola
     * actual mediante la implementación de add() de la clase padre.
     *
     * @param cola Cola cuyos elementos se desean incorporar.
     * @throws IllegalArgumentException Si la cola recibida está vacía.
     */
    @Override
    public void merge(Cola<T> cola){
        if (cola.isEmpty()) throw new IllegalArgumentException("La cola a fusionar esta vacia");
        while (!cola.isEmpty()){
            T aux=cola.poll();
            super.add(aux);
        }
    }

}
