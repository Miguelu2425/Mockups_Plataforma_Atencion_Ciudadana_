package patrones.comportamiento.memento;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Patrón Memento (Caretaker): administra el historial de instantáneas (mementos)
 * por código de expediente, permitiendo almacenar y recuperar estados previos
 * con límite de capacidad para prevenir consumo excesivo de memoria.
 * GRASP Fabricación Pura: clase técnica de soporte desacoplada del dominio.
 */
public class HistorialMementos {

    private static final int LIMITE_POR_EXPEDIENTE = 20;
    private final Map<String, Deque<MementoSeguimiento>> historial = new HashMap<>();

    /**
     * Patrón Memento: guarda una instantánea en la pila correspondiente a su expediente.
     * Si supera el límite de 20 mementos, descarta el más antiguo.
     *
     * @param memento instantánea a guardar
     */
    public synchronized void guardar(MementoSeguimiento memento) {
        if (memento == null) return;
        Deque<MementoSeguimiento> pila = historial.computeIfAbsent(
                memento.getCodigo(), k -> new ArrayDeque<>()
        );
        if (pila.size() >= LIMITE_POR_EXPEDIENTE) {
            pila.removeLast();
        }
        pila.push(memento);
    }

    /**
     * Patrón Memento: recupera y remueve la última instantánea guardada para el expediente.
     *
     * @param codigo código del expediente
     * @return última instantánea guardada o null si no hay ninguna
     */
    public synchronized MementoSeguimiento recuperar(String codigo) {
        if (codigo == null) return null;
        Deque<MementoSeguimiento> pila = historial.get(codigo);
        if (pila == null || pila.isEmpty()) {
            return null;
        }
        return pila.pop();
    }

    /**
     * Patrón Memento: verifica si existe al menos una instantánea guardada para el expediente.
     *
     * @param codigo código del expediente
     * @return true si hay mementos disponibles para recuperar
     */
    public synchronized boolean hayMemento(String codigo) {
        if (codigo == null) return false;
        Deque<MementoSeguimiento> pila = historial.get(codigo);
        return pila != null && !pila.isEmpty();
    }

    /**
     * Patrón Memento: limpia el historial de instantáneas de un expediente (ej. al ser eliminado).
     *
     * @param codigo código del expediente
     */
    public synchronized void limpiar(String codigo) {
        if (codigo != null) {
            historial.remove(codigo);
        }
    }
}

