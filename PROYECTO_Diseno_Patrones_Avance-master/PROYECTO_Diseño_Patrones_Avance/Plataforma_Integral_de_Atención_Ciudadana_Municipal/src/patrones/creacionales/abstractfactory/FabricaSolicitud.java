package patrones.creacionales.abstractfactory;

import patrones.creacionales.productos.Solicitud;
import patrones.creacionales.productos.Tramite;
import patrones.creacionales.prototype.EncuestaSatisfaccion;

/**
 * Abstract Factory: fábrica concreta de la familia de solicitudes.
 */
public class FabricaSolicitud implements FabricaAtencion {

    /**
     * Abstract Factory: crea el producto trámite Solicitud.
     */
    @Override
    public Tramite crearTramite() {
        return new Solicitud();
    }

    /**
     * Abstract Factory: crea la encuesta asociada a la familia de solicitudes.
     */
    @Override
    public EncuestaSatisfaccion crearEncuesta() {
        return new EncuestaSatisfaccion("Encuesta de satisfacción de la solicitud");
    }
}
