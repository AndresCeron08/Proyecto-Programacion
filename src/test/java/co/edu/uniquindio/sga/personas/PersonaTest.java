package co.edu.uniquindio.sga.personas;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PersonaTest {

    private final Ocupante nacidaEl10deMarzo2020 = new Ocupante("Sofía", LocalDate.of(2020, 3, 10));

    @Test
    void edadSeCumpleElDiaDelCumpleanios() {
        assertEquals(5, nacidaEl10deMarzo2020.calcularEdad(LocalDate.of(2026, 3, 9)));
        assertEquals(6, nacidaEl10deMarzo2020.calcularEdad(LocalDate.of(2026, 3, 10)));
    }

    @Test
    void menorDeSeisAniosNoPagaYDeSeisEnAdelanteSi() {
        assertFalse(nacidaEl10deMarzo2020.paga(LocalDate.of(2026, 3, 9)));
        assertTrue(nacidaEl10deMarzo2020.paga(LocalDate.of(2026, 3, 10)));
    }

    @Test
    void rechazaNombreVacioONulo() {
        assertThrows(IllegalArgumentException.class, () -> new Ocupante("  ", LocalDate.of(2000, 1, 1)));
        assertThrows(IllegalArgumentException.class, () -> new Ocupante(null, LocalDate.of(2000, 1, 1)));
    }

    @Test
    void rechazaFechaDeNacimientoNula() {
        assertThrows(IllegalArgumentException.class, () -> new Ocupante("Luis", null));
    }

    @Test
    void noCalculaEdadAntesDelNacimiento() {
        assertThrows(IllegalArgumentException.class,
                () -> nacidaEl10deMarzo2020.calcularEdad(LocalDate.of(2019, 1, 1)));
    }

    @Test
    void elNombreSeGuardaSinEspaciosSobrantes() {
        assertEquals("Luis", new Ocupante("  Luis ", LocalDate.of(2000, 1, 1)).getNombre());
    }
}
