package co.edu.uniquindio.sga.folio;

import java.time.LocalDateTime;

public abstract class Movimiento {

    private final LocalDateTime fecha = LocalDateTime.now();
    private final long monto; // pesos colombianos, sin decimales
    private final String descripcion;
    private final boolean esReverso;

    protected Movimiento(long monto, String descripcion, boolean esReverso) {
        if (!esReverso && monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
        this.monto = monto;
        this.descripcion = descripcion;
        this.esReverso = esReverso;
    }

    public abstract Movimiento crearReverso();

    public LocalDateTime getFecha() { return fecha; }
    public long getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }
    public boolean isEsReverso() { return esReverso; }
}
