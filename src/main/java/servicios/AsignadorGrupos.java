package servicios;

import estructuras.ListaDoblementeEnlazada;
import excepciones.CapacidadExcedidaException;
import modelo.Apartamento;

public class AsignadorGrupos {

    public ListaDoblementeEnlazada<Apartamento> asignar(int personas, ListaDoblementeEnlazada<Apartamento> libres)
            throws CapacidadExcedidaException {
        if (personas <= 0) {
            throw new IllegalArgumentException("El grupo debe tener al menos una persona");
        }
        int n = libres.tamano();
        Apartamento[] candidatos = new Apartamento[n];
        int i = 0;
        for (Apartamento a : libres) {
            candidatos[i++] = a;
        }
        ordenarPorCapacidadDescendente(candidatos);

        int[] sufijo = new int[n + 1];
        for (int k = n - 1; k >= 0; k--) {
            sufijo[k] = sufijo[k + 1] + candidatos[k].getCapacidadMaxima();
        }
        if (sufijo[0] < personas) {
            throw new CapacidadExcedidaException("Los apartamentos libres suman " + sufijo[0]
                    + " cupos y el grupo es de " + personas + " personas");
        }

        Busqueda busqueda = new Busqueda(candidatos, sufijo, personas);
        busqueda.retroceder(0, 0, 0);

        ListaDoblementeEnlazada<Apartamento> resultado = new ListaDoblementeEnlazada<>();
        for (int k = 0; k < busqueda.mejorCantidad; k++) {
            resultado.agregar(busqueda.mejor[k]);
        }
        return resultado;
    }

    private void ordenarPorCapacidadDescendente(Apartamento[] v) {
        for (int i = 1; i < v.length; i++) {
            Apartamento clave = v[i];
            int j = i - 1;
            while (j >= 0 && vaDespues(v[j], clave)) {
                v[j + 1] = v[j];
                j--;
            }
            v[j + 1] = clave;
        }
    }

    private boolean vaDespues(Apartamento a, Apartamento b) {
        if (a.getCapacidadMaxima() != b.getCapacidadMaxima()) {
            return a.getCapacidadMaxima() < b.getCapacidadMaxima();
        }
        return a.compareTo(b) > 0;
    }

    private static final class Busqueda {
        private final Apartamento[] candidatos;
        private final int[] sufijo;
        private final int objetivo;
        private final Apartamento[] actual;
        private final Apartamento[] mejor;
        private int mejorCantidad = Integer.MAX_VALUE;
        private int mejorCapacidad = Integer.MAX_VALUE;

        Busqueda(Apartamento[] candidatos, int[] sufijo, int objetivo) {
            this.candidatos = candidatos;
            this.sufijo = sufijo;
            this.objetivo = objetivo;
            this.actual = new Apartamento[candidatos.length];
            this.mejor = new Apartamento[candidatos.length];
        }


        void retroceder(int indice, int cantidad, int capacidad) {
            if (capacidad >= objetivo) {
                registrar(cantidad, capacidad);
                return;
            }
            if (indice == candidatos.length) {
                return;
            }
            if (cantidad >= mejorCantidad) {
                return;
            }
            if (capacidad + sufijo[indice] < objetivo) {
                return;
            }
            actual[cantidad] = candidatos[indice];
            retroceder(indice + 1, cantidad + 1, capacidad + candidatos[indice].getCapacidadMaxima());
            actual[cantidad] = null;
            retroceder(indice + 1, cantidad, capacidad);
        }

        private void registrar(int cantidad, int capacidad) {
            if (cantidad < mejorCantidad || (cantidad == mejorCantidad && capacidad < mejorCapacidad)) {
                mejorCantidad = cantidad;
                mejorCapacidad = capacidad;
                for (int k = 0; k < cantidad; k++) {
                                mejor[k] = actual[k];
                }
            }
        }
    }
}
