package pruebas;

import patrones.creacionales.singleton.ConexionSingleton;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Diagnostico de conexion a SQL Server:
 * Imprime el estado del entorno, configuracion, red y conexion JDBC
 * sin exponer nunca credenciales de usuario o contrasena.
 */
public class DiagnosticoConexion {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println(" DIAGNOSTICO DE CONEXION - PLATAFORMA CIUDADANA MUNICIPAL");
        System.out.println("======================================================================");

        // a) Version de Java y directorio de trabajo (user.dir)
        System.out.println("a) Entorno de ejecucion:");
        System.out.println("   Java Version : " + System.getProperty("java.version")
                + " (" + System.getProperty("java.vendor") + ")");
        System.out.println("   user.dir     : " + System.getProperty("user.dir"));
        System.out.println();

        // b) Ruta absoluta de config.properties o NO ENCONTRADO, y estado de variables
        System.out.println("b) Archivo config.properties y variables:");
        ConexionSingleton singleton = ConexionSingleton.getInstancia();
        Path rutaEncontrada = singleton.getRutaConfigEncontrada();

        if (rutaEncontrada != null) {
            System.out.println("   Estado config: ENCONTRADO en " + rutaEncontrada.toAbsolutePath());
        } else {
            System.out.println("   Estado config: NO ENCONTRADO. Rutas probadas:");
            List<Path> probadas = singleton.getRutasProbadas();
            for (Path p : probadas) {
                System.out.println("     - " + p.toAbsolutePath());
            }
        }

        String server = singleton.obtenerConfiguracion("DB_SERVER", "localhost");
        String portStr = singleton.obtenerConfiguracion("DB_PORT", "1433");
        String dbName = singleton.obtenerConfiguracion("DB_NAME", "Municipalidad");
        boolean userDefinido = singleton.isUserDefinido();
        boolean passDefinido = singleton.isPasswordDefinido();

        System.out.println("   DB_SERVER   : " + (server != null ? "definido (valor: '" + server + "')" : "no definido"));
        System.out.println("   DB_PORT     : " + (portStr != null ? "definido (valor: '" + portStr + "')" : "no definido"));
        System.out.println("   DB_NAME     : " + (dbName != null ? "definido (valor: '" + dbName + "')" : "no definido"));
        System.out.println("   DB_USER     : " + (userDefinido ? "definido" : "no definido"));
        System.out.println("   DB_PASSWORD : " + (passDefinido ? "definido" : "no definido"));
        System.out.println();

        // c) URL JDBC final SIN credenciales
        System.out.println("c) URL JDBC (sin credenciales):");
        System.out.println("   " + singleton.getUrlSinCredenciales());
        System.out.println();

        // d) Prueba de red: Socket a servidor:puerto con timeout de 3 s
        System.out.println("d) Prueba de red (Socket TCP a " + server + ":" + portStr + "):");
        int puerto = 1433;
        try {
            puerto = Integer.parseInt(portStr);
        } catch (NumberFormatException ignored) {}

        boolean redOk = false;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(server, puerto), 3000);
            redOk = true;
            System.out.println("   Resultado: OK (puerto " + puerto + " abierto y respondiendo)");
        } catch (Exception ex) {
            System.out.println("   Resultado: FALLO (" + ex.getClass().getSimpleName() + ": " + ex.getMessage() + ")");
        }
        System.out.println();

        // e) Intento real de conexion con ConexionSingleton.getInstancia().getConexion()
        System.out.println("e) Intento real de conexion JDBC con SQL Server:");
        try {
            Connection conn = singleton.getConexion();
            if (conn != null && !conn.isClosed()) {
                System.out.println("   CONEXION OK");
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM TipoTramite")) {
                    if (rs.next()) {
                        System.out.println("   Consulta de prueba: SELECT COUNT(*) FROM TipoTramite -> "
                                + rs.getInt(1) + " registros");
                    }
                }
            } else {
                System.out.println("   FALLO: La conexion devuelta es nula o esta cerrada.");
            }
        } catch (Throwable ex) {
            System.out.println("   FALLO EN CONEXION JDBC");
            System.out.println("   --- Cadena de excepciones ---");

            Throwable actual = ex;
            int nivel = 1;
            while (actual != null) {
                System.out.println("   [Nivel " + nivel + "] " + actual.getClass().getName());
                String mensajeLimpio = sanitizar(actual.getMessage());
                System.out.println("      Mensaje: " + mensajeLimpio);
                if (actual instanceof SQLException) {
                    SQLException sqle = (SQLException) actual;
                    System.out.println("      SQLState:  " + sqle.getSQLState());
                    System.out.println("      ErrorCode: " + sqle.getErrorCode());
                }
                actual = actual.getCause();
                nivel++;
            }

            System.out.println();
            System.out.println("   --- Pista de solucion ---");
            imprimirPista(ex, server, puerto, dbName, userDefinido, passDefinido, redOk);
        }

        System.out.println("======================================================================");
    }

    private static void imprimirPista(Throwable ex, String server, int puerto,
                                      String dbName, boolean userDef, boolean passDef, boolean redOk) {
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        if (!userDef || !passDef) {
            System.out.println("   [Pista - Configuracion]: Faltan credenciales DB_USER o DB_PASSWORD.");
            System.out.println("   Asegurese de que config.properties exista y contenga DB_USER y DB_PASSWORD.");
            return;
        }

        if (!redOk || msg.contains("connection refused") || msg.contains("timed out")
                || msg.contains("getsockopt") || msg.contains("tcp/ip") || msg.contains("conexiÃ³n tcp/ip")) {
            System.out.println("   [Pista - Red / Puerto / Protocolo TCP/IP]:");
            System.out.println("   No se pudo establecer conexion TCP con " + server + ":" + puerto + ".");
            System.out.println("   Causas mas comunes y solucion:");
            System.out.println("   1. En SQL Server 2025/17.0 el protocolo TCP/IP viene DESHABILITADO por defecto.");
            System.out.println("      Solucion: Abra 'SQL Server Configuration Manager', vaya a");
            System.out.println("      'SQL Server Network Configuration' -> 'Protocols for MSSQLSERVER',");
            System.out.println("      haga doble clic en 'TCP/IP', cambie 'Enabled' a 'Yes' y reinicie el servicio MSSQLSERVER.");
            System.out.println("   2. Verifique en la pestana 'IP Addresses' de TCP/IP que 'IPAll' tenga TCP Port en 1433.");
            System.out.println("   3. Si esta en un sandbox o contenedor, recuerde que el aislamiento de red puede bloquear sockets locales.");
            return;
        }

        if (msg.contains("login failed") || msg.contains("inicio de sesiÃ³n") || msg.contains("18456")) {
            System.out.println("   [Pista - Login fallido / Autenticacion]:");
            System.out.println("   El usuario o clave configurados en config.properties fueron rechazados por SQL Server.");
            System.out.println("   Verifique en SSMS las propiedades del servidor -> Security -> 'SQL Server and Windows Authentication mode'.");
            return;
        }

        if (msg.contains("ssl") || msg.contains("tls") || msg.contains("certificate") || msg.contains("certificado") || msg.contains("pkix")) {
            System.out.println("   [Pista - Certificado SSL/TLS]:");
            System.out.println("   Hubo un fallo en la negociacion segura TLS.");
            System.out.println("   La URL ya incluye 'encrypt=true;trustServerCertificate=true;loginTimeout=5'.");
            System.out.println("   Verifique que Java soporte el protocolo TLS de la version instalada de SQL Server.");
            return;
        }

        if (msg.contains("database") || msg.contains("base de datos") || msg.contains("4060")) {
            System.out.println("   [Pista - Base de datos no encontrada]:");
            System.out.println("   No se encontro la base de datos '" + dbName + "'.");
            System.out.println("   Ejecute el script 'bd/script_municipalidad.sql' en SSMS para crearla.");
            return;
        }

        System.out.println("   [Pista general]:");
        System.out.println("   Revise el log de errores de SQL Server (ERRORLOG) y verifique las variables en config.properties.");
    }

    private static String sanitizar(String texto) {
        if (texto == null) return "null";
        return texto.replaceAll("(?i)user\\s+'[^']*'", "user '***'")
                    .replaceAll("(?i)password\\s*=\\s*[^;\\s]*", "password=***");
    }
}