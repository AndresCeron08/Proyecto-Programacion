package co.edu.uniquindio.sga.folio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FolioTest {

    private Folio folio;
    private Cargo alojamiento;

    @BeforeEach
    void preparar() {
        folio = new Folio();
        alojamiento = new Cargo(TipoCargo.ALOJAMIENTO, 1_000_000, "Alojamiento");
        folio.agregarCargo(alojamiento);
    }

    @Test
    void saldoEsCargosMenosPagosConVariosMediosDePago() {
        folio.agregarPago(new Pago(MedioPago.EFECTIVO, 300_000, "Anticipo"));
        folio.agregarPago(new Pago(MedioPago.TARJETA, 200_000, "Abono"));
        assertEquals(1_000_000, folio.totalCargos());
        assertEquals(500_000, folio.totalPagos());
        assertEquals(500_000, folio.calcularSaldo());
    }

    @Test
    void losServiciosAdicionalesSumanAlSaldo() {
        folio.agregarCargo(new Cargo(TipoCargo.SERVICIO, 30_000, "Desayuno"));
        folio.agregarCargo(new Cargo(TipoCargo.PENALIDAD, 20_000, "Daño"));
        assertEquals(1_050_000, folio.calcularSaldo());
    }

    @Test
    void corregirUnPagoRegistraUnMovimientoContrarioSinTocarElOriginal() {
        Pago pago = new Pago(MedioPago.TRANSFERENCIA, 200_000, "Abono");
        folio.agregarPago(pago);
        Movimiento reverso = folio.revertir(pago);
        assertTrue(reverso.isEsReverso());
        assertEquals(-200_000, reverso.getMonto());
        assertEquals(200_000, pago.getMonto()); // el original no cambia
        assertEquals(3, folio.getMovimientos().size()); // nada se elimina
        assertEquals(1_000_000, folio.calcularSaldo());
    }

    @Test
    void corregirUnCargoReduceLosCargos() {
        Cargo desayuno = new Cargo(TipoCargo.SERVICIO, 30_000, "Desayuno");
        folio.agregarCargo(desayuno);
        folio.revertir(desayuno);
        assertEquals(1_000_000, folio.totalCargos());
    }

    @Test
    void unMovimientoSoloSePuedeRevertirUnaVez() {
        Pago pago = new Pago(MedioPago.EFECTIVO, 100_000, "Abono");
        folio.agregarPago(pago);
        folio.revertir(pago);
        assertThrows(IllegalStateException.class, () -> folio.revertir(pago));
    }

    @Test
    void unReversoNoSePuedeRevertir() {
        Pago pago = new Pago(MedioPago.EFECTIVO, 100_000, "Abono");
        folio.agregarPago(pago);
        Movimiento reverso = folio.revertir(pago);
        assertThrows(IllegalStateException.class, () -> folio.revertir(reverso));
    }

    @Test
    void noRevierteMovimientosAjenos() {
        Pago ajeno = new Pago(MedioPago.EFECTIVO, 100_000, "De otro folio");
        assertThrows(IllegalArgumentException.class, () -> folio.revertir(ajeno));
    }

    @Test
    void losMovimientosNormalesExigenMontoPositivo() {
        assertThrows(IllegalArgumentException.class, () -> new Pago(MedioPago.EFECTIVO, 0, "x"));
        assertThrows(IllegalArgumentException.class, () -> new Cargo(TipoCargo.SERVICIO, -1, "x"));
    }

    @Test
    void laListaDeMovimientosNoSePuedeModificarDesdeAfuera() {
        assertThrows(UnsupportedOperationException.class, () -> folio.getMovimientos().clear());
        assertThrows(UnsupportedOperationException.class, () -> folio.getMovimientos().remove(0));
    }

    @Test
    void guardaMedioFechaYTipo() {
        Pago p = new Pago(MedioPago.TARJETA, 5_000, "Abono");
        assertEquals(MedioPago.TARJETA, p.getMedio());
        assertNotNull(p.getFecha());
        assertEquals(TipoCargo.ALOJAMIENTO, alojamiento.getTipo());
    }
}
