package patrones.estructurales.decorator;

/**
 * Concrete Component: envío básico que será ampliado por un Decorator.
 */
public class NotificadorBase implements Notificador {

    @Override
    /**
     * Concrete Component: realiza la notificación principal.
     */
    public void enviar(String mensaje) {
        System.out.println("Enviando notificacion por plataforma: " + mensaje);
    }
}
