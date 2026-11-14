package servicios;

import enums.CanalReserva;
import enums.EstadoApartamento;
import enums.EstadoReserva;
import estructuras.ArbolBinarioBusqueda;
import estructuras.ListaDoblementeEnlazada;
import excepciones.CapacidadExcedidaException;
import excepciones.ReservaSolapadaException;
import modelo.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Crea, confirma y cancela reservas garantizando la regla fundamental: un apartamento
 * no puede tener dos reservas que compartan al menos una noche, sin importar el canal.
 *
 * El arbol de reservas de cada apartamento solo conserva las reservas que reclaman
 * noches (todas menos CANCELADA y NO_SHOW), de modo que entre ellas nunca hay solapamiento.
 */
public class GestorReservas {

    public static final double PORCENTAJE_ANTICIPO = 0.30;
    public static final int DIAS_DEVOLUCION_TOTAL = 7;
    public static final int DIAS_DEVOLUCION_PARCIAL = 2;
    public static final double PORCENTAJE_DEVOLUCION_PARCIAL = 0.50;
    private final CotizadorService cotizador;
    private final AsignadorGrupos asignador;
    private int consecutivo = 0;

    public GestorReservas(CotizadorService cotizador, AsignadorGrupos asignador) {
        this.cotizador = cotizador;
        this.asignador = asignador;
    }


    /**
     * Crea una reserva. Portal queda PENDIENTE
     */
    public Reserva crearReserva(Huesped titular, Apartamento apartamento, Estancia estancia,
                                ListaDoblementeEnlazada<Ocupante> ocupantes, CanalReserva canal)
            throws ReservaSolapadaException, CapacidadExcedidaException {
        return crearReserva(titular, apartamento, estancia, ocupantes, canal, LocalDateTime.now());
    }

    public Reserva crearReserva(Huesped titular, Apartamento apartamento, Estancia estancia,
                                ListaDoblementeEnlazada<Ocupante> ocupantes, CanalReserva canal,
                                LocalDateTime creadaEn)
            throws ReservaSolapadaException, CapacidadExcedidaException {
        if (titular == null || apartamento == null || estancia == null || ocupantes == null || canal == null) {
            throw new IllegalArgumentException("Titular, apartamento, estancia, ocupantes y canal son obligatorios");
        }
        if (ocupantes.estaVacia()) {
            throw new IllegalArgumentException("La reserva debe tener al menos un ocupante");
        }
        if (apartamento.getEstadoOperativo() == EstadoApartamento.FUERA_DE_SERVICIO) {
            throw new IllegalStateException("El apartamento " + apartamento.getCodigo() + " esta fuera de servicio");
        }
        if (ocupantes.tamano() > apartamento.getCapacidadMaxima()) {
            throw new CapacidadExcedidaException("El apartamento " + apartamento.getCodigo() + " admite maximo "
                    + apartamento.getCapacidadMaxima() + " personas y se indicaron " + ocupantes.tamano());
        }
        verificarSolapamiento(apartamento, estancia);

        consecutivo++;
        String id = String.format("RES-%04d", consecutivo);
        Reserva reserva = new Reserva(id, canal, titular, apartamento, estancia, ocupantes, creadaEn);
        if (canal != CanalReserva.PORTAL) {
            reserva.confirmar();
        }
        apartamento.agregarReserva(reserva);
        titular.agregarAlHistorial(reserva);
        return reserva;
    }

    /**
     * Reserva un grupo que no cabe en un solo apartamento: busca con backtracking la
     * combinacion de apartamentos libres con el menor numero de unidades y genera una
     * reserva por apartamento repartiendo los ocupantes.
     */
    public ListaDoblementeEnlazada<Reserva> reservarGrupoGrande(Huesped titular, Estancia estancia,
                                                                ListaDoblementeEnlazada<Ocupante> grupo, ArbolBinarioBusqueda<Apartamento> catalogo,
                                                                CanalReserva canal) throws ReservaSolapadaException, CapacidadExcedidaException {
        ListaDoblementeEnlazada<Apartamento> combinacion = asignarGrupo(grupo.tamano(), estancia, catalogo);
        ListaDoblementeEnlazada<Reserva> reservas = new ListaDoblementeEnlazada<>();
        Iterator<Ocupante> iterador = grupo.iterator();
        for (Apartamento apartamento : combinacion) {
            ListaDoblementeEnlazada<Ocupante> parte = new ListaDoblementeEnlazada<>();
            while (iterador.hasNext() && parte.tamano() < apartamento.getCapacidadMaxima()) {
                parte.agregar(iterador.next());
            }
            reservas.agregar(crearReserva(titular, apartamento, estancia, parte, canal));
        }
        return reservas;
    }

    /** Solo calcula la combinacion de apartamentos para el grupo (sin reservar). */
    public ListaDoblementeEnlazada<Apartamento> asignarGrupo(int personas, Estancia estancia,
                                                             ArbolBinarioBusqueda<Apartamento> catalogo) throws CapacidadExcedidaException {
        return asignador.asignar(personas, apartamentosLibres(catalogo, estancia));
    }


    /** Lanza ReservaSolapadaException si la estancia choca con una reserva vigente del apartamento. */
    public void verificarSolapamiento(Apartamento apartamento, Estancia estancia) throws ReservaSolapadaException {
        Reserva conflicto = buscarSolapamiento(apartamento.getReservas().getRaiz(), estancia);
        if (conflicto != null) {
            throw new ReservaSolapadaException("El apartamento " + apartamento.getCodigo()
                    + " ya tiene la reserva " + conflicto.getId() + " (" + conflicto.getEstancia()
                    + ") que comparte noches con " + estancia);
        }
    }

    public boolean estaLibre(Apartamento apartamento, Estancia estancia) {
        return buscarSolapamiento(apartamento.getReservas().getRaiz(), estancia) == null;
    }

    /**
     * Recorre recursivamente el arbol (ordenado por fecha de entrada) y retorna la primera
     *
     */
    private Reserva buscarSolapamiento(ArbolBinarioBusqueda.NodoArbol<Reserva> nodo, Estancia buscada) {
        if (nodo == null) {
            return null;
        }
        Reserva reserva = nodo.getDato();
        Estancia estancia = reserva.getEstancia();
        if (reserva.ocupaNoches() && estancia.seSolapaCon(buscada)) {
            return reserva;
        }
        if (estancia.getEntrada().isAfter(buscada.getEntrada())) {
            Reserva enIzquierda = buscarSolapamiento(nodo.getIzquierdo(), buscada);
            if (enIzquierda != null) {
                return enIzquierda;
            }
        }
        if (estancia.getEntrada().isBefore(buscada.getSalida())) {
            return buscarSolapamiento(nodo.getDerecho(), buscada);
        }
        return null;
    }


    /** Apartamentos (de cualquier capacidad) que estan en servicio y libres en la estancia. */
    public ListaDoblementeEnlazada<Apartamento> apartamentosLibres(ArbolBinarioBusqueda<Apartamento> catalogo,
                                                                   Estancia estancia) {
        ListaDoblementeEnlazada<Apartamento> libres = new ListaDoblementeEnlazada<>();
        for (Apartamento a : catalogo) {
            if (a.getEstadoOperativo() != EstadoApartamento.FUERA_DE_SERVICIO && estaLibre(a, estancia)) {
                libres.agregar(a);
            }
        }
        return libres;
    }

    /** Apartamentos libres con capacidad suficiente, ordenados con el criterio elegido. */
    public ListaDoblementeEnlazada<Apartamento> buscarDisponibles(ArbolBinarioBusqueda<Apartamento> catalogo,
                                                                  Estancia estancia, int personas, Comparator<Apartamento> orden) {
        ListaDoblementeEnlazada<Apartamento> resultado = new ListaDoblementeEnlazada<>();
        for (Apartamento a : apartamentosLibres(catalogo, estancia)) {
            if (a.getCapacidadMaxima() >= personas) {
                resultado.insertarOrdenado(a, orden);
            }
        }
        return resultado;
    }


    public void confirmar(Reserva reserva) {
        reserva.confirmar();
    }

    /** Muestra cuanto se devolveria si se cancelara hoy, sin cancelar. */
    public double calcularDevolucion(Reserva reserva, double montoPagado) {
        return calcularDevolucion(reserva, montoPagado, LocalDate.now());
    }

    public double calcularDevolucion(Reserva reserva, double montoPagado, LocalDate hoy) {
        if (reserva.getEstado() != EstadoReserva.PENDIENTE && reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("La reserva " + reserva.getId() + " no se puede cancelar en estado "
                    + reserva.getEstado());
        }
        long dias = ChronoUnit.DAYS.between(hoy, reserva.getEstancia().getEntrada());
        if (dias >= DIAS_DEVOLUCION_TOTAL) {
            return montoPagado;
        }
        if (dias >= DIAS_DEVOLUCION_PARCIAL) {
            return montoPagado * PORCENTAJE_DEVOLUCION_PARCIAL;
        }
        return 0;
    }

    /** Cancela la reserva, libera sus noches y retorna el valor a devolver. */
    public double cancelar(Reserva reserva, double montoPagado) {
        return cancelar(reserva, montoPagado, LocalDate.now());
    }

    public double cancelar(Reserva reserva, double montoPagado, LocalDate hoy) {
        double devolucion = calcularDevolucion(reserva, montoPagado, hoy);
        reserva.cancelar();
        reserva.getApartamento().quitarReserva(reserva);
        return devolucion;
    }

    /** El huesped no llego: la reserva pasa a NO_SHOW y libera sus noches. */
    public void marcarNoShow(Reserva reserva) {
        reserva.marcarNoShow();
        reserva.getApartamento().quitarReserva(reserva);
    }


    public Cotizacion cotizarReserva(Reserva reserva) throws CapacidadExcedidaException {
        return cotizador.cotizar(reserva.getApartamento(), reserva.getEstancia(), reserva.getOcupantes());
    }

    /** Anticipo del 30 % que debe pagar el huesped en el canal Portal. */
    public double calcularAnticipo(Reserva reserva) throws CapacidadExcedidaException {
        return cotizarReserva(reserva).getTotal() * PORCENTAJE_ANTICIPO;
    }
}
