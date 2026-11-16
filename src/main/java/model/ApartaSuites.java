package model;

import model.enums.CanalReserva;
import model.enums.CriterioOrden;
import model.enums.RolUsuario;
import model.enums.RolUsuario;
import model.interfaces.Usuario;
import model.enums.Temporada;
import model.estructuras.ArbolBinarioBusqueda;
import model.estructuras.Conjunto;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.excepciones.CapacidadExcedidaException;
import model.excepciones.ExcepcionSGA;
import model.excepciones.FechasInvalidasException;
import model.excepciones.ReservaSolapadaException;
import model.modelo.*;
import model.servicios.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApartaSuites {

    private static final Logger LOGGER = Logger.getLogger(ApartaSuites.class.getName());
    private static ApartaSuites instance;
    private Usuario usuarioAutenticado;

    private final ArbolBinarioBusqueda<Apartamento> apartamentos;
    private final ArbolBinarioBusqueda<Huesped> huespedes;

    private final GestorTemporadas temporadas;
    private final CotizadorService cotizador;
    private final AsignadorGrupos asignador;
    private final GestorVencimientos vencimientos;
    private final GestorReservas reservas;
    private final GestorCanalExterno canalExterno;
    private final GestorPreparacion preparacion;
    private final GestorEdificio edificio;
    private final GestorTurnos turnos;
    private final GestorAdministracion administracion;
    private final GestorReportes reportes;
    private final CargadorDatos cargador;
    private final ScheduledExecutorService procesadorAutomaticoVencimientos;


    private ApartaSuites() {
        this.apartamentos = new ArbolBinarioBusqueda<>();
        this.huespedes = new ArbolBinarioBusqueda<>();
        this.temporadas = new GestorTemporadas();
        this.cotizador = new CotizadorService(temporadas);
        this.asignador = new AsignadorGrupos();
        this.vencimientos = new GestorVencimientos();
        this.reservas = new GestorReservas(cotizador, asignador, vencimientos);
        this.canalExterno = new GestorCanalExterno(reservas);
        this.preparacion = new GestorPreparacion();
        this.edificio = new GestorEdificio();
        this.turnos = new GestorTurnos();
        this.administracion =
                new GestorAdministracion(apartamentos, huespedes, turnos);
        this.reportes = new GestorReportes(apartamentos, huespedes, temporadas);
        this.cargador =
                new CargadorDatos(apartamentos, huespedes, reservas, temporadas);

        // El procesador es daemon: no impide cerrar la aplicación. Se ejecuta
        // periódicamente sin depender de que el usuario abra un panel o pulse un botón.
        this.procesadorAutomaticoVencimientos = Executors.newSingleThreadScheduledExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "sga-vencimientos");
            hilo.setDaemon(true);
            return hilo;
        });
        this.procesadorAutomaticoVencimientos.scheduleAtFixedRate(() -> {
            synchronized (ApartaSuites.this) {
                try {
                    vencimientos.procesarVencimientos(LocalDateTime.now());
                } catch (RuntimeException e) {
                    LOGGER.log(Level.SEVERE, "No fue posible procesar los vencimientos de reservas", e);
                }
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    // Punto único de acceso al sistema.
    public static ApartaSuites getInstance() {
        if (instance == null) {
            instance = new ApartaSuites();
        }
        return instance;
    }
    // ==================== Autenticacion y sesion ====================

    /** Busca el documento en los arboles existentes y valida su contrasena. */
    public synchronized boolean iniciarSesion(String documento, String contrasena) {
        if (documento == null || documento.isBlank() || contrasena == null) return false;
        Usuario usuario = buscarHuesped(documento.trim());
        if (usuario == null) usuario = buscarEmpleado(documento.trim());
        if (usuario == null || !usuario.tieneContrasena() || !usuario.validarContrasena(contrasena)) {
            usuarioAutenticado = null;
            return false;
        }
        usuarioAutenticado = usuario;
        return true;
    }

    public synchronized Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }

    public synchronized RolUsuario getRolAutenticado() {
        return usuarioAutenticado == null ? null : usuarioAutenticado.getRolUsuario();
    }

    public synchronized void cerrarSesion() {
        usuarioAutenticado = null;
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
        if (huesped == null) throw new IllegalArgumentException("El huesped es obligatorio");
        if (buscarEmpleado(huesped.getDocumento()) != null) {
            throw new ExcepcionSGA("Ya existe un usuario con ese documento");
        }
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
        if (empleado == null) throw new IllegalArgumentException("El empleado es obligatorio");
        if (buscarHuesped(empleado.getDocumento()) != null) {
            throw new ExcepcionSGA("Ya existe un usuario con ese documento");
        }
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

    public ListaDoblementeEnlazada<Empleado> listarEmpleadosPorRol(RolUsuario rol) {
        return administracion.listarEmpleadosPorRol(rol);
    }

    // ==================== Edificio y recorridos ====================

    /** Gestor del grafo físico: recepción, pasillos, escaleras, aseo y apartamentos. */
    public GestorEdificio getGestorEdificio() {
        return edificio;
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

    // ==================== Reportes administrativos ====================

    /** Ocupacion por apartamento y temporada en el intervalo [desde, hasta). */
    public synchronized ListaDoblementeEnlazada<ReporteOcupacion> reporteOcupacion(LocalDate desde, LocalDate hasta) {
        return reportes.generarOcupacion(desde, hasta);
    }

    /** Pagos netos recibidos en el intervalo [desde, hasta). */
    public synchronized ReporteIngresos reporteIngresos(LocalDate desde, LocalDate hasta) {
        return reportes.generarIngresos(desde, hasta);
    }

    /** Cantidad de reservas por canal y estado, incluyendo canceladas y no-show. */
    public synchronized ListaDoblementeEnlazada<ReporteReservasCanal> reporteReservasPorCanal() {
        return reportes.generarReservasPorCanal();
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

    /** Crea una reserva directa (confirmada) o del Portal (pendiente). EXTERNA usa la cola FIFO. */
    public synchronized Reserva crearReserva(Huesped titular, Apartamento apartamento, Estancia estancia,
                                             ListaDoblementeEnlazada<Ocupante> ocupantes, CanalReserva canal)
            throws ReservaSolapadaException, CapacidadExcedidaException {
        return reservas.crearReserva(titular, apartamento, estancia, ocupantes, canal);
    }

    public synchronized Reserva crearReserva(Huesped titular, Apartamento apartamento, Estancia estancia,
                                             ListaDoblementeEnlazada<Ocupante> ocupantes, CanalReserva canal,
                                             LocalDateTime creadaEn)
            throws ReservaSolapadaException, CapacidadExcedidaException {
        return reservas.crearReserva(titular, apartamento, estancia, ocupantes, canal, creadaEn);
    }

    /** Version que resuelve titular y apartamento por documento y codigo. */
    public synchronized Reserva crearReserva(String documentoTitular, String codigoApartamento, Estancia estancia,
                                             ListaDoblementeEnlazada<Ocupante> ocupantes, CanalReserva canal)
            throws ExcepcionSGA {
        return reservas.crearReserva(exigirHuesped(documentoTitular), exigirApartamento(codigoApartamento),
                estancia, ocupantes, canal);
    }

    /** Reserva un grupo repartido en la combinacion minima de apartamentos libres (todo o nada). */
    public synchronized ListaDoblementeEnlazada<Reserva> reservarGrupoGrande(Huesped titular, Estancia estancia,
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

    public synchronized void confirmarReserva(Reserva reserva) {
        reservas.confirmar(reserva);
    }

    /** Registra un abono del Portal y confirma al completar el anticipo minimo del 30 %. */
    public synchronized boolean registrarPagoPortal(Reserva reserva, double monto, LocalDateTime fecha) {
        return reservas.registrarPagoPortal(reserva, monto, fecha);
    }

    /** Registra un cargo adicional en el folio desde recepción. */
    public MovimientoFolio registrarCargo(Reserva reserva, String concepto,
                                          BigDecimal valor, LocalDateTime fecha) {
        return reservas.registrarCargo(reserva, concepto, valor, fecha);
    }

    /** Registra un pago de recepción en el folio. */
    public MovimientoFolio registrarPago(Reserva reserva, String concepto,
                                         BigDecimal valor, LocalDateTime fecha) {
        return reservas.registrarPago(reserva, concepto, valor, fecha);
    }

    /** Deshace el último cargo o pago, registrando su movimiento contrario. */
    public MovimientoFolio deshacerUltimaOperacionFolio(Reserva reserva, LocalDateTime fecha) {
        return reservas.deshacerUltimaOperacion(reserva, fecha);
    }

    public boolean puedeDeshacerOperacionFolio(Reserva reserva) {
        return reservas.puedeDeshacer(reserva);
    }

    // ==================== Canal externo (cola FIFO) ====================

    /** Recibe una peticion del tercero y la encola sin bloquear noches todavia. */
    public SolicitudReservaExterna recibirSolicitudExterna(Huesped titular, Apartamento apartamento,
                                                           Estancia estancia,
                                                           ListaDoblementeEnlazada<Ocupante> ocupantes) {
        return canalExterno.recibirSolicitud(titular, apartamento, estancia, ocupantes);
    }

    /** Variante con hora de recepcion controlada, util para pruebas y simulacion. */
    public SolicitudReservaExterna recibirSolicitudExterna(Huesped titular, Apartamento apartamento,
                                                           Estancia estancia,
                                                           ListaDoblementeEnlazada<Ocupante> ocupantes,
                                                           LocalDateTime recibidaEn) {
        return canalExterno.recibirSolicitud(titular, apartamento, estancia, ocupantes, recibidaEn);
    }

    /** Procesa la solicitud mas antigua; retorna null si no hay solicitudes pendientes. */
    public synchronized SolicitudReservaExterna procesarSiguienteSolicitudExterna() {
        return canalExterno.procesarSiguiente();
    }

    /** Procesa todas las solicitudes pendientes en el orden FIFO. */
    public synchronized ListaDoblementeEnlazada<SolicitudReservaExterna> procesarSolicitudesExternas() {
        return canalExterno.procesarTodas();
    }

    public int cantidadSolicitudesExternasPendientes() {
        return canalExterno.cantidadPendientes();
    }

    public ListaDoblementeEnlazada<SolicitudReservaExterna> listarSolicitudesExternasPendientes() {
        return canalExterno.listarPendientes();
    }

    public ListaDoblementeEnlazada<SolicitudReservaExterna> getHistorialCanalExterno() {
        return canalExterno.getHistorial();
    }

    public ListaDoblementeEnlazada<SolicitudReservaExterna> getConflictosCanalExterno() {
        return canalExterno.getConflictos();
    }

    /** El huésped llega: solo puede iniciar si el apartamento está preparado. */
    public synchronized void iniciarEstancia(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria");
        }
        Apartamento apartamento = reserva.getApartamento();
        if (apartamento.getEstadoOperativo() != model.enums.EstadoApartamento.PREPARADO) {
            throw new IllegalStateException("El apartamento " + apartamento.getCodigo()
                    + " no está preparado para recibir huéspedes");
        }
        reserva.iniciar();
        apartamento.cambiarEstado(model.enums.EstadoApartamento.OCUPADO);
    }

    /** Checkout sin hora de llegada siguiente conocida: queda al final de prioridad 2. */
    public synchronized void finalizarEstancia(Reserva reserva) {
        finalizarEstancia(reserva, null);
    }

    /**
     * Checkout y registro en la cola de preparación. Si la llegada estimada es hoy,
     * el apartamento obtiene prioridad 1 y se ordena por esa hora.
     */
    public synchronized void finalizarEstancia(Reserva reserva, LocalDateTime llegadaEstimada) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria");
        }
        Apartamento apartamento = reserva.getApartamento();
        if (apartamento.getEstadoOperativo() != model.enums.EstadoApartamento.OCUPADO) {
            throw new IllegalStateException("El apartamento no está ocupado");
        }
        reserva.finalizar();
        apartamento.cambiarEstado(model.enums.EstadoApartamento.PENDIENTE_PREPARACION);
        preparacion.registrarPendiente(apartamento, llegadaEstimada, LocalDateTime.now());
    }

    public SolicitudPreparacion iniciarSiguientePreparacion(LocalDateTime ahora) {
        return preparacion.iniciarSiguiente(ahora);
    }

    public SolicitudPreparacion finalizarPreparacionActual(LocalDateTime ahora) {
        return preparacion.finalizarActual(ahora);
    }

    public ListaDoblementeEnlazada<SolicitudPreparacion> listarPreparacionesPendientes() {
        return preparacion.listarPendientes();
    }

    /** Recorrido desde recepción siguiendo la prioridad de las preparaciones pendientes. */
    public ListaDoblementeEnlazada<model.estructuras.Grafo.NodoGrafo<Object>> rutaPreparacionesPendientes() {
        return edificio.rutaPreparacion(preparacion.listarPendientes());
    }

    public int cantidadPreparacionesPendientes() {
        return preparacion.cantidadPendientes();
    }

    public double promedioMinutosPreparacion() {
        return preparacion.promedioMinutosPreparacion();
    }

    /** Calcula la devolución a partir de los pagos netos reales del folio. */
    public double calcularDevolucion(Reserva reserva) {
        return reservas.calcularDevolucion(reserva);
    }

    /** Cuanto se devolveria si se cancelara hoy, sin cancelar. */
    public double calcularDevolucion(Reserva reserva, double montoPagado) {
        return reservas.calcularDevolucion(reserva, montoPagado);
    }

    public double calcularDevolucion(Reserva reserva, double montoPagado, LocalDate hoy) {
        return reservas.calcularDevolucion(reserva, montoPagado, hoy);
    }

    /** Cancela usando los pagos reales del folio y registra la devolución correspondiente. */
    public synchronized double cancelarReserva(Reserva reserva) {
        return reservas.cancelar(reserva);
    }

    public synchronized double cancelarReserva(Reserva reserva, LocalDate hoy) {
        return reservas.cancelar(reserva, hoy);
    }

    /** Cancela la reserva, libera sus noches y retorna el valor a devolver. */
    public synchronized double cancelarReserva(Reserva reserva, double montoPagado) {
        return reservas.cancelar(reserva, montoPagado);
    }

    public synchronized double cancelarReserva(Reserva reserva, double montoPagado, LocalDate hoy) {
        return reservas.cancelar(reserva, montoPagado, hoy);
    }

    /** Cancela las reservas Pendientes vencidas y libera sus noches. Retorna cuantas cancelo. */
    public synchronized int procesarVencimientos(LocalDateTime ahora) {
        return vencimientos.procesarVencimientos(ahora);
    }

    /** Cuantas reservas Pendientes siguen esperando su vencimiento. */
    public int reservasPorVencer() {
        return vencimientos.cantidadEnCola();
    }

    /** El huesped no llego: la reserva pasa a NO_SHOW y libera sus noches. */
    public synchronized void marcarNoShow(Reserva reserva) {
        reservas.marcarNoShow(reserva);
    }

    // ==================== Persistencia y datos de prueba ====================

    public synchronized void cargarDesdeArchivo(String ruta) throws IOException, ExcepcionSGA {
        cargador.cargarDesdeArchivo(ruta);
    }

    public synchronized void guardarEnArchivo(String ruta) throws IOException {
        cargador.guardarEnArchivo(ruta);
    }

    /** Carga temporadas, apartamentos, huespedes y reservas de ejemplo. */
    public synchronized void cargarEscenarioPrueba() throws ExcepcionSGA {
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