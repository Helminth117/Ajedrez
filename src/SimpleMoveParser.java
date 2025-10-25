import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * MODELO: Utilidad para parsear movimientos desde texto o archivo.
 */
public class SimpleMoveParser {
    public static Move parse(String s) {
        if (s==null) return null;
        s = s.trim();
        if (s.length() < 4) return null;
        String pieceName = null;
        String moveStr = s;
        if (s.contains(":")) {
            String[] parts = s.split(":" ,2);
            pieceName = parts[0];
            moveStr = parts[1];
        }
        String core = moveStr;
        String promotion = null;
        if (moveStr.contains("=")) {
            String[] t = moveStr.split("=");
            core = t[0];
            promotion = t[1];
        }
        if (core.length() != 4) return null;
        int fc = core.charAt(0)-'a';
        int fr = 8-(core.charAt(1)-'0');
        int tc = core.charAt(2)-'a';
        int tr = 8-(core.charAt(3)-'0');
        Move m = new Move(fr, fc, tr, tc, pieceName);
        if (promotion!=null && promotion.length()>0) {
            m.isPromotion = true;
            m.promotionType = promotion.substring(0,1).toUpperCase();
        }
        return m;
    }

    public static List<Move> parseFile(File f) throws IOException {
        List<Move> moves=new ArrayList<>();
        Scanner sc=new Scanner(f);
        while(sc.hasNext()){
            String tok=sc.next();
            Move m=parse(tok.trim());
            if(m!=null) moves.add(m);
        }
        sc.close();
        return moves;
    }
}