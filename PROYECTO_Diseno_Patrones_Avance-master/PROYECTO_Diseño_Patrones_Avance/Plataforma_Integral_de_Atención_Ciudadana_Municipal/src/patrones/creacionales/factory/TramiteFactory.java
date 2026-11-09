package patrones.creacionales.factory;

import patrones.creacionales.productos.Reclamo;
import patrones.creacionales.productos.Solicitud;
import patrones.creacionales.productos.Tramite;

/**
 * Patrón Factory Method (Creacional):
 * GRASP: Creador - asigna la responsabilidad de instanciar productos concretos de Tramite (Reclamo, Solicitud).
 * SOLID: O (Abierto/Cerrado) - centraliza la creación encapsulando las clases concretas de productos.
 */
public class TramiteFactory {

    /**
     * Factory: selecciona y construye el producto concreto solicitado.
     */
    public static Tramite crearTramite(String tipo) {
        if (tipo.equalsIgnoreCase("reclamo")) {
            return new Reclamo();
        }
        if (tipo.equalsIgnoreCase("solicitud")) {
            return new Solicitud();
        }
        throw new IllegalArgumentException("Tipo de tramite desconocido");
    }
}
