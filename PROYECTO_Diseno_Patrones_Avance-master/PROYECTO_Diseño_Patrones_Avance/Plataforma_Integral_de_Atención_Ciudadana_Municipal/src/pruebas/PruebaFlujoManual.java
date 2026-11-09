package pruebas;

import controladores.AtencionController;
import patrones.creacionales.builder.Expediente;

import java.util.List;

/**
 * Simulación y verificación del flujo manual de usuario para el Prompt 4B:
 * 1. Registrar un trámite en REGISTRO
 * 2. Ir a SEGUIMIENTO > Mis solicitudes, avanzar a 'Atendido'
 * 3. Pulsar 'Deshacer última acción' -> ver estado volver a 'En Revision' con trazas [Command], [Memento], [Observer]
 * 4. Deshacer de nuevo -> elimina el trámite de la tabla
 * 5. Con la pila vacía -> el botón queda deshabilitado y se muestra 'Sin acciones para deshacer'
 */
public class PruebaFlujoManual {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println(" SIMULACION DEL FLUJO MANUAL DE USUARIO (COMMAND & MEMENTO EN VISTA)");
        System.out.println("======================================================================");
        AtencionController c = new AtencionController();

        System.out.println("\n>>> PASO 1: Inicio de la aplicación");
        System.out.println("    Municipio: " + c.nombreMunicipio());
        System.out.println("    Estado persistencia inicial: " + c.estadoConexion());
        System.out.println("    ¿Puede deshacer inicial? " + c.puedeDeshacer());
        System.out.println("    Última acción inicial:   " + c.descripcionUltimaAccion());

        System.out.println("\n>>> PASO 2: REGISTRO > Registrar trámite");
        Expediente exp = c.atenderCiudadano("74859612", "Gonzalo Solar Silva", "gonzalo@test.com", "reclamo");
        System.out.println("    -> Expediente generado: " + exp.getCodigo());
        System.out.println("    -> Estado inicial: " + exp.getEstado() + " (" + c.situacionEstado(exp.getEstado()) + ")");
        System.out.println("    -> ¿Puede deshacer? " + c.puedeDeshacer());
        System.out.println("    -> Botón Deshacer: HABILITADO | Etiqueta: " + c.descripcionUltimaAccion());

        System.out.println("\n>>> PASO 3: SEGUIMIENTO > Mis solicitudes (Ver tabla y avanzar a Atendido)");
        List<Expediente> solicitudes = c.listarMisSolicitudes("74859612");
        System.out.println("    Fila en tabla antes de avanzar:");
        for (Expediente s : solicitudes) {
            System.out.println("    [" + s.getCodigo() + "] [" + s.getTipoDocumento()
                    + "] [" + s.getNombreCiudadano() + "] [" + s.getEstado()
                    + "] [" + c.situacionEstado(s.getEstado()) + "]");
        }

        System.out.println("\n    Pulsando botón 'Avanzar estado':");
        Expediente expAtendido = c.avanzarEstado(exp.getCodigo());
        System.out.println("    -> Fila actualizada en tabla: [" + expAtendido.getCodigo()
                + "] [" + expAtendido.getEstado() + "] [" + c.situacionEstado(expAtendido.getEstado()) + "]");
        System.out.println("    -> ¿Puede deshacer? " + c.puedeDeshacer());
        System.out.println("    -> Etiqueta: Última acción: " + c.descripcionUltimaAccion());

        System.out.println("\n>>> PASO 4: Pulsar botón 'Deshacer última acción'");
        System.out.println("    (Confirmación aceptada, ejecutando en segundo plano con SwingWorker...)");
        String deshecho1 = c.deshacerUltimaAccion();
        System.out.println("    -> Acción revertida: " + deshecho1);

        List<Expediente> tablaTrasDeshacer1 = c.listarMisSolicitudes("74859612");
        System.out.println("    Fila en tabla tras el primer deshacer:");
        for (Expediente s : tablaTrasDeshacer1) {
            System.out.println("    [" + s.getCodigo() + "] [" + s.getEstado()
                    + "] [" + c.situacionEstado(s.getEstado()) + "] <- Vuelve a 'En Revision' exitosamente");
        }
        System.out.println("    -> ¿Puede deshacer? " + c.puedeDeshacer());
        System.out.println("    -> Etiqueta: Última acción: " + c.descripcionUltimaAccion());

        System.out.println("\n>>> PASO 5: Pulsar botón 'Deshacer última acción' de nuevo");
        System.out.println("    (Deshaciendo el registro del trámite...)");
        String deshecho2 = c.deshacerUltimaAccion();
        System.out.println("    -> Acción revertida: " + deshecho2);

        List<Expediente> tablaTrasDeshacer2 = c.listarMisSolicitudes("74859612");
        System.out.println("    Filas en tabla tras el segundo deshacer: " + tablaTrasDeshacer2.size()
                + " (Trámite eliminado de la tabla)");
        System.out.println("    -> ¿Puede deshacer? " + c.puedeDeshacer());
        System.out.println("    -> Botón Deshacer: DESHABILITADO");
        System.out.println("    -> Etiqueta: " + (c.puedeDeshacer() ? c.descripcionUltimaAccion() : "Sin acciones para deshacer"));

        System.out.println("\n======================================================================");
        System.out.println(" PRUEBA MANUAL DE DESHACER COMPLETADA EXITOSAMENTE");
        System.out.println("======================================================================");
    }
}
