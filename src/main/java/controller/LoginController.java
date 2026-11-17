package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;

import model.ApartaSuites;
import model.enums.RolUsuario;

import java.io.IOException;

public class LoginController {

    public TextField usernameField;
    public PasswordField passwordField;
    public Label errorLabel;
    public Button loginButton;
    public Button registerButton;

    private ApartaSuites sistema;

    public void initialize() {
        sistema = ApartaSuites.getInstance();
        errorLabel.setText("");
    }

    public void handleLogin(ActionEvent event) {
        String documento = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String contrasena = passwordField.getText();

        if (documento.isEmpty() || contrasena == null || contrasena.isEmpty()) {
            errorLabel.setText("Ingresa tu documento y contraseña.");
            return;
        }

        if (!sistema.iniciarSesion(documento, contrasena)) {
            errorLabel.setText("Documento o contraseña incorrectos.");
            passwordField.clear();
            return;
        }

        RolUsuario rol = sistema.getRolAutenticado();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (rol == RolUsuario.ADMINISTRADOR) {
            if (cambiarVista(event, "/views/adminDashboard.fxml")) {
                stage.setResizable(true);
                stage.setWidth(1280);
                stage.setHeight(820);
                stage.centerOnScreen();
            }
            return;
        }

        if (rol == RolUsuario.RECEPCIONISTA) {
            if (cambiarVista(event, "/views/receptionDashboard.fxml")) {
                stage.setResizable(true);
                stage.setWidth(1440);
                stage.setHeight(900);
                stage.centerOnScreen();
            }
            return;
        }

        if (rol == RolUsuario.HUESPED) {
            if (cambiarVista(event, "/views/dasboardHuesped.fxml")) {
                stage.setResizable(true);
                stage.setWidth(1280);
                stage.setHeight(820);
                stage.centerOnScreen();
            }
            return;
        }

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Inicio de sesión");
        alerta.setHeaderText("¡Bienvenido a Aparta-Suites UQ!");
        alerta.setContentText("Sesión iniciada correctamente.\nRol: " + rol);
        alerta.showAndWait();
    }

    public void handleRegister(ActionEvent event) {
        cambiarVista(event, "/views/register.fxml");
    }

    private boolean cambiarVista(ActionEvent event, String ruta) {
        try {
            Node source = (Node) event.getSource();
            Scene scene = source.getScene();
            if (scene == null) throw new IllegalStateException("La pantalla actual no está asociada a una escena.");
            Parent root = FXMLLoader.load(getClass().getResource(ruta));
            scene.setRoot(root);
            return true;
        } catch (IOException | RuntimeException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText("No se pudo abrir la pantalla");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
            return false;
        }
    }
}
