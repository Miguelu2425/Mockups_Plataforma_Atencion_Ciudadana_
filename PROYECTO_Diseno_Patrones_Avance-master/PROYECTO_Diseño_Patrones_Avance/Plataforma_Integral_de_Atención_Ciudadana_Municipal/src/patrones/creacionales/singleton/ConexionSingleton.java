package patrones.creacionales.singleton;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Singleton: centraliza la unica conexion reutilizable con SQL Server.
 * Implementa double-checked locking para seguridad en entornos multihilo.
 */
public final class ConexionSingleton {

    private static final String ARCHIVO_CONFIG = "config.properties";

    private static volatile ConexionSingleton instancia;
    private Connection conexion;

    private final Properties archivo = new Properties();
    private Path rutaConfigEncontrada;
    private final List<Path> rutasProbadas = new ArrayList<>();

    private final String url;
    private final String usuario;
    private final String clave;

    /**
     * Singleton: carga la configuracion de conexion sin exponer credenciales
     * dentro del codigo fuente.
     */
    private ConexionSingleton() {
        cargarArchivo();

        String servidor   = obtenerConfiguracion("DB_SERVER", "localhost");
        String puerto     = obtenerConfiguracion("DB_PORT",   "1433");
        String baseDatos  = obtenerConfiguracion("DB_NAME",   "Municipalidad");
        String encrypt    = obtenerConfiguracion("DB_ENCRYPT", "true");
        String trustCert  = obtenerConfiguracion("DB_TRUST_SERVER_CERTIFICATE", "true");

        this.url = "jdbc:sqlserver://" + servidor + ":" + puerto
                + ";databaseName=" + baseDatos
                + ";encrypt=" + encrypt
                + ";trustServerCertificate=" + trustCert
                + ";loginTimeout=5";

        this.usuario = obtenerConfiguracion("DB_USER",     "");
        this.clave   = obtenerConfiguracion("DB_PASSWORD", "");
    }

    /**
     * Singleton: devuelve la instancia unica de conexion de forma segura
     * cuando la aplicacion es utilizada por varios hilos.
     *
     * @return instancia unica de ConexionSingleton
     */
    public static ConexionSingleton getInstancia() {
        if (instancia == null) {
            synchronized (ConexionSingleton.class) {
                if (instancia == null) {
                    instancia = new ConexionSingleton();
                    System.out.println("Singleton: creando la unica instancia de conexion");
                }
            }
        }
        return instancia;
    }

    /**
     * Singleton: abre la conexion una sola vez y reutiliza una conexion activa.
     *
     * @return conexion a la base de datos Municipalidad
     * @throws SQLException si faltan credenciales o SQL Server no esta disponible
     */
    public synchronized Connection getConexion() throws SQLException {
        validarCredenciales();

        if (conexion == null || conexion.isClosed()) {
            conexion = DriverManager.getConnection(url, usuario, clave);
        } else {
            System.out.println("Singleton: reutilizando la conexion existente");
        }
        return conexion;
    }

    /**
     * Singleton: cierra la conexion compartida al finalizar la aplicacion.
     *
     * @throws SQLException si SQL Server informa un error al cerrar
     */
    public synchronized void cerrarConexion() throws SQLException {
        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
        }
    }

    /**
     * Singleton: busca config.properties en user.dir y en hasta 3 carpetas
     * superiores para que funcione tanto desde la raiz como desde subdirectorios de IDEs.
     */
    private void cargarArchivo() {
        Path actual = Paths.get("").toAbsolutePath();
        Path dir = actual;
        for (int i = 0; i <= 3 && dir != null; i++) {
            Path candidata = dir.resolve(ARCHIVO_CONFIG);
            rutasProbadas.add(candidata);
            if (Files.exists(candidata)) {
                rutaConfigEncontrada = candidata;
                break;
            }
            dir = dir.getParent();
        }

        if (rutaConfigEncontrada != null) {
            try (Reader lector = new InputStreamReader(
                    Files.newInputStream(rutaConfigEncontrada), StandardCharsets.UTF_8)) {
                archivo.load(lector);
                System.out.println("Singleton: config.properties encontrado en: "
                        + rutaConfigEncontrada.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("No se pudo leer " + rutaConfigEncontrada
                        + ": " + e.getMessage());
            }
        }
    }

    /**
     * Singleton: busca un valor en este orden: propiedad del sistema,
     * variable de entorno, config.properties y valor predeterminado.
     */
    public String obtenerConfiguracion(String nombre, String valorPorDefecto) {
        String valor = System.getProperty(nombre);
        if (valor == null || valor.trim().isEmpty()) {
            valor = System.getenv(nombre);
        }
        if (valor == null || valor.trim().isEmpty()) {
            valor = archivo.getProperty(nombre);
        }
        if (valor == null || valor.trim().isEmpty()) {
            return valorPorDefecto;
        }
        return valor.trim();
    }

    /**
     * Devuelve la URL JDBC final sin incluir credenciales.
     */
    public String getUrlSinCredenciales() {
        return url;
    }

    public Path getRutaConfigEncontrada() {
        return rutaConfigEncontrada;
    }

    public List<Path> getRutasProbadas() {
        return new ArrayList<>(rutasProbadas);
    }

    public boolean isUserDefinido() {
        return !usuario.isEmpty();
    }

    public boolean isPasswordDefinido() {
        return !clave.isEmpty();
    }

    /**
     * Singleton: evita intentar una conexion silenciosa sin usuario o clave.
     */
    private void validarCredenciales() throws SQLException {
        if (usuario.isEmpty() || clave.isEmpty()) {
            throw new SQLException(
                    "Configure DB_USER y DB_PASSWORD en config.properties "
                            + "o como variables de entorno.");
        }
    }
}