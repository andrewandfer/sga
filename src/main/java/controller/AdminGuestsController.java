package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;
import model.ApartaSuites;
import model.excepciones.ExcepcionSGA;
import model.interfaces.Usuario;
import model.modelo.Empleado;
import model.modelo.Huesped;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AdminGuestsController {

    public Label adminNameLabel;
    public Label adminInitialLabel;
    public Label guestCountLabel;
    public Label messageLabel;
    public TextField searchField;
    public TextField documentField;
    public TextField nameField;
    public DatePicker birthDatePicker;
    public TextField phoneField;
    public TextField emailField;
    public PasswordField passwordField;
    public TableView<Huesped> guestsTable;
    public TableColumn<Huesped, String> documentColumn;
    public TableColumn<Huesped, String> nameColumn;
    public TableColumn<Huesped, String> birthDateColumn;
    public TableColumn<Huesped, String> phoneColumn;
    public TableColumn<Huesped, String> emailColumn;
    public TableColumn<Huesped, String> reservationsColumn;

    private final ApartaSuites sistema = ApartaSuites.getInstance();
    private final ObservableList<Huesped> huespedes = FXCollections.observableArrayList();
    private String documentoEnEdicion;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void initialize() {
        Usuario usuario = sistema.getUsuarioAutenticado();
        String nombreAdministrador = usuario instanceof Empleado e ? e.getNombre() : "Administrador";
        adminNameLabel.setText(nombreAdministrador);
        adminInitialLabel.setText(nombreAdministrador == null || nombreAdministrador.isBlank() ? "A" : nombreAdministrador.trim().substring(0, 1).toUpperCase(Locale.ROOT));
        documentColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDocumento()));
        nameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        birthDateColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaNacimiento() == null ? "—" : d.getValue().getFechaNacimiento().format(FORMATO_FECHA)));
        phoneColumn.setCellValueFactory(d -> new SimpleStringProperty(valor(d.getValue().getTelefono())));
        emailColumn.setCellValueFactory(d -> new SimpleStringProperty(valor(d.getValue().getCorreo())));
        reservationsColumn.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getHistorial().getTamano())));
        guestsTable.setItems(huespedes);
        guestsTable.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) cargarFormulario(seleccionado);
        });
        cargarHuespedes();
        messageLabel.setText("");
    }

    private void cargarHuespedes() {
        huespedes.clear();
        for (Huesped huesped : sistema.listarHuespedes()) huespedes.add(huesped);
        guestCountLabel.setText(huespedes.size() + " HUÉSPEDES");
        aplicarFiltro();
    }

    public void handleSearch(KeyEvent event) { aplicarFiltro(); }

    private void aplicarFiltro() {
        String filtro = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        ObservableList<Huesped> filtrados = FXCollections.observableArrayList();
        for (Huesped huesped : sistema.listarHuespedes()) {
            String datos = (huesped.getDocumento() + " " + huesped.getNombre() + " "
                    + valor(huesped.getTelefono()) + " " + valor(huesped.getCorreo())).toLowerCase(Locale.ROOT);
            if (datos.contains(filtro)) filtrados.add(huesped);
        }
        guestsTable.setItems(filtrados);
    }

    private void cargarFormulario(Huesped huesped) {
        documentoEnEdicion = huesped.getDocumento();
        documentField.setText(huesped.getDocumento());
        documentField.setDisable(true);
        nameField.setText(huesped.getNombre());
        birthDatePicker.setValue(huesped.getFechaNacimiento());
        phoneField.setText(huesped.getTelefono());
        emailField.setText(huesped.getCorreo());
        passwordField.clear();
        passwordField.setDisable(true);
        passwordField.setPromptText("No editable desde administración");
        messageLabel.setText("Puedes actualizar los datos del huésped, pero no su contraseña.");
        messageLabel.setStyle("-fx-text-fill: #829398;");
    }

    public void handleSave(ActionEvent event) {
        String documento = limpiar(documentField.getText());
        String nombre = limpiar(nameField.getText());
        String telefono = limpiar(phoneField.getText());
        String correo = limpiar(emailField.getText());
        String contrasena = passwordField.getText();
        LocalDate nacimiento = birthDatePicker.getValue();

        if (documento.isEmpty() || nombre.isEmpty()) {
            mostrarMensaje("Documento y nombre son obligatorios.", false);
            return;
        }
        try {
            if (documentoEnEdicion == null) {
                if (contrasena == null || contrasena.isBlank()) {
                    mostrarMensaje("La contraseña es obligatoria para registrar un huésped.", false);
                    return;
                }
                Huesped nuevo = new Huesped(documento, nombre, nacimiento,
                        telefono.isEmpty() ? null : telefono, correo.isEmpty() ? null : correo, contrasena);
                sistema.registrarHuesped(nuevo);
                mostrarMensaje("Huésped registrado correctamente.", true);
            } else {
                sistema.actualizarHuesped(documentoEnEdicion, nombre, nacimiento,
                        telefono.isEmpty() ? null : telefono, correo.isEmpty() ? null : correo);
                mostrarMensaje("Información del huésped actualizada. La contraseña no fue modificada.", true);
            }
            limpiarFormulario();
            cargarHuespedes();
        } catch (ExcepcionSGA | IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), false);
        }
    }

    public void handleDelete(ActionEvent event) {
        Huesped seleccionado = guestsTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) { mostrarMensaje("Selecciona un huésped de la tabla.", false); return; }
        if (sistema.getUsuarioAutenticado() != null
                && seleccionado.getDocumento().equals(sistema.getUsuarioAutenticado().getDocumento())) {
            mostrarMensaje("No puedes eliminar la cuenta con la que has iniciado sesión.", false);
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar huésped");
        confirmacion.setHeaderText("¿Eliminar a " + seleccionado.getNombre() + "?");
        confirmacion.setContentText("Solo será posible si no tiene reservas pendientes, confirmadas o en curso.");
        if (confirmacion.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL)
                != javafx.scene.control.ButtonType.OK) return;
        try {
            sistema.eliminarHuesped(seleccionado.getDocumento());
            limpiarFormulario();
            cargarHuespedes();
            mostrarMensaje("Huésped eliminado correctamente.", true);
        } catch (ExcepcionSGA | IllegalArgumentException e) { mostrarMensaje(e.getMessage(), false); }
    }

    public void handleClear(ActionEvent event) { limpiarFormulario(); mostrarMensaje("Formulario listo para un nuevo huésped.", true); }
    public void handleDashboard(javafx.scene.input.MouseEvent event) { cambiarVista(event, "/views/adminDashboard.fxml"); }
    public void handleApartments(javafx.scene.input.MouseEvent event) { cambiarVista(event, "/views/adminApartments.fxml"); }
    public void handleEmployees(javafx.scene.input.MouseEvent event) { cambiarVista(event, "/views/adminEmployees.fxml"); }

    public void handleSeasons(javafx.scene.input.MouseEvent event) { cambiarVista(event, "/views/adminSeasons.fxml"); }

    public void handleBuildingMap(javafx.scene.input.MouseEvent event) { cambiarVista(event, "/views/adminBuildingMap.fxml"); }

    public void handleLogout(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        sistema.cerrarSesion();
        if (cambiarVista(event, "/views/login.fxml")) {
            stage.setResizable(false); stage.setWidth(1000); stage.setHeight(650); stage.centerOnScreen();
        }
    }

    private boolean cambiarVista(javafx.event.Event event, String ruta) {
        try {
            Node origen = (Node) event.getSource();
            Scene scene = origen.getScene();
            if (scene == null) throw new IllegalStateException("La pantalla actual no está asociada a una escena.");
            Parent root = FXMLLoader.load(getClass().getResource(ruta));
            scene.setRoot(root);
            return true;
        } catch (IOException | RuntimeException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de navegación"); alerta.setHeaderText("No se pudo cambiar de pantalla");
            alerta.setContentText(e.getMessage()); alerta.showAndWait(); return false;
        }
    }

    private void limpiarFormulario() {
        documentoEnEdicion = null; documentField.clear(); documentField.setDisable(false);
        nameField.clear(); birthDatePicker.setValue(null); phoneField.clear(); emailField.clear(); passwordField.clear();
        passwordField.setDisable(false);
        passwordField.setPromptText("Obligatoria al registrar");
        guestsTable.getSelectionModel().clearSelection();
    }
    private String valor(String texto) { return texto == null || texto.isBlank() ? "—" : texto; }
    private String limpiar(String texto) { return texto == null ? "" : texto.trim(); }
    private void mostrarMensaje(String mensaje, boolean exito) {
        messageLabel.setText(mensaje);
        messageLabel.setStyle(exito ? "-fx-text-fill: #31845A;" : "-fx-text-fill: #C65D5D;");
    }
}
