package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.ApartaSuites;
import model.enums.EstadoSolicitudExterna;
import model.interfaces.Usuario;
import model.modelo.Empleado;
import model.modelo.SolicitudReservaExterna;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/** Interfaz de recepción para gestionar solicitudes del canal externo en orden FIFO. */
public class ReceptionExternalRequestsController {

    @FXML private Label employeeNameLabel;
    @FXML private Label employeeInitialLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label acceptedCountLabel;
    @FXML private Label rejectedCountLabel;
    @FXML private VBox pendingList;
    @FXML private VBox historyList;
    @FXML private VBox conflictsList;
    @FXML private Label emptyPendingLabel;
    @FXML private Label emptyHistoryLabel;
    @FXML private Label emptyConflictsLabel;
    @FXML private Button processNextButton;
    @FXML private Button processAllButton;

    private final ApartaSuites sistema = ApartaSuites.getInstance();
    private static final Locale LOCALE = new Locale("es", "CO");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("d MMM yyyy · h:mm a", LOCALE);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", LOCALE);

    @FXML
    private void initialize() {
        Usuario usuario = sistema.getUsuarioAutenticado();
        String nombre = usuario instanceof Empleado empleado ? empleado.getNombre() : "Recepción";
        employeeNameLabel.setText(nombre);
        employeeInitialLabel.setText(nombre.isBlank() ? "R" : nombre.substring(0, 1).toUpperCase(LOCALE));
        actualizarVista();
    }

    @FXML
    private void actualizar() {
        actualizarVista();
    }

    @FXML
    private void procesarSiguiente() {
        if (sistema.cantidadSolicitudesExternasPendientes() == 0) {
            mostrarInfo("Cola vacía", "No hay solicitudes externas pendientes por procesar.");
            actualizarVista();
            return;
        }
        SolicitudReservaExterna procesada = sistema.procesarSiguienteSolicitudExterna();
        if (procesada == null) {
            mostrarInfo("Cola vacía", "No se encontró una solicitud pendiente.");
        } else if (procesada.getEstado() == EstadoSolicitudExterna.ACEPTADA) {
            mostrarInfo("Solicitud aceptada", "La solicitud " + procesada.getId()
                    + " fue aceptada. Se creó la reserva " + procesada.getReservaGenerada().getId() + ".");
        } else {
            mostrarInfo("Solicitud rechazada", "La solicitud " + procesada.getId()
                    + " fue rechazada. Motivo: " + procesada.getMotivoRechazo());
        }
        actualizarVista();
    }

    @FXML
    private void procesarTodas() {
        int cantidad = sistema.cantidadSolicitudesExternasPendientes();
        if (cantidad == 0) {
            mostrarInfo("Cola vacía", "No hay solicitudes externas pendientes por procesar.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Procesar solicitudes");
        confirmacion.setHeaderText("¿Procesar toda la cola externa?");
        confirmacion.setContentText("Se evaluarán " + cantidad
                + " solicitudes en orden de llegada (FIFO). Las que tengan conflictos serán rechazadas.");
        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            int aceptadas = 0;
            int rechazadas = 0;
            for (SolicitudReservaExterna solicitud : sistema.procesarSolicitudesExternas()) {
                if (solicitud.getEstado() == EstadoSolicitudExterna.ACEPTADA) aceptadas++;
                else if (solicitud.getEstado() == EstadoSolicitudExterna.RECHAZADA) rechazadas++;
            }
            mostrarInfo("Procesamiento terminado", "Aceptadas: " + aceptadas + "\nRechazadas: " + rechazadas);
            actualizarVista();
        }
    }

    @FXML
    private void volver(ActionEvent event) {
        cambiarVista(event, "/views/receptionDashboard.fxml");
    }

    @FXML
    private void abrirPreparacion(ActionEvent event) {
        cambiarVista(event, "/views/receptionPreparation.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        sistema.cerrarSesion();
        cambiarVista(event, "/views/login.fxml");
    }

    private void actualizarVista() {
        pendingList.getChildren().clear();
        historyList.getChildren().clear();
        conflictsList.getChildren().clear();

        int pendientes = 0;
        for (SolicitudReservaExterna solicitud : sistema.listarSolicitudesExternasPendientes()) {
            pendingList.getChildren().add(crearTarjeta(solicitud, "PENDIENTE"));
            pendientes++;
        }

        int aceptadas = 0;
        int rechazadas = 0;
        for (SolicitudReservaExterna solicitud : sistema.getHistorialCanalExterno()) {
            if (solicitud.getEstado() == EstadoSolicitudExterna.ACEPTADA) aceptadas++;
            if (solicitud.getEstado() == EstadoSolicitudExterna.RECHAZADA) rechazadas++;
            historyList.getChildren().add(crearTarjeta(solicitud, solicitud.getEstado().name()));
        }
        for (SolicitudReservaExterna solicitud : sistema.getConflictosCanalExterno()) {
            conflictsList.getChildren().add(crearTarjeta(solicitud, "CONFLICTO"));
        }

        pendingCountLabel.setText(String.valueOf(pendientes));
        acceptedCountLabel.setText(String.valueOf(aceptadas));
        rejectedCountLabel.setText(String.valueOf(rechazadas));
        mostrarVacio(emptyPendingLabel, pendientes == 0);
        mostrarVacio(emptyHistoryLabel, historyList.getChildren().isEmpty());
        mostrarVacio(emptyConflictsLabel, conflictsList.getChildren().isEmpty());
        processNextButton.setDisable(pendientes == 0);
        processAllButton.setDisable(pendientes == 0);
    }

    private VBox crearTarjeta(SolicitudReservaExterna solicitud, String estado) {
        VBox tarjeta = new VBox(7);
        tarjeta.getStyleClass().add("request-card");

        HBox encabezado = new HBox(10);
        encabezado.setAlignment(Pos.CENTER_LEFT);
        Label id = new Label(solicitud.getId());
        id.getStyleClass().add("request-id");
        Label badge = new Label(estado.replace('_', ' '));
        badge.getStyleClass().add("request-status");
        badge.getStyleClass().add(estado.toLowerCase(LOCALE).replace('_', '-'));
        HBox.setHgrow(id, Priority.ALWAYS);
        encabezado.getChildren().addAll(id, badge);

        Label huesped = new Label(solicitud.getTitular().getNombre() + " · " + solicitud.getTitular().getDocumento());
        huesped.getStyleClass().add("request-main");
        Label estancia = new Label("Apartamento " + solicitud.getApartamento().getCodigo()
                + "   |   " + solicitud.getEstancia().getEntrada().format(DATE)
                + " — " + solicitud.getEstancia().getSalida().format(DATE)
                + "   |   " + solicitud.getEstancia().noches() + " noches");
        estancia.getStyleClass().add("request-detail");
        Label recibida = new Label("Recibida: " + solicitud.getRecibidaEn().format(DATE_TIME));
        recibida.getStyleClass().add("request-detail");
        tarjeta.getChildren().addAll(encabezado, huesped, estancia, recibida);

        if (solicitud.getMotivoRechazo() != null && !solicitud.getMotivoRechazo().isBlank()) {
            Label motivo = new Label("Motivo: " + solicitud.getMotivoRechazo());
            motivo.getStyleClass().add("request-reason");
            motivo.setWrapText(true);
            tarjeta.getChildren().add(motivo);
        }
        if (solicitud.getReservaGenerada() != null) {
            Label reserva = new Label("Reserva generada: " + solicitud.getReservaGenerada().getId());
            reserva.getStyleClass().add("request-detail");
            tarjeta.getChildren().add(reserva);
        }
        return tarjeta;
    }

    private void mostrarVacio(Label label, boolean vacio) {
        label.setVisible(vacio);
        label.setManaged(vacio);
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cambiarVista(ActionEvent event, String ruta) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(ruta));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
        } catch (IOException | RuntimeException ex) {
            mostrarInfo("No se pudo abrir la vista", ex.getMessage() == null ? ruta : ex.getMessage());
        }
    }
}
