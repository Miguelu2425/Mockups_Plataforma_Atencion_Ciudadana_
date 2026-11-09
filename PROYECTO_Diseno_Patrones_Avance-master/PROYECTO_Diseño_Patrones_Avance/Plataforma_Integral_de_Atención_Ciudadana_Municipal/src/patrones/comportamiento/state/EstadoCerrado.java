package patrones.comportamiento.state;

/**
 * Patron State: estado terminal ("Cerrado") - tramite finalizado.
 * Desde aqui siguiente() devuelve this y esFinal() devuelve true.
 */
public class EstadoCerrado implements EstadoTramite {

    @Override
    public String getNombre() {
        return "Cerrado";
    }

    @Override
    public String getDescripcion() {
        return "Tramite finalizado";
    }

    @Override
    public EstadoTramite siguiente() {
        return this;
    }

    @Override
    public boolean esFinal() {
        return true;
    }
}