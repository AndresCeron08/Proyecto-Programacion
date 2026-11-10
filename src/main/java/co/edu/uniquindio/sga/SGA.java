package co.edu.uniquindio.sga;

import co.edu.uniquindio.sga.cancelacion.PoliticaCancelacionEstandar;
import co.edu.uniquindio.sga.personas.Administrador;
import co.edu.uniquindio.sga.servicios.ServicioApartamentos;
import co.edu.uniquindio.sga.servicios.ServicioAutenticacion;
import co.edu.uniquindio.sga.servicios.ServicioEmpleados;
import co.edu.uniquindio.sga.servicios.ServicioHuespedes;
import co.edu.uniquindio.sga.servicios.ServicioReportes;
import co.edu.uniquindio.sga.servicios.ServicioReservas;
import co.edu.uniquindio.sga.servicios.ServicioTarifas;

import java.time.Clock;
import java.time.LocalDate;

public class SGA {

    public static final String ADMIN_USUARIO = "admin";
    public static final String ADMIN_CLAVE_INICIAL = "admin123";

    private final ServicioApartamentos apartamentos = new ServicioApartamentos();
    private final ServicioTarifas tarifas = new ServicioTarifas();
    private final ServicioHuespedes huespedes = new ServicioHuespedes();
    private final ServicioEmpleados empleados = new ServicioEmpleados();
    private final ServicioReservas reservas;
    private final ServicioAutenticacion autenticacion;
    private final ServicioReportes reportes;

    public SGA() { this(Clock.systemDefaultZone()); }

    public SGA(Clock reloj) {
        this.reservas = new ServicioReservas(apartamentos, tarifas, new PoliticaCancelacionEstandar(), reloj);
        this.autenticacion = new ServicioAutenticacion(huespedes, empleados);
        this.reportes = new ServicioReportes(reservas, apartamentos);
        empleados.contratar(new Administrador("Administrador", LocalDate.of(1980, 1, 1),
                ADMIN_USUARIO, ADMIN_CLAVE_INICIAL, tarifas));
    }

    public ServicioApartamentos apartamentos() { return apartamentos; }
    public ServicioTarifas tarifas() { return tarifas; }
    public ServicioHuespedes huespedes() { return huespedes; }
    public ServicioEmpleados empleados() { return empleados; }
    public ServicioReservas reservas() { return reservas; }
    public ServicioAutenticacion autenticacion() { return autenticacion; }
    public ServicioReportes reportes() { return reportes; }
}
