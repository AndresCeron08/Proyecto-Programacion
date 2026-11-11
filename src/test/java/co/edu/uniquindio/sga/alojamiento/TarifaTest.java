package co.edu.uniquindio.sga.alojamiento;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TarifaTest {

    @Test
    void guardaTipoYValor() {
        Tarifa t = new Tarifa(TipoTemporada.MEDIA, 150_000);
        assertEquals(TipoTemporada.MEDIA, t.getTipo());
        assertEquals(150_000, t.getValorPersonaNoche());
    }

    @Test
    void rechazaValoresInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new Tarifa(TipoTemporada.BAJA, 0));
        assertThrows(IllegalArgumentException.class, () -> new Tarifa(TipoTemporada.BAJA, -5));
        assertThrows(IllegalArgumentException.class, () -> new Tarifa(null, 1000));
    }
}
