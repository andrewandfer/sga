package model.modelo;


import model.estructuras.Conjunto;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.enums.RolUsuario;
import model.interfaces.Usuario;
import java.time.LocalDate;
import java.util.Objects;

public class Huesped implements Comparable<Huesped>, Usuario {

    private final String documento;
    private String nombre;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String correo;
    private String rutaFotografia;
    private String contrasena;
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

    public Huesped(String documento, String nombre, LocalDate fechaNacimiento, String telefono,
                   String correo, String contrasena) {
        this(documento, nombre, fechaNacimiento, telefono, correo);
        cambiarContrasena(contrasena);
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

    /** Solo para deshacer una reserva que no llego a completarse (rollback de grupos). */
    public boolean quitarDelHistorial(Reserva reserva) {
        return historial.eliminar(reserva);
    }

    @Override
    public String getDocumento() {
        return documento;
    }

    @Override
    public void cambiarContrasena(String contrasena) {
        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException("La contrasena es obligatoria");
        }
        this.contrasena = contrasena;
    }

    @Override
    public boolean validarContrasena(String contrasena) {
        return this.contrasena != null && this.contrasena.equals(contrasena);
    }

    @Override
    public boolean tieneContrasena() {
        return contrasena != null && !contrasena.isBlank();
    }

    @Override
    public RolUsuario getRolUsuario() {
        return RolUsuario.HUESPED;
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
