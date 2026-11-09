package patrones.comportamiento.state;

/**
 * Patron State: estado intermedio ("Atendido") - procesado por la municipalidad.
 */
public class EstadoAtendido implements EstadoTramite {

    @Override
    public String getNombre() {
        return "Atendido";
    }

    @Override
    public String getDescripcion() {
        return "Atendido por la municipalidad";
    }

    @Override
    public EstadoTramite siguiente() {
        return new EstadoCerrado();
    }

    @Override
    public boolean esFinal() {
        return false;
    }
}