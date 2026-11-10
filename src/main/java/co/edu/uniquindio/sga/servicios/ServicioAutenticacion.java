package co.edu.uniquindio.sga.servicios;

import co.edu.uniquindio.sga.personas.Empleado;
import co.edu.uniquindio.sga.personas.Huesped;
import co.edu.uniquindio.sga.personas.IAutenticable;

import java.util.Optional;

public class ServicioAutenticacion {

    public static final String ROL_HUESPED = "HUESPED";

    private final ServicioHuespedes huespedes;
    private final ServicioEmpleados empleados;

    public ServicioAutenticacion(ServicioHuespedes huespedes, ServicioEmpleados empleados) {
        this.huespedes = huespedes;
        this.empleados = empleados;
    }

    public Optional<IAutenticable> iniciarSesion(String usuario, String clave) {
        if (usuario == null || clave == null) return Optional.empty();
        Optional<IAutenticable> empleado = empleados.buscarPorUsuario(usuario)
                .filter(e -> e.autenticar(usuario, clave)).map(e -> (IAutenticable) e);
        if (empleado.isPresent()) return empleado;
        return huespedes.buscarPorUsuario(usuario).filter(h -> h.autenticar(usuario, clave)).map(h -> (IAutenticable) h);
    }

    public static String rolDe(IAutenticable sesion) {
        if (sesion instanceof Empleado e) return e.getRol();
        if (sesion instanceof Huesped) return ROL_HUESPED;
        throw new IllegalArgumentException("Tipo de usuario desconocido");
    }
}
