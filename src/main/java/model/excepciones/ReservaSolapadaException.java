package model.excepciones;
/**
 * Excepción que indica que una reserva se solapa con otra existente.
 */
public class ReservaSolapadaException extends ExcepcionSGA {
    /** Identificador de versión para la serialización de la excepción. */
    private static final long serialVersionUID = 1L;
    /**
     * Construye una excepción con el mensaje especificado.
     *
     * @param mensaje mensaje descriptivo de la excepción.
     */
    public ReservaSolapadaException(String mensaje) {
        super(mensaje);
    }
}
