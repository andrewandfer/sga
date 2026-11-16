package model.servicios;

import model.enums.RolEmpleado;
import model.enums.Temporada;
import model.estructuras.ArbolBinarioBusqueda;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.excepciones.ExcepcionSGA;
import model.modelo.Apartamento;
import model.modelo.Empleado;
import model.modelo.Huesped;
import model.modelo.Reserva;

import java.time.LocalDate;

/**
 * Gestion jerarquica del alojamiento: altas, consultas, ediciones y bajas de
 * apartamentos, huespedes y empleados, apoyada en arboles binarios de busqueda
 * (apartamentos por codigo, huespedes y empleados por documento).
 */
public class GestorAdministracion {

    private final ArbolBinarioBusqueda<Apartamento> apartamentos;
    private final ArbolBinarioBusqueda<Huesped> huespedes;
    private final ArbolBinarioBusqueda<Empleado> empleados = new ArbolBinarioBusqueda<>();
    private final GestorTurnos turnos;

    public GestorAdministracion(ArbolBinarioBusqueda<Apartamento> apartamentos,
                                ArbolBinarioBusqueda<Huesped> huespedes, GestorTurnos turnos) {
        this.apartamentos = apartamentos;
        this.huespedes = huespedes;
        this.turnos = turnos;
    }


    public void registrarApartamento(Apartamento apartamento) throws ExcepcionSGA {
        if (apartamento == null) {
            throw new IllegalArgumentException("El apartamento es obligatorio");
        }
        for (Temporada t : Temporada.values()) {
            if (!apartamento.tieneTarifa(t)) {
                throw new ExcepcionSGA("El apartamento " + apartamento.getCodigo()
                        + " debe tener tarifa para la temporada " + t);
            }
        }
        if (!apartamentos.insertar(apartamento)) {
            throw new ExcepcionSGA("Ya existe un apartamento con el codigo " + apartamento.getCodigo());
        }
    }

    public Apartamento buscarApartamento(String codigo) {
        return apartamentos.buscar(Apartamento.paraBusqueda(codigo));
    }

    public void editarApartamento(String codigo, String nombre, String bloque, int piso, String caracteristicas)
            throws ExcepcionSGA {
        Apartamento a = exigirApartamento(codigo);
        a.setNombre(nombre);
        a.setBloque(bloque);
        a.setPiso(piso);
        a.setCaracteristicas(caracteristicas);
    }

    public void ponerApartamentoFueraDeServicio(String codigo, String motivo) throws ExcepcionSGA {
        exigirApartamento(codigo).ponerFueraDeServicio(motivo);
    }

    /** No permite eliminar un apartamento que tenga reservas pendientes, confirmadas o en curso. */
    public void eliminarApartamento(String codigo) throws ExcepcionSGA {
        Apartamento a = exigirApartamento(codigo);
        for (Reserva r : a.getReservas()) {
            if (r.esActiva()) {
                throw new ExcepcionSGA("No se puede eliminar " + codigo + ": tiene la reserva activa " + r.getId());
            }
        }
        apartamentos.eliminar(a);
    }

    public ListaDoblementeEnlazada<Apartamento> listarApartamentos() {
        return apartamentos.inOrden();
    }

    private Apartamento exigirApartamento(String codigo) throws ExcepcionSGA {
        Apartamento a = buscarApartamento(codigo);
        if (a == null) {
            throw new ExcepcionSGA("Apartamento inexistente: " + codigo);
        }
        return a;
    }

    // ==================== Huespedes ====================

    public Huesped registrarHuesped(Huesped huesped) throws ExcepcionSGA {
        if (huesped == null) {
            throw new IllegalArgumentException("El huesped es obligatorio");
        }
        if (!huespedes.insertar(huesped)) {
            throw new ExcepcionSGA("Ya existe un huesped con el documento " + huesped.getDocumento());
        }
        return huesped;
    }

    public Huesped buscarHuesped(String documento) {
        return huespedes.buscar(Huesped.paraBusqueda(documento));
    }

    public void actualizarHuesped(String documento, String nombre, LocalDate nacimiento, String telefono,
                                  String correo) throws ExcepcionSGA {
        Huesped h = exigirHuesped(documento);
        if (nombre == null || nombre.isBlank()) {
            throw new ExcepcionSGA("El nombre del huesped es obligatorio");
        }
        h.setNombre(nombre);
        h.setFechaNacimiento(nacimiento);
        h.setTelefono(telefono);
        h.setCorreo(correo);
    }

    /** No permite eliminar un huesped con reservas pendientes, confirmadas o en curso. */
    public void eliminarHuesped(String documento) throws ExcepcionSGA {
        Huesped h = exigirHuesped(documento);
        for (Reserva r : h.getHistorial()) {
            if (r.esActiva()) {
                throw new ExcepcionSGA("No se puede eliminar al huesped " + documento
                        + ": tiene la reserva activa " + r.getId());
            }
        }
        huespedes.eliminar(h);
    }

    public ListaDoblementeEnlazada<Huesped> listarHuespedes() {
        return huespedes.inOrden();
    }

    private Huesped exigirHuesped(String documento) throws ExcepcionSGA {
        Huesped h = buscarHuesped(documento);
        if (h == null) {
            throw new ExcepcionSGA("Huesped inexistente: " + documento);
        }
        return h;
    }


    /** Registra al empleado; si es recepcionista tambien entra a la rotacion de turnos. */
    public void registrarEmpleado(Empleado empleado) throws ExcepcionSGA {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado es obligatorio");
        }
        if (!empleados.insertar(empleado)) {
            throw new ExcepcionSGA("Ya existe un empleado con el documento " + empleado.getDocumento());
        }
        if (empleado.getRol() == RolEmpleado.RECEPCIONISTA) {
            turnos.agregarRecepcionista(empleado);
        }
    }

    public Empleado buscarEmpleado(String documento) {
        return empleados.buscar(Empleado.paraBusqueda(documento));
    }

    /** Elimina al empleado y, si estaba en la rotacion de recepcion, lo retira de ella. */
    public void eliminarEmpleado(String documento) throws ExcepcionSGA {
        Empleado e = buscarEmpleado(documento);
        if (e == null) {
            throw new ExcepcionSGA("Empleado inexistente: " + documento);
        }
        turnos.retirar(e);
        empleados.eliminar(e);
    }

    public ListaDoblementeEnlazada<Empleado> listarEmpleados() {
        return empleados.inOrden();
    }

    public ListaDoblementeEnlazada<Empleado> listarEmpleadosPorRol(RolEmpleado rol) {
        ListaDoblementeEnlazada<Empleado> resultado = new ListaDoblementeEnlazada<>();
        for (Empleado e : empleados) {
            if (e.getRol() == rol) {
                resultado.agregar(e);
            }
        }
        return resultado;
    }

    public GestorTurnos getTurnos() {
        return turnos;
    }
}
