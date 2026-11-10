package co.edu.uniquindio.sga.folio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Folio {

    private final List<Movimiento> movimientos = new ArrayList<>();
    private final Set<Movimiento> revertidos = new HashSet<>();

    public void agregarCargo(Cargo cargo) {
        movimientos.add(Objects.requireNonNull(cargo));
    }

    public void agregarPago(Pago pago) {
        movimientos.add(Objects.requireNonNull(pago));
    }

    public Movimiento revertir(Movimiento original) {
        if (!movimientos.contains(original)) {
            throw new IllegalArgumentException("El movimiento no pertenece a este folio");
        }
        if (original.isEsReverso()) {
            throw new IllegalStateException("Un reverso no se puede revertir");
        }
        if (!revertidos.add(original)) {
            throw new IllegalStateException("El movimiento ya fue revertido");
        }
        Movimiento reverso = original.crearReverso();
        movimientos.add(reverso);
        return reverso;
    }

    public List<Movimiento> getMovimientos() { return Collections.unmodifiableList(movimientos); }

    public long totalCargos() {
        return movimientos.stream().filter(m -> m instanceof Cargo).mapToLong(Movimiento::getMonto).sum();
    }

    public long totalPagos() {
        return movimientos.stream().filter(m -> m instanceof Pago).mapToLong(Movimiento::getMonto).sum();
    }

    public long calcularSaldo() { return totalCargos() - totalPagos(); }
}
