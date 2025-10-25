import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * MODELO: El cerebro del juego. Gestiona el estado del tablero y la validación de movimientos.
 */
public class BoardModel {
    Piece[][] board = new Piece[8][8];
    Move lastMove = null;
    int halfmoveClock = 0;
    Map<String,Integer> repetitionMap = new HashMap<>();

    public BoardModel() { reset(); }

    public void reset() {
        board = new Piece[8][8];
        lastMove = null;
        halfmoveClock = 0;
        repetitionMap.clear();

        // Colocar piezas negras
        board[0][0]=new Piece("R",false, 1); board[0][7]=new Piece("R",false, 2);
        board[0][1]=new Piece("N",false, 1); board[0][6]=new Piece("N",false, 2);
        board[0][2]=new Piece("B",false, 1); board[0][5]=new Piece("B",false, 2);
        board[0][3]=new Piece("Q",false, 1); board[0][4]=new Piece("K",false, 1);
        for (int c=0;c<8;c++) board[1][c]=new Piece("P",false, c+1);

        // Colocar piezas blancas
        board[7][0]=new Piece("R",true, 1); board[7][7]=new Piece("R",true, 2);
        board[7][1]=new Piece("N",true, 1); board[7][6]=new Piece("N",true, 2);
        board[7][2]=new Piece("B",true, 1); board[7][5]=new Piece("B",true, 2);
        board[7][3]=new Piece("Q",true, 1); board[7][4]=new Piece("K",true, 1);
        for (int c=0;c<8;c++) board[6][c]=new Piece("P",true, c+1);

        // marcar moved = false
        for (int r=0;r<8;r++) for (int c=0;c<8;c++) if (board[r][c]!=null) board[r][c].moved=false;

        // añadir posición inicial a mapa de repetición
        String key = boardKey(true);
        repetitionMap.put(key,1);
    }

    public Piece get(int r,int c){
        if (r < 0 || r >= 8 || c < 0 || c >= 8) return null;
        return board[r][c];
    }

    public void applyMove(Move m) {
        Piece mover = board[m.fromRow][m.fromCol];
        Piece captured = board[m.toRow][m.toCol];

        // En passant capture
        if (m.isEnPassant) {
            int capRow = mover.white ? m.toRow+1 : m.toRow-1;
            captured = board[capRow][m.toCol];
            board[capRow][m.toCol] = null;
        }

        // Castling (mover torre también)
        if (m.isCastling) {
            if (m.toCol == 6) { // corto
                board[m.toRow][m.toCol] = mover;
                board[m.fromRow][m.fromCol] = null;
                Piece rook = board[m.toRow][7];
                board[m.toRow][5] = rook;
                board[m.toRow][7] = null;
                mover.moved = true;
                if (rook!=null) rook.moved=true;
                recordMoveForRepetition(m, captured);
                return;
            } else if (m.toCol == 2) { // largo
                board[m.toRow][m.toCol] = mover;
                board[m.fromRow][m.fromCol] = null;
                Piece rook = board[m.toRow][0];
                board[m.toRow][3] = rook;
                board[m.toRow][0] = null;
                mover.moved = true;
                if (rook!=null) rook.moved=true;
                recordMoveForRepetition(m, captured);
                return;
            }
        }

        board[m.toRow][m.toCol] = mover;
        board[m.fromRow][m.fromCol] = null;

        // Promoción automática
        if (m.isPromotion && mover!=null) {
            mover.type = m.promotionType;
        }

        if (mover!=null) mover.moved = true;

        // actualizar halfmoveClock
        if (captured!=null || (mover != null && mover.type.equals("P"))) halfmoveClock = 0; else halfmoveClock++;

        recordMoveForRepetition(m, captured);
        lastMove = m;
    }

    private void recordMoveForRepetition(Move m, Piece captured) {
        String key = boardKey(!movedSide());
        repetitionMap.put(key, repetitionMap.getOrDefault(key,0)+1);
    }

    private boolean movedSide(){
        if (lastMove==null) return false;
        Piece p = get(lastMove.toRow,lastMove.toCol);
        if (p == null) return false; // Debería ser raro, pero por si acaso
        return !p.white;
    }

    private String boardKey(boolean whiteToMove) {
        StringBuilder sb=new StringBuilder();
        for (int r=0;r<8;r++) for (int c=0;c<8;c++) {
            Piece p = get(r,c);
            if (p==null) sb.append("."); else sb.append(p.white?Character.toLowerCase(p.type.charAt(0)):Character.toUpperCase(p.type.charAt(0)));
        }
        sb.append(whiteToMove?" w":" b");
        return sb.toString();
    }

    public void undoMove(Move m, Piece captured) {
        Piece mover = board[m.toRow][m.toCol];
        if (m.isEnPassant) {
            board[m.fromRow][m.fromCol] = mover;
            board[m.toRow][m.toCol] = null;
            int capRow = (mover != null && mover.white) ? m.toRow+1 : m.toRow-1;
            board[capRow][m.toCol] = captured;
            if (mover!=null) mover.moved = false;
            lastMove = null;
            return;
        }

        if (m.isCastling) {
            board[m.fromRow][m.fromCol] = mover;
            board[m.toRow][m.toCol] = null;
            if (m.toCol == 6) {
                Piece rook = board[m.toRow][5];
                board[m.toRow][7] = rook;
                board[m.toRow][5] = null;
                if (rook!=null) rook.moved = false;
            } else if (m.toCol == 2) {
                Piece rook = board[m.toRow][3];
                board[m.toRow][0] = rook;
                board[m.toRow][3] = null;
                if (rook!=null) rook.moved = false;
            }
            if (mover!=null) mover.moved = false;
            lastMove = null;
            return;
        }

        board[m.fromRow][m.fromCol] = mover;
        board[m.toRow][m.toCol] = captured;
        if (mover!=null) mover.moved = false;
        lastMove = null;
    }

    // Validación de movimientos (incluye comprobación de no dejar a su rey en jaque)
    public boolean isValidMove(Move move) {
        Piece piece = get(move.fromRow, move.fromCol);
        if (piece == null) return false;

        Piece target = get(move.toRow, move.toCol);
        if (target != null && target.white == piece.white) return false;

        int deltaRow = move.toRow - move.fromRow;
        int deltaCol = move.toCol - move.fromCol;

        boolean basicOk = false;

        switch (piece.type) {
            case "P": basicOk = isValidPawnMove(piece, deltaRow, deltaCol, target != null, move.fromRow, move); break;
            case "R": basicOk = isValidRookMove(deltaRow, deltaCol, move); break;
            case "N": basicOk = isValidKnightMove(deltaRow, deltaCol); break;
            case "B": basicOk = isValidBishopMove(deltaRow, deltaCol, move); break;
            case "Q": basicOk = isValidQueenMove(deltaRow, deltaCol, move); break;
            case "K": basicOk = isValidKingMove(deltaRow, deltaCol, move); break;
        }
        if (!basicOk) return false;

        // Simular y comprobar no dejar en jaque
        Piece captured = get(move.toRow, move.toCol);
        Piece enPassantCaptured = null;
        if (isPotentialEnPassant(piece, move)) {
            move.isEnPassant = true;
            int capRow = piece.white ? move.toRow+1 : move.toRow-1;
            enPassantCaptured = get(capRow, move.toCol);
            board[capRow][move.toCol] = null;
        }

        Piece mover = board[move.fromRow][move.fromCol];
        board[move.toRow][move.toCol] = mover;
        board[move.fromRow][move.fromCol] = null;

        boolean leavesKingSafe = !isInCheck(mover.white);

        // revertir
        board[move.fromRow][move.fromCol] = mover;
        board[move.toRow][move.toCol] = captured;
        if (enPassantCaptured!=null) {
            int capRow = piece.white ? move.toRow+1 : move.toRow-1;
            board[capRow][move.toCol] = enPassantCaptured;
            move.isEnPassant = false;
        }

        return leavesKingSafe;
    }

    public boolean isPotentialEnPassant(Piece pawn, Move move) {
        if (!pawn.type.equals("P")) return false;
        if (lastMove == null) return false;

        Piece lastMover = get(lastMove.toRow, lastMove.toCol);
        if (lastMover == null || !lastMover.type.equals("P")) return false;

        int fromR = lastMove.fromRow, toR = lastMove.toRow;
        if (Math.abs(fromR - toR) == 2) {
            if (lastMove.toCol == move.toCol && Math.abs(lastMove.toCol - move.fromCol) == 1) {
                if (pawn.white && move.toRow == lastMove.toRow+1 && move.fromRow == lastMove.toRow) return true;
                if (!pawn.white && move.toRow == lastMove.toRow-1 && move.fromRow == lastMove.toRow) return true;
            }
        }
        return false;
    }

    private boolean isValidPawnMove(Piece pawn, int deltaRow, int deltaCol, boolean isCapture, int fromRow, Move move) {
        int direction = pawn.white ? -1 : 1;
        if (deltaCol == 0 && !isCapture) {
            if (deltaRow == direction && get(move.toRow, move.toCol) == null) return true;
            if (deltaRow == 2 * direction && (pawn.white ? fromRow == 6 : fromRow == 1)) {
                int midRow = fromRow + direction;
                if (get(midRow, move.fromCol) == null && get(move.toRow, move.toCol) == null) return true;
            }
            return false;
        } else if (Math.abs(deltaCol) == 1 && deltaRow == direction && (isCapture || isPotentialEnPassant(pawn, move))) {
            if (!isCapture && isPotentialEnPassant(pawn, move)) {
                move.isEnPassant = true;
                return true;
            }
            return isCapture;
        }
        return false;
    }

    private boolean isValidRookMove(int deltaRow, int deltaCol, Move move) {
        if (deltaRow != 0 && deltaCol != 0) return false;
        return isPathClear(move);
    }

    private boolean isValidKnightMove(int deltaRow, int deltaCol) {
        return (Math.abs(deltaRow) == 2 && Math.abs(deltaCol) == 1) ||
                (Math.abs(deltaRow) == 1 && Math.abs(deltaCol) == 2);
    }

    private boolean isValidBishopMove(int deltaRow, int deltaCol, Move move) {
        if (Math.abs(deltaRow) != Math.abs(deltaCol)) return false;
        return isPathClear(move);
    }

    private boolean isValidQueenMove(int deltaRow, int deltaCol, Move move) {
        return isValidRookMove(deltaRow, deltaCol, move) || isValidBishopMove(deltaRow, deltaCol, move);
    }

    private boolean isValidKingMove(int deltaRow, int deltaCol, Move move) {
        if (Math.abs(deltaRow) <= 1 && Math.abs(deltaCol) <= 1) return true;

        Piece king = get(move.fromRow, move.fromCol);
        if (king==null || !king.type.equals("K")) return false;
        if (king.moved) return false;
        // corto
        if (deltaRow == 0 && deltaCol == 2) {
            Piece rook = get(move.fromRow, 7);
            if (rook==null || !rook.type.equals("R") || rook.moved) return false;
            if (get(move.fromRow,5)!=null || get(move.fromRow,6)!=null) return false;
            if (isSquareAttacked(move.fromRow, move.fromCol, !king.white)) return false;

            // Simular movimiento a f1/f8
            board[move.fromRow][move.fromCol] = null;
            board[move.fromRow][5] = king;
            boolean passSafe = !isSquareAttacked(move.fromRow, 5, !king.white);
            // Revertir
            board[move.fromRow][move.fromCol] = king;
            board[move.fromRow][5] = null;
            if (!passSafe) return false;

            // Simular movimiento a g1/g8
            board[move.fromRow][move.fromCol] = null;
            board[move.fromRow][6] = king;
            boolean landSafe = !isSquareAttacked(move.fromRow, 6, !king.white);
            // Revertir
            board[move.fromRow][move.fromCol] = king;
            board[move.fromRow][6] = null;
            if (!landSafe) return false;

            move.isCastling = true;
            return true;
        }
        // largo
        if (deltaRow == 0 && deltaCol == -2) {
            Piece rook = get(move.fromRow, 0);
            if (rook==null || !rook.type.equals("R") || rook.moved) return false;
            if (get(move.fromRow,1)!=null || get(move.fromRow,2)!=null || get(move.fromRow,3)!=null) return false;
            if (isSquareAttacked(move.fromRow, move.fromCol, !king.white)) return false;

            // Simular movimiento a d1/d8
            board[move.fromRow][move.fromCol] = null;
            board[move.fromRow][3] = king;
            boolean passSafe = !isSquareAttacked(move.fromRow, 3, !king.white);
            // Revertir
            board[move.fromRow][move.fromCol] = king;
            board[move.fromRow][3] = null;
            if (!passSafe) return false;

            // Simular movimiento a c1/c8
            board[move.fromRow][move.fromCol] = null;
            board[move.fromRow][2] = king;
            boolean landSafe = !isSquareAttacked(move.fromRow, 2, !king.white);
            // Revertir
            board[move.fromRow][move.fromCol] = king;
            board[move.fromRow][2] = null;
            if (!landSafe) return false;

            move.isCastling = true;
            return true;
        }
        return false;
    }

    private boolean isPathClear(Move move) {
        int deltaRow = move.toRow - move.fromRow;
        int deltaCol = move.toCol - move.fromCol;
        int stepRow = deltaRow == 0 ? 0 : deltaRow / Math.abs(deltaRow);
        int stepCol = deltaCol == 0 ? 0 : deltaCol / Math.abs(deltaCol);
        int currentRow = move.fromRow + stepRow;
        int currentCol = move.fromCol + stepCol;
        while (currentRow != move.toRow || currentCol != move.toCol) {
            if (get(currentRow, currentCol) != null) return false;
            currentRow += stepRow;
            currentCol += stepCol;
        }
        return true;
    }

    public boolean isSquareAttacked(int row, int col, boolean byWhite) {
        for (int r=0;r<8;r++) for (int c=0;c<8;c++) {
            Piece p = get(r,c);
            if (p==null) continue;
            if (p.white != byWhite) continue;

            Move test = new Move(r,c,row,col);

            // Lógica de ataque de peón (diferente de movimiento)
            if (p.type.equals("P")) {
                if (Math.abs(col - c)==1 && ((p.white && row==r-1) || (!p.white && row==r+1))) return true;
            }
            // Para otras piezas, simular un movimiento básico
            else if (p.type.equals("N")) {
                if (isValidKnightMove(row-r,col-c)) return true;
            } else if (p.type.equals("B")) {
                if (Math.abs(row-r)==Math.abs(col-c) && isPathClear(test)) return true;
            } else if (p.type.equals("R")) {
                if ((row==r || col==c) && isPathClear(test)) return true;
            } else if (p.type.equals("Q")) {
                if ((row==r || col==c || Math.abs(row-r)==Math.abs(col-c)) && isPathClear(test)) return true;
            } else if (p.type.equals("K")) {
                if (Math.max(Math.abs(row-r),Math.abs(col-c))==1) return true;
            }
        }
        return false;
    }

    public boolean isInCheck(boolean white) {
        for (int r=0;r<8;r++) for (int c=0;c<8;c++) {
            Piece p = get(r,c);
            if (p!=null && p.type.equals("K") && p.white==white) {
                return isSquareAttacked(r,c,!white);
            }
        }
        return false;
    }

    public List<Move> generateAllLegalMoves(boolean white) {
        List<Move> moves = new ArrayList<>();
        for (int r=0;r<8;r++) for (int c=0;c<8;c++) {
            Piece p = get(r,c);
            if (p==null || p.white!=white) continue;
            for (int tr=0; tr<8; tr++) for (int tc=0; tc<8; tc++) {
                Move m = new Move(r,c,tr,tc);
                if (isValidMove(m)) moves.add(m);
            }
        }
        return moves;
    }

    public boolean isCheckmate(boolean white) {
        if (!isInCheck(white)) return false;
        List<Move> moves = generateAllLegalMoves(white);
        return moves.isEmpty();
    }

    public boolean isFiftyMoveRule() { return halfmoveClock >= 100; }

    public boolean isThreefoldRepetition() {
        for (Integer v: repetitionMap.values()) if (v>=3) return true;
        return false;
    }

    public boolean isInsufficientMaterial() {
        List<Piece> pieces = new ArrayList<>();
        for (int r=0;r<8;r++) for (int c=0;c<8;c++) if (get(r,c)!=null) pieces.add(get(r,c));
        if (pieces.size()==2) return true;
        if (pieces.size()==3) {
            int minor=0; for (Piece p:pieces) if (p.type.equals("B")||p.type.equals("N")) minor++;
            if (minor==1) return true;
        }
        // Rey y Alfil vs Rey y Alfil (mismo color de casilla)
        if (pieces.size() == 4) {
            int bishops = 0;
            List<Integer> bishopSquares = new ArrayList<>();
            for (Piece p : pieces) {
                if (p.type.equals("B")) {
                    bishops++;
                    for (int r = 0; r < 8; r++) {
                        for (int c = 0; c < 8; c++) {
                            if (get(r, c) == p) {
                                bishopSquares.add((r + c) % 2); // 0 para oscuro, 1 para claro
                            }
                        }
                    }
                } else if (!p.type.equals("K")) {
                    return false; // Hay otra pieza
                }
            }
            if (bishops == 2 && Objects.equals(bishopSquares.get(0), bishopSquares.get(1))) {
                return true;
            }
        }
        return false;
    }
}