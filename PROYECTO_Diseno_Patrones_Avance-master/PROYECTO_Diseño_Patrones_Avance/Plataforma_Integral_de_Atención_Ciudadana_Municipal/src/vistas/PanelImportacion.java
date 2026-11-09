package vistas;

import controladores.AtencionController;
import patrones.creacionales.builder.Expediente;
import patrones.estructurales.facade.ResultadoImportacion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.ExecutionException;

/**
 * Vista Swing: panel para importar quejas del sistema antiguo.
 * Orquesta los patrones Adapter, Composite, Proxy, Command y Observer a través de AtencionController.
 * GRASP Alta Cohesión: diseño visual responsivo usando EstiloVista.
 */
public class PanelImportacion extends JPanel {

    private final AtencionController controlador;
    private final Runnable alImportar;

    private final JButton btnImportar = new JButton("Importar quejas antiguas");
    private final JLabel lblResumen = new JLabel("Importadas: 0 | Rechazadas: 0");
    private final DefaultTableModel modeloTabla;
    private final JTable tablaResultados;
    private final JLabel lblSinDatos = new JLabel("Presione 'Importar quejas antiguas' para procesar el lote heredado", SwingConstants.CENTER);

    private boolean yaImportoEnSesion = false;

    private static final Color COLOR_PRIMARIO = new Color(0, 74, 173); // #004AAD

    public PanelImportacion(AtencionController controlador, Runnable alImportar) {
        this.controlador = controlador;
        this.alImportar = alImportar;

        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(245, 248, 253));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Inicializar tabla con tooltip dinámico y renderer personalizado
        String[] columnas = {"Registro heredado", "Resultado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaResultados = new JTable(modeloTabla) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof JComponent) {
                    Object val = getValueAt(row, column);
                    ((JComponent) c).setToolTipText(val != null ? val.toString() : null);
                }
                return c;
            }
        };
        configurarTabla();

        // 1. Cabecera con título, descripción y botonera
        add(crearCabecera(), BorderLayout.NORTH);

        // 2. Centro: JTable con scroll
        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.setBackground(Color.WHITE);
        panelCentro.setBorder(BorderFactory.createLineBorder(new Color(210, 225, 245), 1, true));

        JScrollPane scroll = new JScrollPane(tablaResultados);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panelCentro.add(scroll, BorderLayout.CENTER);

        lblSinDatos.setFont(EstiloVista.FUENTE_CURSIVA);
        lblSinDatos.setForeground(new Color(120, 130, 145));
        lblSinDatos.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        panelCentro.add(lblSinDatos, BorderLayout.SOUTH);

        add(panelCentro, BorderLayout.CENTER);
    }

    private JPanel crearCabecera() {
        JPanel cabecera = new JPanel(new GridBagLayout());
        cabecera.setBackground(Color.WHITE);
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 225, 245), 1, true),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        JLabel lblTitulo = new JLabel("Importación de Quejas del Sistema Antiguo (Adapter + Composite + Proxy)");
        lblTitulo.setFont(EstiloVista.FUENTE_TITULO);
        lblTitulo.setForeground(new Color(3, 45, 102));
        cabecera.add(lblTitulo, gbc);

        JLabel lblSubtitulo = new JLabel("Adapta y agrupa registros heredados para validarlos por Proxy y registrarlos en la plataforma.");
        lblSubtitulo.setFont(EstiloVista.FUENTE_BASE);
        lblSubtitulo.setForeground(new Color(90, 105, 120));
        gbc.gridy = 1;
        cabecera.add(lblSubtitulo, gbc);

        // Barra de acciones
        JPanel barraAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        barraAcciones.setBackground(Color.WHITE);

        btnImportar.setFont(EstiloVista.FUENTE_NEGRITA);
        btnImportar.setBackground(COLOR_PRIMARIO);
        btnImportar.setForeground(Color.WHITE);
        btnImportar.setFocusPainted(false);
        btnImportar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnImportar.setMargin(new Insets(6, 16, 6, 16));
        btnImportar.addActionListener(e -> confirmarYEjecutarImportacion());
        barraAcciones.add(btnImportar);

        lblResumen.setFont(EstiloVista.FUENTE_NEGRITA);
        lblResumen.setForeground(new Color(40, 55, 75));
        barraAcciones.add(lblResumen);

        gbc.gridy = 2;
        gbc.insets = new Insets(8, 4, 0, 4);
        cabecera.add(barraAcciones, gbc);

        return cabecera;
    }

    private void configurarTabla() {
        tablaResultados.setFont(EstiloVista.FUENTE_BASE);
        tablaResultados.setRowHeight(EstiloVista.ALTO_FILA);
        tablaResultados.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        tablaResultados.getTableHeader().setFont(EstiloVista.FUENTE_NEGRITA);
        tablaResultados.getTableHeader().setPreferredSize(new Dimension(0, 32));
        tablaResultados.getTableHeader().setBackground(new Color(235, 242, 252));
        tablaResultados.getTableHeader().setForeground(new Color(3, 45, 102));

        // Renderer de color para la columna Resultado (índice 1)
        tablaResultados.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String texto = value != null ? value.toString() : "";
                if (!isSelected) {
                    if (texto.startsWith("Importado")) {
                        c.setBackground(new Color(230, 245, 230)); // Verde suave
                        c.setForeground(new Color(20, 90, 30));
                    } else if (texto.startsWith("Rechazado")) {
                        c.setBackground(new Color(255, 235, 235)); // Rojo suave
                        c.setForeground(new Color(160, 30, 30));
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        });
    }

    private void confirmarYEjecutarImportacion() {
        if (yaImportoEnSesion) {
            int resp = JOptionPane.showConfirmDialog(
                    this,
                    "Ya se importaron registros; se crearán expedientes nuevos. ¿Continuar?",
                    "Confirmar reimportación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (resp != JOptionPane.YES_OPTION) {
                return;
            }
        }

        btnImportar.setEnabled(false);
        lblSinDatos.setVisible(false);

        SwingWorker<ResultadoImportacion, Void> worker = new SwingWorker<ResultadoImportacion, Void>() {
            @Override
            protected ResultadoImportacion doInBackground() {
                return controlador.importarQuejasAntiguas();
            }

            @Override
            protected void done() {
                btnImportar.setEnabled(true);
                try {
                    ResultadoImportacion resultado = get();
                    yaImportoEnSesion = true;

                    modeloTabla.setRowCount(0);

                    // Filas de expedientes importados
                    for (Expediente exp : resultado.getImportados()) {
                        String reg = exp.getCodigo() + " - " + exp.getNombreCiudadano() + " (DNI: " + exp.getDni() + ")";
                        modeloTabla.addRow(new Object[]{reg, "Importado: " + exp.getCodigo()});
                    }

                    // Filas de registros rechazados
                    for (ResultadoImportacion.DetalleRechazo rechazo : resultado.getRechazados()) {
                        modeloTabla.addRow(new Object[]{rechazo.getRegistroHeredado(), "Rechazado: " + rechazo.getMotivo()});
                    }

                    lblResumen.setText("Importadas: " + resultado.getTotalImportados() + " | Rechazadas: " + resultado.getTotalRechazados());
                    EstiloVista.ajustarColumnas(tablaResultados);

                    // Traza para Actividad del sistema con separador de 36 caracteres
                    System.out.println("\n====================================");
                    System.out.println("IMPORTACION FINALIZADA");
                    System.out.println("Total procesados: " + resultado.getTotalProcesados());
                    System.out.println("Importadas:       " + resultado.getTotalImportados());
                    System.out.println("Rechazadas:       " + resultado.getTotalRechazados());
                    System.out.println("====================================\n");

                    JOptionPane.showMessageDialog(
                            PanelImportacion.this,
                            "Lote de quejas procesado correctamente.\n"
                                    + "Importadas: " + resultado.getTotalImportados() + "\n"
                                    + "Rechazadas: " + resultado.getTotalRechazados(),
                            "Importación completada",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    if (alImportar != null) {
                        alImportar.run();
                    }

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(
                            PanelImportacion.this,
                            "Error durante la importación: " + causa.getMessage(),
                            "Error de importación",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }
}
