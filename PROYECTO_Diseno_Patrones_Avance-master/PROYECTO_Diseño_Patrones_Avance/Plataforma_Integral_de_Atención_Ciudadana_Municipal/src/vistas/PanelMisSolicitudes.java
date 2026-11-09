package vistas;

import controladores.AtencionController;
import patrones.creacionales.builder.Expediente;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Vista Swing: panel para listar, filtrar y gestionar el estado de las solicitudes ciudadanas.
 * Patrón MVC: interactúa exclusivamente con AtencionController.
 * Patrón State, Observer, Command y Memento: permite avanzar y deshacer estados en pantalla.
 * GRASP Alta Cohesión: diseño visual adaptativo usando EstiloVista.
 */
public class PanelMisSolicitudes extends JPanel {

    private final AtencionController controlador;

    private final JTextField txtDniFiltro = new JTextField(8);
    private final JButton btnBuscar = new JButton("Buscar mis solicitudes");
    private final JButton btnVerTodas = new JButton("Ver todas");
    private final JButton btnActualizar = new JButton("Actualizar");

    private final DefaultTableModel modeloTabla;
    private final JTable tablaSolicitudes;
    private final JScrollPane scrollTabla;
    private final JLabel lblSinResultados = new JLabel("No hay solicitudes para mostrar", SwingConstants.CENTER);

    private final JLabel lblDetalleEstado = new JLabel("Seleccione un expediente de la tabla para ver su seguimiento.");
    private final JLabel lblSiguienteEstado = new JLabel("");
    private final JButton btnAvanzar = new JButton("Avanzar estado");

    private final JButton btnDeshacer = new JButton("Deshacer última acción");
    private final JLabel lblUltimaAccion = new JLabel("Sin acciones para deshacer");

    private String filtroDniActual = null;

    private static final Color COLOR_PRIMARIO = new Color(0, 74, 173); // #004AAD
    private static final Color COLOR_DESHACER = new Color(180, 40, 40); // Rojo suave para reversión

    public PanelMisSolicitudes(AtencionController controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(245, 248, 253));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Inicializar tabla con tooltip completo por celda
        String[] columnas = {"Código", "Tipo", "Ciudadano", "Estado", "Situación", "Fecha"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaSolicitudes = new JTable(modeloTabla) {
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

        // 1. Barra superior de filtros (sin cortes en botones)
        add(crearBarraFiltros(), BorderLayout.NORTH);

        // 2. Centro: JTable con scroll horizontal y vertical
        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.setBackground(Color.WHITE);
        panelCentro.setBorder(BorderFactory.createLineBorder(new Color(210, 225, 245), 1, true));

        scrollTabla = new JScrollPane(tablaSolicitudes);
        scrollTabla.getViewport().setBackground(Color.WHITE);
        scrollTabla.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panelCentro.add(scrollTabla, BorderLayout.CENTER);

        lblSinResultados.setFont(EstiloVista.FUENTE_BASE);
        lblSinResultados.setForeground(new Color(120, 130, 145));
        lblSinResultados.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        lblSinResultados.setVisible(false);
        panelCentro.add(lblSinResultados, BorderLayout.SOUTH);

        add(panelCentro, BorderLayout.CENTER);

        // 3. Panel inferior de seguimiento, avance y deshacer
        add(crearPanelSeguimiento(), BorderLayout.SOUTH);

        // Refresco automático al hacerse visible
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                recargar();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });

        // Cargar solicitudes iniciales
        cargarSolicitudes(null, null);
    }

    private JPanel crearBarraFiltros() {
        JPanel barra = new JPanel(new GridBagLayout());
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 225, 245), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        JLabel lblDni = new JLabel("DNI:");
        lblDni.setFont(EstiloVista.FUENTE_NEGRITA);
        gbc.gridx = 0;
        barra.add(lblDni, gbc);

        txtDniFiltro.setFont(EstiloVista.FUENTE_BASE);
        txtDniFiltro.setMargin(new Insets(3, 6, 3, 6));
        gbc.gridx = 1;
        barra.add(txtDniFiltro, gbc);

        btnBuscar.setFont(EstiloVista.FUENTE_NEGRITA);
        btnBuscar.setBackground(COLOR_PRIMARIO);
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFocusPainted(false);
        btnBuscar.setMargin(new Insets(5, 12, 5, 12));
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscar.addActionListener(e -> buscarPorDni());
        gbc.gridx = 2;
        barra.add(btnBuscar, gbc);

        btnVerTodas.setFont(EstiloVista.FUENTE_BASE);
        btnVerTodas.setBackground(new Color(235, 242, 250));
        btnVerTodas.setFocusPainted(false);
        btnVerTodas.setMargin(new Insets(5, 12, 5, 12));
        btnVerTodas.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVerTodas.addActionListener(e -> {
            txtDniFiltro.setText("");
            filtroDniActual = null;
            cargarSolicitudes(null, null);
        });
        gbc.gridx = 3;
        barra.add(btnVerTodas, gbc);

        btnActualizar.setFont(EstiloVista.FUENTE_BASE);
        btnActualizar.setBackground(new Color(235, 242, 250));
        btnActualizar.setFocusPainted(false);
        btnActualizar.setMargin(new Insets(5, 12, 5, 12));
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.addActionListener(e -> cargarSolicitudes(filtroDniActual, obtenerCodigoSeleccionado()));
        gbc.gridx = 4;
        barra.add(btnActualizar, gbc);

        // Relleno horizontal para que no se compriman los controles
        gbc.gridx = 5;
        gbc.weightx = 1.0;
        JPanel espaciador = new JPanel();
        espaciador.setOpaque(false);
        barra.add(espaciador, gbc);

        return barra;
    }

    private void configurarTabla() {
        tablaSolicitudes.setFont(EstiloVista.FUENTE_BASE);
        tablaSolicitudes.setRowHeight(EstiloVista.ALTO_FILA);
        tablaSolicitudes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaSolicitudes.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        tablaSolicitudes.getTableHeader().setFont(EstiloVista.FUENTE_NEGRITA);
        tablaSolicitudes.getTableHeader().setPreferredSize(new Dimension(0, 32));
        tablaSolicitudes.getTableHeader().setBackground(new Color(235, 242, 252));
        tablaSolicitudes.getTableHeader().setForeground(new Color(3, 45, 102));

        // Renderer de color para la columna Estado (índice 3)
        tablaSolicitudes.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(EstiloVista.FUENTE_NEGRITA);

                String estado = (value != null) ? value.toString() : "";
                if (!isSelected) {
                    switch (estado) {
                        case "En Revision":
                            c.setBackground(new Color(255, 243, 205)); // Ámbar claro
                            c.setForeground(new Color(133, 100, 4));
                            break;
                        case "Atendido":
                            c.setBackground(new Color(212, 237, 218)); // Verde claro
                            c.setForeground(new Color(21, 87, 36));
                            break;
                        case "Cerrado":
                            c.setBackground(new Color(226, 227, 229)); // Gris claro
                            c.setForeground(new Color(56, 61, 65));
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.BLACK);
                            break;
                    }
                }
                return c;
            }
        });

        // Centrado de Código y Situación
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        tablaSolicitudes.getColumnModel().getColumn(0).setCellRenderer(centrado);
        tablaSolicitudes.getColumnModel().getColumn(4).setCellRenderer(centrado);

        // Selección de fila
        tablaSolicitudes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarDetalleSeleccion();
            }
        });
    }

    private JPanel crearPanelSeguimiento() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 225, 245), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);

        // Fila 0: Título de detalle
        JLabel lblTitulo = new JLabel("Control y seguimiento del trámite seleccionado");
        lblTitulo.setFont(EstiloVista.FUENTE_NEGRITA);
        lblTitulo.setForeground(new Color(3, 45, 102));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        panel.add(lblTitulo, gbc);

        // Fila 1: Estado actual y descripción
        lblDetalleEstado.setFont(EstiloVista.FUENTE_BASE);
        gbc.gridy = 1;
        panel.add(lblDetalleEstado, gbc);

        // Fila 2: Siguiente estado
        lblSiguienteEstado.setFont(EstiloVista.FUENTE_NEGRITA);
        lblSiguienteEstado.setForeground(COLOR_PRIMARIO);
        gbc.gridy = 2;
        panel.add(lblSiguienteEstado, gbc);

        // Fila 3: Fila de acciones (Avanzar estado + Deshacer última acción + Etiqueta de última acción)
        JPanel filaAcciones = new JPanel(new GridBagLayout());
        filaAcciones.setOpaque(false);
        GridBagConstraints gbcAcc = new GridBagConstraints();
        gbcAcc.insets = new Insets(6, 2, 2, 6);
        gbcAcc.gridy = 0;
        gbcAcc.anchor = GridBagConstraints.WEST;

        btnAvanzar.setFont(EstiloVista.FUENTE_NEGRITA);
        btnAvanzar.setBackground(COLOR_PRIMARIO);
        btnAvanzar.setForeground(Color.WHITE);
        btnAvanzar.setFocusPainted(false);
        btnAvanzar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAvanzar.setMargin(new Insets(5, 12, 5, 12));
        btnAvanzar.setEnabled(false);
        btnAvanzar.addActionListener(e -> confirmarYAvanzarEstado());
        gbcAcc.gridx = 0;
        gbcAcc.weightx = 0;
        filaAcciones.add(btnAvanzar, gbcAcc);

        btnDeshacer.setFont(EstiloVista.FUENTE_NEGRITA);
        btnDeshacer.setBackground(COLOR_DESHACER);
        btnDeshacer.setForeground(Color.WHITE);
        btnDeshacer.setFocusPainted(false);
        btnDeshacer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeshacer.setMargin(new Insets(5, 12, 5, 12));
        btnDeshacer.setEnabled(false);
        btnDeshacer.addActionListener(e -> AccionDeshacer.ejecutarDeshacer(this, controlador, btnDeshacer, this::recargar));
        gbcAcc.gridx = 1;
        gbcAcc.weightx = 0;
        filaAcciones.add(btnDeshacer, gbcAcc);

        lblUltimaAccion.setFont(EstiloVista.FUENTE_BASE);
        gbcAcc.gridx = 2;
        gbcAcc.weightx = 1.0;
        gbcAcc.fill = GridBagConstraints.HORIZONTAL;
        filaAcciones.add(lblUltimaAccion, gbcAcc);

        gbc.gridy = 3;
        panel.add(filaAcciones, gbc);

        return panel;
    }

    private void buscarPorDni() {
        String dni = txtDniFiltro.getText().trim();
        if (dni.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese un número de DNI para realizar la búsqueda.",
                    "DNI requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!dni.matches("\\d{8}")) {
            JOptionPane.showMessageDialog(this,
                    "El DNI debe contener exactamente 8 dígitos.",
                    "DNI inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        filtroDniActual = dni;
        cargarSolicitudes(filtroDniActual, null);
    }

    private void actualizarDetalleSeleccion() {
        AccionDeshacer.actualizarEstadoControles(controlador, btnDeshacer, lblUltimaAccion);

        int fila = tablaSolicitudes.getSelectedRow();
        if (fila == -1) {
            lblDetalleEstado.setText("Seleccione un expediente de la tabla para ver su seguimiento.");
            lblSiguienteEstado.setText("");
            btnAvanzar.setEnabled(false);
            return;
        }

        String codigo = (String) modeloTabla.getValueAt(fila, 0);
        String estado = (String) modeloTabla.getValueAt(fila, 3);
        String descripcion = controlador.descripcionEstado(estado);
        String siguiente = controlador.siguienteEstado(estado);

        lblDetalleEstado.setText("Expediente " + codigo + " (" + estado + "): " + descripcion);

        if ("-".equals(siguiente)) {
            lblSiguienteEstado.setText("Trámite finalizado (no admite más avances)");
            btnAvanzar.setEnabled(false);
        } else {
            lblSiguienteEstado.setText("Siguiente estado: " + siguiente);
            btnAvanzar.setEnabled(true);
        }
    }

    private void confirmarYAvanzarEstado() {
        int fila = tablaSolicitudes.getSelectedRow();
        if (fila == -1) return;

        String codigo = (String) modeloTabla.getValueAt(fila, 0);
        String estadoActual = (String) modeloTabla.getValueAt(fila, 3);
        String siguiente = controlador.siguienteEstado(estadoActual);

        if ("-".equals(siguiente)) {
            JOptionPane.showMessageDialog(this,
                    "El trámite ya se encuentra en su estado final (" + estadoActual + ").",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int resp = JOptionPane.showConfirmDialog(this,
                "¿Desea avanzar el expediente " + codigo + " de '" + estadoActual + "' a '" + siguiente + "'?",
                "Confirmar avance de estado",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (resp != JOptionPane.YES_OPTION) {
            return;
        }

        btnAvanzar.setEnabled(false);
        SwingWorker<Expediente, Void> worker = new SwingWorker<Expediente, Void>() {
            @Override
            protected Expediente doInBackground() {
                return controlador.avanzarEstado(codigo);
            }

            @Override
            protected void done() {
                try {
                    Expediente expActualizado = get();
                    cargarSolicitudes(filtroDniActual, expActualizado.getCodigo());
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(PanelMisSolicitudes.this,
                            "Error al avanzar estado: " + causa.getMessage(),
                            "Error de persistencia", JOptionPane.ERROR_MESSAGE);
                    btnAvanzar.setEnabled(true);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        worker.execute();
    }

    public void recargar() {
        cargarSolicitudes(filtroDniActual, obtenerCodigoSeleccionado());
    }

    private String obtenerCodigoSeleccionado() {
        int fila = tablaSolicitudes.getSelectedRow();
        if (fila != -1) {
            return (String) modeloTabla.getValueAt(fila, 0);
        }
        return null;
    }

    private String formatearFecha(String fechaOriginal) {
        if (fechaOriginal == null || fechaOriginal.trim().isEmpty()) {
            return "";
        }
        try {
            if (fechaOriginal.length() >= 19 && fechaOriginal.charAt(10) == ' ') {
                String yyyy = fechaOriginal.substring(0, 4);
                String mm = fechaOriginal.substring(5, 7);
                String dd = fechaOriginal.substring(8, 10);
                String hora = fechaOriginal.substring(11, 16);
                return dd + "/" + mm + "/" + yyyy + " " + hora;
            }
            if (fechaOriginal.contains("T") && fechaOriginal.length() >= 16) {
                String yyyy = fechaOriginal.substring(0, 4);
                String mm = fechaOriginal.substring(5, 7);
                String dd = fechaOriginal.substring(8, 10);
                String hora = fechaOriginal.substring(11, 16);
                return dd + "/" + mm + "/" + yyyy + " " + hora;
            }
        } catch (Exception ignored) {
        }
        return fechaOriginal;
    }

    private void cargarSolicitudes(String dniOpcional, String codigoASeleccionar) {
        SwingWorker<List<Expediente>, Void> worker = new SwingWorker<List<Expediente>, Void>() {
            @Override
            protected List<Expediente> doInBackground() {
                return controlador.listarMisSolicitudes(dniOpcional);
            }

            @Override
            protected void done() {
                try {
                    List<Expediente> lista = get();
                    modeloTabla.setRowCount(0);

                    if (lista == null || lista.isEmpty()) {
                        lblSinResultados.setVisible(true);
                        actualizarDetalleSeleccion();
                        EstiloVista.ajustarColumnas(tablaSolicitudes);
                        return;
                    }

                    lblSinResultados.setVisible(false);
                    int filaSeleccionar = -1;

                    for (int i = 0; i < lista.size(); i++) {
                        Expediente e = lista.get(i);
                        modeloTabla.addRow(new Object[]{
                                e.getCodigo(),
                                e.getTipoDocumento(),
                                e.getNombreCiudadano(),
                                e.getEstado(),
                                controlador.situacionEstado(e.getEstado()),
                                formatearFecha(e.getFechaRegistro())
                        });

                        if (codigoASeleccionar != null && codigoASeleccionar.equals(e.getCodigo())) {
                            filaSeleccionar = i;
                        }
                    }

                    if (filaSeleccionar != -1) {
                        tablaSolicitudes.setRowSelectionInterval(filaSeleccionar, filaSeleccionar);
                    } else if (tablaSolicitudes.getRowCount() > 0) {
                        tablaSolicitudes.setRowSelectionInterval(0, 0);
                    }

                    actualizarDetalleSeleccion();
                    EstiloVista.ajustarColumnas(tablaSolicitudes);
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(PanelMisSolicitudes.this,
                            "Error al consultar trámites: " + causa.getMessage(),
                            "Error de persistencia", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        worker.execute();
    }
}
