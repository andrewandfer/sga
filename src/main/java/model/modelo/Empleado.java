package model.modelo;

import model.enums.RolUsuario;
import model.interfaces.Usuario;

import java.util.Objects;

/** Empleado del alojamiento. Su orden natural (Comparable) es por documento. */
public class Empleado implements Comparable<Empleado>, Usuario {

    private final String documento;
    private String nombre;
    private RolUsuario rol;
    private String telefono;
    private String contrasena;

    public Empleado(String documento, String nombre, RolUsuario rol, String telefono) {
        if (documento == null || documento.isBlank() || nombre == null || nombre.isBlank() || rol == null || rol == RolUsuario.HUESPED) {
            throw new IllegalArgumentException("Documento, nombre y rol son obligatorios");
        }
        this.documento = documento;
        this.nombre = nombre;
        this.rol = rol;
        this.telefono = telefono;
    }

    public Empleado(String documento, String nombre, RolUsuario rol, String telefono, String contrasena) {
        this(documento, nombre, rol, telefono);
        cambiarContrasena(contrasena);
    }

    private Empleado(String documento) {
        this.documento = documento;
    }

    /** Empleado "vacio" que solo sirve como clave para buscar en el arbol por documento. */
    public static Empleado paraBusqueda(String documento) {
        return new Empleado(documento);
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
        return rol == RolUsuario.ADMINISTRADOR ? RolUsuario.ADMINISTRADOR : RolUsuario.RECEPCIONISTA;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        this.nombre = nombre;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        if (rol == null || rol == RolUsuario.HUESPED) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        this.rol = rol;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public int compareTo(Empleado otro) {
        return documento.compareTo(otro.documento);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Empleado)) {
            return false;
        }
        return documento.equals(((Empleado) o).documento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documento);
    }

    @Override
    public String toString() {
        return nombre + " (doc. " + documento + ", " + rol + ")";
    }
}
