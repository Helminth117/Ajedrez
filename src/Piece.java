/**
 * MODELO: Representa una sola pieza de ajedrez.
 */
public class Piece {
    String type;  // P, N, B, R, Q, K
    boolean white;
    int pieceNumber; // Para identificar P1, P2, etc.
    boolean moved = false; // útil para enroque

    public Piece(String type, boolean white, int pieceNumber) {
        this.type = type;
        this.white = white;
        this.pieceNumber = pieceNumber;
    }

    public String toUnicode() {
        switch (type) {
            case "K": return white ? "♔" : "♚";
            case "Q": return white ? "♕" : "♛";
            case "R": return white ? "♖" : "♜";
            case "B": return white ? "♗" : "♝";
            case "N": return white ? "♘" : "♞";
            case "P": return white ? "♙" : "♟";
        }
        return "";
    }

    public String getName() {
        String color = white ? "B" : "N";
        return color + type + pieceNumber;
    }
}