package co.edu.uniquindio.sga.personas;

import co.edu.uniquindio.sga.Fixtures;
import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.cancelacion.PoliticaCancelacionEstandar;
import co.edu.uniquindio.sga.folio.MedioPago;
import co.edu.uniquindio.sga.reservas.EstadoReserva;
import co.edu.uniquindio.sga.reservas.Reserva;
import co.edu.uniquindio.sga.servicios.ServicioApartamentos;
import co.edu.uniquindio.sga.servicios.ServicioReservas;
import co.edu.uniquindio.sga.servicios.ServicioTarifas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HuespedTest {

    private ServicioTarifas tarifas;
    private ServicioReservas servicio;
    private Apartamento apto;

    @BeforeEach
    void preparar() {
        ServicioApartamentos apartamentos = new ServicioApartamentos();
        tarifas = new ServicioTarifas();
        servicio = new ServicioReservas(apartamentos, tarifas, new PoliticaCancelacionEstandar(),
                Fixtures.reloj(Fixtures.AHORA));
        apto = apartamentos.agregar(Fixtures.apto());
    }

    @Test
    void autenticaConCredencialesCorrectasYRechazaLasIncorrectas() {
        Huesped h = Fixtures.huesped("1", "ana");
        assertTrue(h.autenticar("ana", "1234"));
        assertFalse(h.autenticar("ana", "mala"));
        assertFalse(h.autenticar("otro", "1234"));
        assertFalse(h.autenticar(null, null));
    }

    @Test
    void huespedSinUsuarioNoPuedeIniciarSesion() {
        assertFalse(Fixtures.huesped("1", null).autenticar(null, null));
    }

    @Test
    void documentoEsObligatorio() {
        assertThrows(IllegalArgumentException.class, () -> Fixtures.huesped(" ", "ana"));
    }

    @Test
    void cotizaUnaEstancia() {
        long valor = Fixtures.huesped().cotizar(tarifas, apto, Fixtures.ENTRADA, Fixtures.SALIDA, Fixtures.adultos(2));
        assertEquals(600_000, valor);
    }

    @Test
    void cancelaUnaReservaSuyaYRecibeLaDevolucion() {
        Huesped ana = Fixtures.huesped();
        Reserva r = servicio.crearReservaPortal(ana, apto, Fixtures.adultos(2), Fixtures.ENTRADA, Fixtures.SALIDA);
        servicio.registrarPago(r, MedioPago.TARJETA, 180_000);
        assertEquals(180_000, ana.cancelarReserva(servicio, r));
        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
    }

    @Test
    void noPuedeCancelarLaReservaDeOtroHuesped() {
        Reserva r = servicio.crearReservaPortal(Fixtures.huesped("1", "ana"), apto, Fixtures.adultos(2), Fixtures.ENTRADA, Fixtures.SALIDA);
        Huesped intruso = Fixtures.huesped("2", "luis");
        assertThrows(IllegalArgumentException.class, () -> intruso.cancelarReserva(servicio, r));
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
    }
}
