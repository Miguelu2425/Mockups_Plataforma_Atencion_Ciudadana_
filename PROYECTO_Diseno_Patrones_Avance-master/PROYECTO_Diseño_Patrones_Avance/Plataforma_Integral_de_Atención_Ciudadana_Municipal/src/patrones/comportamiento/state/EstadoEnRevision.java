package patrones.comportamiento.state;

/**
 * Patron State: estado inicial ("En Revision") - en espera de atencion vecinal.
 */
public class EstadoEnRevision implements EstadoTramite {

    @Override
    public String getNombre() {
        return "En Revision";
    }

    @Override
    public String getDescripcion() {
        return "En espera de atencion";
    }

    @Override
    public EstadoTramite siguiente() {
        return new EstadoAtendido();
    }

    @Override
    public boolean esFinal() {
        return false;
    }
}