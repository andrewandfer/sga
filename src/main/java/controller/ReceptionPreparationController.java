package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.ApartaSuites;
import model.estructuras.Grafo.NodoGrafo;
import model.modelo.Apartamento;
import model.modelo.Empleado;
import model.modelo.SolicitudPreparacion;
import model.interfaces.Usuario;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Panel operativo de la cola priorizada de preparación. */
public class ReceptionPreparationController {

    @FXML private Label employeeNameLabel;
    @FXML private Label employeeInitialLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label finishedCountLabel;
    @FXML private Label averageTimeLabel;
    @FXML private Label activeApartmentLabel;
    @FXML private Label activeSinceLabel;
    @FXML private Button finishCurrentButton;
    @FXML private Label routeInfoLabel;
    @FXML private VBox pendingList;
    @FXML private VBox routeList;
    @FXML private Label emptyPendingLabel;
    @FXML private Label emptyRouteLabel;

    private final ApartaSuites sistema = ApartaSuites.getInstance();
    private static final Locale LOCALE = new Locale("es", "CO");
    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", LOCALE);

    @FXML
    private void initialize() {
        Usuario usuario = sistema.getUsuarioAutenticado();
        String nombre = usuario instanceof Empleado empleado ? empleado.getNombre() : "Recepción";
        employeeNameLabel.setText(nombre);
        employeeInitialLabel.setText(nombre == null || nombre.isBlank() ? "R" : nombre.trim().substring(0, 1).toUpperCase(LOCALE));
        actualizarPanel();
    }

    @FXML
    private void actualizar() {
        actualizarPanel();
    }

    @FXML
    private void iniciarSiguiente() {
        try {
            SolicitudPreparacion iniciada = sistema.iniciarSiguientePreparacion(LocalDateTime.now());
            if (iniciada == null) {
                mostrarInfo("Cola vacía", "No hay apartamentos pendientes de preparación.");
            }
            actualizarPanel();
        } catch (RuntimeException ex) {
            mostrarError("No se pudo iniciar la preparación", ex.getMessage());
        }
    }

    @FXML
    private void finalizarActual() {
        try {
            SolicitudPreparacion finalizada = sistema.finalizarPreparacionActual(LocalDateTime.now());
            mostrarInfo("Preparación finalizada", "El apartamento "
                    + finalizada.getApartamento().getCodigo() + " quedó Preparado.");
            actualizarPanel();
        } catch (RuntimeException ex) {
            mostrarError("No se pudo finalizar", ex.getMessage());
        }
    }

    private void actualizarPanel() {
        pendingList.getChildren().clear();
        int pendientes = 0;
        for (SolicitudPreparacion solicitud : sistema.listarPreparacionesPendientes()) {
            pendingList.getChildren().add(crearTarjetaPendiente(solicitud));
            pendientes++;
        }
        pendingCountLabel.setText(String.valueOf(pendientes));
        emptyPendingLabel.setVisible(pendientes == 0);
        emptyPendingLabel.setManaged(pendientes == 0);

        SolicitudPreparacion actual = sistema.obtenerPreparacionActual();
        if (actual == null) {
            activeApartmentLabel.setText("Sin preparación en curso");
            activeSinceLabel.setText("Inicia el siguiente apartamento de la cola para comenzar.");
            finishCurrentButton.setVisible(false);
            finishCurrentButton.setManaged(false);
        } else {
            activeApartmentLabel.setText(actual.getApartamento().getCodigo() + " · " + actual.getApartamento().getNombre());
            activeSinceLabel.setText("En preparación desde: " + (actual.getInicioPreparacion() == null
                    ? "—" : actual.getInicioPreparacion().format(HOUR_FORMAT)));
            finishCurrentButton.setVisible(true);
            finishCurrentButton.setManaged(true);
        }

        finishedCountLabel.setText(String.valueOf(sistema.getCantidadPreparacionesFinalizadas()));
        averageTimeLabel.setText(String.format(LOCALE, "%.0f min", sistema.promedioMinutosPreparacion()));
        actualizarRuta();
    }

    private VBox crearTarjetaPendiente(SolicitudPreparacion solicitud) {
        Apartamento apartamento = solicitud.getApartamento();
        VBox card = new VBox(5);
        card.getStyleClass().add("queue-card");
        Label codigo = new Label(apartamento.getCodigo() + " · " + apartamento.getNombre());
        codigo.getStyleClass().add("queue-title");
        Label ubicacion = new Label("Bloque " + apartamento.getBloque() + " · Piso " + apartamento.getPiso());
        ubicacion.getStyleClass().add("queue-detail");
        Label prioridad = new Label(solicitud.getPrioridad() == 1
                ? "PRIORIDAD 1 · ENTRADA PROGRAMADA HOY"
                : "PRIORIDAD 2 · SIN ENTRADA PARA HOY");
        prioridad.getStyleClass().add(solicitud.getPrioridad() == 1 ? "priority-high" : "priority-normal");
        String llegada = solicitud.getLlegadaEstimada() == null ? "Sin hora estimada"
                : "Próxima llegada: " + solicitud.getLlegadaEstimada().format(HOUR_FORMAT);
        Label detalleLlegada = new Label(llegada);
        detalleLlegada.getStyleClass().add("queue-detail");
        card.getChildren().addAll(codigo, ubicacion, prioridad, detalleLlegada);
        return card;
    }

    private void actualizarRuta() {
        routeList.getChildren().clear();
        if (sistema.cantidadPreparacionesPendientes() == 0) {
            emptyRouteLabel.setVisible(true);
            emptyRouteLabel.setManaged(true);
            routeInfoLabel.setText("No hay apartamentos pendientes para incluir en la ruta.");
            return;
        }
        try {
            int paso = 1;
            for (NodoGrafo<Object> nodo : sistema.rutaPreparacionesPendientes()) {
                Object contenido = nodo.getContenido();
                String nombre;
                if (contenido instanceof Apartamento apartamento) {
                    nombre = "Apartamento " + apartamento.getCodigo() + " · " + apartamento.getNombre();
                } else if (contenido == null) {
                    nombre = "Punto del edificio";
                } else {
                    String tipo = contenido.getClass().getSimpleName();
                    nombre = switch (tipo) {
                        case "Recepcion" -> "Recepción";
                        case "Escaleras" -> "Escaleras";
                        case "CuartosAseo" -> "Cuarto de aseo";
                        case "Pasillo" -> "Pasillo";
                        default -> contenido.toString();
                    };
                }
                Label parada = new Label(paso++ + ".  " + nombre);
                parada.getStyleClass().add("route-stop");
                routeList.getChildren().add(parada);
            }
            boolean vacia = routeList.getChildren().isEmpty();
            emptyRouteLabel.setVisible(vacia);
            emptyRouteLabel.setManaged(vacia);
            routeInfoLabel.setText(vacia ? "No hay una ruta pendiente por recorrer."
                    : "Recorrido sugerido desde recepción, respetando la prioridad de la cola.");
        } catch (RuntimeException ex) {
            emptyRouteLabel.setVisible(true);
            emptyRouteLabel.setManaged(true);
            emptyRouteLabel.setText("No fue posible calcular la ruta: " + ex.getMessage());
            routeInfoLabel.setText("Revisa la conectividad del edificio en el mapa.");
        }
    }

    @FXML
    private void volverCalendario(ActionEvent event) {
        cambiarVista(event, "/views/receptionDashboard.fxml");
    }

    @FXML
    private void abrirCanalExterno(ActionEvent event) {
        cambiarVista(event, "/views/receptionExternalRequests.fxml");
    }

    @FXML
    private void volver(ActionEvent event) {
        cambiarVista(event, "/views/receptionDashboard.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        sistema.cerrarSesion();
        cambiarVista(event, "/views/login.fxml");
    }

    private void cambiarVista(ActionEvent event, String ruta) {
        try {
            Node source = (Node) event.getSource();
            Scene scene = source.getScene();
            Parent root = FXMLLoader.load(getClass().getResource(ruta));
            scene.setRoot(root);
            Stage stage = (Stage) scene.getWindow();
            stage.setResizable(!ruta.endsWith("login.fxml"));
            stage.setWidth(ruta.endsWith("login.fxml") ? 1000 : 1440);
            stage.setHeight(ruta.endsWith("login.fxml") ? 650 : 900);
            stage.centerOnScreen();
        } catch (IOException | RuntimeException ex) {
            mostrarError("Error de navegación", ex.getMessage());
        }
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje == null ? "Operación realizada." : mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje == null ? "Ocurrió un error inesperado." : mensaje);
        alerta.showAndWait();
    }
}
