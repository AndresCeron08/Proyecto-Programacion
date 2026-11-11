package co.edu.uniquindio.sga.servicios;

import co.edu.uniquindio.sga.Fixtures;
import co.edu.uniquindio.sga.personas.Huesped;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicioHuespedesTest {

    private ServicioHuespedes servicio;

    @BeforeEach
    void preparar() { servicio = new ServicioHuespedes(); }

    @Test
    void registraYBuscaPorDocumentoYUsuario() {
        Huesped ana = servicio.registrar(Fixtures.huesped("1", "ana"));
        assertSame(ana, servicio.buscarPorDocumento("1").orElseThrow());
        assertSame(ana, servicio.buscarPorUsuario("ana").orElseThrow());
        assertTrue(servicio.buscarPorDocumento("99").isEmpty());
        assertTrue(servicio.buscarPorUsuario(null).isEmpty());
    }

    @Test
    void noPermiteDocumentoNiUsuarioRepetidos() {
        servicio.registrar(Fixtures.huesped("1", "ana"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(Fixtures.huesped("1", "otro")));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(Fixtures.huesped("2", "ana")));
        assertEquals(1, servicio.listar().size());
    }

    @Test
    void variosHuespedesSinUsuarioSonValidos() {
        servicio.registrar(Fixtures.huesped("1", null));
        assertDoesNotThrow(() -> servicio.registrar(Fixtures.huesped("2", null)));
        assertEquals(2, servicio.listar().size());
    }
}
