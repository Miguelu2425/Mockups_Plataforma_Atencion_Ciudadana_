package pruebas;

import controladores.AtencionController;
import patrones.creacionales.builder.Expediente;
import patrones.estructurales.facade.MesaPartesFacade;
import repositorios.TramiteRepositorioMemoria;

import java.util.List;

/**
 * Prueba unitaria sin interfaz gráfica para validar los métodos agregados en AtencionController
 * para el soporte de la vista (descripción, siguiente estado, situación, listados y avances).
 * Opera completamente con el repositorio en memoria mediante inyección de dependencias.
 */
public class PruebaControladorSolicitudes {

    private static int ok = 0;
    private static int fallo = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println(" INICIANDO PRUEBAS DE ATENCIONCONTROLLER (SIN GUI)");
        System.out.println("======================================================================");

        // Inicializar controlador con repositorio en memoria aislado
        TramiteRepositorioMemoria repoMemoria = new TramiteRepositorioMemoria();
        MesaPartesFacade facade = new MesaPartesFacade(repoMemoria);
        AtencionController controller = new AtencionController(facade);

        // 1. Verificación de helpers de estado para vistas
        probarMetodosDeEstado(controller);

        // 2. Verificación de listados con y sin filtro DNI
        probarListadoSolicitudes(controller);

        // 3. Verificación del avance de estados y condición terminal
        probarCicloAvanceEstados(controller);

        // 4. Verificación de prueba de conexión Singleton
        probarMetodoConexion(controller);

        // 5. Verificación de operaciones de deshacer e historial (Command & Memento)
        probarOperacionesDeshacerEHistorial();

        System.out.println("\n=== RESULTADO PRUEBA CONTROLADOR: " + ok + " OK, " + fallo + " FALLO ===");
        if (fallo > 0) {
            throw new AssertionError("PruebaControladorSolicitudes finalizo con " + fallo + " fallos.");
        }
    }

    private static void probarMetodosDeEstado(AtencionController c) {
        System.out.println("\n--- [1] Métodos de descripción, siguiente estado y situación ---");

        // descripcionEstado
        verificar("descripcionEstado('En Revision') describe espera",
                c.descripcionEstado("En Revision").toLowerCase().contains("espera"));
        verificar("descripcionEstado('Atendido') describe atencion municipal",
                c.descripcionEstado("Atendido").toLowerCase().contains("atendido"));
        verificar("descripcionEstado('Cerrado') describe tramite finalizado",
                c.descripcionEstado("Cerrado").toLowerCase().contains("finalizado"));

        // siguienteEstado
        verificar("siguienteEstado('En Revision') es 'Atendido'",
                "Atendido".equals(c.siguienteEstado("En Revision")));
        verificar("siguienteEstado('Atendido') es 'Cerrado'",
                "Cerrado".equals(c.siguienteEstado("Atendido")));
        verificar("siguienteEstado('Cerrado') es '-' (terminal)",
                "-".equals(c.siguienteEstado("Cerrado")));

        // situacionEstado
        verificar("situacionEstado('En Revision') es 'En espera'",
                "En espera".equals(c.situacionEstado("En Revision")));
        verificar("situacionEstado('Atendido') es 'Atendido'",
                "Atendido".equals(c.situacionEstado("Atendido")));
        verificar("situacionEstado('Cerrado') es 'Finalizado'",
                "Finalizado".equals(c.situacionEstado("Cerrado")));

        // nombreMunicipio
        verificar("nombreMunicipio() devuelve texto no vacío",
                c.nombreMunicipio() != null && !c.nombreMunicipio().trim().isEmpty());
    }

    private static void probarListadoSolicitudes(AtencionController c) {
        System.out.println("\n--- [2] Registro y listado de solicitudes ---");

        Expediente e1 = c.atenderCiudadano("11223344", "Carlos Lopez", "carlos@test.com", "solicitud");
        Expediente e2 = c.atenderCiudadano("55667788", "Maria Santos", "maria@test.com", "reclamo");

        verificar("Registro 1 exitoso", e1 != null && e1.getCodigo().startsWith("EXP-"));
        verificar("Registro 2 exitoso", e2 != null && e2.getCodigo().startsWith("EXP-"));

        // Listar todos (DNI = null)
        List<Expediente> todos = c.listarMisSolicitudes(null);
        verificar("listarMisSolicitudes(null) devuelve al menos 2 trámites", todos.size() >= 2);

        // Listar filtrado por DNI existente
        List<Expediente> soloCarlos = c.listarMisSolicitudes("11223344");
        verificar("listarMisSolicitudes('11223344') devuelve exactamente 1 trámite", soloCarlos.size() == 1);
        verificar("DNI del trámite listado coincide", "11223344".equals(soloCarlos.get(0).getDni()));

        // Listar filtrado por DNI inexistente
        List<Expediente> vacio = c.listarMisSolicitudes("00000000");
        verificar("listarMisSolicitudes con DNI inexistente devuelve lista vacía", vacio.isEmpty());
    }

    private static void probarCicloAvanceEstados(AtencionController c) {
        System.out.println("\n--- [3] Ciclo completo de avance de estado ---");

        Expediente exp = c.atenderCiudadano("99887766", "Raul Castro", "raul@test.com", "solicitud");
        verificar("Estado inicial es 'En Revision'", "En Revision".equals(exp.getEstado()));

        // Primer avance: En Revision -> Atendido
        Expediente expAtendido = c.avanzarEstado(exp.getCodigo());
        verificar("Primer avance lleva a 'Atendido'", "Atendido".equals(expAtendido.getEstado()));

        // Segundo avance: Atendido -> Cerrado
        Expediente expCerrado = c.avanzarEstado(exp.getCodigo());
        verificar("Segundo avance lleva a 'Cerrado'", "Cerrado".equals(expCerrado.getEstado()));

        // Tercer avance desde Cerrado (estado terminal)
        Expediente expTercero = c.avanzarEstado(exp.getCodigo());
        verificar("Tercer avance mantiene estado 'Cerrado'", "Cerrado".equals(expTercero.getEstado()));
    }

    private static void probarMetodoConexion(AtencionController c) {
        System.out.println("\n--- [4] Método probarConexion() ---");
        String resultado = c.probarConexion();
        verificar("probarConexion() devuelve respuesta descriptiva",
                resultado != null && (resultado.contains("BD conectada") || resultado.contains("modo memoria")));
    }

    private static void probarOperacionesDeshacerEHistorial() {
        System.out.println("\n--- [5] Operaciones de Deshacer e Historial (Command & Memento) ---");

        TramiteRepositorioMemoria repoDeshacer = new TramiteRepositorioMemoria();
        MesaPartesFacade facadeDeshacer = new MesaPartesFacade(repoDeshacer);
        AtencionController c = new AtencionController(facadeDeshacer);

        // a) puedeDeshacer() es false al inicio
        verificar("puedeDeshacer() es false al inicio", !c.puedeDeshacer());
        verificar("historialAcciones() inicial está vacío", c.historialAcciones().isEmpty());
        verificar("descripcionUltimaAccion() inicial es null", c.descripcionUltimaAccion() == null);

        // b) tras atenderCiudadano y avanzarEstado pasa a true
        Expediente exp = c.atenderCiudadano("44332211", "Rosa Martinez", "rosa@test.com", "solicitud");
        verificar("puedeDeshacer() pasa a true tras registrar trámite", c.puedeDeshacer());

        c.avanzarEstado(exp.getCodigo()); // En Revision -> Atendido
        verificar("puedeDeshacer() se mantiene true tras primer avance de estado", c.puedeDeshacer());

        c.avanzarEstado(exp.getCodigo()); // Atendido -> Cerrado
        verificar("puedeDeshacer() se mantiene true tras segundo avance de estado", c.puedeDeshacer());

        // e) historialAcciones() tiene el orden correcto (más reciente primero con tamaño 3)
        List<String> historial = c.historialAcciones();
        verificar("historialAcciones() contiene exactamente 3 acciones", historial.size() == 3);
        verificar("Acción 1 (más reciente) es el avance a Cerrado",
                historial.get(0).contains("Atendido -> Cerrado"));
        verificar("Acción 2 es el avance a Atendido",
                historial.get(1).contains("En Revision -> Atendido"));
        verificar("Acción 3 (más antigua) es el registro del trámite",
                historial.get(2).contains("Registrar tramite"));

        // c) deshacerUltimaAccion() revierte el estado (Cerrado -> Atendido)
        String undo1 = c.deshacerUltimaAccion();
        verificar("Primer deshacer revierte avance a Cerrado", undo1 != null && undo1.contains("Cerrado"));
        Expediente expRev1 = repoDeshacer.buscarPorCodigo(exp.getCodigo());
        verificar("Estado actual es 'Atendido' tras primer deshacer", "Atendido".equals(expRev1.getEstado()));

        // d) dos deshacer más y puedeDeshacer() vuelve a false
        String undo2 = c.deshacerUltimaAccion(); // Atendido -> En Revision
        verificar("Segundo deshacer revierte avance a Atendido", undo2 != null && undo2.contains("Atendido"));
        Expediente expRev2 = repoDeshacer.buscarPorCodigo(exp.getCodigo());
        verificar("Estado actual es 'En Revision' tras segundo deshacer", "En Revision".equals(expRev2.getEstado()));

        String undo3 = c.deshacerUltimaAccion(); // En Revision -> Eliminado
        verificar("Tercer deshacer elimina el trámite registrado", undo3 != null && undo3.contains("Registrar tramite"));

        boolean eliminado = false;
        try {
            repoDeshacer.buscarPorCodigo(exp.getCodigo());
        } catch (Exception ex) {
            eliminado = true;
        }
        verificar("Expediente fue eliminado del repositorio tras tercer deshacer", eliminado);

        // puedeDeshacer() vuelve a false
        verificar("puedeDeshacer() vuelve a false con la pila vacía", !c.puedeDeshacer());
        verificar("historialAcciones() queda vacío", c.historialAcciones().isEmpty());
        verificar("deshacer sobre pila vacía retorna null", c.deshacerUltimaAccion() == null);
    }

    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            System.out.println("[OK]    " + descripcion);
            ok++;
        } else {
            System.out.println("[FALLO] " + descripcion);
            fallo++;
        }
    }
}

