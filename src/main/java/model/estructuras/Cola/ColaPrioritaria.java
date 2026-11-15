
package model.estructuras.Cola;

import java.util.Comparator;
import java.util.Objects;

public class ColaPrioritaria<T> extends Cola<T> {

    private final Comparator<T> comparador;

    public ColaPrioritaria(Comparator<T> comparador) {
        super();
        this.comparador = Objects.requireNonNull(comparador);
    }

    @Override
    public void add(T dato) {
        Cola<T> auxiliar = new Cola<>();
        boolean insertado = false;

        while (!isEmpty()) {
            T actual = poll();

            if (!insertado && comparador.compare(dato, actual) < 0) {
                auxiliar.add(dato);
                insertado = true;
            }

            auxiliar.add(actual);
        }

        if (!insertado) {
            auxiliar.add(dato);
        }

        while (!auxiliar.isEmpty()) {
            super.add(auxiliar.poll());
        }
    }
}
