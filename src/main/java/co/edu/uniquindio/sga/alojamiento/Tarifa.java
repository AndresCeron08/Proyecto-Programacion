package co.edu.uniquindio.sga.alojamiento;

public class Tarifa {

    private final TipoTemporada tipo;
    private final long valorPersonaNoche;

    public Tarifa(TipoTemporada tipo, long valorPersonaNoche) {
        if (tipo == null) throw new IllegalArgumentException("El tipo de temporada es obligatorio");
        if (valorPersonaNoche <= 0) throw new IllegalArgumentException("La tarifa debe ser mayor que cero");
        this.tipo = tipo;
        this.valorPersonaNoche = valorPersonaNoche;
    }

    public TipoTemporada getTipo() { return tipo; }
    public long getValorPersonaNoche() { return valorPersonaNoche; }
}
