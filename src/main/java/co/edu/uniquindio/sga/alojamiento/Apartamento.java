package co.edu.uniquindio.sga.alojamiento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Apartamento {

    private final String codigo;
    private String nombre;
    private int dormitorios;
    private int capacidadMaxima;
    private final List<String> caracteristicas = new ArrayList<>();
    private EstadoApartamento estado = EstadoApartamento.PREPARADO;
    private String motivoFueraServicio;
    private boolean activo = true;

    private final List<Bloqueo> bloqueos = new ArrayList<>();
    private final List<Novedad> novedades = new ArrayList<>();
    private final Map<TipoTemporada, Tarifa> tarifas = new EnumMap<>(TipoTemporada.class);

    public Apartamento(String codigo, String nombre, int dormitorios, int capacidadMaxima, List<String> caracteristicas) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
        this.codigo = codigo.trim();
        setNombre(nombre);
        setDormitorios(dormitorios);
        setCapacidadMaxima(capacidadMaxima);
        if (caracteristicas != null) this.caracteristicas.addAll(caracteristicas);
    }


    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio");
        this.nombre = nombre.trim();
    }

    public int getDormitorios() { return dormitorios; }

    public void setDormitorios(int dormitorios) {
        if (dormitorios < 1) throw new IllegalArgumentException("Debe tener al menos un dormitorio");
        this.dormitorios = dormitorios;
    }

    public int getCapacidadMaxima() { return capacidadMaxima; }

    public void setCapacidadMaxima(int capacidadMaxima) {
        if (capacidadMaxima < 1) throw new IllegalArgumentException("La capacidad debe ser al menos 1");
        this.capacidadMaxima = capacidadMaxima;
    }

    public List<String> getCaracteristicas() { return Collections.unmodifiableList(caracteristicas); }
    public boolean isActivo() { return activo; }
    public void desactivar() { this.activo = false; }

    public EstadoApartamento getEstado() { return estado; }
    public String getMotivoFueraServicio() { return motivoFueraServicio; }

    public void cambiarEstado(EstadoApartamento nuevo) {
        if (nuevo == EstadoApartamento.FUERA_DE_SERVICIO) {
            throw new IllegalArgumentException("Fuera de servicio requiere un motivo: use cambiarEstado(nuevo, motivo)");
        }
        this.estado = nuevo;
        this.motivoFueraServicio = null;
    }

    public void cambiarEstado(EstadoApartamento nuevo, String motivo) {
        if (nuevo == EstadoApartamento.FUERA_DE_SERVICIO) {
            if (motivo == null || motivo.isBlank()) {
                throw new IllegalArgumentException("Debe registrar el motivo de Fuera de servicio");
            }
            this.estado = nuevo;
            this.motivoFueraServicio = motivo;
        } else {
            cambiarEstado(nuevo);
        }
    }


    public void definirTarifa(TipoTemporada tipo, long valorPersonaNoche) {
        tarifas.put(tipo, new Tarifa(tipo, valorPersonaNoche));
    }

    public long valorPersonaNoche(TipoTemporada tipo) {
        Tarifa t = tarifas.get(tipo);
        if (t == null) {
            throw new IllegalStateException("El apartamento " + codigo + " no tiene tarifa " + tipo);
        }
        return t.getValorPersonaNoche();
    }


    public void agregarBloqueo(Bloqueo bloqueo) { bloqueos.add(bloqueo); }
    public List<Bloqueo> getBloqueos() { return Collections.unmodifiableList(bloqueos); }

    public void registrarNovedad(String descripcion) {
        novedades.add(new Novedad(LocalDateTime.now(), descripcion));
    }

    public List<Novedad> getNovedades() { return Collections.unmodifiableList(novedades); }


    public boolean estaDisponible(LocalDate entrada, LocalDate salida) {
        return activo && bloqueos.stream().noneMatch(b -> b.choca(entrada, salida));
    }

    @Override
    public String toString() { return codigo + " - " + nombre; }
}
