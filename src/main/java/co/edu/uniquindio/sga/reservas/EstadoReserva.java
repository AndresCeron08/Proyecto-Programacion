package co.edu.uniquindio.sga.reservas;

public enum EstadoReserva {
    PENDIENTE, CONFIRMADA, EN_CURSO, FINALIZADA, CANCELADA, NO_SHOW;

    public boolean puedePasarA(EstadoReserva destino) {
        return switch (this) {
            case PENDIENTE -> destino == CONFIRMADA || destino == CANCELADA;
            case CONFIRMADA -> destino == EN_CURSO || destino == CANCELADA || destino == NO_SHOW;
            case EN_CURSO -> destino == FINALIZADA;
            default -> false;
        };
    }

    public boolean esFinal() {
        return this == FINALIZADA || this == CANCELADA || this == NO_SHOW;
    }

    public boolean esActiva() { return !esFinal(); }
}
