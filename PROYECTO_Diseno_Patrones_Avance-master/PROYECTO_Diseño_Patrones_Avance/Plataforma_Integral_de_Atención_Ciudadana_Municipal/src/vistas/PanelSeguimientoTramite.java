package vistas;

import controladores.AtencionController;
import patrones.creacionales.builder.Expediente;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.ExecutionException;

/**
 * Vista Swing: pantalla "Seguimiento y Cambio de Estado de Trámite".
 * Permite buscar un trámite por su código, ver sus datos y cambiar su estado.
 * Patrones involucrados (se ven en el panel "Actividad del sistema"):
 *  - State: define cuál es el siguiente estado válido del trámite.
 *  - Command: el cambio de estado se ejecuta como un comando reversible (AvanzarEstadoComando).
 *  - Memento: antes de cambiar, se guarda una instantánea del estado previo para poder deshacer.
 *  - Observer / Bridge / Decorator: notifican al ciudadano por correo y SMS tras el cambio.
 * MVC: la vista solo se comunica con AtencionController.
 */
public class PanelSeguimientoTramite extends JPanel {

    private static final Color COLOR_AZUL_MARINO = new Color(3, 45, 102);
    private static final Color COLOR_AZUL_ACCION = new Color(0, 74, 173);
    private static final Color COLOR_DESHACER = new Color(180, 40, 40);
    private static final Color COLOR_BORDE = new Color(210, 225, 245);

    private final AtencionController controlador;
    private final Runnable alCambiar;

    private final JTextField txtCodigo = new JTextField(18);
    private final JButton btnBuscar = new JButton("Buscar");

    private final JLabel lblCodigo = new JLabel("-");
    private final JLabel lblCiudadano = new JLabel("-");
    private final JLabel lblCorreo = new JLabel("-");
    private final JLabel lblTramite = new JLabel("-");
    private final JLabel lblEstado = new JLabel("-");

    private final JComboBox<String> cmbNuevoEstado = new JComboBox<>();
    private final JButton btnActualizar = new JButton("Actualizar Estado");
    private final JButton btnDeshacer = new JButton("Deshacer último cambio");
    private final JLabel lblUltimaAccion = new JLabel("Sin acciones para deshacer");
    private final JLabel lblMensaje = new JLabel(" ");

    private Expediente expedienteActual;

    /**
     * @param controlador controlador MVC
     * @param alCambiar   acción a ejecutar cuando cambia el estado o se deshace (refrescar otros paneles)
     */
    public PanelSeguimientoTramite(AtencionController controlador, Runnable alCambiar) {
        this.controlador = controlador;
        this.alCambiar = alCambiar;

        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(245, 248, 253));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        add(crearTarjeta(), BorderLayout.NORTH);

        limpiarDetalle();
        actualizarControlesDeshacer();
    }

    private JPanel crearTarjeta() {
        JPanel tarjeta = new JPanel(new GridBagLayout());
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Título
        JLabel titulo = new JLabel("Seguimiento y Cambio de Estado de Trámite");
        titulo.setFont(EstiloVista.FUENTE_TITULO);
        titulo.setForeground(COLOR_AZUL_MARINO);
        gbc.gridy = 0;
        tarjeta.add(titulo, gbc);

        // Búsqueda por código
        JPanel filaBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaBusqueda.setBackground(Color.WHITE);
        JLabel lblBuscar = new JLabel("Buscar por Código de Trámite:");
        lblBuscar.setFont(EstiloVista.FUENTE_BASE);
        filaBusqueda.add(lblBuscar);
        txtCodigo.setFont(EstiloVista.FUENTE_BASE);
        txtCodigo.setToolTipText("Ejemplo: EXP-2026-0001");
        txtCodigo.addActionListener(e -> buscar());
        filaBusqueda.add(txtCodigo);
        btnBuscar.setFont(EstiloVista.FUENTE_NEGRITA);
        btnBuscar.setBackground(COLOR_AZUL_ACCION);
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFocusPainted(false);
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscar.addActionListener(e -> buscar());
        filaBusqueda.add(btnBuscar);
        gbc.gridy = 1;
        tarjeta.add(filaBusqueda, gbc);

        // Detalle del trámite
        JPanel detalle = new JPanel(new GridBagLayout());
        detalle.setBackground(new Color(250, 252, 255));
        detalle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        agregarFilaDetalle(detalle, 0, "Código:", lblCodigo);
        agregarFilaDetalle(detalle, 1, "Ciudadano:", lblCiudadano);
        agregarFilaDetalle(detalle, 2, "Correo:", lblCorreo);
        agregarFilaDetalle(detalle, 3, "Trámite:", lblTramite);
        agregarFilaDetalle(detalle, 4, "Estado actual:", lblEstado);
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 4, 10, 4);
        tarjeta.add(detalle, gbc);
        gbc.insets = new Insets(5, 4, 5, 4);

        // Cambiar estado
        JPanel filaEstado = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaEstado.setBackground(Color.WHITE);
        JLabel lblCambiar = new JLabel("Cambiar estado a:");
        lblCambiar.setFont(EstiloVista.FUENTE_BASE);
        filaEstado.add(lblCambiar);
        cmbNuevoEstado.setFont(EstiloVista.FUENTE_BASE);
        cmbNuevoEstado.setPrototypeDisplayValue("En Revision   ");
        filaEstado.add(cmbNuevoEstado);
        gbc.gridy = 3;
        tarjeta.add(filaEstado, gbc);

        // Botones
        JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaBotones.setBackground(Color.WHITE);
        btnActualizar.setFont(EstiloVista.FUENTE_NEGRITA);
        btnActualizar.setBackground(COLOR_AZUL_ACCION);
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFocusPainted(false);
        btnActualizar.setMargin(new Insets(6, 16, 6, 16));
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.addActionListener(e -> actualizarEstado());
        filaBotones.add(btnActualizar);

        btnDeshacer.setFont(EstiloVista.FUENTE_NEGRITA);
        btnDeshacer.setBackground(COLOR_DESHACER);
        btnDeshacer.setForeground(Color.WHITE);
        btnDeshacer.setFocusPainted(false);
        btnDeshacer.setMargin(new Insets(6, 16, 6, 16));
        btnDeshacer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeshacer.addActionListener(e -> AccionDeshacer.ejecutarDeshacer(
                this, controlador, btnDeshacer, this::alDeshacer));
        filaBotones.add(btnDeshacer);
        gbc.gridy = 4;
        tarjeta.add(filaBotones, gbc);

        lblUltimaAccion.setFont(EstiloVista.FUENTE_CURSIVA);
        gbc.gridy = 5;
        tarjeta.add(lblUltimaAccion, gbc);

        lblMensaje.setFont(EstiloVista.FUENTE_NEGRITA);
        gbc.gridy = 6;
        tarjeta.add(lblMensaje, gbc);

        return tarjeta;
    }

    private void agregarFilaDetalle(JPanel panel, int fila, String etiqueta, JLabel valor) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(2, 0, 2, 10);
        c.anchor = GridBagConstraints.WEST;

        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(EstiloVista.FUENTE_NEGRITA);
        c.gridx = 0;
        panel.add(lbl, c);

        valor.setFont(EstiloVista.FUENTE_BASE);
        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(valor, c);
    }

    /** Busca el trámite por código y muestra sus datos. */
    private void buscar() {
        String codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            mostrarMensaje("Ingrese un código de trámite (ej. EXP-2026-0001).", true);
            return;
        }
        try {
            expedienteActual = controlador.buscarTramite(codigo);
            mostrarDetalle(expedienteActual);
            mostrarMensaje("Trámite encontrado.", false);
        } catch (Exception ex) {
            expedienteActual = null;
            limpiarDetalle();
            mostrarMensaje("No se encontró el trámite: " + codigo, true);
        }
    }

    /**
     * Cambia el estado del trámite. El estado destino lo define el patrón State
     * (siguiente estado válido); la acción se ejecuta como Command con Memento para poder deshacer.
     */
    private void actualizarEstado() {
        if (expedienteActual == null) {
            mostrarMensaje("Primero busque un trámite.", true);
            return;
        }
        Object seleccion = cmbNuevoEstado.getSelectedItem();
        if (seleccion == null) {
            mostrarMensaje("El trámite ya está en estado final (Cerrado).", true);
            return;
        }

        final String codigo = expedienteActual.getCodigo();
        final String actual = expedienteActual.getEstado();
        int resp = JOptionPane.showConfirmDialog(this,
                "¿Cambiar el estado del trámite " + codigo + "?\n"
                        + actual + "  ->  " + seleccion
                        + "\n\nSe notificará al ciudadano por correo y SMS.",
                "Confirmar cambio de estado",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (resp != JOptionPane.YES_OPTION) {
            return;
        }

        btnActualizar.setEnabled(false);
        SwingWorker<Expediente, Void> worker = new SwingWorker<Expediente, Void>() {
            @Override
            protected Expediente doInBackground() {
                return controlador.avanzarEstado(codigo);
            }

            @Override
            protected void done() {
                try {
                    expedienteActual = get();
                    mostrarDetalle(expedienteActual);
                    mostrarMensaje("Estado actualizado: " + expedienteActual.getEstado(), false);
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(PanelSeguimientoTramite.this,
                            "Error al actualizar el estado: " + causa.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    mostrarDetalle(expedienteActual);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    notificarCambio();
                }
            }
        };
        worker.execute();
    }

    /** Se ejecuta al terminar un Deshacer (Command + Memento): vuelve a leer el trámite mostrado. */
    private void alDeshacer() {
        if (expedienteActual != null) {
            try {
                expedienteActual = controlador.buscarTramite(expedienteActual.getCodigo());
                mostrarDetalle(expedienteActual);
                mostrarMensaje("Cambio deshecho. Estado actual: " + expedienteActual.getEstado(), false);
            } catch (Exception ex) {
                // Si el trámite ya no existe (se deshizo su registro), se limpia la pantalla
                expedienteActual = null;
                limpiarDetalle();
                mostrarMensaje("Acción deshecha.", false);
            }
        }
        notificarCambio();
    }

    private void notificarCambio() {
        actualizarControlesDeshacer();
        if (alCambiar != null) {
            alCambiar.run();
        }
    }

    private void mostrarDetalle(Expediente exp) {
        lblCodigo.setText(exp.getCodigo());
        lblCiudadano.setText(exp.getNombreCiudadano() + "   |   DNI: " + exp.getDni());
        lblCorreo.setText(exp.getCorreo());
        lblTramite.setText(exp.getTipoDocumento());
        lblEstado.setText(exp.getEstado());

        // El combo solo ofrece el siguiente estado válido según el patrón State
        cmbNuevoEstado.removeAllItems();
        String siguiente = controlador.siguienteEstado(exp.getEstado());
        boolean hayAvance = siguiente != null && !"-".equals(siguiente);
        if (hayAvance) {
            cmbNuevoEstado.addItem(siguiente);
        }
        cmbNuevoEstado.setEnabled(hayAvance);
        btnActualizar.setEnabled(hayAvance);
        actualizarControlesDeshacer();
    }

    private void limpiarDetalle() {
        lblCodigo.setText("-");
        lblCiudadano.setText("-");
        lblCorreo.setText("-");
        lblTramite.setText("-");
        lblEstado.setText("-");
        cmbNuevoEstado.removeAllItems();
        cmbNuevoEstado.setEnabled(false);
        btnActualizar.setEnabled(false);
    }

    private void actualizarControlesDeshacer() {
        AccionDeshacer.actualizarEstadoControles(controlador, btnDeshacer, lblUltimaAccion);
    }

    private void mostrarMensaje(String texto, boolean error) {
        lblMensaje.setText(texto);
        lblMensaje.setForeground(error ? COLOR_DESHACER : new Color(30, 120, 60));
    }
}
