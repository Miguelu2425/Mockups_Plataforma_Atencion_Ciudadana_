package patrones.comportamiento.command;

import patrones.comportamiento.memento.HistorialMementos;
import patrones.comportamiento.memento.MementoSeguimiento;
import patrones.comportamiento.observer.CambioEstado;
import patrones.comportamiento.observer.ObservadorTramite;
import patrones.comportamiento.state.SeguimientoTramite;
import patrones.creacionales.builder.Expediente;
import repositorios.TramiteRepositorio;

import java.util.ArrayList;
import java.util.List;

/**
 * Patrón Command y Memento: encapsula la acción de avanzar el estado de un trámite.
 * Antes de avanzar, captura una instantánea inmutable (Memento) y la registra
 * en el Caretaker (HistorialMementos). Al deshacer, recupera el Memento,
 * valida el estado actual en el repositorio y revierte el cambio notificando
 * a los observadores con la bandera de reversión activa.
 */
public class AvanzarEstadoComando implements Comando {

    private final TramiteRepositorio repositorio;
    private final String codigo;
    private final HistorialMementos historialMementos;
    private final List<ObservadorTramite> observadores = new ArrayList<>();

    private boolean huboCambio = false;
    private String estadoAnterior;
    private String estadoNuevo;
    private Expediente expedienteActualizado;

    /**
     * Patrón Command: constructor para avanzar el estado de un expediente.
     *
     * @param repositorio       repositorio de persistencia
     * @param codigo            código único del expediente
     * @param historialMementos administrador de instantáneas Memento
     * @param observadores      lista de observadores suscritos
     */
    public AvanzarEstadoComando(TramiteRepositorio repositorio,
                                String codigo,
                                HistorialMementos historialMementos,
                                List<ObservadorTramite> observadores) {
        this.repositorio = repositorio;
        this.codigo = codigo;
        this.historialMementos = historialMementos;
        if (observadores != null) {
            this.observadores.addAll(observadores);
        }
    }

    /**
     * Patrón Command: ejecuta el avance de estado coordinando State, Memento y Observer.
     * Si el trámite ya está en estado final, no realiza cambios ni permite deshacer.
     */
    @Override
    public void ejecutar() {
        Expediente expediente = repositorio.buscarPorCodigo(codigo);
        SeguimientoTramite seguimiento = new SeguimientoTramite(expediente);
        for (ObservadorTramite obs : observadores) {
            seguimiento.agregarObservador(obs);
        }

        if (seguimiento.getEstado().esFinal()) {
            seguimiento.avanzar(); // Imprime mensaje de estado final
            this.huboCambio = false;
            this.expedienteActualizado = expediente;
            return;
        }

        // 1. Patron Memento: capturar instantanea antes de la modificacion
        MementoSeguimiento memento = seguimiento.guardar();
        historialMementos.guardar(memento);

        this.estadoAnterior = seguimiento.getEstado().getNombre();
        this.estadoNuevo = seguimiento.proximoEstado().getNombre();

        // 2. Persistir en el repositorio
        this.expedienteActualizado = repositorio.cambiarEstado(codigo, estadoNuevo);

        // 3. Patron State y Observer: avanzar en memoria y notificar avance
        seguimiento.avanzar();
        this.huboCambio = true;
    }

    /**
     * Patrón Command: deshace el avance de estado restaurando el Memento previo.
     */
    @Override
    public void deshacer() {
        if (!huboCambio) {
            return;
        }

        // 1. Recuperar memento previo del Caretaker
        MementoSeguimiento memento = historialMementos.recuperar(codigo);
        if (memento == null) {
            throw new IllegalStateException("No hay instantanea para deshacer el avance de: " + codigo);
        }

        // 2. Validar que el estado actual en el repositorio coincida con el que avanzamos
        Expediente actual = repositorio.buscarPorCodigo(codigo);
        if (!actual.getEstado().equals(this.estadoNuevo)) {
            historialMementos.guardar(memento); // Retornar a la pila
            throw new IllegalStateException("El estado actual (" + actual.getEstado()
                    + ") no coincide con el estado esperado para revertir (" + estadoNuevo + ")");
        }

        // 3. Revertir persistencia en el repositorio
        this.expedienteActualizado = repositorio.cambiarEstado(codigo, memento.getNombreEstado());

        // 4. Patron Memento: restaurar estado en SeguimientoTramite
        SeguimientoTramite seguimiento = new SeguimientoTramite(actual);
        seguimiento.restaurar(memento);

        // 5. Patron Observer: notificar la reversion
        CambioEstado cambioReversion = new CambioEstado(
                codigo,
                this.estadoNuevo,
                memento.getNombreEstado(),
                actual.getNombreCiudadano(),
                actual.getCorreo(),
                actual.getTipoDocumento(),
                true // esReversion = true
        );
        notificarObservadores(cambioReversion);
    }

    /**
     * Patrón Command: descripción del avance de estado.
     */
    @Override
    public String getDescripcion() {
        if (estadoAnterior != null && estadoNuevo != null) {
            return "Avanzar " + codigo + ": " + estadoAnterior + " -> " + estadoNuevo;
        }
        return "Avanzar tramite " + codigo;
    }

    /**
     * Indica si la ejecución produjo una transición de estado real.
     *
     * @return true si el estado cambió; false si ya era final
     */
    public boolean isHuboCambio() {
        return huboCambio;
    }

    /**
     * Obtiene el expediente resultante tras ejecutar o deshacer.
     *
     * @return expediente actualizado
     */
    public Expediente getExpedienteActualizado() {
        return expedienteActualizado;
    }

    private void notificarObservadores(CambioEstado cambio) {
        for (ObservadorTramite o : observadores) {
            try {
                o.actualizar(cambio);
            } catch (Exception ex) {
                System.err.println("Advertencia: observador fallo: " + ex.getMessage());
            }
        }
    }
}

