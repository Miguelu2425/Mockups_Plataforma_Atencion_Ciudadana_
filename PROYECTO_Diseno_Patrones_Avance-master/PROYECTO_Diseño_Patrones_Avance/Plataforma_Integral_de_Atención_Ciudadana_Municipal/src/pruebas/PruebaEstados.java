package pruebas;

import modelos.Ciudadano;
import patrones.comportamiento.observer.CambioEstado;
import patrones.comportamiento.observer.ObservadorConsola;
import patrones.comportamiento.observer.ObservadorEncuesta;
import patrones.comportamiento.observer.ObservadorTramite;
import patrones.creacionales.builder.Expediente;
import patrones.estructurales.facade.MesaPartesFacade;
import repositorios.TramiteRepositorioMemoria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Prueba: verifica el ciclo de vida completo de estados (State) y notificaciones (Observer)
 * utilizando el repositorio en memoria sin interfaz grafica.
 */
public class PruebaEstados {

    private static int ok = 0;
    private static int fallo = 0;

    public static void main(String[] args) {
        TramiteRepositorioMemoria repo = new TramiteRepositorioMemoria();

        AtomicInteger contadorNotificaciones = new AtomicInteger(0);
        List<CambioEstado> cambios = new ArrayList<>();

        // Observador contador para verificar cantidad de eventos
        ObservadorTramite observadorContador = cambio -> {
            contadorNotificaciones.incrementAndGet();
            cambios.add(cambio);
        };

        // Observador encuesta con contador para verificar cuando actua
        AtomicInteger encuestasEnviadas = new AtomicInteger(0);
        ObservadorEncuesta observadorEncuesta = new ObservadorEncuesta() {
            @Override
            public void actualizar(CambioEstado cambio) {
                if ("Cerrado".equalsIgnoreCase(cambio.getEstadoNuevo())) {
                    encuestasEnviadas.incrementAndGet();
                }
                super.actualizar(cambio);
            }
        };

        MesaPartesFacade facade = new MesaPartesFacade(
                repo,
                Arrays.asList(new ObservadorConsola(), observadorContador, observadorEncuesta)
        );

        // 1. Registro del tramite
        Ciudadano ciudadano = new Ciudadano("11223344", "Carlos Lopez", "carlos@test.com");
        Expediente exp = facade.registrarNuevoTramite(ciudadano, "solicitud");

        verificar("Estado inicial es 'En Revision'", "En Revision".equals(exp.getEstado()));
        verificar("Notificacion 1 emitida en registro (null -> En Revision)", contadorNotificaciones.get() == 1);
        verificar("ObservadorEncuesta no actua en registro", encuestasEnviadas.get() == 0);

        // 2. Primer avance: En Revision -> Atendido
        Expediente expAtendido = facade.avanzarEstado(exp.getCodigo());
        verificar("avanzarEstado lleva a 'Atendido'", "Atendido".equals(expAtendido.getEstado()));
        verificar("Notificacion 2 emitida (En Revision -> Atendido)", contadorNotificaciones.get() == 2);
        verificar("ObservadorEncuesta no actua en 'Atendido'", encuestasEnviadas.get() == 0);

        // 3. Segundo avance: Atendido -> Cerrado
        Expediente expCerrado = facade.avanzarEstado(exp.getCodigo());
        verificar("avanzarEstado lleva a 'Cerrado'", "Cerrado".equals(expCerrado.getEstado()));
        verificar("Notificacion 3 emitida (Atendido -> Cerrado)", contadorNotificaciones.get() == 3);
        verificar("ObservadorEncuesta actua al llegar a 'Cerrado'", encuestasEnviadas.get() == 1);

        // 4. Tercer avance desde estado terminal (Cerrado)
        Expediente expTercerAvance = facade.avanzarEstado(exp.getCodigo());
        verificar("Tercer avanzar mantiene estado 'Cerrado'", "Cerrado".equals(expTercerAvance.getEstado()));
        verificar("Tercer avanzar no notifica a observadores", contadorNotificaciones.get() == 3);
        verificar("Contador final de notificaciones es exactamente 3 (registro + 2 cambios)", contadorNotificaciones.get() == 3);
        verificar("Total de encuestas enviadas es exactamente 1", encuestasEnviadas.get() == 1);

        System.out.println("\n=== RESULTADO PRUEBA ESTADOS: " + ok + " OK, " + fallo + " FALLO ===");
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