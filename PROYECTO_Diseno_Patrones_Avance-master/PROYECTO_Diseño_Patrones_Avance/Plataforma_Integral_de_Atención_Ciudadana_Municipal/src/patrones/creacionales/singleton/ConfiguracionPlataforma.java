package patrones.creacionales.singleton;

/**
 * Patrón Singleton (Creacional):
 * Mantiene la configuración institucional compartida para toda la plataforma municipal.
 * GRASP Experto en Información: conoce los parámetros organizacionales del municipio.
 * SOLID: S (Responsabilidad Única) - se encarga exclusivamente de proveer la configuración global.
 * Thread-safe mediante el patrón Initialization-on-demand holder idiom (Bill Pugh).
 */
public class ConfiguracionPlataforma {

    private final String nombreMunicipio;

    private static class Holder {
        private static final ConfiguracionPlataforma INSTANCIA = new ConfiguracionPlataforma();
    }

    /**
     * Singleton: constructor privado para evitar instanciación externa.
     */
    private ConfiguracionPlataforma() {
        this.nombreMunicipio = "Municipalidad de Lima Norte";
    }

    /**
     * Singleton: entrega la única instancia compartida de configuración de manera thread-safe.
     *
     * @return instancia única global
     */
    public static ConfiguracionPlataforma getInstancia() {
        return Holder.INSTANCIA;
    }

    /**
     * Singleton: consulta el nombre municipal almacenado globalmente.
     *
     * @return nombre del municipio
     */
    public String getNombreMunicipio() {
        return nombreMunicipio;
    }
}
