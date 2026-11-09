package modelos;

/**
 * Modelo de datos utilizado por la Facade para registrar al ciudadano.
 */
public class Ciudadano {

    private String dni;
    private String nombre;
    private String correo;

    /**
     * Modelo: crea un ciudadano con sus datos básicos de contacto.
     *
     * @param dni    numero de documento de identidad (8 digitos)
     * @param nombre nombre completo del ciudadano
     * @param correo correo electronico de contacto
     */
    public Ciudadano(String dni, String nombre, String correo) {
        this.dni = dni;
        this.nombre = nombre;
        this.correo = correo;
    }

    /**
     * Modelo: devuelve el DNI del ciudadano.
     *
     * @return dni numero de documento de identidad
     */
    public String getDni() {
        return dni;
    }

    /**
     * Modelo: devuelve el nombre del ciudadano.
     *
     * @return nombre completo
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Modelo: devuelve el correo usado por la Facade para notificar.
     *
     * @return correo electronico de contacto
     */
    public String getCorreo() {
        return correo;
    }
}