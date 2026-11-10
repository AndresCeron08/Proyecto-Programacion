package co.edu.uniquindio.sga;

import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.alojamiento.EstadoApartamento;
import co.edu.uniquindio.sga.alojamiento.Temporada;
import co.edu.uniquindio.sga.alojamiento.TipoTemporada;
import co.edu.uniquindio.sga.cancelacion.PoliticaCancelacionEstandar;
import co.edu.uniquindio.sga.folio.MedioPago;
import co.edu.uniquindio.sga.personas.Administrador;
import co.edu.uniquindio.sga.personas.Huesped;
import co.edu.uniquindio.sga.personas.IAutenticable;
import co.edu.uniquindio.sga.personas.Ocupante;
import co.edu.uniquindio.sga.personas.Recepcionista;
import co.edu.uniquindio.sga.reservas.EstadoReserva;
import co.edu.uniquindio.sga.reservas.Reserva;
import co.edu.uniquindio.sga.servicios.ServicioAutenticacion;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

public class MenuConsola {

    private static final NumberFormat PESOS = NumberFormat.getInstance(Locale.forLanguageTag("es-CO"));

    private final SGA sga;
    private final Scanner in = new Scanner(System.in);

    public MenuConsola(SGA sga) { this.sga = sga; }

    public void iniciar() {
        System.out.println("SGA - Aparta-Suites UQ");
        try {
            if (preguntarSiNo("¿Cargar datos de ejemplo (apartamentos, tarifas, un huésped y una recepcionista)?")) {
                cargarDatosEjemplo();
            }
            boolean salir = false;
            while (!salir) {
                sga.reservas().vencerPendientes();
                System.out.println("\n--- INICIO ---");
                System.out.println("1. Iniciar sesión");
                System.out.println("2. Registrarme como huésped");
                System.out.println("0. Salir");
                switch (leerEntero("Opción: ")) {
                    case 1 -> ejecutar(this::iniciarSesion);
                    case 2 -> ejecutar(this::registrarHuesped);
                    case 0 -> salir = true;
                    default -> System.out.println("Opción inválida.");
                }
            }
        } catch (NoSuchElementException finDeEntrada) {
            System.out.println("\n(Entrada finalizada)");
        }
        System.out.println("Hasta pronto.");
    }

    private void iniciarSesion() {
        String usuario = leerTexto("Usuario: ");
        String clave = leerTexto("Clave: ");
        Optional<IAutenticable> sesion = sga.autenticacion().iniciarSesion(usuario, clave);
        if (sesion.isEmpty()) {
            System.out.println("Usuario o clave incorrectos.");
            return;
        }
        IAutenticable quien = sesion.get();
        System.out.println("Bienvenido. Rol: " + ServicioAutenticacion.rolDe(quien));
        if (quien instanceof Huesped h) menuHuesped(h);
        else if (quien instanceof Recepcionista r) menuRecepcion(r);
        else if (quien instanceof Administrador a) menuAdmin(a);
    }

    private void registrarHuesped() {
        String nombre = leerTexto("Nombre: ");
        LocalDate nacimiento = leerFecha("Fecha de nacimiento");
        String documento = leerTexto("Documento: ");
        String telefono = leerTexto("Teléfono: ");
        String correo = leerTexto("Correo: ");
        String usuario = leerTexto("Usuario: ");
        String clave = leerTexto("Clave: ");
        sga.huespedes().registrar(new Huesped(nombre, nacimiento, documento, telefono, correo, usuario, clave));
        System.out.println("Huésped registrado. Ya puedes iniciar sesión.");
    }

    // ======================= HUÉSPED =======================
    private void menuHuesped(Huesped h) {
        boolean activo = true;
        while (activo) {
            sga.reservas().vencerPendientes();
            System.out.println("\n HUÉSPED: " + h.getNombre() + " ---");
            System.out.println("1. Ver apartamentos disponibles");
            System.out.println("2. Cotizar una estancia");
            System.out.println("3. Crear reserva (canal Portal)");
            System.out.println("4. Pagar anticipo o abono");
            System.out.println("5. Mis reservas");
            System.out.println("6. Cancelar una reserva");
            System.out.println("0. Cerrar sesión");
            switch (leerEntero("Opción: ")) {
                case 1 -> ejecutar(this::verDisponibles);
                case 2 -> ejecutar(() -> cotizar(h));
                case 3 -> ejecutar(() -> crearReservaPortal(h));
                case 4 -> ejecutar(() -> pagarComoHuesped(h));
                case 5 -> ejecutar(() -> mostrarReservas(sga.reservas().reservasDe(h)));
                case 6 -> ejecutar(() -> cancelarComoHuesped(h));
                case 0 -> activo = false;
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    private void verDisponibles() {
        LocalDate entrada = leerFecha("Fecha de entrada");
        LocalDate salida = leerFecha("Fecha de salida");
        int personas = leerEntero("Número de personas (contando menores): ");
        List<Apartamento> libres = sga.reservas().apartamentosDisponibles(entrada, salida, personas);
        if (libres.isEmpty()) {
            System.out.println("No hay apartamentos disponibles para esos datos.");
            return;
        }
        System.out.println("Disponibles:");
        for (Apartamento a : libres) {
            System.out.println("  " + a.getCodigo() + " | " + a.getNombre() + " | " + a.getDormitorios() + " dormitorios | capacidad " + a.getCapacidadMaxima() + " | " + a.getCaracteristicas());
        }
    }

    private void cotizar(Huesped h) {
        Apartamento apto = pedirApartamento();
        LocalDate entrada = leerFecha("Fecha de entrada");
        LocalDate salida = leerFecha("Fecha de salida");
        List<Ocupante> ocupantes = pedirOcupantes(h);
        Map<LocalDate, Long> desglose = sga.tarifas().desglose(apto, entrada, salida, ocupantes);
        long pagantes = ocupantes.stream().filter(o -> o.paga(entrada)).count();
        System.out.println("Ocupantes: " + ocupantes.size() + " | pagan: " + pagantes);
        desglose.forEach((noche, valor) -> System.out.println("  " + noche + "  " + sga.tarifas().temporadaDe(noche) + "  " + pesos(valor)));
        System.out.println("TOTAL: " + pesos(h.cotizar(sga.tarifas(), apto, entrada, salida, ocupantes)));
    }

    private void crearReservaPortal(Huesped h) {
        Apartamento apto = pedirApartamento();
        LocalDate entrada = leerFecha("Fecha de entrada");
        LocalDate salida = leerFecha("Fecha de salida");
        List<Ocupante> ocupantes = pedirOcupantes(h);
        Reserva r = sga.reservas().crearReservaPortal(h, apto, ocupantes, entrada, salida);
        System.out.println(" Reserva creada: " + r.getCodigo() + " (Pendiente)");
        System.out.println("  Valor: " + pesos(r.getValorCongelado()) + " | Anticipo del 30 %: " + pesos(r.anticipoRequerido()));
        System.out.println("  Paga el anticipo en las próximas 24 horas o la reserva se cancelará sola.");
    }

    private void pagarComoHuesped(Huesped h) {
        Reserva r = pedirReserva(sga.reservas().reservasDe(h).stream().filter(Reserva::estaActiva).toList()); pagar(r);
    }

    private void pagar(Reserva r) {
        long saldo = r.getFolio().calcularSaldo();
        System.out.println("Saldo actual: " + pesos(saldo) + " | anticipo requerido: " + pesos(r.anticipoRequerido()));
        long monto = leerLong("Monto a pagar: ");
        if (monto > saldo) throw new IllegalArgumentException("El monto supera el saldo de la reserva");
        MedioPago medio = pedirMedioPago();
        sga.reservas().registrarPago(r, medio, monto);
        System.out.println("Pago registrado. Nuevo saldo: " + pesos(r.getFolio().calcularSaldo()));
        if (r.getEstado() == EstadoReserva.PENDIENTE && r.requiereAnticipo() && r.getFolio().totalPagos() >= r.anticipoRequerido()) {
            sga.reservas().confirmar(r);
            System.out.println("Anticipo completo: la reserva quedó CONFIRMADA.");
        }
    }

    private void cancelarComoHuesped(Huesped h) {
        Reserva r = pedirReserva(sga.reservas().reservasDe(h).stream().filter(x -> x.getEstado() == EstadoReserva.PENDIENTE || x.getEstado() == EstadoReserva.CONFIRMADA).toList());
        long devolucion = h.consultarDevolucion(sga.reservas(), r);
        System.out.println("Si cancelas ahora se te devolverían: " + pesos(devolucion));
        if (preguntarSiNo("¿Confirmas la cancelación?")) {
            long devuelto = h.cancelarReserva(sga.reservas(), r);
            System.out.println("Reserva cancelada. Devolución registrada: " + pesos(devuelto));
        }
    }

    //RECEPCIONISTA
    private void menuRecepcion(Recepcionista rec) {
        boolean activo = true;
        while (activo) {
            sga.reservas().vencerPendientes();
            System.out.println("\n RECEPCIÓN: " + rec.getNombre() + " ---");
            System.out.println("1. Crear reserva directa");
            System.out.println("2. Recibir reserva externa");
            System.out.println("3. Confirmar reserva pendiente");
            System.out.println("4. Check-in");
            System.out.println("5. Check-out");
            System.out.println("6. Registrar pago");
            System.out.println("7. Registrar cargo por servicio adicional");
            System.out.println("8. Cambiar estado de un apartamento");
            System.out.println("9. Registrar novedad");
            System.out.println("10. Declarar no-show");
            System.out.println("11. Ver todas las reservas");
            System.out.println("12. Ver detalle del folio de una reserva");
            System.out.println("0. Cerrar sesión");
            switch (leerEntero("Opción: ")) {
                case 1 -> ejecutar(() -> reservaDirecta(rec));
                case 2 -> ejecutar(() -> reservaExterna(rec));
                case 3 -> ejecutar(() -> rec.confirmar(pedirReserva(reservasEn(EstadoReserva.PENDIENTE))));
                case 4 -> ejecutar(() -> rec.checkIn(pedirReserva(reservasEn(EstadoReserva.CONFIRMADA))));
                case 5 -> ejecutar(() -> rec.checkOut(pedirReserva(reservasEn(EstadoReserva.EN_CURSO))));
                case 6 -> ejecutar(() -> pagar(pedirReserva(sga.reservas().getReservas().stream().filter(Reserva::estaActiva).toList())));
                case 7 -> ejecutar(this::cargoServicio);
                case 8 -> ejecutar(() -> cambiarEstadoApartamento(rec));
                case 9 -> ejecutar(() -> rec.registrarNovedad(pedirApartamento(), leerTexto("Descripción: ")));
                case 10 -> ejecutar(() -> rec.declararNoShow(pedirReserva(reservasEn(EstadoReserva.CONFIRMADA))));
                case 11 -> mostrarReservas(sga.reservas().getReservas());
                case 12 -> ejecutar(() -> mostrarFolio(pedirReserva(sga.reservas().getReservas())));
                case 0 -> activo = false;
                default -> System.out.println("Opción inválida.");
            }
            if (activo) System.out.println("(listo)");
        }
    }

    private void reservaDirecta(Recepcionista rec) {
        Huesped titular = pedirHuespedPorDocumento();
        Apartamento apto = pedirApartamento();
        LocalDate entrada = leerFecha("Fecha de entrada");
        LocalDate salida = leerFecha("Fecha de salida");
        Reserva r = rec.registrarDirecta(titular, apto, pedirOcupantes(titular), entrada, salida);
        System.out.println("Reserva directa creada: " + r.getCodigo() + " | valor " + pesos(r.getValorCongelado()));
    }

    private void reservaExterna(Recepcionista rec) {
        String codigoExterno = leerTexto("Código de la plataforma: ");
        Huesped titular = pedirHuespedPorDocumento();
        Apartamento apto = pedirApartamento();
        LocalDate entrada = leerFecha("Fecha de entrada");
        LocalDate salida = leerFecha("Fecha de salida");
        try {
            Reserva r = rec.recibirExterna(codigoExterno, titular, apto, pedirOcupantes(titular), entrada, salida);
            System.out.println("Reserva externa creada: " + r.getCodigo() + " | valor " + pesos(r.getValorCongelado()) + " | comisión a la plataforma: " + pesos(r.calcularComision()));
        } finally {
            if (!sga.reservas().getConflictos().isEmpty()) {
                System.out.println("(Conflictos de canal registrados hasta ahora: " + sga.reservas().getConflictos().size() + ")");
            }
        }
    }

    private void cargoServicio() {
        Reserva r = pedirReserva(sga.reservas().getReservas().stream().filter(Reserva::estaActiva).toList());
        String descripcion = leerTexto("Descripción del servicio (desayuno, lavandería...): ");
        long monto = leerLong("Valor: ");
        sga.reservas().registrarCargoServicio(r, descripcion, monto);
        System.out.println("Cargo registrado. Nuevo saldo: " + pesos(r.getFolio().calcularSaldo()));
    }

    private void cambiarEstadoApartamento(Recepcionista rec) {
        Apartamento apto = pedirApartamento();
        System.out.println("Estado actual: " + apto.getEstado());
        EstadoApartamento[] estados = EstadoApartamento.values();
        for (int i = 0; i < estados.length; i++) System.out.println("  " + (i + 1) + ". " + estados[i]);
        int op = leerEntero("Nuevo estado: ");
        if (op < 1 || op > estados.length) throw new IllegalArgumentException("Estado inválido");
        EstadoApartamento nuevo = estados[op - 1];
        if (nuevo == EstadoApartamento.FUERA_DE_SERVICIO) {
            apto.cambiarEstado(nuevo, leerTexto("Motivo: "));
        } else {
            rec.cambiarEstadoApartamento(apto, nuevo);
        }
        System.out.println("✔ " + apto.getCodigo() + " ahora está " + apto.getEstado());
    }

    //ADMINISTRADOR
    private void menuAdmin(Administrador admin) {
        boolean activo = true;
        while (activo) {
            sga.reservas().vencerPendientes();
            System.out.println("\n ADMINISTRADOR: " + admin.getNombre() + " ---");
            System.out.println("1. Crear apartamento (con sus tarifas)");
            System.out.println("2. Listar apartamentos");
            System.out.println("3. Eliminar apartamento (lógico)");
            System.out.println("4. Definir tarifa de un apartamento");
            System.out.println("5. Definir temporada (Media o Alta)");
            System.out.println("6. Registrar bloqueo");
            System.out.println("7. Contratar recepcionista");
            System.out.println("8. Desvincular empleado");
            System.out.println("9. Configurar política de cancelación");
            System.out.println("10. Reportes");
            System.out.println("0. Cerrar sesión");
            switch (leerEntero("Opción: ")) {
                case 1 -> ejecutar(this::crearApartamento);
                case 2 -> listarApartamentos();
                case 3 -> ejecutar(() -> admin.eliminarApartamento(pedirApartamento()));
                case 4 -> ejecutar(() -> admin.definirTarifa(pedirApartamento(), pedirTipoTemporada(), leerLong("Valor por persona por noche: ")));
                case 5 -> ejecutar(() -> definirTemporada(admin));
                case 6 -> ejecutar(() -> admin.registrarBloqueo(pedirApartamento(), leerFecha("Desde"), leerFecha("Hasta (inclusive)"), leerTexto("Motivo: ")));
                case 7 -> ejecutar(() -> contratar(admin));
                case 8 -> ejecutar(() -> sga.empleados().desvincular(leerTexto("Usuario del empleado: ")));
                case 9 -> ejecutar(() -> configurarPolitica(admin));
                case 10 -> ejecutar(this::menuReportes);
                case 0 -> activo = false;
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    private void crearApartamento() {
        String codigo = leerTexto("Código: ");
        String nombre = leerTexto("Nombre: ");
        int dormitorios = leerEntero("Dormitorios: ");
        int capacidad = leerEntero("Capacidad máxima de personas: ");
        List<String> caracteristicas = Arrays.stream(leerTexto("Características (separadas por coma): ").split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        Apartamento a = sga.apartamentos().crear(codigo, nombre, dormitorios, capacidad, caracteristicas);
        a.definirTarifa(TipoTemporada.BAJA, leerLong("Tarifa por persona por noche - BAJA: "));
        a.definirTarifa(TipoTemporada.MEDIA, leerLong("Tarifa por persona por noche - MEDIA: "));
        a.definirTarifa(TipoTemporada.ALTA, leerLong("Tarifa por persona por noche - ALTA: "));
        System.out.println("Apartamento creado.");
    }

    private void listarApartamentos() {
        if (sga.apartamentos().listar().isEmpty()) {
            System.out.println("No hay apartamentos.");
            return;
        }
        for (Apartamento a : sga.apartamentos().listar()) {
            System.out.println("  " + a.getCodigo() + " | " + a.getNombre() + " | cap " + a.getCapacidadMaxima() + " | " + a.getEstado() + (a.isActivo() ? "" : " | INACTIVO") + (a.getMotivoFueraServicio() != null ? " | motivo: " + a.getMotivoFueraServicio() : "") + " | novedades: " + a.getNovedades().size());
        }
    }

    private void definirTemporada(Administrador admin) {
        System.out.println("1. MEDIA   2. ALTA");
        int op = leerEntero("Tipo: ");
        if (op != 1 && op != 2) throw new IllegalArgumentException("Tipo inválido");
        TipoTemporada tipo = op == 1 ? TipoTemporada.MEDIA : TipoTemporada.ALTA;
        admin.definirTemporada(new Temporada(tipo, leerFecha("Desde"), leerFecha("Hasta (inclusive)")));
        System.out.println("Temporada registrada.");
    }

    private void contratar(Administrador admin) {
        Recepcionista r = admin.contratarRecepcionista(sga.empleados(), sga.reservas(), leerTexto("Nombre: "), leerFecha("Fecha de nacimiento"), leerTexto("Usuario: "), leerTexto("Clave: "));
        System.out.println("✔ Recepcionista contratado: " + r.getNombre());
    }

    private void configurarPolitica(Administrador admin) {
        int dias = leerEntero("Días de antelación para devolución total (ej. 7): ");
        int horas = leerEntero("Horas por debajo de las cuales no hay devolución (ej. 48): ");
        admin.configurarPolitica(sga.reservas(), new PoliticaCancelacionEstandar(dias, horas));
        System.out.println("Política actualizada (aplica solo a reservas nuevas).");
    }

    private void menuReportes() {
        System.out.println("1. Ocupación por apartamento");
        System.out.println("2. Ingresos por periodo");
        System.out.println("3. Reservas por canal");
        System.out.println("4. Saldos pendientes");
        System.out.println("5. Apartamentos con más novedades");
        switch (leerEntero("Reporte: ")) {
            case 1 -> {
                LocalDate desde = leerFecha("Desde");
                LocalDate hasta = leerFecha("Hasta (no incluye ese día)");
                sga.reportes().ocupacionPorApartamento(desde, hasta).forEach((a, pct) ->
                        System.out.printf("  %s | %s | %.1f %%%n", a.getCodigo(), a.getNombre(), pct));
            }
            case 2 -> {
                LocalDate desde = leerFecha("Desde");
                LocalDate hasta = leerFecha("Hasta (inclusive)");
                System.out.println("  Ingresos: " + pesos(sga.reportes().ingresosPorPeriodo(desde, hasta)));
                System.out.println("  Comisiones a plataformas: " + pesos(sga.reportes().comisionesPorPeriodo(desde, hasta)));
                System.out.println("  Ingresos netos: " + pesos(sga.reportes().ingresosNetosPorPeriodo(desde, hasta)));
            }
            case 3 -> sga.reportes().reservasPorCanal().forEach((canal, estados) -> {
                System.out.println("  " + canal + ":");
                estados.forEach((estado, n) -> System.out.println("     " + estado + ": " + n));
            });
            case 4 -> {
                List<Reserva> pendientes = sga.reportes().saldosPendientes();
                if (pendientes.isEmpty()) System.out.println("  No hay saldos pendientes.");
                pendientes.forEach(r -> System.out.println("  " + r.getCodigo() + " | " + r.getTitular().getNombre() + " | saldo " + pesos(r.getFolio().calcularSaldo())));
            }
            case 5 -> {
                List<Apartamento> top = sga.reportes().apartamentosConMasNovedades(5);
                if (top.isEmpty()) System.out.println("  Ningún apartamento tiene novedades.");
                top.forEach(a -> System.out.println("  " + a.getCodigo() + " | " + a.getNovedades().size() + " novedades"));
            }
            default -> System.out.println("Opción inválida.");
        }
    }

    // Ayudas de lectura para que el programa no se cierre automaticamente
    private void ejecutar(Runnable accion) {
        try {
            accion.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("⚠ " + e.getMessage());
        }
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return in.nextLine().trim();
    }

    private int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Escribe un número entero.");
            }
        }
    }

    private long leerLong(String mensaje) {
        while (true) {
            try {
                return Long.parseLong(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Escribe un valor numérico sin puntos ni decimales.");
            }
        }
    }

    private LocalDate leerFecha(String mensaje) {
        while (true) {
            try {
                return LocalDate.parse(leerTexto(mensaje + " (AAAA-MM-DD): "));
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido. Ejemplo: 2030-03-10");
            }
        }
    }

    private boolean preguntarSiNo(String mensaje) {
        String r = leerTexto(mensaje + " (s/n): ").toLowerCase();
        return r.equals("s") || r.equals("si") || r.equals("sí");
    }

    private Apartamento pedirApartamento() {
        String codigo = leerTexto("Código del apartamento: ");
        return sga.apartamentos().buscar(codigo).orElseThrow(() -> new IllegalArgumentException("No existe el apartamento " + codigo));
    }

    private TipoTemporada pedirTipoTemporada() {
        System.out.println("1. BAJA   2. MEDIA   3. ALTA");
        return switch (leerEntero("Temporada: ")) {
            case 1 -> TipoTemporada.BAJA;
            case 2 -> TipoTemporada.MEDIA;
            case 3 -> TipoTemporada.ALTA;
            default -> throw new IllegalArgumentException("Temporada inválida");
        };
    }

    private MedioPago pedirMedioPago() {
        System.out.println("1. Efectivo   2. Tarjeta   3. Transferencia");
        return switch (leerEntero("Medio de pago: ")) {
            case 1 -> MedioPago.EFECTIVO;
            case 2 -> MedioPago.TARJETA;
            case 3 -> MedioPago.TRANSFERENCIA;
            default -> throw new IllegalArgumentException("Medio de pago inválido");
        };
    }

    private List<Ocupante> pedirOcupantes(Huesped titular) {
        List<Ocupante> ocupantes = new ArrayList<>();
        ocupantes.add(new Ocupante(titular.getNombre(), titular.getFechaNacimiento()));
        int acompanantes = leerEntero("¿Cuántos acompañantes además del titular? ");
        for (int i = 1; i <= acompanantes; i++) {
            String nombre = leerTexto("  Acompañante " + i + " - nombre: ");
            LocalDate nacimiento = leerFecha("  Acompañante " + i + " - fecha de nacimiento");
            ocupantes.add(new Ocupante(nombre, nacimiento));
        }
        return ocupantes;
    }

    private Huesped pedirHuespedPorDocumento() {
        String documento = leerTexto("Documento del titular: ");
        Optional<Huesped> existente = sga.huespedes().buscarPorDocumento(documento);
        if (existente.isPresent()) return existente.get();
        System.out.println("No existe un huésped con ese documento. Se registrará ahora.");
        Huesped nuevo = new Huesped(leerTexto("Nombre: "), leerFecha("Fecha de nacimiento"), documento, leerTexto("Teléfono: "), leerTexto("Correo: "), null, null);
        return sga.huespedes().registrar(nuevo);
    }

    private List<Reserva> reservasEn(EstadoReserva estado) {
        return sga.reservas().getReservas().stream().filter(r -> r.getEstado() == estado).toList();
    }

    private Reserva pedirReserva(List<Reserva> opciones) {
        if (opciones.isEmpty()) throw new IllegalStateException("No hay reservas disponibles para esta acción");
        mostrarReservas(opciones);
        String codigo = leerTexto("Código de la reserva: ");
        return opciones.stream().filter(r -> r.getCodigo().equalsIgnoreCase(codigo)).findFirst().orElseThrow(() -> new IllegalArgumentException("Esa reserva no está en la lista"));
    }

    private void mostrarReservas(List<Reserva> reservas) {
        if (reservas.isEmpty()) {
            System.out.println("No hay reservas.");
            return;
        }
        for (Reserva r : reservas) {
            System.out.println("  " + r.getCodigo() + " | " + r.getCanal() + " | " + r.getApartamento().getCodigo() + " | " + r.getEntrada() + " -> " + r.getSalida() + " | " + r.getEstado() + " | valor " + pesos(r.getValorCongelado()) + " | saldo " + pesos(r.getFolio().calcularSaldo()) + " | titular " + r.getTitular().getNombre());
        }
    }

    private void mostrarFolio(Reserva r) {
        System.out.println("Folio de " + r.getCodigo() + ":");
        r.getFolio().getMovimientos().forEach(m -> System.out.println("  " + m.getClass().getSimpleName() + " | " + pesos(m.getMonto()) + " | " + m.getDescripcion()));
        System.out.println("  Cargos: " + pesos(r.getFolio().totalCargos()) + " | Pagos: " + pesos(r.getFolio().totalPagos()) + " | SALDO: " + pesos(r.getFolio().calcularSaldo()));
    }

    private static String pesos(long valor) { return "$" + PESOS.format(valor); }

    // Datos de ejemplo
    private void cargarDatosEjemplo() {
        int anio = LocalDate.now().getYear();
        sga.tarifas().agregarTemporada(new Temporada(TipoTemporada.ALTA, LocalDate.of(anio, 12, 15), LocalDate.of(anio + 1, 1, 15)));
        sga.tarifas().agregarTemporada(new Temporada(TipoTemporada.MEDIA, LocalDate.of(anio + 1, 3, 25), LocalDate.of(anio + 1, 4, 5)));

        String[][] aptos = {
                {"A-101", "Vista a las montañas", "2", "4", "180000", "100000", "130000", "180000"},
                {"A-102", "Rincón del Café", "1", "2", "90000", "70000", "90000", "120000"},
                {"A-201", "Terraza Familiar", "3", "6", "250000", "120000", "160000", "220000"}};
        for (String[] d : aptos) {
            Apartamento a = sga.apartamentos().crear(d[0], d[1], Integer.parseInt(d[2]), Integer.parseInt(d[3]), List.of("balcón", "vista a las montañas"));
            a.definirTarifa(TipoTemporada.BAJA, Long.parseLong(d[5]));
            a.definirTarifa(TipoTemporada.MEDIA, Long.parseLong(d[6]));
            a.definirTarifa(TipoTemporada.ALTA, Long.parseLong(d[7]));
        }
        sga.huespedes().registrar(new Huesped("Ana Gómez", LocalDate.of(1990, 3, 10), "1094000000", "3000000000", "ana@correo.com", "ana", "1234"));
        sga.empleados().contratar(new Recepcionista("Marta Ruiz", LocalDate.of(1995, 5, 5), "marta", "1234", sga.reservas()));

        System.out.println("   Datos cargados. Cuentas para probar:");
        System.out.println("   Administrador: admin / admin123");
        System.out.println("   Recepcionista: marta / 1234");
        System.out.println("   Huésped:       ana / 1234");
        System.out.println("   Apartamentos:  A-101 (cap 4), A-102 (cap 2), A-201 (cap 6)");
        System.out.println("   Temporada ALTA del " + anio + "-12-15 al " + (anio + 1) + "-01-15; MEDIA del " + (anio + 1) + "-03-25 al " + (anio + 1) + "-04-05; el resto es BAJA.");
        System.out.println("   (Para reservar usa fechas futuras, por ejemplo 2030-03-10 a 2030-03-13.)");
    }
}
