package model.modelo;
import model.enums.EstadoApartamento;
import model.enums.Temporada;
import model.estructuras.ArbolBinarioBusqueda;
import model.estructuras.ListaDoblementeEnlazada;

import java.util.Comparator;
import java.util.Objects;
/**
 * Representa un apartamento dentro del sistema.
 * <p>
 * Almacena su información, estado operativo, tarifas por temporada
 * y reservas asociadas. Permite comparar y ordenar apartamentos
 * según diferentes criterios.
 *
 */
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
    /**
     * Construye un apartamento con su información básica.
     *
     * @param codigo identificador único del apartamento
     * @param nombre nombre del apartamento
     * @param bloque bloque al que pertenece
     * @param piso piso en el que se encuentra
     * @param dormitorios número de dormitorios
     * @param capacidadMaxima cantidad máxima de personas que puede alojar
     * @param caracteristicas descripción de sus características
     * @throws IllegalArgumentException si el código es nulo o vacío,
     *                                  o si la capacidad o los dormitorios no son positivos
     */
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
    /**
     * Construye un apartamento utilizando únicamente su código.
     * Se utiliza para realizar búsquedas por identificador.
     *
     * @param codigo identificador del apartamento
     */
    public Apartamento(String codigo) {
        this.codigo = codigo;
    }
    /**
     * Crea un objeto Apartamento destinado a realizar búsquedas por código.
     *
     * @param codigo identificador del apartamento que se desea buscar
     * @return apartamento utilizado como criterio de búsqueda
     */
    public static Apartamento paraBusqueda(String codigo) {
        return new Apartamento(codigo);
    }
    /**
     * Cambia el estado operativo del apartamento.
     * El estado FUERA_DE_SERVICIO debe establecerse mediante
     * {@link #ponerFueraDeServicio(String)}.
     *
     * @param nuevo nuevo estado operativo
     * @throws IllegalArgumentException si el estado es nulo o es FUERA_DE_SERVICIO
     */
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
    /**
     * Establece el apartamento como fuera de servicio e indica el motivo.
     *
     * @param motivo razón por la que el apartamento queda fuera de servicio
     * @throws IllegalArgumentException si el motivo es nulo o está vacío
     */
    public void ponerFueraDeServicio(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo es obligatorio");
        }
        this.estadoOperativo = EstadoApartamento.FUERA_DE_SERVICIO;
        this.motivoFueraServicio = motivo;
    }
    /**
     * Define la tarifa por persona y noche para una temporada.
     * Si ya existe una tarifa para esa temporada, actualiza su valor.
     *
     * @param temporada temporada a la que corresponde la tarifa
     * @param valorPersonaNoche valor por persona y noche
     */
    public void definirTarifa(Temporada temporada, double valorPersonaNoche) {
        for (Tarifa t : tarifas) {
            if (t.getTemporada() == temporada) {
                t.setValorPersonaNoche(valorPersonaNoche);
                return;
            }
        }
        tarifas.agregar(new Tarifa(temporada, valorPersonaNoche));
    }
    /**
     * Comprueba si el apartamento tiene una tarifa definida para una temporada.
     *
     * @param temporada temporada que se desea consultar
     * @return {@code true} si existe una tarifa para esa temporada
     */
    public boolean tieneTarifa(Temporada temporada) {
        for (Tarifa t : tarifas) {
            if (t.getTemporada() == temporada) {
                return true;
            }
        }
        return false;
    }
    /**
     * Obtiene el valor por persona y noche correspondiente a una temporada.
     *
     * @param temporada temporada de la tarifa
     * @return valor por persona y noche
     * @throws IllegalStateException si el apartamento no tiene una tarifa
     *                                definida para la temporada indicada
     */
    public double tarifaPara(Temporada temporada) {
        for (Tarifa t : tarifas) {
            if (t.getTemporada() == temporada) {
                return t.getValorPersonaNoche();
            }
        }
        throw new IllegalStateException("El apartamento " + codigo + " no tiene tarifa para la temporada " + temporada);
    }

    /**
     * Agrega una reserva al árbol de reservas del apartamento.
     *
     * @param r reserva que se desea agregar
     * @return {@code true} si la reserva fue insertada
     */
    public boolean agregarReserva(Reserva r) {
        return reservas.insertar(r);
    }
    /**
     * Elimina una reserva del árbol de reservas del apartamento.
     *
     * @param r reserva que se desea eliminar
     * @return {@code true} si la reserva fue encontrada y eliminada
     */
    public boolean quitarReserva(Reserva r) {
        return reservas.eliminar(r);
    }
    /**
     * Obtiene la cantidad de reservas asociadas al apartamento.
     *
     * @return número de reservas
     */
    public int numeroReservas() {
        return reservas.tamano();
    }
    /**
     * Crea un comparador que ordena los apartamentos por su tarifa
     * de temporada baja, utilizando el código como desempate.
     *
     * @return comparador por precio de temporada baja
     */
    public static Comparator<Apartamento> porPrecio() {
        return porPrecio(Temporada.BAJA);
    }
    /**
     * Crea un comparador que ordena los apartamentos por tarifa
     * de una temporada específica, utilizando el código como desempate.
     *
     * @param temporada temporada cuyo precio se utilizará para ordenar
     * @return comparador por tarifa de la temporada indicada
     * @throws IllegalStateException si algún apartamento no tiene tarifa para esa temporada
     */
    public static Comparator<Apartamento> porPrecio(Temporada temporada) {
        return Comparator.comparingDouble((Apartamento a) -> a.tarifaPara(temporada))
                .thenComparing((x, y) -> x.compareTo(y));
    }
    /**
     * Crea un comparador que ordena los apartamentos por capacidad máxima
     * ascendente, utilizando el código como desempate.
     *
     * @return comparador por capacidad máxima
     */
    public static Comparator<Apartamento> porCapacidad() {
        return Comparator.comparingInt((Apartamento a) -> a.capacidadMaxima)
                .thenComparing((x, y) -> x.compareTo(y));
    }
    /**
     * Crea un comparador que ordena los apartamentos por número de reservas
     * ascendente, utilizando el código como desempate.
     *
     * @return comparador por número de reservas
     */
    public static Comparator<Apartamento> porNumeroReservas() {
        return Comparator.comparingInt((Apartamento a) -> a.reservas.tamano())
                .thenComparing((x, y) -> x.compareTo(y));
    }
    /**
     * Crea un comparador que ordena los apartamentos por número de dormitorios
     * ascendente, utilizando el código como desempate.
     *
     * @return comparador por número de dormitorios
     */
    public static Comparator<Apartamento> porDormitorios() {
        return Comparator.comparingInt((Apartamento a) -> a.dormitorios)
                .thenComparing((x, y) -> x.compareTo(y));
    }

    /**
     * Obtiene el código del apartamento.
     *
     * @return código del apartamento
     */
    public String getCodigo() {
        return codigo;
    }
    /**
     * Obtiene el nombre del apartamento.
     *
     * @return nombre del apartamento
     */
    public String getNombre() {
        return nombre;
    }
    /**
     * Establece el nombre del apartamento.
     *
     * @param nombre nuevo nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    /**
     * Obtiene el bloque al que pertenece el apartamento.
     *
     * @return bloque del apartamento
     */
    public String getBloque() {
        return bloque;
    }
    /**
     * Establece el bloque del apartamento.
     *
     * @param bloque nuevo bloque
     */
    public void setBloque(String bloque) {
        this.bloque = bloque;
    }
    /**
     * Obtiene el piso en el que se encuentra el apartamento.
     *
     * @return piso del apartamento
     */
    public int getPiso() {
        return piso;
    }
    /**
     * Establece el piso del apartamento.
     *
     * @param piso nuevo piso
     */
    public void setPiso(int piso) {
        this.piso = piso;
    }

    /**
     * Obtiene el número de dormitorios del apartamento.
     *
     * @return número de dormitorios
     */
    public int getDormitorios() {
        return dormitorios;
    }
    /**
     * Obtiene la capacidad máxima del apartamento.
     *
     * @return cantidad máxima de personas que puede alojar
     */
    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }
    /**
     * Obtiene las características del apartamento.
     *
     * @return descripción de las características
     */
    public String getCaracteristicas() {
        return caracteristicas;
    }
    /**
     * Establece las características del apartamento.
     *
     * @param caracteristicas nueva descripción de las características
     */
    public void setCaracteristicas(String caracteristicas) {
        this.caracteristicas = caracteristicas;
    }
    /**
     * Obtiene el estado operativo del apartamento.
     *
     * @return estado operativo actual
     */
    public EstadoApartamento getEstadoOperativo() {
        return estadoOperativo;
    }

    /**
     * Obtiene el motivo por el que el apartamento está fuera de servicio.
     *
     * @return motivo de fuera de servicio, o {@code null} si no está fuera de servicio
     */
    public String getMotivoFueraServicio() {
        return motivoFueraServicio;
    }
    /**
     * Obtiene el árbol de reservas del apartamento.
     *
     * @return árbol binario de búsqueda con las reservas
     */
    public ArbolBinarioBusqueda<Reserva> getReservas() {
        return reservas;
    }
    /**
     * Obtiene la lista de tarifas del apartamento.
     *
     * @return lista doblemente enlazada con las tarifas
     */
    public ListaDoblementeEnlazada<Tarifa> getTarifas() {
        return tarifas;
    }
    /**
     * Compara este apartamento con otro según su código.
     *
     * @param otro apartamento con el que se compara
     * @return valor negativo, cero o positivo según el orden de los códigos
     */
    @Override
    public int compareTo(Apartamento otro) {
        return codigo.compareTo(otro.codigo);
    }
    /**
     * Comprueba si otro objeto representa el mismo apartamento.
     * La comparación se realiza mediante el código.
     *
     * @param o objeto que se desea comparar
     * @return {@code true} si ambos objetos tienen el mismo código
     */
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
    /**
     * Genera el código hash del apartamento a partir de su código.
     *
     * @return código hash del apartamento
     */
    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }
    /**
     * Devuelve una representación textual del apartamento con sus
     * principales datos y estado operativo.
     *
     * @return descripción del apartamento
     */
    @Override
    public String toString() {
        return codigo + " - " + nombre + " (bloque " + bloque + ", piso " + piso + ", cap. "
                + capacidadMaxima + ", " + estadoOperativo + ")";
    }
}
