package co.edu.uniquindio.sga.servicios;

import co.edu.uniquindio.sga.personas.Huesped;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServicioHuespedes {

    private final List<Huesped> huespedes = new ArrayList<>();

    public Huesped registrar(Huesped nuevo) {
        if (buscarPorDocumento(nuevo.getDocumento()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un huésped con el documento " + nuevo.getDocumento());
        }
        if (nuevo.getUsuario() != null && buscarPorUsuario(nuevo.getUsuario()).isPresent()) {
            throw new IllegalArgumentException("El usuario ya está en uso: " + nuevo.getUsuario());
        }
        huespedes.add(nuevo);
        return nuevo;
    }

    public Optional<Huesped> buscarPorDocumento(String documento) {
        return huespedes.stream().filter(h -> h.getDocumento().equals(documento)).findFirst();
    }

    public Optional<Huesped> buscarPorUsuario(String usuario) {
        return huespedes.stream().filter(h -> usuario != null && usuario.equals(h.getUsuario())).findFirst();
    }

    public List<Huesped> listar() { return new ArrayList<>(huespedes); }
}
