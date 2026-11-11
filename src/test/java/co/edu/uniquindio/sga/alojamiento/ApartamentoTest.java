package co.edu.uniquindio.sga.alojamiento;

import co.edu.uniquindio.sga.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApartamentoTest {

    private Apartamento apto;

    @BeforeEach
    void preparar() { apto = Fixtures.apto(); }

    @Test
    void naceActivoYPreparado() {
        assertTrue(apto.isActivo());
        assertEquals(EstadoApartamento.PREPARADO, apto.getEstado());
    }

    @Test
    void fueraDeServicioExigeMotivo() {
        assertThrows(IllegalArgumentException.class, () -> apto.cambiarEstado(EstadoApartamento.FUERA_DE_SERVICIO));
        assertThrows(IllegalArgumentException.class, () -> apto.cambiarEstado(EstadoApartamento.FUERA_DE_SERVICIO, "  "));
        apto.cambiarEstado(EstadoApartamento.FUERA_DE_SERVICIO, "Daño en la tubería");
        assertEquals(EstadoApartamento.FUERA_DE_SERVICIO, apto.getEstado());
        assertEquals("Daño en la tubería", apto.getMotivoFueraServicio());
    }

    @Test
    void alSalirDeFueraDeServicioSeLimpiaElMotivo() {
        apto.cambiarEstado(EstadoApartamento.FUERA_DE_SERVICIO, "Mantenimiento eléctrico");
        apto.cambiarEstado(EstadoApartamento.PREPARADO);
        assertNull(apto.getMotivoFueraServicio());
    }

    @Test
    void validaLosDatosBasicos() {
        assertThrows(IllegalArgumentException.class, () -> new Apartamento(" ", "N", 1, 2, null));
        assertThrows(IllegalArgumentException.class, () -> new Apartamento("A", " ", 1, 2, null));
        assertThrows(IllegalArgumentException.class, () -> new Apartamento("A", "N", 0, 2, null));
        assertThrows(IllegalArgumentException.class, () -> new Apartamento("A", "N", 1, 0, null));
    }

    @Test
    void sinTarifaDefinidaNoSePuedeLiquidar() {
        Apartamento sinTarifas = new Apartamento("A-9", "Sin tarifas", 1, 2, List.of());
        assertThrows(IllegalStateException.class, () -> sinTarifas.valorPersonaNoche(TipoTemporada.BAJA));
        assertEquals(100_000, apto.valorPersonaNoche(TipoTemporada.BAJA));
    }

    @Test
    void elBloqueoQuitaLaDisponibilidadSoloEnLasNochesBloqueadas() {
        apto.agregarBloqueo(new Bloqueo(LocalDate.of(2026, 12, 24), LocalDate.of(2026, 12, 26), "Mantenimiento"));
        assertTrue(apto.estaDisponible(LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 24))); // sale el 24: no ocupa esa noche
        assertFalse(apto.estaDisponible(LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 25)));
        assertFalse(apto.estaDisponible(LocalDate.of(2026, 12, 26), LocalDate.of(2026, 12, 28)));
        assertTrue(apto.estaDisponible(LocalDate.of(2026, 12, 27), LocalDate.of(2026, 12, 29)));
    }

    @Test
    void eliminadoLogicamenteNoEstaDisponible() {
        apto.desactivar();
        assertFalse(apto.isActivo());
        assertFalse(apto.estaDisponible(Fixtures.ENTRADA, Fixtures.SALIDA));
    }

    @Test
    void registraNovedadesYNoPermiteModificarLaListaExterna() {
        apto.registrarNovedad("Vidrio roto");
        assertEquals(1, apto.getNovedades().size());
        assertThrows(UnsupportedOperationException.class, () -> apto.getNovedades().clear());
        assertThrows(UnsupportedOperationException.class, () -> apto.getBloqueos().clear());
        assertThrows(UnsupportedOperationException.class, () -> apto.getCaracteristicas().clear());
    }

    @Test
    void novedadExigeDescripcion() {
        assertThrows(IllegalArgumentException.class, () -> new Novedad(LocalDateTime.now(), " "));
        assertThrows(IllegalArgumentException.class, () -> apto.registrarNovedad(null));
    }
}
