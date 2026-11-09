package patrones.estructurales.proxy;

import modelos.Ciudadano;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Patrón Proxy / GRASP Fabricación Pura:
 * Valida integralmente las reglas de formato, consistencia y permisos
 * de una solicitud de trámite ciudadano antes de autorizar su procesamiento.
 */
public final class ValidadorSolicitud {

    private static final Pattern PATRON_DNI_NUMERICO = Pattern.compile("^\\d{8}$");
    private static final Pattern PATRON_NOMBRE_VALIDO = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ' -]{3,}$");
    private static final Pattern PATRON_CORREO_VALIDO = Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

    private ValidadorSolicitud() {
        // Constructor privado para clase utilitaria
    }

    /**
     * Evalúa las reglas de negocio sobre el ciudadano y el tipo de trámite solicitado.
     *
     * @param c           ciudadano solicitante
     * @param tipoTramite tipo de trámite solicitado ("reclamo" o "solicitud")
     * @return lista de motivos de rechazo (vacía si la solicitud es totalmente válida)
     */
    public static List<String> validar(Ciudadano c, String tipoTramite) {
        List<String> motivos = new ArrayList<>();

        if (c == null) {
            motivos.add("El ciudadano no puede ser nulo.");
            return motivos;
        }

        // 1. DNI: exactamente 8 dígitos numéricos
        String dni = c.getDni() != null ? c.getDni().trim() : "";
        if (!PATRON_DNI_NUMERICO.matcher(dni).matches()) {
            motivos.add("El DNI debe contener exactamente 8 dígitos numéricos.");
        } else {
            // 2. Permiso: el DNI no puede tener sus 8 dígitos iguales (00000000, 11111111...)
            boolean todosIguales = true;
            char primerDigito = dni.charAt(0);
            for (int i = 1; i < dni.length(); i++) {
                if (dni.charAt(i) != primerDigito) {
                    todosIguales = false;
                    break;
                }
            }
            if (todosIguales) {
                motivos.add("DNI no permitido");
            }
        }

        // 3. Nombre: no vacío, mínimo 3 caracteres, solo letras (con tildes y ñ), espacios, apóstrofe y guion
        String nombre = c.getNombre() != null ? c.getNombre().trim() : "";
        if (nombre.isEmpty() || !PATRON_NOMBRE_VALIDO.matcher(nombre).matches()) {
            motivos.add("El nombre debe contener al menos 3 caracteres y solo letras válidas.");
        }

        // 4. Correo: formato básico válido (regex razonable) y máximo 120 caracteres
        String correo = c.getCorreo() != null ? c.getCorreo().trim() : "";
        if (correo.isEmpty() || correo.length() > 120 || !PATRON_CORREO_VALIDO.matcher(correo).matches()) {
            motivos.add("El correo electrónico no tiene un formato válido o excede 120 caracteres.");
        }

        // 5. Tipo de trámite: solo los que reconoce TramiteFactory (reclamo, solicitud; sin distinguir mayúsculas)
        if (tipoTramite == null || (!tipoTramite.equalsIgnoreCase("reclamo") && !tipoTramite.equalsIgnoreCase("solicitud"))) {
            motivos.add("Tipo de trámite desconocido: " + tipoTramite);
        }

        return motivos;
    }
}

