package model.servicios;

import model.enums.CanalReserva;
import model.enums.EstadoApartamento;
import model.enums.EstadoReserva;
import model.estructuras.ArbolBinarioBusqueda;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.excepciones.CapacidadExcedidaException;
import model.excepciones.ReservaSolapadaException;
import model.excepciones.TransicionInvalidaException;
import model.modelo.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import model.enums.TipoMovimientoFolio;
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
    private final GestorVencimientos vencimientos;
    private final GestorFolios gestorFolios;
    private int consecutivo = 0;

    public GestorReservas(CotizadorService cotizador, AsignadorGrupos asignador,
                          GestorVencimientos vencimientos) {
        this(cotizador, asignador, vencimientos, new GestorFolios());
    }

    public GestorReservas(CotizadorService cotizador, AsignadorGrupos asignador,
                          GestorVencimientos vencimientos, GestorFolios gestorFolios) {
        if (cotizador == null || asignador == null || vencimientos == null || gestorFolios == null) {
            throw new IllegalArgumentException("Los gestores y servicios de reservas son obligatorios");
        }
        this.cotizador = cotizador;
        this.asignador = asignador;
        this.vencimientos = vencimientos;
        this.gestorFolios = gestorFolios;
    }

    public GestorVencimientos getVencimientos() {
        return vencimientos;
    }

    public GestorFolios getGestorFolios() {
        return gestorFolios;
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
        if (canal == CanalReserva.EXTERNA) {
            throw new IllegalArgumentException(
                    "Las solicitudes EXTERNAS deben ingresar por la cola FIFO del canal externo");
        }
        return crearReservaInterna(titular, apartamento, estancia, ocupantes, canal, creadaEn, false);
    }

    /**
     * Crea y confirma una reserva EXTERNA. Solo debe invocarlo GestorCanalExterno
     * despues de retirar la solicitud de la cola FIFO y validar disponibilidad.
     */
    Reserva crearReservaExternaConfirmada(Huesped titular, Apartamento apartamento, Estancia estancia,
                                          ListaDoblementeEnlazada<Ocupante> ocupantes,
                                          LocalDateTime creadaEn)
            throws ReservaSolapadaException, CapacidadExcedidaException {
        return crearReservaInterna(titular, apartamento, estancia, ocupantes,
                CanalReserva.EXTERNA, creadaEn, true);
    }

    private Reserva crearReservaInterna(Huesped titular, Apartamento apartamento, Estancia estancia,
                                        ListaDoblementeEnlazada<Ocupante> ocupantes, CanalReserva canal,
                                        LocalDateTime creadaEn, boolean confirmarExterna)
            throws ReservaSolapadaException, CapacidadExcedidaException {
        if (titular == null || apartamento == null || estancia == null || ocupantes == null || canal == null
                || creadaEn == null) {
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

        // Se calcula antes de registrar la reserva para no dejar noches ocupadas
        // si la cotizacion falla por alguna validacion.
        double totalAlojamiento = cotizador.cotizar(apartamento, estancia, ocupantes).getTotal();

        consecutivo++;
        String id = String.format("RES-%04d", consecutivo);
        Reserva reserva = new Reserva(id, canal, titular, apartamento, estancia, ocupantes, creadaEn);
        if (canal == CanalReserva.DIRECTA || confirmarExterna) {
            reserva.confirmar();
        }
        // Si el arbol rechaza la reserva no se puede dejar creada: el apartamento quedaria
        // sin registrar sus noches y se podria vender dos veces.
        if (!apartamento.agregarReserva(reserva)) {
            throw new IllegalStateException("No se pudo registrar la reserva " + id + " en el apartamento "
                    + apartamento.getCodigo() + ": ya existe una reserva vigente con la misma fecha de entrada");
        }

        // El folio inicia con el cargo total del alojamiento. Los pagos se registran
        // despues; en Portal la reserva sigue pendiente hasta completar el 30 %.
        if (totalAlojamiento > 0) {
            BigDecimal cargo = dinero(totalAlojamiento);
            gestorFolios.registrarCargo(reserva.getFolio(), new MovimientoFolio(
                    "CARGO-" + id,
                    TipoMovimientoFolio.CARGO,
                    "Alojamiento de la reserva " + id,
                    cargo,
                    creadaEn,
                    null
            ));
        }

        titular.agregarAlHistorial(reserva);
        if (canal == CanalReserva.PORTAL && reserva.getEstado() == EstadoReserva.PENDIENTE) {
            vencimientos.agregarReserva(reserva);
        }
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
        try {
            for (Apartamento apartamento : combinacion) {
                ListaDoblementeEnlazada<Ocupante> parte = new ListaDoblementeEnlazada<>();
                while (iterador.hasNext() && parte.tamano() < apartamento.getCapacidadMaxima()) {
                    parte.agregar(iterador.next());
                }
                reservas.agregar(crearReserva(titular, apartamento, estancia, parte, canal));
            }
        } catch (ReservaSolapadaException | CapacidadExcedidaException | RuntimeException e) {
            // Todo o nada: si una de las reservas del grupo falla, se deshacen las ya creadas.
            deshacerReservas(reservas);
            throw e;
        }
        return reservas;
    }

    /** Libera las noches y borra del historial las reservas de un grupo que no se pudo completar. */
    private void deshacerReservas(ListaDoblementeEnlazada<Reserva> creadas) {
        for (Reserva r : creadas) {
            r.cancelar();
            r.getApartamento().quitarReserva(r);
            r.getTitular().quitarDelHistorial(r);
        }
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


    /**
     * Confirma una reserva. Las reservas del Portal requieren que el folio
     * tenga registrado, como minimo, el 30 % del valor cotizado.
     */
    public void confirmar(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria");
        }

        if (reserva.getCanal() == CanalReserva.PORTAL
                && reserva.getEstado() == EstadoReserva.PENDIENTE) {
            BigDecimal anticipoRequerido;
            try {
                anticipoRequerido = dinero(calcularAnticipo(reserva));
            } catch (CapacidadExcedidaException e) {
                throw new IllegalStateException("No fue posible validar el anticipo de la reserva", e);
            }

            if (reserva.getFolio().calcularTotalPagos().compareTo(anticipoRequerido) < 0) {
                throw new IllegalStateException("La reserva del Portal requiere un anticipo minimo de $"
                        + anticipoRequerido + " para ser confirmada");
            }
        }

        reserva.confirmar();
    }

    /**
     * Registra un pago realizado por el Portal. Admite abonos parciales y confirma
     * automaticamente la reserva cuando los pagos acumulados alcanzan el 30 %.
     * @return true si el pago dejo la reserva confirmada; false si sigue pendiente.
     */
    public boolean registrarPagoPortal(Reserva reserva, double monto, LocalDateTime fecha) {
        if (reserva == null || fecha == null) {
            throw new IllegalArgumentException("La reserva y la fecha del pago son obligatorias");
        }
        if (reserva.getCanal() != CanalReserva.PORTAL) {
            throw new IllegalArgumentException("Este registro de pago aplica unicamente a reservas del Portal");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden registrar anticipos de reservas pendientes");
        }
        if (!Double.isFinite(monto) || monto <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser positivo y finito");
        }

        BigDecimal valor = dinero(monto);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El pago debe ser de al menos un centavo");
        }

        String idMovimiento = "PAGO-" + reserva.getId() + "-" + (reserva.getFolio().numeroMovimientos() + 1);
        gestorFolios.registrarPago(reserva.getFolio(), new MovimientoFolio(
                idMovimiento,
                TipoMovimientoFolio.PAGO,
                "Abono de anticipo por Portal",
                valor,
                fecha,
                null
        ));

        BigDecimal requerido;
        try {
            requerido = dinero(calcularAnticipo(reserva));
        } catch (CapacidadExcedidaException e) {
            throw new IllegalStateException("No fue posible calcular el anticipo de la reserva", e);
        }

        if (reserva.getFolio().calcularTotalPagos().compareTo(requerido) >= 0) {
            confirmar(reserva);
        }
        return reserva.getEstado() == EstadoReserva.CONFIRMADA;
    }

    /** Registra un cargo adicional en el folio de una reserva. */
    public MovimientoFolio registrarCargo(Reserva reserva, String concepto,
                                          BigDecimal valor, LocalDateTime fecha) {
        validarDatosFolio(reserva, concepto, valor, fecha);
        MovimientoFolio movimiento = new MovimientoFolio(
                generarIdMovimiento(reserva, "CARGO"),
                TipoMovimientoFolio.CARGO,
                concepto,
                dinero(valor),
                fecha,
                null
        );
        gestorFolios.registrarCargo(reserva.getFolio(), movimiento);
        return movimiento;
    }

    /** Registra un pago de recepción en el folio de una reserva. */
    public MovimientoFolio registrarPago(Reserva reserva, String concepto,
                                         BigDecimal valor, LocalDateTime fecha) {
        validarDatosFolio(reserva, concepto, valor, fecha);
        MovimientoFolio movimiento = new MovimientoFolio(
                generarIdMovimiento(reserva, "PAGO"),
                TipoMovimientoFolio.PAGO,
                concepto,
                dinero(valor),
                fecha,
                null
        );
        gestorFolios.registrarPago(reserva.getFolio(), movimiento);
        return movimiento;
    }

    /** Deshace el último cargo o pago del folio, conservando ambos movimientos. */
    public MovimientoFolio deshacerUltimaOperacion(Reserva reserva, LocalDateTime fecha) {
        if (reserva == null || fecha == null) {
            throw new IllegalArgumentException("La reserva y la fecha son obligatorias");
        }
        MovimientoFolio ultima = gestorFolios.obtenerUltimaOperacion(reserva.getFolio());
        // Una reserva Portal ya confirmada debe conservar el anticipo mínimo del 30 %.
        if (reserva.getCanal() == CanalReserva.PORTAL
                && reserva.getEstado() == EstadoReserva.CONFIRMADA
                && ultima.getTipo() == TipoMovimientoFolio.PAGO) {
            BigDecimal pagosRestantes = reserva.getFolio().calcularTotalPagos()
                    .subtract(reserva.getFolio().calcularTotalAnulacionesPagos())
                    .subtract(ultima.getValor());
            try {
                if (pagosRestantes.compareTo(dinero(calcularAnticipo(reserva))) < 0) {
                    throw new IllegalStateException(
                            "No se puede deshacer este pago porque la reserva Portal quedaría por debajo del anticipo del 30 %");
                }
            } catch (CapacidadExcedidaException e) {
                throw new IllegalStateException("No fue posible validar el anticipo de la reserva", e);
            }
        }
        return gestorFolios.deshacerUltimaOperacion(reserva.getFolio(), fecha);
    }

    public boolean puedeDeshacer(Reserva reserva) {
        return reserva != null && gestorFolios.puedeDeshacer(reserva.getFolio());
    }

    private void validarDatosFolio(Reserva reserva, String concepto,
                                   BigDecimal valor, LocalDateTime fecha) {
        if (reserva == null || concepto == null || concepto.isBlank()
                || valor == null || fecha == null) {
            throw new IllegalArgumentException(
                    "Reserva, concepto, valor y fecha son obligatorios");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor del movimiento debe ser positivo");
        }
    }

    private String generarIdMovimiento(Reserva reserva, String prefijo) {
        return prefijo + "-" + reserva.getId() + "-" + (reserva.getFolio().numeroMovimientos() + 1);
    }

    private BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal dinero(double valor) {
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException("El valor monetario debe ser finito");
        }
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }

    /** Calcula la devolución usando únicamente los pagos netos registrados en el folio. */
    public double calcularDevolucion(Reserva reserva) {
        return calcularDevolucion(reserva, dineroDisponibleParaDevolver(reserva).doubleValue(), LocalDate.now());
    }

    /** Muestra cuánto se devolvería del monto indicado, sin cancelar la reserva. */
    public double calcularDevolucion(Reserva reserva, double montoPagado) {
        return calcularDevolucion(reserva, montoPagado, LocalDate.now());
    }

    public double calcularDevolucion(Reserva reserva, double montoPagado, LocalDate hoy) {
        validarReservaCancelable(reserva);
        if (hoy == null || !Double.isFinite(montoPagado) || montoPagado < 0) {
            throw new IllegalArgumentException("La fecha y el monto pagado deben ser válidos");
        }
        BigDecimal disponible = dineroDisponibleParaDevolver(reserva);
        BigDecimal solicitado = dinero(montoPagado);
        if (solicitado.compareTo(disponible) > 0) {
            throw new IllegalArgumentException(
                    "El monto indicado supera los pagos netos registrados en el folio");
        }
        long dias = ChronoUnit.DAYS.between(hoy, reserva.getEstancia().getEntrada());
        if (dias >= DIAS_DEVOLUCION_TOTAL) {
            return solicitado.doubleValue();
        }
        if (dias >= DIAS_DEVOLUCION_PARCIAL) {
            return solicitado.multiply(BigDecimal.valueOf(PORCENTAJE_DEVOLUCION_PARCIAL))
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        return 0;
    }

    /** Cancela y devuelve el importe que corresponda según los pagos reales del folio. */
    public double cancelar(Reserva reserva) {
        return cancelar(reserva, LocalDate.now(), LocalDateTime.now());
    }

    public double cancelar(Reserva reserva, LocalDate hoy) {
        return cancelar(reserva, hoy, LocalDateTime.now());
    }

    public double cancelar(Reserva reserva, LocalDate hoy, LocalDateTime fechaMovimiento) {
        validarReservaCancelable(reserva);
        if (hoy == null || fechaMovimiento == null) {
            throw new IllegalArgumentException("Las fechas de cancelación son obligatorias");
        }
        BigDecimal disponible = dineroDisponibleParaDevolver(reserva);
        double devolucion = calcularDevolucion(reserva, disponible.doubleValue(), hoy);
        if (devolucion > 0) {
            MovimientoFolio movimiento = new MovimientoFolio(
                    generarIdMovimiento(reserva, "DEVOLUCION"),
                    TipoMovimientoFolio.DEVOLUCION,
                    "Devolución por cancelación de la reserva " + reserva.getId(),
                    dinero(devolucion),
                    fechaMovimiento,
                    null
            );
            gestorFolios.registrarDevolucion(reserva.getFolio(), movimiento);
        }
        reserva.cancelar();
        reserva.getApartamento().quitarReserva(reserva);
        return devolucion;
    }

    /** Compatibilidad con llamadas que indican el monto pagado; nunca permite devolver dinero no recibido. */
    public double cancelar(Reserva reserva, double montoPagado) {
        return cancelar(reserva, montoPagado, LocalDate.now());
    }

    public double cancelar(Reserva reserva, double montoPagado, LocalDate hoy) {
        validarReservaCancelable(reserva);
        double devolucion = calcularDevolucion(reserva, montoPagado, hoy);
        if (devolucion > 0) {
            MovimientoFolio movimiento = new MovimientoFolio(
                    generarIdMovimiento(reserva, "DEVOLUCION"),
                    TipoMovimientoFolio.DEVOLUCION,
                    "Devolución por cancelación de la reserva " + reserva.getId(),
                    dinero(devolucion),
                    LocalDateTime.now(),
                    null
            );
            gestorFolios.registrarDevolucion(reserva.getFolio(), movimiento);
        }
        reserva.cancelar();
        reserva.getApartamento().quitarReserva(reserva);
        return devolucion;
    }

    private void validarReservaCancelable(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE
                && reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new TransicionInvalidaException("La reserva " + reserva.getId()
                    + " no se puede cancelar en estado " + reserva.getEstado());
        }
    }

    private BigDecimal dineroDisponibleParaDevolver(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria");
        }
        Folio folio = reserva.getFolio();
        return folio.calcularTotalPagos()
                .subtract(folio.calcularTotalDevoluciones())
                .subtract(folio.calcularTotalAnulacionesPagos());
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
        return dinero(cotizarReserva(reserva).getTotal() * PORCENTAJE_ANTICIPO).doubleValue();
    }
}