package model.modelo;


import model.estructuras.Conjunto;
import model.estructuras.ListaDoblementeEnlazada;
import java.time.LocalDate;
import java.util.Objects;
/**
 * Representa a un huésped registrado en el sistema.
 * Almacena sus datos personales, apartamentos favoritos
 * e historial de reservas.
 */
public class Huesped implements Comparable<Huesped> {

    private final String documento;
    private String nombre;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String correo;
    private String rutaFotografia;
    private final Conjunto<Apartamento> favoritos = new Conjunto<>();
    private final ListaDoblementeEnlazada<Reserva> historial = new ListaDoblementeEnlazada<>();
    /**
     * Construye un huésped con sus datos personales.
     *
     * @param documento número de documento del huésped
     * @param nombre nombre del huésped
     * @param fechaNacimiento fecha de nacimiento
     * @param telefono número de teléfono
     * @param correo dirección de correo electrónico
     * @throws IllegalArgumentException si el documento o el nombre son nulos o están vacíos
     */
    public Huesped(String documento, String nombre, LocalDate fechaNacimiento, String telefono, String correo) {
        if (documento == null || documento.isBlank() || nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Documento y nombre son obligatorios");
        }
        this.documento = documento;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.correo = correo;
    }
    /**
     * Construye un huésped utilizando únicamente su documento.
     * Se utiliza para realizar búsquedas por identificador.
     *
     * @param documento documento del huésped que se desea buscar
     */
    private Huesped(String documento) {
        this.documento = documento;
    }
    /**
     * Crea un objeto Huesped destinado a realizar búsquedas por documento.
     *
     * @param documento documento del huésped que se desea buscar
     * @return huésped utilizado como criterio de búsqueda
     */
    public static Huesped paraBusqueda(String documento) {
        return new Huesped(documento);
    }
    /**
     * Agrega un apartamento a la lista de favoritos del huésped.
     *
     * @param apartamento apartamento que se desea agregar
     * @return {@code true} si el apartamento fue agregado
     */
    public boolean agregarFavorito(Apartamento apartamento) {
        return favoritos.agregar(apartamento);
    }
    /**
     * Elimina un apartamento de la lista de favoritos del huésped.
     *
     * @param apartamento apartamento que se desea eliminar
     * @return {@code true} si el apartamento fue encontrado y eliminado
     */
    public boolean quitarFavorito(Apartamento apartamento) {
        return favoritos.eliminar(apartamento);
    }
    /**
     * Agrega una reserva al historial del huésped.
     *
     * @param reserva reserva que se desea registrar
     */
    public void agregarAlHistorial(Reserva reserva) {
        historial.agregar(reserva);
    }
    /**
     * Obtiene el documento del huésped.
     *
     * @return documento del huésped
     */
    public String getDocumento() {
        return documento;
    }
    /**
     * Obtiene el nombre del huésped.
     *
     * @return nombre del huésped
     */
    public String getNombre() {
        return nombre;
    }
    /**
     * Establece el nombre del huésped.
     *
     * @param nombre nuevo nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene la fecha de nacimiento del huésped.
     *
     * @return fecha de nacimiento
     */
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
    /**
     * Establece la fecha de nacimiento del huésped.
     *
     * @param fechaNacimiento nueva fecha de nacimiento
     */
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    /**
     * Obtiene el número de teléfono del huésped.
     *
     * @return número de teléfono
     */
    public String getTelefono() {
        return telefono;
    }
    /**
     * Establece el número de teléfono del huésped.
     *
     * @param telefono nuevo número de teléfono
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    /**
     * Obtiene el correo electrónico del huésped.
     *
     * @return dirección de correo electrónico
     */
    public String getCorreo() {
        return correo;
    }
    /**
     * Establece el correo electrónico del huésped.
     *
     * @param correo nueva dirección de correo electrónico
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }
    /**
     * Obtiene la ruta de la fotografía del huésped.
     *
     * @return ruta de la fotografía
     */
    public String getRutaFotografia() {
        return rutaFotografia;
    }
    /**
     * Establece la ruta de la fotografía del huésped.
     *
     * @param rutaFotografia nueva ruta de la fotografía
     */
    public void setRutaFotografia(String rutaFotografia) {
        this.rutaFotografia = rutaFotografia;
    }
    /**
     * Obtiene el conjunto de apartamentos favoritos del huésped.
     *
     * @return conjunto de apartamentos favoritos
     */
    public Conjunto<Apartamento> getFavoritos() {
        return favoritos;
    }
    /**
     * Obtiene el historial de reservas del huésped.
     *
     * @return lista de reservas del huésped
     */
    public ListaDoblementeEnlazada<Reserva> getHistorial() {
        return historial;
    }
    /**
     * Compara este huésped con otro según su documento.
     *
     * @param otro huésped con el que se compara
     * @return valor negativo, cero o positivo según el orden de los documentos
     */
    @Override
    public int compareTo(Huesped otro) {
        return documento.compareTo(otro.documento);
    }
    /**
     * Comprueba si otro objeto representa al mismo huésped.
     * La comparación se realiza mediante el documento.
     *
     * @param o objeto que se desea comparar
     * @return {@code true} si ambos objetos tienen el mismo documento
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Huesped)) {
            return false;
        }
        return documento.equals(((Huesped) o).documento);
    }
    /**
     * Genera el código hash del huésped a partir de su documento.
     *
     * @return código hash del huésped
     */
    @Override
    public int hashCode() {
        return Objects.hash(documento);
    }
    /**
     * Devuelve una representación textual del huésped con su nombre
     * y documento.
     *
     * @return descripción del huésped
     */
    @Override
    public String toString() {
        return nombre + " (doc. " + documento + ")";
    }
}
