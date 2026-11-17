package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

import model.ApartaSuites;

import java.net.URL;

/** Utilidades comunes para la navegación del portal del huésped. */
public final class NavegacionHuesped {

    private NavegacionHuesped() { }

    public static void abrir(ActionEvent event, String ruta) {
        try {
            if (event == null || !(event.getSource() instanceof Node origen)) {
                throw new IllegalStateException("No se pudo identificar el control que inició la navegación.");
            }
            Scene escena = origen.getScene();
            if (escena == null) {
                throw new IllegalStateException("La vista actual no está asociada a una escena.");
            }
            URL recurso = NavegacionHuesped.class.getResource(ruta);
            if (recurso == null) {
                throw new IllegalStateException("No se encontró la vista: " + ruta);
            }
            Parent raiz = new FXMLLoader(recurso).load();
            escena.setRoot(raiz);
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir la pantalla",
                    ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
        }
    }

    public static void cerrarSesion(ActionEvent event) {
        ApartaSuites.getInstance().cerrarSesion();
        abrir(event, "/views/login.fxml");
    }

    public static void contactar() {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Atención al huésped",
                "Para recibir ayuda con tu alojamiento, comunícate con recepción de Aparta-Suites UQ.");
    }

    private static void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje == null || mensaje.isBlank()
                ? "Ocurrió un error inesperado." : mensaje);
        alerta.showAndWait();
    }
}
