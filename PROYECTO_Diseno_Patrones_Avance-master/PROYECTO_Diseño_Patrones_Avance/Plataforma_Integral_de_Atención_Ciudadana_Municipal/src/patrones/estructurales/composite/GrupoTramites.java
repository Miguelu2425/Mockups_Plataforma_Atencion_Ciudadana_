package patrones.estructurales.composite;

import patrones.creacionales.productos.Tramite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Patrón Composite: trata trámites individuales y colecciones de trámites de forma uniforme.
 * Permite procesar lotes de trámites heredados de manera homogénea.
 */
public class GrupoTramites implements Tramite {

    private final List<Tramite> tramites = new ArrayList<>();

    /**
     * Composite: agrega un componente hijo al grupo de trámites.
     *
     * @param t trámite o grupo a agregar
     */
    public void agregarTramite(Tramite t) {
        if (t != null) {
            tramites.add(t);
        }
    }

    /**
     * Obtiene la lista inmutable de trámites contenidos en el grupo.
     *
     * @return lista de trámites
     */
    public List<Tramite> getTramites() {
        return Collections.unmodifiableList(tramites);
    }

    @Override
    public void procesar() {
        System.out.println("[Composite] Grupo con " + tramites.size() + " tramites heredados");
        for (Tramite t : tramites) {
            t.procesar();
        }
    }
}
