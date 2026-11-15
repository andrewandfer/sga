package model;

import model.enums.CanalReserva;
import model.enums.CriterioOrden;
import model.enums.RolEmpleado;
import model.enums.Temporada;
import model.estructuras.ArbolBinarioBusqueda;
import model.estructuras.Conjunto;
import model.estructuras.ListaDoblementeEnlazada;
import model.excepciones.CapacidadExcedidaException;
import model.excepciones.ExcepcionSGA;
import model.excepciones.FechasInvalidasException;
import model.excepciones.ReservaSolapadaException;
import model.modelo.*;
import model.servicios.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;

public class ApartaSuites {
    private final ArbolBinarioBusqueda<Apartamento> apartamentos;
    private final ArbolBinarioBusqueda<Huesped> huespedes;

    private final GestorTemporadas temporadas;
    private final CotizadorService cotizador;
    private final AsignadorGrupos asignador;
    private final GestorReservas reservas;
    private final GestorTurnos turnos;
    private final GestorAdministracion administracion;
    private final CargadorDatos cargador;

    /** Crea el sistema vacio con todos sus servicios ya conectados. */
    public ApartaSuites() {
        this.apartamentos = new ArbolBinarioBusqueda<>();
        this.huespedes = new ArbolBinarioBusqueda<>();
        this.temporadas = new GestorTemporadas();
        this.cotizador = new CotizadorService(temporadas);
        this.asignador = new AsignadorGrupos();
        this.reservas = new GestorReservas(cotizador, asignador);
        this.turnos = new GestorTurnos();
        this.administracion = new GestorAdministracion(apartamentos, huespedes, turnos);
        this.cargador = new CargadorDatos(apartamentos, huespedes, reservas, temporadas);
    }

    // ==================== Apartamentos ====================

    public void registrarApartamento(Apartamento apartamento) throws ExcepcionSGA {
        administracion.registrarApartamento(apartamento);
    }

    public Apartamento buscarApartamento(String codigo) {
        return administracion.buscarApartamento(codigo);
    }

    public void editarApartamento(String codigo, String nombre, String bloque, int piso, String caracteristicas)
            throws ExcepcionSGA {
        administracion.editarApartamento(codigo, nombre, bloque, piso, caracteristicas);
    }

    public void ponerApartamentoFueraDeServicio(String codigo, String motivo) throws ExcepcionSGA {
        administracion.ponerApartamentoFueraDeServicio(codigo, motivo);
    }

    public void eliminarApartamento(String codigo) throws ExcepcionSGA {
        administracion.eliminarApartamento(codigo);
    }

    public ListaDoblementeEnlazada<Apartamento> listarApartamentos() {
        return administracion.listarApartamentos();
    }

    // ==================== Huespedes ====================

    public Huesped registrarHuesped(Huesped huesped) throws ExcepcionSGA {
        return administracion.registrarHuesped(huesped);
    }

    public Huesped buscarHuesped(String documento) {
        return administracion.buscarHuesped(documento);
    }

    public void actualizarHuesped(String documento, String nombre, LocalDate nacimiento, String telefono,
                                  String correo) throws ExcepcionSGA {
        administracion.actualizarHuesped(documento, nombre, nacimiento, telefono, correo);
    }

    public void eliminarHuesped(String documento) throws ExcepcionSGA {
        administracion.eliminarHuesped(documento);
    }

    public ListaDoblementeEnlazada<Huesped> listarHuespedes() {
        return administracion.listarHuespedes();
    }

    /** Historial de reservas de un huesped. */
    public ListaDoblementeEnlazada<Reserva> historialDeHuesped(String documento) throws ExcepcionSGA {
        return exigirHuesped(documento).getHistorial();
    }

    // ==================== Favoritos ====================

    /** @return true si se agrego; false si el apartamento ya era favorito. */
    public boolean agregarFavorito(String documentoHuesped, String codigoApartamento) throws ExcepcionSGA {
        return exigirHuesped(documentoHuesped).agregarFavorito(exigirApartamento(codigoApartamento));
    }

    /** @return true si se quito; false si no estaba entre los favoritos. */
    public boolean quitarFavorito(String documentoHuesped, String codigoApartamento) throws ExcepcionSGA {
        return exigirHuesped(documentoHuesped).quitarFavorito(exigirApartamento(codigoApartamento));
    }

    public Conjunto<Apartamento> listarFavoritos(String documentoHuesped) throws ExcepcionSGA {
        return exigirHuesped(documentoHuesped).getFavoritos();
    }

    // ==================== Empleados ====================

    /** Registra al empleado; si es recepcionista tambien entra a la rotacion de turnos. */
    public void registrarEmpleado(Empleado empleado) throws ExcepcionSGA {
        administracion.registrarEmpleado(empleado);
    }

    public Empleado buscarEmpleado(String documento) {
        return administracion.buscarEmpleado(documento);
    }

    /** Elimina al empleado y, si estaba en la rotacion de recepcion, lo retira de ella. */
    public void eliminarEmpleado(String documento) throws ExcepcionSGA {
        administracion.eliminarEmpleado(documento);
    }

    public ListaDoblementeEnlazada<Empleado> listarEmpleados() {
        return administracion.listarEmpleados();
    }

    public ListaDoblementeEnlazada<Empleado> listarEmpleadosPorRol(RolEmpleado rol) {
        return administracion.listarEmpleadosPorRol(rol);
    }

    // ==================== Turnos de recepcion ====================

    /** Recepcionista con el turno actual. Lanza EstructuraVaciaException si no hay ninguno. */
    public Empleado turnoActual() {
        return turnos.turnoActual();
    }

    /** Termina el turno actual y retorna al recepcionista que lo recibe. */
    public Empleado pasarTurno() {
        return turnos.pasarTurno();
    }

    public int cantidadRecepcionistas() {
        return turnos.cantidad();
    }

    /** Orden de la rotacion empezando por quien tiene el turno actual. */
    public Iterable<Empleado> getRotacionTurnos() {
        return turnos.getRotacion();
    }

    // ==================== Temporadas y tarifas ====================

    public void definirPeriodo(PeriodoTemporada periodo) throws FechasInvalidasException {
        temporadas.definirPeriodo(periodo);
    }

    public boolean eliminarPeriodo(PeriodoTemporada periodo) {
        return temporadas.eliminarPeriodo(periodo);
    }

    /** Temporada de una fecha (BAJA si no cae en ningun periodo definido). */
    public Temporada temporadaDe(LocalDate fecha) {
        return temporadas.temporadaDe(fecha);
    }

    public void definirTarifa(Apartamento apartamento, Temporada temporada, double valorPersonaNoche) {
        temporadas.definirTarifa(apartamento, temporada, valorPersonaNoche);
    }

    public void definirTarifa(String codigoApartamento, Temporada temporada, double valorPersonaNoche)
            throws ExcepcionSGA {
        temporadas.definirTarifa(exigirApartamento(codigoApartamento), temporada, valorPersonaNoche);
    }

    public double tarifaNoche(Apartamento apartamento, LocalDate noche) {
        return temporadas.tarifaNoche(apartamento, noche);
    }

    public ListaDoblementeEnlazada<PeriodoTemporada> getPeriodos() {
        return temporadas.getPeriodos();
    }

    // ==================== Disponibilidad ====================

    public boolean estaLibre(Apartamento apartamento, Estancia estancia) {
        return reservas.estaLibre(apartamento, estancia);
    }

    /** Lanza ReservaSolapadaException si la estancia choca con una reserva vigente del apartamento. */
    public void verificarSolapamiento(Apartamento apartamento, Estancia estancia) throws ReservaSolapadaException {
        reservas.verificarSolapamiento(apartamento, estancia);
    }

    /** Apartamentos en servicio y libres en la estancia, de cualquier capacidad. */
    public ListaDoblementeEnlazada<Apartamento> apartamentosLibres(Estancia estancia) {
        return reservas.apartamentosLibres(apartamentos, estancia);
    }

    /** Apartamentos libres con capacidad suficiente, ordenados con el comparador dado. */
    public ListaDoblementeEnlazada<Apartamento> buscarDisponibles(Estancia estancia, int personas,
                                                                  Comparator<Apartamento> orden) {
        return reservas.buscarDisponibles(apartamentos, estancia, personas, orden);
    }

    /**
     * Apartamentos libres con capacidad suficiente, ordenados por un criterio.
     * Para PRECIO se usa la temporada de la fecha de entrada de la estancia.
     */
    public ListaDoblementeEnlazada<Apartamento> buscarDisponibles(Estancia estancia, int personas,
                                                                  CriterioOrden criterio) {
        return buscarDisponibles(estancia, personas, comparadorPara(criterio, estancia));
    }

    private Comparator<Apartamento> comparadorPara(CriterioOrden criterio, Estancia estancia) {
        if (criterio == null) {
            throw new IllegalArgumentException("El criterio de orden es obligatorio");
        }
        return switch (criterio) {
            case PRECIO -> Apartamento.porPrecio(temporadas.temporadaDe(estancia.getEntrada()));
            case CAPACIDAD -> Apartamento.porCapacidad();
            case DORMITORIOS -> Apartamento.porDormitorios();
            case NUMERO_RESERVAS -> Apartamento.porNumeroReservas();
        };
    }

    // ==================== Cotizacion ====================

    public Cotizacion cotizar(Apartamento apartamento, Estancia estancia,
                              ListaDoblementeEnlazada<Ocupante> ocupantes) throws CapacidadExcedidaException {
        return cotizador.cotizar(apartamento, estancia, ocupantes);
    }

    public Cotizacion cotizarReserva(Reserva reserva) throws CapacidadExcedidaException {
        return reservas.cotizarReserva(reserva);
    }

    /** Anticipo del 30 % que debe pagar el huesped en el canal Portal. */
    public double calcularAnticipo(Reserva reserva) throws CapacidadExcedidaException {
        return reservas.calcularAnticipo(reserva);
    }

    // ==================== Reservas ====================

    /** Crea una reserva. En el canal Portal queda PENDIENTE; en los demas, CONFIRMADA. */
    public Reserva crearReserva(Huesped titular, Apartamento apartamento, Estancia estancia,
                                ListaDoblementeEnlazada<Ocupante> ocupantes, CanalReserva canal)
            throws ReservaSolapadaException, CapacidadExcedidaException {
        return reservas.crearReserva(titular, apartamento, estancia, ocupantes, canal);
    }

    public Reserva crearReserva(Huesped titular, Apartamento apartamento, Estancia estancia,
                                ListaDoblementeEnlazada<Ocupante> ocupantes, CanalReserva canal,
                                LocalDateTime creadaEn)
            throws ReservaSolapadaException, CapacidadExcedidaException {
        return reservas.crearReserva(titular, apartamento, estancia, ocupantes, canal, creadaEn);
    }

    /** Version que resuelve titular y apartamento por documento y codigo. */
    public Reserva crearReserva(String documentoTitular, String codigoApartamento, Estancia estancia,
                                ListaDoblementeEnlazada<Ocupante> ocupantes, CanalReserva canal)
            throws ExcepcionSGA {
        return reservas.crearReserva(exigirHuesped(documentoTitular), exigirApartamento(codigoApartamento),
                estancia, ocupantes, canal);
    }

    /** Reserva un grupo repartido en la combinacion minima de apartamentos libres (todo o nada). */
    public ListaDoblementeEnlazada<Reserva> reservarGrupoGrande(Huesped titular, Estancia estancia,
                                                                ListaDoblementeEnlazada<Ocupante> grupo,
                                                                CanalReserva canal)
            throws ReservaSolapadaException, CapacidadExcedidaException {
        return reservas.reservarGrupoGrande(titular, estancia, grupo, apartamentos, canal);
    }

    /** Solo calcula la combinacion de apartamentos para el grupo, sin reservar. */
    public ListaDoblementeEnlazada<Apartamento> asignarGrupo(int personas, Estancia estancia)
            throws CapacidadExcedidaException {
        return reservas.asignarGrupo(personas, estancia, apartamentos);
    }

    public void confirmarReserva(Reserva reserva) {
        reservas.confirmar(reserva);
    }

    /** El huesped llego: CONFIRMADA -> EN_CURSO. */
    public void iniciarEstancia(Reserva reserva) {
        reserva.iniciar();
    }

    /** El huesped se fue: EN_CURSO -> FINALIZADA. */
    public void finalizarEstancia(Reserva reserva) {
        reserva.finalizar();
    }

    /** Cuanto se devolveria si se cancelara hoy, sin cancelar. */
    public double calcularDevolucion(Reserva reserva, double montoPagado) {
        return reservas.calcularDevolucion(reserva, montoPagado);
    }

    public double calcularDevolucion(Reserva reserva, double montoPagado, LocalDate hoy) {
        return reservas.calcularDevolucion(reserva, montoPagado, hoy);
    }

    /** Cancela la reserva, libera sus noches y retorna el valor a devolver. */
    public double cancelarReserva(Reserva reserva, double montoPagado) {
        return reservas.cancelar(reserva, montoPagado);
    }

    public double cancelarReserva(Reserva reserva, double montoPagado, LocalDate hoy) {
        return reservas.cancelar(reserva, montoPagado, hoy);
    }

    /** El huesped no llego: la reserva pasa a NO_SHOW y libera sus noches. */
    public void marcarNoShow(Reserva reserva) {
        reservas.marcarNoShow(reserva);
    }

    // ==================== Persistencia y datos de prueba ====================

    public void cargarDesdeArchivo(String ruta) throws IOException, ExcepcionSGA {
        cargador.cargarDesdeArchivo(ruta);
    }

    public void guardarEnArchivo(String ruta) throws IOException {
        cargador.guardarEnArchivo(ruta);
    }

    /** Carga temporadas, apartamentos, huespedes y reservas de ejemplo. */
    public void cargarEscenarioPrueba() throws ExcepcionSGA {
        cargador.cargarEscenarioPrueba();
    }

    // ==================== Auxiliares internos ====================

    private Apartamento exigirApartamento(String codigo) throws ExcepcionSGA {
        Apartamento a = administracion.buscarApartamento(codigo);
        if (a == null) {
            throw new ExcepcionSGA("Apartamento inexistente: " + codigo);
        }
        return a;
    }

    private Huesped exigirHuesped(String documento) throws ExcepcionSGA {
        Huesped h = administracion.buscarHuesped(documento);
        if (h == null) {
            throw new ExcepcionSGA("Huesped inexistente: " + documento);
        }
        return h;
    }
}

