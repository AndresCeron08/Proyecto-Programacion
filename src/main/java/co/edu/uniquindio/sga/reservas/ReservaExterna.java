package co.edu.uniquindio.sga.reservas;

import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.cancelacion.IPoliticaCancelacion;
import co.edu.uniquindio.sga.personas.Huesped;
import co.edu.uniquindio.sga.personas.Ocupante;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReservaExterna extends Reserva {

    public static final int COMISION_PORCENTAJE = 15;

    private final String codigoExterno;

    public ReservaExterna(String codigo, String codigoExterno, Huesped titular, Apartamento apartamento, List<Ocupante> ocupantes, LocalDate entrada, LocalDate salida, long valorCongelado, IPoliticaCancelacion politica, LocalDateTime creadaEn) {
        super(codigo, titular, apartamento, ocupantes, entrada, salida, valorCongelado, politica, creadaEn);
        if (codigoExterno == null || codigoExterno.isBlank()) {
            throw new IllegalArgumentException("El código externo es obligatorio");
        }
        this.codigoExterno = codigoExterno;
    }

    public String getCodigoExterno() { return codigoExterno; }

    @Override
    public boolean requiereAnticipo() { return false; }

    @Override
    public long calcularComision() { return getValorCongelado() * COMISION_PORCENTAJE / 100; }

    @Override
    public String getCanal() { return "EXTERNA"; }
}
