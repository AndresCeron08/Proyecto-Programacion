package co.edu.uniquindio.sga.servicios;

import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.alojamiento.EstadoApartamento;
import co.edu.uniquindio.sga.cancelacion.IPoliticaCancelacion;
import co.edu.uniquindio.sga.folio.Cargo;
import co.edu.uniquindio.sga.folio.MedioPago;
import co.edu.uniquindio.sga.folio.Pago;
import co.edu.uniquindio.sga.folio.TipoCargo;
import co.edu.uniquindio.sga.personas.Huesped;
import co.edu.uniquindio.sga.personas.Ocupante;
import co.edu.uniquindio.sga.reservas.ConflictoCanal;
import co.edu.uniquindio.sga.reservas.EstadoReserva;
import co.edu.uniquindio.sga.reservas.Horarios;
import co.edu.uniquindio.sga.reservas.Reserva;
import co.edu.uniquindio.sga.reservas.ReservaDirecta;
import co.edu.uniquindio.sga.reservas.ReservaExterna;
import co.edu.uniquindio.sga.reservas.ReservaPortal;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServicioReservas {

    private final Clock reloj;
    private final ServicioApartamentos servicioApartamentos;
    private final ServicioTarifas servicioTarifas;
    private IPoliticaCancelacion politicaVigente;

    private final List<Reserva> reservas = new ArrayList<>();
    private final List<ConflictoCanal> conflictos = new ArrayList<>();
    private final Set<String> codigosExternos = new HashSet<>();
    private final Map<String, Long> devoluciones = new HashMap<>();
    private int consecutivo = 0;

    public ServicioReservas(ServicioApartamentos servicioApartamentos, ServicioTarifas servicioTarifas, IPoliticaCancelacion politicaVigente) {
        this(servicioApartamentos, servicioTarifas, politicaVigente, Clock.systemDefaultZone());
    }

    public ServicioReservas(ServicioApartamentos servicioApartamentos, ServicioTarifas servicioTarifas, IPoliticaCancelacion politicaVigente, Clock reloj) {
        this.servicioApartamentos = servicioApartamentos;
        this.servicioTarifas = servicioTarifas;
        this.politicaVigente = politicaVigente;
        this.reloj = reloj;
    }


    public void configurarPolitica(IPoliticaCancelacion nueva) {
        if (nueva == null) throw new IllegalArgumentException("La política es obligatoria");
        this.politicaVigente = nueva;
    }

    public IPoliticaCancelacion getPoliticaVigente() { return politicaVigente; }

    public List<Reserva> getReservas() { return Collections.unmodifiableList(reservas); }
    public List<ConflictoCanal> getConflictos() { return Collections.unmodifiableList(conflictos); }
    public long getDevolucion(String codigoReserva) { return devoluciones.getOrDefault(codigoReserva, 0L); }

    public List<Apartamento> apartamentosDisponibles(LocalDate entrada, LocalDate salida, int personas) {
        List<Apartamento> libres = new ArrayList<>();
        for (Apartamento a : servicioApartamentos.listarActivos()) {
            if (a.getCapacidadMaxima() >= personas && a.estaDisponible(entrada, salida) && !hayDobleVenta(a, entrada, salida)) {libres.add(a);
            }
        }
        return libres;
    }

    public List<Reserva> reservasDe(Huesped titular) {
        return reservas.stream().filter(r -> r.getTitular() == titular).toList();
    }

    public boolean hayDobleVenta(Apartamento apto, LocalDate entrada, LocalDate salida) {
        return reservas.stream().anyMatch(r -> r.estaActiva() && r.getApartamento() == apto && r.compartenNoches(entrada, salida));
    }

    public ReservaPortal crearReservaPortal(Huesped titular, Apartamento apto, List<Ocupante> ocupantes, LocalDate entrada, LocalDate salida) {
        validarNueva(apto, ocupantes, entrada, salida);
        long valor = servicioTarifas.calcularValor(apto, entrada, salida, ocupantes);
        ReservaPortal r = new ReservaPortal(siguienteCodigo("PRT"), titular, apto, ocupantes, entrada, salida, valor, politicaVigente, ahora());
        reservas.add(r);
        return r;
    }

    public ReservaDirecta crearReservaDirecta(Huesped titular, Apartamento apto, List<Ocupante> ocupantes, LocalDate entrada, LocalDate salida) {
        validarNueva(apto, ocupantes, entrada, salida);
        long valor = servicioTarifas.calcularValor(apto, entrada, salida, ocupantes);
        ReservaDirecta r = new ReservaDirecta(siguienteCodigo("DIR"), titular, apto, ocupantes, entrada, salida, valor, politicaVigente, ahora());
        reservas.add(r);
        return r;
    }

    public ReservaExterna recibirReservaExterna(String codigoExterno, Huesped titular, Apartamento apto, List<Ocupante> ocupantes, LocalDate entrada, LocalDate salida) {
        if (codigoExterno == null || codigoExterno.isBlank()) {
            throw new IllegalArgumentException("El código externo es obligatorio");
        }
        if (codigosExternos.contains(codigoExterno)) {
            throw new IllegalArgumentException("El código externo ya generó una reserva: " + codigoExterno);
        }
        try {
            validarNueva(apto, ocupantes, entrada, salida);
        } catch (IllegalStateException conflicto) {
            conflictos.add(new ConflictoCanal(codigoExterno, ahora(), conflicto.getMessage()));
            throw conflicto;
        }
        long valor = servicioTarifas.calcularValor(apto, entrada, salida, ocupantes);
        ReservaExterna r = new ReservaExterna(siguienteCodigo("EXT"), codigoExterno, titular, apto, ocupantes, entrada, salida, valor, politicaVigente, ahora());
        reservas.add(r);
        codigosExternos.add(codigoExterno);
        return r;
    }


    public void registrarPago(Reserva r, MedioPago medio, long monto) {
        r.getFolio().agregarPago(new Pago(medio, monto, "Pago " + medio));
    }

    public void registrarCargoServicio(Reserva r, String descripcion, long monto) {
        r.getFolio().agregarCargo(new Cargo(TipoCargo.SERVICIO, monto, descripcion));
    }


    public void confirmar(Reserva r) {
        if (r.requiereAnticipo() && r.getFolio().totalPagos() < r.anticipoRequerido()) {
            throw new IllegalStateException("Falta el anticipo de " + r.anticipoRequerido() + " (pagado: " + r.getFolio().totalPagos() + ")");
        }
        r.cambiarEstado(EstadoReserva.CONFIRMADA);
    }

    public void checkIn(Reserva r) {
        if (r.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo se hace check-in de una reserva Confirmada");
        }
        if (hoy().isBefore(r.getEntrada())) {
            throw new IllegalStateException("Aún no llega la fecha de entrada");
        }
        if (r.getApartamento().getEstado() != EstadoApartamento.PREPARADO) {
            throw new IllegalStateException("El apartamento no está Preparado");
        }
        r.cambiarEstado(EstadoReserva.EN_CURSO);
        r.getApartamento().cambiarEstado(EstadoApartamento.OCUPADO);
    }

    public void checkOut(Reserva r) {
        r.cambiarEstado(EstadoReserva.FINALIZADA);
        r.getApartamento().cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION);
    }

    public void declararNoShow(Reserva r) {
        LocalDateTime limite = r.getEntrada().atTime(Horarios.LIMITE_NO_SHOW);
        if (ahora().isBefore(limite)) {
            throw new IllegalStateException("El titular aún puede llegar (límite: " + limite + ")");
        }
        r.cambiarEstado(EstadoReserva.NO_SHOW);
        devoluciones.put(r.getCodigo(), calcularDevolucion(r, ahora()));
    }

    public long calcularDevolucion(Reserva r, LocalDateTime cuando) {
        long retencion = r.getPolitica().calcularRetencion(r, cuando);
        return Math.max(0, r.getFolio().totalPagos() - retencion);
    }

    public long cancelar(Reserva r) {
        long devolucion = calcularDevolucion(r, ahora());
        r.cambiarEstado(EstadoReserva.CANCELADA);
        devoluciones.put(r.getCodigo(), devolucion);
        return devolucion;
    }

    public int vencerPendientes() {
        int vencidas = 0;
        for (Reserva r : reservas) {
            if (r.getEstado() == EstadoReserva.PENDIENTE && !ahora().isBefore(r.getCreadaEn().plusHours(Horarios.HORAS_PLAZO_CONFIRMACION))) {
                r.cambiarEstado(EstadoReserva.CANCELADA);
                vencidas++;
            }
        }
        return vencidas;
    }

    private void validarNueva(Apartamento apto, List<Ocupante> ocupantes, LocalDate entrada, LocalDate salida) {
        if (apto == null || !apto.isActivo()) {
            throw new IllegalArgumentException("Apartamento inexistente o inactivo");
        }
        if (entrada == null || salida == null || !salida.isAfter(entrada)) {
            throw new IllegalArgumentException("La salida debe ser posterior a la entrada (mínimo una noche)");
        }
        if (entrada.isBefore(hoy())) {
            throw new IllegalArgumentException("No se puede reservar con fecha de entrada anterior a hoy");
        }
        if (ocupantes == null || ocupantes.isEmpty()) {
            throw new IllegalArgumentException("Debe registrar al menos un ocupante");
        }
        if (ocupantes.size() > apto.getCapacidadMaxima()) {
            throw new IllegalArgumentException("Los ocupantes (" + ocupantes.size() + ") superan la capacidad máxima (" + apto.getCapacidadMaxima() + ")");
        }
        if (!apto.estaDisponible(entrada, salida)) {
            throw new IllegalStateException("El apartamento tiene un bloqueo en esas fechas");
        }
        if (hayDobleVenta(apto, entrada, salida)) {
            throw new IllegalStateException("Doble venta: ya existe una reserva activa que comparte noches");
        }
    }

    private String siguienteCodigo(String prefijo) { return prefijo + "-" + (++consecutivo); }
    private LocalDateTime ahora() { return LocalDateTime.now(reloj); }
    private LocalDate hoy() { return LocalDate.now(reloj); }
}
