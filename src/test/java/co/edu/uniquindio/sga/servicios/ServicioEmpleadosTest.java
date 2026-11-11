package co.edu.uniquindio.sga.servicios;

import co.edu.uniquindio.sga.personas.Administrador;
import co.edu.uniquindio.sga.personas.Empleado;
import co.edu.uniquindio.sga.personas.Recepcionista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ServicioEmpleadosTest {

    private final LocalDate nacimiento = LocalDate.of(1995, 5, 5);
    private ServicioEmpleados servicio;

    @BeforeEach
    void preparar() { servicio = new ServicioEmpleados(); }

    private Recepcionista recepcionista(String usuario) {
        return new Recepcionista("Marta", nacimiento, usuario, "clave1", null);
    }

    @Test
    void contrataYBuscaPorUsuario() {
        Empleado e = servicio.contratar(recepcionista("marta"));
        assertSame(e, servicio.buscarPorUsuario("marta").orElseThrow());
        assertTrue(servicio.buscarPorUsuario("nadie").isEmpty());
    }

    @Test
    void elUsuarioEsUnico() {
        servicio.contratar(recepcionista("marta"));
        assertThrows(IllegalArgumentException.class, () -> servicio.contratar(recepcionista("marta")));
    }

    @Test
    void desvincularConservaElRegistroPeroLoDejaInactivo() {
        servicio.contratar(recepcionista("marta"));
        servicio.contratar(recepcionista("pedro"));
        servicio.desvincular("marta");
        assertEquals(2, servicio.listar().size());
        assertEquals(1, servicio.listarActivos().size());
        assertThrows(IllegalArgumentException.class, () -> servicio.desvincular("nadie"));
    }

    @Test
    void listaSoloLosRecepcionistas() {
        servicio.contratar(recepcionista("marta"));
        servicio.contratar(new Administrador("Root", nacimiento, "root", "clave", new ServicioTarifas()));
        assertEquals(2, servicio.listar().size());
        assertEquals(1, servicio.listarRecepcionistas().size());
    }
}
