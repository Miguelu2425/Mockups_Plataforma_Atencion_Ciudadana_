package patrones.creacionales.productos;

/**
 * Patrón Producto / Interfaz común:
 * Utilizado transversalmente por Factory Method, Abstract Factory, Adapter, Composite y Proxy.
 * GRASP: Polimorfismo - el método procesar() permite tratar cualquier trámite, proxy, grupo o adaptador de forma homogénea.
 * SOLID: I (Segregación de Interfaces) - contrato específico y enfocado que evita forzar métodos innecesarios.
 */
public interface Tramite {

    /**
     * GRASP: Polimorfismo: define la operación común ejecutada polimórficamente por cada tipo de trámite.
     */
    void procesar();
}
