package co.edu.uniquindio.sga.servicios;

import co.edu.uniquindio.sga.Fixtures;
import co.edu.uniquindio.sga.alojamiento.Apartamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicioApartamentosTest {

    private ServicioApartamentos servicio;

    @BeforeEach
    void preparar() { servicio = new ServicioApartamentos(); }

    @Test
    void creaYBuscaPorCodigo() {
        Apartamento a = servicio.crear("A-101", "Vista", 2, 4, List.of("balcón"));
        assertSame(a, servicio.buscar("A-101").orElseThrow());
        assertTrue(servicio.buscar("Z-999").isEmpty());
    }

    @Test
    void elCodigoEsUnico() {
        servicio.crear("A-101", "Uno", 1, 2, null);
        assertThrows(IllegalArgumentException.class, () -> servicio.crear("A-101", "Otro", 1, 2, null));
        assertThrows(IllegalArgumentException.class, () -> servicio.agregar(Fixtures.apto("A-101", 2)));
    }

    @Test
    void modificaLosDatosDeUnApartamento() {
        servicio.crear("A-101", "Uno", 1, 2, null);
        servicio.modificar("A-101", "Renovado", 3, 6);
        Apartamento a = servicio.buscar("A-101").orElseThrow();
        assertEquals("Renovado", a.getNombre());
        assertEquals(3, a.getDormitorios());
        assertEquals(6, a.getCapacidadMaxima());
        assertThrows(IllegalArgumentException.class, () -> servicio.modificar("Z-1", "X", 1, 1));
    }

    @Test
    void laEliminacionEsLogica() {
        servicio.crear("A-101", "Uno", 1, 2, null);
        servicio.crear("A-102", "Dos", 1, 2, null);
        servicio.eliminar("A-101");
        assertEquals(2, servicio.listar().size());
        assertEquals(1, servicio.listarActivos().size());
        assertFalse(servicio.buscar("A-101").orElseThrow().isActivo());
        assertThrows(IllegalArgumentException.class, () -> servicio.eliminar("Z-1"));
    }
}
