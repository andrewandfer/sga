
package model.estructuras.Grafo.Tipo_nodos;

public class CuartosAseo {

    private Integer numero;

    public CuartosAseo(Integer numero) {
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
        return "Cuartos de aseo número: " + numero;
    }
}
