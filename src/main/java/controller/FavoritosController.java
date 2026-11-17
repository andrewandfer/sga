
package controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import model.ApartaSuites;
import model.estructuras.Conjunto;
import model.excepciones.ExcepcionSGA;
import model.interfaces.Usuario;
import model.modelo.Apartamento;
import model.modelo.Huesped;

/**
 * Controlador de la vista de favoritos del huésped.
 */
public class FavoritosController {

    @FXML
    private Label nombreHuesped;

    @FXML
    private Label totalFavoritos;

    @FXML
    private VBox contenedorFavoritos;

    @FXML
    private VBox estadoVacio;

    private final ApartaSuites sistema = ApartaSuites.getInstance();

    private Huesped huesped;

    @FXML
    private void initialize() {

        Usuario usuario = sistema.getUsuarioAutenticado();

        if (!(usuario instanceof Huesped)) {
            mostrarError("No hay una sesión de huésped activa.");
            mostrarEstadoVacio(true);
            return;
        }

        huesped = (Huesped) usuario;

        nombreHuesped.setText(huesped.getNombre());

        cargarFavoritos();
    }

    /**
     * Carga los apartamentos favoritos del huésped autenticado.
     */
    private void cargarFavoritos() {

        try {

            Conjunto<Apartamento> favoritos =
                    sistema.listarFavoritos(huesped.getDocumento());

            contenedorFavoritos.getChildren().clear();

            totalFavoritos.setText(
                    String.valueOf(favoritos.tamano())
            );

            if (favoritos.estaVacio()) {
                mostrarEstadoVacio(true);
                return;
            }

            mostrarEstadoVacio(false);

            for (Apartamento apartamento : favoritos) {
                contenedorFavoritos.getChildren().add(
                        crearTarjeta(apartamento)
                );
            }

        } catch (ExcepcionSGA e) {

            mostrarError(
                    "No fue posible cargar tus favoritos: "
                            + e.getMessage()
            );

            mostrarEstadoVacio(true);
        }
    }

    /**
     * Construye una tarjeta para cada apartamento favorito.
     */
    private HBox crearTarjeta(Apartamento apartamento) {

        HBox tarjeta = new HBox(18.0);
        tarjeta.getStyleClass().add("favorite-card");

        // Imagen representativa
        VBox imagen = new VBox(8.0);
        imagen.setAlignment(Pos.CENTER);
        imagen.setPrefWidth(155.0);
        imagen.setMinWidth(155.0);
        imagen.getStyleClass().add("apartment-placeholder");

        Label simbolo = new Label("⌂");
        simbolo.getStyleClass().add("apartment-symbol");

        Label textoImagen = new Label("ALOJAMIENTO");
        textoImagen.getStyleClass().add("placeholder-caption");

        imagen.getChildren().addAll(simbolo, textoImagen);

        // Información del apartamento
        VBox detalles = new VBox(10.0);
        HBox.setHgrow(detalles, Priority.ALWAYS);

        Label nombre = new Label(
                textoSeguro(apartamento.getNombre(), "Apartamento")
        );
        nombre.getStyleClass().add("apartment-title");

        Label codigo = new Label(
                "Código: " + textoSeguro(
                        apartamento.getCodigo(),
                        "No disponible"
                )
        );
        codigo.getStyleClass().add("apartment-detail");

        HBox filaUbicacion = new HBox(18.0);

        Label bloque = new Label(
                "Bloque: " + textoSeguro(
                        apartamento.getBloque(),
                        "No disponible"
                )
        );
        bloque.getStyleClass().add("apartment-detail");

        Label piso = new Label(
                "Piso: " + apartamento.getPiso()
        );
        piso.getStyleClass().add("apartment-detail");

        filaUbicacion.getChildren().addAll(bloque, piso);

        HBox filaCapacidad = new HBox(18.0);

        Label dormitorios = new Label(
                "Dormitorios: " + apartamento.getDormitorios()
        );
        dormitorios.getStyleClass().add("apartment-detail");

        Label capacidad = new Label(
                "Capacidad: " + apartamento.getCapacidadMaxima()
                        + " personas"
        );
        capacidad.getStyleClass().add("apartment-detail");

        filaCapacidad.getChildren().addAll(
                dormitorios,
                capacidad
        );

        Label caracteristicas = new Label(
                "Consulta los detalles del alojamiento para conocer "
                        + "sus características."
        );
        caracteristicas.setWrapText(true);
        caracteristicas.getStyleClass().add("section-description");

        Label disponibilidad = new Label(
                "Consulta las fechas para verificar disponibilidad"
        );
        disponibilidad.getStyleClass().add("availability-label");

        detalles.getChildren().addAll(
                nombre,
                codigo,
                filaUbicacion,
                filaCapacidad,
                caracteristicas,
                disponibilidad
        );

        // Acciones de la tarjeta
        VBox acciones = new VBox(12.0);
        acciones.setAlignment(Pos.CENTER_RIGHT);

        Label tarifa = new Label(
                "TARIFA POR PERSONA / NOCHE"
        );
        tarifa.getStyleClass().add("field-label");

        Label precio = new Label("Por consultar");
        precio.getStyleClass().add("apartment-price");

        Button consultar = new Button(
                "Consultar alojamiento"
        );
        consultar.getStyleClass().add("primary-button");
        consultar.setDisable(true);
        consultar.setTooltip(
                new Tooltip(
                        "La consulta de disponibilidad se realiza "
                                + "desde Buscar alojamiento."
                )
        );

        Button quitar = new Button(
                "♡  Quitar de favoritos"
        );
        quitar.getStyleClass().add("remove-button");

        quitar.setOnAction(event -> quitarFavorito(apartamento));

        acciones.getChildren().addAll(
                tarifa,
                precio,
                consultar,
                quitar
        );

        tarjeta.getChildren().addAll(
                imagen,
                detalles,
                acciones
        );

        return tarjeta;
    }

    /**
     * Quita un apartamento de la lista de favoritos.
     */
    private void quitarFavorito(Apartamento apartamento) {

        try {

            boolean eliminado = sistema.quitarFavorito(
                    huesped.getDocumento(),
                    apartamento.getCodigo()
            );

            if (eliminado) {
                cargarFavoritos();
            } else {
                mostrarError(
                        "Este apartamento ya no aparece en tus favoritos."
                );
                cargarFavoritos();
            }

        } catch (ExcepcionSGA e) {

            mostrarError(
                    "No fue posible quitar el favorito: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Muestra u oculta el mensaje cuando no hay favoritos.
     */
    private void mostrarEstadoVacio(boolean mostrar) {

        estadoVacio.setVisible(mostrar);
        estadoVacio.setManaged(mostrar);
    }

    private String textoSeguro(String valor, String alternativo) {

        return valor == null || valor.isBlank()
                ? alternativo
                : valor;
    }

    private void mostrarError(String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.ERROR);

        alerta.setTitle("Favoritos");
        alerta.setHeaderText(
                "No se pudo completar la operación"
        );
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}
