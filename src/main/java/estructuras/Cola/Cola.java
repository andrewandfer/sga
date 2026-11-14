package estructuras.Cola;

public class Cola<T> {
    private NodoCola<T> Raiz;


    public Cola(){
        Raiz=null;
    }
    public Cola(NodoCola<T> Raiz){
        this.Raiz=Raiz;
    }
    public void setRaiz(NodoCola<T> Raiz){
        this.Raiz=Raiz;
    }
    public NodoCola<T> peek(){
        return Raiz;
    }
    public T poll(){
        if (isEmpty()) return null;
        NodoCola<T> aux=Raiz;
        Raiz=Raiz.getSiguiente();
        return aux.getDato();
    }
    public void add(T dato){
        if (Raiz==null) this.Raiz = new NodoCola<>(dato);
        else {NodoCola<T> aux= new NodoCola<>(dato);
        Raiz.add(dato);}
    }
    public boolean isEmpty(){
        return Raiz==null;
    }
    public boolean contains(T dato){
        if (isEmpty()) return false;
        return Raiz.contains(dato);
    }
    public void merge(Cola<T> cola){
        if (cola.isEmpty()) throw new IllegalArgumentException("La cola a fusionar esta vacia");
        while (!cola.isEmpty()){
            T aux=cola.poll();
            this.add(aux);
        }
    }

    public String toString() {
        if (isEmpty()) return "[]";
        return Raiz.toString();
    }
}
