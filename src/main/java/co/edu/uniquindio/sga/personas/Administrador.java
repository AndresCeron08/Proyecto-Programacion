package co.edu.uniquindio.sga.personas;

import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.alojamiento.Bloqueo;
import co.edu.uniquindio.sga.alojamiento.Temporada;
import co.edu.uniquindio.sga.alojamiento.TipoTemporada;
import co.edu.uniquindio.sga.cancelacion.IPoliticaCancelacion;
import co.edu.uniquindio.sga.servicios.ServicioEmpleados;
import co.edu.uniquindio.sga.servicios.ServicioReservas;
import co.edu.uniquindio.sga.servicios.ServicioTarifas;

import java.time.LocalDate;

public class Administrador extends Empleado {

    private final ServicioTarifas tarifas;

    public Administrador(String nombre, LocalDate fechaNacimiento, String usuario, String clave, ServicioTarifas tarifas) {
        super(nombre, fechaNacimiento, usuario, clave);
        this.tarifas = tarifas;
    }

    @Override
    public String getRol() { return "ADMINISTRADOR"; }

    public void definirTemporada(Temporada temporada) { tarifas.agregarTemporada(temporada); }

    public void definirTarifa(Apartamento apto, TipoTemporada tipo, long valorPersonaNoche) {
        apto.definirTarifa(tipo, valorPersonaNoche);
    }

    public void registrarBloqueo(Apartamento apto, LocalDate desde, LocalDate hasta, String motivo) {
        apto.agregarBloqueo(new Bloqueo(desde, hasta, motivo));
    }

    public void eliminarApartamento(Apartamento apto) { apto.desactivar(); }

    public void desvincular(Empleado empleado) { empleado.desvincular(); }

    public Recepcionista contratarRecepcionista(ServicioEmpleados empleados, ServicioReservas reservas, String nombre, LocalDate fechaNacimiento, String usuario, String clave) {
        Recepcionista r = new Recepcionista(nombre, fechaNacimiento, usuario, clave, reservas);
        empleados.contratar(r);
        return r;
    }

    public void configurarPolitica(ServicioReservas reservas, IPoliticaCancelacion politica) {
        reservas.configurarPolitica(politica);
    }
}
