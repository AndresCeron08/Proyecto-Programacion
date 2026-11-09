package co.edu.uniquindio.sga.alojamiento;

import java.time.LocalDateTime;

public class Novedad {

    private final LocalDateTime fecha;
    private final String descripcion;

    public Novedad(LocalDateTime fecha, String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    public LocalDateTime getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
}
