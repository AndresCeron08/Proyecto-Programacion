package co.edu.uniquindio.sga.reservas;

import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.cancelacion.IPoliticaCancelacion;
import co.edu.uniquindio.sga.personas.Huesped;
import co.edu.uniquindio.sga.personas.Ocupante;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReservaPortal extends Reserva {

    public ReservaPortal(String codigo, Huesped titular, Apartamento apartamento, List<Ocupante> ocupantes,
                  LocalDate entrada, LocalDate salida, long valorCongelado,
                  IPoliticaCancelacion politica, LocalDateTime creadaEn) {
        super(codigo, titular, apartamento, ocupantes, entrada, salida, valorCongelado, politica, creadaEn);
    }

    @Override
    public boolean requiereAnticipo() { return true; }

    @Override
    public long calcularComision() { return 0; }

    @Override
    public String getCanal() { return "PORTAL"; }
}
