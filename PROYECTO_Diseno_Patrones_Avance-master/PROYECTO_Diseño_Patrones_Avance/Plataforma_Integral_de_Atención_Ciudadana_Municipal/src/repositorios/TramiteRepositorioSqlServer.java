package repositorios;

import modelos.Ciudadano;
import patrones.creacionales.builder.Expediente;
import patrones.creacionales.singleton.ConexionSingleton;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio: implementacion SQL Server que invoca los stored procedures
 * via ConexionSingleton. No cierra la Connection porque es compartida (Singleton).
 * Builder: usa ExpedienteBuilder para construir cada Expediente del ResultSet.
 */
public class TramiteRepositorioSqlServer implements TramiteRepositorio {

    /** {@inheritDoc} */
    @Override
    public Expediente registrar(Ciudadano ciudadano, String tipoTramite) {
        Connection conn = obtenerConexion();
        try (CallableStatement cs = conn.prepareCall("{call dbo.sp_RegistrarTramite(?,?,?,?)}")) {
            cs.setString(1, ciudadano.getDni());
            cs.setString(2, ciudadano.getNombre());
            cs.setString(3, ciudadano.getCorreo());
            cs.setString(4, capitalizar(tipoTramite));
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return construirExpediente(rs);
                }
            }
            throw new RepositorioException("sp_RegistrarTramite no devolvio filas.");
        } catch (SQLException ex) {
            throw new RepositorioException("Error al registrar tramite: " + ex.getMessage(), ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Expediente cambiarEstado(String codigo, String estadoNuevo) {
        validarEstadoOficial(estadoNuevo);
        Connection conn = obtenerConexion();
        try (CallableStatement cs = conn.prepareCall("{call dbo.sp_CambiarEstado(?,?)}")) {
            cs.setString(1, codigo);
            cs.setString(2, estadoNuevo);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return construirExpediente(rs);
                }
            }
            throw new RepositorioException("sp_CambiarEstado no devolvio filas para: " + codigo);
        } catch (SQLException ex) {
            throw new RepositorioException("Error al cambiar estado: " + ex.getMessage(), ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Expediente buscarPorCodigo(String codigo) {
        Connection conn = obtenerConexion();
        try (CallableStatement cs = conn.prepareCall("{call dbo.sp_ObtenerTramite(?)}")) {
            cs.setString(1, codigo);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return construirExpediente(rs);
                }
            }
            throw new RepositorioException("Expediente no encontrado: " + codigo);
        } catch (SQLException ex) {
            throw new RepositorioException("Error al buscar tramite: " + ex.getMessage(), ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void eliminar(String codigo) {
        Connection conn = obtenerConexion();
        try (CallableStatement cs = conn.prepareCall("{call dbo.sp_EliminarTramite(?)}")) {
            cs.setString(1, codigo);
            cs.execute();
        } catch (SQLException ex) {
            throw new RepositorioException("Error al eliminar tramite: " + ex.getMessage(), ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Expediente> listar(String dniOpcional) {
        Connection conn = obtenerConexion();
        List<Expediente> lista = new ArrayList<>();
        try (CallableStatement cs = conn.prepareCall("{call dbo.sp_ListarTramites(?)}")) {
            if (dniOpcional != null) {
                cs.setString(1, dniOpcional);
            } else {
                cs.setNull(1, Types.CHAR);
            }
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(construirExpediente(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RepositorioException("Error al listar tramites: " + ex.getMessage(), ex);
        }
        return lista;
    }

    /** {@inheritDoc} */
    @Override
    public String descripcionOrigen() {
        return "conectado a SQL Server mediante ConexionSingleton";
    }

    private Connection obtenerConexion() {
        try {
            return ConexionSingleton.getInstancia().getConexion();
        } catch (SQLException ex) {
            throw new RepositorioException(
                    "No se pudo obtener conexion Singleton: " + ex.getMessage(), ex);
        }
    }

    /**
     * Builder: construye un Expediente con los datos del ResultSet incluyendo correo.
     */
    private Expediente construirExpediente(ResultSet rs) throws SQLException {
        String correo = null;
        try {
            correo = rs.getString("correo");
        } catch (SQLException ignored) {}

        return new Expediente.ExpedienteBuilder()
                .setCodigo(rs.getString("codigo"))
                .setEstado(rs.getString("estado"))
                .setTipoDocumento(rs.getString("tipo_tramite"))
                .setDni(rs.getString("dni"))
                .setNombreCiudadano(rs.getString("ciudadano"))
                .setCorreo(correo)
                .setFechaRegistro(rs.getString("fecha_registro"))
                .build();
    }

    private void validarEstadoOficial(String estado) {
        if (!"En Revision".equals(estado) && !"Atendido".equals(estado) && !"Cerrado".equals(estado)) {
            throw new RepositorioException("Estado no valido: " + estado
                    + ". Solo se permiten los estados oficiales: En Revision, Atendido, Cerrado.");
        }
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1).toLowerCase();
    }
}