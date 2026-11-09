package patrones.estructurales.bridge;

/**
 * Bridge Concrete Implementor: entrega notificaciones por correo.
 */
public class CanalCorreo implements CanalNotificacion {

    @Override
    /**
     * Concrete Implementor: realiza la entrega usando el canal correo.
     */
    public void enviar(String mensaje) {
        System.out.println("Enviando correo: " + mensaje);
    }
}
