package patrones.creacionales.builder;

/**
 * Patrón Builder (Creacional):
 * Representa un expediente administrativo municipal construido paso a paso.
 * GRASP: Experto en Información - conoce y mantiene todos los datos intrínsecos del expediente.
 * SOLID: S (Responsabilidad Única) - encapsula el estado inmutable de un trámite ciudadano.
 */
public class Expediente {

    private final String codigo;
    private final String estado;
    private final String tipoDocumento;
    private final String dni;
    private final String nombreCiudadano;
    private final String correo;
    private final String fechaRegistro;

    /**
     * Builder: construye el producto final inmutable usando los valores acumulados.
     */
    private Expediente(ExpedienteBuilder builder) {
        this.codigo = builder.codigo;
        this.estado = builder.estado;
        this.tipoDocumento = builder.tipoDocumento;
        this.dni = builder.dni;
        this.nombreCiudadano = builder.nombreCiudadano;
        this.correo = builder.correo;
        this.fechaRegistro = builder.fechaRegistro;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getEstado() {
        return estado;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getDni() {
        return dni;
    }

    public String getNombreCiudadano() {
        return nombreCiudadano;
    }

    public String getCorreo() {
        return correo;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    /**
     * Patrón Builder / GRASP: Creador:
     * Constructor estático interno encargado de ensamblar progresivamente la entidad Expediente.
     */
    public static class ExpedienteBuilder {

        private String codigo;
        private String estado;
        private String tipoDocumento;
        private String dni;
        private String nombreCiudadano;
        private String correo;
        private String fechaRegistro;

        public ExpedienteBuilder setCodigo(String codigo) {
            this.codigo = codigo;
            return this;
        }

        public ExpedienteBuilder setEstado(String estado) {
            this.estado = estado;
            return this;
        }

        public ExpedienteBuilder setTipoDocumento(String tipoDocumento) {
            this.tipoDocumento = tipoDocumento;
            return this;
        }

        public ExpedienteBuilder setDni(String dni) {
            this.dni = dni;
            return this;
        }

        public ExpedienteBuilder setNombreCiudadano(String nombreCiudadano) {
            this.nombreCiudadano = nombreCiudadano;
            return this;
        }

        public ExpedienteBuilder setCorreo(String correo) {
            this.correo = correo;
            return this;
        }

        public ExpedienteBuilder setFechaRegistro(String fechaRegistro) {
            this.fechaRegistro = fechaRegistro;
            return this;
        }

        public Expediente build() {
            return new Expediente(this);
        }
    }

    /**
     * Producto Builder: muestra la informacion del expediente construido.
     */
    public void mostrarDetalle() {
        StringBuilder sb = new StringBuilder();
        sb.append("Expediente: ").append(codigo).append(" | Estado: ").append(estado);
        if (tipoDocumento != null) {
            sb.append(" | Tipo: ").append(tipoDocumento);
        }
        if (dni != null) {
            sb.append(" | DNI: ").append(dni);
        }
        if (nombreCiudadano != null) {
            sb.append(" | Ciudadano: ").append(nombreCiudadano);
        }
        System.out.println(sb.toString());
    }
}
