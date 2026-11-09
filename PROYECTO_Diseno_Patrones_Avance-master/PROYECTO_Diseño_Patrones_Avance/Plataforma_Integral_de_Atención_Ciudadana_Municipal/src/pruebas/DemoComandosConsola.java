package pruebas;

import controladores.AtencionController;
import patrones.creacionales.builder.Expediente;

/**
 * Demostracion interactiva en consola de los patrones COMMAND y MEMENTO.
 * Simula el ciclo de vida completo de un tramite con registro, avances de estado
 * sucesivos y reversiones completas (Undo) hasta la eliminacion del registro,
 * finalizando con un intento de deshacer sobre la pila vacia.
 */
public class DemoComandosConsola {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println(" PLATAFORMA INTEGRAL DE ATENCION CIUDADANA MUNICIPAL");
        System.out.println(" DEMOSTRACION: PATRONES COMMAND Y MEMENTO (DESHACER / UNDO)");
        System.out.println("======================================================================");

        AtencionController controller = new AtencionController();
        System.out.println("Origen de persistencia: " + controller.estadoConexion());
        System.out.println();

        // 1. Registro de un nuevo tramite
        System.out.println(">>> PASO 1: REGISTRAR TRAMITE (Comando: RegistrarTramiteComando)");
        Expediente exp = controller.atenderCiudadano(
                "74859612",
                "Gonzalo Solar Silva",
                "gonzalo.solar@utp.edu.pe",
                "reclamo"
        );
        System.out.println("-> Expediente activo: " + exp.getCodigo() + " | Estado: " + exp.getEstado());
        System.out.println("-> ¿Puede deshacer? " + controller.puedeDeshacer());
        System.out.println("-> Ultima accion:   " + controller.descripcionUltimaAccion());
        System.out.println();

        // 2. Primer avance: En Revision -> Atendido
        System.out.println(">>> PASO 2: PRIMER AVANCE DE ESTADO (Comando: AvanzarEstadoComando)");
        Expediente expAtendido = controller.avanzarEstado(exp.getCodigo());
        System.out.println("-> Expediente actualizado: " + expAtendido.getCodigo() + " | Estado: " + expAtendido.getEstado());
        System.out.println("-> ¿Puede deshacer? " + controller.puedeDeshacer());
        System.out.println("-> Ultima accion:   " + controller.descripcionUltimaAccion());
        System.out.println();

        // 3. Segundo avance: Atendido -> Cerrado
        System.out.println(">>> PASO 3: SEGUNDO AVANCE DE ESTADO (Comando: AvanzarEstadoComando)");
        Expediente expCerrado = controller.avanzarEstado(exp.getCodigo());
        System.out.println("-> Expediente actualizado: " + expCerrado.getCodigo() + " | Estado: " + expCerrado.getEstado());
        System.out.println("-> ¿Puede deshacer? " + controller.puedeDeshacer());
        System.out.println("-> Ultima accion:   " + controller.descripcionUltimaAccion());
        System.out.println();

        // Mostrar pila de acciones antes de revertir
        System.out.println(">>> HISTORIAL DE ACCIONES EN PILA DE DESHACER ("
                + controller.historialAcciones().size() + " acciones):");
        for (String accion : controller.historialAcciones()) {
            System.out.println("    - " + accion);
        }
        System.out.println();

        // 4. Primer deshacer: Cerrado -> Atendido
        System.out.println(">>> PASO 4: DESHACER 1 (Revierte Cerrado -> Atendido usando Memento)");
        String undo1 = controller.deshacerUltimaAccion();
        System.out.println("-> Accion revertida: " + undo1);
        System.out.println("-> Siguiente accion a deshacer: " + controller.descripcionUltimaAccion());
        System.out.println();

        // 5. Segundo deshacer: Atendido -> En Revision
        System.out.println(">>> PASO 5: DESHACER 2 (Revierte Atendido -> En Revision usando Memento)");
        String undo2 = controller.deshacerUltimaAccion();
        System.out.println("-> Accion revertida: " + undo2);
        System.out.println("-> Siguiente accion a deshacer: " + controller.descripcionUltimaAccion());
        System.out.println();

        // 6. Tercer deshacer: En Revision -> Eliminacion del tramite
        System.out.println(">>> PASO 6: DESHACER 3 (Revierte registro eliminando el tramite)");
        String undo3 = controller.deshacerUltimaAccion();
        System.out.println("-> Accion revertida: " + undo3);
        System.out.println("-> ¿Puede deshacer? " + controller.puedeDeshacer());
        System.out.println();

        // 7. Cuarto deshacer sobre pila vacia
        System.out.println(">>> PASO 7: DESHACER 4 (Intento sobre pila vacia)");
        String undo4 = controller.deshacerUltimaAccion();
        System.out.println("-> Resultado retorno: " + undo4);
        System.out.println("-> ¿Puede deshacer? " + controller.puedeDeshacer());

        System.out.println();
        System.out.println("======================================================================");
        System.out.println(" DEMOSTRACION COMPLETADA CON EXITO");
        System.out.println("======================================================================");
    }
}

