package patrones.creacionales.productos;

/**
 * Producto concreto de Factory y Abstract Factory para reclamos.
 */
public class Reclamo implements Tramite {

    @Override
    /**
     * Producto concreto: procesa un reclamo ciudadano.
     */
    public void procesar() {
        System.out.println("Procesando reclamo ciudadano...");
    }
}
