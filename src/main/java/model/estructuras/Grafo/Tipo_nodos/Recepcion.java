
package model.estructuras.Grafo.Tipo_nodos;

public class Recepcion {

    private Integer numero;

    public Recepcion(Integer numero) {
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
        return "Recepción número: " + numero;
    }
}
