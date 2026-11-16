package model.servicios;

import model.ApartaSuites;
import model.enums.*;
import model.estructuras.*;
import model.estructuras.Cola.*;
import model.estructuras.Grafo.*;
import model.estructuras.Pila.Pila;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.excepciones.*;
import model.modelo.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Banco de pruebas manual del modelo y sus estructuras.
 * No inicia JavaFX ni modifica archivos del proyecto.
 */
public class Main {

    private static int pruebas = 0;
    private static int superadas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("          PRUEBAS INTEGRALES DEL SGA");
        System.out.println("==================================================");

        probar("LISTA DOBLEMENTE ENLAZADA", Main::probarListaDoble);
        probar("COLA", Main::probarCola);
        probar("COLA PRIORITARIA", Main::probarColaPrioritaria);
        probar("PILA", Main::probarPila);
        probar("LISTA CIRCULAR", Main::probarListaCircular);
        probar("CONJUNTO", Main::probarConjunto);
        probar("ARBOL BINARIO DE BUSQUEDA", Main::probarArbol);
        probar("GRAFO Y DIJKSTRA", Main::probarGrafo);
        probar("GESTOR DEL EDIFICIO", Main::probarGestorEdificio);
        probar("MODELO: ESTANCIA Y TEMPORADAS", Main::probarFechasYTemporadas);
        probar("MODELO: APARTAMENTO Y TARIFAS", Main::probarApartamento);
        probar("MODELO: HUESPED Y EMPLEADO", Main::probarPersonas);
        probar("COTIZADOR", Main::probarCotizador);
        probar("FOLIO", Main::probarFolio);
        probar("GESTOR DE FOLIOS Y DESHACER", Main::probarGestorFolios);
        probar("CICLO DE VIDA DE RESERVAS", Main::probarReservas);
        probar("ANTICIPO DEL PORTAL", Main::probarAnticipoPortal);
        probar("CANAL EXTERNO FIFO", Main::probarCanalExterno);
        probar("VENCIMIENTOS", Main::probarVencimientos);
        probar("REPORTES ADMINISTRATIVOS", Main::probarReportes);
        probar("ASIGNADOR DE GRUPOS", Main::probarAsignador);
        probar("TURNOS", Main::probarTurnos);
        probar("ADMINISTRACION", Main::probarAdministracion);
        probar("RESERVA DE GRUPOS GRANDES", Main::probarReservaGrupo);
        probar("PERSISTENCIA DE ARCHIVOS", Main::probarPersistencia);
        probar("ESCENARIO DE DATOS DE PRUEBA", Main::probarEscenarioPrueba);
        probar("FACHADA APARTASUITES", Main::probarFachada);
        probar("NODOS TIPADOS DEL GRAFO", Main::probarNodosTipados);
        probar("PREPARACION DE APARTAMENTOS", Main::probarPreparacion);

        System.out.println();
        System.out.println("==================================================");
        System.out.println("                 RESULTADO FINAL");
        System.out.println("==================================================");
        System.out.println("Pruebas ejecutadas : " + pruebas);
        System.out.println("Pruebas superadas  : " + superadas);
        System.out.println("Pruebas fallidas   : " + fallidas);
        System.out.println("==================================================");
        if (fallidas == 0) {
            System.out.println("RESULTADO: TODAS LAS PRUEBAS PASARON");
        } else {
            System.out.println("RESULTADO: HAY PRUEBAS QUE REQUIEREN REVISION");
        }
    }

    @FunctionalInterface
    private interface CasoPrueba {
        void ejecutar() throws Exception;
    }

    private static void probar(String nombre, CasoPrueba caso) {
        System.out.println("\n[" + nombre + "]");
        try {
            caso.ejecutar();
            superadas++;
            System.out.println("  [OK] Modulo verificado");
        } catch (Throwable error) {
            fallidas++;
            System.out.println("  [ERROR] " + error.getClass().getSimpleName()
                    + ": " + error.getMessage());
            error.printStackTrace(System.out);
        }
        pruebas++;
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }

    private static void igual(Object esperado, Object obtenido, String mensaje) {
        if (esperado == null ? obtenido != null : !esperado.equals(obtenido)) {
            throw new AssertionError(mensaje + " | esperado=" + esperado + ", obtenido=" + obtenido);
        }
    }

    private static void cerca(double esperado, double obtenido, String mensaje) {
        if (Math.abs(esperado - obtenido) > 0.0001) {
            throw new AssertionError(mensaje + " | esperado=" + esperado + ", obtenido=" + obtenido);
        }
    }

    private static void esperaExcepcion(Class<? extends Throwable> tipo, Runnable accion, String mensaje) {
        try {
            accion.run();
        } catch (Throwable e) {
            Throwable causa = e;
            while (causa != null) {
                if (tipo.isInstance(causa)) return;
                causa = causa.getCause();
            }
            throw new AssertionError(mensaje + ": se esperaba " + tipo.getSimpleName()
                    + " pero se obtuvo " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(mensaje + ": no se lanzó " + tipo.getSimpleName());
    }

    private static Apartamento apartamento(String codigo, int capacidad, int dormitorios) {
        Apartamento a = new Apartamento(codigo, "Suite " + codigo, "A", 1,
                dormitorios, capacidad, "WiFi, cocina");
        for (Temporada t : Temporada.values()) {
            a.definirTarifa(t, switch (t) {
                case BAJA -> 100_000;
                case MEDIA -> 150_000;
                case ALTA -> 200_000;
            });
        }
        return a;
    }

    private static Huesped huesped(String documento) {
        return new Huesped(documento, "Huesped " + documento,
                LocalDate.of(1995, 4, 10), "3001234567", documento + "@correo.com");
    }

    private static ListaDoblementeEnlazada<Ocupante> ocupantes(int cantidad, LocalDate nacimiento) {
        ListaDoblementeEnlazada<Ocupante> lista = new ListaDoblementeEnlazada<>();
        for (int i = 1; i <= cantidad; i++) {
            lista.agregar(new Ocupante("Ocupante " + i, nacimiento));
        }
        return lista;
    }

    private static Estancia estancia(LocalDate entrada, int noches) throws FechasInvalidasException {
        return new Estancia(entrada, entrada.plusDays(noches));
    }

    private static void probarListaDoble() {
        ListaDoblementeEnlazada<Integer> l = new ListaDoblementeEnlazada<>();
        verificar(l.estaVacia(), "La lista nueva debe estar vacía");
        l.agregar(10);
        l.agregar(30);
        l.insertarEn(1, 20);
        igual(3, l.tamano(), "Tamaño después de insertar");
        igual(10, l.primero(), "Primer elemento");
        igual(30, l.ultimo(), "Último elemento");
        igual(20, l.obtener(1), "Elemento en índice 1");
        verificar(l.eliminar(20), "Debe eliminar el 20");
        igual(2, l.tamano(), "Tamaño después de eliminar");
        l.insertarOrdenado(15, Comparator.naturalOrder());
        igual(15, l.obtener(1), "Inserción ordenada");
        igual(0, l.indiceDe(10), "Índice de 10");
        verificar(l.contiene(30), "Debe contener 30");
        l.eliminarEn(0);
        igual(15, l.primero(), "Eliminar por índice");
        Iterator<Integer> it = l.iterator();
        igual(15, it.next(), "Iterador");
        igual(30, it.next(), "Iterador segundo elemento");
        verificar(!it.hasNext(), "Iterador debe terminar");
        l.limpiar();
        verificar(l.estaVacia(), "Limpiar debe vaciar la lista");
        esperaExcepcion(EstructuraVaciaException.class, l::primero, "primero() en lista vacía");
    }

    private static void probarCola() {
        Cola<Integer> q = new Cola<>();
        q.add(1); q.add(2); q.add(3);
        igual(3, q.size(), "Tamaño de cola");
        igual(1, q.peek(), "Frente de cola");
        igual(1, q.poll(), "FIFO primer elemento");
        igual(2, q.poll(), "FIFO segundo elemento");
        verificar(q.contains(3), "contains debe encontrar 3");
        Cola<Integer> otra = new Cola<>();
        otra.add(4); otra.add(5);
        q.merge(otra);
        igual(3, q.size(), "merge agrega sin vaciar origen");
        igual(3, q.poll(), "La cola conserva FIFO antes de los elementos fusionados");
        q.clear();
        verificar(q.isEmpty(), "clear vacía cola");
        esperaExcepcion(EstructuraVaciaException.class, q::poll, "poll() en cola vacía");
    }

    private static void probarColaPrioritaria() {
        ColaPrioritaria<Integer> q = new ColaPrioritaria<>(Comparator.naturalOrder());
        q.add(5); q.add(1); q.add(3); q.add(1);
        igual(1, q.poll(), "Menor prioridad primero");
        igual(1, q.poll(), "Elementos con igual prioridad se conservan");
        igual(3, q.poll(), "Segundo nivel");
        igual(5, q.poll(), "Último nivel");
        verificar(q.isEmpty(), "Cola prioritaria vacía al extraer todo");
    }

    private static void probarPila() {
        Pila<String> p = new Pila<>();
        p.push("A"); p.push("B"); p.push("C");
        igual("C", p.peek(), "Tope");
        igual("C", p.pop(), "LIFO primero");
        igual("B", p.pop(), "LIFO segundo");
        igual(1, p.size(), "Tamaño");
        p.clear();
        verificar(p.isEmpty(), "clear vacía pila");
        esperaExcepcion(EstructuraVaciaException.class, p::pop, "pop() en pila vacía");
    }

    private static void probarListaCircular() {
        ListaCircular<String> l = new ListaCircular<>();
        l.agregar("Ana"); l.agregar("Luis"); l.agregar("Eva");
        igual("Ana", l.actual(), "Actual inicial");
        igual("Ana", l.siguiente(), "siguiente retorna el actual");
        igual("Luis", l.actual(), "El actual avanza");
        igual("Luis", l.siguiente(), "Segundo turno");
        igual("Eva", l.actual(), "Tercer elemento");
        igual("Eva", l.siguiente(), "Tercer turno");
        igual("Ana", l.actual(), "Debe volver al primero");
        verificar(l.eliminar("Ana"), "Eliminar nodo actual");
        igual("Luis", l.actual(), "Actual luego de eliminar");
        igual(2, l.tamano(), "Tamaño luego de eliminar");
        int contados = 0;
        for (String ignored : l) contados++;
        igual(2, contados, "Iterador circular recorre una sola vuelta");
        l.limpiar();
        esperaExcepcion(EstructuraVaciaException.class, l::actual, "actual() vacía");
    }

    private static void probarConjunto() {
        Conjunto<String> c = new Conjunto<>();
        verificar(c.agregar("A"), "Primer agregado");
        verificar(!c.agregar("A"), "No debe duplicar");
        verificar(c.agregar("B"), "Segundo elemento");
        verificar(c.contiene("B"), "contiene");
        igual(2, c.tamano(), "Tamaño sin duplicados");
        verificar(c.eliminar("A"), "Eliminar elemento");
        verificar(!c.contiene("A"), "Ya no contiene A");
        int n = 0; for (String ignored : c) n++;
        igual(1, n, "Iterador del conjunto");
    }

    private static void probarArbol() {
        ArbolBinarioBusqueda<Integer> a = new ArbolBinarioBusqueda<>();
        for (int x : new int[]{8, 4, 12, 2, 6, 10, 14, 5, 7}) a.insertar(x);
        igual(9, a.tamano(), "Tamaño árbol");
        igual(4, a.altura(), "Altura árbol");
        igual(2, a.obtenerMinimo(), "Mínimo");
        igual(14, a.obtenerMaximo(), "Máximo");
        igual(6, a.buscar(6), "Buscar existente");
        verificar(a.buscar(99) == null, "Buscar inexistente");
        igual(2, a.inOrden().primero(), "Inorden empieza por mínimo");
        verificar(a.eliminar(4), "Eliminar nodo con dos hijos");
        verificar(!a.contiene(4), "Nodo eliminado");
        igual(8, a.tamano(), "Tamaño tras eliminar");
        int cantidad = 0; for (Integer ignored : a) cantidad++;
        igual(8, cantidad, "Iterador recorre todos");
        a.eliminar(2); a.eliminar(5); a.eliminar(6); a.eliminar(7);
        a.eliminar(8); a.eliminar(10); a.eliminar(12); a.eliminar(14);
        verificar(a.estaVacio(), "Árbol debe quedar vacío");
        esperaExcepcion(EstructuraVaciaException.class, a::obtenerMinimo, "Mínimo en árbol vacío");
    }

    private static void probarGrafo() {
        Grafo<String> g = new Grafo<>();
        NodoGrafo<String> a = new NodoGrafo<>("A");
        NodoGrafo<String> b = new NodoGrafo<>("B");
        NodoGrafo<String> c = new NodoGrafo<>("C");
        NodoGrafo<String> d = new NodoGrafo<>("D");
        NodoGrafo<String> aislado = new NodoGrafo<>("X");
        g.agregarNodo(a, b, 2);
        g.agregarNodo(a, c, 8);
        g.agregarNodo(b, c, 1);
        g.agregarNodo(b, d, 5);
        g.agregarNodo(c, d, 2);
        igual(4, g.BFS(a, d).tamano(), "BFS encuentra los cuatro nodos alcanzables");
        igual(4, g.dfs(a).tamano(), "DFS encuentra los cuatro nodos alcanzables");
        igual(Integer.valueOf(5), g.costoCamino(a, d), "Dijkstra costo A-B-C-D");
        ListaDoblementeEnlazada<NodoGrafo<String>> camino = g.armarCamino(a, d);
        igual("A", camino.primero().getContenido(), "Inicio camino");
        igual("D", camino.ultimo().getContenido(), "Fin camino");
        verificar(g.costoCamino(a, aislado) == null, "No existe camino al nodo aislado");
        ListaDoblementeEnlazada<NodoGrafo<String>> alcanzables = g.obtenerGrafo(a,
                new ListaDoblementeEnlazada<>());
        igual(4, alcanzables.tamano(), "DFS recursivo del componente conectado");
        verificar(g.imprimir(a).contains("B"), "Imprimir adyacencias");
        int nodosIterados = 0;
        for (NodoGrafo<String> nodo : g) nodosIterados++;
        igual(4, nodosIterados, "El grafo permite iterar sus nodos registrados");
        esperaExcepcion(IllegalArgumentException.class, () -> g.agregarNodo(a, a, 1),
                "No permitir lazos");
        esperaExcepcion(IllegalArgumentException.class, () -> g.agregarNodo(a, b, 1),
                "No permitir arista duplicada");
    }

    private static void probarGestorEdificio() {
        GestorEdificio gestor = new GestorEdificio();

        model.estructuras.Grafo.Tipo_nodos.Recepcion recepcion =
                new model.estructuras.Grafo.Tipo_nodos.Recepcion(1);
        model.estructuras.Grafo.Tipo_nodos.Pasillo pasillo1 =
                new model.estructuras.Grafo.Tipo_nodos.Pasillo(1);
        model.estructuras.Grafo.Tipo_nodos.Pasillo pasillo2 =
                new model.estructuras.Grafo.Tipo_nodos.Pasillo(2);
        model.estructuras.Grafo.Tipo_nodos.Pasillo pasillo3 =
                new model.estructuras.Grafo.Tipo_nodos.Pasillo(3);
        model.estructuras.Grafo.Tipo_nodos.Escaleras escaleras =
                new model.estructuras.Grafo.Tipo_nodos.Escaleras(1);
        model.estructuras.Grafo.Tipo_nodos.CuartosAseo aseo =
                new model.estructuras.Grafo.Tipo_nodos.CuartosAseo(1);

        Apartamento aptoA = apartamento("ED-1", 2, 1);
        aptoA.setBloque("A");
        Apartamento aptoB = apartamento("ED-2", 3, 2);
        aptoB.setBloque("A");
        Apartamento aptoAislado = apartamento("ED-3", 2, 1);
        aptoAislado.setBloque("B");

        gestor.registrarRecepcion(recepcion);
        gestor.registrarEscaleras(escaleras);
        gestor.registrarCuartoAseo(aseo);
        gestor.registrarApartamento(aptoA);
        gestor.registrarApartamento(aptoB);
        gestor.registrarApartamento(aptoAislado);

        gestor.conectar(recepcion, escaleras, pasillo1, 2);
        gestor.conectar(escaleras, aptoA, pasillo2, 7);
        gestor.conectar(recepcion, aptoB, pasillo3, 7);

        igual(9, gestor.distanciaDesdeRecepcion(aptoA),
                "Dijkstra calcula distancia desde recepción");
        igual(3, gestor.rutaMasCortaDesdeRecepcion(aptoA).tamano(),
                "La ruta contiene recepción, escaleras y apartamento");
        verificar(gestor.buscarArista(pasillo1) != null,
                "El pasillo se conserva como etiqueta de una arista");
        verificar(gestor.buscarNodo(pasillo1) == null,
                "El pasillo no se registra como nodo");
        igual(1, gestor.apartamentosInaccesibles(
                        listaApartamentos(aptoA, aptoB, aptoAislado)).tamano(),
                "Detectar apartamento aislado");
        igual(2, gestor.apartamentosPorBloque("A").tamano(),
                "Agrupar apartamentos por bloque");

        gestor.cerrarPasillo(pasillo2);
        verificar(gestor.distanciaDesdeRecepcion(aptoA) == null,
                "Cerrar un pasillo impide el acceso");
        gestor.abrirPasillo(pasillo2);
        igual(Integer.valueOf(9), gestor.distanciaDesdeRecepcion(aptoA),
                "Reabrir el pasillo recupera la ruta");
        igual(6, gestor.cantidadNodos(),
                "El grafo conserva los puntos físicos y apartamentos, no los pasillos");
    }

    private static ListaDoblementeEnlazada<Apartamento> listaApartamentos(Apartamento... apartamentos) {
        ListaDoblementeEnlazada<Apartamento> lista = new ListaDoblementeEnlazada<>();
        for (Apartamento apartamento : apartamentos) lista.agregar(apartamento);
        return lista;
    }

    private static void probarFechasYTemporadas() throws Exception {
        LocalDate inicio = LocalDate.of(2026, 12, 20);
        Estancia e = new Estancia(inicio, inicio.plusDays(3));
        igual(3, e.noches(), "Noches de estancia");
        verificar(e.incluyeNoche(inicio), "Incluye check-in");
        verificar(!e.incluyeNoche(inicio.plusDays(3)), "No incluye checkout");
        Estancia contigua = new Estancia(inicio.plusDays(3), inicio.plusDays(5));
        verificar(!e.seSolapaCon(contigua), "Estancias contiguas no se solapan");
        esperaExcepcion(FechasInvalidasException.class,
                () -> { try { new Estancia(inicio, inicio); } catch (FechasInvalidasException ex) { throw new RuntimeException(ex); } },
                "No aceptar estancia de cero noches");

        GestorTemporadas gt = new GestorTemporadas();
        PeriodoTemporada alta = new PeriodoTemporada(inicio, inicio.plusDays(5), Temporada.ALTA);
        gt.definirPeriodo(alta);
        igual(Temporada.ALTA, gt.temporadaDe(inicio.plusDays(2)), "Temporada definida");
        igual(Temporada.BAJA, gt.temporadaDe(inicio.plusDays(10)), "Temporada por defecto");
        esperaExcepcion(FechasInvalidasException.class, () -> {
            try {
                gt.definirPeriodo(new PeriodoTemporada(inicio.plusDays(2), inicio.plusDays(7), Temporada.MEDIA));
            } catch (FechasInvalidasException ex) { throw new RuntimeException(ex); }
        }, "No permitir temporadas solapadas");
        verificar(gt.eliminarPeriodo(alta), "Eliminar periodo");
    }

    private static void probarApartamento() {
        Apartamento a = apartamento("A-101", 4, 2);
        igual(EstadoApartamento.PREPARADO, a.getEstadoOperativo(), "Estado inicial");
        igual(100_000.0, a.tarifaPara(Temporada.BAJA), "Tarifa baja");
        a.definirTarifa(Temporada.BAJA, 110_000);
        igual(110_000.0, a.tarifaPara(Temporada.BAJA), "Actualizar tarifa sin duplicar");
        esperaExcepcion(IllegalArgumentException.class, () -> a.ponerFueraDeServicio(" "),
                "Motivo obligatorio para fuera de servicio");
        a.ponerFueraDeServicio("Mantenimiento eléctrico");
        igual(EstadoApartamento.FUERA_DE_SERVICIO, a.getEstadoOperativo(), "Fuera de servicio");
        igual("Mantenimiento eléctrico", a.getMotivoFueraServicio(), "Motivo guardado");
        a.cambiarEstado(EstadoApartamento.PREPARADO);
        igual(null, a.getMotivoFueraServicio(), "Limpiar motivo al reactivar");
    }

    private static void probarPersonas() throws Exception {
        Huesped h = huesped("1001");
        Apartamento a = apartamento("F-1", 3, 2);
        verificar(h.agregarFavorito(a), "Agregar favorito");
        verificar(!h.agregarFavorito(a), "No duplicar favorito");
        verificar(h.quitarFavorito(a), "Quitar favorito");
        verificar(!h.quitarFavorito(a), "No quitar favorito inexistente");
        Empleado e = new Empleado("2001", "Recepcionista", RolEmpleado.RECEPCIONISTA, "3001111111");
        igual("2001", Empleado.paraBusqueda("2001").getDocumento(), "Clave de búsqueda empleado");
        igual("1001", Huesped.paraBusqueda("1001").getDocumento(), "Clave de búsqueda huésped");
    }

    private static void probarCotizador() throws Exception {
        LocalDate d = LocalDate.of(2026, 12, 20);
        Apartamento a = apartamento("C-1", 4, 2);
        GestorTemporadas gt = new GestorTemporadas();
        gt.definirPeriodo(new PeriodoTemporada(d.plusDays(1), d.plusDays(1), Temporada.ALTA));
        gt.definirPeriodo(new PeriodoTemporada(d.plusDays(2), d.plusDays(2), Temporada.MEDIA));
        CotizadorService cotizador = new CotizadorService(gt);
        ListaDoblementeEnlazada<Ocupante> personas = new ListaDoblementeEnlazada<>();
        personas.agregar(new Ocupante("Adulto", LocalDate.of(1990, 1, 1)));
        personas.agregar(new Ocupante("Niño 5", LocalDate.of(2021, 1, 1)));
        Cotizacion c = cotizador.cotizar(a, new Estancia(d, d.plusDays(3)), personas);
        // Baja: 100.000; alta: 200.000; media: 150.000. Solo paga el adulto.
        cerca(450_000, c.getTotal(), "Total con temporada variable y menor gratuito");
        igual(3, c.getDesglose().tamano(), "Desglose por cada noche");
        igual(1, c.getDesglose().obtener(0).getPersonasCobradas(), "Menor no paga");
        esperaExcepcion(CapacidadExcedidaException.class, () -> {
            try {
                cotizador.cotizar(a, new Estancia(d, d.plusDays(1)), ocupantes(5, LocalDate.of(1990, 1, 1)));
            } catch (CapacidadExcedidaException ex) { throw new RuntimeException(ex); }
            catch (FechasInvalidasException ex) { throw new RuntimeException(ex); }
        }, "No exceder capacidad");
    }

    private static MovimientoFolio movimiento(String id, TipoMovimientoFolio tipo, String concepto, String valor) {
        return new MovimientoFolio(id, tipo, concepto, new BigDecimal(valor),
                LocalDateTime.of(2026, 11, 15, 12, 0), null);
    }

    private static void probarFolio() {
        Folio f = new Folio("F-1", "RES-1");
        f.registrarCargo(movimiento("M1", TipoMovimientoFolio.CARGO, "Alojamiento", "500000"));
        f.registrarPago(movimiento("M2", TipoMovimientoFolio.PAGO, "Anticipo", "200000"));
        f.registrarDevolucion(movimiento("M3", TipoMovimientoFolio.DEVOLUCION, "Devolución", "50000"));
        igual(new BigDecimal("500000"), f.calcularTotalCargos(), "Total cargos");
        igual(new BigDecimal("200000"), f.calcularTotalPagos(), "Total pagos");
        igual(new BigDecimal("50000"), f.calcularTotalDevoluciones(), "Total devoluciones");
        igual(new BigDecimal("350000"), f.calcularSaldo(), "Saldo = cargos + devoluciones - pagos");
        igual(3, f.numeroMovimientos(), "Movimientos inmutables conservados");
        esperaExcepcion(IllegalArgumentException.class,
                () -> f.registrarPago(movimiento("M4", TipoMovimientoFolio.PAGO, "Pago excesivo", "999999")),
                "No aceptar pago superior al saldo");
        esperaExcepcion(IllegalArgumentException.class,
                () -> f.registrarDevolucion(movimiento("M5", TipoMovimientoFolio.DEVOLUCION, "Devolución excesiva", "999999")),
                "No aceptar devolución superior a pagos");
    }

    private static void probarGestorFolios() throws Exception {
        LocalDate entrada = LocalDate.of(2027, 2, 10);
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 16, 10, 0);
        Apartamento apartamento = apartamento("FOLIO-GESTOR", 3, 1);
        GestorReservas gestor = new GestorReservas(
                new CotizadorService(new GestorTemporadas()),
                new AsignadorGrupos(),
                new GestorVencimientos()
        );
        Reserva reserva = gestor.crearReserva(
                huesped("FOLIO-GESTOR-H"), apartamento,
                new Estancia(entrada, entrada.plusDays(1)),
                ocupantes(1, LocalDate.of(1990, 1, 1)), CanalReserva.DIRECTA, ahora
        );

        MovimientoFolio cargo = gestor.registrarCargo(
                reserva, "Consumo adicional", new BigDecimal("20000"), ahora.plusMinutes(1));
        verificar(gestor.puedeDeshacer(reserva), "El último cargo debe poder deshacerse");
        MovimientoFolio anulacionCargo = gestor.deshacerUltimaOperacion(reserva, ahora.plusMinutes(2));
        igual(TipoMovimientoFolio.ANULACION_CARGO, anulacionCargo.getTipo(),
                "Deshacer cargo registra una anulación de cargo");
        igual(cargo.getId(), anulacionCargo.getIdMovimientoRelacionado(),
                "La anulación referencia el cargo original");

        MovimientoFolio pago = gestor.registrarPago(
                reserva, "Abono en recepción", new BigDecimal("30000"), ahora.plusMinutes(3));
        MovimientoFolio anulacionPago = gestor.deshacerUltimaOperacion(reserva, ahora.plusMinutes(4));
        igual(TipoMovimientoFolio.ANULACION_PAGO, anulacionPago.getTipo(),
                "Deshacer pago registra una anulación de pago");
        igual(pago.getId(), anulacionPago.getIdMovimientoRelacionado(),
                "La anulación referencia el pago original");
        verificar(gestor.puedeDeshacer(reserva),
                "El cargo inicial del alojamiento también permanece en la pila");
        gestor.deshacerUltimaOperacion(reserva, ahora.plusMinutes(5));
        verificar(!gestor.puedeDeshacer(reserva), "La pila queda vacía tras deshacer el cargo inicial");
        igual(6, reserva.getFolio().numeroMovimientos(),
                "Los movimientos originales y compensatorios permanecen en el folio");

        Apartamento apartamentoCancelacion = apartamento("FOLIO-DEV", 2, 1);
        Reserva cancelable = gestor.crearReserva(
                huesped("FOLIO-DEV-H"), apartamentoCancelacion,
                new Estancia(entrada, entrada.plusDays(1)),
                ocupantes(1, LocalDate.of(1990, 1, 1)), CanalReserva.DIRECTA, ahora
        );
        gestor.registrarPago(cancelable, "Pago recibido", new BigDecimal("50000"), ahora.plusMinutes(5));
        double devuelto = gestor.cancelar(cancelable, entrada.minusDays(10), ahora.plusMinutes(6));
        cerca(50_000, devuelto, "La cancelación devuelve lo realmente pagado");
        igual(new BigDecimal("50000.00"), cancelable.getFolio().calcularTotalDevoluciones(),
                "La devolución queda registrada en el folio");
        igual(TipoMovimientoFolio.DEVOLUCION,
                cancelable.getFolio().obtenerMovimiento(2).getTipo(),
                "La devolución queda como movimiento inmutable");
        verificar(!gestor.puedeDeshacer(cancelable),
                "La devolución actúa como barrera para no deshacer movimientos anteriores");
    }

    private static void probarReservas() throws Exception {
        LocalDate entrada = LocalDate.of(2027, 1, 10);
        Apartamento a = apartamento("R-1", 4, 2);
        Huesped h = huesped("3001");
        GestorTemporadas gt = new GestorTemporadas();
        GestorVencimientos gv = new GestorVencimientos();
        GestorReservas gr = new GestorReservas(new CotizadorService(gt), new AsignadorGrupos(), gv);
        Estancia primera = new Estancia(entrada, entrada.plusDays(3));
        Reserva r = gr.crearReserva(h, a, primera, ocupantes(2, LocalDate.of(1990, 1, 1)), CanalReserva.DIRECTA,
                LocalDateTime.of(2026, 11, 1, 10, 0));
        igual(EstadoReserva.CONFIRMADA, r.getEstado(), "Canal directo confirma");
        verificar(!gr.estaLibre(a, new Estancia(entrada.plusDays(1), entrada.plusDays(4))),
                "Detectar solapamiento");
        verificar(gr.estaLibre(a, new Estancia(entrada.plusDays(3), entrada.plusDays(5))),
                "Permitir estancia contigua");
        esperaExcepcion(ReservaSolapadaException.class, () -> {
            try {
                gr.crearReserva(h, a, new Estancia(entrada.plusDays(2), entrada.plusDays(4)),
                        ocupantes(1, LocalDate.of(1990, 1, 1)), CanalReserva.DIRECTA);
            } catch (ReservaSolapadaException ex) { throw new RuntimeException(ex); }
            catch (CapacidadExcedidaException ex) { throw new RuntimeException(ex); }
            catch (FechasInvalidasException ex) { throw new RuntimeException(ex); }
        }, "No vender noches ya ocupadas");
        r.iniciar();
        igual(EstadoReserva.EN_CURSO, r.getEstado(), "Iniciar estancia");
        r.finalizar();
        igual(EstadoReserva.FINALIZADA, r.getEstado(), "Finalizar estancia");
        verificar(r.ocupaNoches(), "Reserva finalizada mantiene su registro histórico");
        esperaExcepcion(TransicionInvalidaException.class, r::cancelar,
                "No cancelar reserva finalizada");

        Apartamento b = apartamento("R-2", 2, 1);
        Reserva cancelable = gr.crearReserva(h, b, primera, ocupantes(1, LocalDate.of(1990, 1, 1)),
                CanalReserva.DIRECTA);
        gr.registrarPago(cancelable, "Pago recibido", new BigDecimal("200000"),
                LocalDateTime.of(2026, 11, 1, 11, 0));
        cerca(200_000, gr.cancelar(cancelable, 200_000, entrada.minusDays(10)),
                "Devolución total limitada al dinero realmente pagado");
        igual(new BigDecimal("200000.00"), cancelable.getFolio().calcularTotalDevoluciones(),
                "La devolución se registra en el folio");
        igual(EstadoReserva.CANCELADA, cancelable.getEstado(), "Cancelación por el gestor");
        verificar(!b.getReservas().contiene(cancelable), "El gestor debe retirar la reserva del árbol");

        Apartamento c = apartamento("R-3", 2, 1);
        Reserva noShow = gr.crearReserva(h, c, primera, ocupantes(1, LocalDate.of(1990, 1, 1)),
                CanalReserva.DIRECTA);
        gr.marcarNoShow(noShow);
        igual(EstadoReserva.NO_SHOW, noShow.getEstado(), "Marcar no-show");
        verificar(!noShow.ocupaNoches(), "No-show deja de ocupar noches");
        verificar(!c.getReservas().contiene(noShow), "No-show libera el árbol del apartamento");
    }

    private static void probarAnticipoPortal() throws Exception {
        LocalDate entrada = LocalDate.of(2027, 3, 10);
        LocalDateTime creada = LocalDateTime.of(2026, 11, 15, 10, 0);
        Apartamento a = apartamento("PORTAL-1", 3, 1);
        Huesped h = huesped("5001");
        GestorReservas gr = new GestorReservas(
                new CotizadorService(new GestorTemporadas()),
                new AsignadorGrupos(),
                new GestorVencimientos()
        );

        Reserva reserva = gr.crearReserva(
                h, a, new Estancia(entrada, entrada.plusDays(2)),
                ocupantes(1, LocalDate.of(1990, 1, 1)), CanalReserva.PORTAL, creada
        );

        igual(EstadoReserva.PENDIENTE, reserva.getEstado(), "Portal inicia pendiente");
        igual(new BigDecimal("200000.00"), reserva.getFolio().calcularTotalCargos(),
                "El folio registra el cargo total del alojamiento");
        cerca(60_000, gr.calcularAnticipo(reserva), "Anticipo del 30 %");

        esperaExcepcion(IllegalStateException.class, () -> gr.confirmar(reserva),
                "No confirmar Portal sin anticipo");

        boolean confirmada = gr.registrarPagoPortal(reserva, 30_000,
                creada.plusHours(1));
        verificar(!confirmada, "Un abono inferior al 30 % no confirma");
        igual(EstadoReserva.PENDIENTE, reserva.getEstado(), "Sigue pendiente tras abono parcial");

        confirmada = gr.registrarPagoPortal(reserva, 30_000,
                creada.plusHours(2));
        verificar(confirmada, "El pago acumulado del 30 % confirma");
        igual(EstadoReserva.CONFIRMADA, reserva.getEstado(), "Estado confirmado al completar anticipo");
        igual(new BigDecimal("60000.00"), reserva.getFolio().calcularTotalPagos(),
                "El folio conserva ambos abonos");
        igual(new BigDecimal("140000.00"), reserva.getFolio().calcularSaldo(),
                "Saldo pendiente despues del anticipo");
        igual(3, reserva.getFolio().numeroMovimientos(), "Cargo y dos pagos quedan en el historial");
        esperaExcepcion(IllegalStateException.class,
                () -> gr.deshacerUltimaOperacion(reserva, creada.plusHours(3)),
                "No deshacer un pago que dejaría el Portal por debajo del anticipo mínimo");
        igual(new BigDecimal("60000.00"), reserva.getFolio().calcularTotalPagos(),
                "El pago se conserva si no se permite deshacerlo");

        Apartamento b = apartamento("PORTAL-2", 2, 1);
        Reserva directa = gr.crearReserva(h, b, new Estancia(entrada, entrada.plusDays(1)),
                ocupantes(1, LocalDate.of(1990, 1, 1)), CanalReserva.DIRECTA, creada);
        esperaExcepcion(IllegalArgumentException.class,
                () -> gr.registrarPagoPortal(directa, 10_000, creada.plusHours(3)),
                "El metodo de anticipo del Portal rechaza reservas directas");
    }

    private static void probarCanalExterno() throws Exception {
        GestorReservas reservas = new GestorReservas(
                new CotizadorService(new GestorTemporadas()),
                new AsignadorGrupos(),
                new GestorVencimientos()
        );
        GestorCanalExterno externo = new GestorCanalExterno(reservas);

        Apartamento apartamento = apartamento("EXT-1", 3, 1);
        esperaExcepcion(IllegalArgumentException.class, () -> {
            try {
                reservas.crearReserva(huesped("EXT-DIRECTO"), apartamento,
                        new Estancia(LocalDate.of(2027, 8, 1), LocalDate.of(2027, 8, 2)),
                        ocupantes(1, LocalDate.of(1990, 1, 1)), CanalReserva.EXTERNA);
            } catch (ReservaSolapadaException | CapacidadExcedidaException | FechasInvalidasException ex) {
                throw new RuntimeException(ex);
            }
        }, "No permitir saltarse la cola FIFO del canal externo");
        Huesped primero = huesped("EXT-H1");
        Huesped segundo = huesped("EXT-H2");
        Huesped tercero = huesped("EXT-H3");
        LocalDate entrada = LocalDate.of(2027, 8, 10);
        Estancia mismaEstancia = new Estancia(entrada, entrada.plusDays(2));
        LocalDateTime recibida = LocalDateTime.of(2026, 11, 15, 9, 0);

        SolicitudReservaExterna solicitud1 = externo.recibirSolicitud(
                primero, apartamento, mismaEstancia,
                ocupantes(2, LocalDate.of(1990, 1, 1)), recibida);
        SolicitudReservaExterna solicitud2 = externo.recibirSolicitud(
                segundo, apartamento, mismaEstancia,
                ocupantes(1, LocalDate.of(1990, 1, 1)), recibida.plusMinutes(1));
        SolicitudReservaExterna solicitud3 = externo.recibirSolicitud(
                tercero, apartamento,
                new Estancia(entrada.plusDays(2), entrada.plusDays(4)),
                ocupantes(1, LocalDate.of(1990, 1, 1)), recibida.plusMinutes(2));

        igual(3, externo.cantidadPendientes(), "Las tres solicitudes entran a la cola");
        igual(0, apartamento.numeroReservas(), "Encolar no bloquea noches antes del procesamiento");
        igual(solicitud1, externo.listarPendientes().primero(), "La primera solicitud conserva el frente FIFO");

        SolicitudReservaExterna procesada1 = externo.procesarSiguiente();
        igual(solicitud1, procesada1, "Primera solicitud procesada primero");
        igual(EstadoSolicitudExterna.ACEPTADA, procesada1.getEstado(), "Primera solicitud aceptada");
        igual(EstadoReserva.CONFIRMADA, procesada1.getReservaGenerada().getEstado(),
                "Reserva externa aceptada queda confirmada");

        SolicitudReservaExterna procesada2 = externo.procesarSiguiente();
        igual(solicitud2, procesada2, "Segunda solicitud procesada en segundo lugar");
        igual(EstadoSolicitudExterna.RECHAZADA, procesada2.getEstado(),
                "Segunda solicitud rechazada por solapamiento");
        verificar(procesada2.getMotivoRechazo().contains("ya tiene la reserva"),
                "El rechazo conserva el motivo del conflicto");

        SolicitudReservaExterna procesada3 = externo.procesarSiguiente();
        igual(solicitud3, procesada3, "Tercera solicitud procesada en tercer lugar");
        igual(EstadoSolicitudExterna.ACEPTADA, procesada3.getEstado(),
                "Una estancia contigua se acepta porque checkout no ocupa esa noche");
        igual(0, externo.cantidadPendientes(), "La cola queda vacia tras procesar las tres solicitudes");
        igual(3, externo.getHistorial().tamano(), "El historial conserva todas las solicitudes procesadas");
        igual(1, externo.getConflictos().tamano(), "El registro de conflictos conserva el rechazo");
        verificar(externo.procesarSiguiente() == null, "Procesar cola vacia retorna null");
    }

    private static void probarVencimientos() throws Exception {
        LocalDate entrada = LocalDate.of(2027, 2, 10);
        LocalDateTime creada = LocalDateTime.of(2026, 11, 15, 10, 0);
        Apartamento a = apartamento("V-1", 2, 1);
        Huesped h = huesped("4001");
        GestorVencimientos gv = new GestorVencimientos();
        GestorReservas gr = new GestorReservas(new CotizadorService(new GestorTemporadas()),
                new AsignadorGrupos(), gv);
        Reserva vencida = gr.crearReserva(h, a, new Estancia(entrada, entrada.plusDays(2)),
                ocupantes(1, LocalDate.of(1990, 1, 1)), CanalReserva.PORTAL, creada);
        igual(1, gv.cantidadEnCola(), "Reserva pendiente entra a cola");
        igual(0, gv.procesarVencimientos(creada.plusHours(23)), "No vencer antes de 24 horas");
        igual(EstadoReserva.PENDIENTE, vencida.getEstado(), "Sigue pendiente antes del límite");
        igual(1, gv.procesarVencimientos(creada.plusHours(24)), "Vence exactamente a las 24 horas");
        igual(EstadoReserva.CANCELADA, vencida.getEstado(), "Cambiar a cancelada");
        verificar(!a.getReservas().contiene(vencida), "Liberar reserva vencida del árbol");
        igual(0, gv.cantidadEnCola(), "Cola sin pendientes");
    }


    private static void probarReportes() throws Exception {
        ArbolBinarioBusqueda<Apartamento> apartamentos = new ArbolBinarioBusqueda<>();
        ArbolBinarioBusqueda<Huesped> huespedes = new ArbolBinarioBusqueda<>();
        GestorTemporadas temporadas = new GestorTemporadas();
        temporadas.definirPeriodo(new PeriodoTemporada(
                LocalDate.of(2027, 1, 2), LocalDate.of(2027, 1, 3), Temporada.MEDIA));

        Apartamento a = apartamento("REP-1", 3, 2);
        Apartamento b = apartamento("REP-2", 2, 1);
        apartamentos.insertar(a);
        apartamentos.insertar(b);

        Huesped directaHuesped = huesped("REP-H1");
        Huesped portalHuesped = huesped("REP-H2");
        Huesped externaHuesped = huesped("REP-H3");
        huespedes.insertar(directaHuesped);
        huespedes.insertar(portalHuesped);
        huespedes.insertar(externaHuesped);

        GestorReservas gestorReservas = new GestorReservas(
                new CotizadorService(temporadas), new AsignadorGrupos(), new GestorVencimientos());
        LocalDateTime creada = LocalDateTime.of(2027, 1, 1, 8, 0);
        Reserva directa = gestorReservas.crearReserva(directaHuesped, a,
                estancia(LocalDate.of(2027, 1, 1), 3),
                ocupantes(1, LocalDate.of(1990, 1, 1)), CanalReserva.DIRECTA, creada);
        Reserva portal = gestorReservas.crearReserva(portalHuesped, b,
                estancia(LocalDate.of(2027, 1, 1), 1),
                ocupantes(1, LocalDate.of(1990, 1, 1)), CanalReserva.PORTAL, creada);
        gestorReservas.cancelar(portal, LocalDate.of(2027, 1, 1));
        Reserva externa = gestorReservas.crearReservaExternaConfirmada(externaHuesped, b,
                estancia(LocalDate.of(2027, 1, 2), 1),
                ocupantes(1, LocalDate.of(1990, 1, 1)), creada);
        gestorReservas.marcarNoShow(externa);

        // Los movimientos del folio se prueban por separado y deben reflejarse
        // como pagos, devoluciones y anulaciones dentro del periodo consultado.
        LocalDateTime pago1 = LocalDateTime.of(2027, 1, 5, 10, 0);
        gestorReservas.registrarPago(directa, "Primer abono", new BigDecimal("50000"), pago1);
        gestorReservas.getGestorFolios().deshacerUltimaOperacion(
                directa.getFolio(), LocalDateTime.of(2027, 1, 6, 10, 0));
        gestorReservas.registrarPago(directa, "Segundo abono", new BigDecimal("100000"),
                LocalDateTime.of(2027, 1, 7, 10, 0));
        gestorReservas.getGestorFolios().registrarDevolucion(directa.getFolio(), new MovimientoFolio(
                "REP-DEV-1", TipoMovimientoFolio.DEVOLUCION, "Devolucion de prueba",
                new BigDecimal("20000"), LocalDateTime.of(2027, 1, 8, 10, 0), null));

        GestorReportes reportes = new GestorReportes(apartamentos, huespedes, temporadas);
        ListaDoblementeEnlazada<ReporteOcupacion> ocupacion = reportes.generarOcupacion(
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 4));
        igual(4, ocupacion.tamano(), "Una fila por apartamento y temporada presente en el periodo");
        ReporteOcupacion baja = null;
        ReporteOcupacion media = null;
        for (ReporteOcupacion fila : ocupacion) {
            if (fila.getCodigoApartamento().equals("REP-1") && fila.getTemporada() == Temporada.BAJA) baja = fila;
            if (fila.getCodigoApartamento().equals("REP-1") && fila.getTemporada() == Temporada.MEDIA) media = fila;
        }
        verificar(baja != null && media != null, "Reporte contiene temporada baja y media");
        igual(1, baja.getNochesDisponibles(), "Una noche baja en el periodo");
        igual(1, baja.getNochesOcupadas(), "La noche baja de la reserva directa está ocupada");
        igual(2, media.getNochesDisponibles(), "Dos noches medias en el periodo");
        igual(2, media.getNochesOcupadas(), "Las dos noches medias de la reserva directa están ocupadas");
        cerca(100.0, media.getPorcentajeOcupacion(), "Porcentaje de ocupación por temporada");

        ReporteIngresos ingresos = reportes.generarIngresos(
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 2, 1));
        igual(new BigDecimal("150000.00"), ingresos.getPagos(), "Sumar los dos pagos recibidos");
        igual(new BigDecimal("20000"), ingresos.getDevoluciones(), "Restar devolución real por separado");
        igual(new BigDecimal("50000.00"), ingresos.getAnulacionesPagos(), "Contabilizar anulación de pago");
        igual(new BigDecimal("80000.00"), ingresos.getIngresosNetos(), "Calcular ingreso neto del periodo");

        ListaDoblementeEnlazada<ReporteReservasCanal> canales = reportes.generarReservasPorCanal();
        igual(3, canales.tamano(), "Generar una fila por cada canal");
        for (ReporteReservasCanal fila : canales) {
            if (fila.getCanal() == CanalReserva.DIRECTA) {
                igual(1, fila.getCantidad(EstadoReserva.CONFIRMADA), "Contar reserva directa confirmada");
            } else if (fila.getCanal() == CanalReserva.PORTAL) {
                igual(1, fila.getCantidad(EstadoReserva.CANCELADA), "Conservar canceladas en el historial");
            } else if (fila.getCanal() == CanalReserva.EXTERNA) {
                igual(1, fila.getCantidad(EstadoReserva.NO_SHOW), "Conservar no-show en el historial");
            }
        }
        esperaExcepcion(IllegalArgumentException.class,
                () -> reportes.generarOcupacion(LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 1)),
                "Rechazar periodos de reporte vacíos");
    }

    private static void probarAsignador() throws Exception {
        ListaDoblementeEnlazada<Apartamento> libres = new ListaDoblementeEnlazada<>();
        libres.agregar(apartamento("G-1", 2, 1));
        libres.agregar(apartamento("G-2", 4, 2));
        libres.agregar(apartamento("G-3", 3, 2));
        ListaDoblementeEnlazada<Apartamento> seleccion = new AsignadorGrupos().asignar(6, libres);
        igual(2, seleccion.tamano(), "Encontrar combinación mínima de dos apartamentos");
        int capacidad = 0; for (Apartamento a : seleccion) capacidad += a.getCapacidadMaxima();
        verificar(capacidad >= 6, "Capacidad total cubre el grupo");
        esperaExcepcion(CapacidadExcedidaException.class,
                () -> { try { new AsignadorGrupos().asignar(20, libres); }
                catch (CapacidadExcedidaException ex) { throw new RuntimeException(ex); } },
                "No asignar si no alcanza capacidad");
    }

    private static void probarTurnos() throws Exception {
        GestorTurnos gt = new GestorTurnos();
        Empleado a = new Empleado("T-1", "Ana", RolEmpleado.RECEPCIONISTA, "3001");
        Empleado b = new Empleado("T-2", "Luis", RolEmpleado.RECEPCIONISTA, "3002");
        gt.agregarRecepcionista(a); gt.agregarRecepcionista(b);
        igual(a, gt.turnoActual(), "Primer recepcionista tiene turno");
        igual(b, gt.pasarTurno(), "Rotar al segundo");
        igual(a, gt.pasarTurno(), "Rotación circular vuelve al primero");
        verificar(gt.retirar(a), "Retirar recepcionista");
        igual(1, gt.cantidad(), "Cantidad tras retiro");
        esperaExcepcion(ExcepcionSGA.class,
                () -> { try { gt.agregarRecepcionista(new Empleado("T-3", "Admin", RolEmpleado.ADMINISTRADOR, "3003")); }
                catch (ExcepcionSGA ex) { throw new RuntimeException(ex); } },
                "No agregar administrador a turnos");
    }

    private static void probarReservaGrupo() throws Exception {
        ArbolBinarioBusqueda<Apartamento> catalogo = new ArbolBinarioBusqueda<>();
        Apartamento a = apartamento("RG-1", 4, 2);
        Apartamento b = apartamento("RG-2", 3, 2);
        Apartamento c = apartamento("RG-3", 2, 1);
        catalogo.insertar(a); catalogo.insertar(b); catalogo.insertar(c);
        GestorReservas gr = new GestorReservas(new CotizadorService(new GestorTemporadas()),
                new AsignadorGrupos(), new GestorVencimientos());
        Huesped h = huesped("RG-H1");
        ListaDoblementeEnlazada<Ocupante> grupo = ocupantes(6, LocalDate.of(1990, 1, 1));
        ListaDoblementeEnlazada<Reserva> reservas = gr.reservarGrupoGrande(h,
                estancia(LocalDate.of(2027, 4, 1), 2), grupo, catalogo, CanalReserva.DIRECTA);
        igual(2, reservas.tamano(), "Grupo de seis debe ocupar dos apartamentos");
        igual(2, h.getHistorial().tamano(), "Cada reserva queda en historial del titular");
        int personasAsignadas = 0;
        for (Reserva r : reservas) personasAsignadas += r.numeroPersonas();
        igual(6, personasAsignadas, "Todos los ocupantes deben quedar asignados");
    }

    private static void probarPersistencia() throws Exception {
        Path archivo = Files.createTempFile("sga-prueba-", ".txt");
        try {
            ArbolBinarioBusqueda<Apartamento> apartamentos = new ArbolBinarioBusqueda<>();
            ArbolBinarioBusqueda<Huesped> huespedes = new ArbolBinarioBusqueda<>();
            GestorTemporadas temporadas = new GestorTemporadas();
            GestorReservas reservas = new GestorReservas(new CotizadorService(temporadas),
                    new AsignadorGrupos(), new GestorVencimientos());
            Apartamento a = apartamento("P-1", 3, 2);
            apartamentos.insertar(a);
            Huesped h = huesped("P-H1");
            huespedes.insertar(h);
            h.agregarFavorito(a);
            reservas.crearReserva(h, a, estancia(LocalDate.of(2027, 5, 1), 2),
                    ocupantes(2, LocalDate.of(1990, 1, 1)), CanalReserva.DIRECTA,
                    LocalDateTime.of(2026, 11, 15, 10, 0));

            CargadorDatos escritor = new CargadorDatos(apartamentos, huespedes, reservas, temporadas);
            escritor.guardarEnArchivo(archivo.toString());
            verificar(Files.size(archivo) > 0, "El archivo debe contener datos");

            ArbolBinarioBusqueda<Apartamento> apartamentosCargados = new ArbolBinarioBusqueda<>();
            ArbolBinarioBusqueda<Huesped> huespedesCargados = new ArbolBinarioBusqueda<>();
            GestorTemporadas temporadasCargadas = new GestorTemporadas();
            GestorReservas reservasCargadas = new GestorReservas(
                    new CotizadorService(temporadasCargadas), new AsignadorGrupos(), new GestorVencimientos());
            CargadorDatos lector = new CargadorDatos(apartamentosCargados, huespedesCargados,
                    reservasCargadas, temporadasCargadas);
            lector.cargarDesdeArchivo(archivo.toString());

            Apartamento restaurado = apartamentosCargados.buscar(Apartamento.paraBusqueda("P-1"));
            Huesped huespedRestaurado = huespedesCargados.buscar(Huesped.paraBusqueda("P-H1"));
            verificar(restaurado != null, "Restaurar apartamento");
            verificar(huespedRestaurado != null, "Restaurar huésped");
            igual(1, huespedRestaurado.getFavoritos().tamano(), "Restaurar favorito");
            igual(1, restaurado.numeroReservas(), "Restaurar reserva");
            igual(EstadoReserva.CONFIRMADA, restaurado.getReservas().obtenerMinimo().getEstado(),
                    "Restaurar estado confirmado");
        } finally {
            Files.deleteIfExists(archivo);
        }
    }

    private static void probarEscenarioPrueba() throws Exception {
        ArbolBinarioBusqueda<Apartamento> apartamentos = new ArbolBinarioBusqueda<>();
        ArbolBinarioBusqueda<Huesped> huespedes = new ArbolBinarioBusqueda<>();
        GestorTemporadas temporadas = new GestorTemporadas();
        GestorReservas reservas = new GestorReservas(new CotizadorService(temporadas),
                new AsignadorGrupos(), new GestorVencimientos());
        CargadorDatos cargador = new CargadorDatos(apartamentos, huespedes, reservas, temporadas);
        cargador.cargarEscenarioPrueba();
        igual(8, apartamentos.tamano(), "El escenario debe cargar ocho apartamentos");
        igual(5, huespedes.tamano(), "El escenario debe cargar cinco huéspedes");
        igual(3, temporadas.getPeriodos().tamano(), "El escenario debe cargar tres temporadas");
        int cantidadReservas = 0;
        for (Apartamento a : apartamentos) cantidadReservas += a.numeroReservas();
        igual(4, cantidadReservas, "El escenario debe cargar cuatro reservas");
    }

    private static void probarNodosTipados() {
        model.estructuras.Grafo.Tipo_nodos.Pasillo pasillo =
                new model.estructuras.Grafo.Tipo_nodos.Pasillo(7);
        model.estructuras.Grafo.Tipo_nodos.Recepcion recepcion =
                new model.estructuras.Grafo.Tipo_nodos.Recepcion(1);
        model.estructuras.Grafo.Tipo_nodos.CuartosAseo aseo =
                new model.estructuras.Grafo.Tipo_nodos.CuartosAseo(2);
        model.estructuras.Grafo.Tipo_nodos.Escaleras escaleras =
                new model.estructuras.Grafo.Tipo_nodos.Escaleras(3);
        igual(Integer.valueOf(7), pasillo.getNumero(), "Número de pasillo");
        igual(Integer.valueOf(1), recepcion.getNumero(), "Número de recepción");
        igual(Integer.valueOf(2), aseo.getNumero(), "Número de cuarto de aseo");
        igual(Integer.valueOf(3), escaleras.getNumero(), "Número de escaleras");
        pasillo.setNumero(8);
        igual(Integer.valueOf(8), pasillo.getNumero(), "Actualizar nodo tipado");
    }

    private static void probarPreparacion() {
        GestorPreparacion gestor = new GestorPreparacion();
        LocalDateTime ahora = LocalDateTime.of(2027, 6, 10, 8, 0);
        Apartamento manana = apartamento("PREP-2", 2, 1);
        Apartamento hoy = apartamento("PREP-1", 2, 1);
        manana.cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION);
        hoy.cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION);

        gestor.registrarPendiente(manana, ahora.plusDays(1).withHour(9), ahora);
        gestor.registrarPendiente(hoy, ahora.withHour(14), ahora);
        igual(2, gestor.cantidadPendientes(), "Registrar solicitudes de preparación");

        SolicitudPreparacion primera = gestor.iniciarSiguiente(ahora.plusMinutes(5));
        igual("PREP-1", primera.getApartamento().getCodigo(),
                "Las llegadas de hoy tienen prioridad sobre las de mañana");
        igual(EstadoApartamento.EN_PREPARACION, hoy.getEstadoOperativo(),
                "Cambiar a EN_PREPARACION al iniciar");
        esperaExcepcion(IllegalStateException.class,
                () -> gestor.iniciarSiguiente(ahora.plusMinutes(6)),
                "No iniciar dos preparaciones simultáneas");

        gestor.finalizarActual(ahora.plusMinutes(35));
        igual(EstadoApartamento.PREPARADO, hoy.getEstadoOperativo(),
                "Cambiar a PREPARADO al finalizar");
        cerca(30.0, gestor.promedioMinutosPreparacion(),
                "Registrar duración de preparación");

        SolicitudPreparacion segunda = gestor.iniciarSiguiente(ahora.plusHours(1));
        igual("PREP-2", segunda.getApartamento().getCodigo(),
                "Procesar después la llegada de mañana");
        gestor.finalizarActual(ahora.plusHours(2));
        igual(2, gestor.getPreparacionesFinalizadas(), "Contabilizar preparaciones finalizadas");
        cerca(45.0, gestor.promedioMinutosPreparacion(),
                "Calcular promedio de duración de preparaciones");
        igual(0, gestor.cantidadPendientes(), "Vaciar cola de pendientes");
    }

    private static void probarFachada() throws Exception {
        ApartaSuites sistema = ApartaSuites.getInstance();
        Apartamento a = apartamento("FACH-1", 4, 2);
        Apartamento b = apartamento("FACH-2", 2, 1);
        b.definirTarifa(Temporada.BAJA, 80_000);
        sistema.registrarApartamento(a);
        sistema.registrarApartamento(b);
        Huesped h = huesped("FACH-H1");
        sistema.registrarHuesped(h);
        verificar(sistema.buscarApartamento("FACH-1") == a, "Fachada busca apartamento");
        verificar(sistema.buscarHuesped("FACH-H1") == h, "Fachada busca huésped");
        verificar(sistema.agregarFavorito("FACH-H1", "FACH-1"), "Fachada agrega favorito");
        verificar(!sistema.agregarFavorito("FACH-H1", "FACH-1"), "Fachada evita favorito duplicado");
        igual(1, sistema.listarFavoritos("FACH-H1").tamano(), "Fachada lista favoritos");
        igual(2, sistema.listarApartamentos().tamano(), "Fachada lista apartamentos");
        igual(1, sistema.listarHuespedes().tamano(), "Fachada lista huéspedes");
        ListaDoblementeEnlazada<Apartamento> disponibles = sistema.buscarDisponibles(
                estancia(LocalDate.of(2027, 6, 1), 2), 2, CriterioOrden.PRECIO);
        igual(2, disponibles.tamano(), "Fachada consulta disponibilidad");
        igual("FACH-2", disponibles.primero().getCodigo(), "Ordenar disponibilidad por precio");
        sistema.ponerApartamentoFueraDeServicio("FACH-1", "Prueba de mantenimiento");
        igual(EstadoApartamento.FUERA_DE_SERVICIO,
                sistema.buscarApartamento("FACH-1").getEstadoOperativo(), "Fachada cambia estado");
    }

    private static void probarAdministracion() throws Exception {
        ArbolBinarioBusqueda<Apartamento> apartamentos = new ArbolBinarioBusqueda<>();
        ArbolBinarioBusqueda<Huesped> huespedes = new ArbolBinarioBusqueda<>();
        GestorTurnos turnos = new GestorTurnos();
        GestorAdministracion ga = new GestorAdministracion(apartamentos, huespedes, turnos);
        Apartamento a = apartamento("ADM-1", 3, 2);
        ga.registrarApartamento(a);
        igual(a, ga.buscarApartamento("ADM-1"), "Buscar apartamento por código");
        esperaExcepcion(ExcepcionSGA.class, () -> {
            try { ga.registrarApartamento(apartamento("ADM-1", 3, 2)); }
            catch (ExcepcionSGA ex) { throw new RuntimeException(ex); }
        }, "No duplicar código de apartamento");

        Huesped h = huesped("ADM-H1");
        ga.registrarHuesped(h);
        igual(h, ga.buscarHuesped("ADM-H1"), "Buscar huésped por documento");

        Empleado e = new Empleado("ADM-E1", "Recepción", RolEmpleado.RECEPCIONISTA, "3000");
        ga.registrarEmpleado(e);
        igual(e, ga.buscarEmpleado("ADM-E1"), "Buscar empleado");
        igual(1, turnos.cantidad(), "Registrar recepcionista también en turnos");
        ga.eliminarEmpleado("ADM-E1");
        igual(0, turnos.cantidad(), "Eliminar empleado también de turnos");
        ga.ponerApartamentoFueraDeServicio("ADM-1", "Reparación");
        igual(EstadoApartamento.FUERA_DE_SERVICIO, ga.buscarApartamento("ADM-1").getEstadoOperativo(),
                "Administración cambia estado operativo");
        ga.eliminarApartamento("ADM-1");
        verificar(ga.buscarApartamento("ADM-1") == null, "Eliminar apartamento");
    }
}
