import java.awt.Color;

public class CaseChemin {
    private final int x;
    private final int y;
    private final Color couleur;

    public CaseChemin(int x, int y, Color couleur) {
        this.x = x;
        this.y = y;
        this.couleur = couleur;
    }

    public int getX() { return x; }

    public int getY() { return y; }
    
    public Color getCouleur() { return couleur; }

    public Position toPosition() { return new Position(x, y); }
}
