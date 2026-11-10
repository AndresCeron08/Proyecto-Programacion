package co.edu.uniquindio.sga.reservas;

import java.time.LocalTime;

public final class Horarios {
    public static final LocalTime HORA_SALIDA = LocalTime.of(11, 0);
    public static final LocalTime HORA_ENTRADA = LocalTime.of(15, 0);
    public static final LocalTime LIMITE_NO_SHOW = LocalTime.of(22, 0);
    public static final int HORAS_MINIMAS_PREPARACION = 3;
    public static final int HORAS_PLAZO_CONFIRMACION = 24;

    private Horarios() { }
}
