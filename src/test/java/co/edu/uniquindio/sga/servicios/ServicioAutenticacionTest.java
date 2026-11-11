package co.edu.uniquindio.sga.servicios;

import co.edu.uniquindio.sga.Fixtures;
import co.edu.uniquindio.sga.personas.Administrador;
import co.edu.uniquindio.sga.personas.Huesped;
import co.edu.uniquindio.sga.personas.IAutenticable;
import co.edu.uniquindio.sga.personas.Recepcionista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ServicioAutenticacionTest {

    private ServicioHuespedes huespedes;
    private ServicioEmpleados empleados;
    private ServicioAutenticacion autenticacion;
    private Huesped ana;
    private Recepcionista marta;
    private Administrador admin;

    @BeforeEach
    void preparar() {
        huespedes = new ServicioHuespedes();
        empleados = new ServicioEmpleados();
        autenticacion = new ServicioAutenticacion(huespedes, empleados);
        ana = huespedes.registrar(Fixtures.huesped("1", "ana"));
        marta = (Recepcionista) empleados.contratar(
                new Recepcionista("Marta", LocalDate.of(1995, 5, 5), "marta", "clave1", null));
        admin = (Administrador) empleados.contratar(
                new Administrador("Root", LocalDate.of(1980, 1, 1), "root", "clave2", new ServicioTarifas()));
    }

    @Test
    void cadaRolInicaSesionYSeIdentifica() {
        assertSame(ana, autenticacion.iniciarSesion("ana", "1234").orElseThrow());
        assertSame(marta, autenticacion.iniciarSesion("marta", "clave1").orElseThrow());
        assertSame(admin, autenticacion.iniciarSesion("root", "clave2").orElseThrow());
        assertEquals("HUESPED", ServicioAutenticacion.rolDe(ana));
        assertEquals("RECEPCIONISTA", ServicioAutenticacion.rolDe(marta));
        assertEquals("ADMINISTRADOR", ServicioAutenticacion.rolDe(admin));
    }

    @Test
    void rechazaClaveIncorrectaUsuarioInexistenteONulos() {
        assertTrue(autenticacion.iniciarSesion("ana", "mala").isEmpty());
        assertTrue(autenticacion.iniciarSesion("nadie", "1234").isEmpty());
        assertTrue(autenticacion.iniciarSesion(null, "1234").isEmpty());
        assertTrue(autenticacion.iniciarSesion("ana", null).isEmpty());
    }

    @Test
    void elEmpleadoDesvinculadoYaNoEntra() {
        empleados.desvincular("marta");
        Optional<IAutenticable> sesion = autenticacion.iniciarSesion("marta", "clave1");
        assertTrue(sesion.isEmpty());
    }

    @Test
    void rolDeUnTipoDesconocidoFalla() {
        IAutenticable raro = (usuario, clave) -> true;
        assertThrows(IllegalArgumentException.class, () -> ServicioAutenticacion.rolDe(raro));
    }
}
