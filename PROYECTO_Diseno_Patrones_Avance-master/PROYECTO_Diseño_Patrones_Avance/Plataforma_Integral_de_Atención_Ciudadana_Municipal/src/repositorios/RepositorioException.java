package repositorios;

/**
 * Repositorio: excepcion no verificada que encapsula errores de persistencia.
 * Convierte SQLException u otras causas tecnicas en un error de dominio legible.
 */
public class RepositorioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Repositorio: crea una excepcion con mensaje descriptivo.
     *
     * @param mensaje descripcion del error
     */
    public RepositorioException(String mensaje) {
        super(mensaje);
    }

    /**
     * Repositorio: crea una excepcion con mensaje y causa original.
     *
     * @param mensaje descripcion del error
     * @param causa   excepcion tecnica original
     */
    public RepositorioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}