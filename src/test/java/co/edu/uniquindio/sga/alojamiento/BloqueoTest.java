package co.edu.uniquindio.sga.alojamiento;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BloqueoTest {

    private final Bloqueo bloqueo = new Bloqueo(LocalDate.of(2026, 12, 24), LocalDate.of(2026, 12, 26), "Mantenimiento");

    @Test
    void cubreLasNochesDelRangoInclusive() {
        assertFalse(bloqueo.cubre(LocalDate.of(2026, 12, 23)));
        assertTrue(bloqueo.cubre(LocalDate.of(2026, 12, 24)));
        assertTrue(bloqueo.cubre(LocalDate.of(2026, 12, 26)));
        assertFalse(bloqueo.cubre(LocalDate.of(2026, 12, 27)));
    }

    @Test
    void chocaConEstanciasQueComparteAlgunaNoche() {
        assertFalse(bloqueo.choca(LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 24)));
        assertTrue(bloqueo.choca(LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 25)));
        assertTrue(bloqueo.choca(LocalDate.of(2026, 12, 26), LocalDate.of(2026, 12, 28)));
        assertFalse(bloqueo.choca(LocalDate.of(2026, 12, 27), LocalDate.of(2026, 12, 29)));
    }

    @Test
    void validaRangoYMotivo() {
        assertThrows(IllegalArgumentException.class, () -> new Bloqueo(LocalDate.of(2026, 12, 26), LocalDate.of(2026, 12, 24), "x"));
        assertThrows(IllegalArgumentException.class, () -> new Bloqueo(LocalDate.of(2026, 12, 24), LocalDate.of(2026, 12, 26), " "));
        assertThrows(IllegalArgumentException.class, () -> new Bloqueo(null, LocalDate.of(2026, 12, 26), "x"));
    }
}
