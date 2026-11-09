package vistas;

import controladores.AtencionController;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vista Swing principal: ventana modular con navegación por secciones y panel de actividad.
 * Estructura:
 *  - Encabezado institucional azul marino (#032D66).
 *  - Barra de secciones tipo pestaña (REGISTRO | SEGUIMIENTO | SISTEMA).
 *  - Panel izquierdo con las opciones de la sección activa (ancho adaptado a 230 px).
 *  - Panel central dinámico con CardLayout para la opción elegida.
 *  - Panel lateral derecho "Actividad del sistema" (ancho 380 px) con botón "Limpiar".
 *  - Barra inferior con el estado de conexión de la base de datos.
 * Patrón MVC: delega todas las acciones en AtencionController.
 * GRASP Alta Cohesión: estilos centralizados en EstiloVista.
 */
public class Main extends JFrame {

    private final AtencionController controlador = new AtencionController();

    // Colores del diseño
    private static final Color COLOR_AZUL_MARINO = new Color(3, 45, 102);  // #032D66
    private static final Color COLOR_AZUL_ACCION = new Color(0, 74, 173);  // #004AAD
    private static final Color COLOR_FONDO_CLARO = new Color(234, 241, 251); // #EAF1FB
    private static final Color COLOR_BORDE = new Color(210, 225, 245);

    // Contenedores principales
    private final JPanel barraSecciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final DefaultListModel<String> modeloOpciones = new DefaultListModel<>();
    private final JList<String> listaOpciones = new JList<>(modeloOpciones);
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelCentroCards = new JPanel(cardLayout);
    private final JTextArea resultadoArea = new JTextArea();
    private final JLabel estadoLabel = new JLabel();

    // Estructura de opciones por sección
    private static class OpcionMenu {
        final String titulo;
        final JPanel panel;

        OpcionMenu(String titulo, JPanel panel) {
            this.titulo = titulo;
            this.panel = panel;
        }
    }

    private final Map<String, List<OpcionMenu>> opcionesPorSeccion = new LinkedHashMap<>();
    private final Map<String, JButton> botonesSeccion = new LinkedHashMap<>();
    private String seccionActiva = "REGISTRO";

    // Paneles de trabajo
    private PanelRegistro panelRegistro;
    private PanelImportacion panelImportacion;
    private PanelMisSolicitudes panelMisSolicitudes;
    private PanelSeguimientoTramite panelSeguimientoTramite;
    private PanelHistorialAcciones panelHistorialAcciones;
    private PanelSistema panelSistema;
    private PanelDemoPatrones panelDemoPatrones;

    public Main() {
        setTitle("Plataforma Integral de Atención Ciudadana Municipal - " + controlador.nombreMunicipio());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setMinimumSize(new Dimension(1000, 640));
        setResizable(true);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Redirección de consola a la actividad del sistema
        configurarRedireccionConsola();

        // 2. Encabezado institucional
        add(crearEncabezado(), BorderLayout.NORTH);

        // 3. Panel de trabajo central (Navegación + Cards + Actividad)
        add(crearCuerpoPrincipal(), BorderLayout.CENTER);

        // 4. Barra de estado inferior
        add(crearBarraEstado(), BorderLayout.SOUTH);

        // 5. Configurar opciones y registrar módulos
        inicializarModulos();

        // 6. Mensaje inicial en actividad del sistema
        mostrarMensajeBienvenida();
    }

    private JPanel crearEncabezado() {
        JPanel contenedorNorte = new JPanel(new BorderLayout());
        contenedorNorte.setBackground(COLOR_AZUL_MARINO);

        // Franja de títulos
        JPanel panelTitulos = new JPanel(new BorderLayout());
        panelTitulos.setBackground(COLOR_AZUL_MARINO);
        panelTitulos.setBorder(BorderFactory.createEmptyBorder(14, 24, 10, 24));

        JLabel titulo = new JLabel("Plataforma Integral de Atención Ciudadana Municipal");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(EstiloVista.FUENTE_ENCABEZADO);
        panelTitulos.add(titulo, BorderLayout.NORTH);

        JLabel subtitulo = new JLabel(controlador.nombreMunicipio());
        subtitulo.setForeground(new Color(220, 235, 250));
        subtitulo.setFont(EstiloVista.FUENTE_BASE);
        panelTitulos.add(subtitulo, BorderLayout.SOUTH);

        contenedorNorte.add(panelTitulos, BorderLayout.NORTH);

        // Barra de pestañas de secciones
        barraSecciones.setBackground(new Color(15, 60, 120));
        barraSecciones.setBorder(BorderFactory.createEmptyBorder(4, 20, 4, 20));
        contenedorNorte.add(barraSecciones, BorderLayout.SOUTH);

        return contenedorNorte;
    }

    private JPanel crearCuerpoPrincipal() {
        JPanel cuerpo = new JPanel(new BorderLayout(8, 0));
        cuerpo.setBackground(COLOR_FONDO_CLARO);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Panel Izquierdo: Lista de opciones de la sección (ancho 230 px para evitar truncamiento)
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.setPreferredSize(new Dimension(230, 0));
        panelIzquierdo.setBackground(Color.WHITE);
        panelIzquierdo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblOpciones = new JLabel("Menú de opciones");
        lblOpciones.setFont(EstiloVista.FUENTE_NEGRITA);
        lblOpciones.setForeground(COLOR_AZUL_MARINO);
        lblOpciones.setBorder(BorderFactory.createEmptyBorder(4, 6, 10, 6));
        panelIzquierdo.add(lblOpciones, BorderLayout.NORTH);

        listaOpciones.setFont(EstiloVista.FUENTE_BASE);
        listaOpciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaOpciones.setFixedCellHeight(32);
        listaOpciones.setSelectionBackground(COLOR_FONDO_CLARO);
        listaOpciones.setSelectionForeground(COLOR_AZUL_ACCION);
        listaOpciones.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String seleccion = listaOpciones.getSelectedValue();
                if (seleccion != null) {
                    mostrarPanelOpcion(seccionActiva, seleccion);
                }
            }
        });

        JScrollPane scrollOpciones = new JScrollPane(listaOpciones);
        scrollOpciones.setBorder(BorderFactory.createEmptyBorder());
        panelIzquierdo.add(scrollOpciones, BorderLayout.CENTER);

        cuerpo.add(panelIzquierdo, BorderLayout.WEST);

        // Panel Central: Cards de contenido
        panelCentroCards.setBackground(COLOR_FONDO_CLARO);
        cuerpo.add(panelCentroCards, BorderLayout.CENTER);

        // Panel Derecho: Actividad del sistema
        cuerpo.add(crearPanelActividad(), BorderLayout.EAST);

        return cuerpo;
    }

    private JPanel crearPanelActividad() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setPreferredSize(new Dimension(380, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        // Cabecera de actividad con botón Limpiar
        JPanel cabeceraActividad = new JPanel(new BorderLayout());
        cabeceraActividad.setBackground(Color.WHITE);

        JLabel titulo = new JLabel("Actividad del sistema");
        titulo.setFont(EstiloVista.FUENTE_NEGRITA);
        titulo.setForeground(COLOR_AZUL_MARINO);
        cabeceraActividad.add(titulo, BorderLayout.WEST);

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setFont(EstiloVista.FUENTE_BASE);
        btnLimpiar.setBackground(new Color(240, 244, 250));
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiar.setMargin(new java.awt.Insets(3, 8, 3, 8));
        btnLimpiar.addActionListener(e -> resultadoArea.setText(""));
        cabeceraActividad.add(btnLimpiar, BorderLayout.EAST);

        panel.add(cabeceraActividad, BorderLayout.NORTH);

        resultadoArea.setEditable(false);
        resultadoArea.setLineWrap(true);
        resultadoArea.setWrapStyleWord(true);
        resultadoArea.setFont(EstiloVista.FUENTE_MONO);
        resultadoArea.setBackground(new Color(250, 252, 255));

        JScrollPane scrollArea = new JScrollPane(resultadoArea);
        scrollArea.setBorder(BorderFactory.createLineBorder(new Color(225, 235, 248), 1));
        panel.add(scrollArea, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearBarraEstado() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(new Color(245, 248, 252));
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE),
                BorderFactory.createEmptyBorder(6, 20, 6, 20)
        ));

        estadoLabel.setFont(EstiloVista.FUENTE_BASE);
        estadoLabel.setForeground(new Color(70, 85, 100));
        actualizarEstadoBD();
        barra.add(estadoLabel, BorderLayout.WEST);

        return barra;
    }

    private void inicializarModulos() {
        // Instanciar paneles de trabajo
        panelRegistro = new PanelRegistro(controlador, () -> {
            actualizarEstadoBD();
            if (panelMisSolicitudes != null) {
                panelMisSolicitudes.recargar();
            }
            if (panelHistorialAcciones != null) {
                panelHistorialAcciones.cargarHistorial();
            }
        });

        panelImportacion = new PanelImportacion(controlador, () -> {
            actualizarEstadoBD();
            if (panelMisSolicitudes != null) {
                panelMisSolicitudes.recargar();
            }
            if (panelHistorialAcciones != null) {
                panelHistorialAcciones.cargarHistorial();
            }
        });

        panelMisSolicitudes = new PanelMisSolicitudes(controlador);

        panelSeguimientoTramite = new PanelSeguimientoTramite(controlador, () -> {
            if (panelMisSolicitudes != null) {
                panelMisSolicitudes.recargar();
            }
            if (panelHistorialAcciones != null) {
                panelHistorialAcciones.cargarHistorial();
            }
        });

        panelHistorialAcciones = new PanelHistorialAcciones(controlador);

        panelSistema = new PanelSistema(controlador, this::actualizarEstadoBD);

        panelDemoPatrones = new PanelDemoPatrones();

        // Registro de opciones (una línea por opción)
        agregarOpcion("REGISTRO", "Registrar trámite", panelRegistro);
        agregarOpcion("REGISTRO", "Importar quejas del sistema antiguo", panelImportacion);
        agregarOpcion("SEGUIMIENTO", "Seguimiento de trámite", panelSeguimientoTramite);
        agregarOpcion("SEGUIMIENTO", "Mis solicitudes", panelMisSolicitudes);
        agregarOpcion("SEGUIMIENTO", "Historial de acciones", panelHistorialAcciones);
        agregarOpcion("SISTEMA", "Conexión (Singleton)", panelSistema);
        agregarOpcion("SISTEMA", "Demostración de patrones", panelDemoPatrones);

        // Construir botones de sección en la barra superior
        for (String seccion : opcionesPorSeccion.keySet()) {
            JButton btnSeccion = new JButton(seccion);
            btnSeccion.setFont(EstiloVista.FUENTE_NEGRITA);
            btnSeccion.setForeground(Color.WHITE);
            btnSeccion.setFocusPainted(false);
            btnSeccion.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnSeccion.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
            btnSeccion.addActionListener(e -> seleccionarSeccion(seccion));
            barraSecciones.add(btnSeccion);
            botonesSeccion.put(seccion, btnSeccion);
        }

        // Seleccionar sección inicial
        seleccionarSeccion("REGISTRO");
    }

    /**
     * Permite agregar una nueva opción al menú de la aplicación en una sola línea de código.
     * Facilita la extensibilidad para futuros prompts y módulos.
     *
     * @param seccion sección a la que pertenece ("REGISTRO", "SEGUIMIENTO", "SISTEMA")
     * @param titulo  nombre visible de la opción en la lista lateral
     * @param panel   panel de trabajo a mostrar en el centro
     */
    public void agregarOpcion(String seccion, String titulo, JPanel panel) {
        opcionesPorSeccion.computeIfAbsent(seccion, k -> new ArrayList<>())
                .add(new OpcionMenu(titulo, panel));
        String claveCard = seccion + "::" + titulo;
        panelCentroCards.add(panel, claveCard);
    }

    private void seleccionarSeccion(String seccion) {
        this.seccionActiva = seccion;

        // Actualizar estilos de los botones de pestañas
        for (Map.Entry<String, JButton> entry : botonesSeccion.entrySet()) {
            boolean activa = entry.getKey().equals(seccion);
            JButton btn = entry.getValue();
            if (activa) {
                btn.setBackground(COLOR_AZUL_ACCION);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 3, 0, Color.WHITE),
                        BorderFactory.createEmptyBorder(6, 16, 3, 16)
                ));
            } else {
                btn.setBackground(new Color(25, 75, 140));
                btn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
            }
        }

        // Cargar lista de opciones para la sección seleccionada
        modeloOpciones.clear();
        List<OpcionMenu> opciones = opcionesPorSeccion.get(seccion);
        if (opciones != null) {
            for (OpcionMenu op : opciones) {
                modeloOpciones.addElement(op.titulo);
            }
            if (!opciones.isEmpty()) {
                listaOpciones.setSelectedIndex(0);
            }
        }
    }

    private void mostrarPanelOpcion(String seccion, String titulo) {
        String claveCard = seccion + "::" + titulo;
        cardLayout.show(panelCentroCards, claveCard);

        // Si se ingresa a "Mis solicitudes" o "Historial de acciones", recargar datos automáticamente
        if ("Mis solicitudes".equals(titulo) && panelMisSolicitudes != null) {
            panelMisSolicitudes.recargar();
        } else if ("Historial de acciones".equals(titulo) && panelHistorialAcciones != null) {
            panelHistorialAcciones.cargarHistorial();
        }
    }

    public void actualizarEstadoBD() {
        estadoLabel.setText("BD: " + controlador.estadoConexion());
    }

    private void mostrarMensajeBienvenida() {
        resultadoArea.setText("====================================\n"
                + " PLATAFORMA INTEGRAL DE ATENCIÓN\n"
                + " CIUDADANA MUNICIPAL\n"
                + " " + controlador.nombreMunicipio() + "\n"
                + " Estado de persistencia:\n"
                + " " + controlador.estadoConexion() + "\n"
                + "====================================\n"
                + "Bienvenido al sistema. Utilice el\n"
                + "menú superior y lateral para registrar\n"
                + "trámites, consultar solicitudes y\n"
                + "verificar la actividad del sistema en\n"
                + "tiempo real.\n\n");
    }

    /**
     * Redirige System.out al JTextArea de "Actividad del sistema" sin perder la salida en consola.
     */
    private void configurarRedireccionConsola() {
        PrintStream originalOut = System.out;
        OutputStream panelStream = new OutputStream() {
            private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            @Override
            public synchronized void write(int b) {
                originalOut.write(b);
                buffer.write(b);
                if (b == '\n') {
                    volcarBuffer();
                }
            }

            @Override
            public synchronized void write(byte[] b, int off, int len) {
                originalOut.write(b, off, len);
                buffer.write(b, off, len);
                for (int i = off; i < off + len; i++) {
                    if (b[i] == '\n') {
                        volcarBuffer();
                        break;
                    }
                }
            }

            private void volcarBuffer() {
                String texto = buffer.toString(StandardCharsets.UTF_8);
                buffer.reset();
                SwingUtilities.invokeLater(() -> resultadoArea.append(texto));
            }
        };
        System.setOut(new PrintStream(panelStream, true, StandardCharsets.UTF_8));
    }

    /**
     * Punto de entrada de la aplicación con interfaz gráfica Swing.
     */
    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Interfaz gráfica no disponible en este entorno.");
            return;
        }
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
