package excepciones;

public class CapacidadExcedidaException extends ExcepcionSGA {

    private static final long serialVersionUID = 1L;

    public CapacidadExcedidaException(String mensaje) {
        super(mensaje);
    }
}