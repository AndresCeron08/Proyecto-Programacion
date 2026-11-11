package co.edu.uniquindio.sga.reservas;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static co.edu.uniquindio.sga.reservas.EstadoReserva.*;
import static org.junit.jupiter.api.Assertions.*;

class EstadoReservaTest {

    private static final Map<EstadoReserva, Set<EstadoReserva>> PERMITIDAS = Map.of(
            PENDIENTE, Set.of(CONFIRMADA, CANCELADA),
            CONFIRMADA, Set.of(EN_CURSO, CANCELADA, NO_SHOW),
            EN_CURSO, Set.of(FINALIZADA),
            FINALIZADA, Set.of(),
            CANCELADA, Set.of(),
            NO_SHOW, Set.of());

    @Test
    void soloPermiteLasTransicionesDeLaTablaDelCicloDeVida() {
        for (EstadoReserva origen : values()) {
            for (EstadoReserva destino : values()) {
                assertEquals(PERMITIDAS.get(origen).contains(destino), origen.puedePasarA(destino),
                        origen + " -> " + destino);
            }
        }
    }

    @Test
    void finalizadaCanceladaYNoShowSonEstadosFinales() {
        assertTrue(FINALIZADA.esFinal());
        assertTrue(CANCELADA.esFinal());
        assertTrue(NO_SHOW.esFinal());
        assertFalse(PENDIENTE.esFinal());
        assertFalse(CONFIRMADA.esFinal());
        assertFalse(EN_CURSO.esFinal());
    }

    @Test
    void activaMientrasEstePendienteConfirmadaOEnCurso() {
        assertTrue(PENDIENTE.esActiva());
        assertTrue(CONFIRMADA.esActiva());
        assertTrue(EN_CURSO.esActiva());
        assertFalse(FINALIZADA.esActiva());
        assertFalse(CANCELADA.esActiva());
        assertFalse(NO_SHOW.esActiva());
    }
}
