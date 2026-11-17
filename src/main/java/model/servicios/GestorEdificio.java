package model.servicios;

import model.enums.EstadoApartamento;
import model.estructuras.Conjunto;
import model.estructuras.Grafo.Arista;
import model.estructuras.Grafo.Grafo;
import model.estructuras.Grafo.NodoGrafo;
import model.estructuras.Grafo.Tipo_nodos.CuartosAseo;
import model.estructuras.Grafo.Tipo_nodos.Escaleras;
import model.estructuras.Grafo.Tipo_nodos.Pasillo;
import model.estructuras.Grafo.Tipo_nodos.Recepcion;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.modelo.Apartamento;
import model.modelo.SolicitudPreparacion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Administra la topología lógica del alojamiento. Los puntos físicos son nodos;
 * los pasillos son aristas ponderadas (metros), nunca nodos intermedios.
 * Las distancias de la topología inicial son estimadas hasta disponer de un plano.
 */
public class GestorEdificio {
    private final Grafo<Object> grafo = new Grafo<>();
    private final ListaDoblementeEnlazada<NodoGrafo<Object>> nodos = new ListaDoblementeEnlazada<>();
    private final Map<String, NodoGrafo<Object>> escalerasPorBloque = new HashMap<>();
    private final Map<Sector, NodoGrafo<Object>> aseoPorSector = new HashMap<>();
    private NodoGrafo<Object> nodoRecepcion;
    private int siguienteNumeroPasillo = 1;
    private int siguienteNumeroEscalera = 1;
    private int siguienteNumeroAseo = 1;

    private record Sector(String bloque, int piso) { }

    public NodoGrafo<Object> registrarApartamento(Apartamento apartamento) {
        if (apartamento == null) throw new IllegalArgumentException("El apartamento es obligatorio");
        if (buscarNodo(apartamento) != null) throw new IllegalArgumentException("El apartamento ya está registrado en el mapa");
        return registrar(apartamento);
    }

    public NodoGrafo<Object> registrarRecepcion(Recepcion recepcion) {
        if (nodoRecepcion != null) throw new IllegalStateException("Ya existe una recepción registrada");
        nodoRecepcion = registrar(recepcion);
        return nodoRecepcion;
    }

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

    /** Conecta dos puntos físicos con un pasillo de peso no negativo. */
    public void conectar(Object a, Object b, Pasillo pasillo, int distancia) {
        if (pasillo == null) throw new IllegalArgumentException("El pasillo es obligatorio");
        if (distancia < 0) throw new IllegalArgumentException("La distancia no puede ser negativa");
        if (buscarArista(pasillo) != null) throw new IllegalArgumentException("El pasillo ya está registrado");
        grafo.agregarNodo(exigirNodo(a), exigirNodo(b), distancia, pasillo);
    }

    public void cerrarConexion(Object a, Object b) { grafo.cerrarConexion(exigirNodo(a), exigirNodo(b)); }
    public void abrirConexion(Object a, Object b) { grafo.abrirConexion(exigirNodo(a), exigirNodo(b)); }
    public void cerrarPasillo(Pasillo pasillo) { exigirArista(pasillo).cerrar(); }
    public void abrirPasillo(Pasillo pasillo) { exigirArista(pasillo).abrir(); }

    public Arista<Object> buscarArista(Pasillo pasillo) {
        if (pasillo == null) return null;
        for (NodoGrafo<Object> nodo : nodos) {
            for (Arista<Object> arista : nodo.getAdyacentes()) {
                if (arista.getOrigen() == nodo && arista.getEtiqueta() == pasillo) return arista;
            }
        }
        return null;
    }

    private Arista<Object> exigirArista(Pasillo pasillo) {
        Arista<Object> arista = buscarArista(pasillo);
        if (arista == null) throw new IllegalArgumentException("El pasillo no está registrado");
        return arista;
    }

    /** Sincroniza altas y bajas del catálogo sin perder cierres manuales de pasillos. */
    public void sincronizarApartamentos(ListaDoblementeEnlazada<Apartamento> apartamentos) {
        if (apartamentos == null) throw new IllegalArgumentException("La lista de apartamentos es obligatoria");
        if (nodoRecepcion == null) return;

        Set<String> codigosPresentes = new HashSet<>();
        for (Apartamento apartamento : apartamentos) codigosPresentes.add(apartamento.getCodigo());

        List<NodoGrafo<Object>> eliminar = new ArrayList<>();
        for (NodoGrafo<Object> nodo : nodos) {
            if (nodo.getContenido() instanceof Apartamento apartamento
                    && !codigosPresentes.contains(apartamento.getCodigo())) eliminar.add(nodo);
        }
        for (NodoGrafo<Object> nodo : eliminar) {
            grafo.eliminarNodo(nodo);
            nodos.eliminar(nodo);
        }

        for (Apartamento apartamento : apartamentos) {
            if (buscarNodo(apartamento) == null) agregarApartamentoATopologia(apartamento);
        }
    }

    public void eliminarApartamento(String codigo) {
        if (codigo == null || codigo.isBlank()) return;
        NodoGrafo<Object> encontrado = null;
        for (NodoGrafo<Object> nodo : nodos) {
            if (nodo.getContenido() instanceof Apartamento apartamento
                    && codigo.equalsIgnoreCase(apartamento.getCodigo())) { encontrado = nodo; break; }
        }
        if (encontrado != null) {
            grafo.eliminarNodo(encontrado);
            nodos.eliminar(encontrado);
        }
    }

    private void agregarApartamentoATopologia(Apartamento apartamento) {
        registrarApartamento(apartamento);
        String bloque = normalizarBloque(apartamento.getBloque());
        Sector sector = new Sector(bloque, apartamento.getPiso());
        NodoGrafo<Object> escalera = escalerasPorBloque.computeIfAbsent(bloque, b -> {
            NodoGrafo<Object> nueva = registrarEscaleras(new Escaleras(siguienteNumeroEscalera++));
            conectar(nodoRecepcion.getContenido(), nueva.getContenido(), nuevoPasillo(), 8 + (siguienteNumeroEscalera - 2) * 3);
            return nueva;
        });
        NodoGrafo<Object> aseo = aseoPorSector.computeIfAbsent(sector, s -> {
            NodoGrafo<Object> nuevo = registrarCuartoAseo(new CuartosAseo(siguienteNumeroAseo++));
            conectar(escalera.getContenido(), nuevo.getContenido(), nuevoPasillo(), 5 + Math.abs(s.piso()) * 2);
            return nuevo;
        });
        // Dos accesos por apartamento: genera ciclos y rutas alternativas reales en el grafo.
        conectar(escalera.getContenido(), apartamento, nuevoPasillo(), 6);
        conectar(aseo.getContenido(), apartamento, nuevoPasillo(), 4);
    }

    private Pasillo nuevoPasillo() { return new Pasillo(siguienteNumeroPasillo++); }
    private String normalizarBloque(String bloque) { return bloque == null || bloque.isBlank() ? "SIN_BLOQUE" : bloque.trim(); }

    public ListaDoblementeEnlazada<NodoGrafo<Object>> rutaMasCortaDesdeRecepcion(Apartamento apartamento) {
        exigirRecepcion();
        return grafo.armarCamino(nodoRecepcion, exigirNodo(apartamento));
    }

    public Integer distanciaDesdeRecepcion(Apartamento apartamento) {
        exigirRecepcion();
        return grafo.costoCamino(nodoRecepcion, exigirNodo(apartamento));
    }

    /** Calcula cada tramo con Dijkstra y elige como siguiente destino el pendiente más cercano. */
    public ListaDoblementeEnlazada<NodoGrafo<Object>> rutaPreparacionPendientes(
            ListaDoblementeEnlazada<Apartamento> apartamentos) {
        exigirRecepcion();
        if (apartamentos == null) throw new IllegalArgumentException("La lista de apartamentos no puede ser null");
        List<Apartamento> pendientes = new ArrayList<>();
        for (Apartamento apartamento : apartamentos) {
            EstadoApartamento estado = apartamento.getEstadoOperativo();
            if (estado == EstadoApartamento.PENDIENTE_PREPARACION || estado == EstadoApartamento.EN_PREPARACION) {
                if (buscarNodo(apartamento) != null) pendientes.add(apartamento);
            }
        }
        ListaDoblementeEnlazada<NodoGrafo<Object>> recorrido = new ListaDoblementeEnlazada<>();
        NodoGrafo<Object> actual = nodoRecepcion;
        recorrido.agregar(actual);
        while (!pendientes.isEmpty()) {
            Apartamento siguiente = null;
            Integer menorCosto = null;
            for (Apartamento candidato : pendientes) {
                Integer costo = grafo.costoCamino(actual, exigirNodo(candidato));
                if (costo != null && (menorCosto == null || costo < menorCosto)) {
                    menorCosto = costo;
                    siguiente = candidato;
                }
            }
            if (siguiente == null) break; // Los pendientes restantes están aislados por pasillos cerrados.
            NodoGrafo<Object> destino = exigirNodo(siguiente);
            ListaDoblementeEnlazada<NodoGrafo<Object>> tramo = grafo.armarCamino(actual, destino);
            for (NodoGrafo<Object> nodo : tramo) if (recorrido.ultimo() != nodo) recorrido.agregar(nodo);
            actual = destino;
            pendientes.remove(siguiente);
        }
        return recorrido;
    }

    /** Compatibilidad con el flujo de solicitudes existente: conserva su orden de prioridad. */
    public ListaDoblementeEnlazada<NodoGrafo<Object>> rutaPreparacion(
            ListaDoblementeEnlazada<SolicitudPreparacion> solicitudes) {
        exigirRecepcion();
        if (solicitudes == null) throw new IllegalArgumentException("La lista de preparaciones no puede ser null");
        ListaDoblementeEnlazada<NodoGrafo<Object>> recorrido = new ListaDoblementeEnlazada<>();
        NodoGrafo<Object> actual = nodoRecepcion;
        recorrido.agregar(actual);
        for (SolicitudPreparacion solicitud : solicitudes) {
            if (solicitud == null) throw new IllegalArgumentException("La solicitud no puede ser null");
            NodoGrafo<Object> destino = exigirNodo(solicitud.getApartamento());
            ListaDoblementeEnlazada<NodoGrafo<Object>> tramo = grafo.armarCamino(actual, destino);
            if (tramo.estaVacia()) {
                throw new IllegalStateException("No existe una ruta abierta hasta "
                        + solicitud.getApartamento().getCodigo()
                        + ". Revisa los pasillos o conexiones del edificio.");
            }
            for (NodoGrafo<Object> nodo : tramo) if (recorrido.ultimo() != nodo) recorrido.agregar(nodo);
            actual = destino;
        }
        return recorrido;
    }

    public ListaDoblementeEnlazada<Apartamento> apartamentosInaccesibles(ListaDoblementeEnlazada<Apartamento> apartamentos) {
        if (apartamentos == null) throw new IllegalArgumentException("La lista no puede ser null");
        ListaDoblementeEnlazada<Apartamento> resultado = new ListaDoblementeEnlazada<>();
        if (nodoRecepcion == null) { for (Apartamento a : apartamentos) resultado.agregar(a); return resultado; }
        Conjunto<NodoGrafo<Object>> alcanzables = grafo.dfs(nodoRecepcion);
        for (Apartamento apartamento : apartamentos) {
            NodoGrafo<Object> nodo = buscarNodo(apartamento);
            if (nodo == null || !alcanzables.contiene(nodo)) resultado.agregar(apartamento);
        }
        return resultado;
    }

    public ListaDoblementeEnlazada<Apartamento> apartamentosPorBloque(String bloque) {
        if (bloque == null || bloque.isBlank()) throw new IllegalArgumentException("El bloque es obligatorio");
        ListaDoblementeEnlazada<Apartamento> resultado = new ListaDoblementeEnlazada<>();
        for (NodoGrafo<Object> nodo : nodos) {
            if (nodo.getContenido() instanceof Apartamento a && bloque.equalsIgnoreCase(a.getBloque())) resultado.agregar(a);
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
        if (nodo == null) throw new IllegalArgumentException("El elemento no está registrado en el mapa: " + elemento);
        return nodo;
    }
    private void exigirRecepcion() { if (nodoRecepcion == null) throw new IllegalStateException("No se ha registrado la recepción"); }

    public int cantidadNodos() { return grafo.cantidadNodos(); }
    public Grafo<Object> getGrafo() { return grafo; }
    public NodoGrafo<Object> getNodoRecepcion() { return nodoRecepcion; }
    public ListaDoblementeEnlazada<NodoGrafo<Object>> getNodos() { return nodos; }

    /** Crea una topología lógica agrupada por bloque y piso, partiendo de recepción. */
    public void configurarDistribucionInicial(ListaDoblementeEnlazada<Apartamento> apartamentos) {
        if (apartamentos == null) throw new IllegalArgumentException("La lista de apartamentos es obligatoria");
        if (nodoRecepcion == null) registrarRecepcion(new Recepcion(1));
        sincronizarApartamentos(apartamentos);
        // Conexiones laterales entre escaleras para permitir rutas alternativas entre bloques.
        List<NodoGrafo<Object>> escaleras = new ArrayList<>(escalerasPorBloque.values());
        escaleras.sort(Comparator.comparing(n -> ((Escaleras) n.getContenido()).getNumero()));
        for (int i = 1; i < escaleras.size(); i++) {
            NodoGrafo<Object> anterior = escaleras.get(i - 1);
            NodoGrafo<Object> actual = escaleras.get(i);
            if (!anterior.contieneAdyacente(actual)) conectar(anterior.getContenido(), actual.getContenido(), nuevoPasillo(), 10);
        }
    }
}
