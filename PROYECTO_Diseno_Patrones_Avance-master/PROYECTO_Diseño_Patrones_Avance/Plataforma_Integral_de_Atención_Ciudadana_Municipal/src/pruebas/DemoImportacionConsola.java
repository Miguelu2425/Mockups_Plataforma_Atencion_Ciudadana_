package pruebas;

import controladores.AtencionController;
import patrones.creacionales.builder.Expediente;
import patrones.estructurales.facade.MesaPartesFacade;
import patrones.estructurales.facade.ResultadoImportacion;
import repositorios.TramiteRepositorioMemoria;

/**
 * Demostración por consola: ejecuta la importación de quejas del sistema antiguo
 * en modo memoria, evidenciando en orden la intervención de los patrones:
 *  - [Composite]
 *  - [Adapter]
 *  - [Proxy]
 *  - [Command]
 *  - [Observer]
 */
public class DemoImportacionConsola {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println(" DEMOSTRACION EN CONSOLA: IMPORTACION DEL SISTEMA ANTIGUO");
        System.out.println(" (Composite -> Adapter -> Proxy -> Command -> Observer)");
        System.out.println("======================================================================\n");

        // Configuración con repositorio en memoria y observadores completos
        MesaPartesFacade facade = new MesaPartesFacade(new TramiteRepositorioMemoria());
        AtencionController controlador = new AtencionController(facade);

        System.out.println("Iniciando importacion mediante AtencionController...");
        ResultadoImportacion resultado = controlador.importarQuejasAntiguas();

        System.out.println("\n====================================");
        System.out.println("RESUMEN DE IMPORTACION");
        System.out.println("Total procesados: " + resultado.getTotalProcesados());
        System.out.println("Importadas:       " + resultado.getTotalImportados());
        System.out.println("Rechazadas:       " + resultado.getTotalRechazados());
        System.out.println("====================================");

        System.out.println("\nExpedientes importados con exito:");
        for (Expediente exp : resultado.getImportados()) {
            System.out.println(" - " + exp.getCodigo() + " | DNI: " + exp.getDni() + " | " + exp.getNombreCiudadano() + " | Estado: " + exp.getEstado());
        }

        System.out.println("\nRegistros rechazados por el Proxy:");
        for (ResultadoImportacion.DetalleRechazo rechazo : resultado.getRechazados()) {
            System.out.println(" - " + rechazo.getRegistroHeredado() + " => Motivo: " + rechazo.getMotivo());
        }

        System.out.println("\nEstado del historial de deshacer (Command):");
        System.out.println(" - Puede deshacer: " + controlador.puedeDeshacer());
        System.out.println(" - Ultima accion:  " + controlador.descripcionUltimaAccion());
        System.out.println(" - Total acciones: " + controlador.historialAcciones().size());
        System.out.println("\nDemostracion completada.");
    }
}

