package co.edu.uniquindio.sga.servicios;

import co.edu.uniquindio.sga.alojamiento.Apartamento;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServicioApartamentos {

    private final Map<String, Apartamento> inventario = new LinkedHashMap<>();

    public Apartamento crear(String codigo, String nombre, int dormitorios, int capacidadMaxima, List<String> caracteristicas) {
        return agregar(new Apartamento(codigo, nombre, dormitorios, capacidadMaxima, caracteristicas));
    }

    public Apartamento agregar(Apartamento apartamento) {
        if (inventario.containsKey(apartamento.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un apartamento con el código " + apartamento.getCodigo());
        }
        inventario.put(apartamento.getCodigo(), apartamento);
        return apartamento;
    }

    public Optional<Apartamento> buscar(String codigo) {
        return Optional.ofNullable(inventario.get(codigo));
    }

    public void modificar(String codigo, String nombre, int dormitorios, int capacidadMaxima) {
        Apartamento a = obtener(codigo);
        a.setNombre(nombre);
        a.setDormitorios(dormitorios);
        a.setCapacidadMaxima(capacidadMaxima);
    }

    public void eliminar(String codigo) { obtener(codigo).desactivar(); }

    public List<Apartamento> listar() { return new ArrayList<>(inventario.values()); }

    public List<Apartamento> listarActivos() {
        return inventario.values().stream().filter(Apartamento::isActivo).toList();
    }

    private Apartamento obtener(String codigo) {
        return buscar(codigo).orElseThrow(() ->
                new IllegalArgumentException("No existe el apartamento " + codigo));
    }
}
