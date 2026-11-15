package model.servicios;

import model.enums.CanalReserva;
import model.enums.EstadoApartamento;
import model.enums.EstadoReserva;
import model.enums.RolEmpleado;
import model.enums.Temporada;
import model.estructuras.ArbolBinarioBusqueda;
import model.estructuras.Cola.Cola;
import model.estructuras.Cola.ColaPrioritaria;
import model.estructuras.Conjunto;
import model.estructuras.Grafo.Grafo;
import model.estructuras.Grafo.NodoGrafo;
import model.estructuras.Grafo.Tipo_nodos.CuartosAseo;
import model.estructuras.Grafo.Tipo_nodos.Recepcion;
import model.estructuras.ListaCircular;
import model.estructuras.ListaDoblementeEnlazada;
import model.estructuras.Pila.Pila;
import model.excepciones.CapacidadExcedidaException;
import model.excepciones.EstructuraVaciaException;
import model.excepciones.ExcepcionSGA;
import model.excepciones.FechasInvalidasException;
import model.excepciones.ReservaSolapadaException;
import model.excepciones.TransicionInvalidaException;
import model.modelo.Apartamento;
import model.modelo.Cotizacion;
import model.modelo.Empleado;
import model.modelo.Estancia;
import model.modelo.Huesped;
import model.modelo.Ocupante;
import model.modelo.PeriodoTemporada;
import model.modelo.Reserva;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Main centralizado del Sistema de Gestion de Alojamiento (SGA): recorre, en un solo programa,
 * todo lo que hoy existe en la rama Dev.
 *
 * PARTE 1 - Dominio principal (secciones 1 a 14), sobre el escenario de prueba de {@link CargadorDatos}:
 * catalogo, temporadas, disponibilidad, cotizacion, reservas por canal, solapamiento, grupos grandes
 * (backtracking), cancelaciones, no-show, favoritos, historial, archivo plano, recorridos del arbol,
 * turnos (lista circular), administracion, excepciones propias y persistencia.
 *
 * PARTE 2 - Topologia, colas y pilas (secciones 15 a 19), sobre un escenario limpio propio: cola FIFO del
 * canal externo, pila, grafo del edificio (Dijkstra, DFS, BFS), cola prioritaria de preparacion con su
 * ruta y analisis de inalcanzables al cerrar un pasillo.
 *
 * Importante: el canal externo, la cola de preparacion y la ruta se SIMULAN aqui con las estructuras
 * propias; todavia no existen como servicios del sistema (ver las notas de cada seccion).
 */
public class Main {

    /** Fecha fija de "hoy" para que las politicas de devolucion den resultados reproducibles. */
    private static final LocalDate HOY = LocalDate.of(2026, 11, 14);

    public static void main(String[] args) {
        ArbolBinarioBusqueda<Apartamento> apartamentos = new ArbolBinarioBusqueda<>();
        ArbolBinarioBusqueda<Huesped> huespedes = new ArbolBinarioBusqueda<>();

        GestorTemporadas temporadas = new GestorTemporadas();
        CotizadorService cotizador = new CotizadorService(temporadas);
        AsignadorGrupos asignador = new AsignadorGrupos();
        GestorReservas reservas = new GestorReservas(cotizador, asignador);
        CargadorDatos cargador = new CargadorDatos(apartamentos, huespedes, reservas, temporadas);

        try {
            System.out.println("############ PARTE 1: DOMINIO PRINCIPAL ############");
            pruebaCarga(cargador, apartamentos, huespedes);
            pruebaTemporadas(temporadas);
            pruebaCotizacion(apartamentos, reservas);
            pruebaDisponibilidad(apartamentos, reservas);
            pruebaReglasDeNegocio(apartamentos, huespedes, reservas);
            pruebaCanalPortal(apartamentos, huespedes, reservas);
            pruebaGrupoGrande(huespedes, apartamentos, reservas);
            pruebaCancelacionesYNoShow(apartamentos, huespedes, reservas);
            pruebaFavoritosEHistorial(apartamentos, huespedes);
            pruebaArchivoPlano();
            pruebaRecorridosDelArbol(apartamentos);
            pruebaTurnosYAdministracion(apartamentos, huespedes);
            pruebaExcepcionesPropias(apartamentos);
            pruebaPersistencia(cargador, reservas, temporadas, apartamentos, huespedes);

            System.out.println();
            System.out.println("############ PARTE 2: TOPOLOGIA, COLAS Y PILAS ############");
            pruebaParteDos();

            System.out.println();
            System.out.println("=== FIN DE LA PRUEBA ===");
        } catch (ExcepcionSGA e) {
            System.out.println("ERROR INESPERADO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 1. Carga de datos ====================

    private static void pruebaCarga(CargadorDatos cargador, ArbolBinarioBusqueda<Apartamento> apartamentos,
                                    ArbolBinarioBusqueda<Huesped> huespedes) throws ExcepcionSGA {
        titulo("1. CARGA DEL ESCENARIO DE PRUEBA");
        cargador.cargarEscenarioPrueba();

        System.out.println("Apartamentos registrados: " + apartamentos.tamano()
                + " (altura del arbol: " + apartamentos.altura() + ")");
        for (Apartamento a : apartamentos.inOrden()) {
            System.out.println("  " + a + " | " + a.getDormitorios() + " dorm. | " + a.getCaracteristicas()
                    + " | reservas: " + a.numeroReservas());
        }

        System.out.println("Huespedes registrados: " + huespedes.tamano());
        for (Huesped h : huespedes) {
            System.out.println("  " + h + " | reservas en historial: " + h.getHistorial().tamano());
        }

        Apartamento buscado = apartamentos.buscar(Apartamento.paraBusqueda("B-101"));
        System.out.println("Busqueda por codigo B-101 -> " + buscado);
        System.out.println("Busqueda por codigo Z-999 -> " + apartamentos.buscar(Apartamento.paraBusqueda("Z-999")));
    }

    // ==================== 2. Temporadas ====================

    private static void pruebaTemporadas(GestorTemporadas temporadas) {
        titulo("2. TEMPORADAS");
        for (PeriodoTemporada p : temporadas.getPeriodos()) {
            System.out.println("  Periodo definido: " + p);
        }
        LocalDate[] fechas = {
                LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 25), LocalDate.of(2026, 12, 15),
                LocalDate.of(2027, 1, 15), LocalDate.of(2027, 1, 16), LocalDate.of(2027, 3, 25)
        };
        for (LocalDate f : fechas) {
            System.out.println("  " + f + " -> temporada " + temporadas.temporadaDe(f));
        }

        try {
            temporadas.definirPeriodo(new PeriodoTemporada(
                    LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 20), Temporada.ALTA));
            System.out.println("  ERROR: se acepto un periodo que se cruza con otro");
        } catch (FechasInvalidasException e) {
            System.out.println("  Periodo cruzado rechazado -> " + e.getMessage());
        }
    }

    // ==================== 3. Cotizacion ====================

    private static void pruebaCotizacion(ArbolBinarioBusqueda<Apartamento> apartamentos, GestorReservas reservas)
            throws ExcepcionSGA {
        titulo("3. COTIZACION (los menores de 6 anios no pagan)");
        Apartamento a102 = apartamentos.buscar(Apartamento.paraBusqueda("A-102"));
        Reserva reserva = a102.getReservas().getRaiz().getDato();

        System.out.println("Reserva: " + reserva);
        System.out.println("Ocupantes:");
        for (Ocupante o : reserva.getOcupantes()) {
            System.out.println("  - " + o + " | edad al ingresar: " + o.edadEn(reserva.getEstancia().getEntrada())
                    + " | paga: " + o.pagaCargo(reserva.getEstancia().getEntrada()));
        }
        Cotizacion cotizacion = reservas.cotizarReserva(reserva);
        System.out.println("Desglose por noche (cambia de MEDIA a ALTA el 2026-12-15):");
        System.out.println(cotizacion);
        System.out.println("Anticipo del 30%: " + dinero(reservas.calcularAnticipo(reserva)));
    }

    // ==================== 4. Disponibilidad ====================

    private static void pruebaDisponibilidad(ArbolBinarioBusqueda<Apartamento> apartamentos,
                                             GestorReservas reservas) throws ExcepcionSGA {
        titulo("4. DISPONIBILIDAD Y ORDENAMIENTO");
        Estancia estancia = new Estancia(LocalDate.of(2026, 12, 12), LocalDate.of(2026, 12, 15));
        System.out.println("Busqueda para 4 personas en " + estancia);

        System.out.println("-- Ordenados por precio (temporada ALTA):");
        for (Apartamento a : reservas.buscarDisponibles(apartamentos, estancia, 4,
                Apartamento.porPrecio(Temporada.ALTA))) {
            System.out.println("   " + a + " -> " + dinero(a.tarifaPara(Temporada.ALTA)) + " por persona/noche");
        }

        System.out.println("-- Ordenados por capacidad:");
        for (Apartamento a : reservas.buscarDisponibles(apartamentos, estancia, 4, Apartamento.porCapacidad())) {
            System.out.println("   " + a);
        }

        System.out.println("-- Ordenados por numero de dormitorios:");
        for (Apartamento a : reservas.buscarDisponibles(apartamentos, estancia, 4, Apartamento.porDormitorios())) {
            System.out.println("   " + a + " | " + a.getDormitorios() + " dorm.");
        }

        System.out.println("-- Ordenados por numero de reservas (los menos usados primero):");
        for (Apartamento a : reservas.buscarDisponibles(apartamentos, estancia, 2,
                Apartamento.porNumeroReservas())) {
            System.out.println("   " + a + " | reservas: " + a.numeroReservas());
        }
    }

    // ==================== 5. Reglas de negocio (model.excepciones) ====================

    private static void pruebaReglasDeNegocio(ArbolBinarioBusqueda<Apartamento> apartamentos,
                                              ArbolBinarioBusqueda<Huesped> huespedes,
                                              GestorReservas reservas) throws ExcepcionSGA {
        titulo("5. REGLAS DE NEGOCIO");
        Huesped sofia = huespedes.buscar(Huesped.paraBusqueda("1094000005"));
        Apartamento a102 = apartamentos.buscar(Apartamento.paraBusqueda("A-102"));
        Apartamento a101 = apartamentos.buscar(Apartamento.paraBusqueda("A-101"));

        // 5.1 Solapamiento: A-102 ya esta reservado del 10 al 14 de diciembre.
        Estancia choque = new Estancia(LocalDate.of(2026, 12, 13), LocalDate.of(2026, 12, 16));
        try {
            reservas.crearReserva(sofia, a102, choque,
                    ocupantes("Sofia Torres:1992-01-09"), CanalReserva.DIRECTA);
            System.out.println("ERROR: se permitio una reserva solapada");
        } catch (ReservaSolapadaException e) {
            System.out.println("5.1 Solapamiento rechazado -> " + e.getMessage());
        }

        // 5.2 Salida el mismo dia de la entrada de otra reserva: NO se solapa (no comparten noche).
        Estancia contigua = new Estancia(LocalDate.of(2026, 12, 14), LocalDate.of(2026, 12, 16));
        Reserva contigua1 = reservas.crearReserva(sofia, a102, contigua,
                ocupantes("Sofia Torres:1992-01-09"), CanalReserva.DIRECTA);
        System.out.println("5.2 Estancia contigua aceptada (check-out = check-in) -> " + contigua1);

        // 5.3 Capacidad excedida: A-101 admite 2 personas.
        try {
            reservas.crearReserva(sofia, a101,
                    new Estancia(LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 3)),
                    ocupantes("Sofia Torres:1992-01-09", "Ana Torres:1990-02-02", "Luis Torres:1988-03-03"),
                    CanalReserva.DIRECTA);
            System.out.println("ERROR: se permitio exceder la capacidad");
        } catch (CapacidadExcedidaException e) {
            System.out.println("5.3 Capacidad excedida rechazada -> " + e.getMessage());
        }

        // 5.4 Fechas invalidas: salida anterior a la entrada.
        try {
            new Estancia(LocalDate.of(2027, 2, 5), LocalDate.of(2027, 2, 1));
            System.out.println("ERROR: se permitieron fechas invalidas");
        } catch (FechasInvalidasException e) {
            System.out.println("5.4 Fechas invalidas rechazadas -> " + e.getMessage());
        }

        // 5.5 Apartamento fuera de servicio.
        Apartamento b102 = apartamentos.buscar(Apartamento.paraBusqueda("B-102"));
        b102.ponerFueraDeServicio("Fuga en la tuberia del bano");
        System.out.println("5.5 " + b102 + " | motivo: " + b102.getMotivoFueraServicio());
        try {
            reservas.crearReserva(sofia, b102,
                    new Estancia(LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 3)),
                    ocupantes("Sofia Torres:1992-01-09"), CanalReserva.DIRECTA);
            System.out.println("ERROR: se reservo un apartamento fuera de servicio");
        } catch (IllegalStateException e) {
            System.out.println("    Reserva rechazada -> " + e.getMessage());
        }
        b102.cambiarEstado(EstadoApartamento.PREPARADO);
        System.out.println("    Tras repararlo: " + b102);

        // Limpieza: se libera la reserva de prueba 5.2 para no afectar los demas casos.
        reservas.cancelar(contigua1, 0, HOY);
    }

    // ==================== 6. Canal Portal: pendiente, anticipo, vencimiento ====================

    private static void pruebaCanalPortal(ArbolBinarioBusqueda<Apartamento> apartamentos,
                                          ArbolBinarioBusqueda<Huesped> huespedes,
                                          GestorReservas reservas) throws ExcepcionSGA {
        titulo("6. CANAL PORTAL: PENDIENTE -> CONFIRMADA");
        Huesped sofia = huespedes.buscar(Huesped.paraBusqueda("1094000005"));
        Apartamento c201 = apartamentos.buscar(Apartamento.paraBusqueda("C-201"));

        LocalDateTime creada = LocalDateTime.of(2026, 11, 14, 10, 0);
        Reserva r = reservas.crearReserva(sofia, c201,
                new Estancia(LocalDate.of(2026, 11, 25), LocalDate.of(2026, 11, 28)),
                ocupantes("Sofia Torres:1992-01-09", "Pablo Rios:1991-06-06"),
                CanalReserva.PORTAL, creada);

        System.out.println("Creada por Portal: " + r);
        System.out.println("Vence en: " + r.getVenceEn());
        System.out.println("Anticipo requerido (30%): " + dinero(reservas.calcularAnticipo(r)));
        System.out.println("Vencida el 2026-11-14 20:00? " + r.estaVencida(LocalDateTime.of(2026, 11, 14, 20, 0)));
        System.out.println("Vencida el 2026-11-15 10:00? " + r.estaVencida(LocalDateTime.of(2026, 11, 15, 10, 0)));

        reservas.confirmar(r);
        System.out.println("Tras pagar el anticipo: " + r);
        System.out.println("Vencida despues de confirmar? " + r.estaVencida(LocalDateTime.of(2026, 11, 20, 10, 0)));

        // Ciclo de vida completo
        r.iniciar();
        System.out.println("Check-in: " + r);
        r.finalizar();
        System.out.println("Check-out: " + r + " | activa: " + r.esActiva());

        try {
            r.cancelar();
        } catch (IllegalStateException e) {
            System.out.println("Cancelar una reserva finalizada -> " + e.getMessage());
        }
    }

    // ==================== 7. Grupo grande (backtracking) ====================

    private static void pruebaGrupoGrande(ArbolBinarioBusqueda<Huesped> huespedes,
                                          ArbolBinarioBusqueda<Apartamento> apartamentos,
                                          GestorReservas reservas) throws ExcepcionSGA {
        titulo("7. GRUPO GRANDE: MENOR NUMERO DE APARTAMENTOS");
        Huesped carlos = huespedes.buscar(Huesped.paraBusqueda("1094000002"));
        Estancia estancia = new Estancia(LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 25));

        ListaDoblementeEnlazada<Ocupante> grupo = new ListaDoblementeEnlazada<>();
        for (int i = 1; i <= 11; i++) {
            grupo.agregar(new Ocupante("Invitado " + i, LocalDate.of(1990 + (i % 8), 1 + (i % 12), 1 + i)));
        }
        System.out.println("Grupo de " + grupo.tamano() + " personas en " + estancia);
        System.out.println("(C-101 esta ocupado del 20 al 27 de diciembre y no entra en la busqueda)");

        ListaDoblementeEnlazada<Apartamento> propuesta = reservas.asignarGrupo(grupo.tamano(), estancia, apartamentos);
        int cupos = 0;
        System.out.println("Combinacion propuesta (sin reservar):");
        for (Apartamento a : propuesta) {
            cupos += a.getCapacidadMaxima();
            System.out.println("   " + a);
        }
        System.out.println("   -> " + propuesta.tamano() + " apartamentos, " + cupos + " cupos");

        ListaDoblementeEnlazada<Reserva> creadas = reservas.reservarGrupoGrande(
                carlos, estancia, grupo, apartamentos, CanalReserva.DIRECTA);
        System.out.println("Reservas generadas:");
        for (Reserva r : creadas) {
            System.out.println("   " + r + " | personas: " + r.numeroPersonas());
        }

        // Un grupo imposible de alojar
        ListaDoblementeEnlazada<Ocupante> enorme = new ListaDoblementeEnlazada<>();
        for (int i = 1; i <= 60; i++) {
            enorme.agregar(new Ocupante("Persona " + i, LocalDate.of(1990, 1, 1)));
        }
        try {
            reservas.asignarGrupo(enorme.tamano(), estancia, apartamentos);
            System.out.println("ERROR: se asigno un grupo que no cabe");
        } catch (CapacidadExcedidaException e) {
            System.out.println("Grupo de 60 personas -> " + e.getMessage());
        }
    }

    // ==================== 8. Cancelaciones y no-show ====================

    private static void pruebaCancelacionesYNoShow(ArbolBinarioBusqueda<Apartamento> apartamentos,
                                                   ArbolBinarioBusqueda<Huesped> huespedes,
                                                   GestorReservas reservas) throws ExcepcionSGA {
        titulo("8. CANCELACIONES Y NO-SHOW");
        System.out.println("Politica: >= " + GestorReservas.DIAS_DEVOLUCION_TOTAL + " dias de antelacion: 100%; >= "
                + GestorReservas.DIAS_DEVOLUCION_PARCIAL + " dias: "
                + (int) (GestorReservas.PORCENTAJE_DEVOLUCION_PARCIAL * 100) + "%; menos: 0%");
        System.out.println("Fecha de referencia (hoy): " + HOY);

        Apartamento a102 = apartamentos.buscar(Apartamento.paraBusqueda("A-102"));
        Apartamento b101 = apartamentos.buscar(Apartamento.paraBusqueda("B-101"));
        Apartamento c101 = apartamentos.buscar(Apartamento.paraBusqueda("C-101"));
        Apartamento a101 = apartamentos.buscar(Apartamento.paraBusqueda("A-101"));

        Reserva r1 = a102.getReservas().getRaiz().getDato(); // entrada 2026-12-10 (26 dias)
        Reserva r2 = b101.getReservas().getRaiz().getDato(); // entrada 2026-12-12
        Reserva r3 = c101.getReservas().getRaiz().getDato(); // entrada 2026-12-20
        double pagado = 300000;

        System.out.println("Monto pagado en cada caso: " + dinero(pagado));
        mostrarDevolucion(reservas, r1, pagado, HOY);                          // 26 dias -> 100%
        mostrarDevolucion(reservas, r2, pagado, LocalDate.of(2026, 12, 8));    // 4 dias  -> 50%
        mostrarDevolucion(reservas, r3, pagado, LocalDate.of(2026, 12, 19));   // 1 dia   -> 0%

        int antes = a102.numeroReservas();
        double devuelto = reservas.cancelar(r1, pagado, HOY);
        System.out.println("Cancelada " + r1.getId() + ": devolucion " + dinero(devuelto)
                + " | estado: " + r1.getEstado()
                + " | reservas vigentes de A-102: " + antes + " -> " + a102.numeroReservas());

        Estancia liberada = r1.getEstancia();
        System.out.println("A-102 libre en " + liberada + "? " + reservas.estaLibre(a102, liberada));

        // No-show: solo aplica a reservas CONFIRMADAS (esta entro por Portal y sigue PENDIENTE)
        Reserva r4 = a101.getReservas().getRaiz().getDato();
        try {
            reservas.marcarNoShow(r4);
            System.out.println("ERROR: se marco no-show sobre una reserva pendiente");
        } catch (IllegalStateException e) {
            System.out.println("No-show sobre reserva PENDIENTE -> " + e.getMessage());
        }
        reservas.confirmar(r4);
        reservas.marcarNoShow(r4);
        System.out.println("No-show de " + r4.getId() + " -> " + r4.getEstado()
                + " | ocupa noches: " + r4.ocupaNoches()
                + " | A-101 libre en " + r4.getEstancia() + "? " + reservas.estaLibre(a101, r4.getEstancia()));

        // Una reserva cancelada no se puede volver a cancelar
        try {
            reservas.cancelar(r1, pagado, HOY);
        } catch (IllegalStateException e) {
            System.out.println("Cancelar dos veces -> " + e.getMessage());
        }

        // Las noches liberadas vuelven a poder reservarse
        Huesped laura = huespedes.buscar(Huesped.paraBusqueda("1094000001"));
        Reserva nueva = reservas.crearReserva(laura, a102, liberada,
                ocupantes("Laura Gomez:1990-05-14"), CanalReserva.DIRECTA);
        System.out.println("Nueva reserva sobre las noches liberadas: " + nueva);
    }

    private static void mostrarDevolucion(GestorReservas reservas, Reserva r, double pagado, LocalDate hoy) {
        System.out.println("  " + r.getId() + " (entrada " + r.getEstancia().getEntrada() + ") cancelada el " + hoy
                + " -> devolucion " + dinero(reservas.calcularDevolucion(r, pagado, hoy)));
    }

    // ==================== 9. Favoritos e historial ====================

    private static void pruebaFavoritosEHistorial(ArbolBinarioBusqueda<Apartamento> apartamentos,
                                                  ArbolBinarioBusqueda<Huesped> huespedes) {
        titulo("9. FAVORITOS (CONJUNTO) E HISTORIAL");
        Huesped laura = huespedes.buscar(Huesped.paraBusqueda("1094000001"));
        Apartamento a102 = apartamentos.buscar(Apartamento.paraBusqueda("A-102"));
        Apartamento c101 = apartamentos.buscar(Apartamento.paraBusqueda("C-101"));

        System.out.println("Agregar A-102 a favoritos: " + laura.agregarFavorito(a102));
        System.out.println("Agregar A-102 otra vez (el conjunto no admite repetidos): " + laura.agregarFavorito(a102));
        System.out.println("Agregar C-101 a favoritos: " + laura.agregarFavorito(c101));
        System.out.println("Favoritos de " + laura.getNombre() + ": " + laura.getFavoritos()
                + " (" + laura.getFavoritos().tamano() + ")");
        System.out.println("Quitar C-101: " + laura.quitarFavorito(c101)
                + " -> " + laura.getFavoritos().tamano() + " favorito(s)");

        System.out.println("Historial de reservas de " + laura.getNombre() + ":");
        for (Reserva r : laura.getHistorial()) {
            System.out.println("   " + r);
        }
        Huesped carlos = huespedes.buscar(Huesped.paraBusqueda("1094000002"));
        System.out.println("Historial de reservas de " + carlos.getNombre() + ":");
        for (Reserva r : carlos.getHistorial()) {
            System.out.println("   " + r);
        }
    }

    // ==================== 10. Carga desde archivo plano ====================

    private static void pruebaArchivoPlano() {
        titulo("10. CARGA DESDE ARCHIVO PLANO");
        ArbolBinarioBusqueda<Apartamento> apartamentos = new ArbolBinarioBusqueda<>();
        ArbolBinarioBusqueda<Huesped> huespedes = new ArbolBinarioBusqueda<>();
        GestorTemporadas temporadas = new GestorTemporadas();
        GestorReservas reservas = new GestorReservas(new CotizadorService(temporadas), new AsignadorGrupos());
        CargadorDatos cargador = new CargadorDatos(apartamentos, huespedes, reservas, temporadas);

        String valido = String.join(System.lineSeparator(),
                "# tipo;datos...",
                "TEMPORADA;ALTA;2027-06-15;2027-07-15",
                "APARTAMENTO;D-101;Frailejon;D;1;2;4;70000;90000;130000;Chimenea",
                "HUESPED;2000000001;Elena Vargas;1989-09-09;3001234567;elena@correo.com",
                "RESERVA;2000000001;D-101;2027-06-20;2027-06-23;DIRECTA;Elena Vargas:1989-09-09|Mario Vargas:2019-01-01",
                "");
        String conError = String.join(System.lineSeparator(),
                "HUESPED;2000000002;Pedro Mora;1990-01-01;3009876543;pedro@correo.com",
                "RESERVA;2000000002;X-999;2027-06-20;2027-06-23;DIRECTA;Pedro Mora:1990-01-01",
                "");

        Path archivo = null;
        try {
            archivo = Files.createTempFile("sga-prueba", ".txt");

            Files.writeString(archivo, valido);
            cargador.cargarDesdeArchivo(archivo.toString());
            System.out.println("Archivo valido cargado: " + apartamentos.tamano() + " apartamento(s), "
                    + huespedes.tamano() + " huesped(es)");
            Apartamento d101 = apartamentos.buscar(Apartamento.paraBusqueda("D-101"));
            System.out.println("Reservas de D-101: " + d101.numeroReservas() + " -> "
                    + d101.getReservas().getRaiz().getDato());

            Files.writeString(archivo, conError);
            try {
                cargador.cargarDesdeArchivo(archivo.toString());
                System.out.println("ERROR: se cargo un archivo con una referencia inexistente");
            } catch (ExcepcionSGA e) {
                System.out.println("Archivo con error rechazado -> " + e.getMessage());
            }

            try {
                cargador.cargarDesdeArchivo("/ruta/que/no/existe.txt");
            } catch (IOException e) {
                System.out.println("Archivo inexistente -> " + e.getClass().getSimpleName());
            }
        } catch (IOException | ExcepcionSGA e) {
            System.out.println("ERROR en la prueba de archivo: " + e.getMessage());
        } finally {
            if (archivo != null) {
                try {
                    Files.deleteIfExists(archivo);
                } catch (IOException ignorada) {
                    // el archivo temporal se borra solo al reiniciar; no afecta la prueba
                }
            }
        }
    }

    // ==================== 11. Recorridos del arbol ====================

    private static void pruebaRecorridosDelArbol(ArbolBinarioBusqueda<Apartamento> apartamentos) {
        titulo("11. RECORRIDOS DEL ARBOL, MINIMO/MAXIMO E ITERADOR PROPIO");
        System.out.println("Altura del arbol de apartamentos: " + apartamentos.altura()
                + " (codigos insertados en orden ascendente -> el arbol queda como una cadena)");
        System.out.println("inOrden   : " + codigos(apartamentos.inOrden()));
        System.out.println("preOrden  : " + codigos(apartamentos.preOrden()));
        System.out.println("postOrden : " + codigos(apartamentos.postOrden()));
        System.out.println("Menor codigo: " + apartamentos.obtenerMinimo().getCodigo()
                + " | mayor codigo: " + apartamentos.obtenerMaximo().getCodigo());

        StringBuilder porIterador = new StringBuilder();
        for (Apartamento a : apartamentos) {
            porIterador.append(a.getCodigo()).append(' ');
        }
        System.out.println("for-each (iterador propio, perezoso): " + porIterador.toString().trim());

        // Un arbol con forma distinta muestra la diferencia entre los tres recorridos
        ArbolBinarioBusqueda<Integer> numeros = new ArbolBinarioBusqueda<>();
        for (int n : new int[] {50, 30, 70, 20, 40, 60, 80}) {
            numeros.insertar(n);
        }
        System.out.println("Arbol de enteros (raiz 50, altura " + numeros.altura() + "):");
        System.out.println("   inOrden   : " + numeros.inOrden());
        System.out.println("   preOrden  : " + numeros.preOrden());
        System.out.println("   postOrden : " + numeros.postOrden());
    }

    private static String codigos(ListaDoblementeEnlazada<Apartamento> lista) {
        StringBuilder sb = new StringBuilder();
        for (Apartamento a : lista) {
            sb.append(a.getCodigo()).append(' ');
        }
        return sb.toString().trim();
    }

    // ==================== 12. Turnos y administracion ====================

    private static void pruebaTurnosYAdministracion(ArbolBinarioBusqueda<Apartamento> apartamentos,
                                                    ArbolBinarioBusqueda<Huesped> huespedes) throws ExcepcionSGA {
        titulo("12. TURNOS DE RECEPCION (LISTA CIRCULAR) Y ADMINISTRACION");
        GestorTurnos turnos = new GestorTurnos();
        GestorAdministracion admin = new GestorAdministracion(apartamentos, huespedes, turnos);

        try {
            turnos.turnoActual();
            System.out.println("ERROR: se consulto un turno sin recepcionistas");
        } catch (EstructuraVaciaException e) {
            System.out.println("Turno sin recepcionistas -> " + e.getMessage());
        }

        admin.registrarEmpleado(new Empleado("900001", "Camila Rojas", RolEmpleado.RECEPCIONISTA, "3001112222"));
        admin.registrarEmpleado(new Empleado("900002", "Julian Mora", RolEmpleado.RECEPCIONISTA, "3002223333"));
        admin.registrarEmpleado(new Empleado("900003", "Elena Ruiz", RolEmpleado.RECEPCIONISTA, "3003334444"));
        admin.registrarEmpleado(new Empleado("900004", "Pedro Soto", RolEmpleado.PERSONAL_ASEO, "3004445555"));
        admin.registrarEmpleado(new Empleado("900005", "Ana Vera", RolEmpleado.ADMINISTRADOR, "3005556666"));

        System.out.println("Empleados (arbol, orden por documento):");
        for (Empleado e : admin.listarEmpleados()) {
            System.out.println("   " + e);
        }
        System.out.println("Recepcionistas en la rotacion: " + turnos.cantidad());
        System.out.println("Turno actual: " + turnos.turnoActual().getNombre());
        for (int i = 1; i <= 4; i++) {
            System.out.println("Termina el turno -> recibe " + turnos.pasarTurno().getNombre());
        }
        System.out.println("(con 3 recepcionistas la rotacion vuelve a empezar: es una lista circular)");

        try {
            admin.registrarEmpleado(new Empleado("900001", "Otra Persona", RolEmpleado.ADMINISTRADOR, ""));
            System.out.println("ERROR: se registro un documento repetido");
        } catch (ExcepcionSGA e) {
            System.out.println("Empleado repetido rechazado -> " + e.getMessage());
        }
        try {
            turnos.agregarRecepcionista(admin.buscarEmpleado("900004"));
            System.out.println("ERROR: personal de aseo entro a la rotacion de recepcion");
        } catch (ExcepcionSGA e) {
            System.out.println("Solo recepcionistas rotan -> " + e.getMessage());
        }

        String quienTieneElTurno = turnos.turnoActual().getDocumento();
        admin.eliminarEmpleado(quienTieneElTurno);
        System.out.println("Se retira a quien tenia el turno (" + quienTieneElTurno + ") -> ahora atiende "
                + turnos.turnoActual().getNombre() + " | recepcionistas restantes: " + turnos.cantidad());
        System.out.println("Personal de aseo: " + admin.listarEmpleadosPorRol(RolEmpleado.PERSONAL_ASEO));

        // --- Apartamentos ---
        Apartamento nuevo = new Apartamento("D-101", "Frailejon", "D", 1, 2, 4, "Chimenea");
        try {
            admin.registrarApartamento(nuevo);
            System.out.println("ERROR: se registro un apartamento sin tarifas");
        } catch (ExcepcionSGA e) {
            System.out.println("Apartamento sin tarifas rechazado -> " + e.getMessage());
        }
        nuevo.definirTarifa(Temporada.BAJA, 70000);
        nuevo.definirTarifa(Temporada.MEDIA, 90000);
        nuevo.definirTarifa(Temporada.ALTA, 130000);
        admin.registrarApartamento(nuevo);
        System.out.println("D-101 registrado. Apartamentos: " + apartamentos.tamano());

        Apartamento copia = new Apartamento("D-101", "Copia", "D", 1, 1, 2, "");
        for (Temporada t : Temporada.values()) {
            copia.definirTarifa(t, 1000);
        }
        try {
            admin.registrarApartamento(copia);
            System.out.println("ERROR: se registro un codigo repetido");
        } catch (ExcepcionSGA e) {
            System.out.println("Codigo repetido rechazado -> " + e.getMessage());
        }

        admin.editarApartamento("D-101", "Frailejon Real", "D", 2, "Chimenea y balcon");
        System.out.println("Editado: " + admin.buscarApartamento("D-101") + " | "
                + admin.buscarApartamento("D-101").getCaracteristicas());

        try {
            admin.eliminarApartamento("C-101");
            System.out.println("ERROR: se elimino un apartamento con reserva activa");
        } catch (ExcepcionSGA e) {
            System.out.println("Eliminar con reserva activa rechazado -> " + e.getMessage());
        }
        admin.eliminarApartamento("D-101");
        System.out.println("D-101 eliminado. Apartamentos: " + apartamentos.tamano());
        try {
            admin.eliminarApartamento("Z-999");
        } catch (ExcepcionSGA e) {
            System.out.println("Eliminar inexistente -> " + e.getMessage());
        }

        // --- Huespedes ---
        admin.registrarHuesped(new Huesped("1094000099", "Temporal Prueba", LocalDate.of(2000, 1, 1),
                "3000000000", "temporal@correo.com"));
        admin.actualizarHuesped("1094000099", "Temporal Actualizado", LocalDate.of(2000, 1, 1),
                "3111111111", "nuevo@correo.com");
        System.out.println("Huesped actualizado: " + admin.buscarHuesped("1094000099")
                + " | " + admin.buscarHuesped("1094000099").getCorreo());
        try {
            admin.eliminarHuesped("1094000003");
            System.out.println("ERROR: se elimino un huesped con reserva activa");
        } catch (ExcepcionSGA e) {
            System.out.println("Eliminar huesped con reserva activa rechazado -> " + e.getMessage());
        }
        admin.eliminarHuesped("1094000099");
        System.out.println("Huesped temporal eliminado. Huespedes: " + huespedes.tamano());
    }

    // ==================== 13. Excepciones propias ====================

    private static void pruebaExcepcionesPropias(ArbolBinarioBusqueda<Apartamento> apartamentos) {
        titulo("13. EXCEPCIONES PROPIAS: TRANSICION INVALIDA Y ESTRUCTURA VACIA");
        Reserva confirmada = apartamentos.buscar(Apartamento.paraBusqueda("A-102")).getReservas().getRaiz().getDato();
        System.out.println("Reserva de prueba: " + confirmada);
        try {
            confirmada.finalizar();
            System.out.println("ERROR: se finalizo una reserva que no estaba en curso");
        } catch (TransicionInvalidaException e) {
            System.out.println("CONFIRMADA -> FINALIZADA -> TransicionInvalidaException: " + e.getMessage());
            System.out.println("   Sigue siendo un IllegalStateException: " + (e instanceof IllegalStateException));
        }

        try {
            new ListaCircular<String>().siguiente();
        } catch (EstructuraVaciaException e) {
            System.out.println("Lista circular vacia -> EstructuraVaciaException: " + e.getMessage());
        }
        try {
            new ListaDoblementeEnlazada<String>().primero();
        } catch (EstructuraVaciaException e) {
            System.out.println("Lista doble vacia (primero) -> EstructuraVaciaException: " + e.getMessage());
        }
        try {
            new ListaDoblementeEnlazada<String>().ultimo();
        } catch (EstructuraVaciaException e) {
            System.out.println("Lista doble vacia (ultimo) -> EstructuraVaciaException: " + e.getMessage());
        }
        try {
            new ArbolBinarioBusqueda<Integer>().obtenerMaximo();
        } catch (EstructuraVaciaException e) {
            System.out.println("Arbol vacio -> EstructuraVaciaException: " + e.getMessage());
        }
    }

    // ==================== 14. Persistencia ====================

    private static void pruebaPersistencia(CargadorDatos cargador, GestorReservas reservas,
                                           GestorTemporadas temporadas,
                                           ArbolBinarioBusqueda<Apartamento> apartamentos,
                                           ArbolBinarioBusqueda<Huesped> huespedes) throws ExcepcionSGA {
        titulo("14. PERSISTENCIA: GUARDAR EN ARCHIVO PLANO Y RECARGAR");
        Huesped sofia = huespedes.buscar(Huesped.paraBusqueda("1094000005"));
        Apartamento a201 = apartamentos.buscar(Apartamento.paraBusqueda("A-201"));

        // Casos dificiles: una reserva Portal pendiente (con su vencimiento), un favorito y un apartamento fuera de servicio
        Reserva pendiente = reservas.crearReserva(sofia, a201,
                new Estancia(LocalDate.of(2027, 2, 10), LocalDate.of(2027, 2, 12)),
                ocupantes("Sofia Torres:1992-01-09"), CanalReserva.PORTAL, LocalDateTime.of(2026, 11, 14, 9, 0));
        sofia.agregarFavorito(a201);
        a201.ponerFueraDeServicio("Pintura de fachada");

        Path archivo = null;
        try {
            archivo = Files.createTempFile("sga-guardado", ".txt");
            cargador.guardarEnArchivo(archivo.toString());

            System.out.println("Contenido guardado (registros FAVORITO, RESERVA y ESTADO):");
            try (BufferedReader lector = Files.newBufferedReader(archivo)) {
                String linea;
                while ((linea = lector.readLine()) != null) {
                    if (linea.startsWith("FAVORITO") || linea.startsWith("RESERVA") || linea.startsWith("ESTADO")) {
                        System.out.println("   " + linea);
                    }
                }
            }

            ArbolBinarioBusqueda<Apartamento> apartamentos2 = new ArbolBinarioBusqueda<>();
            ArbolBinarioBusqueda<Huesped> huespedes2 = new ArbolBinarioBusqueda<>();
            GestorTemporadas temporadas2 = new GestorTemporadas();
            GestorReservas reservas2 = new GestorReservas(new CotizadorService(temporadas2), new AsignadorGrupos());
            CargadorDatos cargador2 = new CargadorDatos(apartamentos2, huespedes2, reservas2, temporadas2);
            cargador2.cargarDesdeArchivo(archivo.toString());

            int diferencias = 0;
            if (apartamentos.tamano() != apartamentos2.tamano() || huespedes.tamano() != huespedes2.tamano()) {
                diferencias++;
            }
            int totalReservas = 0;
            for (Apartamento a : apartamentos) {
                Apartamento b = apartamentos2.buscar(a);
                if (b == null || a.getEstadoOperativo() != b.getEstadoOperativo()
                        || a.numeroReservas() != b.numeroReservas()) {
                    diferencias++;
                    continue;
                }
                Iterator<Reserva> recargadas = b.getReservas().iterator();
                for (Reserva r : a.getReservas()) {
                    totalReservas++;
                    Reserva q = recargadas.next();
                    if (r.getEstado() != q.getEstado() || r.getCanal() != q.getCanal()
                            || !r.getEstancia().getEntrada().equals(q.getEstancia().getEntrada())
                            || !r.getEstancia().getSalida().equals(q.getEstancia().getSalida())
                            || r.numeroPersonas() != q.numeroPersonas()
                            || !r.getVenceEn().equals(q.getVenceEn())) {
                        diferencias++;
                    }
                }
            }
            for (Huesped h : huespedes) {
                Huesped g = huespedes2.buscar(h);
                if (g == null || g.getFavoritos().tamano() != h.getFavoritos().tamano()) {
                    diferencias++;
                }
            }
            if (temporadas.getPeriodos().tamano() != temporadas2.getPeriodos().tamano()) {
                diferencias++;
            }
            System.out.println("Recargado: " + apartamentos2.tamano() + " apartamentos, " + huespedes2.tamano()
                    + " huespedes, " + totalReservas + " reservas vigentes, " + temporadas2.getPeriodos().tamano()
                    + " periodos de temporada");
            System.out.println("Diferencias entre el estado original y el recargado: " + diferencias
                    + (diferencias == 0 ? "  (OK)" : "  (ERROR)"));

            Reserva recargadaPortal = apartamentos2.buscar(Apartamento.paraBusqueda("A-201"))
                    .getReservas().getRaiz().getDato();
            System.out.println("Reserva Portal pendiente recargada: " + recargadaPortal
                    + " | vence " + recargadaPortal.getVenceEn()
                    + " | estado " + (recargadaPortal.getEstado() == EstadoReserva.PENDIENTE ? "PENDIENTE (OK)" : "ERROR"));
            Apartamento a201Recargado = apartamentos2.buscar(Apartamento.paraBusqueda("A-201"));
            System.out.println("A-201 recargado: " + a201Recargado.getEstadoOperativo()
                    + " | motivo: " + a201Recargado.getMotivoFueraServicio());
        } catch (IOException e) {
            System.out.println("ERROR de lectura/escritura: " + e.getMessage());
        } finally {
            a201.cambiarEstado(EstadoApartamento.PREPARADO);
            sofia.quitarFavorito(a201);
            reservas.cancelar(pendiente, 0, HOY);
            if (archivo != null) {
                try {
                    Files.deleteIfExists(archivo);
                } catch (IOException ignorada) {
                    // el archivo temporal se borra solo al reiniciar; no afecta la prueba
                }
            }
        }
    }

    // ==================== 15. Controlador (fachada para la GUI) ====================



    // ==================== PARTE 2: topologia, colas y pilas ====================

    /** Entorno limpio (escenario de prueba recien cargado) para que la Parte 2 no dependa de la Parte 1. */
    private static final class Entorno {
        final ArbolBinarioBusqueda<Apartamento> apartamentos = new ArbolBinarioBusqueda<>();
        final ArbolBinarioBusqueda<Huesped> huespedes = new ArbolBinarioBusqueda<>();
        final GestorTemporadas temporadas = new GestorTemporadas();
        final GestorReservas reservas =
                new GestorReservas(new CotizadorService(temporadas), new AsignadorGrupos());

        Entorno() throws ExcepcionSGA {
            new CargadorDatos(apartamentos, huespedes, reservas, temporadas).cargarEscenarioPrueba();
        }

        Apartamento apartamento(String codigo) {
            return apartamentos.buscar(Apartamento.paraBusqueda(codigo));
        }

        Huesped huesped(String documento) {
            return huespedes.buscar(Huesped.paraBusqueda(documento));
        }
    }

    private static void pruebaParteDos() throws ExcepcionSGA {
        Entorno entorno = new Entorno();
        pruebaColaCanalExterno(entorno);
        pruebaPilaNavegacion();
        MapaEdificio mapa = new MapaEdificio(entorno);
        pruebaGrafoDelEdificio(mapa);
        pruebaColaPrioritariaYRuta(entorno, mapa);
        pruebaCierreDePasillo(mapa);
    }

    // ---------- 15. Cola FIFO: canal externo ----------

    /** Solicitud que envia la plataforma de terceros (simulada). */
    private record SolicitudExterna(String documento, String codigoApartamento, LocalDate entrada, LocalDate salida) {
        @Override
        public String toString() {
            return "EXT[" + codigoApartamento + " " + entrada + "->" + salida + " titular " + documento + "]";
        }
    }

    private static void pruebaColaCanalExterno(Entorno entorno) throws ExcepcionSGA {
        titulo("15. COLA FIFO: SOLICITUDES DEL CANAL EXTERNO (simulado)");
        System.out.println("Nota: el procesador del canal externo aun no existe como servicio; aqui se simula con Cola<T>.");

        Cola<SolicitudExterna> cola = new Cola<>();
        System.out.println("Cola vacia? " + cola.isEmpty() + " | contenido: " + cola);

        SolicitudExterna s1 = new SolicitudExterna("1094000005", "A-201", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 4));
        SolicitudExterna s2 = new SolicitudExterna("1094000002", "C-101", LocalDate.of(2026, 12, 24), LocalDate.of(2026, 12, 26));
        SolicitudExterna s3 = new SolicitudExterna("1094000001", "B-102", LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 7));
        cola.add(s1);
        cola.add(s2);
        cola.add(s3);

        Cola<SolicitudExterna> lote = new Cola<>();
        lote.add(new SolicitudExterna("1094000004", "A-201", LocalDate.of(2026, 12, 3), LocalDate.of(2026, 12, 6)));
        cola.merge(lote);
        System.out.println("Llegaron 3 solicitudes y luego un lote de 1 (merge): " + cola);
        System.out.println("Primera en la fila (peek): " + cola.peek().getDato());
        System.out.println("Contiene la solicitud de C-101? " + cola.contains(s2));
        try {
            cola.merge(new Cola<>());
        } catch (IllegalArgumentException e) {
            System.out.println("Fusionar una cola vacia -> " + e.getMessage());
        }

        ListaDoblementeEnlazada<String> conflictos = new ListaDoblementeEnlazada<>();
        int aceptadas = 0;
        System.out.println("Procesando en orden de llegada (FIFO):");
        while (!cola.isEmpty()) {
            SolicitudExterna s = cola.poll();
            Huesped titular = entorno.huesped(s.documento());
            ListaDoblementeEnlazada<Ocupante> ocupantes = new ListaDoblementeEnlazada<>();
            ocupantes.agregar(new Ocupante(titular.getNombre(), titular.getFechaNacimiento()));
            try {
                Reserva r = entorno.reservas.crearReserva(titular, entorno.apartamento(s.codigoApartamento()),
                        new Estancia(s.entrada(), s.salida()), ocupantes, CanalReserva.EXTERNA);
                aceptadas++;
                System.out.println("   ACEPTADA  " + s + " -> " + r.getId());
            } catch (ReservaSolapadaException e) {
                conflictos.agregar(s.toString());
                System.out.println("   RECHAZADA " + s + " -> conflicto de canal: " + e.getMessage());
            }
        }
        System.out.println("Cola vacia al terminar? " + cola.isEmpty());
        System.out.println("Resumen: " + aceptadas + " aceptadas, " + conflictos.tamano() + " conflictos de canal " + conflictos);
        System.out.println("(la ultima solicitud choca con la primera porque comparten la noche del 2026-12-03: el orden de llegada decide)");
    }

    // ---------- 16. Pila: navegacion "atras" ----------

    private static void pruebaPilaNavegacion() {
        titulo("16. PILA: NAVEGACION \"ATRAS\" ENTRE PANTALLAS");
        System.out.println("Nota: el deshacer del folio aun no existe; aqui se muestra la pila con la navegacion entre pantallas.");
        Pila<String> pantallas = new Pila<>();
        System.out.println("Pila vacia? " + pantallas.isEmpty() + " | contenido: " + pantallas);
        pantallas.push("Inicio");
        pantallas.push("Panel de reservas");
        pantallas.push("Calendario de ocupacion");
        System.out.println("Tras 3 push (cima primero): " + pantallas);
        System.out.println("Pantalla actual (peek): " + pantallas.peek().getDato());
        System.out.println("Atras -> sale " + pantallas.pop() + " | queda: " + pantallas);
        System.out.println("Atras -> sale " + pantallas.pop() + " | queda: " + pantallas);
        pantallas.push("Mapa del edificio");
        System.out.println("Nueva pantalla -> " + pantallas + " (la cima es " + pantallas.peek().getDato() + ")");
    }

    // ---------- Mapa del edificio ----------

    /**
     * Mapa del alojamiento: nodos = apartamentos, recepcion, escaleras y cuartos de aseo;
     * aristas = pasillos con peso en metros. Las escaleras se modelan con NodoGrafo&lt;String&gt; porque el
     * constructor de Escaleras no es publico (solo se puede crear dentro de su propio paquete).
     */
    private static final class MapaEdificio {
        final Grafo grafo = new Grafo();
        final NodoGrafo<Recepcion> recepcion = new NodoGrafo<>(new Recepcion(1));
        final NodoGrafo<String> escalera = new NodoGrafo<>("Escaleras numero: 1");
        final NodoGrafo<CuartosAseo> aseo1 = new NodoGrafo<>(new CuartosAseo(1));
        final NodoGrafo<CuartosAseo> aseo2 = new NodoGrafo<>(new CuartosAseo(2));
        final NodoGrafo<?>[] todos;

        MapaEdificio(Entorno entorno) {
            NodoGrafo<Apartamento> a101 = new NodoGrafo<>(entorno.apartamento("A-101"));
            NodoGrafo<Apartamento> a102 = new NodoGrafo<>(entorno.apartamento("A-102"));
            NodoGrafo<Apartamento> a201 = new NodoGrafo<>(entorno.apartamento("A-201"));
            NodoGrafo<Apartamento> b101 = new NodoGrafo<>(entorno.apartamento("B-101"));
            NodoGrafo<Apartamento> b102 = new NodoGrafo<>(entorno.apartamento("B-102"));
            NodoGrafo<Apartamento> b201 = new NodoGrafo<>(entorno.apartamento("B-201"));
            NodoGrafo<Apartamento> c101 = new NodoGrafo<>(entorno.apartamento("C-101"));
            NodoGrafo<Apartamento> c201 = new NodoGrafo<>(entorno.apartamento("C-201"));
            todos = new NodoGrafo<?>[] {recepcion, escalera, aseo1, aseo2,
                    a101, a102, a201, b101, b102, b201, c101, c201};

            // Piso 1
            grafo.agregarNodo(recepcion, a101, 10);
            grafo.agregarNodo(a101, a102, 5);
            grafo.agregarNodo(recepcion, b101, 12);
            grafo.agregarNodo(b101, b102, 5);
            grafo.agregarNodo(recepcion, c101, 15);
            grafo.agregarNodo(recepcion, aseo1, 8);
            // Escaleras y piso 2
            grafo.agregarNodo(recepcion, escalera, 6);
            grafo.agregarNodo(a102, escalera, 11);
            grafo.agregarNodo(escalera, a201, 7);
            grafo.agregarNodo(escalera, b201, 9);
            grafo.agregarNodo(b201, c201, 6);
            grafo.agregarNodo(escalera, aseo2, 4);
        }

        /** Nodo cuyo contenido es el apartamento con ese codigo (null si no existe). */
        NodoGrafo<?> nodoDe(String codigo) {
            for (NodoGrafo<?> nodo : todos) {
                if (nodo.getContenido() instanceof Apartamento a && a.getCodigo().equals(codigo)) {
                    return nodo;
                }
            }
            return null;
        }

        /** Cierra el pasillo entre dos nodos quitando la arista de ambos extremos. */
        boolean cerrarPasillo(NodoGrafo<?> uno, NodoGrafo<?> otro) {
            for (var arista : uno.getAdyacentes()) {
                if (arista.getExtremo(uno) == otro) {
                    uno.quitarAdyacente(arista);
                    otro.quitarAdyacente(arista);
                    return true;
                }
            }
            return false;
        }
    }

    /** Nombre corto de un nodo para imprimir rutas. */
    private static String nombre(NodoGrafo<?> nodo) {
        if (nodo.getContenido() instanceof Apartamento a) {
            return a.getCodigo();
        }
        return String.valueOf(nodo.getContenido()).replace(" numero: ", " "); // "Recepcion numero: 1" -> "Recepcion 1"
    }

    private static String ruta(ListaDoblementeEnlazada<NodoGrafo<?>> camino) {
        if (camino.estaVacia()) {
            return "(sin ruta)";
        }
        StringBuilder sb = new StringBuilder();
        for (NodoGrafo<?> n : camino) {
            if (sb.length() > 0) {
                sb.append(" -> ");
            }
            sb.append(nombre(n));
        }
        return sb.toString();
    }

    // ---------- 17. Grafo: recorridos y ruta mas corta ----------

    private static void pruebaGrafoDelEdificio(MapaEdificio mapa) {
        titulo("17. GRAFO DEL EDIFICIO: DFS, BFS Y DIJKSTRA");
        System.out.println("Nodos: " + mapa.todos.length + " (8 apartamentos, recepcion, escaleras y 2 cuartos de aseo)."
                + " Pasillos con peso en metros.");
        System.out.println("Pasillos que salen de recepcion:");
        System.out.print(mapa.grafo.imprimir(mapa.recepcion));

        try {
            mapa.grafo.agregarNodo(mapa.recepcion, mapa.escalera, 99);
            System.out.println("ERROR: se acepto un pasillo repetido");
        } catch (IllegalArgumentException e) {
            System.out.println("Pasillo repetido rechazado -> " + e.getMessage());
        }

        ListaDoblementeEnlazada<NodoGrafo<?>> componente =
                mapa.grafo.obtenerGrafo(mapa.recepcion, new ListaDoblementeEnlazada<>());
        System.out.println("obtenerGrafo desde recepcion: " + componente.tamano() + " nodos conectados");

        Conjunto<NodoGrafo<?>> dfs = mapa.grafo.dfs(mapa.recepcion);
        System.out.println("DFS desde recepcion alcanza " + dfs.tamano() + " nodos (sin repetidos)");

        NodoGrafo<?> destino = mapa.nodoDe("C-201");
        System.out.println("BFS recepcion -> C-201 (orden de visita): " + ruta(mapa.grafo.BFS(mapa.recepcion, destino)));

        System.out.println("Dijkstra desde recepcion (ruta y metros):");
        String[] codigos = {"A-101", "A-102", "A-201", "B-102", "C-101", "C-201"};
        for (String codigo : codigos) {
            NodoGrafo<?> fin = mapa.nodoDe(codigo);
            System.out.println("   " + codigo + ": " + ruta(mapa.grafo.armarCamino(mapa.recepcion, fin))
                    + " | " + mapa.grafo.costoCamino(mapa.recepcion, fin) + " m");
        }
        System.out.println("(A-102 se alcanza por A-101: 10+5=15 m, mejor que por las escaleras: 6+11=17 m)");
    }

    // ---------- 18. Cola prioritaria: preparacion y su ruta ----------

    /** Tarea de preparacion. Su orden natural es la prioridad que pide el enunciado. */
    private static final class TareaPreparacion implements Comparable<TareaPreparacion> {
        private static final Comparator<TareaPreparacion> PRIORIDAD = Comparator
                .comparing((TareaPreparacion t) -> t.horaLlegada == null)               // entrada hoy primero
                .thenComparing(t -> t.horaLlegada, Comparator.nullsLast(Comparator.<LocalTime>naturalOrder()))
                .thenComparing(t -> t.codigo);                                          // desempate estable

        final String codigo;
        final LocalTime horaLlegada; // null = sin entrada hoy

        TareaPreparacion(String codigo, LocalTime horaLlegada) {
            this.codigo = codigo;
            this.horaLlegada = horaLlegada;
        }

        @Override
        public int compareTo(TareaPreparacion otra) {
            return PRIORIDAD.compare(this, otra);
        }

        @Override
        public String toString() {
            return codigo + (horaLlegada == null ? " (P2, sin entrada hoy)" : " (P1, llega " + horaLlegada + ")");
        }
    }

    private static void pruebaColaPrioritariaYRuta(Entorno entorno, MapaEdificio mapa) {
        titulo("18. COLA PRIORITARIA DE PREPARACION Y RUTA CON DIJKSTRA (simulado)");
        System.out.println("Nota: el servicio de preparacion aun no existe; aqui la cola se arma con ColaPrioritaria<T>.");

        ColaPrioritaria<Integer> enteros = new ColaPrioritaria<>();
        for (int n : new int[] {5, 3, 9, 1, 7}) {
            enteros.add(n);
        }
        System.out.println("Orden natural con enteros (5,3,9,1,7 entran en desorden): " + enteros);

        ColaPrioritaria<TareaPreparacion> cola = new ColaPrioritaria<>(Comparator.comparingInt(TareaPreparacion::getPrioridad)
                .thenComparing(TareaPreparacion::getHoraLlegada);
);
        TareaPreparacion[] tareas = {
                new TareaPreparacion("C-101", null),
                new TareaPreparacion("A-102", LocalTime.of(15, 0)),
                new TareaPreparacion("B-102", null),
                new TareaPreparacion("B-101", LocalTime.of(11, 30)),
        };
        for (TareaPreparacion t : tareas) {
            entorno.apartamento(t.codigo).cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION);
            cola.add(t);
        }
        System.out.println("Tras el check-out quedan PENDIENTE_PREPARACION: C-101, A-102, B-102, B-101");
        System.out.println("Cola (prioridad 1 por hora de llegada, luego prioridad 2): " + cola);

        NodoGrafo<?> posicion = mapa.recepcion;
        int total = 0;
        System.out.println("Atencion en orden de prioridad, partiendo de recepcion:");
        while (!cola.isEmpty()) {
            TareaPreparacion t = cola.poll();
            NodoGrafo<?> destino = mapa.nodoDe(t.codigo);
            Integer metros = mapa.grafo.costoCamino(posicion, destino);
            System.out.println("   " + t.codigo + ": " + ruta(mapa.grafo.armarCamino(posicion, destino)) + " | " + metros + " m");
            total += metros;
            Apartamento apto = entorno.apartamento(t.codigo);
            apto.cambiarEstado(EstadoApartamento.EN_PREPARACION);
            apto.cambiarEstado(EstadoApartamento.PREPARADO);
            posicion = destino;
        }
        System.out.println("Recorrido total: " + total + " m | cola vacia? " + cola.isEmpty()
                + " | estado final de B-101: " + entorno.apartamento("B-101").getEstadoOperativo());
    }

    // ---------- 19. Cierre de pasillo e inalcanzables ----------

    private static void pruebaCierreDePasillo(MapaEdificio mapa) {
        titulo("19. ANALISIS DEL GRAFO: PASILLOS CERRADOS E INALCANZABLES");
        NodoGrafo<?> a201 = mapa.nodoDe("A-201");
        NodoGrafo<?> a102 = mapa.nodoDe("A-102");
        System.out.println("Con todo abierto: recepcion -> A-201 = " + mapa.grafo.costoCamino(mapa.recepcion, a201) + " m");

        mapa.cerrarPasillo(mapa.recepcion, mapa.escalera);
        System.out.println("Se cierra el pasillo recepcion-escaleras:");
        listarInalcanzables(mapa);
        System.out.println("   A-201 ahora: " + ruta(mapa.grafo.armarCamino(mapa.recepcion, a201))
                + " | " + mapa.grafo.costoCamino(mapa.recepcion, a201) + " m (da la vuelta por A-101 y A-102)");

        mapa.cerrarPasillo(a102, mapa.escalera);
        System.out.println("Se cierra tambien A-102 - escaleras:");
        listarInalcanzables(mapa);
        System.out.println("   A-201 ahora: " + ruta(mapa.grafo.armarCamino(mapa.recepcion, a201))
                + " | costo: " + mapa.grafo.costoCamino(mapa.recepcion, a201));

        mapa.grafo.agregarNodo(mapa.recepcion, mapa.escalera, 6);
        mapa.grafo.agregarNodo(a102, mapa.escalera, 11);
        System.out.println("Se reabren ambos pasillos: recepcion -> A-201 = "
                + mapa.grafo.costoCamino(mapa.recepcion, a201) + " m");
        listarInalcanzables(mapa);
    }

    private static void listarInalcanzables(MapaEdificio mapa) {
        Conjunto<NodoGrafo<?>> alcanzables = mapa.grafo.dfs(mapa.recepcion);
        StringBuilder sb = new StringBuilder();
        for (NodoGrafo<?> nodo : mapa.todos) {
            if (!alcanzables.contiene(nodo)) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(nombre(nodo));
            }
        }
        System.out.println("   Alcanzables desde recepcion: " + alcanzables.tamano() + " de " + mapa.todos.length
                + " | inalcanzables: " + (sb.length() == 0 ? "ninguno" : sb));
    }

    // ==================== Utilidades ====================

    /** Crea una lista de ocupantes a partir de textos "Nombre:AAAA-MM-DD". */
    private static ListaDoblementeEnlazada<Ocupante> ocupantes(String... datos) {
        ListaDoblementeEnlazada<Ocupante> lista = new ListaDoblementeEnlazada<>();
        for (String d : datos) {
            String[] partes = d.split(":");
            lista.agregar(new Ocupante(partes[0], LocalDate.parse(partes[1])));
        }
        return lista;
    }

    private static String dinero(double valor) {
        return String.format("$%,.0f", valor);
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("==================================================");
        System.out.println(texto);
        System.out.println("==================================================");
    }
}