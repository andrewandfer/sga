package model.excepciones;


public class TransicionInvalidaException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
