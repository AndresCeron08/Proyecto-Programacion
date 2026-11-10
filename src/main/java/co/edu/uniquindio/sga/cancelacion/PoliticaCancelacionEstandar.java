package co.edu.uniquindio.sga.cancelacion;

import co.edu.uniquindio.sga.reservas.EstadoReserva;
import co.edu.uniquindio.sga.reservas.Horarios;
import co.edu.uniquindio.sga.reservas.Reserva;

import java.time.Duration;
import java.time.LocalDateTime;

public class PoliticaCancelacionEstandar implements IPoliticaCancelacion {

    private final int diasReembolsoTotal;
    private final int horasSinReembolso;

    public PoliticaCancelacionEstandar() { this(7, 48); }

    public PoliticaCancelacionEstandar(int diasReembolsoTotal, int horasSinReembolso) {
        this.diasReembolsoTotal = diasReembolsoTotal;
        this.horasSinReembolso = horasSinReembolso;
    }

    @Override
    public long calcularRetencion(Reserva reserva, LocalDateTime cuando) {
        long valor = reserva.getValorCongelado();
        if (reserva.getEstado() == EstadoReserva.NO_SHOW) {
            return valor;
        }
        long horas = Duration.between(cuando, reserva.getEntrada().atTime(Horarios.HORA_ENTRADA)).toHours();
        if (horas < horasSinReembolso) return valor;
        if (horas > diasReembolsoTotal * 24L) return 0;
        return valor * 50 / 100;
    }
}
