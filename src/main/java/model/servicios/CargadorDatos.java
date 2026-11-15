package model.servicios;

import model.enums.CanalReserva;
import model.enums.Temporada;
import model.estructuras.ArbolBinarioBusqueda;
import model.estructuras.ListaDoblementeEnlazada;
import model.excepciones.ExcepcionSGA;
import model.modelo.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;

public class CargadorDatos {

    private final ArbolBinarioBusqueda<Apartamento> apartamentos;
    private final ArbolBinarioBusqueda<Huesped> huespedes;
    private final GestorReservas gestorReservas;
    private final GestorTemporadas gestorTemporadas;

    public CargadorDatos(ArbolBinarioBusqueda<Apartamento> apartamentos, ArbolBinarioBusqueda<Huesped> huespedes,
                         GestorReservas gestorReservas, GestorTemporadas gestorTemporadas) {
        this.apartamentos = apartamentos;
        this.huespedes = huespedes;
        this.gestorReservas = gestorReservas;
        this.gestorTemporadas = gestorTemporadas;
    }

    // ==================== Archivo plano ====================

    public void cargarDesdeArchivo(String ruta) throws IOException, ExcepcionSGA {
        try (BufferedReader lector = new BufferedReader(new FileReader(ruta))) {
            String linea;
            int numero = 0;
            while ((linea = lector.readLine()) != null) {
                numero++;
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }
                try {
                    procesarLinea(linea);
                } catch (ExcepcionSGA e) {
                    throw new ExcepcionSGA("Linea " + numero + ": " + e.getMessage(), e);
                } catch (RuntimeException e) {
                    throw new ExcepcionSGA("Linea " + numero + " mal formada (" + e.getMessage() + "): " + linea, e);
                }
            }
        }
    }

    private void procesarLinea(String linea) throws ExcepcionSGA {
        String[] c = linea.split(";");
        switch (c[0].trim().toUpperCase()) {
            case "TEMPORADA":
                gestorTemporadas.definirPeriodo(new PeriodoTemporada(
                        LocalDate.parse(c[2].trim()), LocalDate.parse(c[3].trim()),
                        Temporada.valueOf(c[1].trim().toUpperCase())));
                break;
            case "APARTAMENTO":
                Apartamento a = new Apartamento(c[1].trim(), c[2].trim(), c[3].trim(),
                        Integer.parseInt(c[4].trim()), Integer.parseInt(c[5].trim()),
                        Integer.parseInt(c[6].trim()), c.length > 10 ? c[10].trim() : "");
                a.definirTarifa(Temporada.BAJA, Double.parseDouble(c[7].trim()));
                a.definirTarifa(Temporada.MEDIA, Double.parseDouble(c[8].trim()));
                a.definirTarifa(Temporada.ALTA, Double.parseDouble(c[9].trim()));
                registrar(a);
                break;
            case "HUESPED":
                registrar(new Huesped(c[1].trim(), c[2].trim(), LocalDate.parse(c[3].trim()),
                        c[4].trim(), c[5].trim()));
                break;
            case "RESERVA":
                cargarReserva(c);
                break;
            default:
                throw new ExcepcionSGA("Tipo de registro desconocido: " + c[0]);
        }
    }

    private void cargarReserva(String[] c) throws ExcepcionSGA {
        Huesped titular = huespedes.buscar(Huesped.paraBusqueda(c[1].trim()));
        Apartamento apartamento = apartamentos.buscar(Apartamento.paraBusqueda(c[2].trim()));
        if (titular == null) {
            throw new ExcepcionSGA("Huesped inexistente: " + c[1]);
        }
        if (apartamento == null) {
            throw new ExcepcionSGA("Apartamento inexistente: " + c[2]);
        }
        Estancia estancia = new Estancia(LocalDate.parse(c[3].trim()), LocalDate.parse(c[4].trim()));
        CanalReserva canal = CanalReserva.valueOf(c[5].trim().toUpperCase());
        ListaDoblementeEnlazada<Ocupante> ocupantes = new ListaDoblementeEnlazada<>();
        for (String par : c[6].split("\\|")) {
            String[] datos = par.split(":");
            ocupantes.agregar(new Ocupante(datos[0].trim(), LocalDate.parse(datos[1].trim())));
        }
        gestorReservas.crearReserva(titular, apartamento, estancia, ocupantes, canal);
    }

    private void registrar(Apartamento a) throws ExcepcionSGA {
        if (!apartamentos.insertar(a)) {
            throw new ExcepcionSGA("Apartamento repetido: " + a.getCodigo());
        }
    }

    private void registrar(Huesped h) throws ExcepcionSGA {
        if (!huespedes.insertar(h)) {
            throw new ExcepcionSGA("Huesped repetido: " + h.getDocumento());
        }
    }

    // ==================== Escenario de prueba incorporado ====================

    /** Carga temporadas, 8 apartamentos, 5 huespedes y 4 reservas de ejemplo. */
    public void cargarEscenarioPrueba() throws ExcepcionSGA {
        gestorTemporadas.definirPeriodo(new PeriodoTemporada(
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 14), Temporada.MEDIA));
        gestorTemporadas.definirPeriodo(new PeriodoTemporada(
                LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 15), Temporada.ALTA));
        gestorTemporadas.definirPeriodo(new PeriodoTemporada(
                LocalDate.of(2027, 3, 20), LocalDate.of(2027, 4, 10), Temporada.MEDIA));

        apto("A-101", "Cafetal", "A", 1, 1, 2, 60000, 80000, 110000, "Balcon");
        apto("A-102", "Guadual", "A", 1, 2, 4, 85000, 110000, 150000, "Cocina equipada");
        apto("A-201", "Palma de cera", "A", 2, 2, 4, 90000, 115000, 155000, "Vista al valle");
        apto("B-101", "Cocora", "B", 1, 3, 6, 120000, 150000, 200000, "Terraza y parqueadero");
        apto("B-102", "Samaria", "B", 1, 2, 4, 88000, 112000, 152000, "Cocina equipada");
        apto("B-201", "Mirador", "B", 2, 3, 6, 125000, 155000, 210000, "Vista al valle");
        apto("C-101", "Hacienda", "C", 1, 4, 8, 160000, 200000, 270000, "Jacuzzi");
        apto("C-201", "Colibri", "C", 2, 1, 2, 65000, 85000, 115000, "Balcon");

        huesped("1094000001", "Laura Gomez", LocalDate.of(1990, 5, 14), "3101112233", "laura@correo.com");
        huesped("1094000002", "Carlos Ruiz", LocalDate.of(1985, 11, 2), "3104445566", "carlos@correo.com");
        huesped("1094000003", "Marta Lopez", LocalDate.of(1978, 3, 21), "3207778899", "marta@correo.com");
        huesped("1094000004", "Andres Perez", LocalDate.of(1995, 8, 30), "3153334455", "andres@correo.com");
        huesped("1094000005", "Sofia Torres", LocalDate.of(1992, 1, 9), "3186667788", "sofia@correo.com");

        reserva("1094000001", "A-102", LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 14), CanalReserva.PORTAL,
                "Laura Gomez:1990-05-14|Pedro Gomez:2018-02-10|Ana Gomez:2022-06-01");
        reserva("1094000002", "B-101", LocalDate.of(2026, 12, 12), LocalDate.of(2026, 12, 18), CanalReserva.DIRECTA,
                "Carlos Ruiz:1985-11-02|Diana Ruiz:1987-04-17|Tomas Ruiz:2012-09-09|Luisa Ruiz:2015-12-12");
        reserva("1094000003", "C-101", LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 27), CanalReserva.EXTERNA,
                "Marta Lopez:1978-03-21|Jorge Lopez:1976-07-07|Sara Lopez:2005-01-15|Mateo Lopez:2020-10-10");
        reserva("1094000004", "A-101", LocalDate.of(2027, 1, 2), LocalDate.of(2027, 1, 5), CanalReserva.PORTAL,
                "Andres Perez:1995-08-30|Camila Diaz:1996-02-20");
    }

    private void apto(String codigo, String nombre, String bloque, int piso, int dormitorios, int capacidad,
                      double baja, double media, double alta, String caracteristicas) throws ExcepcionSGA {
        Apartamento a = new Apartamento(codigo, nombre, bloque, piso, dormitorios, capacidad, caracteristicas);
        a.definirTarifa(Temporada.BAJA, baja);
        a.definirTarifa(Temporada.MEDIA, media);
        a.definirTarifa(Temporada.ALTA, alta);
        registrar(a);
    }

    private void huesped(String documento, String nombre, LocalDate nacimiento, String telefono, String correo)
            throws ExcepcionSGA {
        registrar(new Huesped(documento, nombre, nacimiento, telefono, correo));
    }

    private void reserva(String documento, String codigoApto, LocalDate entrada, LocalDate salida,
                         CanalReserva canal, String ocupantes) throws ExcepcionSGA {
        cargarReserva(new String[] {"RESERVA", documento, codigoApto, entrada.toString(), salida.toString(),
                canal.name(), ocupantes});
    }
}
