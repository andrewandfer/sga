
package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;

import model.ApartaSuites;
import model.enums.CriterioOrden;
import model.modelo.Apartamento;
import model.modelo.Estancia;
import model.modelo.Ocupante;
import model.modelo.Cotizacion;
import model.modelo.LineaNoche;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.excepciones.CapacidadExcedidaException;
import model.excepciones.FechasInvalidasException;

import java.time.LocalDate;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BuscarAlojamientoController {

    @FXML
    private DatePicker fechaEntrada;

    @FXML
    private DatePicker fechaSalida;

    @FXML
    private TextField campoPersonas;

    @FXML
    private ComboBox<String> comboCriterio;

    @FXML
    private Label mensajeBusqueda;

    @FXML
    private Label contadorResultados;

    @FXML
    private VBox contenedorResultados;

    @FXML
    private VBox seccionOcupantes;

    @FXML
    private VBox contenedorOcupantes;

    @FXML
    private VBox contenedorDesglose;

    @FXML
    private Label mensajeCotizacion;

    @FXML
    private Label totalCotizacion;

    private final ApartaSuites sistema = ApartaSuites.getInstance();

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private Estancia estanciaSeleccionada;
    private Apartamento apartamentoSeleccionado;
    private int cantidadPersonas;

    private final List<TextField> nombresOcupantes = new ArrayList<>();
    private final List<DatePicker> fechasNacimiento = new ArrayList<>();

    @FXML
    public void initialize() {

        comboCriterio.getItems().addAll(
                "Precio",
                "Capacidad",
                "Dormitorios",
                "Número de reservas"
        );

        comboCriterio.getSelectionModel().select("Precio");

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

                setDisable(vacio
                        || fecha.isBefore(LocalDate.now())
                        || (entrada != null && !fecha.isAfter(entrada)));
            }
        });
    }

    @FXML
    private void buscarDisponibilidad() {

        try {
            LocalDate entrada = fechaEntrada.getValue();
            LocalDate salida = fechaSalida.getValue();

            if (entrada == null || salida == null) {
                mostrarMensaje("Selecciona las fechas de entrada y salida.");
                return;
            }

            if (entrada.isBefore(LocalDate.now())) {
                mostrarMensaje("La fecha de entrada no puede ser anterior a hoy.");
                return;
            }

            int personas;

            try {
                personas = Integer.parseInt(campoPersonas.getText().trim());
            } catch (NumberFormatException e) {
                mostrarMensaje("Escribe una cantidad válida de personas.");
                return;
            }

            if (personas <= 0) {
                mostrarMensaje("La cantidad de personas debe ser mayor que cero.");
                return;
            }

            estanciaSeleccionada = new Estancia(entrada, salida);
            cantidadPersonas = personas;

            CriterioOrden criterio = obtenerCriterio();

            var disponibles = sistema.buscarDisponibles(
                    estanciaSeleccionada,
                    cantidadPersonas,
                    criterio
            );

            contenedorResultados.getChildren().clear();
            limpiarSeleccion();

            int cantidad = 0;

            for (Apartamento apartamento : disponibles) {
                contenedorResultados.getChildren().add(
                        crearTarjetaApartamento(apartamento)
                );

                cantidad++;
            }

            contadorResultados.setText(cantidad + " RESULTADOS");

            if (cantidad == 0) {
                mostrarMensaje("No encontramos apartamentos disponibles para esas fechas y cantidad de personas.");
            } else {
                mostrarMensaje("Encontramos " + cantidad + " apartamento(s) disponible(s).");
            }

        } catch (FechasInvalidasException e) {
            mostrarMensaje(e.getMessage());

        } catch (Exception e) {
            mostrarMensaje("No fue posible realizar la búsqueda: " + e.getMessage());
        }
    }

    private CriterioOrden obtenerCriterio() {

        String criterio = comboCriterio.getValue();

        if (criterio == null) {
            return CriterioOrden.PRECIO;
        }

        return switch (criterio) {
            case "Capacidad" -> CriterioOrden.CAPACIDAD;
            case "Dormitorios" -> CriterioOrden.DORMITORIOS;
            case "Número de reservas" -> CriterioOrden.NUMERO_RESERVAS;
            default -> CriterioOrden.PRECIO;
        };
    }

    private HBox crearTarjetaApartamento(Apartamento apartamento) {

        HBox tarjeta = new HBox(18);
        tarjeta.getStyleClass().add("apartment-card");
        tarjeta.setAlignment(Pos.CENTER_LEFT);

        VBox icono = new VBox(8);
        icono.setAlignment(Pos.CENTER);
        icono.setPrefWidth(145);
        icono.setMinWidth(145);
        icono.getStyleClass().add("apartment-placeholder");

        Label simbolo = new Label("⌂");
        simbolo.getStyleClass().add("apartment-symbol");

        Label textoIcono = new Label("APARTAMENTO");
        textoIcono.getStyleClass().add("placeholder-caption");

        icono.getChildren().addAll(simbolo, textoIcono);

        VBox informacion = new VBox(10);
        HBox.setHgrow(informacion, Priority.ALWAYS);

        Label nombre = new Label(
                apartamento.getNombre() == null
                        ? "Apartamento " + apartamento.getCodigo()
                        : apartamento.getNombre()
        );
        nombre.getStyleClass().add("apartment-title");

        Label codigo = new Label("Código: " + apartamento.getCodigo());
        codigo.getStyleClass().add("apartment-detail");

        Label bloquePiso = new Label(
                "Bloque: " + apartamento.getBloque()
                        + "     Piso: " + apartamento.getPiso()
        );
        bloquePiso.getStyleClass().add("apartment-detail");

        Label dormitorios = new Label(
                "Dormitorios: " + apartamento.getDormitorios()
                        + "     Capacidad máxima: " + apartamento.getCapacidadMaxima()
        );
        dormitorios.getStyleClass().add("apartment-detail");

        Label caracteristicas = new Label(
                apartamento.getCaracteristicas() == null
                        ? "Sin características registradas."
                        : apartamento.getCaracteristicas()
        );
        caracteristicas.setWrapText(true);
        caracteristicas.getStyleClass().add("section-description");

        informacion.getChildren().addAll(
                nombre,
                codigo,
                bloquePiso,
                dormitorios,
                caracteristicas
        );

        VBox acciones = new VBox(10);
        acciones.setAlignment(Pos.CENTER_RIGHT);

        Label tarifa = new Label("TARIFA SEGÚN TEMPORADA");
        tarifa.getStyleClass().add("field-label");

        Label precio = new Label("Consultar cotización");
        precio.getStyleClass().add("apartment-price");

        Button seleccionar = new Button("Seleccionar");
        seleccionar.getStyleClass().add("secondary-button");
        seleccionar.setOnAction(event -> seleccionarApartamento(apartamento));

        acciones.getChildren().addAll(tarifa, precio, seleccionar);

        tarjeta.getChildren().addAll(icono, informacion, acciones);

        return tarjeta;
    }

    private void seleccionarApartamento(Apartamento apartamento) {

        apartamentoSeleccionado = apartamento;

        contenedorOcupantes.getChildren().clear();
        nombresOcupantes.clear();
        fechasNacimiento.clear();

        for (int i = 1; i <= cantidadPersonas; i++) {

            VBox formulario = new VBox(8);
            formulario.getStyleClass().add("quote-row");

            Label titulo = new Label("Ocupante " + i);
            titulo.getStyleClass().add("section-title");

            TextField nombre = new TextField();
            nombre.setPromptText("Nombre completo del ocupante " + i);

            DatePicker nacimiento = new DatePicker();
            nacimiento.setPromptText("Fecha de nacimiento");
            nacimiento.setMaxWidth(Double.MAX_VALUE);
            nacimiento.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate fecha, boolean vacio) {
                    super.updateItem(fecha, vacio);
                    setDisable(vacio || fecha.isAfter(LocalDate.now()));
                }
            });

            nombresOcupantes.add(nombre);
            fechasNacimiento.add(nacimiento);

            formulario.getChildren().addAll(
                    titulo,
                    new Label("Nombre completo"),
                    nombre,
                    new Label("Fecha de nacimiento"),
                    nacimiento
            );

            contenedorOcupantes.getChildren().add(formulario);
        }

        seccionOcupantes.setVisible(true);
        seccionOcupantes.setManaged(true);

        contenedorDesglose.getChildren().clear();
        totalCotizacion.setText("Pendiente de cotización");

        mensajeCotizacion.setText(
                "Apartamento seleccionado: " + apartamento.getCodigo()
                        + ". Completa los datos de los ocupantes para calcular el valor."
        );

        seccionOcupantes.requestFocus();
    }

    @FXML
    private void calcularCotizacion() {

        if (apartamentoSeleccionado == null || estanciaSeleccionada == null) {
            mostrarAlerta(
                    "Selecciona un apartamento",
                    "Primero debes buscar y seleccionar un apartamento."
            );
            return;
        }

        ListaDoblementeEnlazada<Ocupante> ocupantes =
                new ListaDoblementeEnlazada<>();

        try {

            for (int i = 0; i < cantidadPersonas; i++) {

                String nombre = nombresOcupantes.get(i).getText().trim();
                LocalDate nacimiento = fechasNacimiento.get(i).getValue();

                if (nombre.isBlank() || nacimiento == null) {
                    mostrarAlerta(
                            "Datos incompletos",
                            "Completa el nombre y la fecha de nacimiento del ocupante " + (i + 1) + "."
                    );
                    return;
                }

                if (nacimiento.isAfter(LocalDate.now())) {
                    mostrarAlerta(
                            "Fecha incorrecta",
                            "La fecha de nacimiento no puede ser futura."
                    );
                    return;
                }

                ocupantes.agregar(new Ocupante(nombre, nacimiento));
            }

            Cotizacion cotizacion = sistema.cotizar(
                    apartamentoSeleccionado,
                    estanciaSeleccionada,
                    ocupantes
            );

            mostrarDesglose(cotizacion);

            mensajeCotizacion.setText(
                    "Cotización calculada para " + apartamentoSeleccionado.getCodigo()
                            + " · " + estanciaSeleccionada.noches() + " noche(s)."
            );

        } catch (CapacidadExcedidaException e) {
            mostrarAlerta("Capacidad excedida", e.getMessage());

        } catch (Exception e) {
            mostrarAlerta(
                    "No se pudo calcular la cotización",
                    e.getMessage()
            );
        }
    }

    private void mostrarDesglose(Cotizacion cotizacion) {

        contenedorDesglose.getChildren().clear();

        for (LineaNoche linea : cotizacion.getDesglose()) {

            HBox fila = new HBox(8);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.getStyleClass().add("quote-row");

            Label noche = new Label(linea.getNoche().toString());
            Label temporada = new Label(linea.getTemporada().toString());
            Label personas = new Label(String.valueOf(linea.getPersonasCobradas()));
            Label subtotal = new Label(formatoMoneda.format(linea.getValor()));

            noche.getStyleClass().add("quote-cell");
            temporada.getStyleClass().add("quote-cell");
            personas.getStyleClass().add("quote-cell");
            subtotal.getStyleClass().addAll("quote-cell", "quote-value");

            HBox.setHgrow(noche, Priority.ALWAYS);
            HBox.setHgrow(temporada, Priority.ALWAYS);
            HBox.setHgrow(personas, Priority.ALWAYS);
            HBox.setHgrow(subtotal, Priority.ALWAYS);

            fila.getChildren().addAll(
                    noche,
                    temporada,
                    personas,
                    subtotal
            );

            contenedorDesglose.getChildren().add(fila);
        }

        totalCotizacion.setText(
                formatoMoneda.format(cotizacion.getTotal())
        );
    }

    private void limpiarSeleccion() {

        apartamentoSeleccionado = null;

        seccionOcupantes.setVisible(false);
        seccionOcupantes.setManaged(false);

        contenedorOcupantes.getChildren().clear();
        contenedorDesglose.getChildren().clear();

        nombresOcupantes.clear();
        fechasNacimiento.clear();

        totalCotizacion.setText("Pendiente de cotización");

        mensajeCotizacion.setText(
                "Selecciona un apartamento y registra los ocupantes para calcular la cotización."
        );
    }

    private void mostrarMensaje(String mensaje) {
        mensajeBusqueda.setText(mensaje);
    }

    private void mostrarAlerta(String titulo, String contenido) {

        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}

