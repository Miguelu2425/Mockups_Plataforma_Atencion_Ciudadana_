package patrones.estructurales.bridge;

/**
 * Bridge Concrete Implementor: entrega notificaciones por SMS.
 */
public class CanalSMS implements CanalNotificacion {

    @Override
    /**
     * Concrete Implementor: realiza la entrega usando el canal SMS.
     */
    public void enviar(String mensaje) {
        System.out.println("Enviando SMS: " + mensaje);
    }
}
