package model.interfaces;

import model.enums.RolUsuario;

/** Contrato comun para las cuentas de huespedes y empleados. */
public interface Usuario {
    String getDocumento();
    boolean validarContrasena(String contrasena);
    void cambiarContrasena(String contrasena);
    boolean tieneContrasena();
    RolUsuario getRolUsuario();
}
