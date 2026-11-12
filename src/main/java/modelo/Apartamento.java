package modelo;
import enums.EstadoApartamento;
import enums.Temporada;
public class Apartamento implements Comparable<Apartamento> {

    private final String codigo;
    private String nombre;
    private String bloque;
    private int piso;
    private int dormitorios;
    private int capacidadMaxima;
    private String caracteristicas;
    private EstadoApartamento estadoOperativo = EstadoApartamento.PREPARADO;
    private String motivoFueraServicio;
    private final ArbolBinarioBusqueda<Reserva> reservas = new ArbolBinarioBusqueda<>();
    private final ListaDoblementeEnlazada<Tarifa> tarifas = new ListaDoblementeEnlazada<>();

    public Apartamento(String codigo, String nombre, String bloque, int piso,
                       int dormitorios, int capacidadMaxima, String caracteristicas) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo es obligatorio");
        }
        if (capacidadMaxima <= 0 || dormitorios <= 0) {
            throw new IllegalArgumentException("Capacidad y dormitorios deben ser positivos");
        }
        this.codigo = codigo;
        this.nombre = nombre;
        this.bloque = bloque;
        this.piso = piso;
        this.dormitorios = dormitorios;
        this.capacidadMaxima = capacidadMaxima;
        this.caracteristicas = caracteristicas;
    }

    private Apartamento(String codigo) {
        this.codigo = codigo;
    }

    /** Objeto "sonda" que solo lleva el codigo, para buscar en el arbol. */
    public static Apartamento paraBusqueda(String codigo) {
        return new Apartamento(codigo);
    }

    // ---------------- Estado operativo ----------------

    /** Cambia el estado. Para FUERA_DE_SERVICIO use {@link #ponerFueraDeServicio(String)}. */
    public void cambiarEstado(EstadoApartamento nuevo) {
        if (nuevo == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        if (nuevo == EstadoApartamento.FUERA_DE_SERVICIO) {
            throw new IllegalArgumentException("Para FUERA_DE_SERVICIO debe indicar el motivo");
        }
        this.estadoOperativo = nuevo;
        this.motivoFueraServicio = null;
    }

    public void ponerFueraDeServicio(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo es obligatorio");
        }
        this.estadoOperativo = EstadoApartamento.FUERA_DE_SERVICIO;
        this.motivoFueraServicio = motivo;
    }

    // ---------------- Tarifas ----------------

    public void definirTarifa(Temporada temporada, double valorPersonaNoche) {
        for (Tarifa t : tarifas) {
            if (t.getTemporada() == temporada) {
                t.setValorPersonaNoche(valorPersonaNoche);
                return;
            }
        }
        tarifas.agregar(new Tarifa(temporada, valorPersonaNoche));
    }

    public boolean tieneTarifa(Temporada temporada) {
        for (Tarifa t : tarifas) {
            if (t.getTemporada() == temporada) {
                return true;
            }
        }
        return false;
    }

    /** Valor por persona por noche en la temporada dada. */
    public double tarifaPara(Temporada temporada) {
        for (Tarifa t : tarifas) {
            if (t.getTemporada() == temporada) {
                return t.getValorPersonaNoche();
            }
        }
        throw new IllegalStateException("El apartamento " + codigo + " no tiene tarifa para la temporada " + temporada);
    }

    // ---------------- Reservas ----------------

    public boolean agregarReserva(Reserva r) {
        return reservas.insertar(r);
    }

    public boolean quitarReserva(Reserva r) {
        return reservas.eliminar(r);
    }

    public int numeroReservas() {
        return reservas.tamano();
    }

    // ---------------- Comparadores alternos ----------------

    /** Por precio (tarifa de temporada baja, la base); desempata por codigo. */
    public static Comparator<Apartamento> porPrecio() {
        return porPrecio(Temporada.BAJA);
    }

    public static Comparator<Apartamento> porPrecio(Temporada temporada) {
        return Comparator.comparingDouble((Apartamento a) -> a.tarifaPara(temporada))
                .thenComparing((x, y) -> x.compareTo(y));
    }

    public static Comparator<Apartamento> porCapacidad() {
        return Comparator.comparingInt((Apartamento a) -> a.capacidadMaxima)
                .thenComparing((x, y) -> x.compareTo(y));
    }

    public static Comparator<Apartamento> porNumeroReservas() {
        return Comparator.comparingInt((Apartamento a) -> a.reservas.tamano())
                .thenComparing((x, y) -> x.compareTo(y));
    }

    public static Comparator<Apartamento> porDormitorios() {
        return Comparator.comparingInt((Apartamento a) -> a.dormitorios)
                .thenComparing((x, y) -> x.compareTo(y));
    }

    // ---------------- Getters / setters ----------------

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getBloque() {
        return bloque;
    }

    public void setBloque(String bloque) {
        this.bloque = bloque;
    }

    public int getPiso() {
        return piso;
    }

    public void setPiso(int piso) {
        this.piso = piso;
    }

    public int getDormitorios() {
        return dormitorios;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public String getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(String caracteristicas) {
        this.caracteristicas = caracteristicas;
    }

    public EstadoApartamento getEstadoOperativo() {
        return estadoOperativo;
    }

    public String getMotivoFueraServicio() {
        return motivoFueraServicio;
    }

    public ArbolBinarioBusqueda<Reserva> getReservas() {
        return reservas;
    }

    public ListaDoblementeEnlazada<Tarifa> getTarifas() {
        return tarifas;
    }

    @Override
    public int compareTo(Apartamento otro) {
        return codigo.compareTo(otro.codigo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Apartamento)) {
            return false;
        }
        return codigo.equals(((Apartamento) o).codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (bloque " + bloque + ", piso " + piso + ", cap. "
                + capacidadMaxima + ", " + estadoOperativo + ")";
    }
}
