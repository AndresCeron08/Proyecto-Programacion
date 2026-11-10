package co.edu.uniquindio.sga.reservas;

import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.cancelacion.IPoliticaCancelacion;
import co.edu.uniquindio.sga.folio.Cargo;
import co.edu.uniquindio.sga.folio.Folio;
import co.edu.uniquindio.sga.folio.TipoCargo;
import co.edu.uniquindio.sga.personas.Huesped;
import co.edu.uniquindio.sga.personas.Ocupante;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Reserva {

    private final String codigo;
    private final Huesped titular;
    private final Apartamento apartamento;
    private final List<Ocupante> ocupantes;
    private final LocalDate entrada;
    private final LocalDate salida;
    private EstadoReserva estado = EstadoReserva.PENDIENTE;
    private final long valorCongelado;
    private final IPoliticaCancelacion politica;
    private final LocalDateTime creadaEn;
    private final Folio folio = new Folio();

    protected Reserva(String codigo, Huesped titular, Apartamento apartamento, List<Ocupante> ocupantes, LocalDate entrada, LocalDate salida, long valorCongelado, IPoliticaCancelacion politica, LocalDateTime creadaEn) {
        if (titular == null || apartamento == null || politica == null) {
            throw new IllegalArgumentException("Titular, apartamento y política son obligatorios");
        }
        if (entrada == null || salida == null || !salida.isAfter(entrada)) {
            throw new IllegalArgumentException("La salida debe ser posterior a la entrada (mínimo una noche)");
        }
        if (ocupantes == null || ocupantes.isEmpty()) {
            throw new IllegalArgumentException("Debe registrar al menos un ocupante");
        }
        if (ocupantes.size() > apartamento.getCapacidadMaxima()) {
            throw new IllegalArgumentException("Los ocupantes superan la capacidad del apartamento");
        }
        this.codigo = codigo;
        this.titular = titular;
        this.apartamento = apartamento;
        this.ocupantes = new ArrayList<>(ocupantes);
        this.entrada = entrada;
        this.salida = salida;
        this.valorCongelado = valorCongelado;
        this.politica = politica;
        this.creadaEn = creadaEn;
        if (valorCongelado > 0) {
            folio.agregarCargo(new Cargo(TipoCargo.ALOJAMIENTO, valorCongelado, "Alojamiento " + apartamento.getCodigo() + " " + entrada + " a " + salida));
        }
    }

    public abstract boolean requiereAnticipo();
    public abstract long calcularComision();
    public abstract String getCanal();

    public int calcularNoches() { return (int) ChronoUnit.DAYS.between(entrada, salida); }

    public long anticipoRequerido() { return requiereAnticipo() ? valorCongelado * 30 / 100 : 0; }

    public void cambiarEstado(EstadoReserva nuevo) {
        if (!estado.puedePasarA(nuevo)) {
            throw new IllegalStateException("Cambio de estado no permitido: " + estado + " -> " + nuevo);
        }
        this.estado = nuevo;
    }

    public boolean estaActiva() { return estado.esActiva(); }

    public boolean compartenNoches(LocalDate ini, LocalDate fin) {
        return ini.isBefore(salida) && fin.isAfter(entrada);
    }

    public String getCodigo() { return codigo; }
    public Huesped getTitular() { return titular; }
    public Apartamento getApartamento() { return apartamento; }
    public List<Ocupante> getOcupantes() { return Collections.unmodifiableList(ocupantes); }
    public LocalDate getEntrada() { return entrada; }
    public LocalDate getSalida() { return salida; }
    public EstadoReserva getEstado() { return estado; }
    public long getValorCongelado() { return valorCongelado; }
    public IPoliticaCancelacion getPolitica() { return politica; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
    public Folio getFolio() { return folio; }
}
