package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import model.ApartaSuites;
import model.enums.EstadoReserva;
import model.enums.Temporada;
import model.modelo.*;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AdminReportsController {
    public DatePicker occupancyFromPicker;
    public DatePicker occupancyToPicker;
    public DatePicker incomeFromPicker;
    public DatePicker incomeToPicker;
    public Label occupancySummaryLabel;
    public Label incomePaymentsLabel;
    public Label incomeRefundsLabel;
    public Label incomeVoidsLabel;
    public Label incomeNetLabel;
    public Label conflictCountLabel;
    public Label preparationAverageLabel;
    public Label preparationCountLabel;
    public Label preparationInfoLabel;
    public TableView<ReporteOcupacion> occupancyTable;
    public TableColumn<ReporteOcupacion, String> occupancyApartmentColumn;
    public TableColumn<ReporteOcupacion, String> occupancySeasonColumn;
    public TableColumn<ReporteOcupacion, String> occupancyNightsColumn;
    public TableColumn<ReporteOcupacion, String> occupancyPercentColumn;
    public TableView<ReporteReservasCanal> channelTable;
    public TableColumn<ReporteReservasCanal, String> channelColumn;
    public TableColumn<ReporteReservasCanal, String> channelTotalColumn;
    public TableColumn<ReporteReservasCanal, String> channelPendingColumn;
    public TableColumn<ReporteReservasCanal, String> channelConfirmedColumn;
    public TableColumn<ReporteReservasCanal, String> channelInProgressColumn;
    public TableColumn<ReporteReservasCanal, String> channelFinishedColumn;
    public TableColumn<ReporteReservasCanal, String> channelCancelledColumn;
    public TableColumn<ReporteReservasCanal, String> channelNoShowColumn;
    public TableView<SolicitudReservaExterna> conflictsTable;
    public TableColumn<SolicitudReservaExterna, String> conflictIdColumn;
    public TableColumn<SolicitudReservaExterna, String> conflictDateColumn;
    public TableColumn<SolicitudReservaExterna, String> conflictApartmentColumn;
    public TableColumn<SolicitudReservaExterna, String> conflictReasonColumn;

    private final ApartaSuites sistema = ApartaSuites.getInstance();
    private final ObservableList<ReporteOcupacion> ocupacion = FXCollections.observableArrayList();
    private final ObservableList<ReporteReservasCanal> canales = FXCollections.observableArrayList();
    private final ObservableList<SolicitudReservaExterna> conflictos = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat COP = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    public void initialize() {
        configurarTablas();
        LocalDate hoy = LocalDate.now();
        occupancyFromPicker.setValue(hoy.withDayOfMonth(1));
        occupancyToPicker.setValue(hoy.plusDays(1));
        incomeFromPicker.setValue(hoy.withDayOfMonth(1));
        incomeToPicker.setValue(hoy.plusDays(1));
        actualizarReportes();
        cargarReportesPermanentes();
    }

    private void configurarTablas() {
        occupancyApartmentColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigoApartamento()));
        occupancySeasonColumn.setCellValueFactory(d -> new SimpleStringProperty(nombreTemporada(d.getValue().getTemporada())));
        occupancyNightsColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNochesOcupadas() + " / " + d.getValue().getNochesDisponibles()));
        occupancyPercentColumn.setCellValueFactory(d -> new SimpleStringProperty(
                String.format(Locale.forLanguageTag("es-CO"), "%.2f%%", d.getValue().getPorcentajeOcupacion())));
        occupancyTable.setItems(ocupacion);

        channelColumn.setCellValueFactory(d -> new SimpleStringProperty(nombreCanal(d.getValue())));
        channelTotalColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getTotal())));
        channelPendingColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCantidad(EstadoReserva.PENDIENTE))));
        channelConfirmedColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCantidad(EstadoReserva.CONFIRMADA))));
        channelInProgressColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCantidad(EstadoReserva.EN_CURSO))));
        channelFinishedColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCantidad(EstadoReserva.FINALIZADA))));
        channelCancelledColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCantidad(EstadoReserva.CANCELADA))));
        channelNoShowColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCantidad(EstadoReserva.NO_SHOW))));
        channelTable.setItems(canales);

        conflictIdColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        conflictDateColumn.setCellValueFactory(d -> new SimpleStringProperty(DATE_TIME.format(d.getValue().getRecibidaEn())));
        conflictApartmentColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApartamento().getCodigo()));
        conflictReasonColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMotivoRechazo()));
        conflictsTable.setItems(conflictos);
    }


    private void actualizarReportes() {
        try {
            LocalDate desdeOcupacion = occupancyFromPicker.getValue();
            LocalDate hastaOcupacion = occupancyToPicker.getValue();
            ocupacion.clear();
            for (ReporteOcupacion fila : sistema.reporteOcupacion(desdeOcupacion, hastaOcupacion)) ocupacion.add(fila);
            int noches = 0;
            for (ReporteOcupacion fila : ocupacion) noches += fila.getNochesOcupadas();
            occupancySummaryLabel.setText(ocupacion.isEmpty() ? "No hay datos para el periodo seleccionado"
                    : "Noches ocupadas registradas: " + noches + " · " + ocupacion.size() + " combinaciones apartamento/temporada");

            ReporteIngresos ingresos = sistema.reporteIngresos(incomeFromPicker.getValue(), incomeToPicker.getValue());
            incomePaymentsLabel.setText(COP.format(ingresos.getPagos()));
            incomeRefundsLabel.setText(COP.format(ingresos.getDevoluciones()));
            incomeVoidsLabel.setText(COP.format(ingresos.getAnulacionesPagos()));
            incomeNetLabel.setText(COP.format(ingresos.getIngresosNetos()));
        } catch (RuntimeException ex) {
            mostrarError("No se pudieron actualizar los reportes", ex.getMessage());
        }
    }

    private void cargarReportesPermanentes() {
        canales.clear();
        for (ReporteReservasCanal fila : sistema.reporteReservasPorCanal()) canales.add(fila);
        conflictos.clear();
        for (SolicitudReservaExterna solicitud : sistema.getConflictosCanalExterno()) conflictos.add(solicitud);
        conflictCountLabel.setText(String.valueOf(conflictos.size()));

        int finalizadas = sistema.getCantidadPreparacionesFinalizadas();
        preparationCountLabel.setText(String.valueOf(finalizadas));
        if (finalizadas == 0) {
            preparationAverageLabel.setText("Sin datos");
            preparationInfoLabel.setText("Aún no hay preparaciones finalizadas registradas.");
        } else {
            preparationAverageLabel.setText(String.format(Locale.forLanguageTag("es-CO"), "%.1f min",
                    sistema.promedioMinutosPreparacion()));
            preparationInfoLabel.setText("Promedio global calculado con " + finalizadas + " preparaciones finalizadas.");
        }
    }

    public void handleUpdateOccupancy(ActionEvent event) { actualizarReportes(); }
    public void handleUpdateIncome(ActionEvent event) { actualizarReportes(); }
    public void handleRefresh(ActionEvent event) { actualizarReportes(); cargarReportesPermanentes(); }
    public void handleDashboard(javafx.scene.input.MouseEvent event) { cambiarVista(event, "/views/adminDashboard.fxml"); }
    public void handleApartments(javafx.scene.input.MouseEvent event) { cambiarVista(event, "/views/adminApartments.fxml"); }
    public void handleGuests(javafx.scene.input.MouseEvent event) { cambiarVista(event, "/views/adminGuests.fxml"); }
    public void handleEmployees(javafx.scene.input.MouseEvent event) { cambiarVista(event, "/views/adminEmployees.fxml"); }
    public void handleSeasons(javafx.scene.input.MouseEvent event) { cambiarVista(event, "/views/adminSeasons.fxml"); }
    public void handleBuildingMap(javafx.scene.input.MouseEvent event) { cambiarVista(event, "/views/adminBuildingMap.fxml"); }
    public void handleLogout(ActionEvent event) {
        sistema.cerrarSesion();
        cambiarVista(event, "/views/login.fxml");
    }

    private boolean cambiarVista(javafx.event.Event event, String ruta) {
        try {
            Scene scene = ((Node) event.getSource()).getScene();
            Parent root = FXMLLoader.load(getClass().getResource(ruta));
            scene.setRoot(root);
            return true;
        } catch (IOException | RuntimeException ex) {
            mostrarError("No se pudo cambiar de pantalla", ex.getMessage());
            return false;
        }
    }

    private void mostrarError(String encabezado, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Reportes administrativos");
        alerta.setHeaderText(encabezado);
        alerta.setContentText(mensaje == null ? "Error inesperado" : mensaje);
        alerta.showAndWait();
    }

    private String nombreTemporada(Temporada temporada) {
        return switch (temporada) { case BAJA -> "Baja"; case MEDIA -> "Media"; case ALTA -> "Alta"; };
    }
    private String nombreCanal(ReporteReservasCanal reporte) {
        return switch (reporte.getCanal()) { case PORTAL -> "Portal"; case DIRECTA -> "Directa"; case EXTERNA -> "Externa"; };
    }
}
