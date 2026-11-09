package patrones.comportamiento.observer;

/**
 * Patrón Observer (Observador):
 * Define el contrato de suscripción para los interesados en eventos y transiciones de estado de trámites.
 * GRASP: Polimorfismo - el método actualizar() es resuelto polimórficamente por ObservadorConsola, ObservadorCiudadano y ObservadorEncuesta.
 * SOLID: D (Inversión de Dependencias) - el sujeto emisor depende de esta abstracción y no de sus observadores concretos.
 * SOLID: O (Abierto/Cerrado) - permite conectar nuevos observadores sin alterar las clases emisoras de eventos.
 */
public interface ObservadorTramite {

    /**
     * Patron Observer / GRASP: Polimorfismo:
     * Actualiza el observador con el cambio de estado o evento acontecido.
     *
     * @param cambio datos del cambio de estado o reversión
     */
    void actualizar(CambioEstado cambio);
}
