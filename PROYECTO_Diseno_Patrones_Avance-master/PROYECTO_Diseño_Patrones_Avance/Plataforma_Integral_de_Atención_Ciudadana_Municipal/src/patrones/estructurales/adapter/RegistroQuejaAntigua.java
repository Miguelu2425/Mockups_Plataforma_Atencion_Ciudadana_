package patrones.estructurales.adapter;

/**
 * Patrón Adapter: representa un registro heredado tal como existía en el sistema antiguo de quejas.
 * Contiene datos ficticios que requieren ser adaptados al formato moderno del portal.
 */
public class RegistroQuejaAntigua {

    private final String id;
    private final String dni;
    private final String nombreCompleto;
    private final String correo;
    private final String descripcionQueja;

    public RegistroQuejaAntigua(String id, String dni, String nombreCompleto, String correo, String descripcionQueja) {
        this.id = id;
        this.dni = dni;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.descripcionQueja = descripcionQueja;
    }

    public String getId() {
        return id;
    }

    public String getDni() {
        return dni;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public String getDescripcionQueja() {
        return descripcionQueja;
    }

    @Override
    public String toString() {
        return id + " | DNI: " + dni + " | " + nombreCompleto + " | " + descripcionQueja;
    }
}

