package co.edu.uniquindio.sga.alojamiento;

import java.time.LocalDate;

public class Temporada {

    private final TipoTemporada tipo;
    private final LocalDate desde;
    private final LocalDate hasta;

    public Temporada(TipoTemporada tipo, LocalDate desde, LocalDate hasta) {
        if (tipo == null || tipo == TipoTemporada.BAJA) {
            throw new IllegalArgumentException("Solo se definen rangos para temporada MEDIA o ALTA");
        }
        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            throw new IllegalArgumentException("Rango de fechas inválido");
        }
        this.tipo = tipo;
        this.desde = desde;
        this.hasta = hasta;
    }

    public TipoTemporada getTipo() { return tipo; }
    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }

    public boolean contiene(LocalDate noche) {
        return !noche.isBefore(desde) && !noche.isAfter(hasta);
    }
}
