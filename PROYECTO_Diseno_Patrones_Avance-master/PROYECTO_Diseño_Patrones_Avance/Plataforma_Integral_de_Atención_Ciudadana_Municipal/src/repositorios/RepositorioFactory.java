package repositorios;

import patrones.creacionales.singleton.ConexionSingleton;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Repositorio: factoria estatica que elige entre SQL Server y memoria
 * segun disponibilidad de la base de datos.
 * Factory Method: encapsula la logica de seleccion del repositorio concreto.
 * Singleton: usa ConexionSingleton para verificar disponibilidad de la BD.
 */
public final class RepositorioFactory {

    private RepositorioFactory() {}

    /**
     * Factory Method: devuelve TramiteRepositorioSqlServer si el Singleton
     * tiene una conexion valida en menos de 2 segundos; de lo contrario
     * imprime aviso con motivo y devuelve TramiteRepositorioMemoria.
     *
     * @return repositorio activo segun disponibilidad de la BD
     */
    public static TramiteRepositorio crear() {
        try {
            Connection conn = ConexionSingleton.getInstancia().getConexion();
            if (conn != null && conn.isValid(2)) {
                return new TramiteRepositorioSqlServer();
            }
        } catch (SQLException ex) {
            System.out.println("BD no disponible: usando repositorio en memoria");
            String primerRenglon = extraerPrimerRenglon(ex.getMessage());
            System.out.println("Motivo: " + ex.getClass().getName() + ": " + primerRenglon);
        } catch (Exception ex) {
            System.out.println("BD no disponible: usando repositorio en memoria");
            String primerRenglon = extraerPrimerRenglon(ex.getMessage());
            System.out.println("Motivo: " + ex.getClass().getName() + ": " + primerRenglon);
        }
        return new TramiteRepositorioMemoria();
    }

    private static String extraerPrimerRenglon(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return "Sin mensaje detallado";
        }
        String primerRenglon = mensaje.split("\\r?\\n")[0].trim();
        // Sanitizar cualquier mencion accidental de usuario o clave
        return primerRenglon
                .replaceAll("(?i)user\\s+'[^']*'", "user '***'")
                .replaceAll("(?i)password\\s*=\\s*[^;\\s]*", "password=***");
    }
}