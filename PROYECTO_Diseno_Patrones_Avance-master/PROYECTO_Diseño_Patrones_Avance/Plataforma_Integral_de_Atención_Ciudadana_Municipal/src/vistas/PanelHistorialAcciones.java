package vistas;

import controladores.AtencionController;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Vista Swing: panel para visualizar la pila de historial de comandos ejecutados
 * y permitir deshacer la última acción mediante los patrones Command y Memento.
 * GRASP Alta Cohesión: se encarga exclusivamente del monitoreo y reversión del historial.
 * MVC: se comunica únicamente con AtencionController.
 * Adaptación visual: utiliza EstiloVista para tipografías unificadas y prevención de truncamientos.
 */
public class PanelHistorialAcciones extends JPanel {

    private final AtencionController controlador;

    private final DefaultListModel<String> modeloLista = new DefaultListModel<>();
    private final JList<String> listaHistorial = new JList<>(modeloLista);
    private final JLabel lblVacio = new JLabel("No hay acciones para deshacer", SwingConstants.CENTER);

    private final JButton btnActualizar = new JButton("Actualizar");
    private final JButton btnDeshacer = new JButton("Deshacer última acción");
    private final JLabel lblUltimaAccion = new JLabel("Sin acciones para deshacer");

    private static final Color COLOR_PRIMARIO = new Color(0, 74, 173); // #004AAD
    private static final Color COLOR_DESHACER = new Color(180, 40, 40); // Rojo suave para reversión

    public PanelHistorialAcciones(AtencionController controlador) {
        this.controlador = controlador;

        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(245, 248, 253));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        inicializarComponentes();

        // Refresco automático al hacerse visible
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                cargarHistorial();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });

        cargarHistorial();
    }

    private void inicializarComponentes() {
        // 1. Tarjeta superior: Título y descripción
        JPanel cabecera = new JPanel(new BorderLayout(8, 4));
        cabecera.setBackground(Color.WHITE);
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 225, 245), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblTitulo = new JLabel("Historial de Acciones en Memoria (Patrones Command y Memento)");
        lblTitulo.setFont(EstiloVista.FUENTE_TITULO);
        lblTitulo.setForeground(new Color(3, 45, 102));
        cabecera.add(lblTitulo, BorderLayout.NORTH);

        JLabel lblSubtitulo = new JLabel("Muestra la pila de operaciones reversibles en orden cronológico inverso (la más reciente arriba).");
        lblSubtitulo.setFont(EstiloVista.FUENTE_BASE);
        lblSubtitulo.setForeground(new Color(90, 105, 120));
        cabecera.add(lblSubtitulo, BorderLayout.SOUTH);

        add(cabecera, BorderLayout.NORTH);

        // 2. Centro: JList con scroll y mensaje de lista vacía
        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.setBackground(Color.WHITE);
        panelCentro.setBorder(BorderFactory.createLineBorder(new Color(210, 225, 245), 1, true));

        listaHistorial.setFont(EstiloVista.FUENTE_BASE);
        listaHistorial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaHistorial.setFixedCellHeight(30);
        listaHistorial.setSelectionBackground(new Color(230, 240, 255));
        listaHistorial.setSelectionForeground(COLOR_PRIMARIO);

        // Tooltip en cada elemento de la lista para visualización completa
        listaHistorial.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    label.setToolTipText(value.toString());
                }
                return label;
            }
        });

        JScrollPane scroll = new JScrollPane(listaHistorial);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panelCentro.add(scroll, BorderLayout.CENTER);

        lblVacio.setFont(EstiloVista.FUENTE_CURSIVA);
        lblVacio.setForeground(new Color(130, 140, 155));
        lblVacio.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        lblVacio.setVisible(false);
        panelCentro.add(lblVacio, BorderLayout.SOUTH);

        add(panelCentro, BorderLayout.CENTER);

        // 3. Panel inferior de control
        add(crearPanelInferior(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 225, 245), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);

        // Fila 0, Columna 0: Etiqueta de última acción
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        lblUltimaAccion.setFont(EstiloVista.FUENTE_NEGRITA);
        panel.add(lblUltimaAccion, gbc);

        // Fila 0, Columna 1: Botonera
        JPanel botonera = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botonera.setBackground(Color.WHITE);

        btnActualizar.setFont(EstiloVista.FUENTE_BASE);
        btnActualizar.setBackground(new Color(235, 242, 250));
        btnActualizar.setFocusPainted(false);
        btnActualizar.setMargin(new Insets(5, 12, 5, 12));
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.addActionListener(e -> cargarHistorial());
        botonera.add(btnActualizar);

        btnDeshacer.setFont(EstiloVista.FUENTE_NEGRITA);
        btnDeshacer.setBackground(COLOR_DESHACER);
        btnDeshacer.setForeground(Color.WHITE);
        btnDeshacer.setFocusPainted(false);
        btnDeshacer.setMargin(new Insets(5, 12, 5, 12));
        btnDeshacer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeshacer.setEnabled(false);
        btnDeshacer.addActionListener(e -> AccionDeshacer.ejecutarDeshacer(this, controlador, btnDeshacer, this::cargarHistorial));
        botonera.add(btnDeshacer);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(botonera, gbc);

        return panel;
    }

    public void cargarHistorial() {
        List<String> historial = controlador.historialAcciones();
        modeloLista.clear();

        if (historial == null || historial.isEmpty()) {
            lblVacio.setVisible(true);
        } else {
            lblVacio.setVisible(false);
            for (int i = 0; i < historial.size(); i++) {
                modeloLista.addElement(String.format("%d. %s", (i + 1), historial.get(i)));
            }
        }

        AccionDeshacer.actualizarEstadoControles(controlador, btnDeshacer, lblUltimaAccion);
    }
}
