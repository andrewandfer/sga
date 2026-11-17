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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.ApartaSuites;
import model.enums.EstadoApartamento;
import model.enums.Temporada;
import model.excepciones.ExcepcionSGA;
import model.interfaces.Usuario;
import model.modelo.Apartamento;
import model.modelo.Empleado;
import model.modelo.Tarifa;

import java.io.IOException;
import java.util.Locale;

public class AdminApartmentsController {

    public Label adminNameLabel;
    public Label adminInitialLabel;
    public Label apartmentCountLabel;
    public Label messageLabel;
    public TextField searchField;
    public TextField codeField;
    public TextField nameField;
    public TextField blockField;
    public TextField floorField;
    public TextField bedroomsField;
    public TextField capacityField;
    public TextArea featuresField;
    public TextField lowRateField;
    public TextField mediumRateField;
    public TextField highRateField;
    public TextField outOfServiceReasonField;
    public ComboBox<String> stateComboBox;
    public TableView<Apartamento> apartmentsTable;
    public TableColumn<Apartamento, String> codeColumn;
    public TableColumn<Apartamento, String> nameColumn;
    public TableColumn<Apartamento, String> blockColumn;
    public TableColumn<Apartamento, String> capacityColumn;
    public TableColumn<Apartamento, String> stateColumn;

    private final ApartaSuites sistema = ApartaSuites.getInstance();
    private final ObservableList<Apartamento> apartamentos = FXCollections.observableArrayList();
    private String codigoEnEdicion;

    public void initialize() {
        Usuario usuario = sistema.getUsuarioAutenticado();
        String nombreAdministrador = usuario instanceof Empleado empleado
                ? empleado.getNombre() : "Administrador";
        adminNameLabel.setText(nombreAdministrador);
        adminInitialLabel.setText(obtenerInicial(nombreAdministrador));

        codeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigo()));
        nameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        blockColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBloque()));
        capacityColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCapacidadMaxima() + " personas · " + d.getValue().getDormitorios() + " hab."));
        stateColumn.setCellValueFactory(d -> new SimpleStringProperty(nombreEstado(d.getValue().getEstadoOperativo())));
        apartmentsTable.setItems(apartamentos);
        stateComboBox.setItems(FXCollections.observableArrayList("Conservar estado actual", "Preparado", "Fuera de servicio"));
        stateComboBox.setValue("Preparado");
        cargarApartamentos();
        messageLabel.setText("");
    }

    private void cargarApartamentos() {
        apartamentos.clear();
        for (Apartamento apartamento : sistema.listarApartamentos()) apartamentos.add(apartamento);
        apartmentCountLabel.setText(apartamentos.size() + (apartamentos.size() == 1 ? " APARTAMENTO" : " APARTAMENTOS"));
        aplicarFiltro();
    }

    public void handleSearch(KeyEvent event) { aplicarFiltro(); }

    private void aplicarFiltro() {
        String filtro = limpiar(searchField.getText()).toLowerCase(Locale.ROOT);
        ObservableList<Apartamento> filtrados = FXCollections.observableArrayList();
        for (Apartamento apartamento : sistema.listarApartamentos()) {
            String datos = (apartamento.getCodigo() + " " + apartamento.getNombre() + " "
                    + apartamento.getBloque() + " " + nombreEstado(apartamento.getEstadoOperativo()))
                    .toLowerCase(Locale.ROOT);
            if (datos.contains(filtro)) filtrados.add(apartamento);
        }
        apartmentsTable.setItems(filtrados);
    }

    public void handleSelectApartment() {
        Apartamento seleccionado = apartmentsTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;
        codigoEnEdicion = seleccionado.getCodigo();
        codeField.setText(seleccionado.getCodigo());
        codeField.setDisable(true);
        nameField.setText(seleccionado.getNombre());
        blockField.setText(seleccionado.getBloque());
        floorField.setText(String.valueOf(seleccionado.getPiso()));
        bedroomsField.setText(String.valueOf(seleccionado.getDormitorios()));
        bedroomsField.setDisable(true);
        capacityField.setText(String.valueOf(seleccionado.getCapacidadMaxima()));
        capacityField.setDisable(true);
        featuresField.setText(seleccionado.getCaracteristicas());
        lowRateField.setText(valorTarifa(seleccionado, Temporada.BAJA));
        mediumRateField.setText(valorTarifa(seleccionado, Temporada.MEDIA));
        highRateField.setText(valorTarifa(seleccionado, Temporada.ALTA));
        stateComboBox.setValue(seleccionado.getEstadoOperativo() == EstadoApartamento.FUERA_DE_SERVICIO
                ? "Fuera de servicio" : "Conservar estado actual");
        outOfServiceReasonField.setText(seleccionado.getMotivoFueraServicio());
        mostrarMensaje("Editando apartamento " + seleccionado.getCodigo() + ".", true);
    }

    public void handleSave(ActionEvent event) {
        String codigo = limpiar(codeField.getText());
        String nombre = limpiar(nameField.getText());
        String bloque = limpiar(blockField.getText());
        String caracteristicas = limpiar(featuresField.getText());
        String motivo = limpiar(outOfServiceReasonField.getText());
        if (codigo.isEmpty() || nombre.isEmpty() || bloque.isEmpty() || limpiar(floorField.getText()).isEmpty()
                || limpiar(bedroomsField.getText()).isEmpty() || limpiar(capacityField.getText()).isEmpty()) {
            mostrarMensaje("Completa código, nombre, bloque, piso, dormitorios y capacidad.", false);
            return;
        }
        if (codigoEnEdicion != null && "Fuera de servicio".equals(stateComboBox.getValue()) && motivo.isEmpty()) {
            mostrarMensaje("Indica el motivo para poner el apartamento fuera de servicio.", false);
            return;
        }
        try {
            int piso = Integer.parseInt(limpiar(floorField.getText()));
            int dormitorios = Integer.parseInt(limpiar(bedroomsField.getText()));
            int capacidad = Integer.parseInt(limpiar(capacityField.getText()));
            double baja = parsearTarifa(lowRateField.getText());
            double media = parsearTarifa(mediumRateField.getText());
            double alta = parsearTarifa(highRateField.getText());
            if (dormitorios <= 0 || capacidad <= 0 || piso < 0) throw new IllegalArgumentException("Revisa piso, dormitorios y capacidad.");

            boolean esRegistro = codigoEnEdicion == null;
            if (esRegistro) {
                Apartamento nuevo = new Apartamento(codigo, nombre, bloque, piso, dormitorios, capacidad, caracteristicas);
                nuevo.definirTarifa(Temporada.BAJA, baja);
                nuevo.definirTarifa(Temporada.MEDIA, media);
                nuevo.definirTarifa(Temporada.ALTA, alta);
                sistema.registrarApartamento(nuevo);
            } else {
                sistema.editarApartamento(codigoEnEdicion, nombre, bloque, piso, caracteristicas);
                sistema.definirTarifa(codigoEnEdicion, Temporada.BAJA, baja);
                sistema.definirTarifa(codigoEnEdicion, Temporada.MEDIA, media);
                sistema.definirTarifa(codigoEnEdicion, Temporada.ALTA, alta);
                Apartamento editado = sistema.buscarApartamento(codigoEnEdicion);
                if ("Fuera de servicio".equals(stateComboBox.getValue())) {
                    sistema.ponerApartamentoFueraDeServicio(codigoEnEdicion, motivo);
                } else if ("Preparado".equals(stateComboBox.getValue())
                        && editado.getEstadoOperativo() == EstadoApartamento.FUERA_DE_SERVICIO) {
                    editado.cambiarEstado(EstadoApartamento.PREPARADO);
                }
            }
            cargarApartamentos();
            limpiarFormulario();
            mostrarMensaje(esRegistro ? "Apartamento registrado correctamente." : "Apartamento actualizado correctamente.", true);
        } catch (NumberFormatException e) {
            mostrarMensaje("Piso, dormitorios, capacidad y tarifas deben ser valores numéricos.", false);
        } catch (ExcepcionSGA | IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), false);
        }
    }

    public void handleDelete(ActionEvent event) {
        Apartamento seleccionado = apartmentsTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) { mostrarMensaje("Selecciona un apartamento de la tabla.", false); return; }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar apartamento");
        confirmacion.setHeaderText("¿Eliminar " + seleccionado.getCodigo() + "?");
        confirmacion.setContentText("No se podrá eliminar si tiene reservas activas.");
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            sistema.eliminarApartamento(seleccionado.getCodigo());
            cargarApartamentos();
            limpiarFormulario();
            mostrarMensaje("Apartamento eliminado correctamente.", true);
        } catch (ExcepcionSGA | IllegalArgumentException e) { mostrarMensaje(e.getMessage(), false); }
    }

    public void handleClear(ActionEvent event) { limpiarFormulario(); mostrarMensaje("Formulario limpio.", true); }

    public void handleDashboard(MouseEvent event) { cambiarVista(event, "/views/adminDashboard.fxml"); }
    public void handleEmployees(MouseEvent event) { cambiarVista(event, "/views/adminEmployees.fxml"); }

    public void handleGuests(javafx.scene.input.MouseEvent event) {
        cambiarVista(event, "/views/adminGuests.fxml");
    }

    public void handleSeasons(javafx.scene.input.MouseEvent event) { cambiarVista(event, "/views/adminSeasons.fxml"); }

    public void handleBuildingMap(javafx.scene.input.MouseEvent event) { cambiarVista(event, "/views/adminBuildingMap.fxml"); }

    public void handleLogout(ActionEvent event) {
        Node origen = (Node) event.getSource();
        Scene scene = origen.getScene();
        Stage stage = scene == null ? null : (Stage) scene.getWindow();
        if (cambiarVista(event, "/views/login.fxml")) {
            sistema.cerrarSesion();
            if (stage != null) { stage.setResizable(false); stage.setWidth(1000); stage.setHeight(650); stage.centerOnScreen(); }
        }
    }

    private boolean cambiarVista(javafx.event.Event event, String ruta) {
        try {
            Scene scene = ((Node) event.getSource()).getScene();
            if (scene == null) throw new IllegalStateException("La pantalla actual no está asociada a una escena.");
            Parent root = FXMLLoader.load(getClass().getResource(ruta));
            scene.setRoot(root);
            return true;
        } catch (IOException | RuntimeException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de navegación"); alerta.setHeaderText("No se pudo cambiar de pantalla");
            alerta.setContentText(e.getMessage()); alerta.showAndWait(); return false;
        }
    }

    private String valorTarifa(Apartamento apartamento, Temporada temporada) {
        for (Tarifa tarifa : apartamento.getTarifas()) if (tarifa.getTemporada() == temporada)
            return String.valueOf(tarifa.getValorPersonaNoche());
        return "0";
    }
    private double parsearTarifa(String valor) {
        double tarifa = Double.parseDouble(limpiar(valor).replace(",", "."));
        if (!Double.isFinite(tarifa) || tarifa < 0) throw new IllegalArgumentException("Las tarifas no pueden ser negativas.");
        return tarifa;
    }
    private String nombreEstado(EstadoApartamento estado) {
        return switch (estado) {
            case PREPARADO -> "Preparado";
            case OCUPADO -> "Ocupado";
            case PENDIENTE_PREPARACION -> "Pendiente de preparación";
            case EN_PREPARACION -> "En preparación";
            case FUERA_DE_SERVICIO -> "Fuera de servicio";
        };
    }
    private String limpiar(String valor) { return valor == null ? "" : valor.trim(); }
    private void limpiarFormulario() {
        codigoEnEdicion = null; codeField.clear(); codeField.setDisable(false); nameField.clear(); blockField.clear();
        floorField.clear(); bedroomsField.clear(); bedroomsField.setDisable(false); capacityField.clear(); capacityField.setDisable(false);
        featuresField.clear(); lowRateField.clear(); mediumRateField.clear(); highRateField.clear();
        outOfServiceReasonField.clear(); stateComboBox.setValue("Preparado"); apartmentsTable.getSelectionModel().clearSelection();
    }
    private void mostrarMensaje(String mensaje, boolean exito) {
        messageLabel.setText(mensaje);
        messageLabel.setStyle(exito ? "-fx-text-fill: #31845A;" : "-fx-text-fill: #C65D5D;");
    }

    private String obtenerInicial(String nombre) {
        if (nombre == null || nombre.isBlank()) return "A";
        return nombre.trim().substring(0, 1).toUpperCase(java.util.Locale.ROOT);
    }
}
