package patrones.estructurales.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Patrón Proxy: excepción lanzada cuando una solicitud ciudadana no cumple
 * con las reglas de negocio o autorización requeridas por el Proxy de trámites.
 */
public class SolicitudRechazadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final List<String> motivos;

    /**
     * Construye la excepción a partir de una lista de motivos de rechazo.
     *
     * @param motivos lista de motivos detectados
     */
    public SolicitudRechazadaException(List<String> motivos) {
        super(motivos != null && !motivos.isEmpty() ? String.join("; ", motivos) : "Solicitud rechazada");
        this.motivos = motivos != null ? new ArrayList<>(motivos) : Collections.emptyList();
    }

    /**
     * Construye la excepción para un motivo individual de rechazo.
     *
     * @param motivo descripción del rechazo
     */
    public SolicitudRechazadaException(String motivo) {
        this(Collections.singletonList(motivo));
    }

    /**
     * Obtiene la lista inmutable de motivos del rechazo.
     *
     * @return lista de motivos
     */
    public List<String> getMotivos() {
        return Collections.unmodifiableList(motivos);
    }
}

