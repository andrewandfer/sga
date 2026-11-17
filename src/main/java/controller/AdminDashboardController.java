package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.ApartaSuites;
import model.enums.EstadoApartamento;
import model.interfaces.Usuario;
import model.modelo.Apartamento;
import model.modelo.Empleado;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AdminDashboardController {

    public Label adminNameLabel;
    public Label adminInitialLabel;
    public Label dateLabel;
    public Label totalApartmentsLabel;
    public Label preparedLabel;
    public Label occupiedLabel;
    public Label preparationLabel;
    public Label outOfServiceLabel;
    public Label totalGuestsLabel;
    public Label totalEmployeesLabel;
    public Label totalReservationsLabel;

    public TableView<Apartamento> apartmentsTable;
    public TableColumn<Apartamento, String> codeColumn;
    public TableColumn<Apartamento, String> nameColumn;
    public TableColumn<Apartamento, String> blockColumn;
    public TableColumn<Apartamento, String> floorColumn;
    public TableColumn<Apartamento, String> capacityColumn;
    public TableColumn<Apartamento, String> statusColumn;

    private ApartaSuites sistema;
    private final ObservableList<Apartamento> apartamentos = FXCollections.observableArrayList();

    public void initialize() {
        sistema = ApartaSuites.getInstance();
        configurarTabla();
        cargarDatos();

        Usuario usuario = sistema.getUsuarioAutenticado();
        String nombreAdministrador = usuario instanceof Empleado empleado
                ? empleado.getNombre() : "Administrador";
        adminNameLabel.setText(nombreAdministrador);
        adminInitialLabel.setText(obtenerInicial(nombreAdministrador));

        dateLabel.setText(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "CO"))));
    }

    private void configurarTabla() {
        codeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodigo()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        blockColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBloque()));
        floorColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getPiso())));
        capacityColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCapacidadMaxima() + " personas"));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(
                traducirEstado(data.getValue().getEstadoOperativo())));
        apartmentsTable.setItems(apartamentos);
    }

    private void cargarDatos() {
        int preparados = 0;
        int ocupados = 0;
        int pendientes = 0;
        int fueraServicio = 0;
        int reservas = 0;

        apartamentos.clear();
        for (Apartamento apartamento : sistema.listarApartamentos()) {
            apartamentos.add(apartamento);
            reservas += apartamento.numeroReservas();

            switch (apartamento.getEstadoOperativo()) {
                case PREPARADO -> preparados++;
                case OCUPADO -> ocupados++;
                case PENDIENTE_PREPARACION, EN_PREPARACION -> pendientes++;
                case FUERA_DE_SERVICIO -> fueraServicio++;
            }
        }

        totalApartmentsLabel.setText(String.valueOf(apartamentos.size()));
        preparedLabel.setText(String.valueOf(preparados));
        occupiedLabel.setText(String.valueOf(ocupados));
        preparationLabel.setText(String.valueOf(pendientes));
        outOfServiceLabel.setText(String.valueOf(fueraServicio));
        totalGuestsLabel.setText(String.valueOf(sistema.listarHuespedes().tamano()));
        totalEmployeesLabel.setText(String.valueOf(sistema.listarEmpleados().tamano()));
        totalReservationsLabel.setText(String.valueOf(reservas));
    }

    private String traducirEstado(EstadoApartamento estado) {
        return switch (estado) {
            case PREPARADO -> "Preparado";
            case OCUPADO -> "Ocupado";
            case PENDIENTE_PREPARACION -> "Pendiente de preparación";
            case EN_PREPARACION -> "En preparación";
            case FUERA_DE_SERVICIO -> "Fuera de servicio";
        };
    }

    public void handleRefresh() {
        cargarDatos();
    }

    public void handleApartments(MouseEvent event) {
        cambiarVista(event, "/views/adminApartments.fxml");
    }

    public void handleEmployees(MouseEvent event) {
        cambiarVista(event, "/views/adminEmployees.fxml");
    }

    public void handleGuests(javafx.scene.input.MouseEvent event) {
        cambiarVista(event, "/views/adminGuests.fxml");
    }

    public void handleSeasons(javafx.scene.input.MouseEvent event) {
        cambiarVista(event, "/views/adminSeasons.fxml");
    }

    public void handleBuildingMap(MouseEvent event) {
        cambiarVista(event, "/views/adminBuildingMap.fxml");
    }

    public void handleReports(MouseEvent event) {
        cambiarVista(event, "/views/adminReports.fxml");
    }

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
            e.printStackTrace();
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de navegación");
            alerta.setHeaderText("No se pudo cambiar de pantalla");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
            return false;
        }
    }

    private String obtenerInicial(String nombre) {
        if (nombre == null || nombre.isBlank()) return "A";
        return nombre.trim().substring(0, 1).toUpperCase(java.util.Locale.ROOT);
    }
}
