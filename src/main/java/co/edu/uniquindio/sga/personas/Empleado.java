package co.edu.uniquindio.sga.personas;

import java.time.LocalDate;


public abstract class Empleado extends Persona implements IAutenticable {

    private String usuario;
    private String clave;
    private boolean activo = true;

    protected Empleado(String nombre, LocalDate fechaNacimiento, String usuario, String clave) {
        super(nombre, fechaNacimiento);
        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("Usuario y clave son obligatorios");
        }
        this.usuario = usuario;
        this.clave = clave;
    }


    public abstract String getRol();

    public String getUsuario() { return usuario; }
    public void cambiarClave(String nueva) {
        if (nueva == null || nueva.isBlank()) throw new IllegalArgumentException("Clave inválida");
        this.clave = nueva;
    }

    public boolean isActivo() { return activo; }
    public void desvincular() { this.activo = false; }

    @Override
    public boolean autenticar(String usuario, String clave) {
        return activo && this.usuario.equals(usuario) && this.clave.equals(clave);
    }
}
