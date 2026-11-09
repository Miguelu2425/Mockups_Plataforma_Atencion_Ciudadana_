package patrones.estructurales.decorator;

/**
 * Decorator: conserva un Component y permite añadir comportamiento.
 */
public abstract class NotificadorDecorator implements Notificador {

    protected Notificador notificadorDecorado;

    /**
     * Decorator: recibe el componente que será envuelto.
     */
    public NotificadorDecorator(Notificador notificadorDecorado) {
        this.notificadorDecorado = notificadorDecorado;
    }

    /**
     * Decorator: delega el comportamiento base al componente envuelto.
     */
    public void enviar(String mensaje) {
        notificadorDecorado.enviar(mensaje);
    }
}
