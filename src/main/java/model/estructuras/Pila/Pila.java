package model.estructuras.Pila;

public class Pila<T>{
    NodoPila<T> Raiz;
    public Pila(){
        Raiz=null;
    }
    public Pila(NodoPila<T> raiz){
        this.Raiz=raiz;
    }

    public void setRaiz(NodoPila<T> raiz) {
        Raiz = raiz;
    }
    public NodoPila<T> peek() {
        return Raiz;
    }
    public void push(T dato){
        NodoPila<T> nuevo=new NodoPila<>(dato);
        nuevo.setSiguiente(Raiz);
        this.setRaiz(nuevo);
    }
    public T pop(){
        NodoPila<T> aux=Raiz;
        this.setRaiz(Raiz.getSiguiente());
        aux.setSiguiente(null);
        return aux.getDato();
    }
    public boolean isEmpty(){
        return Raiz==null;
    }
    public String toString(){
        if (isEmpty()) return "[]";
        NodoPila<T> aux=Raiz;
        StringBuilder cadena=new StringBuilder();
        while (aux!=null){
            cadena.append(aux.getDato());
            if (aux.nextExist()) {
                cadena.append("->");}
            aux=aux.getSiguiente();
        }
        return cadena.toString();
    }
}
