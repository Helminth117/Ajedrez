import javax.swing.JButton;
import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Image;
import java.awt.Color;
import java.awt.AlphaComposite; // Para transparencias

/**
 * VISTA: Un botón personalizado para una casilla del tablero.
 * Dibuja su propio fondo y escala la imagen de la pieza.
 */
public class ChessSquareButton extends JButton {

    private Image pieceImage = null; // Imagen de la pieza (o null si está vacía)
    private Color baseColor;         // Color de fondo original (claro u oscuro)
    private Color highlightColor = null; // Color de resaltado actual (o null si no hay)

    public ChessSquareButton(Color baseColor) {
        this.baseColor = baseColor;
        // Configuración inicial copiada de createChessBoardComponents
        setOpaque(false); // Dejamos que paintComponent maneje el fondo
        setContentAreaFilled(false);
        setBorderPainted(false);
        setBorder(null);
        setMargin(new java.awt.Insets(0, 0, 0, 0));
        setPreferredSize(new java.awt.Dimension(70, 70));
        setFocusPainted(false);
    }

    /** Establece la imagen de la pieza a mostrar (o null para vaciar). */
    public void setPieceImage(Image image) {
        this.pieceImage = image;
        this.repaint(); // Repintar para mostrar/ocultar la imagen
    }

    /** Establece el color de resaltado (o null para quitarlo). */
    public void setHighlight(Color highlight) {
        // Solo repintar si el color realmente cambia
        if (this.highlightColor == null && highlight == null) return;
        if (this.highlightColor != null && this.highlightColor.equals(highlight)) return;

        this.highlightColor = highlight;
        this.repaint();
    }

    /** Restaura el color de fondo original. */
    public void clearHighlight() {
        setHighlight(null);
    }

    /** ¡NUEVO! Obtiene el color de resaltado actual (puede ser null). */
    public Color getHighlightColor() {
        return highlightColor;
    }


    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Dibujar fondo (base o resaltado)
        Color bgColor = (highlightColor != null) ? highlightColor : baseColor;
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // 2. Dibujar la imagen de la pieza (si existe)
        if (pieceImage != null) {
            // Calcular tamaño escalado (ej. 80% del tamaño de la casilla)
            int size = Math.min(getWidth(), getHeight());
            if (size <= 0) size = 60; // Fallback si el botón aún no tiene tamaño
            int imgSize = (int) (size * 0.8); // Ajusta este factor (0.7 a 0.85) si es necesario
            int x = (getWidth() - imgSize) / 2; // Centrar horizontalmente
            int y = (getHeight() - imgSize) / 2; // Centrar verticalmente

            g2d.drawImage(pieceImage, x, y, imgSize, imgSize, this);
        }

        g2d.dispose();
        // No llamamos a super.paintComponent() para tener control total
    }
}