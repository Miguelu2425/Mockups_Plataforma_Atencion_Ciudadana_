package repositorios;

import modelos.Ciudadano;
import patrones.creacionales.builder.Expediente;
import java.util.List;

/**
 * Patrón Repository (Arquitectura):
 * Abstrae las operaciones de persistencia del expediente ciudadano.
 * GRASP: Fabricación Pura - clase técnica que no representa un concepto del dominio pero otorga alta cohesión.
 * GRASP: Polimorfismo - permite intercambiar implementaciones (SQL Server y memoria) de forma transparente.
 * SOLID: D (Inversión de Dependencias) - el dominio depende de esta interfaz y no de controladores JDBC o SQL.
 * SOLID: L (Sustitución de Liskov) - las implementaciones en memoria y SQL Server son perfectamente intercambiables.
 */
public interface TramiteRepositorio {

    /**
     * Repositorio: registra un nuevo tramite y devuelve el expediente creado.
     *
     * @param ciudadano   ciudadano solicitante
     * @param tipoTramite tipo de tramite (ej. "Reclamo" o "Solicitud")
     * @return expediente con codigo unico asignado
     */
    Expediente registrar(Ciudadano ciudadano, String tipoTramite);

    /**
     * Repositorio: cambia el estado de un expediente identificado por su codigo.
     * Solo se permiten los estados oficiales: "En Revision", "Atendido", "Cerrado".
     *
     * @param codigo      codigo del expediente (ej. EXP-2026-0001)
     * @param estadoNuevo nuevo estado oficial a asignar
     * @return expediente actualizado
     */
    Expediente cambiarEstado(String codigo, String estadoNuevo);

    /**
     * Repositorio: busca un expediente por su codigo unico.
     *
     * @param codigo codigo del expediente
     * @return expediente encontrado
     * @throws RepositorioException si no se encuentra ningun expediente con ese codigo
     */
    Expediente buscarPorCodigo(String codigo);

    /**
     * Repositorio: elimina un expediente identificado por su codigo unico.
     * Patrón Command: soporte para la operacion reversible de deshacer registro.
     * Solo se permite eliminar expedientes en estado 'En Revision'.
     *
     * @param codigo codigo del expediente a eliminar
     * @throws RepositorioException si no se encuentra o no esta en estado 'En Revision'
     */
    void eliminar(String codigo);

    /**
     * Repositorio: lista expedientes; filtra por DNI solo si dniOpcional no es null.
     *
     * @param dniOpcional DNI del ciudadano o null para listar todos
     * @return lista ordenada por fecha de registro descendente
     */
    List<Expediente> listar(String dniOpcional);

    /**
     * Repositorio: describe el origen del repositorio activo.
     *
     * @return texto descriptivo del modo de persistencia
     */
    String descripcionOrigen();
}