package patrones.comportamiento.memento;

/**
 * Patrón Memento: representa una instantánea inmutable del estado interno de un expediente
 * en un momento determinado, permitiendo restaurar su estado previo sin violar el encapsulamiento.
 */
public class MementoSeguimiento {

    private final String codigo;
    private final String nombreEstado;
    private final long instante;

    /**
     * Patrón Memento: crea una instantánea con código, nombre del estado e instante temporal.
     *
     * @param codigo       código del expediente
     * @param nombreEstado nombre del estado oficial ("En Revision", "Atendido", "Cerrado")
     * @param instante     marca de tiempo en milisegundos
     */
    public MementoSeguimiento(String codigo, String nombreEstado, long instante) {
        this.codigo = codigo;
        this.nombreEstado = nombreEstado;
        this.instante = instante;
    }

    /**
     * Patrón Memento: crea una instantánea con la marca de tiempo actual del sistema.
     *
     * @param codigo       código del expediente
     * @param nombreEstado nombre del estado oficial
     */
    public MementoSeguimiento(String codigo, String nombreEstado) {
        this(codigo, nombreEstado, System.currentTimeMillis());
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombreEstado() {
        return nombreEstado;
    }

    public long getInstante() {
        return instante;
    }

    @Override
    public String toString() {
        return "MementoSeguimiento{" +
                "codigo='" + codigo + '\'' +
                ", nombreEstado='" + nombreEstado + '\'' +
                ", instante=" + instante +
                '}';
    }
}

