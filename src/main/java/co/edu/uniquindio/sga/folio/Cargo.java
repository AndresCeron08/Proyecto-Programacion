package co.edu.uniquindio.sga.folio;

public class Cargo extends Movimiento {

    private final TipoCargo tipo;

    public Cargo(TipoCargo tipo, long monto, String descripcion) {
        super(monto, descripcion, false);
        this.tipo = tipo;
    }

    private Cargo(TipoCargo tipo, long monto, String descripcion, boolean reverso) {
        super(monto, descripcion, reverso);
        this.tipo = tipo;
    }

    public TipoCargo getTipo() { return tipo; }

    @Override
    public Movimiento crearReverso() {
        return new Cargo(tipo, -getMonto(), "Reverso: " + getDescripcion(), true);
    }
}
