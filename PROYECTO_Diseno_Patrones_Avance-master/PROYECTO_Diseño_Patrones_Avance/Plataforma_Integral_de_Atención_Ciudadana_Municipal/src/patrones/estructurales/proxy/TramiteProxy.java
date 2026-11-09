package patrones.estructurales.proxy;

import modelos.Ciudadano;
import patrones.creacionales.factory.TramiteFactory;
import patrones.creacionales.productos.Tramite;

import java.util.List;

/**
 * Patrón Proxy: actúa como intermediario de control de acceso y validación,
 * instanciando el trámite real de forma perezosa solo tras superar las reglas de negocio.
 */
public class TramiteProxy implements Tramite {

    private final Ciudadano ciudadano;
    private final String tipoTramite;
    private final boolean ciudadanoAutorizado;
    private final boolean modoValidacionCiudadano;
    private Tramite tramiteReal;

    /**
     * Constructor original conservado para compatibilidad con demostraciones técnicas previas.
     *
     * @param tipoTramite         tipo de trámite
     * @param ciudadanoAutorizado indicador booleano de autorización
     */
    public TramiteProxy(String tipoTramite, boolean ciudadanoAutorizado) {
        this.ciudadano = null;
        this.tipoTramite = tipoTramite;
        this.ciudadanoAutorizado = ciudadanoAutorizado;
        this.modoValidacionCiudadano = false;
    }

    /**
     * Constructor principal para el flujo real de atención ciudadana.
     * Valida integralmente las reglas de negocio sobre el ciudadano y el tipo de trámite.
     *
     * @param ciudadano   ciudadano solicitante
     * @param tipoTramite tipo de trámite ("reclamo" o "solicitud")
     */
    public TramiteProxy(Ciudadano ciudadano, String tipoTramite) {
        this.ciudadano = ciudadano;
        this.tipoTramite = tipoTramite;
        this.ciudadanoAutorizado = true;
        this.modoValidacionCiudadano = true;
    }

    @Override
    public void procesar() {
        if (!modoValidacionCiudadano) {
            // Modo demo / autorización simple
            if (!ciudadanoAutorizado) {
                System.out.println("Acceso denegado: el ciudadano no esta autorizado.");
                return;
            }
            if (tramiteReal == null) {
                System.out.println("Proxy: validacion exitosa. Creando tramite real...");
                tramiteReal = TramiteFactory.crearTramite(tipoTramite);
            }
            tramiteReal.procesar();
            return;
        }

        // Modo flujo real con validación y autorización integral
        String dni = ciudadano != null && ciudadano.getDni() != null ? ciudadano.getDni() : "desconocido";
        System.out.println("[Proxy] Validando solicitud del DNI " + dni + "...");

        List<String> motivos = ValidadorSolicitud.validar(ciudadano, tipoTramite);
        if (!motivos.isEmpty()) {
            System.out.println("[Proxy] Acceso denegado: " + String.join(", ", motivos));
            throw new SolicitudRechazadaException(motivos);
        }

        System.out.println("[Proxy] Acceso concedido");
        if (tramiteReal == null) {
            tramiteReal = TramiteFactory.crearTramite(tipoTramite);
        }
        tramiteReal.procesar();
    }
}
