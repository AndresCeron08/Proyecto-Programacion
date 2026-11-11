package co.edu.uniquindio.sga.reservas;

import co.edu.uniquindio.sga.Fixtures;
import co.edu.uniquindio.sga.cancelacion.IPoliticaCancelacion;
import co.edu.uniquindio.sga.cancelacion.PoliticaCancelacionEstandar;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CanalesReservaTest {

    private final IPoliticaCancelacion politica = new PoliticaCancelacionEstandar();

    private ReservaPortal portal() {
        return new ReservaPortal("P", Fixtures.huesped(), Fixtures.apto(), Fixtures.adultos(2), Fixtures.ENTRADA, Fixtures.SALIDA, 1_000_000, politica, Fixtures.AHORA);
    }

    private ReservaDirecta directa() {
        return new ReservaDirecta("D", Fixtures.huesped(), Fixtures.apto(), Fixtures.adultos(2), Fixtures.ENTRADA, Fixtures.SALIDA, 1_000_000, politica, Fixtures.AHORA);
    }

    private ReservaExterna externa(String codigoExterno) {
        return new ReservaExterna("E", codigoExterno, Fixtures.huesped(), Fixtures.apto(), Fixtures.adultos(2), Fixtures.ENTRADA, Fixtures.SALIDA, 1_000_000, politica, Fixtures.AHORA);
    }

    @Test
    void portalExigeAnticipoDel30PorCientoYNoPagaComision() {
        ReservaPortal r = portal();
        assertTrue(r.requiereAnticipo());
        assertEquals(300_000, r.anticipoRequerido());
        assertEquals(0, r.calcularComision());
        assertEquals("PORTAL", r.getCanal());
    }

    @Test
    void directaNoExigeAnticipoNiPagaComision() {
        ReservaDirecta r = directa();
        assertFalse(r.requiereAnticipo());
        assertEquals(0, r.anticipoRequerido());
        assertEquals(0, r.calcularComision());
        assertEquals("DIRECTA", r.getCanal());
    }

    @Test
    void externaPagaComisionDel15PorCientoYGuardaSuCodigo() {
        ReservaExterna r = externa("BKG-123");
        assertFalse(r.requiereAnticipo());
        assertEquals(150_000, r.calcularComision());
        assertEquals("BKG-123", r.getCodigoExterno());
        assertEquals("EXTERNA", r.getCanal());
    }

    @Test
    void externaExigeCodigoExterno() {
        assertThrows(IllegalArgumentException.class, () -> externa(" "));
        assertThrows(IllegalArgumentException.class, () -> externa(null));
    }

    @Test
    void tratamientoPolimorficoDesdeLaClaseBase() {
        Reserva[] reservas = {portal(), directa(), externa("X")};
        long totalComisiones = 0;
        for (Reserva r : reservas) totalComisiones += r.calcularComision();
        assertEquals(150_000, totalComisiones);
    }
}
