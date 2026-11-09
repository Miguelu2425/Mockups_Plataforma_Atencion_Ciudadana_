package patrones.creacionales.productos;

/**
 * Producto concreto de Factory y Abstract Factory para solicitudes.
 */
public class Solicitud implements Tramite {

    @Override
    /**
     * Producto concreto: procesa una solicitud ciudadana.
     */
    public void procesar() {
        System.out.println("Procesando solicitud de servicio...");
    }
}
