
package model.estructuras.Grafo.Tipo_nodos;

public class Pasillo {

    private Integer numero;

    public Pasillo(Integer numero) {
        this.numero = numero;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    @Override
    public String toString() {
        return "Pasillo número: " + numero;
    }
}
