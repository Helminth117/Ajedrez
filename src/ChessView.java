import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL; // Necesario para getResource
import java.util.*; // Import general util
import java.util.List; // ¡ASEGÚRATE DE QUE ESTA LÍNEA ESTÉ PRESENTE!
import java.awt.*; // Import general AWT (incluye Image, LayoutManager, etc.)

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * VISTA: La ventana principal y todos sus componentes UI.
 * ¡AHORA CON ICONOS ESCALABLES Y GETTER CORREGIDO!
 */
public class ChessView extends JFrame {
    private final BoardModel model;
    private ChessController controller;

    // ¡MODIFICADO! Usa el botón personalizado
    ChessSquareButton[][] squares = new ChessSquareButton[8][8];

    DefaultListModel<String> moveListModel = new DefaultListModel<>();
    JList<String> moveList = new JList<>(moveListModel);
    JPanel capturesWhitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel capturesBlackPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel statusLabel = new JLabel("Turno: Blancas", SwingConstants.CENTER);
    JPanel boardPanel; // Panel estándar

    boolean flipped = false;
    int lastFromR = -1, lastFromC = -1, lastToR = -1, lastToC = -1;

    // --- Paleta de Colores ---
    Color lightColor = new Color(238, 238, 210); // Crema claro
    Color darkColor = new Color(118, 150, 86);   // Verde oscuro
    Color selectedColor = Color.CYAN;
    Color validMoveColor = new Color(144, 238, 144, 150);
    Color moveHighlight = new Color(210, 180, 140, 200);

    // Guarda Image en lugar de ImageIcon
    private Map<String, Image> pieceImages = new HashMap<>();


    public ChessView(BoardModel model) {
        super("♚ Chess Master Pro ♔ (MVC - Scaled Icons v2)");
        this.model = model;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { e.printStackTrace(); }

        loadPieceImages(); // Cargar imágenes

        setJMenuBar(createMenuBar());
        initializeUI();
        updateBoard();
    }

    public void setController(ChessController controller) {
        this.controller = controller;
        linkListeners();
    }

    /**
     * Carga y guarda Image.
     */
    private void loadPieceImages() {
        String[] types = {"P", "N", "B", "R", "Q", "K"};
        String[] colors = {"W", "B"};
        String basePath = "/img/";

        System.out.println("--- Loading Piece Images ---"); // DEBUG
        for (String color : colors) {
            for (String type : types) {
                String fileName = color + type + ".png";
                String key = color + type;
                String fullPath = basePath + fileName;
                try {
                    URL imageURL = getClass().getResource(fullPath);
                    if (imageURL != null) {
                        ImageIcon icon = new ImageIcon(imageURL);
                        pieceImages.put(key, icon.getImage()); // Guarda el Image original
                        System.out.println("Loaded image: " + fullPath + " as key " + key); // DEBUG
                    } else {
                        System.err.println("Error: Image not found at path: " + fullPath);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading image: " + fullPath);
                    e.printStackTrace();
                }
            }
        }
        System.out.println("--- Image Loading Finished ---"); // DEBUG
    }


    // --- Menús y Ayuda (sin cambios) ---
    private JMenuBar createMenuBar() { /*...*/ JMenuBar menuBar = new JMenuBar(); JMenu creditsMenu = new JMenu("Créditos"); JMenuItem aboutItem = new JMenuItem("Acerca de..."); aboutItem.addActionListener(e -> showCredits()); creditsMenu.add(aboutItem); JMenu helpMenu = new JMenu("Ayuda"); JMenuItem helpItem = new JMenuItem("Cómo usar..."); helpItem.addActionListener(e -> showHelp()); helpMenu.add(helpItem); menuBar.add(creditsMenu); menuBar.add(helpMenu); return menuBar; }
    private void showCredits() { /*...*/ String creditsMsg = "<html><div style='text-align: center;'><b>Instituto Tecnologico de Altamira</b><br><br>Ingenieria en Sistemas Computacionales<br>7mo Semestre<br><br><b>Asignatura:</b> Lenguajes y Automatas II<br><b>Asesor/Maestro:</b> Ing. Jose Antonio Castillo Gutierrez<br><br><b>Autor:</b> Erick Joseph Torres Suarez</div></html>"; JOptionPane.showMessageDialog(this, creditsMsg, "Créditos", JOptionPane.INFORMATION_MESSAGE); }
    private void showHelp() { /*...*/ String helpMsg = "<html><body style='width: 400px;'><h2>Ayuda - Chess Master Pro (MVC)</h2><h3>Uso Básico del Tablero (Clic-Clic)</h3><ol><li><b>Seleccionar Pieza:</b> Haz clic en una de tus piezas. La casilla se volverá cian y las casillas destino válidas se resaltarán en verde.</li><li><b>Mover Pieza:</b> Haz clic en una casilla de destino resaltada en verde.</li><li><b>Deseleccionar/Cambiar:</b> Haz clic en otra de tus piezas o en una casilla vacía/inválida.</li></ol><h3>Lista de Movimientos</h3><p>Puedes hacer clic en cualquier movimiento de la lista de la derecha para saltar a esa posición en la partida.</p><h3>Controles Principales</h3><ul><li><b>Cargar:</b> Abre un archivo de texto (.txt) con una partida.</li><li><b>Guardar:</b> Guarda los movimientos de la partida actual en un archivo de texto.</li><li><b>Anterior / Siguiente:</b> Navega por los movimientos de una partida cargada.</li><li><b>Reset:</b> Limpia el tablero y reinicia el juego.</li><li><b>Girar:</b> Invierte la vista del tablero (negras abajo, blancas arriba).</li><li><b>Exportar PGN:</b> Guarda la partida en formato PGN (aún en desarrollo).</li></ul><h3>Cómo Cargar Partidas (Formato de Archivo)</h3><p>El botón <b>'Cargar'</b> lee archivos de texto plano (.txt) con movimientos específicos.</p><p><b>Pasos:</b></p><ol><li>Crea un archivo (ej. <code>mi_partida.txt</code>).</li><li>Escribe los movimientos uno tras otro, separados por un espacio.</li><li>El formato requerido es: <b><code>[ID_Pieza]:[CasillaOrigen][CasillaDestino]</code></b></li></ol><p><b>Ejemplo de contenido del archivo:</b></p><p><code>BP4:e7e5 BN1:b8c6 BB1:f8c5...</code></p></body></html>"; JOptionPane.showMessageDialog(this, helpMsg, "Ayuda y Cómo Usar", JOptionPane.INFORMATION_MESSAGE); }

    // --- Creación de UI ---
    private void initializeUI() { /*...*/ setLayout(new BorderLayout(10, 10)); getContentPane().setBackground(new Color(245, 245, 245)); JPanel mainPanel = new JPanel(new BorderLayout(15, 15)); mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); mainPanel.setBackground(new Color(245, 245, 245)); createChessBoardComponents(); mainPanel.add(createBoardWithCoordinates(), BorderLayout.CENTER); mainPanel.add(createRightPanel(), BorderLayout.EAST); mainPanel.add(createControlPanel(), BorderLayout.SOUTH); createStatusPanel(); mainPanel.add(statusLabel, BorderLayout.NORTH); add(mainPanel, BorderLayout.CENTER); }

    /**
     * Crea ChessSquareButton.
     */
    private void createChessBoardComponents() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Color baseColor = (r + c) % 2 == 0 ? lightColor : darkColor;
                squares[r][c] = new ChessSquareButton(baseColor);
            }
        }
    }

    private JPanel createBoardWithCoordinates() { /*...*/ JPanel boardContainer = new JPanel(new BorderLayout()); boardContainer.setBackground(new Color(245, 245, 245));
        boardPanel = new JPanel(new GridLayout(8, 8)); // Panel estándar
        for (int r = 0; r < 8; r++) { for (int c = 0; c < 8; c++) { boardPanel.add(squares[r][c]); } } // Añade los ChessSquareButton
        JPanel topCoords = new JPanel(new GridLayout(1, 9)); topCoords.setBackground(new Color(245, 245, 245)); topCoords.add(new JLabel("")); for (int c = 0; c < 8; c++) { char coord = (char)('a' + (flipped ? 7 - c : c)); JLabel coordLabel = new JLabel(String.valueOf(coord), SwingConstants.CENTER); coordLabel.setFont(new Font("Arial", Font.BOLD, 14)); coordLabel.setForeground(new Color(80, 80, 80)); topCoords.add(coordLabel); }
        JPanel leftCoords = new JPanel(new GridLayout(8, 1)); leftCoords.setBackground(new Color(245, 245, 245)); for (int r = 0; r < 8; r++) { int rowNum = flipped ? r + 1 : 8 - r; JLabel rowLabel = new JLabel(String.valueOf(rowNum), SwingConstants.CENTER); rowLabel.setFont(new Font("Arial", Font.BOLD, 14)); rowLabel.setForeground(new Color(80, 80, 80)); leftCoords.add(rowLabel); }
        JPanel rightCoords = new JPanel(new GridLayout(8, 1)); rightCoords.setBackground(new Color(245, 245, 245)); for (int r = 0; r < 8; r++) { JLabel dummyLabel = new JLabel(""); rightCoords.add(dummyLabel); }
        JPanel bottomCoords = new JPanel(new GridLayout(1, 9)); bottomCoords.setBackground(new Color(245, 245, 245)); bottomCoords.add(new JLabel("")); for (int c = 0; c < 8; c++) { char coord = (char)('a' + (flipped ? 7 - c : c)); JLabel coordLabel = new JLabel(String.valueOf(coord), SwingConstants.CENTER); coordLabel.setFont(new Font("Arial", Font.BOLD, 14)); coordLabel.setForeground(new Color(80, 80, 80)); bottomCoords.add(coordLabel); }
        boardContainer.add(topCoords, BorderLayout.NORTH); boardContainer.add(leftCoords, BorderLayout.WEST); boardContainer.add(boardPanel, BorderLayout.CENTER); boardContainer.add(rightCoords, BorderLayout.EAST); boardContainer.add(bottomCoords, BorderLayout.SOUTH); boardContainer.setBorder(BorderFactory.createCompoundBorder( BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5) )); return boardContainer;
    }

    private void linkListeners() { /* ... (Sin cambios respecto a la versión clic-clic) ... */
        if (controller == null) { System.err.println("¡Advertencia! El controlador es nulo en linkListeners."); return; }
        for (int r = 0; r < 8; r++) { for (int c = 0; c < 8; c++) { final int row = r; final int col = c; for(ActionListener al : squares[r][c].getActionListeners()) { squares[r][c].removeActionListener(al); } squares[r][c].addActionListener(e -> controller.onSquareClick(row, col)); } }
    }

    // --- Paneles Laterales y Controles (sin cambios lógicos) ---
    private JPanel createRightPanel() { /*...*/ JPanel rightPanel = new JPanel(new BorderLayout(10, 10)); rightPanel.setPreferredSize(new Dimension(320, 600)); rightPanel.setBackground(new Color(245, 245, 245)); JPanel movesPanel = new JPanel(new BorderLayout()); movesPanel.setBorder(BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), "📜 Lista de Movimientos", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), new Color(60, 60, 60) )); moveList.setFont(new Font("Consolas", Font.PLAIN, 13)); moveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); moveList.setBackground(Color.WHITE); moveList.setSelectionBackground(new Color(173, 216, 230)); moveList.setSelectionForeground(Color.BLACK); moveList.setCellRenderer(new MoveListCellRenderer()); moveList.addListSelectionListener(new ListSelectionListener() { @Override public void valueChanged(ListSelectionEvent e) { if (!e.getValueIsAdjusting() && controller != null) { controller.onMoveListSelected(); } } }); JScrollPane moveScrollPane = new JScrollPane(moveList); moveScrollPane.setPreferredSize(new Dimension(300, 250)); moveScrollPane.setBorder(BorderFactory.createLoweredBevelBorder()); movesPanel.add(moveScrollPane, BorderLayout.CENTER); rightPanel.add(movesPanel, BorderLayout.CENTER); JPanel capturesPanel = createCapturesPanel(); rightPanel.add(capturesPanel, BorderLayout.SOUTH); return rightPanel; }
    private JPanel createCapturesPanel() { /*...*/ JPanel capturesMain = new JPanel(new GridLayout(2, 1, 5, 5)); capturesMain.setBorder(BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), "⚔️ Piezas Capturadas", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), new Color(60, 60, 60) )); capturesMain.setBackground(new Color(245, 245, 245)); JPanel whitePanel = new JPanel(new BorderLayout()); whitePanel.setBorder(BorderFactory.createTitledBorder("♔ Blancas")); whitePanel.setBackground(Color.WHITE); capturesWhitePanel.setBackground(Color.WHITE); JScrollPane whiteScroll = new JScrollPane(capturesWhitePanel); whiteScroll.setPreferredSize(new Dimension(280, 60)); whiteScroll.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5)); whitePanel.add(whiteScroll, BorderLayout.CENTER); JPanel blackPanel = new JPanel(new BorderLayout()); blackPanel.setBorder(BorderFactory.createTitledBorder("♚ Negras")); blackPanel.setBackground(new Color(240, 240, 240)); capturesBlackPanel.setBackground(new Color(240, 240, 240)); JScrollPane blackScroll = new JScrollPane(capturesBlackPanel); blackScroll.setPreferredSize(new Dimension(280, 60)); blackScroll.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5)); blackPanel.add(blackScroll, BorderLayout.CENTER); capturesMain.add(whitePanel); capturesMain.add(blackPanel); return capturesMain; }
    private JPanel createControlPanel() { /*...*/ JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); controlPanel.setBackground(new Color(245, 245, 245)); GradientButton loadBtn = new GradientButton("📁 Cargar", new Color(52, 152, 219), new Color(41, 128, 185)); GradientButton saveBtn = new GradientButton("💾 Guardar", new Color(46, 204, 113), new Color(39, 174, 96)); GradientButton prevBtn = new GradientButton("⏪ Anterior", new Color(230, 126, 34), new Color(211, 84, 0)); GradientButton nextBtn = new GradientButton("⏩ Siguiente", new Color(230, 126, 34), new Color(211, 84, 0)); GradientButton resetBtn = new GradientButton("🔄 Reset", new Color(231, 76, 60), new Color(192, 57, 43)); GradientButton flipBtn = new GradientButton("🔄 Girar", new Color(155, 89, 182), new Color(142, 68, 173)); GradientButton exportBtn = new GradientButton("📤 Exportar PGN", new Color(52, 73, 94), new Color(44, 62, 80)); loadBtn.addActionListener(e -> { if(controller != null) controller.loadFile(); }); saveBtn.addActionListener(e -> { if(controller != null) controller.saveFile(); }); nextBtn.addActionListener(e -> { if(controller != null) controller.playNext(); }); prevBtn.addActionListener(e -> { if(controller != null) controller.playPrev(); }); resetBtn.addActionListener(e -> { if(controller != null) controller.resetGame(); }); flipBtn.addActionListener(e -> { if(controller != null) controller.toggleFlip(); }); exportBtn.addActionListener(e -> { if(controller != null) controller.exportPGN(); }); controlPanel.add(loadBtn); controlPanel.add(saveBtn); controlPanel.add(prevBtn); controlPanel.add(nextBtn); controlPanel.add(resetBtn); controlPanel.add(flipBtn); controlPanel.add(exportBtn); return controlPanel; }
    private void createStatusPanel() { /*...*/ statusLabel.setFont(new Font("Arial", Font.BOLD, 16)); statusLabel.setForeground(new Color(60, 60, 60)); statusLabel.setBorder(BorderFactory.createCompoundBorder( BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(8, 15, 8, 15) )); statusLabel.setBackground(Color.WHITE); statusLabel.setOpaque(true); }


    // --- Métodos de Actualización de UI ---

    public void updateCapturedPanels(List<Piece> allCaptured) { /*...*/ capturesWhitePanel.removeAll(); capturesBlackPanel.removeAll(); for (Piece p : allCaptured) { String key = (p.white ? "W" : "B") + p.type; Image img = pieceImages.get(key); if (img != null) { Image scaledImg = img.getScaledInstance(24, 24, Image.SCALE_SMOOTH); JLabel pieceLabel = new JLabel(new ImageIcon(scaledImg)); pieceLabel.setToolTipText(p.getName()); (p.white ? capturesWhitePanel : capturesBlackPanel).add(pieceLabel); } else { JLabel pieceLabel = new JLabel(p.toUnicode()); pieceLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20)); pieceLabel.setToolTipText(p.getName()); (p.white ? capturesWhitePanel : capturesBlackPanel).add(pieceLabel); } } capturesWhitePanel.revalidate(); capturesWhitePanel.repaint(); capturesBlackPanel.revalidate(); capturesBlackPanel.repaint(); }

    /**
     * Actualiza la vista del tablero usando iconos escalables. ¡CORREGIDO!
     */
    public void updateBoard() {
        System.out.println("DEBUG: updateBoard called."); // DEBUG
        // Actualizar coordenadas
        Container contentPane = getContentPane(); JPanel mainPanel = null; if (contentPane.getComponentCount() > 0 && contentPane.getComponent(0) instanceof JPanel) { JPanel topLevelPanel = (JPanel) contentPane.getComponent(0); if (topLevelPanel.getLayout() instanceof BorderLayout) { mainPanel = topLevelPanel; } } if (mainPanel != null) { BorderLayout layout = (BorderLayout) mainPanel.getLayout(); Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER); if (centerComp instanceof JPanel) { JPanel boardContainer = (JPanel) centerComp; if (boardContainer.getLayout() instanceof BorderLayout) { BorderLayout boardContainerLayout = (BorderLayout) boardContainer.getLayout(); Component topCoords = boardContainerLayout.getLayoutComponent(BorderLayout.NORTH); if (topCoords instanceof JPanel) updateCoordinates((JPanel) topCoords, false); Component bottomCoords = boardContainerLayout.getLayoutComponent(BorderLayout.SOUTH); if (bottomCoords instanceof JPanel) updateCoordinates((JPanel) bottomCoords, false); Component leftCoordsPanel = boardContainerLayout.getLayoutComponent(BorderLayout.WEST); if (leftCoordsPanel instanceof JPanel) updateCoordinates((JPanel) leftCoordsPanel, true); } } else { System.err.println("DEBUG: updateBoard - Center component is not a JPanel."); } } else { System.err.println("DEBUG: updateBoard - Could not find mainPanel."); }

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int dr = flipped ? 7 - r : r; int dc = flipped ? 7 - c : c;
                Piece p = model.get(dr, dc);

                // Obtener imagen (o null si no hay pieza)
                Image pieceImage = null;
                if (p != null) {
                    String key = (p.white ? "W" : "B") + p.type;
                    pieceImage = pieceImages.get(key);
                    if (pieceImage == null) {
                        System.err.println("WARN: Image not found in map for key: " + key);
                    }
                }
                // Actualizar el botón personalizado
                squares[r][c].setPieceImage(pieceImage);

                squares[r][c].setToolTipText(p != null ? (p.getName() + (p.moved ? " (moved)" : "")) : null);

                // Restaurar color base SI NO está resaltado por selección/válido
                // ¡CORREGIDO! Usar el getter
                Color currentHighlight = squares[r][c].getHighlightColor();
                boolean isSpecialHighlight = currentHighlight != null && (currentHighlight.equals(selectedColor) || currentHighlight.equals(validMoveColor));

                if (!isSpecialHighlight) {
                    // Restaurar color base (puede ser el normal o el de último movimiento)
                    boolean isLastMoveSquare = (lastFromR != -1) && ((dr == lastFromR && dc == lastFromC) || (dr == lastToR && dc == lastToC));
                    squares[r][c].setHighlight(isLastMoveSquare ? moveHighlight : null); // Llama a repaint internamente
                } else {
                    // Si está resaltado, el setHighlight ya fue llamado
                    // No necesitamos hacer nada extra aquí, solo asegurarnos que repinte
                    squares[r][c].repaint();
                }
            }
        }
    }

    private void updateCoordinates(JPanel panel, boolean isRowNumbers) { /*...*/ Component[] components = panel.getComponents(); for (int i = 0; i < components.length; i++) { if (components[i] instanceof JLabel) { JLabel label = (JLabel) components[i]; if (isRowNumbers) { int r = i; int rowNum = flipped ? r + 1 : 8 - r; label.setText(String.valueOf(rowNum)); } else { if (i > 0) { int c = i - 1; char coord = (char)('a' + (flipped ? 7 - c : c)); label.setText(String.valueOf(coord)); } } } } }

    /**
     * Resalta la casilla seleccionada y las de destino válidas.
     */
    public void highlightSelectedAndValidMoves(int viewR, int viewC, int modelR, int modelC) {
        clearHighlights(); // Limpiar resaltados anteriores primero

        // 1. Resaltar la casilla seleccionada
        if (viewR >= 0 && viewR < 8 && viewC >= 0 && viewC < 8) {
            squares[viewR][viewC].setHighlight(selectedColor);
        }

        // 2. Resaltar movimientos válidos
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (model.isValidMove(new Move(modelR, modelC, r, c))) {
                    int targetViewR = flipped ? 7 - r : r;
                    int targetViewC = flipped ? 7 - c : c;
                    if (targetViewR != viewR || targetViewC != viewC) {
                        squares[targetViewR][targetViewC].setHighlight(validMoveColor);
                    }
                }
            }
        }
    }


    /**
     * Limpia todos los resaltados (selección y válidos),
     * pero respeta el resaltado del último movimiento.
     */
    public void clearHighlights() {
        System.out.println("DEBUG: clearHighlights called."); // DEBUG
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int dr = flipped ? 7 - r : r;
                int dc = flipped ? 7 - c : c;
                boolean isLastMoveSquare = (lastFromR != -1) && ((dr == lastFromR && dc == lastFromC) || (dr == lastToR && dc == lastToC));

                // Poner resaltado de último movimiento o quitar cualquier otro resaltado
                squares[r][c].setHighlight(isLastMoveSquare ? moveHighlight : null);
            }
        }
    }

    // --- Métodos de Mostrar Mensajes y Diálogos (sin cambios) ---
    public void showError(String title, String message) { /*...*/ JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE); }
    public void showMessage(String title, String message) { /*...*/ JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE); }
    public String askForPromotion() { /*...*/ String[] options = {"♕ Dama", "♖ Torre", "♗ Alfil", "♘ Caballo"}; String[] values = {"Q", "R", "B", "N"}; int choice = JOptionPane.showOptionDialog(this, "🎉 ¡Promoción de peón!\nElige la pieza:", "Promoción", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]); return (choice >= 0) ? values[choice] : "Q"; }
    public File askForOpenFile() { /*...*/ JFileChooser fc = new JFileChooser(); fc.setFileSelectionMode(JFileChooser.FILES_ONLY); fc.setDialogTitle("Cargar partida"); if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { return fc.getSelectedFile(); } return null; }
    public File askForSaveFile() { /*...*/ JFileChooser fc = new JFileChooser(); fc.setDialogTitle("Guardar partida"); if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) { return fc.getSelectedFile(); } return null; }

    // --- Clases Internas para la Vista ---
    private class MoveListCellRenderer extends DefaultListCellRenderer { /*...*/ @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) { super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); if (!isSelected) { setBackground(index % 2 == 0 ? Color.WHITE : new Color(248, 248, 248)); } String moveText = value.toString(); int moveNumber = (index / 2) + 1; boolean isWhiteMove = index % 2 == 0; setText(isWhiteMove ? String.format("%d. %s", moveNumber, moveText) : String.format("   %s", moveText)); setIcon(isWhiteMove ? new ColorIcon(Color.WHITE, 12) : new ColorIcon(Color.BLACK, 12)); setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5)); return this; } }
    private class ColorIcon implements Icon { /*...*/ private Color color; private int size; public ColorIcon(Color color, int size) { this.color = color; this.size = size; } @Override public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(color); int verticalOffset = y + (c.getHeight() - size) / 2; g2.fillOval(x, verticalOffset, size, size); g2.setColor(Color.DARK_GRAY); g2.drawOval(x, verticalOffset, size, size); g2.dispose(); } @Override public int getIconWidth() { return size + 4; } @Override public int getIconHeight() { return size; } }

} // Fin de la clase ChessView