package co.edu.uniquindio.sga.personas;

import co.edu.uniquindio.sga.Fixtures;
import co.edu.uniquindio.sga.SGA;
import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.alojamiento.Temporada;
import co.edu.uniquindio.sga.alojamiento.TipoTemporada;
import co.edu.uniquindio.sga.cancelacion.IPoliticaCancelacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AdministradorTest {

    private SGA sga;
    private Administrador admin;

    @BeforeEach
    void preparar() {
        sga = new SGA(Fixtures.reloj(Fixtures.AHORA));
        admin = (Administrador) sga.empleados().buscarPorUsuario(SGA.ADMIN_USUARIO).orElseThrow();
    }

    @Test
    void contrataUnRecepcionistaConUsuarioUnico() {
        Recepcionista r = admin.contratarRecepcionista(sga.empleados(), sga.reservas(), "Marta", LocalDate.of(1995, 5, 5), "marta", "clave1");
        assertTrue(sga.empleados().listarRecepcionistas().contains(r));
        assertThrows(IllegalArgumentException.class, () -> admin.contratarRecepcionista(sga.empleados(), sga.reservas(), "Otra", LocalDate.of(1990, 1, 1), "marta", "x"));
    }

    @Test
    void desvinculaEmpleados() {
        Recepcionista r = admin.contratarRecepcionista(sga.empleados(), sga.reservas(), "Marta", LocalDate.of(1995, 5, 5), "marta", "clave1");
        admin.desvincular(r);
        assertFalse(r.isActivo());
    }

    @Test
    void defineTemporadasYTarifas() {
        Apartamento apto = sga.apartamentos().crear("A-1", "Uno", 1, 2, null);
        admin.definirTarifa(apto, TipoTemporada.BAJA, 90_000);
        assertEquals(90_000, apto.valorPersonaNoche(TipoTemporada.BAJA));
        admin.definirTemporada(new Temporada(TipoTemporada.ALTA, LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 15)));
        assertEquals(TipoTemporada.ALTA, sga.tarifas().temporadaDe(LocalDate.of(2026, 12, 31)));
    }

    @Test
    void registraBloqueosYEliminaLogicamenteApartamentos() {
        Apartamento apto = sga.apartamentos().agregar(Fixtures.apto());
        admin.registrarBloqueo(apto, LocalDate.of(2026, 12, 21), LocalDate.of(2026, 12, 22), "Mantenimiento eléctrico");
        assertFalse(apto.estaDisponible(Fixtures.ENTRADA, Fixtures.SALIDA));
        admin.eliminarApartamento(apto);
        assertFalse(apto.isActivo());
    }

    @Test
    void configuraLaPoliticaDeCancelacion() {
        IPoliticaCancelacion sinRetencion = (reserva, cuando) -> 0L;
        admin.configurarPolitica(sga.reservas(), sinRetencion);
        assertSame(sinRetencion, sga.reservas().getPoliticaVigente());
    }
}
