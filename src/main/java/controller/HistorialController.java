
package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import model.ApartaSuites;
import model.enums.EstadoReserva;
import model.excepciones.ExcepcionSGA;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.interfaces.Usuario;
import model.modelo.Apartamento;
import model.modelo.Folio;
import model.modelo.Huesped;
import model.modelo.Reserva;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class HistorialController {

    @FXML private Label nombreHuesped;
    @FXML private Label totalFinalizadas;
    @FXML private Label ultimaEstancia;
    @FXML private Label totalNoches;

    @FXML private DatePicker fechaDesde;
    @FXML private DatePicker fechaHasta;
    @FXML private ComboBox<String> filtroEstado;

    @FXML private VBox contenedorEstancias;
    @FXML private VBox estadoVacio;
    @FXML private VBox detalleVacio;
    @FXML private VBox movimientosDetalle;

    @FXML private Label codigoDetalle;
    @FXML private Label apartamentoDetalle;
    @FXML private Label entradaDetalle;
    @FXML private Label salidaDetalle;
    @FXML private Label nochesDetalle;
    @FXML private Label totalDetalle;

    private final ApartaSuites sistema = ApartaSuites.getInstance();

    private Huesped huesped;

    private final List<Reserva> reservas = new ArrayList<>();

    private Reserva reservaSeleccionada;

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    @FXML
    private void initialize() {

        Usuario usuario = sistema.getUsuarioAutenticado();

        if (!(usuario instanceof Huesped)) {
            mostrarError("No hay una sesión de huésped activa.");
            return;
        }

        huesped = (Huesped) usuario;

        nombreHuesped.setText(huesped.getNombre());

        filtroEstado.setItems(
                FXCollections.observableArrayList(
                        "Todos los estados",
                        "FINALIZADA",
                        "CANCELADA",
                        "NO_SHOW"
                )
        );

        filtroEstado.setValue("Todos los estados");

        cargarHistorial();
    }

    private void cargarHistorial() {

        try {

            reservas.clear();

            ListaDoblementeEnlazada<Reserva> historial =
                    sistema.historialDeHuesped(huesped.getDocumento());

            for (Reserva reserva : historial) {
                reservas.add(reserva);
            }

            reservas.sort(
                    Comparator.comparing(
                            (Reserva r) -> r.getEstancia().getSalida()
                    ).reversed()
            );

            actualizarResumen();
            aplicarFiltros();

        } catch (ExcepcionSGA e) {

            mostrarError(
                    "No fue posible cargar el historial: "
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void consultarHistorial() {

        if (fechaDesde.getValue() != null
                && fechaHasta.getValue() != null
                && fechaDesde.getValue().isAfter(fechaHasta.getValue())) {

            mostrarError(
                    "La fecha inicial no puede ser posterior a la fecha final."
            );

            return;
        }

        aplicarFiltros();
    }

    private void aplicarFiltros() {

        contenedorEstancias.getChildren().clear();

        LocalDate desde = fechaDesde.getValue();
        LocalDate hasta = fechaHasta.getValue();

        String estado = filtroEstado.getValue();

        List<Reserva> filtradas = new ArrayList<>();

        for (Reserva reserva : reservas) {

            LocalDate entrada = reserva.getEstancia().getEntrada();
            LocalDate salida = reserva.getEstancia().getSalida();

            boolean cumpleFecha =
                    (desde == null || !entrada.isBefore(desde))
                            && (hasta == null || !salida.isAfter(hasta));

            boolean cumpleEstado =
                    estado == null
                            || estado.equals("Todos los estados")
                            || reserva.getEstado().name().equals(estado);

            if (cumpleFecha && cumpleEstado) {
                filtradas.add(reserva);
            }
        }

        if (filtradas.isEmpty()) {
            mostrarEstadoVacio(true);
            limpiarDetalle();
            return;
        }

        mostrarEstadoVacio(false);

        for (Reserva reserva : filtradas) {
            contenedorEstancias.getChildren().add(
                    crearTarjeta(reserva)
            );
        }

        if (reservaSeleccionada != null
                && filtradas.contains(reservaSeleccionada)) {
            mostrarDetalle(reservaSeleccionada);
        } else {
            mostrarDetalle(filtradas.get(0));
        }
    }

    private void actualizarResumen() {

        int finalizadas = 0;
        long noches = 0;
        LocalDate ultimaSalida = null;

        for (Reserva reserva : reservas) {

            if (reserva.getEstado() == EstadoReserva.FINALIZADA) {

                finalizadas++;

                LocalDate entrada = reserva.getEstancia().getEntrada();
                LocalDate salida = reserva.getEstancia().getSalida();

                noches += ChronoUnit.DAYS.between(entrada, salida);

                if (ultimaSalida == null
                        || salida.isAfter(ultimaSalida)) {
                    ultimaSalida = salida;
                }
            }
        }

        totalFinalizadas.setText(String.valueOf(finalizadas));
        totalNoches.setText(String.valueOf(noches));

        ultimaEstancia.setText(
                ultimaSalida == null
                        ? "Sin estancias"
                        : ultimaSalida.format(formatoFecha)
        );
    }

    private VBox crearTarjeta(Reserva reserva) {

        Apartamento apartamento = reserva.getApartamento();

        VBox tarjeta = new VBox(14.0);
        tarjeta.getStyleClass().add("stay-card");

        HBox encabezado = new HBox();
        encabezado.setAlignment(Pos.CENTER_LEFT);

        VBox identificacion = new VBox(5.0);

        Label tituloCodigo = new Label("CÓDIGO DE RESERVA");
        tituloCodigo.getStyleClass().add("field-label");

        Label codigo = new Label(reserva.getId());
        codigo.getStyleClass().add("reservation-code");

        identificacion.getChildren().addAll(tituloCodigo, codigo);

        HBox.setHgrow(identificacion, Priority.ALWAYS);

        Label estado = new Label(
                reserva.getEstado().name().replace("_", " ")
        );

        estado.getStyleClass().add(
                reserva.getEstado() == EstadoReserva.FINALIZADA
                        ? "status-completed"
                        : "availability-label"
        );

        encabezado.getChildren().addAll(identificacion, estado);

        tarjeta.getChildren().addAll(
                encabezado,
                separador()
        );

        HBox informacion = new HBox(18.0);

        VBox imagen = new VBox(8.0);
        imagen.setAlignment(Pos.CENTER);
        imagen.setPrefWidth(125.0);
        imagen.setMinWidth(125.0);
        imagen.getStyleClass().add("apartment-placeholder");

        Label simbolo = new Label("⌂");
        simbolo.getStyleClass().add("apartment-symbol");

        Label textoImagen = new Label("ALOJAMIENTO");
        textoImagen.getStyleClass().add("placeholder-caption");

        imagen.getChildren().addAll(simbolo, textoImagen);

        VBox detalles = new VBox(9.0);
        HBox.setHgrow(detalles, Priority.ALWAYS);

        Label nombre = new Label(apartamento.getNombre());
        nombre.getStyleClass().add("apartment-title");

        Label codigoApartamento = detalle(
                "Código: " + apartamento.getCodigo()
        );

        Label ubicacion = detalle(
                "Bloque: " + apartamento.getBloque()
                        + "       Piso: " + apartamento.getPiso()
        );

        Label capacidad = detalle(
                "Dormitorios: " + apartamento.getDormitorios()
                        + "       Huéspedes: "
                        + reserva.numeroPersonas()
        );

        detalles.getChildren().addAll(
                nombre,
                codigoApartamento,
                ubicacion,
                capacidad
        );

        informacion.getChildren().addAll(imagen, detalles);

        tarjeta.getChildren().addAll(
                informacion,
                separador()
        );

        HBox fechas = new HBox(15.0);

        fechas.getChildren().addAll(
                crearDato("FECHA DE ENTRADA",
                        reserva.getEstancia().getEntrada().format(formatoFecha)),
                crearDato("FECHA DE SALIDA",
                        reserva.getEstancia().getSalida().format(formatoFecha)),
                crearDato("NOCHES",
                        String.valueOf(numeroNoches(reserva))),
                crearDato("VALOR TOTAL",
                        formatoMoneda.format(totalCargos(reserva)))
        );

        tarjeta.getChildren().add(fechas);

        Button consultar = new Button("Consultar detalles");
        consultar.getStyleClass().add("secondary-button");

        consultar.setOnAction(
                event -> mostrarDetalle(reserva)
        );

        HBox acciones = new HBox();
        acciones.setAlignment(Pos.CENTER_RIGHT);
        acciones.getChildren().add(consultar);

        tarjeta.getChildren().add(acciones);

        return tarjeta;
    }

    private VBox crearDato(String titulo, String valor) {

        VBox dato = new VBox(5.0);
        HBox.setHgrow(dato, Priority.ALWAYS);

        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("field-label");

        Label contenido = new Label(valor);
        contenido.getStyleClass().add("stay-detail");

        dato.getChildren().addAll(etiqueta, contenido);

        return dato;
    }

    private Label detalle(String texto) {

        Label label = new Label(texto);
        label.getStyleClass().add("apartment-detail");

        return label;
    }

    private javafx.scene.control.Separator separador() {

        javafx.scene.control.Separator separator =
                new javafx.scene.control.Separator();

        separator.getStyleClass().add("soft-divider");

        return separator;
    }

    private long numeroNoches(Reserva reserva) {

        return ChronoUnit.DAYS.between(
                reserva.getEstancia().getEntrada(),
                reserva.getEstancia().getSalida()
        );
    }

    private BigDecimal totalCargos(Reserva reserva) {

        Folio folio = reserva.getFolio();

        return folio == null
                ? BigDecimal.ZERO
                : folio.calcularTotalCargos();
    }

    private void mostrarDetalle(Reserva reserva) {

        reservaSeleccionada = reserva;

        detalleVacio.setVisible(false);
        detalleVacio.setManaged(false);

        movimientosDetalle.setVisible(true);
        movimientosDetalle.setManaged(true);

        codigoDetalle.setText(reserva.getId());

        apartamentoDetalle.setText(
                reserva.getApartamento().getNombre()
        );

        entradaDetalle.setText(
                reserva.getEstancia().getEntrada().format(formatoFecha)
        );

        salidaDetalle.setText(
                reserva.getEstancia().getSalida().format(formatoFecha)
        );

        nochesDetalle.setText(
                String.valueOf(numeroNoches(reserva))
        );

        totalDetalle.setText(
                formatoMoneda.format(totalCargos(reserva))
        );
    }

    private void limpiarDetalle() {

        reservaSeleccionada = null;

        detalleVacio.setVisible(true);
        detalleVacio.setManaged(true);

        movimientosDetalle.setVisible(false);
        movimientosDetalle.setManaged(false);
    }

    private void mostrarEstadoVacio(boolean mostrar) {

        estadoVacio.setVisible(mostrar);
        estadoVacio.setManaged(mostrar);
    }

    private void mostrarError(String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.ERROR);

        alerta.setTitle("Historial de estancias");
        alerta.setHeaderText("No se pudo completar la operación");
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}

