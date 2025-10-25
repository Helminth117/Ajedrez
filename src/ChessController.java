import javax.swing.*; // Necesario para JOptionPane
import javax.sound.sampled.*; // Necesario para el audio
import java.io.BufferedInputStream; // Para leer el archivo de audio
import java.io.InputStream;       // Para leer el archivo de audio
import java.io.File;              // Para FileChooser/PrintWriter
import java.io.PrintWriter;       // Para guardar
import java.util.ArrayList;       // Para List
import java.util.List;            // Para List
import java.awt.Color;            // Para el statusLabel
import java.io.IOException;       // Para excepciones de sonido/archivo
// Importar explícitamente las excepciones de sonido
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * CONTROLADOR: El cerebro que conecta la Vista y el Modelo.
 * ¡AHORA CON SONIDOS CORREGIDOS!
 */
public class ChessController {
    private final BoardModel model;
    private final ChessView view;

    // Estado de la aplicación
    private List<Move> loadedMoves = new ArrayList<>();
    private List<Piece> capturedHistory = new ArrayList<>();
    private List<Piece> allCaptured = new ArrayList<>();
    private int moveIndex = 0;
    private Move tempMove = null; // Para clic-clic
    private boolean whiteToMove = true;

    private boolean isListAdjusting = false; // Para la JList

    public ChessController(BoardModel model, ChessView view) {
        this.model = model;
        this.view = view;
    }

    /**
     * Reproduce un archivo de sonido .wav desde la carpeta 'sounds'.
     * ¡CORREGIDO!
     */
    private void playSound(String soundFileName) {
        try {
            String resourcePath = "/sounds/" + soundFileName;
            InputStream audioSrc = getClass().getResourceAsStream(resourcePath);

            if (audioSrc == null) {
                System.err.println("Error: No se pudo encontrar el archivo de sonido: " + resourcePath);
                return;
            }

            // Envolver en BufferedInputStream
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            // Declarar audioIn aquí para que sea accesible en el listener
            final AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedIn);

            Clip clip = AudioSystem.getClip();

            // Añadir listener para cerrar recursos después de reproducir
            clip.addLineListener(event -> {
                if (LineEvent.Type.STOP.equals(event.getType())) {
                    Clip c = (Clip) event.getSource();
                    c.close(); // Cierra el clip
                    try {
                        // Cerrar el stream original que abrimos
                        audioIn.close();
                        System.out.println("DEBUG: AudioInputStream closed for " + soundFileName); // DEBUG
                    } catch (IOException ex) {
                        System.err.println("Error closing audio stream: " + ex.getMessage());
                    }
                }
            });

            clip.open(audioIn);
            clip.start(); // Reproducir

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error al reproducir sonido '" + soundFileName + "': " + e.getMessage());
        } catch (Exception e) { // Captura genérica
            System.err.println("Error inesperado al reproducir sonido '" + soundFileName + "': " + e.getMessage());
        }
    }


    // --- Métodos de Clic-Clic ---
    public void onSquareClick(int r, int c) { /* ... (sin cambios) ... */
        int boardR = view.flipped ? 7 - r : r; int boardC = view.flipped ? 7 - c : c;
        if (tempMove == null) { Piece piece = model.get(boardR, boardC); if (piece != null && piece.white == whiteToMove) { tempMove = new Move(boardR, boardC, -1, -1); view.highlightSelectedAndValidMoves(r, c, boardR, boardC); System.out.println("DEBUG: Piece selected at model " + boardR + "," + boardC); } else { System.out.println("DEBUG: Clicked on empty or opponent piece, ignoring."); view.clearHighlights(); } } else { Move potentialMove = new Move(tempMove.fromRow, tempMove.fromCol, boardR, boardC); Piece clickedPiece = model.get(boardR, boardC); System.out.println("DEBUG: Second click on view " + r + "," + c + " (model " + boardR + "," + boardC + ")"); view.clearHighlights(); if (clickedPiece != null && clickedPiece.white == whiteToMove) { System.out.println("DEBUG: Clicked on another friendly piece, changing selection."); tempMove = new Move(boardR, boardC, -1, -1); view.highlightSelectedAndValidMoves(r, c, boardR, boardC); return; } if (model.isValidMove(potentialMove)) { System.out.println("DEBUG: Move to " + boardR + "," + boardC + " is valid. Executing..."); executeMove(potentialMove); tempMove = null; } else { System.out.println("DEBUG: Move to " + boardR + "," + boardC + " is invalid."); view.showError("Movimiento Inválido", "No puedes mover esa pieza ahí."); tempMove = null; } }
    }

    /**
     * Lógica interna para ejecutar un movimiento. ¡CON SONIDOS!
     */
    private void executeMove(Move m) { /* ... (sin cambios respecto a la versión anterior, ya incluye playSound) ... */
        Piece piece = model.get(m.fromRow, m.fromCol); if (piece == null) return;
        if (piece.type.equals("P") && (m.toRow == 0 || m.toRow == 7)) { m.isPromotion = true; m.promotionType = view.askForPromotion(); }
        boolean isEnPassantMove = model.isPotentialEnPassant(piece, m); if(isEnPassantMove) m.isEnPassant = true;
        Move namedMove = new Move(m.fromRow, m.fromCol, m.toRow, m.toCol, piece.getName()); namedMove.isPromotion = m.isPromotion; namedMove.promotionType = m.promotionType; namedMove.isEnPassant = m.isEnPassant; namedMove.isCastling = m.isCastling;
        Piece capturedAt = model.get(namedMove.toRow, namedMove.toCol); boolean isCapture = (capturedAt != null) || namedMove.isEnPassant;
        Piece actuallyCaptured = capturedAt; if (namedMove.isEnPassant) { int capRow = piece.white ? namedMove.toRow + 1 : namedMove.toRow - 1; actuallyCaptured = model.get(capRow, namedMove.toCol); }
        if (isCapture && actuallyCaptured != null) { playSound("capture.wav"); } else if (!namedMove.isCastling){ playSound("move.wav"); }
        if (actuallyCaptured != null) { allCaptured.add(actuallyCaptured); if(capturedHistory.size() == moveIndex) { capturedHistory.add(actuallyCaptured); } else { capturedHistory.set(moveIndex, actuallyCaptured); } } else if (!namedMove.isCastling) { if(capturedHistory.size() == moveIndex) { capturedHistory.add(null); } else { capturedHistory.set(moveIndex, null); } }
        model.applyMove(namedMove);
        view.lastFromR = namedMove.fromRow; view.lastFromC = namedMove.fromCol; view.lastToR = namedMove.toRow; view.lastToC = namedMove.toCol; String moveText = formatMoveForList(namedMove, actuallyCaptured); view.moveListModel.addElement(moveText);
        isListAdjusting = true; view.moveList.setSelectedIndex(view.moveListModel.getSize() - 1); view.moveList.ensureIndexIsVisible(view.moveListModel.getSize() - 1); isListAdjusting = false;
        if (moveIndex < loadedMoves.size()) { loadedMoves = loadedMoves.subList(0, moveIndex); if (capturedHistory.size() > moveIndex) { capturedHistory = capturedHistory.subList(0, moveIndex); } }
        loadedMoves.add(namedMove);
        moveIndex++; whiteToMove = !whiteToMove;
        view.clearHighlights(); view.updateBoard(); checkGameStatus(); view.updateCapturedPanels(allCaptured);
    }


    private void checkGameStatus() { /* ... (sin cambios, puede añadir playSound("check.wav")) ... */
        String statusText = "Turno: " + (whiteToMove ? "Blancas" : "Negras"); Color statusColor = new Color(60, 60, 60);
        boolean inCheck = model.isInCheck(whiteToMove); List<Move> legalMoves = model.generateAllLegalMoves(whiteToMove);
        boolean isMate = inCheck && legalMoves.isEmpty(); boolean isStalemate = !inCheck && legalMoves.isEmpty();
        if (isMate) { statusText = "🏆 ¡Jaque mate! Ganador: " + (whiteToMove ? "♚ Negras" : "♔ Blancas"); statusColor = Color.RED; view.showMessage("¡Fin del juego!", "¡Jaque mate!\n\nGanador: " + (whiteToMove ? "Negras" : "Blancas"));
        } else if (isStalemate) { statusText = "⚖️ ¡Tablas por Ahogado!"; statusColor = new Color(255, 140, 0); view.showMessage("¡Fin del juego!", "¡Tablas por Ahogado!\n\n(El jugador no está en jaque, pero no tiene movimientos legales).");
        } else if (inCheck) { /* playSound("check.wav"); */ statusText = "¡Jaque a " + (whiteToMove ? "♔ Blancas" : "♚ Negras") + "!"; statusColor = Color.RED;
        } else if (model.isFiftyMoveRule()) { statusText = "Tablas por regla de 50-moves"; statusColor = new Color(255, 140, 0);
        } else if (model.isThreefoldRepetition()) { statusText = "Tablas por repetición triple"; statusColor = new Color(255, 140, 0);
        } else if (model.isInsufficientMaterial()) { statusText = "Tablas por material insuficiente"; statusColor = new Color(255, 140, 0); }
        else { statusText = "Turno: " + (whiteToMove ? "Blancas" : "Negras"); statusColor = new Color(60, 60, 60); }
        view.statusLabel.setText(statusText); view.statusLabel.setForeground(statusColor);
    }

    private String formatMoveForList(Move move, Piece captured) { /* ... (sin cambios) ... */
        StringBuilder sb = new StringBuilder(); if (move.isCastling) { sb.append(move.toCol == 6 ? "O-O" : "O-O-O"); } else { if (move.pieceName != null && move.pieceName.length() >= 2) { char pieceType = move.pieceName.charAt(1); if (pieceType != 'P') sb.append(pieceType); } if (captured != null || move.isEnPassant) { if (sb.length() == 0 && move.pieceName != null && move.pieceName.charAt(1) == 'P') { sb.append((char)('a' + move.fromCol)); } sb.append("x"); } sb.append((char)('a' + move.toCol)); sb.append(8 - move.toRow); if (move.isPromotion) sb.append("=").append(move.promotionType); } return sb.toString();
    }
    public void loadFile() { /* ... (sin cambios) ... */
        File f = view.askForOpenFile(); if (f == null) return; try { resetGame(); loadedMoves = SimpleMoveParser.parseFile(f); view.statusLabel.setText("Archivo cargado: " + loadedMoves.size() + " movimientos"); view.statusLabel.setForeground(new Color(39, 174, 96)); } catch (Exception ex) { view.showError("Error", "Error cargando archivo:\n" + ex.getMessage()); }
    }
    public void saveFile() { /* ... (sin cambios) ... */
        File f = view.askForSaveFile(); if (f == null) return; try (PrintWriter pw = new PrintWriter(f)) { for (Move m : loadedMoves) { pw.print(m + " "); } view.showMessage("Éxito", "Partida guardada exitosamente"); } catch (Exception ex) { view.showError("Error", "Error guardando archivo:\n" + ex.getMessage()); }
    }
    public void playNext() { /* ... (sin cambios, ya reproduce sonido) ... */
        if (moveIndex < loadedMoves.size()) { Move m = loadedMoves.get(moveIndex); Piece piece = model.get(m.fromRow, m.fromCol); if (piece == null || piece.white != whiteToMove) { view.showError("Error de Carga", "Movimiento #" + (moveIndex + 1) + " (" + m + ") inválido:\n Pieza origen no encontrada o turno incorrecto."); loadedMoves = loadedMoves.subList(0, moveIndex); return; } Piece captured = model.get(m.toRow, m.toCol); boolean isEnPassant = model.isPotentialEnPassant(piece, m); if(isEnPassant) m.isEnPassant = true; Piece actuallyCaptured = captured; if(m.isEnPassant) { int capRow = piece.white ? m.toRow + 1 : m.toRow - 1; actuallyCaptured = model.get(capRow, m.toCol); } if(actuallyCaptured != null || m.isEnPassant) { playSound("capture.wav"); } else if (!m.isCastling) { playSound("move.wav"); } if (actuallyCaptured != null) allCaptured.add(actuallyCaptured); model.applyMove(m); if (capturedHistory.size() == moveIndex) { capturedHistory.add(actuallyCaptured); } else { capturedHistory.set(moveIndex, actuallyCaptured); } if (view.moveListModel.getSize() <= moveIndex) { view.moveListModel.addElement(formatMoveForList(m, actuallyCaptured)); } view.lastFromR = m.fromRow; view.lastFromC = m.fromCol; view.lastToR = m.toRow; view.lastToC = m.toCol; moveIndex++; whiteToMove = !whiteToMove; view.updateBoard(); checkGameStatus(); view.updateCapturedPanels(allCaptured); if (!isListAdjusting && moveIndex > 0) { isListAdjusting = true; view.moveList.setSelectedIndex(moveIndex - 1); view.moveList.ensureIndexIsVisible(moveIndex - 1); isListAdjusting = false; } } else { view.statusLabel.setText("Fin de la partida"); view.statusLabel.setForeground(new Color(255, 140, 0)); }
    }
    public void playPrev() { /* ... (sin cambios) ... */
        if (moveIndex > 0) { moveIndex--; Move m = loadedMoves.get(moveIndex); Piece captured = (capturedHistory.size() > moveIndex) ? capturedHistory.get(moveIndex) : null; if (captured != null) { boolean removed = allCaptured.remove(captured); if(!removed) { System.err.println("WARN: Could not find captured piece in allCaptured during undo: " + captured.getName()); } } model.undoMove(m, captured); whiteToMove = !whiteToMove; if (moveIndex > 0) { Move prevMove = loadedMoves.get(moveIndex - 1); view.lastFromR = prevMove.fromRow; view.lastFromC = prevMove.fromCol; view.lastToR = prevMove.toRow; view.lastToC = prevMove.toCol; } else { view.lastFromR = view.lastFromC = view.lastToR = view.lastToC = -1; } view.updateBoard(); checkGameStatus(); view.updateCapturedPanels(allCaptured); if (!isListAdjusting) { isListAdjusting = true; if (moveIndex > 0) { view.moveList.setSelectedIndex(moveIndex - 1); view.moveList.ensureIndexIsVisible(moveIndex - 1); } else { view.moveList.clearSelection(); } isListAdjusting = false; } }
    }
    public void onMoveListSelected() { /* ... (sin cambios) ... */
        int selectedIndex = view.moveList.getSelectedIndex(); if (selectedIndex == -1 || isListAdjusting) return; int targetMoveCount = selectedIndex + 1; if (targetMoveCount == moveIndex) return; this.isListAdjusting = true; while (moveIndex < targetMoveCount) playNext(); while (moveIndex > targetMoveCount) playPrev(); view.moveList.setSelectedIndex(selectedIndex); view.moveList.ensureIndexIsVisible(selectedIndex); this.isListAdjusting = false;
    }
    public void resetGame() { /* ... (sin cambios) ... */
        model.reset(); whiteToMove = true; moveIndex = 0; loadedMoves.clear(); capturedHistory.clear(); allCaptured.clear(); view.lastFromR = view.lastFromC = view.lastToR = view.lastToC = -1; isListAdjusting = true; view.moveListModel.clear(); isListAdjusting = false; tempMove = null; view.clearHighlights(); view.updateBoard(); view.statusLabel.setText("🔄 Juego reiniciado"); view.statusLabel.setForeground(new Color(60, 60, 60)); view.updateCapturedPanels(allCaptured);
    }
    public void toggleFlip() { /* ... (sin cambios) ... */
        view.flipped = !view.flipped; tempMove = null; view.clearHighlights(); view.updateBoard(); view.statusLabel.setText("🔄 Tablero " + (view.flipped ? "girado" : "normal"));
    }
    public void exportPGN() { /* ... (sin cambios) ... */
        StringBuilder pgn = new StringBuilder(); pgn.append("[Event \"Chess Master Pro Game\"]\n"); pgn.append("[Site \"Local\"]\n"); pgn.append("[Date \"").append(new java.util.Date()).append("\"]\n"); pgn.append("[White \"Jugador 1\"]\n"); pgn.append("[Black \"Jugador 2\"]\n"); pgn.append("[Result \"*\"]\n\n"); BoardModel tempModel = new BoardModel(); for (int i = 0; i < loadedMoves.size(); i++) { Move currentMove = loadedMoves.get(i); Piece capturedPieceForSAN = tempModel.get(currentMove.toRow, currentMove.toCol); if (currentMove.isEnPassant) { Piece movingPawn = tempModel.get(currentMove.fromRow, currentMove.fromCol); if(movingPawn != null) { int capRow = movingPawn.white ? currentMove.toRow + 1 : currentMove.toRow - 1; capturedPieceForSAN = tempModel.get(capRow, currentMove.toCol); } } if (i % 2 == 0) pgn.append((i / 2 + 1)).append(". "); pgn.append(moveToSAN(currentMove, capturedPieceForSAN)).append(" "); if (i % 2 == 1) pgn.append("\n"); tempModel.applyMove(currentMove); } File f = view.askForSaveFile(); if (f == null) return; try (PrintWriter pw = new PrintWriter(f)) { pw.print(pgn.toString()); view.showMessage("Éxito", "✅ PGN exportado exitosamente"); } catch (Exception ex) { view.showError("Error", "❌ Error exportando PGN:\n" + ex.getMessage()); }
    }

    private String moveToSAN(Move m, Piece captured) { /* ... (sin cambios) ... */
        if (m.isCastling) return m.toCol == 6 ? "O-O" : "O-O-O"; StringBuilder s = new StringBuilder(); if (m.pieceName != null && m.pieceName.length() >= 2) { char pieceChar = m.pieceName.charAt(1); if (pieceChar != 'P') s.append(pieceChar); } String dest = "" + (char)('a' + m.toCol) + (8 - m.toRow); boolean wasCapture = m.isEnPassant || captured != null; if (wasCapture) { if (m.pieceName != null && m.pieceName.charAt(1) == 'P') { s.append((char)('a' + m.fromCol)); } s.append("x"); } s.append(dest); if (m.isPromotion) s.append("=").append(m.promotionType); return s.toString();
    }

} // Fin de ChessController