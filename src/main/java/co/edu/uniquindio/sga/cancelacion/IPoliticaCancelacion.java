package co.edu.uniquindio.sga.cancelacion;

import co.edu.uniquindio.sga.reservas.Reserva;

import java.time.LocalDateTime;

public interface IPoliticaCancelacion {
    long calcularRetencion(Reserva reserva, LocalDateTime cuando);
}
