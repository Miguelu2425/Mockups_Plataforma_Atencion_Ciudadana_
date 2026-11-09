package patrones.comportamiento.state;

import patrones.comportamiento.memento.MementoSeguimiento;
import patrones.comportamiento.observer.CambioEstado;
import patrones.comportamiento.observer.ObservadorTramite;
import patrones.creacionales.builder.Expediente;

import java.util.ArrayList;
import java.util.List;

/**
 * Patrón State (Contexto), Patrón Observer (Sujeto) y Patrón Memento (Originator):
 * Administra el estado actual del expediente, coordina la notificación a observadores
 * y genera/restaura instantáneas de su estado interno.
 * GRASP: Experto en Información - conoce el expediente, su estado vigente y las reglas de avance.
 * SOLID: S (Responsabilidad Única) - centraliza el control del ciclo de vida del trámite.
 */
public class SeguimientoTramite {

    private final Expediente expediente;
    private EstadoTramite estado;
    private final List<ObservadorTramite> observadores = new ArrayList<>();

    /**
     * Patron State: inicializa el seguimiento con el estado del expediente provisto.
     *
     * @param expediente expediente a seguir
     */
    public SeguimientoTramite(Expediente expediente) {
        this.expediente = expediente;
        this.estado = EstadosTramite.desdeNombre(expediente.getEstado());
    }

    /**
     * Patron State: devuelve el estado actual.
     *
     * @return estado actual
     */
    public EstadoTramite getEstado() {
        return estado;
    }

    /**
     * Patron State: devuelve el expediente asociado.
     *
     * @return expediente
     */
    public Expediente getExpediente() {
        return expediente;
    }

    /**
     * Patron Memento: crea una instantanea inmutable del estado actual.
     *
     * @return memento con el estado y codigo actuales
     */
    public MementoSeguimiento guardar() {
        MementoSeguimiento memento = new MementoSeguimiento(
                expediente.getCodigo(),
                estado.getNombre()
        );
        System.out.println("[Memento] Instantanea guardada: " + expediente.getCodigo()
                + " estado=" + estado.getNombre());
        return memento;
    }

    /**
     * Patron Memento: restaura el estado interno a partir de una instantanea
     * sin notificar a los observadores (la coordinacion de la notificacion
     * corresponde al comando que ejecuta la reversion).
     *
     * @param m instantanea a restaurar
     * @throws IllegalArgumentException si el memento es nulo o no pertenece al expediente
     */
    public void restaurar(MementoSeguimiento m) {
        if (m == null) {
            throw new IllegalArgumentException("El memento no puede ser nulo.");
        }
        if (!expediente.getCodigo().equals(m.getCodigo())) {
            throw new IllegalArgumentException("El memento pertenece al expediente "
                    + m.getCodigo() + ", no a " + expediente.getCodigo());
        }
        this.estado = EstadosTramite.desdeNombre(m.getNombreEstado());
        System.out.println("[Memento] Estado restaurado: " + expediente.getCodigo()
                + " estado=" + estado.getNombre());
    }

    /**
     * Patron State: consulta el siguiente estado sin modificar el estado actual.
     *
     * @return siguiente estado segun el patron State
     */
    public EstadoTramite proximoEstado() {
        return estado.siguiente();
    }

    /**
     * Patron Observer: registra un observador interesado en cambios de estado.
     *
     * @param o observador a agregar
     */
    public void agregarObservador(ObservadorTramite o) {
        if (o != null) {
            observadores.add(o);
        }
    }

    /**
     * Patron State y Observer:
     * Si el estado es final, imprime el aviso y retorna false.
     * Si no, cambia al siguiente estado, notifica a todos los observadores y retorna true.
     * Captura cualquier excepcion en un observador para asegurar que los demas se notifiquen.
     *
     * @return true si la transicion ocurrio; false si el tramite ya estaba Cerrado
     */
    public boolean avanzar() {
        if (estado.esFinal()) {
            System.out.println("Mensaje: el tramite " + expediente.getCodigo() + " ya esta Cerrado");
            return false;
        }

        EstadoTramite anterior = this.estado;
        this.estado = this.estado.siguiente();

        CambioEstado cambio = new CambioEstado(
                expediente.getCodigo(),
                anterior.getNombre(),
                this.estado.getNombre(),
                expediente.getNombreCiudadano(),
                expediente.getCorreo(),
                expediente.getTipoDocumento()
        );

        for (ObservadorTramite o : observadores) {
            try {
                o.actualizar(cambio);
            } catch (Exception ex) {
                System.err.println("Advertencia: observador fallo: " + ex.getMessage());
            }
        }
        return true;
    }
}