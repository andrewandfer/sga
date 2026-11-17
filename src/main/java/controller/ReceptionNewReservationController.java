package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.ApartaSuites;
import model.enums.CanalReserva;
import model.enums.CriterioOrden;
import model.excepciones.CapacidadExcedidaException;
import model.excepciones.ExcepcionSGA;
import model.excepciones.ReservaSolapadaException;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.modelo.*;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReceptionNewReservationController {
    @FXML private TextField documentField, holderNameField, phoneField, emailField;
    @FXML private DatePicker holderBirthDatePicker, checkInPicker, checkOutPicker;
    @FXML private VBox companionsBox;
    @FXML private ComboBox<Apartamento> apartmentCombo;
    @FXML private ComboBox<CriterioOrden> sortCombo;
    @FXML private ComboBox<CanalReserva> channelCombo;
    @FXML private Label availabilityLabel, quoteLabel, advanceLabel, guestLookupLabel, feedbackLabel;
    @FXML private TextField portalPaymentField;

    private final ApartaSuites sistema = ApartaSuites.getInstance();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    private final List<CompanionFields> companions = new ArrayList<>();
    private Cotizacion cotizacionActual;
    private Huesped huespedEncontrado;
    private boolean loadingGuest;

    @FXML private void initialize() {
        // El titular crea o utiliza una cuenta: debe tener al menos 15 años.
        LocalDate fechaMaximaNacimiento = LocalDate.now().minusYears(15);
        holderBirthDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setDisable(vacia || fecha.isAfter(fechaMaximaNacimiento));
            }
        });

        channelCombo.setItems(FXCollections.observableArrayList(CanalReserva.DIRECTA, CanalReserva.PORTAL));
        channelCombo.setValue(CanalReserva.DIRECTA);
        sortCombo.setItems(FXCollections.observableArrayList(CriterioOrden.PRECIO, CriterioOrden.CAPACIDAD,
                CriterioOrden.DORMITORIOS, CriterioOrden.NUMERO_RESERVAS));
        sortCombo.setValue(CriterioOrden.PRECIO);
        apartmentCombo.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(Apartamento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textoApartamento(item));
            }
        });
        apartmentCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Apartamento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Selecciona un apartamento" : textoApartamento(item));
            }
        });
        documentField.textProperty().addListener((obs, oldValue, newValue) -> buscarTitular());
        apartmentCombo.valueProperty().addListener((obs, oldValue, newValue) -> actualizarCotizacion());
        checkInPicker.valueProperty().addListener((obs, oldValue, newValue) -> actualizarCotizacion());
        checkOutPicker.valueProperty().addListener((obs, oldValue, newValue) -> actualizarCotizacion());
        holderNameField.textProperty().addListener((obs, oldValue, newValue) -> actualizarCotizacion());
        holderBirthDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> actualizarCotizacion());
        channelCombo.valueProperty().addListener((obs, oldValue, newValue) -> actualizarAnticipo());
        portalPaymentField.clear();
        agregarAcompanante();
        actualizarAnticipo();
    }

    @FXML private void agregarAcompanante() {
        TextField name = new TextField();
        name.setPromptText("Nombre completo");
        DatePicker birth = new DatePicker();
        birth.setPromptText("Fecha de nacimiento");
        Button remove = new Button("Quitar");
        HBox row = new HBox(9, name, birth, remove);
        row.getStyleClass().add("companion-row");
        companionsBox.getChildren().add(row);
        CompanionFields fields = new CompanionFields(name, birth, row);
        companions.add(fields);
        remove.setOnAction(event -> {
            companions.remove(fields);
            companionsBox.getChildren().remove(row);
            actualizarCotizacion();
        });
        name.textProperty().addListener((o, a, b) -> actualizarCotizacion());
        birth.valueProperty().addListener((o, a, b) -> actualizarCotizacion());
    }

    private void buscarTitular() {
        if (loadingGuest) return;
        String document = documentField.getText() == null ? "" : documentField.getText().trim();
        huespedEncontrado = document.isBlank() ? null : sistema.buscarHuesped(document);
        if (huespedEncontrado != null) {
            loadingGuest = true;
            holderNameField.setText(huespedEncontrado.getNombre());
            holderBirthDatePicker.setValue(huespedEncontrado.getFechaNacimiento());
            phoneField.setText(huespedEncontrado.getTelefono());
            emailField.setText(huespedEncontrado.getCorreo());
            holderNameField.setDisable(true);
            holderBirthDatePicker.setDisable(true);
            phoneField.setDisable(true);
            emailField.setDisable(true);
            guestLookupLabel.setText("Huésped encontrado. Se reutilizará su perfil y contraseña.");
            guestLookupLabel.setStyle("-fx-text-fill: #347653;");
            loadingGuest = false;
        } else {
            holderNameField.setDisable(false);
            holderBirthDatePicker.setDisable(false);
            phoneField.setDisable(false);
            emailField.setDisable(false);
            if (!document.isBlank()) {
                holderNameField.clear(); holderBirthDatePicker.setValue(null);
                phoneField.clear(); emailField.clear();
                guestLookupLabel.setText("No existe: se registrará al crear la reserva.");
                guestLookupLabel.setStyle("-fx-text-fill: #9A762B;");
            } else guestLookupLabel.setText("Ingresa el documento para buscar o registrar al titular.");
        }
        actualizarCotizacion();
    }

    @FXML private void consultarDisponibilidad() {
        try {
            Estancia estancia = obtenerEstancia();
            int personas = cantidadOcupantes();
            if (personas < 1) throw new IllegalArgumentException("Agrega al menos al titular como ocupante.");
            ListaDoblementeEnlazada<Apartamento> disponibles = sistema.buscarDisponibles(estancia, personas,
                    sortCombo.getValue() == null ? CriterioOrden.PRECIO : sortCombo.getValue());
            apartmentCombo.getItems().clear();
            for (Apartamento apartamento : disponibles) apartmentCombo.getItems().add(apartamento);
            availabilityLabel.setText(disponibles.estaVacia()
                    ? "No hay apartamentos disponibles para esas fechas y capacidad."
                    : disponibles.tamano() + " apartamento(s) disponible(s).");
            if (!disponibles.estaVacia()) apartmentCombo.getSelectionModel().selectFirst();
            else { cotizacionActual = null; quoteLabel.setText("Cotización: —"); }
        } catch (Exception ex) { mostrarError("No se pudo consultar disponibilidad", ex); }
    }

    private Estancia obtenerEstancia() throws Exception {
        return new Estancia(checkInPicker.getValue(), checkOutPicker.getValue());
    }

    private int cantidadOcupantes() {
        int count = 1;
        for (CompanionFields companion : companions) {
            if (!companion.name.getText().isBlank() || companion.birth.getValue() != null) count++;
        }
        return count;
    }

    private ListaDoblementeEnlazada<Ocupante> construirOcupantes() {
        ListaDoblementeEnlazada<Ocupante> result = new ListaDoblementeEnlazada<>();
        result.agregar(new Ocupante(holderNameField.getText().trim(), holderBirthDatePicker.getValue()));
        for (CompanionFields companion : companions) {
            String name = companion.name.getText() == null ? "" : companion.name.getText().trim();
            LocalDate birth = companion.birth.getValue();
            if (name.isBlank() && birth == null) continue;
            if (name.isBlank() || birth == null)
                throw new IllegalArgumentException("Completa nombre y fecha de nacimiento de cada acompañante.");
            result.agregar(new Ocupante(name, birth));
        }
        return result;
    }

    private void actualizarCotizacion() {
        Apartamento apartamento = apartmentCombo.getValue();
        if (apartamento == null || checkInPicker.getValue() == null || checkOutPicker.getValue() == null
                || holderNameField.getText().isBlank() || holderBirthDatePicker.getValue() == null) {
            quoteLabel.setText("Cotización: consulta disponibilidad y selecciona un apartamento.");
            cotizacionActual = null;
            actualizarAnticipo();
            return;
        }
        try {
            cotizacionActual = sistema.cotizar(apartamento, obtenerEstancia(), construirOcupantes());
            quoteLabel.setText("Total: " + currency.format(cotizacionActual.getTotal()) + " · "
                    + obtenerEstancia().noches() + " noche(s)");
        } catch (Exception ex) {
            cotizacionActual = null;
            quoteLabel.setText("No fue posible cotizar: " + ex.getMessage());
        }
        actualizarAnticipo();
    }

    private void actualizarAnticipo() {
        if (channelCombo == null || advanceLabel == null) return;
        if (channelCombo.getValue() != CanalReserva.PORTAL) {
            advanceLabel.setText("Las reservas directas se confirman automáticamente.");
            portalPaymentField.setDisable(true);
            return;
        }
        portalPaymentField.setDisable(false);
        double minimum = cotizacionActual == null ? 0 : cotizacionActual.getTotal() * 0.30;
        advanceLabel.setText("Anticipo mínimo (30 %): " + currency.format(minimum)
                + ". La reserva queda pendiente hasta completar este valor.");
    }

    @FXML private void crearReserva() {
        try {
            String document = documentField.getText() == null ? "" : documentField.getText().trim();
            if (document.isBlank()) throw new IllegalArgumentException("El documento del titular es obligatorio.");
            if (holderNameField.getText().isBlank() || holderBirthDatePicker.getValue() == null)
                throw new IllegalArgumentException("Completa nombre y fecha de nacimiento del titular.");
            LocalDate fechaNacimientoTitular = holderBirthDatePicker.getValue();
            if (fechaNacimientoTitular.isAfter(LocalDate.now()))
                throw new IllegalArgumentException("La fecha de nacimiento del titular no puede estar en el futuro.");
            if (Period.between(fechaNacimientoTitular, LocalDate.now()).getYears() < 15)
                throw new IllegalArgumentException("El titular debe tener al menos 15 años para crear una cuenta.");
            if (apartmentCombo.getValue() == null) throw new IllegalArgumentException("Selecciona un apartamento disponible.");
            if (channelCombo.getValue() == null) throw new IllegalArgumentException("Selecciona el canal de reserva.");

            Estancia estancia = obtenerEstancia();
            if (estancia.getEntrada().isBefore(LocalDate.now()))
                throw new IllegalArgumentException("La fecha de entrada no puede estar en el pasado.");
            ListaDoblementeEnlazada<Ocupante> ocupantes = construirOcupantes();
            for (Ocupante ocupante : ocupantes) if (ocupante.getFechaNacimiento().isAfter(LocalDate.now()))
                throw new IllegalArgumentException("La fecha de nacimiento de los ocupantes no puede estar en el futuro.");
            if (!sistema.estaLibre(apartmentCombo.getValue(), estancia))
                throw new IllegalStateException("El apartamento dejó de estar disponible para esas fechas. Consulta nuevamente.");
            // Revalida capacidad y cotización antes de registrar un huésped nuevo.
            cotizacionActual = sistema.cotizar(apartmentCombo.getValue(), estancia, ocupantes);

            Huesped titular = huespedEncontrado;
            if (titular == null) {
                titular = new Huesped(document, holderNameField.getText().trim(), holderBirthDatePicker.getValue(),
                        phoneField.getText().trim(), emailField.getText().trim(), document);
                sistema.registrarHuesped(titular);
            }
            Reserva reserva = sistema.crearReserva(titular, apartmentCombo.getValue(), estancia, ocupantes,
                    channelCombo.getValue());

            if (channelCombo.getValue() == CanalReserva.PORTAL) {
                double payment = parsearMonto(portalPaymentField.getText());
                if (payment > 0) sistema.registrarPagoPortal(reserva, payment, LocalDateTime.now());
                double minimum = sistema.calcularAnticipo(reserva);
                mostrarInfo("Reserva creada", "Reserva " + reserva.getId() + " creada. Estado: "
                        + reserva.getEstado() + ". Anticipo requerido: " + currency.format(minimum) + ".");
            } else {
                mostrarInfo("Reserva confirmada", "La reserva " + reserva.getId()
                        + " fue creada y confirmada correctamente.");
            }
            volverAlDashboard();
        } catch (Exception ex) { mostrarError("No se pudo crear la reserva", ex); }
    }

    private double parsearMonto(String value) {
        if (value == null || value.isBlank()) return 0;
        double amount = Double.parseDouble(value.trim().replace(',', '.'));
        if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("El anticipo debe ser un valor válido y no negativo.");
        return amount;
    }

    @FXML private void volver() { volverAlDashboard(); }

    private void volverAlDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/receptionDashboard.fxml"));
            Scene scene = documentField.getScene();
            scene.setRoot(root);
            Stage stage = (Stage) scene.getWindow();
            stage.setResizable(true); stage.setWidth(1440); stage.setHeight(900); stage.centerOnScreen();
        } catch (IOException | RuntimeException ex) { mostrarError("No se pudo regresar al dashboard", ex); }
    }

    private String textoApartamento(Apartamento a) {
        return a.getCodigo() + " · " + a.getNombre() + " · capacidad " + a.getCapacidadMaxima()
                + " · " + a.getDormitorios() + " hab.";
    }

    private void mostrarError(String title, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title); alert.setHeaderText(title);
        alert.setContentText(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
        alert.showAndWait();
    }
    private void mostrarInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(title); alert.setContentText(message); alert.showAndWait();
    }

    private static class CompanionFields {
        final TextField name; final DatePicker birth; final HBox row;
        CompanionFields(TextField name, DatePicker birth, HBox row) { this.name = name; this.birth = birth; this.row = row; }
    }
}
