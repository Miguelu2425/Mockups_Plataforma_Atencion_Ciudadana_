package patrones.estructurales.bridge;

/**
 * Patrón Bridge (Implementor):
 * Define el contrato común para la dimensión de canales de notificación.
 * GRASP: Polimorfismo - el método enviar() es implementado polimórficamente por CanalCorreo y CanalSMS.
 * SOLID: D (Inversión de Dependencias) - la jerarquía de alertas depende de esta abstracción.
 */
public interface CanalNotificacion {

    /**
     * Bridge Implementor / GRASP: Polimorfismo: entrega el mensaje mediante el canal concreto.
     *
     * @param mensaje texto a emitir
     */
    void enviar(String mensaje);
}
