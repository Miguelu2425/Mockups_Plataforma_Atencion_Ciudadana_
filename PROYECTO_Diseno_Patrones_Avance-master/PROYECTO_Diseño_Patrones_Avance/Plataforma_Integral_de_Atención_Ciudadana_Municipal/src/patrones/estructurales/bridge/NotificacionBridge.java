package patrones.estructurales.bridge;

/**
 * Bridge Abstraction: mantiene una referencia al canal Implementor.
 */
public abstract class NotificacionBridge {

    protected final CanalNotificacion canal;

    /**
     * Bridge Abstraction: conecta la alerta con su Implementor de canal.
     */
    protected NotificacionBridge(CanalNotificacion canal) {
        this.canal = canal;
    }

    /**
     * Bridge Abstraction: operación que las alertas concretas especializan.
     */
    public abstract void enviar(String mensaje);
}
