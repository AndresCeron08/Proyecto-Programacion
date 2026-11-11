package co.edu.uniquindio.sga.servicios;

import co.edu.uniquindio.sga.Fixtures;
import co.edu.uniquindio.sga.SGA;
import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.folio.MedioPago;
import co.edu.uniquindio.sga.personas.Huesped;
import co.edu.uniquindio.sga.reservas.EstadoReserva;
import co.edu.uniquindio.sga.reservas.Reserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class ServicioReportesTest {

    private SGA sga;
    private Apartamento a101;
    private Apartamento a102;
    private Reserva r1, r2, r3, r4;

    private static LocalDate dic(int dia) { return LocalDate.of(2026, 12, dia); }

    @BeforeEach
    void preparar() {
        sga = new SGA(Fixtures.reloj(Fixtures.AHORA));
        a101 = sga.apartamentos().agregar(Fixtures.apto("A-101", 4));
        a102 = sga.apartamentos().agregar(Fixtures.apto("A-102", 4));
        Huesped ana = sga.huespedes().registrar(Fixtures.huesped());
        ServicioReservas rs = sga.reservas();

        r1 = rs.crearReservaPortal(ana, a101, Fixtures.adultos(2), dic(20), dic(23));
        rs.registrarPago(r1, MedioPago.TRANSFERENCIA, r1.anticipoRequerido());
        rs.confirmar(r1);
        r2 = rs.crearReservaDirecta(ana, a101, Fixtures.adultos(2), dic(25), dic(27));
        rs.confirmar(r2);
        r3 = rs.recibirReservaExterna("BKG-1", ana, a102, Fixtures.adultos(2), dic(20), dic(22));
        rs.confirmar(r3);
        r4 = rs.crearReservaDirecta(ana, a102, Fixtures.adultos(2), dic(22), dic(24));
        rs.cancelar(r4);
    }

    @Test
    void cuentaLasNochesOcupadasSinContarCanceladas() {
        assertEquals(5, sga.reportes().nochesOcupadas(a101, dic(1), LocalDate.of(2027, 1, 1)));
        assertEquals(2, sga.reportes().nochesOcupadas(a102, dic(1), LocalDate.of(2027, 1, 1)));
    }

    @Test
    void recortaLasEstanciasAlPeriodoConsultado() {
        assertEquals(3, sga.reportes().nochesOcupadas(a101, dic(21), dic(26)));
    }

    @Test
    void calculaLaOcupacionPorApartamentoEnPorcentaje() {
        Map<Apartamento, Double> ocupacion = sga.reportes().ocupacionPorApartamento(dic(1), LocalDate.of(2027, 1, 1));
        assertEquals(5 * 100.0 / 31, ocupacion.get(a101), 0.0001);
        assertEquals(2 * 100.0 / 31, ocupacion.get(a102), 0.0001);
    }

    @Test
    void laOcupacionIgnoraApartamentosInactivos() {
        sga.apartamentos().eliminar("A-102");
        Map<Apartamento, Double> ocupacion = sga.reportes().ocupacionPorApartamento(dic(1), LocalDate.of(2027, 1, 1));
        assertEquals(1, ocupacion.size());
        assertTrue(ocupacion.containsKey(a101));
    }

    @Test
    void ingresosPorPeriodoExcluyenCanceladasYRestanComisiones() {
        assertEquals(1_400_000, sga.reportes().ingresosPorPeriodo(dic(1), dic(31)));
        assertEquals(60_000, sga.reportes().comisionesPorPeriodo(dic(1), dic(31)));
        assertEquals(1_340_000, sga.reportes().ingresosNetosPorPeriodo(dic(1), dic(31)));
        assertEquals(400_000, sga.reportes().ingresosPorPeriodo(dic(24), dic(31))); // solo r2 entra en ese rango
    }

    @Test
    void cuentaLasReservasPorCanalYEstadoIncluyendoCanceladas() {
        Map<String, Map<EstadoReserva, Long>> r = sga.reportes().reservasPorCanal();
        assertEquals(1L, r.get("PORTAL").get(EstadoReserva.CONFIRMADA));
        assertEquals(1L, r.get("DIRECTA").get(EstadoReserva.CONFIRMADA));
        assertEquals(1L, r.get("DIRECTA").get(EstadoReserva.CANCELADA));
        assertEquals(1L, r.get("EXTERNA").get(EstadoReserva.CONFIRMADA));
        assertEquals(0L, r.get("EXTERNA").get(EstadoReserva.NO_SHOW));
    }

    @Test
    void listaLosSaldosPendientesDeMayorAMenor() {
        List<Reserva> pendientes = sga.reportes().saldosPendientes();
        assertEquals(3, pendientes.size());
        assertSame(r1, pendientes.get(0));
        assertEquals(420_000, pendientes.get(0).getFolio().calcularSaldo());
        assertFalse(pendientes.contains(r4));
    }

    @Test
    void unaReservaTotalmentePagadaNoTieneSaldoPendiente() {
        sga.reservas().registrarPago(r1, MedioPago.EFECTIVO, 420_000);
        assertFalse(sga.reportes().saldosPendientes().contains(r1));
    }

    @Test
    void ordenaLosApartamentosPorCantidadDeNovedades() {
        a102.registrarNovedad("Vidrio roto");
        a102.registrarNovedad("Falta una toalla");
        a101.registrarNovedad("Bombillo dañado");
        List<Apartamento> top = sga.reportes().apartamentosConMasNovedades(5);
        assertEquals(List.of(a102, a101), top);
        assertEquals(List.of(a102), sga.reportes().apartamentosConMasNovedades(1));
    }

    @Test
    void sinNovedadesElReporteVieneVacio() {
        assertTrue(sga.reportes().apartamentosConMasNovedades(3).isEmpty());
    }

    @Test
    void rechazaPeriodosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> sga.reportes().ocupacionPorApartamento(dic(10), dic(10)));
        assertThrows(IllegalArgumentException.class, () -> sga.reportes().nochesOcupadas(a101, dic(10), dic(5)));
        assertThrows(IllegalArgumentException.class, () -> sga.reportes().ingresosPorPeriodo(dic(10), dic(5)));
    }
}
