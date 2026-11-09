package patrones.estructurales.facade;

import modelos.Ciudadano;
import patrones.comportamiento.command.AvanzarEstadoComando;
import patrones.comportamiento.command.InvocadorComandos;
import patrones.comportamiento.command.RegistrarTramiteComando;
import patrones.comportamiento.memento.HistorialMementos;
import patrones.comportamiento.observer.CambioEstado;
import patrones.comportamiento.observer.ObservadorCiudadano;
import patrones.comportamiento.observer.ObservadorConsola;
import patrones.comportamiento.observer.ObservadorEncuesta;
import patrones.comportamiento.observer.ObservadorTramite;
import patrones.creacionales.builder.Expediente;
import patrones.estructurales.adapter.AdaptadorSistemaAntiguo;
import patrones.estructurales.adapter.RegistroQuejaAntigua;
import patrones.estructurales.adapter.SistemaAntiguoQuejas;
import patrones.estructurales.composite.GrupoTramites;
import patrones.estructurales.proxy.SolicitudRechazadaException;
import repositorios.RepositorioException;
import repositorios.RepositorioFactory;
import repositorios.TramiteRepositorio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Facade: simplifica el registro coordinando Factory, Builder, Repositorio,
 * State, Observer, Command, Memento, Adapter, Composite y Proxy.
 * GRASP Controlador / Fachada: punto de entrada de alto nivel para los casos de uso.
 */
public class MesaPartesFacade {

    private final TramiteRepositorio repositorio;
    private final List<ObservadorTramite> observadores = new ArrayList<>();
    private final InvocadorComandos invocador = new InvocadorComandos();
    private final HistorialMementos historialMementos = new HistorialMementos();

    /**
     * Facade: constructor con inyeccion de dependencias completa.
     *
     * @param repositorio  repositorio a utilizar
     * @param observadores lista inicial de observadores
     */
    public MesaPartesFacade(TramiteRepositorio repositorio, List<ObservadorTramite> observadores) {
        this.repositorio = repositorio;
        if (observadores != null) {
            this.observadores.addAll(observadores);
        }
    }

    /**
     * Facade: constructor con repositorio inyectado y observadores por defecto
     * (ObservadorConsola, ObservadorCiudadano, ObservadorEncuesta).
     *
     * @param repositorio implementacion del repositorio a usar
     */
    public MesaPartesFacade(TramiteRepositorio repositorio) {
        this(repositorio, Arrays.asList(
                new ObservadorConsola(),
                new ObservadorCiudadano(),
                new ObservadorEncuesta()
        ));
    }

    /**
     * Facade: constructor sin parametros que usa RepositorioFactory y observadores por defecto.
     */
    public MesaPartesFacade() {
        this(RepositorioFactory.crear());
    }

    /**
     * Patron Observer: permite suscribir un nuevo observador a la Facade.
     *
     * @param observador observador a agregar
     */
    public void agregarObservador(ObservadorTramite observador) {
        if (observador != null) {
            observadores.add(observador);
        }
    }

    /**
     * Facade y Patron Command: orquesta la creacion del tramite mediante
     * un comando ejecutable y reversible, persistiendo en el repositorio
     * y notificando el evento de registro a los observadores.
     *
     * @param ciudadano   ciudadano solicitante
     * @param tipoTramite tipo de tramite ("reclamo" o "solicitud")
     * @return expediente creado con codigo unico real
     */
    public Expediente registrarNuevoTramite(Ciudadano ciudadano, String tipoTramite) {
        RegistrarTramiteComando comando = new RegistrarTramiteComando(
                repositorio,
                ciudadano,
                tipoTramite,
                observadores
        );
        invocador.ejecutar(comando);
        return comando.getExpedienteCreado();
    }

    /**
     * Patrón Facade, Adapter y Composite:
     * Importa las quejas del sistema antiguo. Carga los registros heredados,
     * los adapta con AdaptadorSistemaAntiguo, compone el lote con GrupoTramites
     * y registra cada uno individualmente pasando por Proxy, Command, Repositorio y Observers.
     *
     * @return resultado consolidado de la importación
     */
    public ResultadoImportacion importarQuejasAntiguas() {
        ResultadoImportacion resultado = new ResultadoImportacion();
        SistemaAntiguoQuejas sistemaAntiguo = new SistemaAntiguoQuejas();
        List<RegistroQuejaAntigua> registros = sistemaAntiguo.obtenerQuejasHeredadas();

        // 1. Adaptar y agrupar con Composite
        GrupoTramites grupo = new GrupoTramites();
        List<AdaptadorSistemaAntiguo> adaptadores = new ArrayList<>();
        for (RegistroQuejaAntigua reg : registros) {
            AdaptadorSistemaAntiguo adaptador = new AdaptadorSistemaAntiguo(reg, sistemaAntiguo);
            adaptadores.add(adaptador);
            grupo.agregarTramite(adaptador);
        }

        // 2. Procesar el lote de forma uniforme con Composite
        grupo.procesar();

        // 3. Registrar cada trámite adaptado en el flujo completo
        for (AdaptadorSistemaAntiguo adaptador : adaptadores) {
            RegistroQuejaAntigua reg = adaptador.getRegistro();
            String descHeredado = reg.getId() + " (DNI: " + reg.getDni() + ", " + reg.getNombreCompleto() + ")";
            try {
                Expediente exp = registrarNuevoTramite(adaptador.getCiudadano(), adaptador.getTipoTramite());
                resultado.agregarImportado(exp);
            } catch (SolicitudRechazadaException ex) {
                resultado.agregarRechazado(descHeredado, ex.getMessage());
            } catch (RepositorioException ex) {
                resultado.agregarRechazado(descHeredado, ex.getMessage());
            } catch (Exception ex) {
                resultado.agregarRechazado(descHeredado, ex.getMessage());
            }
        }

        return resultado;
    }

    /**
     * Patron State, Command, Memento y Observer:
     * Avanza el estado del expediente mediante un comando reversible.
     * Guarda una instantánea Memento del estado previo, persiste el cambio,
     * actualiza el seguimiento y notifica a los observadores.
     *
     * @param codigo codigo unico del expediente
     * @return expediente actualizado
     */
    public Expediente avanzarEstado(String codigo) {
        AvanzarEstadoComando comando = new AvanzarEstadoComando(
                repositorio,
                codigo,
                historialMementos,
                observadores
        );
        invocador.ejecutar(comando);
        return comando.getExpedienteActualizado();
    }

    /**
     * Patron Command: deshace la ultima accion ejecutada (registro o avance de estado).
     *
     * @return descripcion de la accion deshecha o null si no habia acciones
     */
    public String deshacerUltimaAccion() {
        return invocador.deshacerUltimo();
    }

    /**
     * Patron Command: verifica si existen acciones disponibles para deshacer.
     *
     * @return true si hay al menos una accion en la pila
     */
    public boolean puedeDeshacer() {
        return invocador.hayAccionesParaDeshacer();
    }

    /**
     * Patron Command: retorna la descripcion de la ultima accion ejecutable a deshacer.
     *
     * @return descripcion de la ultima accion o null si no hay ninguna
     */
    public String descripcionUltimaAccion() {
        return invocador.descripcionUltimaAccion();
    }

    /**
     * Patron Command: retorna la lista con las descripciones de las acciones en la pila de deshacer.
     *
     * @return lista de descripciones
     */
    public List<String> historialAcciones() {
        return invocador.historial();
    }

    /**
     * Facade: delega la consulta de tramites al repositorio activo.
     */
    public List<Expediente> listarTramites(String dniOpcional) {
        return repositorio.listar(dniOpcional);
    }

    /**
     * Facade: busca un expediente por su codigo unico para la pantalla de seguimiento.
     *
     * @param codigo codigo del expediente (ej. EXP-2026-0001)
     * @return expediente encontrado
     * @throws RepositorioException si no existe un expediente con ese codigo
     */
    public Expediente buscarTramite(String codigo) {
        return repositorio.buscarPorCodigo(codigo);
    }

    /**
     * Facade: delega el cambio de estado directo al repositorio activo.
     */
    public Expediente cambiarEstado(String codigo, String estadoNuevo) {
        return repositorio.cambiarEstado(codigo, estadoNuevo);
    }

    /**
     * Facade: describe el origen del repositorio activo.
     */
    public String descripcionRepositorio() {
        return repositorio.descripcionOrigen();
    }

    private void notificarObservadores(CambioEstado cambio) {
        for (ObservadorTramite o : observadores) {
            try {
                o.actualizar(cambio);
            } catch (Exception ex) {
                System.err.println("Advertencia: observador fallo: " + ex.getMessage());
            }
        }
    }
}
