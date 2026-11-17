
package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import model.ApartaSuites;
import model.enums.CanalReserva;
import model.enums.EstadoApartamento;
import model.enums.EstadoReserva;
import model.enums.RolUsuario;
import model.enums.Temporada;
import model.estructuras.listaDoble.ListaDoblementeEnlazada;
import model.excepciones.ExcepcionSGA;
import model.modelo.Apartamento;
import model.modelo.Empleado;
import model.modelo.Estancia;
import model.modelo.Huesped;
import model.modelo.Ocupante;
import model.modelo.PeriodoTemporada;
import model.modelo.Reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

/**
 * Aplicación de demostración.
 * Los datos se crean en memoria cada vez que inicia el programa;
 * no se guardan ni modifican archivos de persistencia.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/views/login.fxml")
        );

        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Aparta-Suites UQ - Iniciar sesión");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) throws ExcepcionSGA {
        ApartaSuites sistema = ApartaSuites.getInstance();

        cargarDatosDemostracion(sistema);

        launch(args);
    }

    private static void cargarDatosDemostracion(
            ApartaSuites sistema
    ) throws ExcepcionSGA {

        registrarEmpleados(sistema);
        registrarTemporadas(sistema);
        registrarApartamentos(sistema);

        Huesped[] huespedes = registrarHuespedes(sistema);

        cargarReservasHistoricas(sistema, huespedes);
        cargarReservasActuales(sistema, huespedes);
        cargarReservasProximas(sistema, huespedes);
        cargarSolicitudesExternas(sistema, huespedes);
    }

    // =========================================================
    // EMPLEADOS
    // =========================================================

    private static void registrarEmpleados(
            ApartaSuites sistema
    ) throws ExcepcionSGA {

        // Contraseña de todas las cuentas de empleado: 1234.
        sistema.registrarEmpleado(
                new Empleado(
                        "1",
                        "Administrador Principal",
                        RolUsuario.ADMINISTRADOR,
                        "3001112233",
                        "1234"
                )
        );

        sistema.registrarEmpleado(
                new Empleado(
                        "1002",
                        "Laura Gómez - Recepción",
                        RolUsuario.RECEPCIONISTA,
                        "3002223344",
                        "1234"
                )
        );

        sistema.registrarEmpleado(
                new Empleado(
                        "1003",
                        "Carlos Ruiz - Recepción",
                        RolUsuario.RECEPCIONISTA,
                        "3003334455",
                        "1234"
                )
        );

        sistema.registrarEmpleado(
                new Empleado(
                        "1004",
                        "Mariana Torres - Administración",
                        RolUsuario.ADMINISTRADOR,
                        "3004445566",
                        "1234"
                )
        );

        sistema.registrarEmpleado(
                new Empleado(
                        "1005",
                        "Andrés Pérez - Recepción",
                        RolUsuario.RECEPCIONISTA,
                        "3005556677",
                        "1234"
                )
        );
    }

    // =========================================================
    // TEMPORADAS
    // =========================================================

    private static void registrarTemporadas(
            ApartaSuites sistema
    ) throws ExcepcionSGA {

        // La temporada BAJA se asigna automáticamente
        // a los meses que no estén configurados.
        sistema.definirPeriodo(
                new PeriodoTemporada(
                        Month.JUNE,
                        Month.AUGUST,
                        Temporada.MEDIA
                )
        );

        sistema.definirPeriodo(
                new PeriodoTemporada(
                        Month.SEPTEMBER,
                        Month.JANUARY,
                        Temporada.ALTA
                )
        );
    }

    // =========================================================
    // APARTAMENTOS
    // =========================================================

    private static void registrarApartamentos(
            ApartaSuites sistema
    ) throws ExcepcionSGA {

        Object[][] datos = {
                {"A-101", "Suite Roble", "A", 1, 1, 2},
                {"A-102", "Suite Nogal", "A", 1, 2, 4},
                {"A-103", "Suite Arrayán", "A", 1, 2, 4},
                {"A-201", "Suite Cedro", "A", 2, 1, 2},
                {"A-202", "Suite Olivo", "A", 2, 2, 4},
                {"A-203", "Suite Guadual", "A", 2, 3, 6},
                {"B-101", "Suite Luna", "B", 1, 1, 2},
                {"B-102", "Suite Sol", "B", 1, 2, 4},
                {"B-103", "Suite Aurora", "B", 1, 2, 4},
                {"B-201", "Suite Cielo", "B", 2, 1, 2},
                {"B-202", "Suite Estrella", "B", 2, 2, 4},
                {"C-101", "Suite Río", "C", 1, 2, 4},
                {"C-102", "Suite Bosque", "C", 1, 3, 6},
                {"C-201", "Suite Montaña", "C", 2, 2, 4},
                {"C-202", "Suite Mirador", "C", 2, 3, 6},
                {"D-101", "Suite Jardín", "D", 1, 1, 2}
        };

        for (int i = 0; i < datos.length; i++) {

            Object[] d = datos[i];

            Apartamento apartamento = new Apartamento(
                    (String) d[0],
                    (String) d[1],
                    (String) d[2],
                    (int) d[3],
                    (int) d[4],
                    (int) d[5],
                    "Alojamiento turístico amoblado, con baño privado, Wi-Fi y cocina equipada"
            );

            apartamento.definirTarifa(
                    Temporada.BAJA,
                    85000 + (i % 4) * 10000
            );

            apartamento.definirTarifa(
                    Temporada.MEDIA,
                    115000 + (i % 4) * 12000
            );

            apartamento.definirTarifa(
                    Temporada.ALTA,
                    155000 + (i % 4) * 15000
            );

            if (i == 15) {
                apartamento.ponerFueraDeServicio(
                        "Mantenimiento de redes hidráulicas (demostración)"
                );
            }

            sistema.registrarApartamento(apartamento);
        }
    }

    // =========================================================
    // HUÉSPEDES
    // =========================================================

    private static Huesped[] registrarHuespedes(
            ApartaSuites sistema
    ) throws ExcepcionSGA {

        String[][] datos = {
                {"10", "Laura Gómez", "1990-05-14", "3101112233", "laura.gomez@correo.com"},
                {"1094000002", "Carlos Ruiz", "1985-11-02", "3104445566", "carlos.ruiz@correo.com"},
                {"1094000003", "Marta López", "1978-03-21", "3207778899", "marta.lopez@correo.com"},
                {"1094000004", "Andrés Pérez", "1995-08-30", "3153334455", "andres.perez@correo.com"},
                {"1094000005", "Sofía Torres", "1992-01-09", "3186667788", "sofia.torres@correo.com"},
                {"1094000006", "Daniela Castro", "1988-07-19", "3112223344", "daniela.castro@correo.com"},
                {"1094000007", "Felipe Ramírez", "1982-03-11", "3123334455", "felipe.ramirez@correo.com"},
                {"1094000008", "Valentina Rojas", "1998-09-25", "3134445566", "valentina.rojas@correo.com"},
                {"1094000009", "Santiago Vargas", "1991-12-04", "3145556677", "santiago.vargas@correo.com"},
                {"1094000010", "Camila Moreno", "1987-06-16", "3166667788", "camila.moreno@correo.com"},
                {"1094000011", "Julián Herrera", "1975-02-08", "3177778899", "julian.herrera@correo.com"},
                {"1094000012", "Isabella Medina", "2000-10-12", "3198889900", "isabella.medina@correo.com"},
                {"1094000013", "Mateo Cárdenas", "1984-04-23", "3009876543", "mateo.cardenas@correo.com"},
                {"1094000014", "Natalia Ospina", "1993-08-07", "3018765432", "natalia.ospina@correo.com"},
                {"1094000015", "Sebastián Muñoz", "1989-11-29", "3027654321", "sebastian.munoz@correo.com"},
                {"1094000016", "Paula Giraldo", "1996-02-17", "3031112233", "paula.giraldo@correo.com"},
                {"1094000017", "David Restrepo", "1986-07-03", "3042223344", "david.restrepo@correo.com"},
                {"1094000018", "Manuela Valencia", "1994-12-22", "3053334455", "manuela.valencia@correo.com"},
                {"1094000019", "Tomás Marín", "1980-04-18", "3064445566", "tomas.marin@correo.com"},
                {"1094000020", "Gabriela Quintero", "1999-03-27", "3075556677", "gabriela.quintero@correo.com"},
                {"1094000021", "Nicolás Duque", "1991-10-05", "3086667788", "nicolas.duque@correo.com"},
                {"1094000022", "Juliana Arias", "1983-01-31", "3097778899", "juliana.arias@correo.com"},
                {"1094000023", "Samuel Montoya", "1997-05-09", "3108889900", "samuel.montoya@correo.com"},
                {"1094000024", "Mariana Salazar", "1989-09-13", "3119990011", "mariana.salazar@correo.com"}
        };

        Huesped[] resultado = new Huesped[datos.length];

        for (int i = 0; i < datos.length; i++) {

            String[] d = datos[i];

            resultado[i] = sistema.registrarHuesped(
                    new Huesped(
                            d[0],
                            d[1],
                            LocalDate.parse(d[2]),
                            d[3],
                            d[4],
                            "1"
                    )
            );
        }

        return resultado;
    }

    // =========================================================
    // RESERVAS HISTÓRICAS
    // =========================================================

    /**
     * Ocho estadías antiguas finalizadas y cuatro casos
     * cancelados o marcados como no-show.
     */
    private static void cargarReservasHistoricas(
            ApartaSuites sistema,
            Huesped[] huespedes
    ) throws ExcepcionSGA {

        LocalDate hoy = LocalDate.now();
        LocalDate entrada = hoy.minusDays(35);

        for (int i = 0; i < 12; i++) {

            Apartamento apartamento = sistema.buscarApartamento(
                    codigoApartamento(i)
            );

            Huesped titular = huespedes[i];

            CanalReserva canal = (i % 2 == 0)
                    ? CanalReserva.DIRECTA
                    : CanalReserva.PORTAL;

            Reserva reserva = crearReserva(
                    sistema,
                    titular,
                    apartamento,
                    entrada.plusDays(i % 3),
                    entrada.plusDays(i % 3 + 3),
                    canal,
                    LocalDateTime.now().minusDays(40).plusMinutes(i * 13L),
                    i % 3 == 0
            );

            pagarSiCorresponde(sistema, reserva);

            if (i == 2 || i == 7) {

                sistema.cancelarReserva(
                        reserva,
                        entrada.minusDays(2)
                );

            } else if (i == 3 || i == 9) {

                if (reserva.getEstado() == EstadoReserva.PENDIENTE) {
                    pagarPortalParaConfirmar(sistema, reserva);
                }

                sistema.marcarNoShow(reserva);

            } else {

                if (reserva.getEstado() == EstadoReserva.PENDIENTE) {
                    pagarPortalParaConfirmar(sistema, reserva);
                }

                // Saldar la reserva antes de finalizar la estancia.
                pagarSaldoPendiente(sistema, reserva);

                // Esta reserva pertenece al historial de demostración: su estancia
                // ocurrió en el pasado. Se simula el ciclo ya finalizado sin invocar
                // iniciarEstancia(), porque ese método aplica la regla real de check-in
                // y rechaza correctamente las reservas cuya fecha de entrada venció.
                apartamento.cambiarEstado(EstadoApartamento.OCUPADO);
                reserva.iniciar();
                sistema.finalizarEstancia(reserva);

                LocalDateTime inicioPreparacion = LocalDateTime.now()
                        .plusSeconds(2);

                sistema.iniciarSiguientePreparacion(inicioPreparacion);

                int minutos = 32 + (i % 6) * 9;

                sistema.finalizarPreparacionActual(
                        inicioPreparacion.plusMinutes(minutos)
                );
            }
        }
    }

    // =========================================================
    // RESERVAS ACTUALES
    // =========================================================

    /**
     * Cuatro huéspedes alojados actualmente, cuatro reservas
     * confirmadas y cuatro pendientes de anticipo.
     */
    private static void cargarReservasActuales(
            ApartaSuites sistema,
            Huesped[] huespedes
    ) throws ExcepcionSGA {

        LocalDate hoy = LocalDate.now();

        for (int i = 0; i < 12; i++) {

            Apartamento apartamento = sistema.buscarApartamento(
                    codigoApartamento(i)
            );

            CanalReserva canal = i >= 8
                    ? CanalReserva.PORTAL
                    : ((i % 3 == 0)
                    ? CanalReserva.PORTAL
                    : CanalReserva.DIRECTA);

            // Las primeras cuatro reservas representan huéspedes que hacen
            // check-in hoy. No deben tener una entrada de ayer: la regla de
            // negocio rechaza correctamente cualquier check-in después del
            // día inicial y las marcaría como NO_SHOW al cargar la aplicación.
            // Las siguientes cuatro quedan confirmadas para mañana y las
            // últimas cuatro siguen pendientes de anticipo para mañana.
            LocalDate entrada = i < 4 ? hoy : hoy.plusDays(1);

            Reserva reserva = crearReserva(
                    sistema,
                    huespedes[(i + 8) % huespedes.length],
                    apartamento,
                    entrada,
                    entrada.plusDays(2 + (i % 2)),
                    canal,
                    LocalDateTime.now().minusHours(3).plusMinutes(i * 7L),
                    i % 4 == 0
            );

            if (i < 4) {

                if (reserva.getEstado() == EstadoReserva.PENDIENTE) {
                    pagarPortalParaConfirmar(sistema, reserva);
                } else {
                    pagarSiCorresponde(sistema, reserva);
                }

                sistema.iniciarEstancia(reserva);

                sistema.registrarCargo(
                        reserva,
                        "Consumo de minibar",
                        BigDecimal.valueOf(18000 + i * 2500),
                        LocalDateTime.now().minusMinutes(25)
                );

            } else if (i < 8) {

                pagarSiCorresponde(sistema, reserva);

            }

            // i >= 8 conserva la reserva del Portal
            // pendiente de anticipo.
        }
    }

    // =========================================================
    // RESERVAS PRÓXIMAS
    // =========================================================

    /**
     * Doce reservas de los siguientes días:
     * confirmadas y pendientes de anticipo.
     */
    private static void cargarReservasProximas(
            ApartaSuites sistema,
            Huesped[] huespedes
    ) throws ExcepcionSGA {

        LocalDate hoy = LocalDate.now();

        for (int i = 0; i < 12; i++) {

            Apartamento apartamento = sistema.buscarApartamento(
                    codigoApartamento(i)
            );

            CanalReserva canal = (i % 2 == 0)
                    ? CanalReserva.PORTAL
                    : CanalReserva.DIRECTA;

            Reserva reserva = crearReserva(
                    sistema,
                    huespedes[(i + 15) % huespedes.length],
                    apartamento,
                    hoy.plusDays(5),
                    hoy.plusDays(8 + (i % 3)),
                    canal,
                    LocalDateTime.now().minusHours(1).plusMinutes(i * 3L),
                    i % 4 == 1
            );

            if (i % 4 != 0) {
                pagarSiCorresponde(sistema, reserva);
            }
        }
    }

    // =========================================================
    // SOLICITUDES EXTERNAS
    // =========================================================

    private static void cargarSolicitudesExternas(
            ApartaSuites sistema,
            Huesped[] huespedes
    ) throws ExcepcionSGA {

        LocalDate hoy = LocalDate.now();

        // C-102 está preparado y no tiene reservas
        // en los periodos anteriores.
        Apartamento apartamento = sistema.buscarApartamento("C-102");

        Estancia estancia = new Estancia(
                hoy.plusDays(20),
                hoy.plusDays(23)
        );

        sistema.recibirSolicitudExterna(
                huespedes[20],
                apartamento,
                estancia,
                ocupantes(huespedes[20], 1),
                LocalDateTime.now().minusMinutes(20)
        );

        sistema.recibirSolicitudExterna(
                huespedes[21],
                apartamento,
                estancia,
                ocupantes(huespedes[21], 1),
                LocalDateTime.now().minusMinutes(15)
        );

        sistema.procesarSolicitudesExternas();
    }

    // =========================================================
    // CREACIÓN DE RESERVAS
    // =========================================================

    private static Reserva crearReserva(
            ApartaSuites sistema,
            Huesped titular,
            Apartamento apartamento,
            LocalDate entrada,
            LocalDate salida,
            CanalReserva canal,
            LocalDateTime creadaEn,
            boolean dosOcupantes
    ) throws ExcepcionSGA {

        return sistema.crearReserva(
                titular,
                apartamento,
                new Estancia(entrada, salida),
                ocupantes(titular, dosOcupantes ? 2 : 1),
                canal,
                creadaEn
        );
    }

    // =========================================================
    // OCUPANTES
    // =========================================================

    private static ListaDoblementeEnlazada<Ocupante> ocupantes(
            Huesped titular,
            int cantidad
    ) {

        ListaDoblementeEnlazada<Ocupante> lista =
                new ListaDoblementeEnlazada<>();

        lista.agregar(
                new Ocupante(
                        titular.getNombre(),
                        titular.getFechaNacimiento()
                )
        );

        if (cantidad > 1) {
            lista.agregar(
                    new Ocupante(
                            "Acompañante de " + titular.getNombre(),
                            LocalDate.of(1995, 6, 12)
                    )
            );
        }

        return lista;
    }

    // =========================================================
    // PAGOS
    // =========================================================

    private static void pagarSiCorresponde(
            ApartaSuites sistema,
            Reserva reserva
    ) throws ExcepcionSGA {

        if (reserva.getCanal() == CanalReserva.PORTAL
                && reserva.getEstado() == EstadoReserva.PENDIENTE) {

            pagarPortalParaConfirmar(sistema, reserva);

        } else if (reserva.getCanal() == CanalReserva.DIRECTA) {

            double total = sistema.cotizarReserva(reserva).getTotal();

            sistema.registrarPago(
                    reserva,
                    "Pago de alojamiento en recepción",
                    BigDecimal.valueOf(total),
                    LocalDateTime.now().minusMinutes(10)
            );
        }
    }

    private static void pagarPortalParaConfirmar(
            ApartaSuites sistema,
            Reserva reserva
    ) throws ExcepcionSGA {

        double total = sistema.cotizarReserva(reserva).getTotal();

        sistema.registrarPagoPortal(
                reserva,
                Math.ceil(total * 0.35),
                LocalDateTime.now().minusMinutes(5)
        );
    }

    /**
     * Paga el saldo restante de una reserva histórica
     * antes de finalizar su estancia.
     */
    private static void pagarSaldoPendiente(
            ApartaSuites sistema,
            Reserva reserva
    ) throws ExcepcionSGA {

        BigDecimal saldoPendiente = reserva.getFolio().calcularSaldo();

        if (saldoPendiente.compareTo(BigDecimal.ZERO) > 0) {

            sistema.registrarPago(
                    reserva,
                    "Pago de saldo restante de alojamiento",
                    saldoPendiente,
                    LocalDateTime.now().minusMinutes(2)
            );
        }
    }

    // =========================================================
    // CÓDIGOS DE APARTAMENTOS
    // =========================================================

    private static String codigoApartamento(int indice) {

        String[] codigos = {
                "A-101", "A-102", "A-103",
                "A-201", "A-202", "A-203",
                "B-101", "B-102", "B-103",
                "B-201", "B-202", "C-101"
        };

        return codigos[indice];
    }
}
