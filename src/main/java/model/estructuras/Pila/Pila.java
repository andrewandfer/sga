
package model.estructuras.Pila;

import model.excepciones.EstructuraVaciaException;

import java.util.Iterator;

public class Pila<T> implements Iterable<T> {

    private NodoPila<T> tope;
    private int tamanio;

    public Pila() {
        tope = null;
        tamanio = 0;
    }

    public void push(T dato) {
        NodoPila<T> nuevo = new NodoPila<>(dato);

        nuevo.setSiguiente(tope);
        tope = nuevo;

        tamanio++;
    }

    public T pop() {
        if (isEmpty()) {
            throw new EstructuraVaciaException("La pila está vacía");
        }

        T dato = tope.getDato();
        tope = tope.getSiguiente();

        tamanio--;

        return dato;
    }

    public T peek() {
        if (isEmpty()) {
            throw new EstructuraVaciaException("La pila está vacía");
        }

        return tope.getDato();
    }

    public boolean isEmpty() {
        return tope == null;
    }

    public int size() {
        return tamanio;
    }

    public void clear() {
        tope = null;
        tamanio = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new IteradorPila<>(tope);
    }

    @Override
    public String toString() {
        StringBuilder cadena = new StringBuilder();

        Iterator<T> iterador = iterator();

        while (iterador.hasNext()) {
            cadena.append(iterador.next());

            if (iterador.hasNext()) {
                cadena.append("->");
            }
        }

        return cadena.toString();
    }
}
