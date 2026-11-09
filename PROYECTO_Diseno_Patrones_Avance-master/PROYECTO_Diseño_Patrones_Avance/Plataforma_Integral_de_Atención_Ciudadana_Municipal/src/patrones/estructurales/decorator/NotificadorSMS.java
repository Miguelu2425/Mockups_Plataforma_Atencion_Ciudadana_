package patrones.estructurales.decorator;

/**
 * Concrete Decorator: añade el envío adicional por SMS.
 */
public class NotificadorSMS extends NotificadorDecorator {

    /**
     * Concrete Decorator: configura el componente que será decorado.
     */
    public NotificadorSMS(Notificador notificadorDecorado) {
        super(notificadorDecorado);
    }

    @Override
    /**
     * Concrete Decorator: conserva el envío base y añade el SMS.
     */
    public void enviar(String mensaje) {
        super.enviar(mensaje);
        System.out.println("Enviando copia por SMS al celular del ciudadano...");
    }
}
