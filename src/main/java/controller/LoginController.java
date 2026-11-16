package controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import model.ApartaSuites;
import model.enums.RolUsuario;

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

        String documento = usernameField.getText().trim();
        String contrasena = passwordField.getText();

        // Verificar que se ingresen ambos campos.
        if (documento.isEmpty() || contrasena == null || contrasena.isEmpty()) {
            errorLabel.setText("Ingresa tu documento y contraseña.");
            return;
        }

        // Validar las credenciales.
        boolean autenticado = sistema.iniciarSesion(documento, contrasena);

        if (!autenticado) {
            errorLabel.setText("Documento o contraseña incorrectos.");
            passwordField.clear();
            return;
        }

        // Identificar el rol del usuario autenticado.
        RolUsuario rol = sistema.getRolAutenticado();

        mostrarBienvenida(rol);
    }

    private void mostrarBienvenida(RolUsuario rol) {

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Inicio de sesión");
        alerta.setHeaderText("¡Inicio de sesión exitoso!");
        alerta.setContentText("Tu rol es: " + rol);
        alerta.showAndWait();
    }

    public void handleRegister(ActionEvent event) {
        // Lo conectaremos cuando creemos el formulario de registro.
    }
}