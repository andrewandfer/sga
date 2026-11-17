package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import model.ApartaSuites;
import model.interfaces.Usuario;
import model.modelo.Huesped;

import java.time.format.DateTimeFormatter;

public class PerfilController {

    @FXML private Label nombreBanner;
    @FXML private Label nombreHuesped;

    @FXML private TextField campoNombres;
    @FXML private TextField campoApellidos;
    @FXML private TextField campoDocumento;
    @FXML private TextField campoCorreo;
    @FXML private TextField campoTelefono;
    @FXML private TextField campoFechaNacimiento;

    @FXML private Label tipoUsuario;
    @FXML private Label estadoCuenta;
    @FXML private Label totalReservas;

    private final ApartaSuites sistema = ApartaSuites.getInstance();

    @FXML
    private void initialize() {
        cargarPerfil();
    }

    private void cargarPerfil() {

        Usuario usuario = sistema.getUsuarioAutenticado();

        if (!(usuario instanceof Huesped)) {
            mostrarError("No hay una sesión de huésped activa.");
            return;
        }

        Huesped huesped = (Huesped) usuario;

        String nombre = valorSeguro(huesped.getNombre());

        nombreBanner.setText(nombre);
        nombreHuesped.setText(nombre);

        // El modelo proporciona un único campo de nombre.
        campoNombres.setText(nombre);
        campoApellidos.setText("No disponible por separado");

        campoDocumento.setText(
                valorSeguro(huesped.getDocumento())
        );

        campoCorreo.setText(
                valorSeguro(huesped.getCorreo())
        );

        campoTelefono.setText(
                valorSeguro(huesped.getTelefono())
        );

        campoFechaNacimiento.setText(
                huesped.getFechaNacimiento() == null
                        ? "No registrado"
                        : huesped.getFechaNacimiento().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                )
        );

        tipoUsuario.setText("Huésped");
        estadoCuenta.setText("No disponible");

        totalReservas.setText(
                String.valueOf(huesped.getHistorial().tamano())
        );
    }

    private String valorSeguro(String valor) {
        return valor == null || valor.isBlank()
                ? "No registrado"
                : valor;
    }

    private void mostrarError(String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Mi perfil");
        alerta.setHeaderText("No se pudo cargar el perfil");
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
