package repositorios;

import modelos.Ciudadano;
import patrones.creacionales.builder.Expediente;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repositorio: implementacion en memoria para cuando SQL Server no esta disponible.
 * Builder: usa ExpedienteBuilder para construir cada Expediente.
 * Misma semantica de codigos y estados oficiales que SQL Server.
 */
public class TramiteRepositorioMemoria implements TramiteRepositorio {

    private final List<Expediente> almacen = new ArrayList<>();
    private final AtomicInteger secuencia = new AtomicInteger(0);
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** {@inheritDoc} */
    @Override
    public synchronized Expediente registrar(Ciudadano ciudadano, String tipoTramite) {
        int seq = secuencia.incrementAndGet();
        String anio = String.valueOf(LocalDateTime.now().getYear());
        String codigo = "EXP-" + anio + "-" + String.format("%04d", seq);
        String fecha = LocalDateTime.now().format(FMT);

        Expediente exp = new Expediente.ExpedienteBuilder()
                .setCodigo(codigo)
                .setEstado("En Revision")
                .setTipoDocumento(tipoTramite)
                .setDni(ciudadano.getDni())
                .setNombreCiudadano(ciudadano.getNombre())
                .setCorreo(ciudadano.getCorreo())
                .setFechaRegistro(fecha)
                .build();
        almacen.add(exp);
        return exp;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Expediente cambiarEstado(String codigo, String estadoNuevo) {
        validarEstadoOficial(estadoNuevo);

        for (int i = 0; i < almacen.size(); i++) {
            Expediente actual = almacen.get(i);
            if (actual.getCodigo().equals(codigo)) {
                Expediente actualizado = new Expediente.ExpedienteBuilder()
                        .setCodigo(actual.getCodigo())
                        .setEstado(estadoNuevo)
                        .setTipoDocumento(actual.getTipoDocumento())
                        .setDni(actual.getDni())
                        .setNombreCiudadano(actual.getNombreCiudadano())
                        .setCorreo(actual.getCorreo())
                        .setFechaRegistro(actual.getFechaRegistro())
                        .build();
                almacen.set(i, actualizado);
                return actualizado;
            }
        }
        throw new RepositorioException("Expediente no encontrado: " + codigo);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Expediente buscarPorCodigo(String codigo) {
        for (Expediente exp : almacen) {
            if (exp.getCodigo().equals(codigo)) {
                return exp;
            }
        }
        throw new RepositorioException("Expediente no encontrado: " + codigo);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void eliminar(String codigo) {
        for (int i = 0; i < almacen.size(); i++) {
            Expediente exp = almacen.get(i);
            if (exp.getCodigo().equals(codigo)) {
                if (!"En Revision".equals(exp.getEstado())) {
                    throw new RepositorioException(
                            "Solo se puede deshacer el registro de un tramite En Revision");
                }
                almacen.remove(i);
                return;
            }
        }
        throw new RepositorioException("Expediente no encontrado: " + codigo);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized List<Expediente> listar(String dniOpcional) {
        List<Expediente> resultado = new ArrayList<>();
        for (Expediente exp : almacen) {
            if (dniOpcional == null || dniOpcional.equals(exp.getDni())) {
                resultado.add(exp);
            }
        }
        Collections.sort(resultado, (a, b) -> b.getCodigo().compareTo(a.getCodigo()));
        return resultado;
    }

    /** {@inheritDoc} */
    @Override
    public String descripcionOrigen() {
        return "modo memoria (sin conexion)";
    }

    private void validarEstadoOficial(String estado) {
        if (!"En Revision".equals(estado) && !"Atendido".equals(estado) && !"Cerrado".equals(estado)) {
            throw new RepositorioException("Estado no valido: " + estado
                    + ". Solo se permiten los estados oficiales: En Revision, Atendido, Cerrado.");
        }
    }
}