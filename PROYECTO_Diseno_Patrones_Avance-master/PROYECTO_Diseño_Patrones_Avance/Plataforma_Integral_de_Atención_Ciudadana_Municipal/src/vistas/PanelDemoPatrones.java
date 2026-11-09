package vistas;

import pruebas.DemoPatrones;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Vista Swing: panel para disparar la demostración técnica de todos los patrones implementados.
 * GRASP Alta Cohesión: aísla la invocación de las pruebas técnicas sin ensuciar la ventana principal.
 * Adaptación visual: utiliza EstiloVista para tipografías unificadas.
 */
public class PanelDemoPatrones extends JPanel {

    private final JLabel lblEstadoDemo = new JLabel("Presione el botón para ejecutar la demostración completa.");
    private final JButton btnEjecutar = new JButton("Ejecutar demostración de patrones");

    private static final Color COLOR_PRIMARIO = new Color(0, 74, 173); // #004AAD

    public PanelDemoPatrones() {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 248, 253));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        JPanel tarjeta = new JPanel(new GridBagLayout());
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 225, 245), 1, true),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        // Título
        JLabel titulo = new JLabel("Demostración Integral de Patrones del Curso");
        titulo.setFont(EstiloVista.FUENTE_TITULO);
        titulo.setForeground(new Color(3, 45, 102));
        gbc.gridx = 0;
        gbc.gridy = 0;
        tarjeta.add(titulo, gbc);

        // Descripción
        JLabel descripcion = new JLabel("<html>Ejecuta secuencialmente los patrones implementados:<br>"
                + "• <b>Creacionales:</b> Singleton, Factory Method, Abstract Factory, Builder, Prototype.<br>"
                + "• <b>Estructurales:</b> Adapter, Bridge, Composite, Decorator, Facade, Proxy.<br>"
                + "• <b>Comportamiento:</b> State, Observer, Command, Memento.<br><br>"
                + "La traza completa de ejecución se imprimirá en el panel <i>Actividad del sistema</i>.</html>");
        descripcion.setFont(EstiloVista.FUENTE_BASE);
        descripcion.setForeground(new Color(50, 65, 80));
        gbc.gridy = 1;
        tarjeta.add(descripcion, gbc);

        // Botón Ejecutar
        btnEjecutar.setFont(EstiloVista.FUENTE_NEGRITA);
        btnEjecutar.setBackground(COLOR_PRIMARIO);
        btnEjecutar.setForeground(Color.WHITE);
        btnEjecutar.setFocusPainted(false);
        btnEjecutar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEjecutar.setMargin(new Insets(6, 14, 6, 14));
        btnEjecutar.addActionListener(e -> ejecutarDemo());

        gbc.gridy = 2;
        gbc.insets = new Insets(16, 8, 10, 8);
        tarjeta.add(btnEjecutar, gbc);

        // Estado
        lblEstadoDemo.setFont(EstiloVista.FUENTE_CURSIVA);
        lblEstadoDemo.setForeground(new Color(100, 115, 130));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 8, 5, 8);
        tarjeta.add(lblEstadoDemo, gbc);

        GridBagConstraints gbcPrincipal = new GridBagConstraints();
        gbcPrincipal.gridx = 0;
        gbcPrincipal.gridy = 0;
        gbcPrincipal.weightx = 1.0;
        gbcPrincipal.fill = GridBagConstraints.HORIZONTAL;
        add(tarjeta, gbcPrincipal);
    }

    private void ejecutarDemo() {
        btnEjecutar.setEnabled(false);
        lblEstadoDemo.setText("Ejecutando demostración... revise el panel de actividad.");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                DemoPatrones.demostrarPatrones();
                DemoPatrones.probarConexionBaseDatos();
                return null;
            }

            @Override
            protected void done() {
                btnEjecutar.setEnabled(true);
                lblEstadoDemo.setText("Demostración finalizada exitosamente.");
            }
        };
        worker.execute();
    }
}
