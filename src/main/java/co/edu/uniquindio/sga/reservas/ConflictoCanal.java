package co.edu.uniquindio.sga.reservas;

import java.time.LocalDateTime;

public class ConflictoCanal {

    private final String codigoExterno;
    private final LocalDateTime fecha;
    private final String motivo;

    public ConflictoCanal(String codigoExterno, LocalDateTime fecha, String motivo) {
        this.codigoExterno = codigoExterno;
        this.fecha = fecha;
        this.motivo = motivo;
    }

    public String getCodigoExterno() { return codigoExterno; }
    public LocalDateTime getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
}
