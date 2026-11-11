package co.edu.uniquindio.sga.cancelacion;

import co.edu.uniquindio.sga.Fixtures;
import co.edu.uniquindio.sga.reservas.EstadoReserva;
import co.edu.uniquindio.sga.reservas.Reserva;
import co.edu.uniquindio.sga.reservas.ReservaDirecta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PoliticaCancelacionEstandarTest {

    private final PoliticaCancelacionEstandar politica = new PoliticaCancelacionEstandar();
    private Reserva reserva;

    @BeforeEach
    void preparar() {
        reserva = new ReservaDirecta("R", Fixtures.huesped(), Fixtures.apto(), Fixtures.adultos(2), Fixtures.ENTRADA, Fixtures.SALIDA, 1_000_000, politica, Fixtures.AHORA);
    }

    private long retencion(int mes, int dia, int hora) {
        return politica.calcularRetencion(reserva, LocalDateTime.of(2026, mes, dia, hora, 0));
    }

    @Test
    void masDeSieteDiasSeDevuelveTodo() {
        assertEquals(0, retencion(11, 5, 9));
        assertEquals(0, retencion(12, 13, 14)); // 7 días y 1 hora antes
    }

    @Test
    void exactamenteSieteDiasYaRetieneElCincuentaPorCiento() {
        assertEquals(500_000, retencion(12, 13, 15));
    }

    @Test
    void entreDosYSieteDiasRetieneElCincuentaPorCiento() {
        assertEquals(500_000, retencion(12, 15, 15));
        assertEquals(500_000, retencion(12, 18, 15)); // exactamente 48 horas
    }

    @Test
    void menosDe48HorasRetieneTodo() {
        assertEquals(1_000_000, retencion(12, 18, 16)); // 47 horas
        assertEquals(1_000_000, retencion(12, 20, 10));
    }

    @Test
    void despuesDeLaFechaDeEntradaRetieneTodo() {
        assertEquals(1_000_000, retencion(12, 21, 9));
    }

    @Test
    void elNoShowRetieneElCienPorCientoSinImportarLaFecha() {
        reserva.cambiarEstado(EstadoReserva.CONFIRMADA);
        reserva.cambiarEstado(EstadoReserva.NO_SHOW);
        assertEquals(1_000_000, retencion(11, 5, 9));
    }

    @Test
    void sePuedenConfigurarLosUmbrales() {
        PoliticaCancelacionEstandar estricta = new PoliticaCancelacionEstandar(14, 72);
        assertEquals(500_000, estricta.calcularRetencion(reserva, LocalDateTime.of(2026, 12, 10, 15, 0)));
        assertEquals(1_000_000, estricta.calcularRetencion(reserva, LocalDateTime.of(2026, 12, 18, 0, 0)));
    }
}
