package patrones.comportamiento.command;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Patrón Command (Invocador): gestiona la ejecución y la pila de operaciones reversibles.
 * Mantiene un límite máximo de comandos en la pila de deshacer para proteger la memoria.
 * GRASP Fabricación Pura: clase técnica que desacopla la invocación de las acciones del dominio.
 */
public class InvocadorComandos {

    private static final int LIMITE_PILA = 20;
    private final Deque<Comando> pilaDeshacer = new ArrayDeque<>();

    /**
     * Patrón Command: ejecuta el comando, imprime la confirmación y lo apila
     * para permitir su posterior reversión si la ejecución finalizó exitosamente
     * y representa una acción reversible.
     *
     * @param comando comando a ejecutar
     */
    public synchronized void ejecutar(Comando comando) {
        if (comando == null) return;

        comando.ejecutar();
        System.out.println("[Command] Ejecutado: " + comando.getDescripcion());

        // Si es un comando de avance pero no hubo cambio real (ej. ya en Cerrado), no es deshacible
        if (comando instanceof AvanzarEstadoComando) {
            AvanzarEstadoComando avance = (AvanzarEstadoComando) comando;
            if (!avance.isHuboCambio()) {
                return;
            }
        }

        if (pilaDeshacer.size() >= LIMITE_PILA) {
            pilaDeshacer.removeLast(); // Descarta el comando mas antiguo
        }
        pilaDeshacer.push(comando);
    }

    /**
     * Patrón Command: desapila y deshace la última acción ejecutada.
     * Si la operación de deshacer falla, reapila el comando y propaga la excepción.
     *
     * @return descripción del comando deshecho o null si la pila estaba vacía
     */
    public synchronized String deshacerUltimo() {
        if (pilaDeshacer.isEmpty()) {
            System.out.println("[Command] No hay acciones para deshacer");
            return null;
        }

        Comando comando = pilaDeshacer.pop();
        try {
            comando.deshacer();
            String descripcion = comando.getDescripcion();
            System.out.println("[Command] Deshecho: " + descripcion);
            return descripcion;
        } catch (RuntimeException ex) {
            // Si el undo falla, reapilar para mantener consistencia
            pilaDeshacer.push(comando);
            throw ex;
        }
    }

    /**
     * Patrón Command: verifica si existen acciones en la pila listas para deshacer.
     *
     * @return true si hay al menos una acción disponible
     */
    public synchronized boolean hayAccionesParaDeshacer() {
        return !pilaDeshacer.isEmpty();
    }

    /**
     * Patrón Command: retorna la descripción legible de la última acción ejecutable a deshacer.
     *
     * @return descripción de la acción o null si no hay ninguna
     */
    public synchronized String descripcionUltimaAccion() {
        if (pilaDeshacer.isEmpty()) {
            return null;
        }
        return pilaDeshacer.peek().getDescripcion();
    }

    /**
     * Patrón Command: lista las descripciones de todas las acciones pendientes en la pila
     * de deshacer, desde la más reciente hasta la más antigua.
     *
     * @return lista inmutable o copia con las descripciones de las acciones en pila
     */
    public synchronized List<String> historial() {
        List<String> lista = new ArrayList<>();
        for (Comando c : pilaDeshacer) {
            lista.add(c.getDescripcion());
        }
        return lista;
    }
}

