package pruebas;

import modelos.Ciudadano;
import patrones.comportamiento.observer.CambioEstado;
import patrones.comportamiento.observer.ObservadorConsola;
import patrones.comportamiento.observer.ObservadorEncuesta;
import patrones.comportamiento.observer.ObservadorTramite;
import patrones.creacionales.builder.Expediente;
import patrones.estructurales.facade.MesaPartesFacade;
import repositorios.RepositorioException;
import repositorios.TramiteRepositorioMemoria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Prueba unitaria y de integracion: verifica la implementacion completa de los
 * patrones Command y Memento para la funcionalidad de deshacer (Undo).
 */
public class PruebaComandos {

    private static int ok = 0;
    private static int fallo = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println(" INICIANDO PRUEBAS DE PATRONES COMMAND Y MEMENTO");
        System.out.println("======================================================================");

        // Caso 1: Registrar tramite y deshacer registro -> el tramite es eliminado
        probarDeshacerRegistro();

        // Caso 2: Registrar -> avanzar a Atendido -> deshacer avance -> vuelve a En Revision
        probarDeshacerAvanceSimple();

        // Caso 3: Avances multiples (En Revision -> Atendido -> Cerrado) y desapilado sucesivo
        probarDeshacerMultipleCompleto();

        // Caso 4: Deshacer con pila vacia retorna null sin excepciones
        probarDeshacerPilaVacia();

        // Caso 5: Regla de negocio: no se puede eliminar tramite que no este en 'En Revision'
        probarRestriccionEliminarSoloEnRevision();

        // Caso 6: Notificaciones de reversion a observadores (esReversion = true)
        probarNotificacionesReversionYEncuesta();

        // Caso 7: Intento de avance desde estado terminal no genera comando en la pila
        probarAvanceTerminalNoAfectaPila();

        System.out.println("\n=== RESULTADO PRUEBA COMANDOS: " + ok + " OK, " + fallo + " FALLO ===");
        if (fallo > 0) {
            throw new AssertionError("PruebaComandos finalizo con " + fallo + " fallos.");
        }
    }

    private static void probarDeshacerRegistro() {
        System.out.println("\n--- [Caso 1] Deshacer registro de tramite ---");
        TramiteRepositorioMemoria repo = new TramiteRepositorioMemoria();
        MesaPartesFacade facade = new MesaPartesFacade(repo, Arrays.asList(new ObservadorConsola()));

        Ciudadano c = new Ciudadano("12345678", "Ana Rios", "ana@test.com");
        Expediente exp = facade.registrarNuevoTramite(c, "solicitud");
        verificar("Tramite creado en repositorio", exp != null);
        verificar("puedeDeshacer() retorna true tras registro", facade.puedeDeshacer());

        String accionDeshecha = facade.deshacerUltimaAccion();
        verificar("Accion deshecha contiene descripcion de registro",
                accionDeshecha != null && accionDeshecha.contains("Registrar tramite"));

        boolean noExiste = false;
        try {
            repo.buscarPorCodigo(exp.getCodigo());
        } catch (RepositorioException ex) {
            noExiste = true;
        }
        verificar("Tramite fue eliminado del repositorio tras deshacer", noExiste);
        verificar("puedeDeshacer() retorna false tras vaciar pila", !facade.puedeDeshacer());
    }

    private static void probarDeshacerAvanceSimple() {
        System.out.println("\n--- [Caso 2] Deshacer avance simple (En Revision -> Atendido) ---");
        TramiteRepositorioMemoria repo = new TramiteRepositorioMemoria();
        MesaPartesFacade facade = new MesaPartesFacade(repo, Arrays.asList(new ObservadorConsola()));

        Ciudadano c = new Ciudadano("87654321", "Pedro Gomez", "pedro@test.com");
        Expediente exp = facade.registrarNuevoTramite(c, "reclamo");

        facade.avanzarEstado(exp.getCodigo());
        Expediente atendido = repo.buscarPorCodigo(exp.getCodigo());
        verificar("Tramite paso a 'Atendido'", "Atendido".equals(atendido.getEstado()));

        String deshecho = facade.deshacerUltimaAccion();
        verificar("Descripcion de accion deshecha es de avance",
                deshecho != null && deshecho.contains("Avanzar " + exp.getCodigo()));

        Expediente revertido = repo.buscarPorCodigo(exp.getCodigo());
        verificar("Estado revertido exitosamente a 'En Revision'",
                "En Revision".equals(revertido.getEstado()));
    }

    private static void probarDeshacerMultipleCompleto() {
        System.out.println("\n--- [Caso 3] Deshacer sucesivo de ciclo completo ---");
        TramiteRepositorioMemoria repo = new TramiteRepositorioMemoria();
        MesaPartesFacade facade = new MesaPartesFacade(repo, Arrays.asList(new ObservadorConsola()));

        Ciudadano c = new Ciudadano("44556677", "Lucia Mendez", "lucia@test.com");
        Expediente exp = facade.registrarNuevoTramite(c, "solicitud");
        facade.avanzarEstado(exp.getCodigo()); // -> Atendido
        facade.avanzarEstado(exp.getCodigo()); // -> Cerrado

        Expediente cerrado = repo.buscarPorCodigo(exp.getCodigo());
        verificar("Tramite llego a estado 'Cerrado'", "Cerrado".equals(cerrado.getEstado()));

        // Deshacer 1: Cerrado -> Atendido
        facade.deshacerUltimaAccion();
        Expediente vueltoAtendido = repo.buscarPorCodigo(exp.getCodigo());
        verificar("Deshacer 1: Vuelve a 'Atendido'", "Atendido".equals(vueltoAtendido.getEstado()));

        // Deshacer 2: Atendido -> En Revision
        facade.deshacerUltimaAccion();
        Expediente vueltoRevision = repo.buscarPorCodigo(exp.getCodigo());
        verificar("Deshacer 2: Vuelve a 'En Revision'", "En Revision".equals(vueltoRevision.getEstado()));

        // Deshacer 3: En Revision -> Eliminado
        facade.deshacerUltimaAccion();
        boolean eliminado = false;
        try {
            repo.buscarPorCodigo(exp.getCodigo());
        } catch (RepositorioException ex) {
            eliminado = true;
        }
        verificar("Deshacer 3: Registro deshecho y expediente eliminado", eliminado);
        verificar("Pila de comandos queda vacia", !facade.puedeDeshacer());
    }

    private static void probarDeshacerPilaVacia() {
        System.out.println("\n--- [Caso 4] Deshacer sobre pila vacia ---");
        TramiteRepositorioMemoria repo = new TramiteRepositorioMemoria();
        MesaPartesFacade facade = new MesaPartesFacade(repo, Arrays.asList(new ObservadorConsola()));

        verificar("puedeDeshacer() inicial es false", !facade.puedeDeshacer());
        verificar("descripcionUltimaAccion() inicial es null", facade.descripcionUltimaAccion() == null);
        String resultado = facade.deshacerUltimaAccion();
        verificar("deshacerUltimaAccion() en vacio retorna null sin error", resultado == null);
    }

    private static void probarRestriccionEliminarSoloEnRevision() {
        System.out.println("\n--- [Caso 5] Restriccion de eliminar solo en estado 'En Revision' ---");
        TramiteRepositorioMemoria repo = new TramiteRepositorioMemoria();
        Ciudadano c = new Ciudadano("99887766", "Mario Ruiz", "mario@test.com");
        Expediente exp = repo.registrar(c, "reclamo");
        repo.cambiarEstado(exp.getCodigo(), "Atendido");

        boolean excepcionLanzada = false;
        try {
            repo.eliminar(exp.getCodigo());
        } catch (RepositorioException ex) {
            excepcionLanzada = ex.getMessage().contains("Solo se puede deshacer el registro");
        }
        verificar("Eliminar expediente 'Atendido' lanza RepositorioException", excepcionLanzada);
    }

    private static void probarNotificacionesReversionYEncuesta() {
        System.out.println("\n--- [Caso 6] Notificaciones Observer en reversiones ---");
        TramiteRepositorioMemoria repo = new TramiteRepositorioMemoria();

        List<CambioEstado> eventosReversion = new ArrayList<>();
        ObservadorTramite observadorVerificador = cambio -> {
            if (cambio.isEsReversion()) {
                eventosReversion.add(cambio);
            }
        };

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
                Arrays.asList(new ObservadorConsola(), observadorVerificador, observadorEncuesta)
        );

        Ciudadano c = new Ciudadano("33221100", "Elena Diaz", "elena@test.com");
        Expediente exp = facade.registrarNuevoTramite(c, "solicitud");
        facade.avanzarEstado(exp.getCodigo()); // -> Atendido
        facade.avanzarEstado(exp.getCodigo()); // -> Cerrado (dispara encuesta 1)
        verificar("Encuesta enviada al llegar a 'Cerrado'", encuestasEnviadas.get() == 1);

        // Revertir: Cerrado -> Atendido
        facade.deshacerUltimaAccion();
        verificar("Observador recibio evento de reversion", eventosReversion.size() == 1);
        CambioEstado rev1 = eventosReversion.get(0);
        verificar("Evento esReversion es true", rev1.isEsReversion());
        verificar("Estado anterior en reversion es 'Cerrado'", "Cerrado".equals(rev1.getEstadoAnterior()));
        verificar("Estado nuevo en reversion es 'Atendido'", "Atendido".equals(rev1.getEstadoNuevo()));
        verificar("Encuesta NO se vuelve a enviar en reversion", encuestasEnviadas.get() == 1);
    }

    private static void probarAvanceTerminalNoAfectaPila() {
        System.out.println("\n--- [Caso 7] Avance desde terminal no agrega comando a la pila ---");
        TramiteRepositorioMemoria repo = new TramiteRepositorioMemoria();
        MesaPartesFacade facade = new MesaPartesFacade(repo, Arrays.asList(new ObservadorConsola()));

        Ciudadano c = new Ciudadano("55443322", "Jorge Vega", "jorge@test.com");
        Expediente exp = facade.registrarNuevoTramite(c, "reclamo");
        facade.avanzarEstado(exp.getCodigo()); // -> Atendido
        facade.avanzarEstado(exp.getCodigo()); // -> Cerrado

        int tamanoAntes = facade.historialAcciones().size();
        facade.avanzarEstado(exp.getCodigo()); // Ya esta Cerrado, no cambia
        int tamanoDespues = facade.historialAcciones().size();

        verificar("Avance no operacional no incrementa la pila de deshacer", tamanoAntes == tamanoDespues);
        verificar("Ultima accion deshacible sigue siendo avanzar a Cerrado",
                facade.descripcionUltimaAccion().contains("Atendido -> Cerrado"));
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

