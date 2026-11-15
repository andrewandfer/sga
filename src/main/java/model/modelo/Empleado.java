package model.modelo;

import model.enums.RolEmpleado;

import java.util.Objects;

/** Empleado del alojamiento. Su orden natural (Comparable) es por documento. */
public class Empleado implements Comparable<Empleado> {

    private final String documento;
    private String nombre;
    private RolEmpleado rol;
    private String telefono;

    public Empleado(String documento, String nombre, RolEmpleado rol, String telefono) {
        if (documento == null || documento.isBlank() || nombre == null || nombre.isBlank() || rol == null) {
            throw new IllegalArgumentException("Documento, nombre y rol son obligatorios");
        }
        this.documento = documento;
        this.nombre = nombre;
        this.rol = rol;
        this.telefono = telefono;
    }

    private Empleado(String documento) {
        this.documento = documento;
    }

    /** Empleado "vacio" que solo sirve como clave para buscar en el arbol por documento. */
    public static Empleado paraBusqueda(String documento) {
        return new Empleado(documento);
    }

    public String getDocumento() {
        return documento;
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

    public RolEmpleado getRol() {
        return rol;
    }

    public void setRol(RolEmpleado rol) {
        if (rol == null) {
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
