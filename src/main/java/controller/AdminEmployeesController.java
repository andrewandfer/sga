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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;
import model.ApartaSuites;
import model.enums.RolUsuario;
import model.excepciones.ExcepcionSGA;
import model.interfaces.Usuario;
import model.modelo.Empleado;

import java.io.IOException;
import java.util.Locale;

public class AdminEmployeesController {

    public Label adminNameLabel;
    public Label adminInitialLabel;
    public Label employeeCountLabel;
    public Label messageLabel;
    public TextField searchField;
    public TextField documentField;
    public TextField nameField;
    public TextField phoneField;
    public PasswordField passwordField;
    public ComboBox<RolUsuario> roleComboBox;
    public TableView<Empleado> employeesTable;
    public TableColumn<Empleado, String> documentColumn;
    public TableColumn<Empleado, String> nameColumn;
    public TableColumn<Empleado, String> phoneColumn;
    public TableColumn<Empleado, String> roleColumn;

    private final ApartaSuites sistema = ApartaSuites.getInstance();
    private final ObservableList<Empleado> empleados = FXCollections.observableArrayList();

    public void initialize() {
        Usuario usuario = sistema.getUsuarioAutenticado();
        String nombreAdministrador = usuario instanceof Empleado e ? e.getNombre() : "Administrador";
        adminNameLabel.setText(nombreAdministrador);
        adminInitialLabel.setText(obtenerInicial(nombreAdministrador));
        roleComboBox.setItems(FXCollections.observableArrayList(
                RolUsuario.RECEPCIONISTA, RolUsuario.ADMINISTRADOR));
        roleComboBox.setValue(RolUsuario.RECEPCIONISTA);
        roleComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(RolUsuario rol) {
                if (rol == null) return "";
                return rol == RolUsuario.ADMINISTRADOR ? "Administrador" : "Recepcionista";
            }
            @Override public RolUsuario fromString(String texto) { return null; }
        });
        documentColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDocumento()));
        nameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        phoneColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTelefono() == null || d.getValue().getTelefono().isBlank()
                        ? "—" : d.getValue().getTelefono()));
        roleColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getRol() == RolUsuario.ADMINISTRADOR ? "Administrador" : "Recepcionista"));
        employeesTable.setItems(empleados);
        cargarEmpleados();
        messageLabel.setText("");
    }

    private void cargarEmpleados() {
        empleados.clear();
        for (Empleado empleado : sistema.listarEmpleados()) empleados.add(empleado);
        employeeCountLabel.setText(empleados.size() + " EMPLEADOS");
        aplicarFiltro();
    }

    public void handleSearch(KeyEvent event) {
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        String filtro = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        ObservableList<Empleado> filtrados = FXCollections.observableArrayList();
        for (Empleado empleado : sistema.listarEmpleados()) {
            String datos = (empleado.getDocumento() + " " + empleado.getNombre() + " " + empleado.getRol())
                    .toLowerCase(Locale.ROOT);
            if (datos.contains(filtro)) filtrados.add(empleado);
        }
        employeesTable.setItems(filtrados);
    }

    public void handleRegister(ActionEvent event) {
        String documento = limpiar(documentField.getText());
        String nombre = limpiar(nameField.getText());
        String telefono = limpiar(phoneField.getText());
        String contrasena = passwordField.getText();
        RolUsuario rol = roleComboBox.getValue();

        if (documento.isEmpty() || nombre.isEmpty() || contrasena == null || contrasena.isBlank() || rol == null) {
            mostrarMensaje("Completa documento, nombre, rol y contraseña.", false);
            return;
        }

        try {
            Empleado empleado = new Empleado(documento, nombre, rol,
                    telefono.isEmpty() ? null : telefono, contrasena);
            sistema.registrarEmpleado(empleado);
            limpiarFormulario();
            cargarEmpleados();
            mostrarMensaje("Empleado registrado correctamente.", true);
        } catch (ExcepcionSGA | IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), false);
        }
    }

    public void handleDelete(ActionEvent event) {
        Empleado seleccionado = employeesTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje("Selecciona un empleado de la tabla.", false);
            return;
        }
        if (sistema.getUsuarioAutenticado() != null
                && seleccionado.getDocumento().equals(sistema.getUsuarioAutenticado().getDocumento())) {
            mostrarMensaje("No puedes eliminar la cuenta con la que has iniciado sesión.", false);
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar empleado");
        confirmacion.setHeaderText("¿Eliminar a " + seleccionado.getNombre() + "?");
        confirmacion.setContentText("La cuenta dejará de estar disponible para iniciar sesión.");
        if (confirmacion.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL)
                != javafx.scene.control.ButtonType.OK) return;
        try {
            sistema.eliminarEmpleado(seleccionado.getDocumento());
            cargarEmpleados();
            mostrarMensaje("Empleado eliminado correctamente.", true);
        } catch (ExcepcionSGA | IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), false);
        }
    }

    public void handleApartments(javafx.scene.input.MouseEvent event) {
        cambiarVista(event, "/views/adminApartments.fxml");
    }

    public void handleDashboard(javafx.scene.input.MouseEvent event) {
        cambiarVista(event, "/views/adminDashboard.fxml");
    }

    public void handleGuests(javafx.scene.input.MouseEvent event) {
        cambiarVista(event, "/views/adminGuests.fxml");
    }

    public void handleSeasons(javafx.scene.input.MouseEvent event) {
        cambiarVista(event, "/views/adminSeasons.fxml");
    }

    public void handleBuildingMap(javafx.scene.input.MouseEvent event) { cambiarVista(event, "/views/adminBuildingMap.fxml"); }

    public void handleLogout(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        sistema.cerrarSesion();
        if (cambiarVista(event, "/views/login.fxml")) {
            stage.setResizable(false);
            stage.setWidth(1000);
            stage.setHeight(650);
            stage.centerOnScreen();
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
            alerta.setTitle("Error de navegación");
            alerta.setHeaderText("No se pudo cambiar de pantalla");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
            return false;
        }
    }

    private String limpiar(String valor) { return valor == null ? "" : valor.trim(); }

    private void limpiarFormulario() {
        documentField.clear(); nameField.clear(); phoneField.clear(); passwordField.clear();
        roleComboBox.setValue(RolUsuario.RECEPCIONISTA);
    }

    private void mostrarMensaje(String mensaje, boolean exito) {
        messageLabel.setText(mensaje);
        messageLabel.setStyle(exito ? "-fx-text-fill: #31845A;" : "-fx-text-fill: #C65D5D;");
    }

    private String obtenerInicial(String nombre) {
        if (nombre == null || nombre.isBlank()) return "A";
        return nombre.trim().substring(0, 1).toUpperCase(java.util.Locale.ROOT);
    }
}
