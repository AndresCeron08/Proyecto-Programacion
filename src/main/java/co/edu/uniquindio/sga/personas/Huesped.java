package co.edu.uniquindio.sga.personas;

import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.reservas.Reserva;
import co.edu.uniquindio.sga.servicios.ServicioReservas;
import co.edu.uniquindio.sga.servicios.ServicioTarifas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public class Huesped extends Persona implements IAutenticable {

    private String documento;
    private String telefono;
    private String correo;
    private final String usuario;
    private final String clave;

    public Huesped(String nombre, LocalDate fechaNacimiento, String documento, String telefono, String correo, String usuario, String clave) {
        super(nombre, fechaNacimiento);
        setDocumento(documento);
        this.telefono = telefono;
        this.correo = correo;
        this.usuario = usuario;
        this.clave = clave;
    }

    public String getDocumento() { return documento; }

    public void setDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }
        this.documento = documento.trim();
    }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getUsuario() { return usuario; }

    @Override
    public boolean autenticar(String usuario, String clave) {
        return this.usuario != null && this.usuario.equals(usuario) && this.clave != null && this.clave.equals(clave);
    }

    public long cotizar(ServicioTarifas tarifas, Apartamento apto, LocalDate entrada, LocalDate salida, List<Ocupante> ocupantes) {
        return tarifas.calcularValor(apto, entrada, salida, ocupantes);
    }

    public long cancelarReserva(ServicioReservas servicio, Reserva reserva) {
        if (reserva.getTitular() != this) {
            throw new IllegalArgumentException("La reserva no pertenece a este huésped");
        }
        return servicio.cancelar(reserva);
    }

    public long consultarDevolucion(ServicioReservas servicio, Reserva reserva) {
        return servicio.calcularDevolucion(reserva, LocalDateTime.now());
    }
}
