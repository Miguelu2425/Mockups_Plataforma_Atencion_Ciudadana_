package patrones.estructurales.facade;

import patrones.creacionales.builder.Expediente;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Patrón Facade: encapsula el resultado consolidado del proceso de importación
 * de quejas heredadas del sistema antiguo, detallando expedientes importados
 * y registros rechazados con sus motivos.
 */
public class ResultadoImportacion {

    /**
     * Estructura inmutable que detalla el motivo de rechazo de un registro heredado.
     */
    public static class DetalleRechazo {
        private final String registroHeredado;
        private final String motivo;

        public DetalleRechazo(String registroHeredado, String motivo) {
            this.registroHeredado = registroHeredado;
            this.motivo = motivo;
        }

        public String getRegistroHeredado() {
            return registroHeredado;
        }

        public String getMotivo() {
            return motivo;
        }

        @Override
        public String toString() {
            return registroHeredado + " -> Rechazado: " + motivo;
        }
    }

    private final List<Expediente> importados = new ArrayList<>();
    private final List<DetalleRechazo> rechazados = new ArrayList<>();

    public void agregarImportado(Expediente exp) {
        if (exp != null) {
            importados.add(exp);
        }
    }

    public void agregarRechazado(String registroHeredado, String motivo) {
        rechazados.add(new DetalleRechazo(registroHeredado, motivo));
    }

    public List<Expediente> getImportados() {
        return Collections.unmodifiableList(importados);
    }

    public List<DetalleRechazo> getRechazados() {
        return Collections.unmodifiableList(rechazados);
    }

    public int getTotalImportados() {
        return importados.size();
    }

    public int getTotalRechazados() {
        return rechazados.size();
    }

    public int getTotalProcesados() {
        return importados.size() + rechazados.size();
    }
}

