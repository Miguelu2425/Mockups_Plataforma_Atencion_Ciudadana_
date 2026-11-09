package controladores;

import modelos.Ciudadano;
import patrones.comportamiento.state.EstadoTramite;
import patrones.comportamiento.state.EstadosTramite;
import patrones.creacionales.builder.Expediente;
import patrones.creacionales.singleton.ConexionSingleton;
import patrones.creacionales.singleton.ConfiguracionPlataforma;
import patrones.estructurales.facade.MesaPartesFacade;
import patrones.estructurales.facade.ResultadoImportacion;

import java.util.List;

/**
 * Controlador MVC: conecta la vista con la Facade de mesa de partes.
 * GRASP: Controlador - es el primer objeto que recibe y coordina las solicitudes de la vista hacia la lógica de negocio.
 * SOLID: S (Responsabilidad Única) - su única función es intermediar entre la UI y el subsistema de atención ciudadana.
 * SOLID: D (Inversión de Dependencias) - admite inyección de dependencias de MesaPartesFacade para facilitar pruebas desacopladas.
 */
public class AtencionController {

    private final MesaPartesFacade mesaPartes;

    /**
     * Controlador: inicializa la Facade por defecto que coordina el caso de uso.
     */
    public AtencionController() {
        this(new MesaPartesFacade());
    }

    /**
     * Controlador: constructor con inyección de Facade para pruebas y personalización.
     * GRASP Controlador / Polimorfismo.
     *
     * @param mesaPartes fachada de mesa de partes
     */
    public AtencionController(MesaPartesFacade mesaPartes) {
        this.mesaPartes = mesaPartes;
    }

    /**
     * Controlador: recibe la solicitud de la vista y delega el registro a la Facade.
     *
     * @return expediente creado con codigo unico real
     */
    public Expediente atenderCiudadano(String dni, String nombre, String correo, String tramite) {
        Ciudadano ciudadano = new Ciudadano(dni, nombre, correo);
        return mesaPartes.registrarNuevoTramite(ciudadano, tramite);
    }

    /**
     * Patrón Facade, Adapter, Composite y Proxy:
     * Importa las quejas del sistema heredado procesándolas mediante el flujo completo.
     *
     * @return resultado consolidado de la importación
     */
    public ResultadoImportacion importarQuejasAntiguas() {
        return mesaPartes.importarQuejasAntiguas();
    }

    /**
     * Patron State y Observer: avanza el estado del tramite delegando en la Facade.
     *
     * @param codigo codigo unico del expediente
     * @return expediente actualizado
     */
    public Expediente avanzarEstado(String codigo) {
        return mesaPartes.avanzarEstado(codigo);
    }

    /**
     * Controlador: busca un tramite por su codigo unico (pantalla Seguimiento de tramite).
     *
     * @param codigo codigo del expediente
     * @return expediente encontrado
     */
    public Expediente buscarTramite(String codigo) {
        return mesaPartes.buscarTramite(codigo == null ? "" : codigo.trim().toUpperCase());
    }

    /**
     * Controlador: lista los tramites del ciudadano con el DNI indicado.
     *
     * @param dni DNI del ciudadano o null para listar todos
     * @return lista de expedientes
     */
    public List<Expediente> listarMisSolicitudes(String dni) {
        return mesaPartes.listarTramites(dni);
    }

    /**
     * Controlador: informa el origen del repositorio activo para mostrarlo en la vista.
     *
     * @return descripcion del origen de persistencia
     */
    public String estadoConexion() {
        return mesaPartes.descripcionRepositorio();
    }

    /**
     * Patron Command: deshace la ultima accion ejecutada (registro o cambio de estado).
     *
     * @return descripcion de la accion deshecha o null si no habia acciones
     */
    public String deshacerUltimaAccion() {
        return mesaPartes.deshacerUltimaAccion();
    }

    /**
     * Patron Command: consulta si hay acciones en la pila listas para deshacer.
     *
     * @return true si se puede deshacer; false si la pila esta vacia
     */
    public boolean puedeDeshacer() {
        return mesaPartes.puedeDeshacer();
    }

    /**
     * Patron Command: obtiene la descripcion de la ultima accion que se puede deshacer.
     *
     * @return descripcion de la accion o null si no hay ninguna
     */
    public String descripcionUltimaAccion() {
        return mesaPartes.descripcionUltimaAccion();
    }

    /**
     * Controlador: devuelve el historial de descripciones de acciones pendientes de deshacer.
     *
     * @return lista de descripciones
     */
    public List<String> historialAcciones() {
        return mesaPartes.historialAcciones();
    }

    /**
     * GRASP Alta Cohesión: devuelve la descripción detallada del estado oficial.
     * Evita que la vista conozca o importe la jerarquía de patrones.comportamiento.state.
     *
     * @param nombreEstado nombre oficial del estado ("En Revision", "Atendido", "Cerrado")
     * @return descripción legible del estado
     */
    public String descripcionEstado(String nombreEstado) {
        if (nombreEstado == null) return "Estado no especificado";
        try {
            return EstadosTramite.desdeNombre(nombreEstado).getDescripcion();
        } catch (Exception ex) {
            return "Estado desconocido: " + nombreEstado;
        }
    }

    /**
     * GRASP Alta Cohesión: devuelve el nombre del siguiente estado según el patrón State,
     * o "-" si el estado actual es final (Cerrado).
     *
     * @param nombreEstado nombre oficial del estado actual
     * @return nombre del siguiente estado o "-" si no avanza más
     */
    public String siguienteEstado(String nombreEstado) {
        if (nombreEstado == null) return "-";
        try {
            EstadoTramite estado = EstadosTramite.desdeNombre(nombreEstado);
            if (estado.esFinal()) {
                return "-";
            }
            return estado.siguiente().getNombre();
        } catch (Exception ex) {
            return "-";
        }
    }

    /**
     * GRASP Alta Cohesión: mapea el estado oficial a su término de situación para la interfaz:
     * "En espera" para "En Revision", "Atendido" para "Atendido", "Finalizado" para "Cerrado".
     *
     * @param nombreEstado nombre oficial del estado
     * @return texto de situación
     */
    public String situacionEstado(String nombreEstado) {
        if (nombreEstado == null) return "Desconocido";
        switch (nombreEstado) {
            case "En Revision":
                return "En espera";
            case "Atendido":
                return "Atendido";
            case "Cerrado":
                return "Finalizado";
            default:
                return nombreEstado;
        }
    }

    /**
     * Patron Singleton: obtiene el nombre del municipio desde ConfiguracionPlataforma.
     *
     * @return nombre del municipio configurado
     */
    public String nombreMunicipio() {
        return ConfiguracionPlataforma.getInstancia().getNombreMunicipio();
    }

    /**
     * Patron Singleton: realiza dos llamadas consecutivas a ConexionSingleton para evidenciar
     * la creación y posterior reutilización de la única instancia de conexión.
     *
     * @return texto informativo del estado de la conexión sin exponer credenciales
     */
    public String probarConexion() {
        try {
            ConexionSingleton singleton = ConexionSingleton.getInstancia();
            singleton.getConexion(); // Primera llamada: crea o reutiliza
            singleton.getConexion(); // Segunda llamada: reutiliza
            return "BD conectada: " + estadoConexion();
        } catch (Exception ex) {
            return "Sin conexion: modo memoria";
        }
    }
}