package model.excepciones;
/**
 * Excepción base del sistema SGA para representar errores específicos
 * de la aplicación.
 *
 * <p>Al heredar de {@link Exception}, es una excepción comprobada
 * que debe declararse o gestionarse mediante bloques de captura.</p>
 */
public class ExcepcionSGA extends Exception {
    /** Identificador de versión para la serialización de la excepción. */
    private static final long serialVersionUID = 1L;
    /**
     * Construye una excepción con un mensaje descriptivo.
     *
     * @param mensaje descripción del error ocurrido.
     */
    public ExcepcionSGA(String mensaje) {
        super(mensaje);
    }
    /**
     * Construye una excepción con un mensaje descriptivo y una causa original.
     *
     * @param mensaje descripción del error ocurrido.
     * @param causa excepción o error que originó esta excepción.
     */
    public ExcepcionSGA(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
