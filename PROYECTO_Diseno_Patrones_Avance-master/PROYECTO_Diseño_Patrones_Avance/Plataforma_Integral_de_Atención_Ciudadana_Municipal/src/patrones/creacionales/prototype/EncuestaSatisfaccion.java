package patrones.creacionales.prototype;

/**
 * Prototype: objeto base que puede clonarse para reutilizar encuestas.
 */
public class EncuestaSatisfaccion implements Cloneable {

    private String preguntasBase;

    /**
     * Prototype: crea el prototipo base reutilizable de la encuesta.
     */
    public EncuestaSatisfaccion() {
        this.preguntasBase = "¿Como calificaria la atencion virtual?";
    }

    /**
     * Abstract Factory/Prototype: permite crear una encuesta con contexto y clonarla.
     */
    public EncuestaSatisfaccion(String preguntasBase) {
        this.preguntasBase = preguntasBase;
    }

    @Override
    /**
     * Prototype: devuelve una copia de la encuesta sin recrearla manualmente.
     */
    public EncuestaSatisfaccion clone() {
        try {
            return (EncuestaSatisfaccion) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public void enviarEncuesta() {
        System.out.println("Enviando encuesta: " + preguntasBase);
    }
}
