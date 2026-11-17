package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import model.ApartaSuites;
import model.enums.EstadoApartamento;
import model.interfaces.Usuario;
import model.modelo.Apartamento;
import model.modelo.Empleado;
import model.estructuras.Grafo.Arista;
import model.estructuras.Grafo.NodoGrafo;
import model.estructuras.Grafo.Tipo_nodos.CuartosAseo;
import model.estructuras.Grafo.Tipo_nodos.Escaleras;
import model.estructuras.Grafo.Tipo_nodos.Recepcion;
import model.servicios.GestorEdificio;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class AdminBuildingMapController {
    public Label adminNameLabel;
    public Label adminInitialLabel;
    public Label nodeCountLabel;
    public Label connectionCountLabel;
    public Label inaccessibleCountLabel;
    public Label routeInfoLabel;
    public Label selectedInfoLabel;
    public Pane graphPane;
    public ComboBox<Apartamento> apartmentComboBox;
    public ComboBox<Arista<Object>> corridorComboBox;

    private final ApartaSuites sistema = ApartaSuites.getInstance();
    private final ObservableList<Apartamento> apartamentos = FXCollections.observableArrayList();
    private final ObservableList<Arista<Object>> conexiones = FXCollections.observableArrayList();
    private final Map<NodoGrafo<Object>, Point2D> posiciones = new IdentityHashMap<>();
    private ListaDoblementeEnlazada<NodoGrafo<Object>> rutaActual;
    private Apartamento apartamentoSeleccionado;
    private Timeline actualizadorEstados;

    public void initialize() {
        Usuario usuario = sistema.getUsuarioAutenticado();
        String nombre = usuario instanceof Empleado empleado ? empleado.getNombre() : "Administrador";
        adminNameLabel.setText(nombre);
        adminInitialLabel.setText(nombre == null || nombre.isBlank() ? "A" : nombre.trim().substring(0, 1).toUpperCase(Locale.ROOT));
        try {
            sistema.getGestorEdificio().configurarDistribucionInicial(sistema.listarApartamentos());
            cargarControles();
            dibujarGrafo();
            iniciarActualizacionAutomatica();
        } catch (RuntimeException ex) {
            mostrarError("No se pudo preparar el mapa", ex.getMessage());
        }
    }

    /** Refresca los colores de estado mientras el mapa permanece abierto. */
    private void iniciarActualizacionAutomatica() {
        if (actualizadorEstados != null) actualizadorEstados.stop();
        actualizadorEstados = new Timeline(new KeyFrame(Duration.seconds(3), evento -> dibujarGrafo()));
        actualizadorEstados.setCycleCount(Timeline.INDEFINITE);
        actualizadorEstados.play();
    }

    private void cargarControles() {
        apartamentos.clear();
        for (Apartamento apartamento : sistema.listarApartamentos()) apartamentos.add(apartamento);
        apartmentComboBox.setItems(apartamentos);
        apartmentComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Apartamento a) { return a == null ? "" : a.getCodigo() + " · " + a.getNombre(); }
            @Override public Apartamento fromString(String value) { return null; }
        });
        if (!apartamentos.isEmpty()) apartmentComboBox.getSelectionModel().selectFirst();

        conexiones.clear();
        GestorEdificio gestor = sistema.getGestorEdificio();
        for (NodoGrafo<Object> nodo : gestor.getGrafo()) {
            for (Arista<Object> arista : nodo.getAdyacentes()) {
                if (arista.getOrigen() == nodo) conexiones.add(arista);
            }
        }
        corridorComboBox.setItems(conexiones);
        corridorComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Arista<Object> a) {
                if (a == null) return "";
                return etiquetaNodo(a.getOrigen()) + " ↔ " + etiquetaNodo(a.getDestino()) + " · " + a.getValor() + " m" + (a.isActiva() ? "" : " · CERRADO");
            }
            @Override public Arista<Object> fromString(String value) { return null; }
        });
        if (!conexiones.isEmpty()) corridorComboBox.getSelectionModel().selectFirst();
        apartmentComboBox.setOnAction(e -> seleccionarApartamento());
        if (apartmentComboBox.getValue() != null) seleccionarApartamento();
    }

    private void seleccionarApartamento() {
        apartamentoSeleccionado = apartmentComboBox.getValue();
        if (apartamentoSeleccionado == null) return;
        Integer distancia = sistema.getGestorEdificio().distanciaDesdeRecepcion(apartamentoSeleccionado);
        if (distancia == null) {
            routeInfoLabel.setText("No existe una ruta abierta desde recepción.");
            rutaActual = null;
        } else {
            rutaActual = sistema.getGestorEdificio().rutaMasCortaDesdeRecepcion(apartamentoSeleccionado);
            routeInfoLabel.setText("Ruta desde recepción · " + distancia + " m · " + (rutaActual.tamano() - 1) + " conexiones");
        }
        selectedInfoLabel.setText(apartamentoSeleccionado.getCodigo() + " · " + apartamentoSeleccionado.getNombre()
                + "\nBloque " + apartamentoSeleccionado.getBloque() + " · Piso " + apartamentoSeleccionado.getPiso()
                + "\nEstado: " + traducirEstado(apartamentoSeleccionado.getEstadoOperativo()));
        dibujarGrafo();
    }

    private void dibujarGrafo() {
        if (graphPane == null) return;
        graphPane.getChildren().clear();
        posiciones.clear();
        GestorEdificio gestor = sistema.getGestorEdificio();

        // Distribución lógica por bloque y piso; no pretende sustituir un plano arquitectónico.
        TreeMap<String, TreeMap<Integer, List<NodoGrafo<Object>>>> apartamentosPorUbicacion = new TreeMap<>();
        for (NodoGrafo<Object> nodo : gestor.getGrafo()) {
            if (nodo.getContenido() instanceof Apartamento a) {
                apartamentosPorUbicacion.computeIfAbsent(bloque(a), k -> new TreeMap<>())
                        .computeIfAbsent(a.getPiso(), k -> new ArrayList<>()).add(nodo);
            }
        }
        int maxApartamentosFila = 1;
        for (TreeMap<Integer, List<NodoGrafo<Object>>> pisos : apartamentosPorUbicacion.values()) {
            for (List<NodoGrafo<Object>> fila : pisos.values()) {
                fila.sort(Comparator.comparing(n -> ((Apartamento) n.getContenido()).getCodigo()));
                maxApartamentosFila = Math.max(maxApartamentosFila, fila.size());
            }
        }
        double anchoBloque = Math.max(330, 500 + (maxApartamentosFila - 1) * 95);
        int maxPisos = 1;
        for (TreeMap<Integer, List<NodoGrafo<Object>>> pisos : apartamentosPorUbicacion.values()) maxPisos = Math.max(maxPisos, pisos.size());
        double alto = Math.max(graphPane.getHeight(), 150 + maxPisos * 155);
        double centroY = alto / 2.0;
        double anchoNecesario = Math.max(1000, 210 + apartamentosPorUbicacion.size() * anchoBloque);
        graphPane.setPrefWidth(anchoNecesario);
        graphPane.setPrefHeight(alto);

        Map<String, Integer> indiceBloque = new HashMap<>();
        int bi = 0;
        for (String bloque : apartamentosPorUbicacion.keySet()) indiceBloque.put(bloque, bi++);
        posiciones.put(gestor.getNodoRecepcion(), new Point2D(90, centroY));

        for (NodoGrafo<Object> nodo : gestor.getGrafo()) {
            Object contenido = nodo.getContenido();
            if (contenido instanceof Apartamento a) {
                int columna = indiceBloque.getOrDefault(bloque(a), 0);
                TreeMap<Integer, List<NodoGrafo<Object>>> pisos = apartamentosPorUbicacion.get(bloque(a));
                List<Integer> ordenPisos = new ArrayList<>(pisos.keySet());
                int indicePiso = ordenPisos.indexOf(a.getPiso());
                List<NodoGrafo<Object>> fila = pisos.get(a.getPiso());
                int indiceApartamento = fila.indexOf(nodo);
                double x = 210 + columna * anchoBloque + 510 + indiceApartamento * 95;
                double y = 105 + indicePiso * 155;
                posiciones.put(nodo, new Point2D(x, y));
            } else if (contenido instanceof Escaleras) {
                String bloqueNodo = bloqueAdyacente(nodo);
                int columna = indiceBloque.getOrDefault(bloqueNodo, Math.max(0, ((Escaleras) contenido).getNumero() - 1));
                posiciones.put(nodo, new Point2D(210 + columna * anchoBloque + 285, centroY));
            } else if (contenido instanceof CuartosAseo) {
                Apartamento vecino = apartamentoAdyacente(nodo);
                int columna = vecino == null ? 0 : indiceBloque.getOrDefault(bloque(vecino), 0);
                double y = centroY;
                if (vecino != null) {
                    TreeMap<Integer, List<NodoGrafo<Object>>> pisos = apartamentosPorUbicacion.get(bloque(vecino));
                    List<Integer> ordenPisos = new ArrayList<>(pisos.keySet());
                    int indicePiso = ordenPisos.indexOf(vecino.getPiso());
                    y = 105 + indicePiso * 155;
                }
                posiciones.put(nodo, new Point2D(210 + columna * anchoBloque + 405, y));
            }
        }

        // Las aristas se pintan primero; los nodos y sus etiquetas quedan encima.
        for (NodoGrafo<Object> nodo : gestor.getGrafo()) {
            for (Arista<Object> arista : nodo.getAdyacentes()) {
                if (arista.getOrigen() != nodo) continue;
                Point2D a = posiciones.get(arista.getOrigen());
                Point2D b = posiciones.get(arista.getDestino());
                if (a == null || b == null) continue;
                Line linea = new Line(a.getX(), a.getY(), b.getX(), b.getY());
                linea.setStroke(arista.isActiva() ? Color.web("#9CB4B9") : Color.web("#D66A6A"));
                linea.setStrokeWidth(3);
                if (!arista.isActiva()) linea.getStrokeDashArray().setAll(7.0, 6.0);
                if (rutaContiene(arista.getOrigen(), arista.getDestino())) {
                    linea.setStroke(Color.web("#D6A64F"));
                    linea.setStrokeWidth(6);
                }
                graphPane.getChildren().add(linea);
                Text peso = new Text((a.getX() + b.getX()) / 2 - 12, (a.getY() + b.getY()) / 2 - 9, arista.getValor() + " m");
                peso.setFill(Color.web("#607A80"));
                peso.setFont(Font.font("System", FontWeight.BOLD, 11));
                graphPane.getChildren().add(peso);
            }
        }
        for (NodoGrafo<Object> nodo : gestor.getGrafo()) dibujarNodo(nodo);
        int inaccesibles = gestor.apartamentosInaccesibles(sistema.listarApartamentos()).tamano();
        nodeCountLabel.setText(String.valueOf(gestor.cantidadNodos()));
        connectionCountLabel.setText(String.valueOf(conexiones.size()));
        inaccessibleCountLabel.setText(String.valueOf(inaccesibles));
    }

    private String bloque(Apartamento apartamento) {
        return apartamento.getBloque() == null || apartamento.getBloque().isBlank()
                ? "SIN_BLOQUE" : apartamento.getBloque().trim();
    }

    private Apartamento apartamentoAdyacente(NodoGrafo<Object> nodo) {
        for (Arista<Object> arista : nodo.getAdyacentes()) {
            Object contenido = arista.getExtremo(nodo).getContenido();
            if (contenido instanceof Apartamento apartamento) return apartamento;
        }
        return null;
    }

    private String bloqueAdyacente(NodoGrafo<Object> nodo) {
        Apartamento apartamento = apartamentoAdyacente(nodo);
        return apartamento == null ? "" : bloque(apartamento);
    }

    private void dibujarNodo(NodoGrafo<Object> nodo) {
        Point2D p = posiciones.get(nodo);
        Object contenido = nodo.getContenido();
        Color color = Color.web("#205966");
        String titulo;
        String subtitulo = "";
        if (contenido instanceof Apartamento a) {
            titulo = a.getCodigo();
            subtitulo = a.getNombre();
            color = colorEstado(a.getEstadoOperativo());
        } else if (contenido instanceof Recepcion) {
            titulo = "RECEPCIÓN"; color = Color.web("#123B46");
        } else if (contenido instanceof Escaleras) {
            titulo = "ESCALERAS"; color = Color.web("#B58A3F");
        } else if (contenido instanceof CuartosAseo) {
            titulo = "ASEO"; color = Color.web("#668A79");
        } else return;
        Circle circulo = new Circle(p.getX(), p.getY(), contenido instanceof Apartamento ? 23 : 28, color);
        circulo.setStroke(rutaIncluye(nodo) ? Color.web("#D6A64F") : Color.WHITE);
        circulo.setStrokeWidth(rutaIncluye(nodo) ? 5 : 2);
        graphPane.getChildren().add(circulo);
        Text texto = new Text(p.getX() - (titulo.length() * 3.2), p.getY() + 4, titulo);
        texto.setFill(Color.WHITE); texto.setFont(Font.font("System", FontWeight.BOLD, 9));
        graphPane.getChildren().add(texto);
        if (!subtitulo.isBlank()) {
            Text detalle = new Text(p.getX() - 34, p.getY() + 39, subtitulo.length() > 14 ? subtitulo.substring(0, 13) + "…" : subtitulo);
            detalle.setFill(Color.web("#49666D")); detalle.setFont(Font.font("System", 10)); graphPane.getChildren().add(detalle);
        }
    }

    private boolean rutaIncluye(NodoGrafo<Object> nodo) {
        if (rutaActual == null) return false;
        for (NodoGrafo<Object> paso : rutaActual) if (paso == nodo) return true;
        return false;
    }
    private boolean rutaContiene(NodoGrafo<Object> a, NodoGrafo<Object> b) {
        if (rutaActual == null) return false;
        NodoGrafo<Object> anterior = null;
        for (NodoGrafo<Object> paso : rutaActual) {
            if (anterior == a && paso == b || anterior == b && paso == a) return true;
            anterior = paso;
        }
        return false;
    }

    public void handleToggleCorridor() {
        Arista<Object> seleccionada = corridorComboBox.getValue();
        if (seleccionada == null) { mostrarError("Selecciona un pasillo", "Elige una conexión de la lista."); return; }
        if (seleccionada.isActiva()) seleccionada.cerrar(); else seleccionada.abrir();
        corridorComboBox.setValue(null);
        corridorComboBox.setValue(seleccionada);
        seleccionarApartamento();
        dibujarGrafo();
    }

    public void handleCalculateRoute() { seleccionarApartamento(); }

    public void handleRoutePending() {
        try {
            rutaActual = sistema.getGestorEdificio().rutaPreparacionPendientes(sistema.listarApartamentos());
            int visitables = 0;
            int metros = 0;
            NodoGrafo<Object> anterior = null;
            for (NodoGrafo<Object> nodo : rutaActual) {
                if (nodo.getContenido() instanceof Apartamento) visitables++;
                if (anterior != null) {
                    for (Arista<Object> arista : anterior.getAdyacentes()) {
                        if (arista.getExtremo(anterior) == nodo) { metros += arista.getValor(); break; }
                    }
                }
                anterior = nodo;
            }
            routeInfoLabel.setText("Ruta de preparación desde recepción · " + visitables
                    + " apartamentos alcanzables · " + metros + " m aproximados. Orden: siguiente pendiente más cercano.");
            selectedInfoLabel.setText("Se resaltó el recorrido de preparación. Los pendientes aislados por pasillos cerrados no se incluyen.");
            dibujarGrafo();
        } catch (RuntimeException ex) {
            mostrarError("No se pudo calcular la ruta", ex.getMessage());
        }
    }
    public void handleRefresh() {
        try {
            sistema.getGestorEdificio().configurarDistribucionInicial(sistema.listarApartamentos());
            cargarControles();
            if (apartmentComboBox.getValue() == null) {
                rutaActual = null;
                routeInfoLabel.setText("Registra un apartamento para consultar sus rutas.");
                selectedInfoLabel.setText("No hay apartamentos registrados.");
            }
            dibujarGrafo();
        } catch (RuntimeException ex) {
            mostrarError("No se pudo actualizar el mapa", ex.getMessage());
        }
    }
    public void handleDashboard(MouseEvent event) { cambiarVista(event, "/views/adminDashboard.fxml"); }
    public void handleApartments(MouseEvent event) { cambiarVista(event, "/views/adminApartments.fxml"); }
    public void handleGuests(MouseEvent event) { cambiarVista(event, "/views/adminGuests.fxml"); }
    public void handleEmployees(MouseEvent event) { cambiarVista(event, "/views/adminEmployees.fxml"); }
    public void handleSeasons(MouseEvent event) { cambiarVista(event, "/views/adminSeasons.fxml"); }
    public void handleLogout(ActionEvent event) {
        sistema.cerrarSesion(); cambiarVista(event, "/views/login.fxml");
    }
    private void cambiarVista(javafx.event.Event event, String ruta) {
        try {
            Node origen = (Node) event.getSource(); Scene scene = origen.getScene();
            Parent root = FXMLLoader.load(getClass().getResource(ruta));
            if (actualizadorEstados != null) actualizadorEstados.stop();
            scene.setRoot(root);
        } catch (IOException | RuntimeException ex) { mostrarError("Error de navegación", ex.getMessage()); }
    }
    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR); alerta.setTitle(titulo); alerta.setHeaderText(null);
        alerta.setContentText(mensaje == null ? "Error inesperado" : mensaje); alerta.showAndWait();
    }
    private String etiquetaNodo(NodoGrafo<Object> nodo) {
        Object o = nodo.getContenido(); return o instanceof Apartamento a ? a.getCodigo() : o.getClass().getSimpleName();
    }
    private String traducirEstado(EstadoApartamento estado) {
        return switch (estado) {
            case PREPARADO -> "Preparado"; case OCUPADO -> "Ocupado";
            case PENDIENTE_PREPARACION -> "Pendiente de preparación"; case EN_PREPARACION -> "En preparación";
            case FUERA_DE_SERVICIO -> "Fuera de servicio";
        };
    }
    private Color colorEstado(EstadoApartamento estado) {
        return switch (estado) {
            case PREPARADO -> Color.web("#4F9B70"); case OCUPADO -> Color.web("#5D91B4");
            case PENDIENTE_PREPARACION, EN_PREPARACION -> Color.web("#D6A64F");
            case FUERA_DE_SERVICIO -> Color.web("#C56B6B");
        };
    }
}
