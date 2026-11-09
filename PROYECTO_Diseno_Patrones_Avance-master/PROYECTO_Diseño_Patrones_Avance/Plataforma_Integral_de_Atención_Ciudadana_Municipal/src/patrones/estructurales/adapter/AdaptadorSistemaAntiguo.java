package patrones.estructurales.adapter;

import modelos.Ciudadano;
import patrones.creacionales.productos.Tramite;

/**
 * Patrón Adapter: adapta un registro heredado del sistema antiguo al contrato moderno
 * de Tramite y al modelo Ciudadano utilizado por la plataforma actual.
 */
public class AdaptadorSistemaAntiguo implements Tramite {

    private final RegistroQuejaAntigua registro;
    private final SistemaAntiguoQuejas sistemaViejo;

    /**
     * Constructor por defecto para compatibilidad con demostraciones técnicas previas.
     */
    public AdaptadorSistemaAntiguo() {
        this(new RegistroQuejaAntigua(
                "LEG-DEMO",
                "74859612",
                "Ciudadano Demo",
                "demo@example.com",
                "Queja de demostracion"
        ), new SistemaAntiguoQuejas());
    }

    /**
     * Constructor con sistema heredado inyectado para compatibilidad.
     *
     * @param sistemaViejo instancia del sistema heredado
     */
    public AdaptadorSistemaAntiguo(SistemaAntiguoQuejas sistemaViejo) {
        this(new RegistroQuejaAntigua(
                "LEG-DEMO",
                "74859612",
                "Ciudadano Demo",
                "demo@example.com",
                "Queja de demostracion"
        ), sistemaViejo);
    }

    /**
     * Constructor principal: adapta un registro heredado específico.
     *
     * @param registro registro antiguo a adaptar
     */
    public AdaptadorSistemaAntiguo(RegistroQuejaAntigua registro) {
        this(registro, new SistemaAntiguoQuejas());
    }

    /**
     * Constructor completo: recibe el registro y el sistema heredado.
     *
     * @param registro     registro antiguo a adaptar
     * @param sistemaViejo sistema antiguo de referencia
     */
    public AdaptadorSistemaAntiguo(RegistroQuejaAntigua registro, SistemaAntiguoQuejas sistemaViejo) {
        this.registro = registro;
        this.sistemaViejo = sistemaViejo;
    }

    @Override
    public void procesar() {
        System.out.println("[Adapter] Traduciendo registro heredado " + registro.getId() + " al formato actual");
    }

    /**
     * Traduce los datos del registro heredado a la entidad Ciudadano de la plataforma moderna.
     *
     * @return ciudadano correspondiente
     */
    public Ciudadano getCiudadano() {
        return new Ciudadano(
                registro.getDni(),
                registro.getNombreCompleto(),
                registro.getCorreo()
        );
    }

    /**
     * En el sistema antiguo todos los registros corresponden a quejas vecinales,
     * homologadas como 'reclamo' en la nueva plataforma.
     *
     * @return tipo de trámite homogéneo ("reclamo")
     */
    public String getTipoTramite() {
        return "reclamo";
    }

    /**
     * Obtiene el registro original heredado.
     *
     * @return registro antiguo
     */
    public RegistroQuejaAntigua getRegistro() {
        return registro;
    }
}
