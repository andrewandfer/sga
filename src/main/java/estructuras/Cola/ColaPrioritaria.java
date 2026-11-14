package estructuras.Cola;

public class ColaPrioritaria<T extends Comparable<T>> extends Cola<T>{

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

    @Override
    public void add(T dato){
        super.add(dato);
        if (!ordenado(this.peek())) {
            this.merge(this.ordenar(this));
        }
    }
    @Override
    public void merge(Cola<T> cola){
        if (cola.isEmpty()) throw new IllegalArgumentException("La cola a fusionar esta vacia");
        while (!cola.isEmpty()){
            T aux=cola.poll();
            super.add(aux);
        }
    }

}
