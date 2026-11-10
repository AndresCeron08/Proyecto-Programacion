package co.edu.uniquindio.sga.personas;

import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.alojamiento.EstadoApartamento;
import co.edu.uniquindio.sga.folio.MedioPago;
import co.edu.uniquindio.sga.reservas.Reserva;
import co.edu.uniquindio.sga.reservas.ReservaDirecta;
import co.edu.uniquindio.sga.reservas.ReservaExterna;
import co.edu.uniquindio.sga.servicios.ServicioReservas;

import java.time.LocalDate;
import java.util.List;

public class Recepcionista extends Empleado {

    private final ServicioReservas servicio;

    public Recepcionista(String nombre, LocalDate fechaNacimiento, String usuario, String clave, ServicioReservas servicio) {
        super(nombre, fechaNacimiento, usuario, clave);
        this.servicio = servicio;
    }

    @Override
    public String getRol() { return "RECEPCIONISTA"; }

    public ReservaDirecta registrarDirecta(Huesped titular, Apartamento apto, List<Ocupante> ocupantes, LocalDate entrada, LocalDate salida) {
        return servicio.crearReservaDirecta(titular, apto, ocupantes, entrada, salida);
    }

    public ReservaExterna recibirExterna(String codigoExterno, Huesped titular, Apartamento apto, List<Ocupante> ocupantes, LocalDate entrada, LocalDate salida) {
        return servicio.recibirReservaExterna(codigoExterno, titular, apto, ocupantes, entrada, salida);
    }

    public void confirmar(Reserva r) { servicio.confirmar(r); }
    public void checkIn(Reserva r) { servicio.checkIn(r); }
    public void checkOut(Reserva r) { servicio.checkOut(r); }
    public void declararNoShow(Reserva r) { servicio.declararNoShow(r); }

    public void registrarPago(Reserva r, MedioPago medio, long monto) {
        servicio.registrarPago(r, medio, monto);
    }

    public void cambiarEstadoApartamento(Apartamento a, EstadoApartamento nuevo) {
        a.cambiarEstado(nuevo);
    }

    public void registrarNovedad(Apartamento a, String descripcion) {
        a.registrarNovedad(descripcion);
    }
}
