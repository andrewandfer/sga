package modelo;


import estructuras.Conjunto;
import estructuras.ListaDoblementeEnlazada;
import java.time.LocalDate;
import java.util.Objects;

public class Huesped implements Comparable<Huesped> {

    private final String documento;
    private String nombre;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String correo;
    private String rutaFotografia;
    private final Conjunto<Apartamento> favoritos = new Conjunto<>();
    private final ListaDoblementeEnlazada<Reserva> historial = new ListaDoblementeEnlazada<>();

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

    private Huesped(String documento) {
        this.documento = documento;
    }

    public static Huesped paraBusqueda(String documento) {
        return new Huesped(documento);
    }

    public boolean agregarFavorito(Apartamento apartamento) {
        return favoritos.agregar(apartamento);
    }

    public boolean quitarFavorito(Apartamento apartamento) {
        return favoritos.eliminar(apartamento);
    }

    public void agregarAlHistorial(Reserva reserva) {
        historial.agregar(reserva);
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRutaFotografia() {
        return rutaFotografia;
    }

    public void setRutaFotografia(String rutaFotografia) {
        this.rutaFotografia = rutaFotografia;
    }

    public Conjunto<Apartamento> getFavoritos() {
        return favoritos;
    }

    public ListaDoblementeEnlazada<Reserva> getHistorial() {
        return historial;
    }

    @Override
    public int compareTo(Huesped otro) {
        return documento.compareTo(otro.documento);
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(documento);
    }

    @Override
    public String toString() {
        return nombre + " (doc. " + documento + ")";
    }
}
