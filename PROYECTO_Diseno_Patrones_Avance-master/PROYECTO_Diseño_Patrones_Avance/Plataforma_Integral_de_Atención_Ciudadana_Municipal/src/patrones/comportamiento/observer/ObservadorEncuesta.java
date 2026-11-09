package patrones.comportamiento.observer;

import patrones.creacionales.prototype.EncuestaSatisfaccion;

/**
 * Patron Observer: cuando el estado llega a "Cerrado", clona una EncuestaSatisfaccion
 * (Patron Prototype) y notifica el envio al correo del ciudadano.
 */
public class ObservadorEncuesta implements ObservadorTramite {

    private final EncuestaSatisfaccion prototipo;

    public ObservadorEncuesta() {
        this.prototipo = new EncuestaSatisfaccion();
    }

    public ObservadorEncuesta(EncuestaSatisfaccion prototipo) {
        this.prototipo = prototipo;
    }

    @Override
    public void actualizar(CambioEstado cambio) {
        if (cambio.isEsReversion()) {
            return; // No se envian encuestas en reversiones de estado
        }
        if ("Cerrado".equalsIgnoreCase(cambio.getEstadoNuevo())) {
            // Patron Prototype: clona la encuesta base
            EncuestaSatisfaccion copia = prototipo.clone();
            System.out.println("[Encuesta] Encuesta de satisfaccion enviada a "
                    + cambio.getCorreo() + " por el expediente " + cambio.getCodigo());
        }
    }
}