package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import model.ApartaSuites;
import model.enums.EstadoReserva;
import model.modelo.Huesped;
import model.modelo.Folio;
import model.modelo.Reserva;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HuespedController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblCantidadReservas;
    @FXML private Label lblCantidadFavoritos;
    @FXML private Label lblCantidadHistorial;
    @FXML private Label lblSaldoPendiente;

    @FXML private DatePicker fechaEntrada;
    @FXML private DatePicker fechaSalida;
    @FXML private TextField campoPersonas;
    @FXML private VBox contenedorProximas;

    private final ApartaSuites sistema = ApartaSuites.getInstance();

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        configurarFechas();
        campoPersonas.setText("2");
        cargarDatosHuesped();
    }

    private void configurarFechas() {
        fechaEntrada.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacio) {
                super.updateItem(fecha, vacio);
                setDisable(vacio || fecha.isBefore(LocalDate.now()));
            }
        });

        fechaSalida.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacio) {
                super.updateItem(fecha, vacio);
                LocalDate entrada = fechaEntrada.getValue();
                setDisable(vacio || fecha.isBefore(LocalDate.now())
                        || (entrada != null && !fecha.isAfter(entrada)));
            }
        });

        fechaEntrada.valueProperty().addListener((obs, anterior, nueva) -> {
            if (nueva != null && fechaSalida.getValue() != null
                    && !fechaSalida.getValue().isAfter(nueva)) {
                fechaSalida.setValue(null);
            }
        });
    }

    private void cargarDatosHuesped() {
        if (!(sistema.getUsuarioAutenticado() instanceof Huesped huesped)) {
            lblBienvenida.setText("¡Bienvenido!");
            lblNombreUsuario.setText("Huésped");
            lblCantidadReservas.setText("0");
            lblCantidadFavoritos.setText("0");
            lblCantidadHistorial.setText("0");
            lblSaldoPendiente.setText(formatoMoneda.format(BigDecimal.ZERO));
            mostrarEstadoVacio("Inicia sesión como huésped para consultar tus reservas.");
            return;
        }

        String nombre = huesped.getNombre() == null ? "Huésped" : huesped.getNombre();
        lblBienvenida.setText("¡Bienvenido, " + nombre + "!");
        lblNombreUsuario.setText(nombre);

        int totalReservas = 0;
        int totalHistorial = 0;
        BigDecimal saldoPendiente = BigDecimal.ZERO;
        for (Reserva reserva : huesped.getHistorial()) {
            totalReservas++;
            if (reserva.getEstado() == EstadoReserva.FINALIZADA) {
                totalHistorial++;
            }
            Folio folio = reserva.getFolio();
            if (folio != null) {
                saldoPendiente = saldoPendiente.add(folio.calcularSaldo());
            }
        }

        lblCantidadReservas.setText(String.valueOf(totalReservas));
        lblCantidadFavoritos.setText(String.valueOf(huesped.getFavoritos().tamano()));
        lblCantidadHistorial.setText(String.valueOf(totalHistorial));
        lblSaldoPendiente.setText(formatoMoneda.format(saldoPendiente));

        cargarProximasReservas(huesped);
    }

    private void cargarProximasReservas(Huesped huesped) {
        contenedorProximas.getChildren().clear();

        List<Reserva> proximas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        for (Reserva reserva : huesped.getHistorial()) {
            EstadoReserva estado = reserva.getEstado();
            LocalDate entrada = reserva.getEstancia().getEntrada();
            LocalDate salida = reserva.getEstancia().getSalida();
            boolean futura = (estado == EstadoReserva.PENDIENTE
                    || estado == EstadoReserva.CONFIRMADA)
                    && !entrada.isBefore(hoy);
            boolean enCurso = estado == EstadoReserva.EN_CURSO
                    && !salida.isBefore(hoy);

            if (futura || enCurso) {
                proximas.add(reserva);
            }
        }

        proximas.sort(Comparator.comparing(r -> r.getEstancia().getEntrada()));

        if (proximas.isEmpty()) {
            mostrarEstadoVacio("No tienes reservas próximas. Explora los apartamentos para planear tu siguiente estancia.");
            return;
        }

        int limite = Math.min(3, proximas.size());
        for (int i = 0; i < limite; i++) {
            contenedorProximas.getChildren().add(crearTarjetaReserva(proximas.get(i)));
        }
    }

    private VBox crearTarjetaReserva(Reserva reserva) {
        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().add("upcoming-card");

        HBox encabezado = new HBox(10);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        VBox identificacion = new VBox(3);
        Label codigo = new Label("RESERVA " + reserva.getId());
        codigo.getStyleClass().add("upcoming-code");
        Label apartamento = new Label(
                reserva.getApartamento().getNombre() + " · " + reserva.getApartamento().getCodigo()
        );
        apartamento.getStyleClass().add("upcoming-apartment");
        identificacion.getChildren().addAll(codigo, apartamento);
        HBox.setHgrow(identificacion, Priority.ALWAYS);

        Label estado = new Label(reserva.getEstado().name().replace('_', ' '));
        estado.getStyleClass().add("upcoming-status");
        encabezado.getChildren().addAll(identificacion, estado);

        HBox datos = new HBox(18);
        datos.getChildren().addAll(
                crearDatoReserva("ENTRADA", reserva.getEstancia().getEntrada().format(formatoFecha)),
                crearDatoReserva("SALIDA", reserva.getEstancia().getSalida().format(formatoFecha)),
                crearDatoReserva("NOCHES", String.valueOf(ChronoUnit.DAYS.between(
                        reserva.getEstancia().getEntrada(), reserva.getEstancia().getSalida())))
        );

        Button ver = new Button("Ver mis reservas");
        ver.getStyleClass().add("upcoming-link");
        ver.setOnAction(event -> irA("/views/misReservas.fxml"));

        HBox acciones = new HBox(ver);
        acciones.setAlignment(Pos.CENTER_RIGHT);
        tarjeta.getChildren().addAll(encabezado, datos, acciones);
        return tarjeta;
    }

    private VBox crearDatoReserva(String titulo, String valor) {
        VBox dato = new VBox(4);
        HBox.setHgrow(dato, Priority.ALWAYS);
        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("upcoming-field");
        Label contenido = new Label(valor);
        contenido.getStyleClass().add("upcoming-value");
        dato.getChildren().addAll(etiqueta, contenido);
        return dato;
    }

    private void mostrarEstadoVacio(String mensaje) {
        HBox vacio = new HBox(15);
        vacio.setAlignment(Pos.CENTER_LEFT);
        vacio.getStyleClass().add("empty-reservations");

        Label icono = new Label("▤");
        icono.getStyleClass().add("empty-icon");

        VBox textos = new VBox(5);
        Label titulo = new Label("No hay próximas estancias para mostrar");
        titulo.getStyleClass().add("empty-title");
        Label descripcion = new Label(mensaje);
        descripcion.getStyleClass().add("empty-description");
        descripcion.setWrapText(true);
        textos.getChildren().addAll(titulo, descripcion);
        vacio.getChildren().addAll(icono, textos);
        contenedorProximas.getChildren().add(vacio);
    }

    // ==================== Navegación ====================

    @FXML private void irInicio() {
        // Ya estamos en el inicio; se conserva la vista y se actualizan sus datos.
        cargarDatosHuesped();
    }

    @FXML private void irBuscar() {
        abrirBusqueda(null, null, "2");
    }

    @FXML private void buscarConFechas() {
        LocalDate entrada = fechaEntrada.getValue();
        LocalDate salida = fechaSalida.getValue();
        String personas = campoPersonas.getText() == null ? "" : campoPersonas.getText().trim();

        if (entrada == null || salida == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Fechas incompletas",
                    "Selecciona las fechas de entrada y salida.");
            return;
        }
        if (entrada.isBefore(LocalDate.now()) || !salida.isAfter(entrada)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Fechas incorrectas",
                    "La entrada no puede ser anterior a hoy y la salida debe ser posterior a la entrada.");
            return;
        }
        try {
            if (Integer.parseInt(personas) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, "Cantidad incorrecta",
                    "Ingresa un número de personas mayor que cero.");
            return;
        }
        abrirBusqueda(entrada, salida, personas);
    }

    private void abrirBusqueda(LocalDate entrada, LocalDate salida, String personas) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/buscarAlojamiento.fxml"));
            Parent root = loader.load();
            BuscarAlojamientoController controller = loader.getController();
            controller.configurarBusqueda(entrada, salida, personas);
            cambiarRaiz(root);
        } catch (IOException | RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir la búsqueda", e.getMessage());
        }
    }

    @FXML private void irReservas() { irA("/views/misReservas.fxml"); }
    @FXML private void irFavoritos() { irA("/views/favoritosHuesped.fxml"); }
    @FXML private void irHistorial() { irA("/views/historialHuesped.fxml"); }
    @FXML private void irPerfil() { irA("/views/perfilHuesped.fxml"); }

    @FXML
    private void contactar() {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Atención al huésped",
                "Para recibir ayuda con tu alojamiento, comunícate con recepción de Aparta-Suites UQ.");
    }

    @FXML
    private void cerrarSesion() {
        sistema.cerrarSesion();
        irA("/views/login.fxml");
    }

    private void irA(String ruta) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(ruta));
            cambiarRaiz(root);
        } catch (IOException | RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir la pantalla", e.getMessage());
        }
    }

    private void cambiarRaiz(Parent root) {
        Scene scene = contenedorProximas.getScene();
        if (scene == null) throw new IllegalStateException("La vista no está asociada a una escena.");
        scene.setRoot(root);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje == null ? "Ocurrió un error inesperado." : mensaje);
        alerta.showAndWait();
    }
}
