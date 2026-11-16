package model.estructuras.Grafo;

public class Arista<T> {

    private final int valor;
    private final NodoGrafo<T> origen;
    private final NodoGrafo<T> destino;
    private boolean activa = true;

    public Arista(int valor, NodoGrafo<T> origen, NodoGrafo<T> destino) {
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("Los extremos no pueden ser null");
        }
        if (origen == destino) {
            throw new IllegalArgumentException("Una arista no puede conectar un nodo consigo mismo");
        }
        if (valor < 0) {
            throw new IllegalArgumentException("El peso no puede ser negativo");
        }
        this.valor = valor;
        this.origen = origen;
        this.destino = destino;
    }

    public int getValor() { return valor; }
    public NodoGrafo<T> getOrigen() { return origen; }
    public NodoGrafo<T> getDestino() { return destino; }
    public boolean isActiva() { return activa; }
    public void cerrar() { activa = false; }
    public void abrir() { activa = true; }

    public NodoGrafo<T> getExtremo(NodoGrafo<T> nodo) {
        if (nodo == origen) return destino;
        if (nodo == destino) return origen;
        throw new IllegalArgumentException("El nodo no pertenece a esta arista");
    }

    @Override
    public String toString() {
        return valor + (activa ? "" : " (cerrada)");
    }
}
