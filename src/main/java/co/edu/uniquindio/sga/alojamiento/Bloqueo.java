package co.edu.uniquindio.sga.alojamiento;

import java.time.LocalDate;

public class Bloqueo {

    private final LocalDate desde;
    private final LocalDate hasta;
    private final String motivo;

    public Bloqueo(LocalDate desde, LocalDate hasta, String motivo) {
        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            throw new IllegalArgumentException("Rango de bloqueo inválido");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo del bloqueo es obligatorio");
        }
        this.desde = desde;
        this.hasta = hasta;
        this.motivo = motivo;
    }

    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }
    public String getMotivo() { return motivo; }

    public boolean cubre(LocalDate noche) {
        return !noche.isBefore(desde) && !noche.isAfter(hasta);
    }

    public boolean choca(LocalDate entrada, LocalDate salida) {
        return !entrada.isAfter(hasta) && salida.isAfter(desde);
    }
}
