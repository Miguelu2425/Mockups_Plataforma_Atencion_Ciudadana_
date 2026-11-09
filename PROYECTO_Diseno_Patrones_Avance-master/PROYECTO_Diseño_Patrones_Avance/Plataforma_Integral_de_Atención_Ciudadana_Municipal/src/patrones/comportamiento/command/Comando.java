package patrones.comportamiento.command;

/**
 * Patrón Command (Comportamiento):
 * Encapsula una solicitud u operación como un objeto desacoplando al invocador del receptor.
 * Proporciona soporte uniforme para operaciones reversibles (deshacer / Undo).
 * GRASP: Polimorfismo - los métodos ejecutar() y deshacer() se invocan polimórficamente sobre cualquier comando.
 * SOLID: I (Segregación de Interfaces) - contrato mínimo y cohesivo para acciones reversibles.
 */
public interface Comando {

    /**
     * Patrón Command / GRASP: Polimorfismo: ejecuta la operación encapsulada.
     */
    void ejecutar();

    /**
     * Patrón Command / GRASP: Polimorfismo: revierte la operación realizada durante la ejecución.
     */
    void deshacer();

    /**
     * Patrón Command: retorna una descripción legible de la acción.
     *
     * @return descripción de la acción para trazas e historial
     */
    String getDescripcion();
}
