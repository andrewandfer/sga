
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
import model.modelo.Huesped;
import model.modelo.SolicitudReservaExterna;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.excepciones.CapacidadExcedidaException;
import model.excepciones.FechasInvalidasException;

import java.time.LocalDate;
import java.time.Period;
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

    @FXML
    private Button btnEnviarSolicitud;

    private final ApartaSuites sistema = ApartaSuites.getInstance();

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private Estancia estanciaSeleccionada;
    private Apartamento apartamentoSeleccionado;
    private int cantidadPersonas;
    private Cotizacion cotizacionCalculada;

    // El huésped autenticado es siempre el titular.
    // Aquí solo se guardan los acompañantes.
    private final List<TextField> nombresOcupantes = new ArrayList<>();
    private final List<DatePicker> fechasNacimiento = new ArrayList<>();

    /**
     * Recibe los filtros enviados desde el dashboard del huésped.
     */
    public void configurarBusqueda(
            LocalDate entrada,
            LocalDate salida,
            String personas
    ) {

        if (entrada != null) {
            fechaEntrada.setValue(entrada);
        }

        if (salida != null) {
            fechaSalida.setValue(salida);
        }

        if (personas != null && !personas.isBlank()) {
            campoPersonas.setText(personas);
        }
    }

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

        fechaEntrada.valueProperty().addListener(
                (obs, anterior, nuevo) -> invalidarBusqueda()
        );

        fechaSalida.valueProperty().addListener(
                (obs, anterior, nuevo) -> invalidarBusqueda()
        );

        campoPersonas.textProperty().addListener(
                (obs, anterior, nuevo) -> invalidarBusqueda()
        );

        fechaSalida.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacio) {
                super.updateItem(fecha, vacio);

                LocalDate entrada = fechaEntrada.getValue();

                setDisable(
                        vacio
                                || fecha.isBefore(LocalDate.now())
                                || (entrada != null && !fecha.isAfter(entrada))
                );
            }
        });
    }

    @FXML
    private void buscarDisponibilidad() {

        try {

            LocalDate entrada = fechaEntrada.getValue();
            LocalDate salida = fechaSalida.getValue();

            if (entrada == null || salida == null) {
                mostrarMensaje(
                        "Selecciona las fechas de entrada y salida."
                );
                return;
            }

            if (entrada.isBefore(LocalDate.now())) {
                mostrarMensaje(
                        "La fecha de entrada no puede ser anterior a hoy."
                );
                return;
            }

            int personas;

            try {
                personas = Integer.parseInt(
                        campoPersonas.getText().trim()
                );
            } catch (NumberFormatException e) {
                mostrarMensaje(
                        "Escribe una cantidad válida de personas."
                );
                return;
            }

            if (personas <= 0) {
                mostrarMensaje(
                        "La cantidad de personas debe ser mayor que cero."
                );
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

            contadorResultados.setText(
                    cantidad + " RESULTADOS"
            );

            if (cantidad == 0) {

                mostrarMensaje(
                        "No encontramos apartamentos disponibles para esas fechas y cantidad de personas."
                );

            } else {

                mostrarMensaje(
                        "Encontramos " + cantidad +
                                " apartamento(s) disponible(s)."
                );
            }

        } catch (FechasInvalidasException e) {

            mostrarMensaje(e.getMessage());

        } catch (Exception e) {

            mostrarMensaje(
                    "No fue posible realizar la búsqueda: " +
                            e.getMessage()
            );
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

        Label codigo = new Label(
                "Código: " + apartamento.getCodigo()
        );

        codigo.getStyleClass().add("apartment-detail");

        Label bloquePiso = new Label(
                "Bloque: " + apartamento.getBloque()
                        + "     Piso: " + apartamento.getPiso()
        );

        bloquePiso.getStyleClass().add("apartment-detail");

        Label dormitorios = new Label(
                "Dormitorios: " + apartamento.getDormitorios()
                        + "     Capacidad máxima: " +
                        apartamento.getCapacidadMaxima()
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

        seleccionar.setOnAction(
                event -> seleccionarApartamento(apartamento)
        );

        acciones.getChildren().addAll(
                tarifa,
                precio,
                seleccionar
        );

        tarjeta.getChildren().addAll(
                icono,
                informacion,
                acciones
        );

        return tarjeta;
    }

    /**
     * Genera los formularios de los acompañantes.
     * Ya no existe el selector Sí/No de menor de 6 años.
     */
    private void seleccionarApartamento(Apartamento apartamento) {

        apartamentoSeleccionado = apartamento;
        cotizacionCalculada = null;

        btnEnviarSolicitud.setDisable(true);

        contenedorOcupantes.getChildren().clear();

        nombresOcupantes.clear();
        fechasNacimiento.clear();

        // La persona 1 es el huésped titular.
        // Solo solicitamos información de los acompañantes.
        for (int numero = 2; numero <= cantidadPersonas; numero++) {

            VBox formulario = new VBox(8);
            formulario.getStyleClass().add("quote-row");

            Label titulo = new Label("Acompañante " + numero);
            titulo.getStyleClass().add("section-title");

            TextField nombre = new TextField();

            nombre.setPromptText(
                    "Nombre completo del acompañante " + numero
            );

            DatePicker nacimiento = new DatePicker();

            nacimiento.setPromptText("Fecha de nacimiento");
            nacimiento.setMaxWidth(Double.MAX_VALUE);

            nacimiento.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate fecha, boolean vacio) {
                    super.updateItem(fecha, vacio);

                    setDisable(
                            vacio || fecha.isAfter(LocalDate.now())
                    );
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

        boolean requiereAcompanantes = cantidadPersonas > 1;

        seccionOcupantes.setVisible(requiereAcompanantes);
        seccionOcupantes.setManaged(requiereAcompanantes);

        contenedorDesglose.getChildren().clear();

        totalCotizacion.setText("Pendiente de cotización");

        mensajeCotizacion.setText(
                "Apartamento seleccionado: " +
                        apartamento.getCodigo() + ". " +
                        (requiereAcompanantes
                                ? "Registra únicamente los acompañantes; tú eres el titular."
                                : "Tu cuenta será la titular. No necesitas registrar más personas.")
        );

        if (requiereAcompanantes) {

            seccionOcupantes.requestFocus();

        } else {

            // Si es una persona, se calcula directamente con el titular.
            calcularCotizacion();
        }
    }

    @FXML
    private void calcularCotizacion() {

        cotizacionCalculada = null;
        btnEnviarSolicitud.setDisable(true);

        contenedorDesglose.getChildren().clear();

        totalCotizacion.setText("Pendiente de cotización");

        if (apartamentoSeleccionado == null
                || estanciaSeleccionada == null) {

            mostrarAlerta(
                    "Selecciona un apartamento",
                    "Primero debes buscar y seleccionar un apartamento."
            );

            return;
        }

        try {

            ListaDoblementeEnlazada<Ocupante> ocupantes =
                    construirListaOcupantes();

            Cotizacion cotizacion = sistema.cotizar(
                    apartamentoSeleccionado,
                    estanciaSeleccionada,
                    ocupantes
            );

            cotizacionCalculada = cotizacion;

            mostrarDesglose(cotizacion);

            btnEnviarSolicitud.setDisable(false);

            mensajeCotizacion.setText(
                    "Cotización calculada para " +
                            apartamentoSeleccionado.getCodigo()
                            + " · " + estanciaSeleccionada.noches()
                            + " noche(s). Revisa el total y confirma."
            );

        } catch (CapacidadExcedidaException e) {

            mostrarAlerta("Capacidad excedida", e.getMessage());

        } catch (IllegalArgumentException e) {

            mostrarAlerta("Datos incompletos", e.getMessage());

        } catch (Exception e) {

            mostrarAlerta(
                    "No se pudo calcular la cotización",
                    e.getMessage() == null
                            ? "Verifica los datos e inténtalo nuevamente."
                            : e.getMessage()
            );
        }
    }

    /**
     * Construye la lista de ocupantes.
     *
     * La edad se determina automáticamente mediante la fecha
     * de nacimiento. No se solicita ninguna respuesta Sí/No.
     */
    private ListaDoblementeEnlazada<Ocupante> construirListaOcupantes() {

        Object usuario = sistema.getUsuarioAutenticado();

        if (!(usuario instanceof Huesped titular)) {

            throw new IllegalArgumentException(
                    "La sesión del huésped no es válida. Inicia sesión nuevamente."
            );
        }

        if (titular.getNombre() == null
                || titular.getNombre().isBlank()
                || titular.getFechaNacimiento() == null) {

            throw new IllegalArgumentException(
                    "Completa tu nombre y fecha de nacimiento en el perfil antes de solicitar una reserva."
            );
        }

        ListaDoblementeEnlazada<Ocupante> ocupantes =
                new ListaDoblementeEnlazada<>();

        // Persona 1: titular de la cuenta.
        ocupantes.agregar(
                new Ocupante(
                        titular.getNombre(),
                        titular.getFechaNacimiento()
                )
        );

        // Acompañantes.
        for (int i = 0; i < nombresOcupantes.size(); i++) {

            String nombre = nombresOcupantes.get(i).getText() == null
                    ? ""
                    : nombresOcupantes.get(i).getText().trim();

            LocalDate nacimiento = fechasNacimiento.get(i).getValue();

            if (nombre.isBlank() || nacimiento == null) {

                throw new IllegalArgumentException(
                        "Completa el nombre y la fecha de nacimiento del acompañante "
                                + (i + 2) + "."
                );
            }

            if (nacimiento.isAfter(LocalDate.now())) {

                throw new IllegalArgumentException(
                        "La fecha de nacimiento del acompañante "
                                + (i + 2) + " no puede ser futura."
                );
            }

            // Edad calculada automáticamente al momento del ingreso.
            int edadAlIngresar = Period.between(
                    nacimiento,
                    estanciaSeleccionada.getEntrada()
            ).getYears();

            // Se agrega el ocupante con su fecha real.
            // El CotizadorService determina automáticamente si paga.
            ocupantes.agregar(
                    new Ocupante(nombre, nacimiento)
            );
        }

        if (ocupantes.tamano() != cantidadPersonas) {

            throw new IllegalArgumentException(
                    "La cantidad de ocupantes no coincide con la búsqueda."
            );
        }

        return ocupantes;
    }

    private void mostrarDesglose(Cotizacion cotizacion) {

        contenedorDesglose.getChildren().clear();

        for (LineaNoche linea : cotizacion.getDesglose()) {

            HBox fila = new HBox(8);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.getStyleClass().add("quote-row");

            Label noche = new Label(
                    linea.getNoche().toString()
            );

            Label temporada = new Label(
                    linea.getTemporada().toString()
            );

            Label tarifaPersona = new Label(
                    formatoMoneda.format(
                            apartamentoSeleccionado.tarifaPara(
                                    linea.getTemporada()
                            )
                    )
            );

            Label personas = new Label(
                    String.valueOf(linea.getPersonasCobradas())
            );

            Label subtotal = new Label(
                    formatoMoneda.format(linea.getValor())
            );

            noche.getStyleClass().add("quote-cell");
            temporada.getStyleClass().add("quote-cell");
            tarifaPersona.getStyleClass().add("quote-cell");
            personas.getStyleClass().add("quote-cell");

            subtotal.getStyleClass().addAll(
                    "quote-cell",
                    "quote-value"
            );

            HBox.setHgrow(noche, Priority.ALWAYS);
            HBox.setHgrow(temporada, Priority.ALWAYS);
            HBox.setHgrow(tarifaPersona, Priority.ALWAYS);
            HBox.setHgrow(personas, Priority.ALWAYS);
            HBox.setHgrow(subtotal, Priority.ALWAYS);

            fila.getChildren().addAll(
                    noche,
                    temporada,
                    tarifaPersona,
                    personas,
                    subtotal
            );

            contenedorDesglose.getChildren().add(fila);
        }

        totalCotizacion.setText(
                formatoMoneda.format(cotizacion.getTotal())
        );
    }

    private void invalidarBusqueda() {

        if (contenedorResultados == null) {
            return;
        }

        contenedorResultados.getChildren().clear();

        limpiarSeleccion();

        if (mensajeBusqueda != null) {
            mensajeBusqueda.setText(
                    "Los criterios cambiaron. Busca disponibilidad nuevamente."
            );
        }

        if (contadorResultados != null) {
            contadorResultados.setText("0 RESULTADOS");
        }
    }

    private void limpiarSeleccion() {

        apartamentoSeleccionado = null;
        cotizacionCalculada = null;

        if (btnEnviarSolicitud != null) {
            btnEnviarSolicitud.setDisable(true);
        }

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

    /**
     * Envía la selección como solicitud externa.
     * Recepción decide si puede confirmarla.
     */
    @FXML
    private void enviarSolicitudReserva() {

        Object usuario = sistema.getUsuarioAutenticado();

        if (!(usuario instanceof Huesped huesped)) {

            mostrarAlerta(
                    "Sesión no válida",
                    "Inicia sesión como huésped para enviar una solicitud."
            );

            return;
        }

        if (apartamentoSeleccionado == null
                || estanciaSeleccionada == null
                || cotizacionCalculada == null) {

            mostrarAlerta(
                    "Solicitud incompleta",
                    "Primero calcula la cotización de tu estancia."
            );

            return;
        }

        try {

            ListaDoblementeEnlazada<Ocupante> ocupantes =
                    construirListaOcupantes();

            cotizacionCalculada = sistema.cotizar(
                    apartamentoSeleccionado,
                    estanciaSeleccionada,
                    ocupantes
            );

            mostrarDesglose(cotizacionCalculada);

            SolicitudReservaExterna solicitud =
                    sistema.recibirSolicitudExterna(
                            huesped,
                            apartamentoSeleccionado,
                            estanciaSeleccionada,
                            ocupantes
                    );

            btnEnviarSolicitud.setDisable(true);

            mensajeCotizacion.setText(
                    "Solicitud " + solicitud.getId()
                            + " enviada a recepción. Está pendiente de revisión; "
                            + "aún no es una reserva confirmada."
            );

            mostrarAlerta(
                    "Solicitud enviada",
                    "Tu solicitud " + solicitud.getId()
                            + " fue registrada y enviada a recepción. "
                            + "Puedes consultar su estado en Mis reservas. "
                            + "El envío no confirma la reserva ni bloquea las fechas."
            );

        } catch (Exception e) {

            mostrarAlerta(
                    "No se pudo enviar la solicitud",
                    e.getMessage() == null
                            ? "Verifica los datos e inténtalo nuevamente."
                            : e.getMessage()
            );
        }
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

    // Navegación del portal del huésped.

    @FXML
    private void irInicio(javafx.event.ActionEvent e) {
        NavegacionHuesped.abrir(e, "/views/dasboardHuesped.fxml");
    }

    @FXML
    private void irBuscar(javafx.event.ActionEvent e) {
        NavegacionHuesped.abrir(e, "/views/buscarAlojamiento.fxml");
    }

    @FXML
    private void irReservas(javafx.event.ActionEvent e) {
        NavegacionHuesped.abrir(e, "/views/misReservas.fxml");
    }

    @FXML
    private void irFavoritos(javafx.event.ActionEvent e) {
        NavegacionHuesped.abrir(e, "/views/favoritosHuesped.fxml");
    }

    @FXML
    private void irHistorial(javafx.event.ActionEvent e) {
        NavegacionHuesped.abrir(e, "/views/historialHuesped.fxml");
    }

    @FXML
    private void irPerfil(javafx.event.ActionEvent e) {
        NavegacionHuesped.abrir(e, "/views/perfilHuesped.fxml");
    }

    @FXML
    private void cerrarSesion(javafx.event.ActionEvent e) {
        NavegacionHuesped.cerrarSesion(e);
    }

    @FXML
    private void contactar(javafx.event.ActionEvent e) {
        NavegacionHuesped.contactar();
    }

}
