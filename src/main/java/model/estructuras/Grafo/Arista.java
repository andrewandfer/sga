
package model.estructuras.Grafo;

public class Arista {

    private Integer valor;
    private NodoGrafo<?> siguiente;
    private NodoGrafo<?> anterior;

    public Arista(Integer valor) {
        this.valor = valor;
    }

    public Integer getValor() {
        return valor;
    }

    public void setValor(Integer valor) {
        this.valor = valor;
    }

    public NodoGrafo<?> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoGrafo<?> siguiente) {
        this.siguiente = siguiente;
    }

    public NodoGrafo<?> getAnterior() {
        return anterior;
    }

    public void setAnterior(NodoGrafo<?> anterior) {
        this.anterior = anterior;
    }

    public NodoGrafo<?> getExtremo(NodoGrafo<?> nodo) {

        if (nodo.equals(siguiente)) {
            return anterior;
        }

        if (nodo.equals(anterior)) {
            return siguiente;
        }

        throw new IllegalArgumentException(
                "El nodo no pertenece a esta arista"
        );
    }

    @Override
    public String toString() {
        return String.valueOf(valor);
    }
}
