package co.edu.uniquindio.sga.personas;

import co.edu.uniquindio.sga.servicios.ServicioTarifas;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EmpleadoTest {

    private final LocalDate nacimiento = LocalDate.of(1995, 5, 5);

    private Recepcionista recepcionista() {
        return new Recepcionista("Marta", nacimiento, "marta", "clave1", null);
    }

    @Test
    void cadaRolSeIdentificaASiMismo() {
        assertEquals("RECEPCIONISTA", recepcionista().getRol());
        assertEquals("ADMINISTRADOR", new Administrador("Root", nacimiento, "root", "clave", new ServicioTarifas()).getRol());
    }

    @Test
    void autenticaConCredencialesCorrectas() {
        Empleado e = recepcionista();
        assertTrue(e.autenticar("marta", "clave1"));
        assertFalse(e.autenticar("marta", "otra"));
        assertFalse(e.autenticar("pedro", "clave1"));
    }

    @Test
    void elEmpleadoDesvinculadoNoPuedeEntrar() {
        Empleado e = recepcionista();
        assertTrue(e.isActivo());
        e.desvincular();
        assertFalse(e.isActivo());
        assertFalse(e.autenticar("marta", "clave1"));
    }

    @Test
    void usuarioYClaveSonObligatorios() {
        assertThrows(IllegalArgumentException.class, () -> new Recepcionista("M", nacimiento, " ", "x", null));
        assertThrows(IllegalArgumentException.class, () -> new Recepcionista("M", nacimiento, "m", null, null));
    }

    @Test
    void cambiaLaClave() {
        Empleado e = recepcionista();
        e.cambiarClave("nueva");
        assertFalse(e.autenticar("marta", "clave1"));
        assertTrue(e.autenticar("marta", "nueva"));
        assertThrows(IllegalArgumentException.class, () -> e.cambiarClave(" "));
    }
}
