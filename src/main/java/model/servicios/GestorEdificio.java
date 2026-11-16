package model.servicios;

import model.estructuras.Conjunto;
import model.estructuras.Grafo.Grafo;
import model.estructuras.Grafo.NodoGrafo;
import model.estructuras.Grafo.Tipo_nodos.CuartosAseo;
import model.estructuras.Grafo.Tipo_nodos.Escaleras;
import model.estructuras.Grafo.Tipo_nodos.Pasillo;
import model.estructuras.Grafo.Tipo_nodos.Recepcion;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.modelo.Apartamento;
import model.modelo.SolicitudPreparacion;

/** Administra el grafo físico reutilizando las clases de Tipo_nodos existentes. */
public class GestorEdificio {
    private final Grafo<Object> grafo = new Grafo<>();
    private final ListaDoblementeEnlazada<NodoGrafo<Object>> nodos = new ListaDoblementeEnlazada<>();
    private NodoGrafo<Object> nodoRecepcion;

    public NodoGrafo<Object> registrarApartamento(Apartamento apartamento) { return registrar(apartamento); }

    public NodoGrafo<Object> registrarRecepcion(Recepcion recepcion) {
        if (nodoRecepcion != null) throw new IllegalStateException("Ya existe una recepción registrada");
        nodoRecepcion = registrar(recepcion);
        return nodoRecepcion;
    }

    public NodoGrafo<Object> registrarPasillo(Pasillo pasillo) { return registrar(pasillo); }
    public NodoGrafo<Object> registrarEscaleras(Escaleras escaleras) { return registrar(escaleras); }
    public NodoGrafo<Object> registrarCuartoAseo(CuartosAseo cuarto) { return registrar(cuarto); }

    private NodoGrafo<Object> registrar(Object elemento) {
        if (elemento == null) throw new IllegalArgumentException("El elemento no puede ser null");
        if (buscarNodo(elemento) != null) throw new IllegalArgumentException("El elemento ya está registrado");
        NodoGrafo<Object> nodo = new NodoGrafo<>(elemento);
        grafo.registrarNodo(nodo);
        nodos.agregar(nodo);
        return nodo;
    }

    public void conectar(Object a, Object b, int distancia) {
        grafo.agregarNodo(exigirNodo(a), exigirNodo(b), distancia);
    }
    public void cerrarConexion(Object a, Object b) { grafo.cerrarConexion(exigirNodo(a), exigirNodo(b)); }
    public void abrirConexion(Object a, Object b) { grafo.abrirConexion(exigirNodo(a), exigirNodo(b)); }

    public ListaDoblementeEnlazada<NodoGrafo<Object>> rutaMasCortaDesdeRecepcion(Apartamento apartamento) {
        exigirRecepcion();
        return grafo.armarCamino(nodoRecepcion, exigirNodo(apartamento));
    }

    public Integer distanciaDesdeRecepcion(Apartamento apartamento) {
        exigirRecepcion();
        return grafo.costoCamino(nodoRecepcion, exigirNodo(apartamento));
    }

    /**
     * Construye el recorrido del personal desde recepción visitando los apartamentos
     * en el mismo orden de prioridad de la cola de preparación. Cada tramo utiliza Dijkstra.
     */
    public ListaDoblementeEnlazada<NodoGrafo<Object>> rutaPreparacion(
            ListaDoblementeEnlazada<SolicitudPreparacion> solicitudes) {
        exigirRecepcion();
        if (solicitudes == null) {
            throw new IllegalArgumentException("La lista de preparaciones no puede ser null");
        }

        ListaDoblementeEnlazada<NodoGrafo<Object>> recorrido = new ListaDoblementeEnlazada<>();
        NodoGrafo<Object> actual = nodoRecepcion;
        recorrido.agregar(actual);

        for (SolicitudPreparacion solicitud : solicitudes) {
            if (solicitud == null) {
                throw new IllegalArgumentException("La solicitud no puede ser null");
            }
            NodoGrafo<Object> destino = exigirNodo(solicitud.getApartamento());
            Integer distancia = grafo.costoCamino(actual, destino);
            if (distancia == null) {
                throw new IllegalStateException("No existe una ruta abierta hasta el apartamento "
                        + solicitud.getApartamento().getCodigo());
            }

            ListaDoblementeEnlazada<NodoGrafo<Object>> tramo = grafo.armarCamino(actual, destino);
            for (int i = 0; i < tramo.tamano(); i++) {
                NodoGrafo<Object> nodo = tramo.obtener(i);
                if (recorrido.ultimo() != nodo) {
                    recorrido.agregar(nodo);
                }
            }
            actual = destino;
        }
        return recorrido;
    }

    public ListaDoblementeEnlazada<Apartamento> apartamentosInaccesibles(
            ListaDoblementeEnlazada<Apartamento> apartamentos) {
        if (apartamentos == null) throw new IllegalArgumentException("La lista no puede ser null");
        ListaDoblementeEnlazada<Apartamento> resultado = new ListaDoblementeEnlazada<>();
        if (nodoRecepcion == null) {
            for (Apartamento a : apartamentos) resultado.agregar(a);
            return resultado;
        }
        model.estructuras.Conjunto<NodoGrafo<Object>> alcanzables = grafo.dfs(nodoRecepcion);
        for (Apartamento a : apartamentos) {
            NodoGrafo<Object> nodo = buscarNodo(a);
            if (nodo == null || !alcanzables.contiene(nodo)) resultado.agregar(a);
        }
        return resultado;
    }

    public ListaDoblementeEnlazada<Apartamento> apartamentosPorBloque(String bloque) {
        if (bloque == null || bloque.isBlank()) throw new IllegalArgumentException("El bloque es obligatorio");
        ListaDoblementeEnlazada<Apartamento> resultado = new ListaDoblementeEnlazada<>();
        Conjunto<NodoGrafo<Object>> visitados = new Conjunto<>();

        // Se recorren todos los componentes para incluir también apartamentos aislados.
        for (NodoGrafo<Object> inicio : grafo) {
            if (visitados.contiene(inicio)) continue;
            Conjunto<NodoGrafo<Object>> componente = grafo.dfs(inicio);
            for (NodoGrafo<Object> nodo : componente) {
                if (!visitados.contiene(nodo)) visitados.agregar(nodo);
                Object contenido = nodo.getContenido();
                if (contenido instanceof Apartamento a
                        && bloque.equalsIgnoreCase(a.getBloque())
                        && !resultado.contiene(a)) {
                    resultado.agregar(a);
                }
            }
        }
        return resultado;
    }

    public NodoGrafo<Object> buscarNodo(Object elemento) {
        if (elemento == null) return null;
        for (NodoGrafo<Object> nodo : nodos) {
            if (nodo.getContenido() == elemento || nodo.getContenido().equals(elemento)) return nodo;
        }
        return null;
    }

    private NodoGrafo<Object> exigirNodo(Object elemento) {
        NodoGrafo<Object> nodo = buscarNodo(elemento);
        if (nodo == null) throw new IllegalArgumentException("El elemento no está registrado");
        return nodo;
    }
    private void exigirRecepcion() {
        if (nodoRecepcion == null) throw new IllegalStateException("No se ha registrado la recepción");
    }
    public int cantidadNodos() { return grafo.cantidadNodos(); }
    public Grafo<Object> getGrafo() { return grafo; }
    public NodoGrafo<Object> getNodoRecepcion() { return nodoRecepcion; }
}
