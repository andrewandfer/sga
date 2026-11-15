package model.servicios;

import model.enums.RolEmpleado;
import model.estructuras.ListaCircular;
import model.excepciones.ExcepcionSGA;
import model.modelo.Empleado;

/**
 * Rotacion continua de los turnos de recepcion sobre una lista circular: al llegar
 * al ultimo recepcionista, el turno vuelve al primero.
 */
public class GestorTurnos {

    private final ListaCircular<Empleado> turnos = new ListaCircular<>();

    public void agregarRecepcionista(Empleado empleado) throws ExcepcionSGA {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado es obligatorio");
        }
        if (empleado.getRol() != RolEmpleado.RECEPCIONISTA) {
            throw new ExcepcionSGA("Solo los recepcionistas participan en la rotacion de turnos: " + empleado);
        }
        for (Empleado existente : turnos) {
            if (existente.equals(empleado)) {
                throw new ExcepcionSGA("El recepcionista ya esta en la rotacion: " + empleado);
            }
        }
        turnos.agregar(empleado);
    }

    /** Retira a un recepcionista de la rotacion (por ejemplo, si deja la empresa). */
    public boolean retirar(Empleado empleado) {
        return turnos.eliminar(empleado);
    }

    /** Recepcionista que tiene el turno ahora. Lanza EstructuraVaciaException si no hay ninguno. */
    public Empleado turnoActual() {
        return turnos.actual();
    }

    /** Termina el turno actual y retorna al recepcionista que lo recibe. */
    public Empleado pasarTurno() {
        turnos.siguiente();
        return turnos.actual();
    }

    public int cantidad() {
        return turnos.tamano();
    }

    /** Orden de la rotacion empezando por quien tiene el turno actual. */
    public Iterable<Empleado> getRotacion() {
        return turnos;
    }
}
