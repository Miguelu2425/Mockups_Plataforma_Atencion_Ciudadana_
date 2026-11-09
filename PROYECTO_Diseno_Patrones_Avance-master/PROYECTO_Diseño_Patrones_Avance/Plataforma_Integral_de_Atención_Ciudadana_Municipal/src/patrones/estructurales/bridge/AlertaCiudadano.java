package patrones.estructurales.bridge;

/**
 * Bridge Abstraction: representa una alerta independiente del canal de envío.
 */
public class AlertaCiudadano extends NotificacionBridge {

    /**
     * Bridge: recibe el Implementor que realizará el envío.
     */
    public AlertaCiudadano(CanalNotificacion canal) {
        super(canal);
    }

    @Override
    /**
     * Bridge Abstraction: agrega el contexto de alerta y delega en el canal.
     */
    public void enviar(String mensaje) {
        canal.enviar("Alerta municipal: " + mensaje);
    }
}
