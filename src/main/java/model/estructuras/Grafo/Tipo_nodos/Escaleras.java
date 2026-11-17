
package model.estructuras.Grafo.Tipo_nodos;

public class Escaleras {

    private Integer numero;

    public Escaleras(Integer numero) {
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
        return "Escaleras número: " + numero;
    }
}
