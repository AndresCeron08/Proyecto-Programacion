package co.edu.uniquindio.sga;

import co.edu.uniquindio.sga.personas.Administrador;
import co.edu.uniquindio.sga.personas.IAutenticable;
import co.edu.uniquindio.sga.servicios.ServicioAutenticacion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SGATest {

    @Test
    void exponeTodosLosServicios() {
        SGA sga = new SGA(Fixtures.reloj(Fixtures.AHORA));
        assertNotNull(sga.apartamentos());
        assertNotNull(sga.tarifas());
        assertNotNull(sga.huespedes());
        assertNotNull(sga.empleados());
        assertNotNull(sga.reservas());
        assertNotNull(sga.autenticacion());
        assertNotNull(sga.reportes());
    }

    @Test
    void nacConUnAdministradorInicialQuePuedeIniciarSesion() {
        SGA sga = new SGA();
        IAutenticable sesion = sga.autenticacion()
                .iniciarSesion(SGA.ADMIN_USUARIO, SGA.ADMIN_CLAVE_INICIAL).orElseThrow();
        assertTrue(sesion instanceof Administrador);
        assertEquals("ADMINISTRADOR", ServicioAutenticacion.rolDe(sesion));
    }

    @Test
    void sinCredencialesValidasNoSeEntra() {
        SGA sga = new SGA();
        assertTrue(sga.autenticacion().iniciarSesion(SGA.ADMIN_USUARIO, "mala").isEmpty());
    }
}
