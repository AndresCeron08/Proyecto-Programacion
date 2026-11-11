package co.edu.uniquindio.sga.personas;

import co.edu.uniquindio.sga.Fixtures;
import co.edu.uniquindio.sga.SGA;
import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.alojamiento.EstadoApartamento;
import co.edu.uniquindio.sga.folio.MedioPago;
import co.edu.uniquindio.sga.reservas.EstadoReserva;
import co.edu.uniquindio.sga.reservas.ReservaDirecta;
import co.edu.uniquindio.sga.reservas.ReservaExterna;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RecepcionistaTest {

    private SGA sga;
    private Recepcionista recepcion;
    private Apartamento apto;
    private Huesped ana;

    @BeforeEach
    void preparar() {
        sga = new SGA(Fixtures.reloj(Fixtures.AHORA));
        recepcion = new Recepcionista("Marta", LocalDate.of(1995, 5, 5), "marta", "clave1", sga.reservas());
        apto = sga.apartamentos().agregar(Fixtures.apto());
        ana = sga.huespedes().registrar(Fixtures.huesped());
    }

    @Test
    void registraReservasDirectasYExternas() {
        ReservaDirecta d = recepcion.registrarDirecta(ana, apto, Fixtures.adultos(2), Fixtures.ENTRADA, Fixtures.SALIDA);
        assertEquals(EstadoReserva.PENDIENTE, d.getEstado());
        ReservaExterna e = recepcion.recibirExterna("BKG-1", ana, apto, Fixtures.adultos(2), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 27));
        assertEquals("BKG-1", e.getCodigoExterno());
    }

    @Test
    void confirmaYHaceCheckInYCheckOut() {
        LocalDate hoy = LocalDate.of(2026, 11, 5);
        ReservaDirecta d = recepcion.registrarDirecta(ana, apto, Fixtures.adultos(2), hoy, hoy.plusDays(2));
        recepcion.confirmar(d);
        recepcion.checkIn(d);
        assertEquals(EstadoApartamento.OCUPADO, apto.getEstado());
        recepcion.checkOut(d);
        assertEquals(EstadoReserva.FINALIZADA, d.getEstado());
        assertEquals(EstadoApartamento.PENDIENTE_PREPARACION, apto.getEstado());
    }

    @Test
    void registraPagosEnElFolio() {
        ReservaDirecta d = recepcion.registrarDirecta(ana, apto, Fixtures.adultos(2), Fixtures.ENTRADA, Fixtures.SALIDA);
        recepcion.registrarPago(d, MedioPago.EFECTIVO, 100_000);
        assertEquals(500_000, d.getFolio().calcularSaldo());
    }

    @Test
    void cambiaElEstadoOperativoYRegistraNovedades() {
        recepcion.cambiarEstadoApartamento(apto, EstadoApartamento.EN_PREPARACION);
        assertEquals(EstadoApartamento.EN_PREPARACION, apto.getEstado());
        recepcion.registrarNovedad(apto, "Falta una toalla");
        assertEquals(1, apto.getNovedades().size());
    }
}
