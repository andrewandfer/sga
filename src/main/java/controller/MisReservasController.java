
package controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import model.ApartaSuites;
import model.enums.EstadoReserva;
import model.enums.EstadoSolicitudExterna;
import model.modelo.Folio;
import model.modelo.Huesped;
import model.modelo.MovimientoFolio;
import model.modelo.Reserva;
import model.modelo.SolicitudReservaExterna;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MisReservasController {

    // CONTADORES
    @FXML private Label totalPendientes;
    @FXML private Label totalConfirmadas;
    @FXML private Label totalEnCurso;
    @FXML private Label totalFinalizadas;

    // LISTA DE RESERVAS
    @FXML private VBox contenedorReservas;

    // DETALLE DEL FOLIO
    @FXML private ComboBox<Reserva> reservaSeleccionada;
    @FXML private VBox movimientosFolio;
    @FXML private Label saldoActual;

    // CANCELACIÓN
    @FXML private Label condicionesCancelacion;
    @FXML private Label valorPagado;
    @FXML private Label valorDevolucion;
    @FXML private Button botonConfirmarCancelacion;

    private final ApartaSuites sistema = ApartaSuites.getInstance();

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DateTimeFormatter formatoFechaHora =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NumberFormat formatoDinero =
            NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private double devolucionCalculada = 0;
    private boolean devolucionConsultada = false;

    @FXML
    public void initialize() {
        formatoDinero.setMaximumFractionDigits(0);
        formatoDinero.setMinimumFractionDigits(0);

        cargarReservas();
        cargarSelectorReservas();
        limpiarDetalle();
    }

    // =====================================================
    // CARGAR RESERVAS DEL HUÉSPED
    // =====================================================

    private Huesped obtenerHuesped() {
        Object usuario = sistema.getUsuarioAutenticado();

        if (usuario instanceof Huesped huesped) {
            return huesped;
        }

        return null;
    }

    private void cargarReservas() {

        contenedorReservas.getChildren().clear();

        Huesped huesped = obtenerHuesped();

        if (huesped == null) {
            mostrarMensaje("No hay una sesión de huésped activa.");
            actualizarContadores(0, 0, 0, 0);
            return;
        }

        int pendientes = 0;
        int confirmadas = 0;
        int enCurso = 0;
        int finalizadas = 0;

        for (Reserva reserva : huesped.getHistorial()) {

            switch (reserva.getEstado()) {
                case PENDIENTE -> pendientes++;
                case CONFIRMADA -> confirmadas++;
                case EN_CURSO -> enCurso++;
                case FINALIZADA -> finalizadas++;
                default -> {
                }
            }

            contenedorReservas.getChildren()
                    .add(crearTarjetaReserva(reserva));
        }

        int solicitudesExternas = agregarSolicitudesExternas(huesped);

        actualizarContadores(
                pendientes,
                confirmadas,
                enCurso,
                finalizadas
        );

        if (huesped.getHistorial().estaVacia() && solicitudesExternas == 0) {
            mostrarMensaje("Todavía no tienes reservas ni solicitudes externas registradas.");
        }
    }

    /** Muestra solicitudes del portal aunque todavía no se hayan convertido en reservas. */
    private int agregarSolicitudesExternas(Huesped huesped) {
        int cantidad = 0;
        boolean encabezadoAgregado = false;

        for (SolicitudReservaExterna solicitud : sistema.listarSolicitudesExternasPendientes()) {
            if (solicitud.getTitular().equals(huesped)) {
                if (!encabezadoAgregado) {
                    agregarEncabezadoSolicitudes();
                    encabezadoAgregado = true;
                }
                contenedorReservas.getChildren().add(crearTarjetaSolicitud(solicitud));
                cantidad++;
            }
        }

        for (SolicitudReservaExterna solicitud : sistema.getHistorialCanalExterno()) {
            if (solicitud.getTitular().equals(huesped)) {
                if (!encabezadoAgregado) {
                    agregarEncabezadoSolicitudes();
                    encabezadoAgregado = true;
                }
                contenedorReservas.getChildren().add(crearTarjetaSolicitud(solicitud));
                cantidad++;
            }
        }
        return cantidad;
    }

    private void agregarEncabezadoSolicitudes() {
        Label encabezado = new Label("SOLICITUDES DE RESERVA EXTERNA");
        encabezado.getStyleClass().add("section-title");
        contenedorReservas.getChildren().add(encabezado);
    }

    private VBox crearTarjetaSolicitud(SolicitudReservaExterna solicitud) {
        VBox tarjeta = new VBox(8);
        tarjeta.getStyleClass().add("reservation-card");

        Label codigo = new Label("SOLICITUD " + solicitud.getId());
        codigo.getStyleClass().add("reservation-code");

        String estadoTexto = switch (solicitud.getEstado()) {
            case PENDIENTE -> "Pendiente de revisión por recepción";
            case ACEPTADA -> "Aceptada · Reserva " + solicitud.getReservaGenerada().getId();
            case RECHAZADA -> "No aceptada";
        };
        Label estado = new Label(estadoTexto);
        estado.getStyleClass().add(solicitud.getEstado() == EstadoSolicitudExterna.PENDIENTE
                ? "status-pending" : solicitud.getEstado() == EstadoSolicitudExterna.ACEPTADA
                ? "status-confirmed" : "section-description");
        estado.setWrapText(true);

        Label alojamiento = new Label("Apartamento " + solicitud.getApartamento().getCodigo());
        alojamiento.getStyleClass().add("reservation-title");
        Label fechas = new Label("Entrada: " + solicitud.getEstancia().getEntrada().format(formatoFecha)
                + " · Salida: " + solicitud.getEstancia().getSalida().format(formatoFecha)
                + " · " + solicitud.getEstancia().noches() + " noche(s)");
        fechas.getStyleClass().add("section-description");
        tarjeta.getChildren().addAll(codigo, estado, alojamiento, fechas);

        if (solicitud.getEstado() == EstadoSolicitudExterna.RECHAZADA
                && solicitud.getMotivoRechazo() != null) {
            Label motivo = new Label("Motivo: " + solicitud.getMotivoRechazo());
            motivo.getStyleClass().add("section-description");
            motivo.setWrapText(true);
            tarjeta.getChildren().add(motivo);
        }
        return tarjeta;
    }

    private void actualizarContadores(
            int pendientes,
            int confirmadas,
            int enCurso,
            int finalizadas) {

        totalPendientes.setText(String.valueOf(pendientes));
        totalConfirmadas.setText(String.valueOf(confirmadas));
        totalEnCurso.setText(String.valueOf(enCurso));
        totalFinalizadas.setText(String.valueOf(finalizadas));
    }

    // =====================================================
    // TARJETAS DE RESERVAS
    // =====================================================

    private VBox crearTarjetaReserva(Reserva reserva) {

        VBox tarjeta = new VBox(14);
        tarjeta.getStyleClass().add("reservation-card");

        HBox encabezado = new HBox();
        encabezado.setAlignment(Pos.CENTER_LEFT);

        VBox identificacion = new VBox(4);

        Label tituloCodigo = new Label("RESERVA");
        tituloCodigo.getStyleClass().add("field-label");

        Label codigo = new Label("Código: " + reserva.getId());
        codigo.getStyleClass().add("reservation-code");

        identificacion.getChildren().addAll(
                tituloCodigo,
                codigo
        );

        Label estado = new Label(
                "Estado: " + reserva.getEstado()
        );

        estado.getStyleClass().add(
                claseEstado(reserva.getEstado())
        );

        HBox.setHgrow(identificacion, Priority.ALWAYS);

        encabezado.getChildren().addAll(
                identificacion,
                estado
        );

        HBox informacion = new HBox(20);

        VBox alojamiento = crearDato(
                "ALOJAMIENTO",
                reserva.getApartamento().getNombre(),
                "Bloque: " + reserva.getApartamento().getBloque()
                        + " · Piso: " + reserva.getApartamento().getPiso()
        );

        VBox entrada = crearDato(
                "FECHA DE ENTRADA",
                reserva.getEstancia().getEntrada().format(formatoFecha),
                ""
        );

        VBox salida = crearDato(
                "FECHA DE SALIDA",
                reserva.getEstancia().getSalida().format(formatoFecha),
                ""
        );

        HBox.setHgrow(alojamiento, Priority.ALWAYS);
        HBox.setHgrow(entrada, Priority.ALWAYS);
        HBox.setHgrow(salida, Priority.ALWAYS);

        informacion.getChildren().addAll(
                alojamiento,
                entrada,
                salida
        );

        tarjeta.getChildren().addAll(
                encabezado,
                new javafx.scene.control.Separator(),
                informacion
        );

        return tarjeta;
    }

    private VBox crearDato(
            String titulo,
            String valor,
            String detalle) {

        VBox dato = new VBox(5);

        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("field-label");

        Label contenido = new Label(
                valor == null || valor.isBlank()
                        ? "No disponible"
                        : valor
        );

        contenido.getStyleClass().add("reservation-title");
        contenido.setWrapText(true);

        dato.getChildren().addAll(
                etiqueta,
                contenido
        );

        if (detalle != null && !detalle.isBlank()) {

            Label descripcion = new Label(detalle);
            descripcion.getStyleClass().add("section-description");

            dato.getChildren().add(descripcion);
        }

        return dato;
    }

    private String claseEstado(EstadoReserva estado) {

        return switch (estado) {
            case PENDIENTE -> "status-pending";
            case CONFIRMADA, EN_CURSO, FINALIZADA -> "status-confirmed";
            default -> "section-description";
        };
    }

    // =====================================================
    // SELECTOR DE RESERVAS
    // =====================================================

    private void cargarSelectorReservas() {

        reservaSeleccionada.getItems().clear();

        Huesped huesped = obtenerHuesped();

        if (huesped == null) {
            return;
        }

        for (Reserva reserva : huesped.getHistorial()) {
            reservaSeleccionada.getItems().add(reserva);
        }

        reservaSeleccionada.setConverter(
                new javafx.util.StringConverter<>() {

                    @Override
                    public String toString(Reserva reserva) {
                        if (reserva == null) {
                            return "";
                        }

                        return "Reserva " + reserva.getId()
                                + " - " + reserva.getEstado();
                    }

                    @Override
                    public Reserva fromString(String texto) {
                        return null;
                    }
                }
        );
    }

    @FXML
    private void seleccionarReserva() {

        Reserva reserva = reservaSeleccionada.getValue();

        limpiarDetalle();

        if (reserva == null) {
            return;
        }

        cargarMovimientosFolio(reserva);
        actualizarInformacionCancelacion(reserva);
    }

    // =====================================================
    // DETALLE DEL FOLIO
    // =====================================================

    private void cargarMovimientosFolio(Reserva reserva) {

        movimientosFolio.getChildren().clear();

        Folio folio = reserva.getFolio();

        if (folio == null || folio.estaVacio()) {

            Label mensaje = new Label(
                    "Esta reserva todavía no tiene movimientos registrados."
            );

            mensaje.getStyleClass().add("empty-description");
            mensaje.setWrapText(true);

            movimientosFolio.getChildren().add(mensaje);
            saldoActual.setText(formatoDinero.format(0));
            return;
        }

        for (int i = 0; i < folio.numeroMovimientos(); i++) {

            MovimientoFolio movimiento = folio.obtenerMovimiento(i);

            HBox fila = new HBox(10);
            fila.setAlignment(Pos.CENTER_LEFT);

            Label concepto = new Label(movimiento.getConcepto());
            Label fecha = new Label(
                    movimiento.getFecha().format(formatoFechaHora)
            );
            Label tipo = new Label(movimiento.getTipo().toString());
            Label valor = new Label(
                    formatoDinero.format(movimiento.getValor())
            );

            concepto.setWrapText(true);
            fecha.setWrapText(true);
            tipo.setWrapText(true);

            HBox.setHgrow(concepto, Priority.ALWAYS);
            HBox.setHgrow(fecha, Priority.ALWAYS);
            HBox.setHgrow(tipo, Priority.ALWAYS);
            HBox.setHgrow(valor, Priority.ALWAYS);

            fila.getChildren().addAll(
                    concepto,
                    fecha,
                    tipo,
                    valor
            );

            movimientosFolio.getChildren().add(fila);
        }

        saldoActual.setText(
                formatoDinero.format(folio.calcularSaldo())
        );
    }

    // =====================================================
    // CONSULTAR DEVOLUCIÓN
    // =====================================================

    private void actualizarInformacionCancelacion(Reserva reserva) {

        BigDecimal pagado = obtenerTotalPagado(reserva);

        valorPagado.setText(formatoDinero.format(pagado));

        valorDevolucion.setText("Pendiente de consulta");

        condicionesCancelacion.setText(
                "Consulta la devolución para conocer cuánto dinero "
                        + "podrías recibir si cancelas esta reserva."
        );

        devolucionCalculada = 0;
        devolucionConsultada = false;

        botonConfirmarCancelacion.setDisable(
                !permiteCancelacion(reserva)
        );
    }

    private BigDecimal obtenerTotalPagado(Reserva reserva) {

        Folio folio = reserva.getFolio();

        if (folio == null) {
            return BigDecimal.ZERO;
        }

        return folio.calcularTotalPagos()
                .subtract(folio.calcularTotalAnulacionesPagos())
                .subtract(folio.calcularTotalDevoluciones());
    }

    private boolean permiteCancelacion(Reserva reserva) {

        return reserva.getEstado() == EstadoReserva.PENDIENTE
                || reserva.getEstado() == EstadoReserva.CONFIRMADA;
    }

    @FXML
    private void consultarDevolucion() {

        Reserva reserva = reservaSeleccionada.getValue();

        if (reserva == null) {
            mostrarAlerta(
                    AlertType.WARNING,
                    "Reserva no seleccionada",
                    "Primero debes seleccionar una reserva."
            );
            return;
        }

        if (!permiteCancelacion(reserva)) {

            mostrarAlerta(
                    AlertType.WARNING,
                    "No se puede cancelar",
                    "Esta reserva no tiene un estado que permita cancelarla."
            );

            return;
        }

        try {

            devolucionCalculada = sistema.calcularDevolucion(reserva);

            devolucionConsultada = true;

            valorDevolucion.setText(
                    formatoDinero.format(devolucionCalculada)
            );

            condicionesCancelacion.setText(
                    "Esta es la devolución estimada según las condiciones "
                            + "de cancelación y los pagos registrados."
            );

            botonConfirmarCancelacion.setDisable(false);

        } catch (Exception e) {

            devolucionConsultada = false;
            botonConfirmarCancelacion.setDisable(true);

            mostrarAlerta(
                    AlertType.ERROR,
                    "Error al consultar",
                    "No fue posible calcular la devolución: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // CONFIRMAR CANCELACIÓN
    // =====================================================

    @FXML
    private void confirmarCancelacion() {

        Reserva reserva = reservaSeleccionada.getValue();

        if (reserva == null || !devolucionConsultada) {

            mostrarAlerta(
                    AlertType.WARNING,
                    "Consulta pendiente",
                    "Primero selecciona una reserva y consulta su devolución."
            );

            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);

        confirmacion.setTitle("Confirmar cancelación");
        confirmacion.setHeaderText("¿Deseas cancelar esta reserva?");

        confirmacion.setContentText(
                "Reserva: " + reserva.getId()
                        + "\nDevolución estimada: "
                        + formatoDinero.format(devolucionCalculada)
                        + "\n\nEsta acción cancelará la reserva."
        );

        var respuesta = confirmacion.showAndWait();

        if (respuesta.isEmpty()
                || respuesta.get() != javafx.scene.control.ButtonType.OK) {
            return;
        }

        try {

            double devolucionReal = sistema.cancelarReserva(reserva);

            mostrarAlerta(
                    AlertType.INFORMATION,
                    "Cancelación realizada",
                    "La reserva fue cancelada correctamente."
                            + "\nDevolución registrada: "
                            + formatoDinero.format(devolucionReal)
            );

            // Actualizar tarjetas, contadores y selector.
            cargarReservas();
            cargarSelectorReservas();
            limpiarDetalle();

        } catch (Exception e) {

            mostrarAlerta(
                    AlertType.ERROR,
                    "No se pudo cancelar",
                    "Ocurrió un problema al cancelar la reserva: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // LIMPIAR Y MOSTRAR MENSAJES
    // =====================================================

    private void limpiarDetalle() {

        movimientosFolio.getChildren().clear();

        Label mensaje = new Label(
                "Selecciona una reserva para consultar sus movimientos."
        );

        mensaje.getStyleClass().add("empty-description");
        mensaje.setWrapText(true);

        movimientosFolio.getChildren().add(mensaje);

        saldoActual.setText("Pendiente de consultar");
        valorPagado.setText("Pendiente");
        valorDevolucion.setText("Pendiente de cálculo");

        condicionesCancelacion.setText(
                "Selecciona una reserva para consultar las condiciones "
                        + "y el valor de devolución."
        );

        devolucionCalculada = 0;
        devolucionConsultada = false;

        botonConfirmarCancelacion.setDisable(true);
    }

    private void mostrarMensaje(String mensaje) {

        Label aviso = new Label(mensaje);
        aviso.getStyleClass().add("section-description");
        aviso.setWrapText(true);

        contenedorReservas.getChildren().add(aviso);
    }

    private void mostrarAlerta(
            AlertType tipo,
            String titulo,
            String mensaje) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // Navegación común del portal del huésped
    @FXML private void irInicio(javafx.event.ActionEvent e) { NavegacionHuesped.abrir(e, "/views/dasboardHuesped.fxml"); }
    @FXML private void irBuscar(javafx.event.ActionEvent e) { NavegacionHuesped.abrir(e, "/views/buscarAlojamiento.fxml"); }
    @FXML private void irReservas(javafx.event.ActionEvent e) { NavegacionHuesped.abrir(e, "/views/misReservas.fxml"); }
    @FXML private void irFavoritos(javafx.event.ActionEvent e) { NavegacionHuesped.abrir(e, "/views/favoritosHuesped.fxml"); }
    @FXML private void irHistorial(javafx.event.ActionEvent e) { NavegacionHuesped.abrir(e, "/views/historialHuesped.fxml"); }
    @FXML private void irPerfil(javafx.event.ActionEvent e) { NavegacionHuesped.abrir(e, "/views/perfilHuesped.fxml"); }
    @FXML private void contactar() { NavegacionHuesped.contactar(); }
    @FXML private void cerrarSesion(javafx.event.ActionEvent e) { NavegacionHuesped.cerrarSesion(e); }
}
