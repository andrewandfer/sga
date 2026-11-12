package estructuras;

import java.util.Iterator;
import java.util.Objects;

public class Conjunto<T> implements Iterable<T> {

    private final ListaDoblementeEnlazada<T> elementos = new ListaDoblementeEnlazada<>();

    public boolean agregar(T dato) {
        Objects.requireNonNull(dato, "El conjunto no admite elementos nulos");
        if (contiene(dato)) {
            return false;
        }
        elementos.agregar(dato);
        return true;
    }

    public boolean contiene(T dato) {
        return elementos.contiene(dato);
    }

    public boolean eliminar(T dato) {
        return elementos.eliminar(dato);
    }

    public int tamano() {
        return elementos.tamano();
    }

    public boolean estaVacio() {
        return elementos.estaVacia();
    }

    @Override
    public Iterator<T> iterator() {
        return elementos.iterator();
    }

    @Override
    public String toString() {
        return elementos.toString();
    }
}