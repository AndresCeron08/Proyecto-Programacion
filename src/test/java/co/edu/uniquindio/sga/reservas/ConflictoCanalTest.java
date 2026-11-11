package co.edu.uniquindio.sga.reservas;

import co.edu.uniquindio.sga.Fixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConflictoCanalTest {

    @Test
    void guardaLosDatosDelConflicto() {
        ConflictoCanal c = new ConflictoCanal("BKG-1", Fixtures.AHORA, "Doble venta");
        assertEquals("BKG-1", c.getCodigoExterno());
        assertEquals(Fixtures.AHORA, c.getFecha());
        assertEquals("Doble venta", c.getMotivo());
    }

    @Test
    void horariosDelAlojamiento() {
        assertEquals(11, Horarios.HORA_SALIDA.getHour());
        assertEquals(15, Horarios.HORA_ENTRADA.getHour());
        assertEquals(22, Horarios.LIMITE_NO_SHOW.getHour());
        assertEquals(24, Horarios.HORAS_PLAZO_CONFIRMACION);
        long horasEntreSalidaYEntrada = java.time.Duration.between(Horarios.HORA_SALIDA, Horarios.HORA_ENTRADA).toHours();
        assertTrue(horasEntreSalidaYEntrada >= Horarios.HORAS_MINIMAS_PREPARACION);
    }
}
