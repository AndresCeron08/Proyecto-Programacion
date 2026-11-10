package co.edu.uniquindio.sga.servicios;

import co.edu.uniquindio.sga.personas.Empleado;
import co.edu.uniquindio.sga.personas.Recepcionista;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServicioEmpleados {

    private final List<Empleado> empleados = new ArrayList<>();

    public Empleado contratar(Empleado nuevo) {
        if (buscarPorUsuario(nuevo.getUsuario()).isPresent()) {
            throw new IllegalArgumentException("El usuario ya está en uso: " + nuevo.getUsuario());
        }
        empleados.add(nuevo);
        return nuevo;
    }

    public Optional<Empleado> buscarPorUsuario(String usuario) {
        return empleados.stream().filter(e -> e.getUsuario().equals(usuario)).findFirst();
    }

    public void desvincular(String usuario) {
        buscarPorUsuario(usuario).orElseThrow(() -> new IllegalArgumentException("No existe el empleado " + usuario)).desvincular();
    }

    public List<Empleado> listar() { return new ArrayList<>(empleados); }

    public List<Empleado> listarActivos() { return empleados.stream().filter(Empleado::isActivo).toList(); }

    public List<Recepcionista> listarRecepcionistas() {
        return empleados.stream().filter(e -> e instanceof Recepcionista).map(e -> (Recepcionista) e).toList();
    }
}
