package co.edu.uniquindio.sga.reservas;

import co.edu.uniquindio.sga.Fixtures;
import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.cancelacion.PoliticaCancelacionEstandar;
import co.edu.uniquindio.sga.folio.Cargo;
import co.edu.uniquindio.sga.folio.TipoCargo;
import co.edu.uniquindio.sga.personas.Huesped;
import co.edu.uniquindio.sga.personas.Ocupante;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {

    private Apartamento apto;
    private Huesped ana;

    @BeforeEach
    void preparar() {
        apto = Fixtures.apto();
        ana = Fixtures.huesped();
    }

    private Reserva reserva(LocalDate entrada, LocalDate salida, List<Ocupante> ocupantes, long valor) {
        return new ReservaDirecta("R-1", ana, apto, ocupantes, entrada, salida, valor,
                new PoliticaCancelacionEstandar(), Fixtures.AHORA);
    }

    private Reserva reservaBase() {
        return reserva(Fixtures.ENTRADA, Fixtures.SALIDA, Fixtures.adultos(2), 1_000_000);
    }

    @Test
    void laNocheDeSalidaNoSeCuenta() {
        Reserva r = reserva(LocalDate.of(2027, 3, 10), LocalDate.of(2027, 3, 12), Fixtures.adultos(2), 1);
        assertEquals(2, r.calcularNoches());
    }

    @Test
    void exigeAlMenosUnaNoche() {
        assertThrows(IllegalArgumentException.class, () -> reserva(Fixtures.ENTRADA, Fixtures.ENTRADA, Fixtures.adultos(2), 1));
        assertThrows(IllegalArgumentException.class, () -> reserva(Fixtures.SALIDA, Fixtures.ENTRADA, Fixtures.adultos(2), 1));
    }

    @Test
    void todosLosOcupantesCuentanParaLaCapacidadPaguenONo() {
        List<Ocupante> cinco = new ArrayList<>(Fixtures.adultos(3));
        cinco.add(Fixtures.nino("Niño 1"));
        cinco.add(Fixtures.nino("Niño 2"));
        assertThrows(IllegalArgumentException.class, () -> reserva(Fixtures.ENTRADA, Fixtures.SALIDA, cinco, 1));
        List<Ocupante> cuatro = new ArrayList<>(Fixtures.adultos(3));
        cuatro.add(Fixtures.nino("Niño 1"));
        assertDoesNotThrow(() -> reserva(Fixtures.ENTRADA, Fixtures.SALIDA, cuatro, 1));
    }

    @Test
    void exigeOcupantesTitularYApartamento() {
        assertThrows(IllegalArgumentException.class, () -> reserva(Fixtures.ENTRADA, Fixtures.SALIDA, List.of(), 1));
        assertThrows(IllegalArgumentException.class, () -> reserva(Fixtures.ENTRADA, Fixtures.SALIDA, null, 1));
        assertThrows(IllegalArgumentException.class, () -> new ReservaDirecta("R", null, apto, Fixtures.adultos(1), Fixtures.ENTRADA, Fixtures.SALIDA, 1, new PoliticaCancelacionEstandar(), Fixtures.AHORA));
        assertThrows(IllegalArgumentException.class, () -> new ReservaDirecta("R", ana, null, Fixtures.adultos(1), Fixtures.ENTRADA, Fixtures.SALIDA, 1, new PoliticaCancelacionEstandar(), Fixtures.AHORA));
    }

    @Test
    void nacePendienteYAvanzaPorElCicloDeVida() {
        Reserva r = reservaBase();
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
        assertTrue(r.estaActiva());
        r.cambiarEstado(EstadoReserva.CONFIRMADA);
        r.cambiarEstado(EstadoReserva.EN_CURSO);
        r.cambiarEstado(EstadoReserva.FINALIZADA);
        assertFalse(r.estaActiva());
    }

    @Test
    void rechazaCambiosDeEstadoNoPermitidos() {
        Reserva r = reservaBase();
        assertThrows(IllegalStateException.class, () -> r.cambiarEstado(EstadoReserva.EN_CURSO));
        assertThrows(IllegalStateException.class, () -> r.cambiarEstado(EstadoReserva.FINALIZADA));
        r.cambiarEstado(EstadoReserva.CANCELADA);
        assertThrows(IllegalStateException.class, () -> r.cambiarEstado(EstadoReserva.CONFIRMADA));
        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
    }

    @Test
    void elFolioNaceConElCargoDeAlojamiento() {
        Reserva r = reservaBase();
        assertEquals(1, r.getFolio().getMovimientos().size());
        Cargo c = (Cargo) r.getFolio().getMovimientos().get(0);
        assertEquals(TipoCargo.ALOJAMIENTO, c.getTipo());
        assertEquals(1_000_000, r.getFolio().calcularSaldo());
    }

    @Test
    void sinValorAPagarNoSeCreaCargo() {
        Reserva r = reserva(Fixtures.ENTRADA, Fixtures.SALIDA, Fixtures.adultos(1), 0);
        assertTrue(r.getFolio().getMovimientos().isEmpty());
    }

    @Test
    void detectaNochesCompartidas() {
        Reserva r = reservaBase(); // noches 20, 21 y 22 de diciembre
        assertFalse(r.compartenNoches(LocalDate.of(2026, 12, 23), LocalDate.of(2026, 12, 25)));
        assertTrue(r.compartenNoches(LocalDate.of(2026, 12, 22), LocalDate.of(2026, 12, 24)));
        assertFalse(r.compartenNoches(LocalDate.of(2026, 12, 18), LocalDate.of(2026, 12, 20)));
        assertTrue(r.compartenNoches(LocalDate.of(2026, 12, 19), LocalDate.of(2026, 12, 21)));
        assertTrue(r.compartenNoches(LocalDate.of(2026, 12, 21), LocalDate.of(2026, 12, 22)));
    }

    @Test
    void protegeLaListaDeOcupantes() {
        List<Ocupante> original = new ArrayList<>(Fixtures.adultos(2));
        Reserva r = reserva(Fixtures.ENTRADA, Fixtures.SALIDA, original, 1);
        original.clear();
        assertEquals(2, r.getOcupantes().size());
        assertThrows(UnsupportedOperationException.class, () -> r.getOcupantes().clear());
    }
}
