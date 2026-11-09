package vistas;

import controladores.AtencionController;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.awt.Color;
import java.awt.Component;
import java.util.concurrent.ExecutionException;

/**
 * Vista Helper (GRASP Fabricación Pura / Alta Cohesión):
 * Centraliza la lógica de interacción de usuario para la operación de Deshacer (Undo)
 * entre PanelMisSolicitudes y PanelHistorialAcciones, evitando duplicación de código.
 */
public class AccionDeshacer {

    private AccionDeshacer() {
        // Constructor privado para clase de utilidades
    }

    /**
     * Actualiza el botón y la etiqueta informativa del estado de la pila de comandos.
     *
     * @param controlador     controlador MVC
     * @param btnDeshacer     botón de deshacer
     * @param lblUltimaAccion etiqueta informativa
     */
    public static void actualizarEstadoControles(AtencionController controlador,
                                                JButton btnDeshacer,
                                                JLabel lblUltimaAccion) {
        boolean puede = controlador.puedeDeshacer();
        if (btnDeshacer != null) {
            btnDeshacer.setEnabled(puede);
        }
        if (lblUltimaAccion != null) {
            if (puede) {
                String desc = controlador.descripcionUltimaAccion();
                lblUltimaAccion.setText("Última acción: " + (desc != null ? desc : ""));
                lblUltimaAccion.setForeground(new Color(40, 55, 75));
            } else {
                lblUltimaAccion.setText("Sin acciones para deshacer");
                lblUltimaAccion.setForeground(new Color(130, 140, 155));
            }
        }
    }

    /**
     * Solicita confirmación al usuario y ejecuta el comando de deshacer en segundo plano
     * mediante SwingWorker, manejando excepciones y ejecutando el callback de recarga.
     *
     * @param padre       componente padre para los diálogos
     * @param controlador controlador MVC
     * @param btnDeshacer botón para deshabilitar temporalmente
     * @param alFinalizar acción a ejecutar al concluir la reversión (recarga de vista)
     */
    public static void ejecutarDeshacer(Component padre,
                                       AtencionController controlador,
                                       JButton btnDeshacer,
                                       Runnable alFinalizar) {
        if (!controlador.puedeDeshacer()) {
            JOptionPane.showMessageDialog(padre,
                    "No hay acciones para deshacer.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String descripcion = controlador.descripcionUltimaAccion();
        int resp = JOptionPane.showConfirmDialog(padre,
                "¿Desea deshacer la siguiente acción?\n" + descripcion,
                "Confirmar Deshacer",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (resp != JOptionPane.YES_OPTION) {
            return;
        }

        if (btnDeshacer != null) {
            btnDeshacer.setEnabled(false);
        }

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return controlador.deshacerUltimaAccion();
            }

            @Override
            protected void done() {
                try {
                    String revertida = get();
                    System.out.println("[Deshacer UI] Acción revertida con éxito: " + revertida);
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(padre,
                            "Error al deshacer la acción: " + causa.getMessage(),
                            "Error al deshacer", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    if (alFinalizar != null) {
                        alFinalizar.run();
                    }
                }
            }
        };
        worker.execute();
    }
}
