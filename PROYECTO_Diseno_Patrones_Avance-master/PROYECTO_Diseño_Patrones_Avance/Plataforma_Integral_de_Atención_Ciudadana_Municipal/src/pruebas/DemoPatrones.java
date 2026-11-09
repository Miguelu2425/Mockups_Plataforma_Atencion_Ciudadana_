package pruebas;

import patrones.creacionales.abstractfactory.FabricaAtencion;
import patrones.creacionales.abstractfactory.FabricaReclamo;
import patrones.creacionales.abstractfactory.FabricaSolicitud;
import patrones.creacionales.builder.Expediente;
import patrones.creacionales.factory.TramiteFactory;
import patrones.creacionales.prototype.EncuestaSatisfaccion;
import patrones.creacionales.singleton.ConexionSingleton;
import patrones.creacionales.singleton.ConfiguracionPlataforma;
import patrones.estructurales.adapter.AdaptadorSistemaAntiguo;
import patrones.estructurales.bridge.AlertaCiudadano;
import patrones.estructurales.bridge.CanalCorreo;
import patrones.estructurales.bridge.CanalSMS;
import patrones.estructurales.composite.GrupoTramites;
import patrones.estructurales.decorator.Notificador;
import patrones.estructurales.decorator.NotificadorBase;
import patrones.estructurales.decorator.NotificadorSMS;
import patrones.estructurales.proxy.TramiteProxy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Pruebas: demostración técnica de todos los patrones de diseño del curso.
 * Movido fuera de Main para mantener alta cohesión en la capa de vistas.
 */
public class DemoPatrones {

    /**
     * Demostración: ejecuta una vez cada patrón solicitado por el curso.
     */
    public static void demostrarPatrones() {
        System.out.println("=== INICIANDO DEMOSTRACION DE PATRONES DE DISENO ===");
        ConfiguracionPlataforma.getInstancia();
        EncuestaSatisfaccion prototipo = new EncuestaSatisfaccion();
        prototipo.clone().enviarEncuesta();
        TramiteFactory.crearTramite("reclamo").procesar();

        FabricaAtencion fabricaSolicitud = new FabricaSolicitud();
        fabricaSolicitud.crearTramite().procesar();
        fabricaSolicitud.crearEncuesta().enviarEncuesta();
        FabricaAtencion fabricaReclamo = new FabricaReclamo();
        fabricaReclamo.crearTramite().procesar();

        new Expediente.ExpedienteBuilder()
                .setCodigo("EXP-DEMO")
                .setEstado("En Revision")
                .setTipoDocumento("solicitud")
                .build()
                .mostrarDetalle();

        new AdaptadorSistemaAntiguo().procesar();
        GrupoTramites grupo = new GrupoTramites();
        grupo.agregarTramite(TramiteFactory.crearTramite("solicitud"));
        grupo.procesar();
        Notificador notificador = new NotificadorSMS(new NotificadorBase());
        notificador.enviar("Demostración Decorator");
        new TramiteProxy("reclamo", true).procesar();
        new AlertaCiudadano(new CanalCorreo()).enviar("Demostración Bridge");
        new AlertaCiudadano(new CanalSMS()).enviar("Demostración Bridge");
        System.out.println("=== DEMOSTRACION DE PATRONES FINALIZADA ===");
    }

    /**
     * Persistencia: prueba la conexión Singleton y muestra los tipos de trámite
     * disponibles en SQL Server, sin modificar la lógica de negocio.
     */
    public static void probarConexionBaseDatos() {
        System.out.println("=== PRUEBA DIRECTA DE BASE DE DATOS (SINGLETON) ===");
        String sql = "SELECT id_tipo_tramite, nombre, descripcion "
                + "FROM TipoTramite ORDER BY id_tipo_tramite";

        try (Connection conexion = ConexionSingleton.getInstancia().getConexion();
             Statement sentencia = conexion.createStatement();
             ResultSet resultados = sentencia.executeQuery(sql)) {

            System.out.println("Conectado");
            System.out.println("Contenido de TipoTramite:");
            while (resultados.next()) {
                System.out.println(
                        resultados.getInt("id_tipo_tramite") + " | "
                        + resultados.getString("nombre") + " | "
                        + resultados.getString("descripcion"));
            }
        } catch (SQLException ex) {
            System.err.println("Error exacto de conexión o consulta: "
                    + ex.getMessage());
        }
    }

    /**
     * Punto de entrada de prueba directa desde consola.
     */
    public static void main(String[] args) {
        demostrarPatrones();
        probarConexionBaseDatos();
    }
}

