package co.edu.uniquindio.sga.servicios;

import co.edu.uniquindio.sga.Fixtures;
import co.edu.uniquindio.sga.Fixtures.RelojMutable;
import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.alojamiento.Bloqueo;
import co.edu.uniquindio.sga.alojamiento.EstadoApartamento;
import co.edu.uniquindio.sga.cancelacion.IPoliticaCancelacion;
import co.edu.uniquindio.sga.cancelacion.PoliticaCancelacionEstandar;
import co.edu.uniquindio.sga.folio.MedioPago;
import co.edu.uniquindio.sga.personas.Huesped;
import co.edu.uniquindio.sga.personas.Ocupante;
import co.edu.uniquindio.sga.reservas.EstadoReserva;
import co.edu.uniquindio.sga.reservas.Reserva;
import co.edu.uniquindio.sga.reservas.ReservaDirecta;
import co.edu.uniquindio.sga.reservas.ReservaExterna;
import co.edu.uniquindio.sga.reservas.ReservaPortal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicioReservasTest {

    private static final LocalDate E = Fixtures.ENTRADA;
    private static final LocalDate S = Fixtures.SALIDA;

    private RelojMutable reloj;
    private ServicioApartamentos apartamentos;
    private ServicioTarifas tarifas;
    private ServicioReservas servicio;
    private Apartamento apto;
    private Huesped ana;

    @BeforeEach
    void preparar() {
        reloj = new RelojMutable(Fixtures.AHORA);
        apartamentos = new ServicioApartamentos();
        tarifas = new ServicioTarifas();
        servicio = new ServicioReservas(apartamentos, tarifas, new PoliticaCancelacionEstandar(), reloj);
        apto = apartamentos.agregar(Fixtures.apto());
        ana = Fixtures.huesped();
    }

    private static LocalDate dic(int dia) { return LocalDate.of(2026, 12, dia); }

    private ReservaDirecta directaPagada() {
        ReservaDirecta r = servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, S);
        servicio.registrarPago(r, MedioPago.EFECTIVO, 600_000);
        return r;
    }

    @Test
    void reservaPortal() {
        ReservaPortal r = servicio.crearReservaPortal(ana, apto, Fixtures.adultos(2), E, S);
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
        assertEquals(600_000, r.getValorCongelado());
        assertEquals(180_000, r.anticipoRequerido());
        assertEquals(600_000, r.getFolio().calcularSaldo());
        assertTrue(r.getCodigo().startsWith("PRT-"));
    }

    @Test
    void elValorYLaPoliticaSeCongelanAlCrearLaReserva() {
        ReservaPortal r = servicio.crearReservaPortal(ana, apto, Fixtures.adultos(2), E, S);
        apto.definirTarifa(co.edu.uniquindio.sga.alojamiento.TipoTemporada.BAJA, 999_999);
        assertEquals(600_000, r.getValorCongelado());
    }

    @Test
    void laPoliticaNuevaSoloAplicaAReservasNuevas() {
        IPoliticaCancelacion original = servicio.getPoliticaVigente();
        Reserva vieja = servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, S);
        IPoliticaCancelacion sinRetencion = (reserva, cuando) -> 0L;
        servicio.configurarPolitica(sinRetencion);
        Reserva nueva = servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), dic(25), dic(27));
        assertSame(original, vieja.getPolitica());
        assertSame(sinRetencion, nueva.getPolitica());
        assertThrows(IllegalArgumentException.class, () -> servicio.configurarPolitica(null));
    }

    @Test
    void noPermiteFechaDeEntradaAnterior() {
        assertThrows(IllegalArgumentException.class, () -> servicio.crearReservaDirecta(ana, apto,
                Fixtures.adultos(2), LocalDate.of(2026, 11, 4), LocalDate.of(2026, 11, 6)));
        assertDoesNotThrow(() -> servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2),
                LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 6)));
    }

    @Test
    void exigeAlMenosUnaNoche() {
        assertThrows(IllegalArgumentException.class, () -> servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, E));
        assertThrows(IllegalArgumentException.class, () -> servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), S, E));
    }

    @Test
    void losMenoresCuentanParaLaCapacidadMaxima() {
        List<Ocupante> cinco = new ArrayList<>(Fixtures.adultos(3));
        cinco.add(Fixtures.nino("Niño 1"));
        cinco.add(Fixtures.nino("Niño 2"));
        assertThrows(IllegalArgumentException.class, () -> servicio.crearReservaPortal(ana, apto, cinco, E, S));
    }

    @Test
    void noReservaApartamentoInactivoNiBloqueado() {
        apto.agregarBloqueo(new Bloqueo(dic(21), dic(21), "Mantenimiento"));
        assertThrows(IllegalStateException.class, () -> servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, S));
        Apartamento otro = apartamentos.agregar(Fixtures.apto("A-102", 2));
        apartamentos.eliminar("A-102");
        assertThrows(IllegalArgumentException.class, () -> servicio.crearReservaDirecta(ana, otro, Fixtures.adultos(2), E, S));
    }


    @Test
    void impideLaDobleVentaSinImportarElCanal() {
        servicio.crearReservaPortal(ana, apto, Fixtures.adultos(2), E, S);
        assertThrows(IllegalStateException.class, () -> servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), dic(22), dic(25)));
        assertThrows(IllegalStateException.class, () -> servicio.crearReservaPortal(ana, apto, Fixtures.adultos(2), dic(19), dic(21)));
        assertThrows(IllegalStateException.class, () -> servicio.recibirReservaExterna("BKG-1", ana, apto, Fixtures.adultos(2), dic(21), dic(22)));
        assertEquals(1, servicio.getReservas().size());
    }

    @Test
    void permiteEstanciasConsecutivasPorqueLaNocheDeSalidaNoSeOcupa() {
        servicio.crearReservaPortal(ana, apto, Fixtures.adultos(2), E, S);
        assertDoesNotThrow(() -> servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), S, dic(25)));
        assertDoesNotThrow(() -> servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), dic(18), E));
    }

    @Test
    void unaReservaCanceladaLiberaSusNoches() {
        Reserva r = servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, S);
        assertTrue(servicio.hayDobleVenta(apto, E, S));
        servicio.cancelar(r);
        assertFalse(servicio.hayDobleVenta(apto, E, S));
        assertDoesNotThrow(() -> servicio.crearReservaPortal(ana, apto, Fixtures.adultos(2), E, S));
    }

    @Test
    void reservaExternaQueChocaSeRechazaYQuedaComoConflictoSinReemplazarLaExistente() {
        Reserva existente = servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, S);
        assertThrows(IllegalStateException.class, () -> servicio.recibirReservaExterna("BKG-9", ana, apto, Fixtures.adultos(2), dic(21), dic(24)));
        assertEquals(1, servicio.getConflictos().size());
        assertEquals("BKG-9", servicio.getConflictos().get(0).getCodigoExterno());
        assertEquals(1, servicio.getReservas().size());
        assertEquals(EstadoReserva.PENDIENTE, existente.getEstado());
        assertSame(existente, servicio.getReservas().get(0));
    }

    @Test
    void unMismoCodigoExternoNoPuedeCrearDosReservas() {
        servicio.recibirReservaExterna("BKG-1", ana, apto, Fixtures.adultos(2), E, S);
        assertThrows(IllegalArgumentException.class, () -> servicio.recibirReservaExterna("BKG-1", ana, apto, Fixtures.adultos(2), dic(26), dic(28)));
        assertEquals(1, servicio.getReservas().size());
    }

    @Test
    void reservaExternaPagaComisionDelQuincePorCiento() {
        ReservaExterna r = servicio.recibirReservaExterna("BKG-1", ana, apto, Fixtures.adultos(2), E, S);
        assertEquals(90_000, r.calcularComision());
        assertEquals("BKG-1", r.getCodigoExterno());
    }

    @Test
    void codigoExternoVacioSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> servicio.recibirReservaExterna(" ", ana, apto, Fixtures.adultos(2), E, S));
    }


    @Test
    void reservaPortalNoSeConfirmaSinElAnticipoCompleto() {
        ReservaPortal r = servicio.crearReservaPortal(ana, apto, Fixtures.adultos(2), E, S);
        assertThrows(IllegalStateException.class, () -> servicio.confirmar(r));
        servicio.registrarPago(r, MedioPago.TARJETA, 100_000);
        assertThrows(IllegalStateException.class, () -> servicio.confirmar(r));
        servicio.registrarPago(r, MedioPago.TARJETA, 80_000);
        servicio.confirmar(r);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }

    @Test
    void reservaDirectaSeConfirmaSinAnticipo() {
        Reserva r = servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, S);
        servicio.confirmar(r);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
        assertThrows(IllegalStateException.class, () -> servicio.confirmar(r));
    }

    @Test
    void checkInExigeConfirmadaFechaLlegadaYApartamentoPreparado() {
        Reserva r = servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, S);
        assertThrows(IllegalStateException.class, () -> servicio.checkIn(r));
        servicio.confirmar(r);
        assertThrows(IllegalStateException.class, () -> servicio.checkIn(r));
        reloj.fijar(LocalDateTime.of(2026, 12, 20, 15, 30));
        apto.cambiarEstado(EstadoApartamento.EN_PREPARACION);
        assertThrows(IllegalStateException.class, () -> servicio.checkIn(r));
        apto.cambiarEstado(EstadoApartamento.PREPARADO);
        servicio.checkIn(r);
        assertEquals(EstadoReserva.EN_CURSO, r.getEstado());
        assertEquals(EstadoApartamento.OCUPADO, apto.getEstado());
    }

    @Test
    void checkOutFinalizaYDejaElApartamentoPendienteDePreparacion() {
        Reserva r = servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, S);
        servicio.confirmar(r);
        assertThrows(IllegalStateException.class, () -> servicio.checkOut(r));
        reloj.fijar(LocalDateTime.of(2026, 12, 20, 15, 30));
        servicio.checkIn(r);
        servicio.checkOut(r);
        assertEquals(EstadoReserva.FINALIZADA, r.getEstado());
        assertEquals(EstadoApartamento.PENDIENTE_PREPARACION, apto.getEstado());
    }


    @Test
    void noShowSoloDespuesDeLas10PMDelDiaDeEntrada() {
        ReservaPortal r = servicio.crearReservaPortal(ana, apto, Fixtures.adultos(2), E, S);
        servicio.registrarPago(r, MedioPago.TRANSFERENCIA, 180_000);
        servicio.confirmar(r);
        reloj.fijar(LocalDateTime.of(2026, 12, 20, 21, 59));
        assertThrows(IllegalStateException.class, () -> servicio.declararNoShow(r));
        reloj.fijar(LocalDateTime.of(2026, 12, 20, 22, 0));
        servicio.declararNoShow(r);
        assertEquals(EstadoReserva.NO_SHOW, r.getEstado());
        assertEquals(0, servicio.getDevolucion(r.getCodigo())); // se retiene el 100 %
        assertFalse(servicio.hayDobleVenta(apto, E, S));         // libera las noches
    }

    @Test
    void noShowSoloAplicaAReservasConfirmadas() {
        Reserva r = servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, S);
        reloj.fijar(LocalDateTime.of(2026, 12, 21, 0, 0));
        assertThrows(IllegalStateException.class, () -> servicio.declararNoShow(r));
    }

    @Test
    void pendienteSinConfirmarEn24HorasSeCancelaYLiberaLasNoches() {
        Reserva r = servicio.crearReservaPortal(ana, apto, Fixtures.adultos(2), E, S);
        reloj.fijar(Fixtures.AHORA.plusHours(23));
        assertEquals(0, servicio.vencerPendientes());
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
        reloj.fijar(Fixtures.AHORA.plusHours(24));
        assertEquals(1, servicio.vencerPendientes());
        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
        assertDoesNotThrow(() -> servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, S));
    }

    @Test
    void lasReservasConfirmadasNoVencen() {
        Reserva r = servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, S);
        servicio.confirmar(r);
        reloj.fijar(Fixtures.AHORA.plusHours(48));
        assertEquals(0, servicio.vencerPendientes());
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }

    @Test
    void conMasDeSieteDiasSeDevuelveTodoLoPagado() {
        ReservaDirecta r = directaPagada();
        assertEquals(600_000, servicio.calcularDevolucion(r, LocalDateTime.of(2026, 11, 5, 9, 0)));
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado()); // consultar no cancela
        assertEquals(600_000, servicio.cancelar(r));
        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
        assertEquals(600_000, servicio.getDevolucion(r.getCodigo()));
    }

    @Test
    void entreDosYSieteDiasSeRetieneElCincuentaPorCiento() {
        ReservaDirecta r = directaPagada();
        reloj.fijar(LocalDateTime.of(2026, 12, 15, 12, 0));
        assertEquals(300_000, servicio.cancelar(r));
    }

    @Test
    void conMenosDe48HorasNoSeDevuelveNada() {
        ReservaDirecta r = directaPagada();
        reloj.fijar(LocalDateTime.of(2026, 12, 19, 10, 0));
        assertEquals(0, servicio.cancelar(r));
    }

    @Test
    void laDevolucionNuncaSuperaLoPagado() {
        ReservaPortal r = servicio.crearReservaPortal(ana, apto, Fixtures.adultos(2), E, S);
        servicio.registrarPago(r, MedioPago.TARJETA, 180_000);
        reloj.fijar(LocalDateTime.of(2026, 12, 15, 12, 0));
        assertEquals(0, servicio.cancelar(r));
    }

    @Test
    void noSePuedeCancelarUnaReservaEnCursoNiDosVeces() {
        Reserva r = servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, S);
        servicio.confirmar(r);
        reloj.fijar(LocalDateTime.of(2026, 12, 20, 16, 0));
        servicio.checkIn(r);
        assertThrows(IllegalStateException.class, () -> servicio.cancelar(r));

        Reserva otra = servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), dic(26), dic(28));
        servicio.cancelar(otra);
        assertThrows(IllegalStateException.class, () -> servicio.cancelar(otra));
    }


    @Test
    void pagosParcialesYCargosPorServiciosActualizanElSaldo() {
        Reserva r = servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, S);
        servicio.registrarPago(r, MedioPago.EFECTIVO, 100_000);
        servicio.registrarPago(r, MedioPago.TARJETA, 50_000);
        assertEquals(450_000, r.getFolio().calcularSaldo());
        servicio.registrarCargoServicio(r, "Desayuno", 30_000);
        assertEquals(480_000, r.getFolio().calcularSaldo());
    }


    @Test
    void consultaApartamentosDisponiblesPorFechasYPersonas() {
        Apartamento pequeno = apartamentos.agregar(Fixtures.apto("A-102", 2));
        assertEquals(2, servicio.apartamentosDisponibles(E, S, 2).size());
        assertEquals(1, servicio.apartamentosDisponibles(E, S, 3).size());
        servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, S);
        List<Apartamento> libres = servicio.apartamentosDisponibles(E, S, 2);
        assertEquals(1, libres.size());
        assertSame(pequeno, libres.get(0));
        pequeno.agregarBloqueo(new Bloqueo(E, E, "Mantenimiento"));
        assertTrue(servicio.apartamentosDisponibles(E, S, 2).isEmpty());
    }

    @Test
    void consultaLasReservasDeUnHuesped() {
        Huesped luis = Fixtures.huesped("2", "luis");
        servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), E, S);
        servicio.crearReservaDirecta(luis, apto, Fixtures.adultos(2), dic(26), dic(28));
        assertEquals(1, servicio.reservasDe(ana).size());
        assertEquals(1, servicio.reservasDe(luis).size());
    }

    @Test
    void generaCodigosConsecutivosConPrefijoPorCanal() {
        Reserva a = servicio.crearReservaPortal(ana, apto, Fixtures.adultos(2), E, S);
        Reserva b = servicio.crearReservaDirecta(ana, apto, Fixtures.adultos(2), S, dic(25));
        Reserva c = servicio.recibirReservaExterna("X-1", ana, apto, Fixtures.adultos(2), dic(25), dic(27));
        assertEquals("PRT-1", a.getCodigo());
        assertEquals("DIR-2", b.getCodigo());
        assertEquals("EXT-3", c.getCodigo());
    }
}
