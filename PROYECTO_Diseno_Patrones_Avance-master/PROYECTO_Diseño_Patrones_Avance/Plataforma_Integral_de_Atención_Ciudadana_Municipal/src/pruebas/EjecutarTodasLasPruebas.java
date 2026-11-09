package pruebas;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ejecutor integral de pruebas automatizadas:
 * Descubre y ejecuta por reflexión los métodos main de todas las clases de prueba (Prueba*),
 * intercepta su salida por consola, contabiliza aserciones exitosas y fallidas,
 * y presenta una tabla consolidada con el veredicto final.
 */
public class EjecutarTodasLasPruebas {

    private static class ResultadoClase {
        final String nombreClase;
        final int ok;
        final int fallos;
        final long duracionMs;
        final boolean sinExcepcion;
        final String nota;

        ResultadoClase(String nombreClase, int ok, int fallos, long duracionMs, boolean sinExcepcion, String nota) {
            this.nombreClase = nombreClase;
            this.ok = ok;
            this.fallos = fallos;
            this.duracionMs = duracionMs;
            this.sinExcepcion = sinExcepcion;
            this.nota = nota;
        }

        boolean isExitosa() {
            return sinExcepcion && fallos == 0;
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println(" EJECUCION DE LA SUITE COMPLETA DE PRUEBAS DEL PROYECTO");
        System.out.println(" Plataforma Integral de Atencion Ciudadana Municipal");
        System.out.println("================================================================================\n");

        List<Class<?>> clasesPrueba = Arrays.asList(
                PruebaPersistencia.class,
                PruebaEstados.class,
                PruebaComandos.class,
                PruebaControladorSolicitudes.class,
                PruebaProxy.class,
                PruebaImportacion.class,
                PruebaFlujoManual.class
        );

        List<ResultadoClase> resultados = new ArrayList<>();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        for (Class<?> clazz : clasesPrueba) {
            ByteArrayOutputStream bufferSalida = new ByteArrayOutputStream();
            PrintStream interceptor = new PrintStream(bufferSalida, true, StandardCharsets.UTF_8);

            boolean sinExcepcion = true;
            String errorMsg = null;
            long inicio = System.currentTimeMillis();

            System.setOut(interceptor);
            System.setErr(interceptor);

            try {
                Method metodoMain = clazz.getMethod("main", String[].class);
                metodoMain.invoke(null, (Object) new String[0]);
            } catch (InvocationTargetException ex) {
                sinExcepcion = false;
                Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                errorMsg = causa.getClass().getSimpleName() + ": " + causa.getMessage();
            } catch (Exception ex) {
                sinExcepcion = false;
                errorMsg = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
            long duracionMs = System.currentTimeMillis() - inicio;

            String salida = bufferSalida.toString(StandardCharsets.UTF_8);
            String[] lineas = salida.split("\\r?\\n");

            int conteoOk = 0;
            int conteoFallos = 0;

            for (String linea : lineas) {
                if (linea.contains("[OK]")) {
                    conteoOk++;
                } else if (linea.contains("[FALLO]")) {
                    conteoFallos++;
                }
            }

            String nota = "";
            if (!sinExcepcion) {
                conteoFallos++;
                nota = "Excepcion: " + errorMsg;
            } else if (conteoOk == 0 && conteoFallos == 0) {
                // Caso especial para clases como PruebaFlujoManual que simulan pasos sin imprimir [OK]
                conteoOk = 1;
                nota = "Flujo completo ejecutado sin excepcion";
            }

            resultados.add(new ResultadoClase(clazz.getSimpleName(), conteoOk, conteoFallos, duracionMs, sinExcepcion, nota));
            originalOut.println(" -> " + clazz.getSimpleName() + ": " + (sinExcepcion && conteoFallos == 0 ? "PASO" : "FALLO")
                    + " (" + conteoOk + " OK, " + conteoFallos + " fallos, " + duracionMs + " ms)");
        }

        // Imprimir tabla resumen
        originalOut.println("\n=========================================================================================");
        originalOut.println(" RESUMEN CONSOLIDADO DE PRUEBAS");
        originalOut.println("=========================================================================================");
        originalOut.printf("%-30s | %-19s | %-12s | %-12s | %s%n",
                "Prueba", "Estado (OK / FALLO)", "Tiempo (ms)", "Aserciones", "Detalle");
        originalOut.println("-----------------------------------------------------------------------------------------");

        int granTotalOk = 0;
        int granTotalFallos = 0;
        long tiempoTotalMs = 0;
        boolean todasExitosas = true;

        for (ResultadoClase r : resultados) {
            granTotalOk += r.ok;
            granTotalFallos += r.fallos;
            tiempoTotalMs += r.duracionMs;
            if (!r.isExitosa()) {
                todasExitosas = false;
            }
            originalOut.printf("%-30s | %-19s | %-12d | %-12s | %s%n",
                    r.nombreClase,
                    r.isExitosa() ? "OK" : "FALLO",
                    r.duracionMs,
                    r.ok + " OK / " + r.fallos + " F",
                    r.nota
            );
        }

        originalOut.println("-----------------------------------------------------------------------------------------");
        originalOut.printf("%-30s | %-19s | %-12d | %-12s |%n",
                "TOTAL GENERAL", todasExitosas ? "OK" : "FALLOS", tiempoTotalMs, granTotalOk + " OK / " + granTotalFallos + " F");
        originalOut.println("=========================================================================================");

        if (todasExitosas) {
            originalOut.println("\n>>> TODAS LAS PRUEBAS OK <<<");
        } else {
            originalOut.println("\n>>> HAY FALLOS <<<");
            System.exit(1);
        }
    }
}
