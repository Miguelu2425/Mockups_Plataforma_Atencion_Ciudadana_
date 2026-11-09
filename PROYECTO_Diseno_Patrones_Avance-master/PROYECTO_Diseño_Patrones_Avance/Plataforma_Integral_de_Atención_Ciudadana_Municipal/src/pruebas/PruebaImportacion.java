package pruebas;

import patrones.comportamiento.observer.ObservadorTramite;
import patrones.creacionales.builder.Expediente;
import patrones.estructurales.facade.MesaPartesFacade;
import patrones.estructurales.facade.ResultadoImportacion;
import repositorios.TramiteRepositorioMemoria;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pruebas: verifica el flujo de importación de quejas del sistema antiguo
 * utilizando Adapter, Composite, Proxy, Command y Observer.
 */
public class PruebaImportacion {

    private static int ok = 0;
    private static int fallo = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println(" INICIANDO PRUEBAS DE IMPORTACION (ADAPTER + COMPOSITE + PROXY)");
        System.out.println("======================================================================");

        probarFlujoCompletoImportacion();

        System.out.println("\n=== RESULTADO PRUEBA IMPORTACION: " + ok + " OK, " + fallo + " FALLO ===");
        if (fallo > 0) {
            throw new AssertionError("PruebaImportacion finalizo con " + fallo + " fallos.");
        }
    }

    private static void probarFlujoCompletoImportacion() {
        TramiteRepositorioMemoria repo = new TramiteRepositorioMemoria();
        AtomicInteger notificaciones = new AtomicInteger(0);
        ObservadorTramite obs = cambio -> notificaciones.incrementAndGet();

        MesaPartesFacade facade = new MesaPartesFacade(repo, Collections.singletonList(obs));

        System.out.println("\n--- Ejecutando importarQuejasAntiguas() ---");
        ResultadoImportacion resultado = facade.importarQuejasAntiguas();

        // 1. Verificación de conteos de importación
        afirmar(resultado != null, "Resultado de importacion no es nulo");
        afirmar(resultado.getTotalImportados() == 3, "Total importados es exactamente 3");
        afirmar(resultado.getTotalRechazados() == 1, "Total rechazados es exactamente 1");
        afirmar(resultado.getTotalProcesados() == 4, "Total procesados es exactamente 4");

        // 2. Verificación de persistencia en repositorio
        List<Expediente> listados = facade.listarTramites(null);
        afirmar(listados.size() == 3, "Repositorio contiene exactamente los 3 expedientes importados");

        // 3. Verificación de observadores
        afirmar(notificaciones.get() == 3, "Los observadores recibieron exactamente 3 avisos de registro (1 por cada importado)");

        // 4. Verificación del motivo del registro rechazado
        ResultadoImportacion.DetalleRechazo rechazo = resultado.getRechazados().get(0);
        afirmar(rechazo.getRegistroHeredado().contains("LEG-002"), "Registro rechazado corresponde a LEG-002");
        afirmar(rechazo.getMotivo().toLowerCase().contains("8 dígitos"), "Motivo de rechazo indica formato de DNI invalido");

        // 5. Verificación de la pila de deshacer (Command & Memento)
        afirmar(facade.puedeDeshacer(), "puedeDeshacer() es true tras la importacion");
        afirmar(facade.historialAcciones().size() == 3, "Pila de deshacer contiene exactamente 3 comandos (los rechazados no se apilan)");

        String ultimaAccion = facade.descripcionUltimaAccion();
        System.out.println(">>> Etiqueta de ultima accion reportada: '" + ultimaAccion + "'");
        afirmar(ultimaAccion != null && ultimaAccion.contains("EXP-"), "Ultima accion describe el ultimo expediente importado exitosamente");

        // 6. Deshacer secuencial de los 3 expedientes
        System.out.println("\n--- Deshaciendo las 3 importaciones de la pila ---");
        String deshacer1 = facade.deshacerUltimaAccion();
        afirmar(deshacer1 != null, "Deshacer 1 ejecutado con exito");
        afirmar(facade.listarTramites(null).size() == 2, "Quedan 2 expedientes en repositorio tras primer deshacer");

        String deshacer2 = facade.deshacerUltimaAccion();
        afirmar(deshacer2 != null, "Deshacer 2 ejecutado con exito");
        afirmar(facade.listarTramites(null).size() == 1, "Queda 1 expediente en repositorio tras segundo deshacer");

        String deshacer3 = facade.deshacerUltimaAccion();
        afirmar(deshacer3 != null, "Deshacer 3 ejecutado con exito");
        afirmar(facade.listarTramites(null).isEmpty(), "Repositorio queda vacio tras deshacer las 3 importaciones");

        afirmar(!facade.puedeDeshacer(), "puedeDeshacer() pasa a false tras vaciar la pila de importaciones");
    }

    private static void afirmar(boolean condicion, String mensaje) {
        if (condicion) {
            ok++;
            System.out.println("[OK]    " + mensaje);
        } else {
            fallo++;
            System.err.println("[FALLO] " + mensaje);
        }
    }
}

