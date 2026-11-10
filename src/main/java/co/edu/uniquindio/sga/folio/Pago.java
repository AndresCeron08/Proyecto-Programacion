package co.edu.uniquindio.sga.folio;

public class Pago extends Movimiento {

    private final MedioPago medio;

    public Pago(MedioPago medio, long monto, String descripcion) {
        super(monto, descripcion, false);
        this.medio = medio;
    }

    private Pago(MedioPago medio, long monto, String descripcion, boolean reverso) {
        super(monto, descripcion, reverso);
        this.medio = medio;
    }

    public MedioPago getMedio() { return medio; }

    @Override
    public Movimiento crearReverso() {
        return new Pago(medio, -getMonto(), "Reverso: " + getDescripcion(), true);
    }
}
