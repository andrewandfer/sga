
package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import model.ApartaSuites;
import model.modelo.Huesped;
import model.modelo.Reserva;
import model.enums.EstadoReserva;

public class HuespedController {

    @FXML
    private Label lblBienvenida;

    @FXML
    private Label lblNombreUsuario;

    @FXML
    private Label lblCantidadReservas;

    @FXML
    private Label lblCantidadFavoritos;

    @FXML
    private Label lblCantidadHistorial;

    private final ApartaSuites sistema = ApartaSuites.getInstance();

    @FXML
    public void initialize() {
        cargarDatosHuesped();
    }

    private void cargarDatosHuesped() {

        if (!(sistema.getUsuarioAutenticado() instanceof Huesped)) {
            lblBienvenida.setText("¡Bienvenido!");
            lblNombreUsuario.setText("Huésped");

            lblCantidadReservas.setText("0");
            lblCantidadFavoritos.setText("0");
            lblCantidadHistorial.setText("0");

            return;
        }

        Huesped huesped = (Huesped) sistema.getUsuarioAutenticado();

        String nombre = huesped.getNombre();

        lblBienvenida.setText("¡Bienvenido, " + nombre + "!");
        lblNombreUsuario.setText(nombre);

        int cantidadReservas = huesped.getHistorial().tamano();

        int cantidadFavoritos = huesped.getFavoritos().tamano();

        int cantidadHistorial = 0;

        for (Reserva reserva : huesped.getHistorial()) {
            if (reserva.getEstado() == EstadoReserva.FINALIZADA) {
                cantidadHistorial++;
            }
        }

        lblCantidadReservas.setText(String.valueOf(cantidadReservas));
        lblCantidadFavoritos.setText(String.valueOf(cantidadFavoritos));
        lblCantidadHistorial.setText(String.valueOf(cantidadHistorial));
    }
}

