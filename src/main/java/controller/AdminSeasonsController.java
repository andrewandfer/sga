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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.ApartaSuites;
import model.enums.Temporada;
import model.interfaces.Usuario;
import model.modelo.Empleado;
import model.modelo.PeriodoTemporada;

import java.io.IOException;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class AdminSeasonsController {
    public Label adminNameLabel;
    public Label adminInitialLabel;
    public Label periodCountLabel;
    public Label messageLabel;
    public ComboBox<Temporada> seasonComboBox;
    public ComboBox<Month> startMonthComboBox;
    public ComboBox<Month> endMonthComboBox;
    public TableView<PeriodoTemporada> periodsTable;
    public TableColumn<PeriodoTemporada, String> periodSeasonColumn;
    public TableColumn<PeriodoTemporada, String> periodStartColumn;
    public TableColumn<PeriodoTemporada, String> periodEndColumn;

    private final ApartaSuites sistema = ApartaSuites.getInstance();
    private final ObservableList<PeriodoTemporada> periodos = FXCollections.observableArrayList();
    private static final Locale ESPANOL = Locale.forLanguageTag("es-CO");

    public void initialize() {
        Usuario usuario = sistema.getUsuarioAutenticado();
        String nombre = usuario instanceof Empleado empleado ? empleado.getNombre() : "Administrador";
        adminNameLabel.setText(nombre);
        adminInitialLabel.setText(nombre == null || nombre.isBlank() ? "A" : nombre.trim().substring(0, 1).toUpperCase(ESPANOL));

        seasonComboBox.setItems(FXCollections.observableArrayList(Temporada.ALTA, Temporada.MEDIA));
        startMonthComboBox.setItems(FXCollections.observableArrayList());
        endMonthComboBox.setItems(FXCollections.observableArrayList());
        javafx.util.StringConverter<Month> conversorMes = new javafx.util.StringConverter<>() {
            @Override public String toString(Month mes) {
                return mes == null ? "" : mes.getDisplayName(TextStyle.FULL, ESPANOL).substring(0, 1).toUpperCase(ESPANOL)
                        + mes.getDisplayName(TextStyle.FULL, ESPANOL).substring(1);
            }
            @Override public Month fromString(String texto) { return null; }
        };
        startMonthComboBox.setConverter(conversorMes);
        endMonthComboBox.setConverter(conversorMes);
        startMonthComboBox.setOnAction(event -> actualizarMesesFinales());

        periodSeasonColumn.setCellValueFactory(c -> new SimpleStringProperty(nombreTemporada(c.getValue().getTemporada())));
        periodStartColumn.setCellValueFactory(c -> new SimpleStringProperty(nombreMes(c.getValue().getMesInicio())));
        periodEndColumn.setCellValueFactory(c -> new SimpleStringProperty(nombreMes(c.getValue().getMesFin())));
        periodsTable.setItems(periodos);
        cargarPeriodos();
        actualizarMesesIniciales();
        messageLabel.setText("");
    }

    private String nombreTemporada(Temporada temporada) {
        return temporada.name().substring(0, 1) + temporada.name().substring(1).toLowerCase(ESPANOL);
    }

    private String nombreMes(Month mes) {
        String nombre = mes.getDisplayName(TextStyle.FULL, ESPANOL);
        return nombre.substring(0, 1).toUpperCase(ESPANOL) + nombre.substring(1);
    }

    private void cargarPeriodos() {
        periodos.clear();
        for (PeriodoTemporada periodo : sistema.getPeriodos()) {
            // Los periodos antiguos de temporada baja se ignoran: baja es el valor por defecto.
            if (periodo.getTemporada() != Temporada.BAJA) periodos.add(periodo);
        }
        periodCountLabel.setText(periodos.size() + (periodos.size() == 1 ? " PERIODO" : " PERIODOS"));
        actualizarMesesIniciales();
    }

    /** Los meses de temporada alta/media ya asignados no vuelven a ofrecerse. */
    private boolean mesOcupado(Month mes) {
        for (PeriodoTemporada periodo : periodos) {
            if (periodo.contiene(java.time.LocalDate.of(2000, mes, 1))) return true;
        }
        return false;
    }

    private void actualizarMesesIniciales() {
        ObservableList<Month> disponibles = FXCollections.observableArrayList();
        for (Month mes : Month.values()) if (!mesOcupado(mes)) disponibles.add(mes);
        startMonthComboBox.setItems(disponibles);
        startMonthComboBox.setValue(null);
        endMonthComboBox.setItems(FXCollections.observableArrayList());
        endMonthComboBox.setValue(null);
    }

    /** Solo ofrece meses libres consecutivos desde el mes inicial; permite cruzar diciembre. */
    private void actualizarMesesFinales() {
        Month inicio = startMonthComboBox.getValue();
        ObservableList<Month> disponibles = FXCollections.observableArrayList();
        if (inicio != null) {
            for (int desplazamiento = 0; desplazamiento < 12; desplazamiento++) {
                int numeroMes = ((inicio.getValue() - 1 + desplazamiento) % 12) + 1;
                Month candidato = Month.of(numeroMes);
                if (mesOcupado(candidato)) break;
                disponibles.add(candidato);
            }
        }
        endMonthComboBox.setItems(disponibles);
        endMonthComboBox.setValue(null);
    }

    public void handleSavePeriod() {
        Temporada temporada = seasonComboBox.getValue();
        Month inicio = startMonthComboBox.getValue();
        Month fin = endMonthComboBox.getValue();
        if (temporada == null || inicio == null || fin == null) {
            mostrarMensaje("Selecciona temporada, mes inicial y mes final.", false);
            return;
        }
        if (temporada == Temporada.BAJA) {
            mostrarMensaje("La temporada baja se asigna automáticamente a los meses restantes.", false);
            return;
        }
        if (!endMonthComboBox.getItems().contains(fin)) {
            mostrarMensaje("El intervalo debe contener únicamente meses disponibles y consecutivos.", false);
            return;
        }
        try {
            sistema.definirPeriodo(new PeriodoTemporada(inicio, fin, temporada));
            seasonComboBox.setValue(null);
            startMonthComboBox.setValue(null);
            endMonthComboBox.setValue(null);
            cargarPeriodos();
            mostrarMensaje("Temporada registrada. Los meses asignados ya no estarán disponibles y se repetirán cada año.", true);
        } catch (Exception e) {
            mostrarMensaje(e.getMessage(), false);
        }
    }

    public void handleDeletePeriod() {
        PeriodoTemporada seleccionado = periodsTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje("Selecciona una temporada para eliminar.", false);
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar temporada");
        confirmacion.setHeaderText("¿Eliminar el periodo seleccionado?");
        confirmacion.setContentText("Los meses volverán a considerarse temporada baja, salvo que pertenezcan a otro periodo.");
        if (confirmacion.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK) {
            sistema.eliminarPeriodo(seleccionado);
            cargarPeriodos();
            mostrarMensaje("Temporada eliminada.", true);
        }
    }

    private void mostrarMensaje(String mensaje, boolean correcto) {
        messageLabel.setText(mensaje == null ? "No fue posible completar la operación." : mensaje);
        messageLabel.getStyleClass().removeAll("success-message", "error-label");
        messageLabel.getStyleClass().add(correcto ? "success-message" : "error-label");
    }

    public void handleDashboard(javafx.scene.input.MouseEvent e) { cambiarVista(e, "/views/adminDashboard.fxml"); }
    public void handleApartments(javafx.scene.input.MouseEvent e) { cambiarVista(e, "/views/adminApartments.fxml"); }
    public void handleGuests(javafx.scene.input.MouseEvent e) { cambiarVista(e, "/views/adminGuests.fxml"); }
    public void handleEmployees(javafx.scene.input.MouseEvent e) { cambiarVista(e, "/views/adminEmployees.fxml"); }

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
}
