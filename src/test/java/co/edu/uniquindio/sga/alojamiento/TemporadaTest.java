package co.edu.uniquindio.sga.alojamiento;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TemporadaTest {

    @Test
    void contieneSusFechasLimiteInclusive() {
        Temporada t = new Temporada(TipoTemporada.ALTA, LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 15));
        assertFalse(t.contiene(LocalDate.of(2026, 12, 14)));
        assertTrue(t.contiene(LocalDate.of(2026, 12, 15)));
        assertTrue(t.contiene(LocalDate.of(2027, 1, 15)));
        assertFalse(t.contiene(LocalDate.of(2027, 1, 16)));
    }

    @Test
    void laTemporadaBajaNoSeDefineConRangos() {
        assertThrows(IllegalArgumentException.class,
                () -> new Temporada(TipoTemporada.BAJA, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)));
        assertThrows(IllegalArgumentException.class,
                () -> new Temporada(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)));
    }

    @Test
    void rechazaRangosInvertidosONulos() {
        assertThrows(IllegalArgumentException.class,
                () -> new Temporada(TipoTemporada.MEDIA, LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> new Temporada(TipoTemporada.MEDIA, null, LocalDate.of(2026, 5, 1)));
    }
}
