package controlador;

import enums.CanalReserva;
import enums.CriterioOrden;
import enums.Temporada;
import estructuras.ArbolBinarioBusqueda;
import estructuras.Conjunto;
import estructuras.ListaDoblementeEnlazada;
import excepciones.ExcepcionSGA;
import excepciones.FechasInvalidasException;
import modelo.Apartamento;
import modelo.Cotizacion;
import modelo.Empleado;
import modelo.Estancia;
import modelo.Huesped;
import modelo.Ocupante;
import modelo.PeriodoTemporada;
import modelo.Reserva;
import servicios.AsignadorGrupos;
import servicios.CargadorDatos;
import servicios.CotizadorService;
import servicios.GestorAdministracion;
import servicios.GestorReservas;
import servicios.GestorTemporadas;
import servicios.GestorTurnos;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;

/**
 * Fachada del dominio principal para la interfaz grafica: la GUI solo habla con esta
 * clase (nunca con las estructuras ni con los gestores internos) y recibe resultados
 * listos para mostrar. Reune catalogo, huespedes, reservas, cotizacion, temporadas,
 * turnos, administracion y carga/guardado de datos.
 */
public class SgaControlador {

    private final ArbolBinarioBusqueda<Apartamento> apartamentos = new ArbolBinarioBusqueda<>();
    private final ArbolBinarioBusqueda<Huesped> huespedes = new ArbolBinarioBusqueda<>();
    private final GestorTemporadas temporadas = new GestorTemporadas();
    private final CotizadorService cotizador = new CotizadorService(temporadas);
    private final GestorReservas reservas = new GestorReservas(cotizador, new AsignadorGrupos());
    private final GestorTurnos turnos = new GestorTurnos();
    private final GestorAdministracion administracion = new GestorAdministracion(apartamentos, huespedes, turnos);
    private final CargadorDatos cargador = new CargadorDatos(apartamentos, huespedes, reservas, temporadas);

    // ==================== Carga y guardado de datos ====================

    public void cargarEscenarioPrueba() throws ExcepcionSGA {
        cargador.cargarEscenarioPrueba();
    }

    public void cargarDesdeArchivo(String ruta) throws IOException, ExcepcionSGA {
        cargador.cargarDesdeArchivo(ruta);
    }

    public void guardarEnArchivo(String ruta) throws IOException {
        cargador.guardarEnArchivo(ruta);
    }

    // ==================== Disponibilidad con filtros ====================

    /**
     * Apartamentos libres para el rango y el numero de personas, ordenados por el criterio elegido.
     * Los filtros son opcionales (null = sin filtro): bloque, dormitorios minimos y precio maximo por
     * persona y noche (se compara con la tarifa de la temporada del dia de entrada).
     */
    public ListaDoblementeEnlazada<Apartamento> buscarDisponibilidad(LocalDate entrada, LocalDate salida,
            int personas, CriterioOrden orden, String bloque, Integer dormitoriosMinimos, Double precioMaximo)
            throws FechasInvalidasException {
        Estancia estancia = new Estancia(entrada, salida);
        Temporada temporada = temporadas.temporadaDe(entrada);
        ListaDoblementeEnlazada<Apartamento> candidatos =
                reservas.buscarDisponibles(apartamentos, estancia, personas, comparadorPara(orden, temporada));

        ListaDoblementeEnlazada<Apartamento> resultado = new ListaDoblementeEnlazada<>();
        for (Apartamento a : candidatos) {
            if (bloque != null && !bloque.isBlank()
                    && (a.getBloque() == null || !a.getBloque().equalsIgnoreCase(bloque.trim()))) {
                continue;
            }
            if (dormitoriosMinimos != null && a.getDormitorios() < dormitoriosMinimos) {
                continue;
            }
            if (precioMaximo != null && a.tarifaPara(temporada) > precioMaximo) {
                continue;
            }
            resultado.agregar(a);
        }
        return resultado;
    }

    private Comparator<Apartamento> comparadorPara(CriterioOrden orden, Temporada temporada) {
        CriterioOrden criterio = orden == null ? CriterioOrden.PRECIO : orden;
        return switch (criterio) {
            case PRECIO -> Apartamento.porPrecio(temporada);
            case CAPACIDAD -> Apartamento.porCapacidad();
            case DORMITORIOS -> Apartamento.porDormitorios();
            case NUMERO_RESERVAS -> Apartamento.porNumeroReservas();
        };
    }

    // ==================== Cotizacion ====================

    public Cotizacion cotizar(String codigoApartamento, LocalDate entrada, LocalDate salida,
                              ListaDoblementeEnlazada<Ocupante> ocupantes) throws ExcepcionSGA {
        return cotizador.cotizar(exigirApartamento(codigoApartamento), new Estancia(entrada, salida), ocupantes);
    }

    public Cotizacion cotizarReserva(String idReserva) throws ExcepcionSGA {
        return reservas.cotizarReserva(exigirReserva(idReserva));
    }

    public double calcularAnticipo(String idReserva) throws ExcepcionSGA {
        return reservas.calcularAnticipo(exigirReserva(idReserva));
    }

    // ==================== Huespedes ====================

    public Huesped registrarHuesped(String documento, String nombre, LocalDate nacimiento, String telefono,
                                    String correo) throws ExcepcionSGA {
        return administracion.registrarHuesped(new Huesped(documento, nombre, nacimiento, telefono, correo));
    }

    public Huesped buscarHuesped(String documento) {
        return administracion.buscarHuesped(documento);
    }

    public boolean agregarFavorito(String documento, String codigoApartamento) throws ExcepcionSGA {
        return exigirHuesped(documento).agregarFavorito(exigirApartamento(codigoApartamento));
    }

    public boolean quitarFavorito(String documento, String codigoApartamento) throws ExcepcionSGA {
        return exigirHuesped(documento).quitarFavorito(exigirApartamento(codigoApartamento));
    }

    public Conjunto<Apartamento> getFavoritos(String documento) throws ExcepcionSGA {
        return exigirHuesped(documento).getFavoritos();
    }

    public ListaDoblementeEnlazada<Reserva> getHistorial(String documento) throws ExcepcionSGA {
        return exigirHuesped(documento).getHistorial();
    }

    // ==================== Reservas ====================

    public Reserva crearReserva(String documento, String codigoApartamento, LocalDate entrada, LocalDate salida,
                                CanalReserva canal, ListaDoblementeEnlazada<Ocupante> ocupantes)
            throws ExcepcionSGA {
        return reservas.crearReserva(exigirHuesped(documento), exigirApartamento(codigoApartamento),
                new Estancia(entrada, salida), ocupantes, canal);
    }

    /** Grupo que no cabe en un apartamento: reserva la combinacion con menos unidades (todo o nada). */
    public ListaDoblementeEnlazada<Reserva> reservarGrupoGrande(String documento, LocalDate entrada,
            LocalDate salida, ListaDoblementeEnlazada<Ocupante> grupo, CanalReserva canal) throws ExcepcionSGA {
        return reservas.reservarGrupoGrande(exigirHuesped(documento), new Estancia(entrada, salida), grupo,
                apartamentos, canal);
    }

    public void confirmarReserva(String idReserva) throws ExcepcionSGA {
        reservas.confirmar(exigirReserva(idReserva));
    }

    /** Cuanto se devolveria si se cancelara hoy, sin cancelar. */
    public double consultarDevolucion(String idReserva, double montoPagado) throws ExcepcionSGA {
        return reservas.calcularDevolucion(exigirReserva(idReserva), montoPagado);
    }

    /** Cancela la reserva y retorna el valor a devolver. */
    public double cancelarReserva(String idReserva, double montoPagado) throws ExcepcionSGA {
        return reservas.cancelar(exigirReserva(idReserva), montoPagado);
    }

    /** Busca por folio en el historial de los huespedes (incluye canceladas y no-show). */
    public Reserva buscarReserva(String idReserva) {
        for (Huesped h : huespedes) {
            for (Reserva r : h.getHistorial()) {
                if (r.getId().equals(idReserva)) {
                    return r;
                }
            }
        }
        return null;
    }

    // ==================== Temporadas ====================

    public void definirTemporada(LocalDate inicio, LocalDate fin, Temporada temporada) throws ExcepcionSGA {
        temporadas.definirPeriodo(new PeriodoTemporada(inicio, fin, temporada));
    }

    public Temporada temporadaDe(LocalDate fecha) {
        return temporadas.temporadaDe(fecha);
    }

    public ListaDoblementeEnlazada<PeriodoTemporada> getPeriodos() {
        return temporadas.getPeriodos();
    }

    // ==================== Turnos de recepcion ====================

    public Empleado turnoActual() {
        return turnos.turnoActual();
    }

    public Empleado pasarTurno() {
        return turnos.pasarTurno();
    }

    // ==================== Consultas y administracion ====================

    public Apartamento buscarApartamento(String codigo) {
        return administracion.buscarApartamento(codigo);
    }

    public ListaDoblementeEnlazada<Apartamento> listarApartamentos() {
        return administracion.listarApartamentos();
    }

    public ListaDoblementeEnlazada<Huesped> listarHuespedes() {
        return administracion.listarHuespedes();
    }

    public GestorAdministracion getAdministracion() {
        return administracion;
    }

    public GestorReservas getGestorReservas() {
        return reservas;
    }

    public GestorTemporadas getGestorTemporadas() {
        return temporadas;
    }

    public ArbolBinarioBusqueda<Apartamento> getArbolApartamentos() {
        return apartamentos;
    }

    public ArbolBinarioBusqueda<Huesped> getArbolHuespedes() {
        return huespedes;
    }

    // ==================== Utilidades internas ====================

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

    private Reserva exigirReserva(String id) throws ExcepcionSGA {
        Reserva r = buscarReserva(id);
        if (r == null) {
            throw new ExcepcionSGA("Reserva inexistente: " + id);
        }
        return r;
    }
}
