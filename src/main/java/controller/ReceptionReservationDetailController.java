package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.ApartaSuites;
import model.enums.EstadoReserva;
import model.modelo.Apartamento;
import model.modelo.MovimientoFolio;
import model.modelo.Ocupante;
import model.modelo.Reserva;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ReceptionReservationDetailController {

    @FXML private Label reservationIdLabel;
    @FXML private Label statusLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label channelLabel;
    @FXML private Label dateRangeLabel;
    @FXML private Label nightsLabel;
    @FXML private Label guestCountLabel;

    @FXML private Label apartmentNameLabel;
    @FXML private Label apartmentCodeLabel;
    @FXML private Label apartmentLocationLabel;
    @FXML private Label apartmentRoomsLabel;
    @FXML private Label apartmentCapacityLabel;
    @FXML private Label apartmentFeaturesLabel;
    @FXML private Label apartmentStateLabel;

    @FXML private Label holderNameLabel;
    @FXML private Label holderDocumentLabel;
    @FXML private Label holderPhoneLabel;
    @FXML private Label holderEmailLabel;
    @FXML private VBox guestsList;

    @FXML private Label folioIdLabel;
    @FXML private Label totalChargesLabel;
    @FXML private Label totalPaymentsLabel;
    @FXML private Label balanceLabel;
    @FXML private VBox movementsList;

    @FXML private VBox actionsBox;
    @FXML private Label emptyActionsLabel;
    @FXML private Button confirmButton;
    @FXML private Button checkInButton;
    @FXML private Button checkOutButton;
    @FXML private Button noShowButton;
    @FXML private Button cancelButton;
    @FXML private Button paymentButton;
    @FXML private Button chargeButton;
    @FXML private VBox movementForm;
    @FXML private Label movementFormTitle;
    @FXML private TextField movementConceptField;
    @FXML private TextField movementAmountField;
    @FXML private Label feedbackLabel;
    @FXML private VBox cancelConfirmationBox;

    private final ApartaSuites sistema = ApartaSuites.getInstance();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("d/MM/yyyy · h:mm a", new Locale("es", "CO"));
    private Reserva reserva;
    private LocalDate returnWeekStart;
    private boolean registeringCharge;

    @FXML
    private void initialize() {
        movementForm.setVisible(false);
        movementForm.setManaged(false);
        cancelConfirmationBox.setVisible(false);
        cancelConfirmationBox.setManaged(false);
        feedbackLabel.setText("");
    }

    public void setReturnWeekStart(LocalDate returnWeekStart) {
        this.returnWeekStart = returnWeekStart;
    }

    public void setReserva(Reserva reserva) {
        if (reserva == null) throw new IllegalArgumentException("La reserva no puede ser null.");
        this.reserva = reserva;
        // Si la fecha inicial ya quedó atrás y nunca se hizo check-in,
        // cerrar la reserva como No-show y liberar el apartamento al consultar su ficha.
        if (reserva.getEstado() == EstadoReserva.CONFIRMADA
                && LocalDate.now().isAfter(reserva.getEstancia().getEntrada())) {
            sistema.marcarNoShow(reserva);
        }
        cargarInformacion();
        configurarAcciones();
    }

    private void cargarInformacion() {
        Apartamento apartamento = reserva.getApartamento();
        reservationIdLabel.setText(reserva.getId());
        statusLabel.setText(traducirEstado(reserva.getEstado()));
        statusLabel.getStyleClass().removeIf(style -> style.equals("status-pending")
                || style.equals("status-confirmed") || style.equals("status-current")
                || style.equals("status-finished") || style.equals("status-cancelled")
                || style.equals("status-noshow"));
        statusLabel.getStyleClass().add(claseEstado(reserva.getEstado()));
        createdAtLabel.setText(reserva.getCreadaEn().format(DATE_TIME));
        channelLabel.setText(reserva.getCanal().toString());
        dateRangeLabel.setText(reserva.getEstancia().getEntrada().format(DATE)
                + "  —  " + reserva.getEstancia().getSalida().format(DATE));
        nightsLabel.setText(reserva.getEstancia().noches() + " noches");
        guestCountLabel.setText(reserva.numeroPersonas() + " personas");

        apartmentNameLabel.setText(texto(apartamento.getNombre(), "Apartamento"));
        apartmentCodeLabel.setText(apartamento.getCodigo());
        apartmentLocationLabel.setText("Bloque " + texto(apartamento.getBloque(), "—")
                + " · Piso " + apartamento.getPiso());
        apartmentRoomsLabel.setText(apartamento.getDormitorios() + " habitaciones");
        apartmentCapacityLabel.setText("Capacidad máxima: " + apartamento.getCapacidadMaxima() + " personas");
        apartmentFeaturesLabel.setText(texto(apartamento.getCaracteristicas(), "Sin características registradas"));
        apartmentStateLabel.setText(apartamento.getEstadoOperativo().toString());

        holderNameLabel.setText(reserva.getTitular().getNombre());
        holderDocumentLabel.setText("Documento: " + reserva.getTitular().getDocumento());
        holderPhoneLabel.setText("Teléfono: " + texto(reserva.getTitular().getTelefono(), "No registrado"));
        holderEmailLabel.setText("Correo: " + texto(reserva.getTitular().getCorreo(), "No registrado"));
        cargarHuespedes();
        cargarFolio();
    }

    private void cargarHuespedes() {
        guestsList.getChildren().clear();
        int indice = 1;
        for (Ocupante ocupante : reserva.getOcupantes()) {
            VBox guest = new VBox(3);
            guest.getStyleClass().add("guest-row");
            Label name = new Label(indice++ + ". " + ocupante.getNombre());
            name.getStyleClass().add("guest-name");
            Label birth = new Label("Nacimiento: " + ocupante.getFechaNacimiento().format(DATE)
                    + " · Edad al ingreso: " + ocupante.edadEn(reserva.getEstancia().getEntrada()) + " años");
            birth.getStyleClass().add("muted-text");
            guest.getChildren().addAll(name, birth);
            guestsList.getChildren().add(guest);
        }
        if (guestsList.getChildren().isEmpty()) {
            guestsList.getChildren().add(new Label("No hay huéspedes adicionales registrados."));
        }
    }

    private void cargarFolio() {
        var folio = reserva.getFolio();
        folioIdLabel.setText(folio.getId());
        totalChargesLabel.setText(formatear(folio.calcularTotalCargos().subtract(folio.calcularTotalAnulacionesCargos())));
        totalPaymentsLabel.setText(formatear(folio.calcularTotalPagos().subtract(folio.calcularTotalAnulacionesPagos())));
        balanceLabel.setText(formatear(folio.calcularSaldo()));
        System.out.println("Folio: " + folio.getId());
        System.out.println("Movimientos: " + folio.numeroMovimientos());
        System.out.println("Saldo calculado: " + folio.calcularSaldo());
        System.out.println("Texto del Label: " + balanceLabel.getText());
        movementsList.getChildren().clear();
        for (int i = 0; i < folio.numeroMovimientos(); i++) {
            MovimientoFolio movimiento = folio.obtenerMovimiento(i);
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("movement-row");
            VBox description = new VBox(3);
            Label concept = new Label(movimiento.getConcepto());
            concept.getStyleClass().add("movement-concept");
            Label metadata = new Label(movimiento.getTipo() + " · " + movimiento.getFecha().format(DATE_TIME));
            metadata.getStyleClass().add("muted-text");
            description.getChildren().addAll(concept, metadata);
            Label amount = new Label(formatear(movimiento.getValor()));
            amount.getStyleClass().add("movement-amount");
            row.getChildren().addAll(description, amount);
            HBox.setHgrow(description, javafx.scene.layout.Priority.ALWAYS);
            movementsList.getChildren().add(row);
        }
        if (movementsList.getChildren().isEmpty()) {
            Label empty = new Label("Todavía no hay movimientos registrados en este folio.");
            empty.getStyleClass().add("muted-text");
            movementsList.getChildren().add(empty);
        }
    }

    private void configurarAcciones() {
        EstadoReserva estado = reserva.getEstado();
        mostrar(confirmButton, estado == EstadoReserva.PENDIENTE);
        mostrar(checkInButton, estado == EstadoReserva.CONFIRMADA);
        mostrar(checkOutButton, estado == EstadoReserva.EN_CURSO);
        mostrar(noShowButton, estado == EstadoReserva.CONFIRMADA
                && !LocalDate.now().isBefore(reserva.getEstancia().getEntrada()));
        mostrar(cancelButton, estado == EstadoReserva.PENDIENTE || estado == EstadoReserva.CONFIRMADA);
        boolean noCancelada = estado != EstadoReserva.CANCELADA && estado != EstadoReserva.NO_SHOW;
        boolean operativa = estado == EstadoReserva.PENDIENTE || estado == EstadoReserva.CONFIRMADA
                || estado == EstadoReserva.EN_CURSO;
        mostrar(paymentButton, noCancelada
                && reserva.getFolio().calcularSaldo().compareTo(BigDecimal.ZERO) > 0);
        mostrar(chargeButton, operativa);
        boolean hayAcciones = confirmButton.isVisible() || checkInButton.isVisible()
                || checkOutButton.isVisible() || noShowButton.isVisible() || cancelButton.isVisible()
                || paymentButton.isVisible() || chargeButton.isVisible();
        emptyActionsLabel.setVisible(!hayAcciones);
        emptyActionsLabel.setManaged(!hayAcciones);
    }

    @FXML private void confirmarReserva() { ejecutar("Confirmar reserva", () -> sistema.confirmarReserva(reserva)); }
    @FXML
    private void registrarCheckIn() {
        try {
            sistema.iniciarEstancia(reserva);
            feedbackLabel.setText("Check-in registrado correctamente.");
            refrescarFicha();
        } catch (RuntimeException ex) {
            // iniciarEstancia puede convertir una llegada tardía en No-show y liberar el apartamento.
            // Refrescamos también en ese caso para que la ficha muestre el nuevo estado.
            mostrarError(ex.getMessage());
            refrescarFicha();
        }
    }
    @FXML private void registrarCheckOut() { ejecutar("Check-out", () -> sistema.finalizarEstancia(reserva)); }
    @FXML private void marcarNoShow() { ejecutar("No-show", () -> sistema.marcarNoShow(reserva)); }

    @FXML
    private void solicitarCancelacion() {
        cancelConfirmationBox.setVisible(true);
        cancelConfirmationBox.setManaged(true);
        feedbackLabel.setText("Confirma si deseas cancelar esta reserva. Se liberarán las noches ocupadas.");
    }

    @FXML
    private void confirmarCancelacion() {
        try {
            double devolucion = sistema.cancelarReserva(reserva);
            cancelConfirmationBox.setVisible(false);
            cancelConfirmationBox.setManaged(false);
            feedbackLabel.setText("Reserva cancelada. Devolución calculada: " + formatear(BigDecimal.valueOf(devolucion)));
            refrescarFicha();
        } catch (RuntimeException ex) {
            mostrarError(ex.getMessage());
        }
    }

    @FXML
    private void descartarCancelacion() {
        cancelConfirmationBox.setVisible(false);
        cancelConfirmationBox.setManaged(false);
        feedbackLabel.setText("");
    }

    @FXML private void mostrarFormularioPago() { mostrarFormularioMovimiento(false); }
    @FXML private void mostrarFormularioCargo() { mostrarFormularioMovimiento(true); }

    private void mostrarFormularioMovimiento(boolean cargo) {
        registeringCharge = cargo;
        movementFormTitle.setText(cargo ? "Registrar cargo adicional" : "Registrar pago recibido");
        movementConceptField.clear();
        movementAmountField.clear();
        movementForm.setVisible(true);
        movementForm.setManaged(true);
        feedbackLabel.setText("");
    }

    @FXML
    private void guardarMovimiento() {
        try {
            String concepto = movementConceptField.getText() == null ? "" : movementConceptField.getText().trim();
            if (concepto.isBlank()) throw new IllegalArgumentException("Escribe el concepto del movimiento.");
            String raw = movementAmountField.getText() == null ? "" : movementAmountField.getText().trim();
            BigDecimal amount = new BigDecimal(raw.replace("$", "").replace(".", "").replace(",", ".").replace(" ", ""));
            if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("El valor debe ser mayor que cero.");
            if (registeringCharge) {
                sistema.registrarCargo(reserva, concepto, amount, LocalDateTime.now());
            } else {
                sistema.registrarPago(reserva, concepto, amount, LocalDateTime.now());
            }
            movementForm.setVisible(false);
            movementForm.setManaged(false);
            feedbackLabel.setText(registeringCharge ? "Cargo registrado correctamente." : "Pago registrado correctamente.");
            refrescarFicha();
        } catch (NumberFormatException ex) {
            feedbackLabel.setText("Ingresa un valor válido. Ejemplo: 25000 o 25.000");
        } catch (RuntimeException ex) {
            feedbackLabel.setText(ex.getMessage() == null ? "No se pudo registrar el movimiento." : ex.getMessage());
        }
    }

    @FXML
    private void cerrarFormularioMovimiento() {
        movementForm.setVisible(false);
        movementForm.setManaged(false);
    }

    private void ejecutar(String nombre, Runnable operacion) {
        try {
            operacion.run();
            feedbackLabel.setText(nombre + " registrado correctamente.");
            refrescarFicha();
        } catch (RuntimeException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void refrescarFicha() {
        cargarInformacion();
        configurarAcciones();
    }

    @FXML
    private void volverCalendario() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/receptionDashboard.fxml"));
            Parent root = loader.load();
            ReceptionDashboardController controller = loader.getController();
            controller.setWeekStart(returnWeekStart);
            Scene scene = reservationIdLabel.getScene();
            scene.setRoot(root);
            Stage stage = (Stage) scene.getWindow();
            stage.setResizable(true);
            stage.setWidth(1440);
            stage.setHeight(900);
            stage.centerOnScreen();
        } catch (IOException | RuntimeException ex) {
            mostrarError("No se pudo volver al calendario: " + ex.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        feedbackLabel.setText(mensaje == null || mensaje.isBlank() ? "No se pudo completar la operación." : mensaje);
    }

    private void mostrar(Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible);
    }

    private String formatear(BigDecimal valor) {
        return currency.format(valor.setScale(0, RoundingMode.HALF_UP));
    }

    private String texto(String valor, String defecto) {
        return valor == null || valor.isBlank() ? defecto : valor;
    }

    private String traducirEstado(EstadoReserva estado) {
        return switch (estado) {
            case PENDIENTE -> "Pendiente";
            case CONFIRMADA -> "Confirmada";
            case EN_CURSO -> "En curso";
            case FINALIZADA -> "Finalizada";
            case CANCELADA -> "Cancelada";
            case NO_SHOW -> "No se presentó";
        };
    }

    private String claseEstado(EstadoReserva estado) {
        return switch (estado) {
            case PENDIENTE -> "status-pending";
            case CONFIRMADA -> "status-confirmed";
            case EN_CURSO -> "status-current";
            case FINALIZADA -> "status-finished";
            case CANCELADA -> "status-cancelled";
            case NO_SHOW -> "status-noshow";
        };
    }
}
