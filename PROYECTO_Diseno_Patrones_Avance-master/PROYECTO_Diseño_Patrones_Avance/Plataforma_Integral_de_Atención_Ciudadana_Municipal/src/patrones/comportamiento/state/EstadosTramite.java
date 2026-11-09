package patrones.comportamiento.state;

/**
 * Patron State: factoria utilitaria para instanciar el EstadoTramite
 * a partir de los nombres oficiales permitidos.
 */
public final class EstadosTramite {

    private EstadosTramite() {}

    /**
     * Patron State: devuelve la instancia concreta correspondiente al nombre oficial.
     *
     * @param nombre nombre del estado ("En Revision", "Atendido", "Cerrado")
     * @return EstadoTramite correspondiente
     * @throws IllegalArgumentException si el nombre no es uno de los 3 oficiales
     */
    public static EstadoTramite desdeNombre(String nombre) {
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre del estado no puede ser null");
        }
        String normalizado = nombre.trim();
        if ("En Revision".equalsIgnoreCase(normalizado)) {
            return new EstadoEnRevision();
        } else if ("Atendido".equalsIgnoreCase(normalizado)) {
            return new EstadoAtendido();
        } else if ("Cerrado".equalsIgnoreCase(normalizado)) {
            return new EstadoCerrado();
        }
        throw new IllegalArgumentException("Estado no oficial: " + nombre
                + ". Solo se permiten: En Revision, Atendido, Cerrado.");
    }
}