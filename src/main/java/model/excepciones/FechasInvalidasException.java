package model.excepciones;

/**
 * Excepción que indica que se ha proporcionado una fecha o un conjunto
 * de fechas que no cumplen las condiciones de validez establecidas
 * por el sistema.
 *
 * <p>Hereda de {@link ExcepcionSGA} y permite identificar errores
 * relacionados específicamente con el manejo de fechas.</p>
 */
public class FechasInvalidasException extends ExcepcionSGA {
    /** Identificador de versión para la serialización de la excepción. */
    private static final long serialVersionUID = 1L;
    /**
     * Construye una excepción con un mensaje descriptivo del error
     * relacionado con las fechas.
     *
     * @param mensaje descripción del problema encontrado.
     */
    public FechasInvalidasException(String mensaje) {
        super(mensaje);
    }
}
