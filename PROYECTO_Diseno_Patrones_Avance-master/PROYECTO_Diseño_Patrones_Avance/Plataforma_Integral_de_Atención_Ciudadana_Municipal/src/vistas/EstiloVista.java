package vistas;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;

/**
 * GRASP Fabricación Pura: clase de utilidad que centraliza estilos tipográficos
 * unificados y dimensionamiento adaptativo de tablas para toda la interfaz gráfica.
 */
public final class EstiloVista {

    public static final Font FUENTE_BASE = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FUENTE_NEGRITA = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 17);
    public static final Font FUENTE_ENCABEZADO = new Font("SansSerif", Font.BOLD, 21);
    public static final Font FUENTE_CURSIVA = new Font("SansSerif", Font.ITALIC, 13);
    public static final Font FUENTE_MONO = new Font("Monospaced", Font.PLAIN, 13);

    public static final int ALTO_FILA = 28;
    public static final int MARGEN_COLUMNA = 20;
    public static final int MAX_ANCHO_COLUMNA = 400;
    public static final int MIN_ANCHO_COLUMNA = 80;

    private EstiloVista() {
        // Constructor privado para clase utilitaria
    }

    /**
     * Ajusta dinámicamente el ancho de cada columna de la tabla evaluando el encabezado
     * y el contenido de cada celda con FontMetrics, asegurando que ningún texto quede truncado.
     *
     * @param tabla tabla Swing a redimensionar
     */
    public static void ajustarColumnas(JTable tabla) {
        if (tabla == null) {
            return;
        }

        tabla.setRowHeight(ALTO_FILA);
        TableColumnModel modeloColumnas = tabla.getColumnModel();
        int totalColumnas = tabla.getColumnCount();
        FontMetrics fm = tabla.getFontMetrics(FUENTE_BASE);
        FontMetrics fmHeader = tabla.getTableHeader() != null
                ? tabla.getTableHeader().getFontMetrics(FUENTE_NEGRITA)
                : fm;

        for (int col = 0; col < totalColumnas; col++) {
            TableColumn columna = modeloColumnas.getColumn(col);
            int anchoCalculado = 0;

            // 1. Medir texto de encabezado
            Object valorHeader = columna.getHeaderValue();
            if (valorHeader != null) {
                int anchoHeader = fmHeader.stringWidth(valorHeader.toString());
                anchoCalculado = Math.max(anchoCalculado, anchoHeader);
            }

            // Medir componente renderer de encabezado
            TableCellRenderer rendererHeader = columna.getHeaderRenderer();
            if (rendererHeader == null && tabla.getTableHeader() != null) {
                rendererHeader = tabla.getTableHeader().getDefaultRenderer();
            }
            if (rendererHeader != null) {
                Component compHeader = rendererHeader.getTableCellRendererComponent(
                        tabla, valorHeader, false, false, -1, col);
                if (compHeader != null) {
                    anchoCalculado = Math.max(anchoCalculado, compHeader.getPreferredSize().width);
                }
            }

            // 2. Medir celdas de datos
            int filas = tabla.getRowCount();
            for (int fila = 0; fila < filas; fila++) {
                Object valorCelda = tabla.getValueAt(fila, col);
                if (valorCelda != null) {
                    int anchoTexto = fm.stringWidth(valorCelda.toString());
                    anchoCalculado = Math.max(anchoCalculado, anchoTexto);
                }

                TableCellRenderer renderer = tabla.getCellRenderer(fila, col);
                Component comp = tabla.prepareRenderer(renderer, fila, col);
                if (comp != null) {
                    anchoCalculado = Math.max(anchoCalculado, comp.getPreferredSize().width);
                }
            }

            // 3. Aplicar márgenes y límites razonables
            int anchoFinal = Math.max(anchoCalculado + MARGEN_COLUMNA, MIN_ANCHO_COLUMNA);
            anchoFinal = Math.min(anchoFinal, MAX_ANCHO_COLUMNA);
            columna.setPreferredWidth(anchoFinal);
        }
    }
}
