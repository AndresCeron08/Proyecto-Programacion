package co.edu.uniquindio.sga;

import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.alojamiento.TipoTemporada;
import co.edu.uniquindio.sga.personas.Huesped;
import co.edu.uniquindio.sga.personas.Ocupante;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public final class Fixtures {

    public static final ZoneId ZONA = ZoneId.systemDefault();
    public static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 5, 9, 0);
    public static final LocalDate ENTRADA = LocalDate.of(2026, 12, 20);
    public static final LocalDate SALIDA = LocalDate.of(2026, 12, 23); // 3 noches

    private Fixtures() { }

    public static Apartamento apto() { return apto("A-101", 4); }

    public static Apartamento apto(String codigo, int capacidad) {
        Apartamento a = new Apartamento(codigo, "Apto " + codigo, 2, capacidad, List.of("balcón"));
        a.definirTarifa(TipoTemporada.BAJA, 100_000);
        a.definirTarifa(TipoTemporada.MEDIA, 150_000);
        a.definirTarifa(TipoTemporada.ALTA, 200_000);
        return a;
    }

    public static Huesped huesped() { return huesped("1094000000", "ana"); }

    public static Huesped huesped(String documento, String usuario) {
        return new Huesped("Ana Gómez", LocalDate.of(1990, 3, 10), documento, "3000000000", "ana@correo.com", usuario, usuario == null ? null : "1234");
    }

    public static Ocupante adulto(String nombre) { return new Ocupante(nombre, LocalDate.of(1990, 1, 1)); }

    public static Ocupante nino(String nombre) { return new Ocupante(nombre, LocalDate.of(2022, 5, 1)); }

    public static List<Ocupante> adultos(int n) {
        List<Ocupante> lista = new ArrayList<>();
        for (int i = 1; i <= n; i++) lista.add(adulto("Adulto " + i));
        return lista;
    }

    public static Clock reloj(LocalDateTime momento) {
        return Clock.fixed(momento.atZone(ZONA).toInstant(), ZONA);
    }

    public static class RelojMutable extends Clock {
        private Instant instante;

        public RelojMutable(LocalDateTime inicial) { this.instante = inicial.atZone(ZONA).toInstant(); }

        public void fijar(LocalDateTime momento) { this.instante = momento.atZone(ZONA).toInstant(); }

        @Override public ZoneId getZone() { return ZONA; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instante; }
    }
}
