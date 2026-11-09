package patrones.comportamiento.state;

/**
 * Patrón State (Comportamiento):
 * Interfaz que define el contrato y transiciones de estado de un trámite municipal.
 * GRASP: Experto en Información - cada estado conoce sus atributos y su condición terminal.
 * GRASP: Polimorfismo - delega en cada estado concreto la resolución polimórfica de la transición siguiente.
 * SOLID: O (Abierto/Cerrado) - permite añadir nuevos estados del ciclo de vida sin modificar las clases existentes.
 */
public interface EstadoTramite {

    /**
     * Patron State: devuelve el nombre oficial del estado ("En Revision", "Atendido", "Cerrado").
     *
     * @return nombre oficial del estado
     */
    String getNombre();

    /**
     * Patron State: descripcion legible de la fase actual del tramite.
     *
     * @return descripcion de la fase
     */
    String getDescripcion();

    /**
     * Patron State / GRASP: Polimorfismo:
     * Calcula la siguiente transición en el ciclo de vida del trámite según el estado concreto.
     *
     * @return proximo estado
     */
    EstadoTramite siguiente();

    /**
     * Patron State: indica si este estado es el terminal del ciclo de vida.
     *
     * @return true si es estado final, false si admite transiciones
     */
    boolean esFinal();
}
