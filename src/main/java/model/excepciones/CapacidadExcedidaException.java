package model.excepciones;
/**
 * Excepción que indica que se ha intentado superar la capacidad máxima
 * permitida por algún recurso o estructura del sistema.
 *
 * <p>Hereda de {@link ExcepcionSGA}, por lo que forma parte de las
 * excepciones específicas del sistema.</p>
 */
public class CapacidadExcedidaException extends ExcepcionSGA {
    /** Identificador de versión para la serialización de la excepción. */
    private static final long serialVersionUID = 1L;
    /**
     * Construye una excepción con el mensaje descriptivo proporcionado.
     *
     * @param mensaje descripción de la causa de la excepción.
     */
    public CapacidadExcedidaException(String mensaje) {
        super(mensaje);
    }
}