
package controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    public TextField usernameField;
    public PasswordField passwordField;
    public Label errorLabel;
    public Button loginButton;
    public Button registerButton;

    public void initialize() {
        errorLabel.setText("");
    }

    public void handleLogin(ActionEvent event) {

    }

    public void handleRegister(ActionEvent event) {

    }
}
