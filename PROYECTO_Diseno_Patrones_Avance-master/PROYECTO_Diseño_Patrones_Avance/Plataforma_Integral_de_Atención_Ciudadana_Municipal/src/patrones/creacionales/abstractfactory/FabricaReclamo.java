package patrones.creacionales.abstractfactory;

import patrones.creacionales.productos.Reclamo;
import patrones.creacionales.productos.Tramite;
import patrones.creacionales.prototype.EncuestaSatisfaccion;

/**
 * Abstract Factory: fábrica concreta de la familia de reclamos.
 */
public class FabricaReclamo implements FabricaAtencion {

    /**
     * Abstract Factory: crea el producto trámite Reclamo.
     */
    @Override
    public Tramite crearTramite() {
        return new Reclamo();
    }

    /**
     * Abstract Factory: crea la encuesta asociada a la familia de reclamos.
     */
    @Override
    public EncuestaSatisfaccion crearEncuesta() {
        return new EncuestaSatisfaccion("Encuesta de satisfacción del reclamo");
    }
}
