
package model.excepciones;

/**
 * Excepción del sistema SGA que representa un intento de realizar
 * una operación sobre una estructura de datos vacía.
 *
 * <p>Permite identificar específicamente los errores relacionados
 * con estructuras sin elementos, como pilas, colas, listas o árboles.</p>
 *
 * <p>Al heredar de {@link ExcepcionSGA}, es una excepción comprobada
 * que debe declararse o gestionarse mediante bloques de captura.</p>
 */
public class EstructuraVaciaException extends ExcepcionSGA {

    /** Identificador de versión para la serialización de la excepción. */
    private static final long serialVersionUID = 1L;

    /**
     * Construye una excepción con un mensaje descriptivo.
     *
     * @param mensaje descripción del error ocurrido
     */
    public EstructuraVaciaException(String mensaje) {
        super(mensaje);
    }

    /**
     * Construye una excepción con un mensaje descriptivo y una causa original.
     *
     * @param mensaje descripción del error ocurrido
     * @param causa excepción o error que originó esta excepción
     */
    public EstructuraVaciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
