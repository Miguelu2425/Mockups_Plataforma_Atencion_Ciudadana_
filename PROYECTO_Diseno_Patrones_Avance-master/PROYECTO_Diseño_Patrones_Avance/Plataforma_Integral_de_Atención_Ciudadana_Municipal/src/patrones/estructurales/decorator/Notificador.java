package patrones.estructurales.decorator;

/**
 * Patrón Decorator (Component):
 * Contrato base para el mecanismo extensible de notificaciones al ciudadano.
 * GRASP: Polimorfismo - define la operación enviar() ampliada polimórficamente por los decoradores.
 * SOLID: O (Abierto/Cerrado) - permite añadir nuevas vías de notificación en tiempo de ejecución sin alterar el componente base.
 */
public interface Notificador {

    /**
     * Component / GRASP: Polimorfismo: define el envío polimórfico de mensajes.
     *
     * @param mensaje texto a notificar
     */
    void enviar(String mensaje);
}
