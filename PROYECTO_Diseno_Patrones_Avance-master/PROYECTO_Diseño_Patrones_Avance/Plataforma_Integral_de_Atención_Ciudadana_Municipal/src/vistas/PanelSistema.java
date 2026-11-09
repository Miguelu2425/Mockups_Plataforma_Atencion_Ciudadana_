package vistas;

import controladores.AtencionController;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Vista Swing: panel de diagnóstico y demostración del patrón Singleton.
 * Muestra el municipio configurado en ConfiguracionPlataforma y el estado de ConexionSingleton.
 * MVC: se comunica exclusivamente con AtencionController.
 * GRASP Alta Cohesión: adaptación visual y textos multilínea usando EstiloVista.
 */
public class PanelSistema extends JPanel {

    private final AtencionController controlador;
    private final Runnable alComprobar;

    private final JLabel lblMunicipio = new JLabel();
    private final JLabel lblEstadoBD = new JLabel();
    private final JLabel lblUltimaComprobacion = new JLabel("Última comprobación: --:--:--");

    private static final Color COLOR_PRIMARIO = new Color(0, 74, 173); // #004AAD
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    public PanelSistema(AtencionController controlador, Runnable alComprobar) {
        this.controlador = controlador;
        this.alComprobar = alComprobar;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 253));

        inicializarComponentes();
        actualizarDatos();
    }

    private void inicializarComponentes() {
        JPanel panelContenedor = new JPanel(new GridBagLayout());
        panelContenedor.setBackground(new Color(245, 248, 253));
        panelContenedor.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

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
        JLabel titulo = new JLabel("Configuración y Conexión (Patrón Singleton)");
        titulo.setFont(EstiloVista.FUENTE_TITULO);
        titulo.setForeground(new Color(3, 45, 102));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        tarjeta.add(titulo, gbc);

        gbc.gridwidth = 1;

        // Fila 1: Municipio (ConfiguracionPlataforma Singleton)
        agregarFila(tarjeta, gbc, 1, "Municipio (ConfiguracionPlataforma):", lblMunicipio);

        // Fila 2: Estado de persistencia actual
        agregarFila(tarjeta, gbc, 2, "Estado de BD (ConexionSingleton):", lblEstadoBD);

        // Fila 3: Última comprobación
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        lblUltimaComprobacion.setFont(EstiloVista.FUENTE_CURSIVA);
        lblUltimaComprobacion.setForeground(new Color(100, 115, 130));
        tarjeta.add(lblUltimaComprobacion, gbc);

        // Botón Probar Conexión
        JButton btnProbar = new JButton("Probar conexión (Singleton)");
        btnProbar.setFont(EstiloVista.FUENTE_NEGRITA);
        btnProbar.setBackground(COLOR_PRIMARIO);
        btnProbar.setForeground(Color.WHITE);
        btnProbar.setFocusPainted(false);
        btnProbar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProbar.addActionListener(e -> probarConexion());

        gbc.gridy = 4;
        gbc.insets = new Insets(18, 8, 15, 8);
        tarjeta.add(btnProbar, gbc);

        // Tarjeta explicativa sobre el Singleton
        JPanel panelExplicacion = new JPanel(new GridBagLayout());
        panelExplicacion.setBackground(new Color(240, 246, 255));
        panelExplicacion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 210, 245), 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        GridBagConstraints gbcExp = new GridBagConstraints();
        gbcExp.fill = GridBagConstraints.HORIZONTAL;
        gbcExp.weightx = 1.0;
        gbcExp.insets = new Insets(3, 4, 3, 4);

        JLabel lblExpTitulo = new JLabel("Puntos de uso del Patrón Singleton en esta pantalla:");
        lblExpTitulo.setFont(EstiloVista.FUENTE_NEGRITA);
        lblExpTitulo.setForeground(new Color(3, 45, 102));
        gbcExp.gridy = 0;
        panelExplicacion.add(lblExpTitulo, gbcExp);

        JTextArea txtExp1 = crearTextoExplicativo("• ConfiguracionPlataforma: instancia global única con los datos del municipio.");
        gbcExp.gridy = 1;
        panelExplicacion.add(txtExp1, gbcExp);

        JTextArea txtExp2 = crearTextoExplicativo("• ConexionSingleton: única conexión activa a SQL Server; si no hay BD, opera en memoria.");
        gbcExp.gridy = 2;
        panelExplicacion.add(txtExp2, gbcExp);

        JTextArea txtExp3 = crearTextoExplicativo("• Al pulsar 'Probar conexión', se llama dos veces para verificar 'creando' y 'reutilizando'.");
        gbcExp.gridy = 3;
        panelExplicacion.add(txtExp3, gbcExp);

        gbc.gridy = 5;
        gbc.insets = new Insets(10, 8, 8, 8);
        tarjeta.add(panelExplicacion, gbc);

        GridBagConstraints gbcPrincipal = new GridBagConstraints();
        gbcPrincipal.gridx = 0;
        gbcPrincipal.gridy = 0;
        gbcPrincipal.weightx = 1.0;
        gbcPrincipal.fill = GridBagConstraints.HORIZONTAL;
        gbcPrincipal.anchor = GridBagConstraints.NORTH;
        panelContenedor.add(tarjeta, gbcPrincipal);

        // Envoltura con JScrollPane vertical para cuando la ventana se achique
        JScrollPane scrollPane = new JScrollPane(panelContenedor);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(245, 248, 253));

        add(scrollPane, BorderLayout.CENTER);
    }

    private JTextArea crearTextoExplicativo(String texto) {
        JTextArea area = new JTextArea(texto);
        area.setFont(EstiloVista.FUENTE_BASE);
        area.setForeground(new Color(50, 65, 80));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setEditable(false);
        area.setFocusable(false);
        area.setBorder(null);
        return area;
    }

    private void agregarFila(JPanel panel, GridBagConstraints gbc, int fila,
                            String etiqueta, JLabel valorLabel) {
        gbc.gridy = fila;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(EstiloVista.FUENTE_NEGRITA);
        lbl.setForeground(new Color(40, 50, 70));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        valorLabel.setFont(EstiloVista.FUENTE_BASE);
        valorLabel.setForeground(new Color(20, 30, 45));
        panel.add(valorLabel, gbc);
    }

    private void actualizarDatos() {
        lblMunicipio.setText(controlador.nombreMunicipio());
        lblEstadoBD.setText(controlador.estadoConexion());
    }

    private void probarConexion() {
        System.out.println("\n=== Probando ConexionSingleton desde Panel de Sistema ===");
        String resultado = controlador.probarConexion();
        System.out.println("Resultado de prueba: " + resultado);

        actualizarDatos();
        lblUltimaComprobacion.setText("Última comprobación: " + LocalTime.now().format(FMT_HORA));

        if (alComprobar != null) {
            alComprobar.run();
        }
    }
}
