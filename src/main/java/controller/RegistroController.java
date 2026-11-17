package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DateCell;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import model.ApartaSuites;
import model.excepciones.ExcepcionSGA;
import model.modelo.Huesped;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import javafx.util.StringConverter;

public class RegistroController {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.forLanguageTag("es-CO"))
                    .withResolverStyle(ResolverStyle.STRICT);

    public TextField documentField;
    public TextField nameField;
    public DatePicker birthDatePicker;
    public TextField phoneField;
    public TextField emailField;
    public PasswordField passwordField;
    public PasswordField confirmPasswordField;
    public Label errorLabel;

    private ApartaSuites sistema;

    public void initialize() {
        sistema = ApartaSuites.getInstance();
        errorLabel.setText("");
        // Deshabilita en el calendario las fechas que no cumplen la edad mínima.
        LocalDate fechaMaximaNacimiento = LocalDate.now().minusYears(15);
        birthDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setDisable(vacia || fecha.isAfter(fechaMaximaNacimiento));
            }
        });
        birthDatePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate fecha) {
                return fecha == null ? "" : FORMATO_FECHA.format(fecha);
            }

            @Override
            public LocalDate fromString(String texto) {
                if (texto == null || texto.isBlank()) return null;
                return LocalDate.parse(texto.trim(), FORMATO_FECHA);
            }
        });
    }

    public void handleRegister(ActionEvent event) {
        String documento = texto(documentField);
        String nombre = texto(nameField);
        String telefono = texto(phoneField);
        String correo = texto(emailField);
        String contrasena = passwordField.getText();
        String confirmar = confirmPasswordField.getText();

        if (documento.isEmpty() || nombre.isEmpty()
                || contrasena == null || contrasena.isBlank()
                || confirmar == null || confirmar.isBlank()) {
            errorLabel.setText("Documento, nombre y contraseña son obligatorios.");
            return;
        }

        if (!contrasena.equals(confirmar)) {
            errorLabel.setText("Las contraseñas no coinciden.");
            confirmPasswordField.clear();
            return;
        }

        LocalDate fechaNacimiento;
        try {
            fechaNacimiento = obtenerFechaNacimiento();
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
            birthDatePicker.getEditor().requestFocus();
            return;
        }

        try {
            Huesped huesped = new Huesped(
                    documento,
                    nombre,
                    fechaNacimiento,
                    telefono,
                    correo,
                    contrasena
            );

            sistema.registrarHuesped(huesped);

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Registro exitoso");
            alerta.setHeaderText("¡Tu cuenta fue creada!");
            alerta.setContentText("Ya puedes iniciar sesión con tu documento y contraseña.");
            alerta.showAndWait();

            volverAlLogin(event);

        } catch (ExcepcionSGA | IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    public void handleBack(ActionEvent event) {
        volverAlLogin(event);
    }


    /**
     * Lee el texto visible del DatePicker y valida que la fecha exista
     * y que el titular tenga al menos 15 años al momento del registro.
     */
    private LocalDate obtenerFechaNacimiento() {
        String textoFecha = birthDatePicker.getEditor().getText();
        if (textoFecha == null || textoFecha.isBlank()) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }

        final LocalDate fecha;
        try {
            fecha = LocalDate.parse(textoFecha.trim(), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ingresa una fecha válida con formato dd/MM/aaaa.");
        }

        LocalDate hoy = LocalDate.now();
        if (fecha.isAfter(hoy)) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        }
        if (Period.between(fecha, hoy).getYears() < 15) {
            throw new IllegalArgumentException("Debes tener al menos 15 años para crear una cuenta.");
        }
        return fecha;
    }

    private String texto(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    private void volverAlLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException | RuntimeException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText("No se pudo volver al inicio de sesión");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }
}
