package co.edu.uniquindio.sga.personas;

import java.time.LocalDate;
import java.time.Period;

public abstract class Persona {

    public static final int EDAD_MINIMA_PAGO = 6;

    private String nombre;
    private LocalDate fechaNacimiento;

    protected Persona(String nombre, LocalDate fechaNacimiento) {
        setNombre(nombre);
        setFechaNacimiento(fechaNacimiento);
    }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        this.fechaNacimiento = fechaNacimiento;
    }

    /** Edad en años cumplidos a la fecha indicada. */
    public int calcularEdad(LocalDate fecha) {
        if (fecha.isBefore(fechaNacimiento)) {
            throw new IllegalArgumentException("La fecha es anterior al nacimiento");
        }
        return Period.between(fechaNacimiento, fecha).getYears();
    }

    /** Los menores de 6 años ocupan cupo pero no pagan. */
    public boolean paga(LocalDate fechaEntrada) {
        return calcularEdad(fechaEntrada) >= EDAD_MINIMA_PAGO;
    }
}
