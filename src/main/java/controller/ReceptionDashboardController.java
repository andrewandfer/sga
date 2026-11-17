package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.ApartaSuites;
import model.enums.EstadoApartamento;
import model.enums.EstadoReserva;
import model.interfaces.Usuario;
import model.modelo.Apartamento;
import model.modelo.Empleado;
import model.modelo.Reserva;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ReceptionDashboardController {

    @FXML private Label employeeNameLabel;
    @FXML private Label employeeInitialLabel;
    @FXML private Label weekRangeLabel;
    @FXML private Label arrivalsLabel;
    @FXML private Label departuresLabel;
    @FXML private Label activeReservationsLabel;
    @FXML private Label preparedApartmentsLabel;
    @FXML private GridPane calendarGrid;
    @FXML private VBox calendarContent;
    @FXML private Label emptyCalendarLabel;

    private final ApartaSuites sistema = ApartaSuites.getInstance();
    private LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    private static final Locale LOCALE = new Locale("es", "CO");
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("d", LOCALE);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEE d", LOCALE);

    @FXML
    private void initialize() {
        Usuario usuario = sistema.getUsuarioAutenticado();
        String nombre = usuario instanceof Empleado empleado ? empleado.getNombre() : "Recepción";
        employeeNameLabel.setText(nombre);
        employeeInitialLabel.setText(nombre.isBlank() ? "R" : nombre.substring(0, 1).toUpperCase(LOCALE));
        actualizarVista();
    }

    @FXML
    private void previousWeek() {
        weekStart = weekStart.minusWeeks(1);
        actualizarCalendario();
    }

    @FXML
    private void nextWeek() {
        weekStart = weekStart.plusWeeks(1);
        actualizarCalendario();
    }

    @FXML
    private void currentWeek() {
        weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        actualizarVista();
    }

    @FXML
    private void refresh() {
        actualizarVista();
    }

    @FXML
    private void abrirPreparacion(ActionEvent event) {
        cambiarVista(event, "/views/receptionPreparation.fxml", true);
    }

    @FXML
    private void abrirCanalExterno(ActionEvent event) {
        cambiarVista(event, "/views/receptionExternalRequests.fxml", true);
    }

    @FXML
    private void nuevaReserva(ActionEvent event) {
        cambiarVista(event, "/views/receptionNewReservation.fxml", true);
    }

    @FXML
    private void logout(ActionEvent event) {
        sistema.cerrarSesion();
        cambiarVista(event, "/views/login.fxml", false);
    }

    private void actualizarVista() {
        actualizarResumen();
        actualizarCalendario();
    }

    private void actualizarResumen() {
        LocalDate hoy = LocalDate.now();
        int llegadas = 0;
        int salidas = 0;
        int activas = 0;
        int preparados = 0;

        for (Apartamento apartamento : sistema.listarApartamentos()) {
            if (apartamento.getEstadoOperativo() == EstadoApartamento.PREPARADO) preparados++;
            for (Reserva reserva : apartamento.getReservas()) {
                EstadoReserva estado = reserva.getEstado();
                if (estado == EstadoReserva.CANCELADA || estado == EstadoReserva.NO_SHOW) continue;
                if (reserva.getEstancia().getEntrada().equals(hoy)) llegadas++;
                if (reserva.getEstancia().getSalida().equals(hoy)) salidas++;
                if (reserva.esActiva()) activas++;
            }
        }

        arrivalsLabel.setText(String.valueOf(llegadas));
        departuresLabel.setText(String.valueOf(salidas));
        activeReservationsLabel.setText(String.valueOf(activas));
        preparedApartmentsLabel.setText(String.valueOf(preparados));
    }

    private void actualizarCalendario() {
        LocalDate weekEnd = weekStart.plusDays(7);
        weekRangeLabel.setText(weekStart.format(DateTimeFormatter.ofPattern("d MMM", LOCALE))
                + " – " + weekEnd.minusDays(1).format(DateTimeFormatter.ofPattern("d MMM yyyy", LOCALE)));
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        ColumnConstraints apartmentColumn = new ColumnConstraints();
        apartmentColumn.setPercentWidth(13);
        calendarGrid.getColumnConstraints().add(apartmentColumn);
        for (int i = 0; i < 7; i++) {
            ColumnConstraints dayColumn = new ColumnConstraints();
            dayColumn.setPercentWidth(87.0 / 7.0);
            dayColumn.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(dayColumn);
        }

        Label apartmentHeading = new Label("APARTAMENTO");
        apartmentHeading.getStyleClass().add("calendar-corner");
        calendarGrid.add(apartmentHeading, 0, 0);
        for (int day = 0; day < 7; day++) {
            LocalDate date = weekStart.plusDays(day);
            VBox heading = new VBox(3);
            heading.setAlignment(Pos.CENTER);
            heading.getStyleClass().add("calendar-day-heading");
            Label dayName = new Label(date.format(DateTimeFormatter.ofPattern("EEE", LOCALE)).toUpperCase(LOCALE));
            dayName.getStyleClass().add("calendar-day-name");
            Label dayNumber = new Label(date.format(DAY_FORMAT));
            dayNumber.getStyleClass().add("calendar-day-number");
            if (date.equals(LocalDate.now())) {
                heading.getStyleClass().add("calendar-today");
            }
            heading.getChildren().addAll(dayName, dayNumber);
            calendarGrid.add(heading, day + 1, 0);
        }

        List<Apartamento> apartamentos = new ArrayList<>();
        for (Apartamento apartamento : sistema.listarApartamentos()) apartamentos.add(apartamento);
        apartamentos.sort(Comparator.comparing(Apartamento::getCodigo));

        int row = 1;
        for (Apartamento apartamento : apartamentos) {
            Label apartment = new Label(apartamento.getCodigo());
            apartment.getStyleClass().add("apartment-row-label");
            apartment.setMaxWidth(Double.MAX_VALUE);
            calendarGrid.add(apartment, 0, row);

            for (int day = 0; day < 7; day++) {
                Region cell = new Region();
                cell.getStyleClass().add("calendar-cell");
                cell.setMinHeight(66);
                cell.setMaxHeight(66);
                calendarGrid.add(cell, day + 1, row);
            }

            for (Reserva reserva : apartamento.getReservas()) {
                if (!reserva.ocupaNoches()) continue;
                LocalDate entrada = reserva.getEstancia().getEntrada();
                LocalDate salida = reserva.getEstancia().getSalida();
                if (!entrada.isBefore(weekEnd) || !salida.isAfter(weekStart)) continue;

                int startColumn = Math.max(0, (int) (entrada.toEpochDay() - weekStart.toEpochDay()));
                int endColumn = Math.min(7, (int) (salida.toEpochDay() - weekStart.toEpochDay()));
                int span = Math.max(1, endColumn - startColumn);

                Button booking = crearBloqueReserva(reserva);
                calendarGrid.add(booking, startColumn + 1, row);
                GridPane.setColumnSpan(booking, span);
                GridPane.setMargin(booking, new Insets(7, 3, 7, 3));
                GridPane.setHgrow(booking, Priority.ALWAYS);
            }
            row++;
        }

        emptyCalendarLabel.setVisible(apartamentos.isEmpty());
        emptyCalendarLabel.setManaged(apartamentos.isEmpty());
    }

    private Button crearBloqueReserva(Reserva reserva) {
        String nombre = reserva.getTitular().getNombre();
        String texto = reserva.getId() + " · " + nombre;
        Button bloque = new Button(texto);
        bloque.setMaxWidth(Double.MAX_VALUE);
        bloque.setMaxHeight(Double.MAX_VALUE);
        bloque.setMinHeight(50);
        bloque.setWrapText(true);
        bloque.getStyleClass().add("booking-block");
        bloque.getStyleClass().add(claseEstado(reserva.getEstado()));
        bloque.setOnAction(event -> mostrarDetalleReserva(reserva));
        return bloque;
    }

    private String claseEstado(EstadoReserva estado) {
        return switch (estado) {
            case PENDIENTE -> "booking-pending";
            case CONFIRMADA -> "booking-confirmed";
            case EN_CURSO -> "booking-current";
            case FINALIZADA -> "booking-finished";
            case CANCELADA -> "booking-cancelled";
            case NO_SHOW -> "booking-noshow";
        };
    }

    private void mostrarDetalleReserva(Reserva reserva) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/receptionReservationDetail.fxml"));
            Parent root = loader.load();
            ReceptionReservationDetailController controller = loader.getController();
            controller.setReserva(reserva);
            controller.setReturnWeekStart(weekStart);

            Scene scene = calendarGrid.getScene();
            scene.setRoot(root);
            Stage stage = (Stage) scene.getWindow();
            stage.setResizable(true);
            stage.setWidth(1440);
            stage.setHeight(900);
            stage.centerOnScreen();
        } catch (IOException | RuntimeException ex) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de navegación");
            alerta.setHeaderText("No se pudo abrir la reserva");
            alerta.setContentText(ex.getMessage() == null
                    ? "Ocurrió un error al cargar la ficha de la reserva." : ex.getMessage());
            alerta.showAndWait();
        }
    }

    public void setWeekStart(LocalDate weekStart) {
        if (weekStart != null) {
            this.weekStart = weekStart;
            actualizarVista();
        }
    }

    private void cambiarVista(ActionEvent event, String ruta, boolean ampliar) {
        try {
            Node source = (Node) event.getSource();
            Scene scene = source.getScene();
            Parent root = FXMLLoader.load(getClass().getResource(ruta));
            scene.setRoot(root);
            Stage stage = (Stage) scene.getWindow();
            if (ampliar) {
                stage.setResizable(true);
                stage.setWidth(1440);
                stage.setHeight(900);
            } else {
                stage.setResizable(false);
                stage.setWidth(1000);
                stage.setHeight(650);
            }
            stage.centerOnScreen();
        } catch (IOException | RuntimeException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de navegación");
            alerta.setHeaderText("No se pudo cambiar de pantalla");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }
}
