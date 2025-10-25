/**
 * MODELO: Representa un movimiento (desde-hacia).
 */
public class Move {
    int fromRow, fromCol, toRow, toCol;
    String pieceName;
    boolean isPromotion = false;
    String promotionType = "Q"; // por defecto a Dama
    boolean isEnPassant = false;
    boolean isCastling = false; // true si es enroque

    public Move(int fr, int fc, int tr, int tc) {
        fromRow=fr; fromCol=fc; toRow=tr; toCol=tc;
    }

    public Move(int fr, int fc, int tr, int tc, String pieceName) {
        this(fr,fc,tr,tc);
        this.pieceName = pieceName;
    }

    public String longAlgebraic() {
        String basic = ""+ (char)('a'+fromCol)+(8-fromRow)+(char)('a'+toCol)+(8-toRow);
        String s = pieceName != null ? pieceName+":"+basic : basic;
        if (isPromotion) s += "=" + promotionType;
        if (isEnPassant) s += " e.p.";
        if (isCastling) s = pieceName != null ? pieceName+":O-O" : "O-O";
        return s;
    }

    public String toString() { return longAlgebraic(); }
}
