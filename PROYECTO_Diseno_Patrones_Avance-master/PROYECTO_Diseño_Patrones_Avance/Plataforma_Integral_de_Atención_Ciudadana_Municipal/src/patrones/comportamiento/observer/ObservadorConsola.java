package patrones.comportamiento.observer;

import patrones.comportamiento.state.EstadoTramite;
import patrones.comportamiento.state.EstadosTramite;

/**
 * Patron Observer: imprime en consola la linea de traza por cada cambio de estado.
 */
public class ObservadorConsola implements ObservadorTramite {

    @Override
    public void actualizar(CambioEstado cambio) {
        EstadoTramite est = EstadosTramite.desdeNombre(cambio.getEstadoNuevo());
        if (cambio.isEsReversion()) {
            System.out.println("[Observer] Expediente " + cambio.getCodigo()
                    + " REVERTIDO: " + cambio.getEstadoAnterior() + " -> " + cambio.getEstadoNuevo()
                    + " (" + est.getDescripcion() + ")");
        } else if (cambio.getEstadoAnterior() == null) {
            System.out.println("[Observer] Expediente " + cambio.getCodigo()
                    + " registrado en estado " + cambio.getEstadoNuevo()
                    + " (" + est.getDescripcion() + ")");
        } else {
            System.out.println("[Observer] Expediente " + cambio.getCodigo()
                    + ": " + cambio.getEstadoAnterior() + " -> " + cambio.getEstadoNuevo()
                    + " (" + est.getDescripcion() + ")");
        }
    }
}