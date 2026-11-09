package patrones.creacionales.abstractfactory;

import patrones.creacionales.productos.Tramite;
import patrones.creacionales.prototype.EncuestaSatisfaccion;

/**
 * Patrón Abstract Factory (Creacional):
 * Define la interfaz para la creación de familias de productos afines de atención ciudadana.
 * GRASP: Creador - define la creación coordinada de trámites y encuestas complementarias.
 * SOLID: D (Inversión de Dependencias) - los clientes dependen de esta interfaz abstracta.
 * SOLID: O (Abierto/Cerrado) - permite incorporar nuevas familias de trámites sin alterar clientes.
 */
public interface FabricaAtencion {

    /**
     * Abstract Factory: crea el trámite concreto de una familia.
     */
    Tramite crearTramite();

    /**
     * Abstract Factory: crea el producto complementario de la misma familia.
     */
    EncuestaSatisfaccion crearEncuesta();
}
