package excepciones;

public class ExcepcionSGA extends Exception {

    private static final long serialVersionUID = 1L;

    public ExcepcionSGA(String mensaje) {
        super(mensaje);
    }

    public ExcepcionSGA(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
