import java.util.*;
import java.awt.Color;

public class Joueur {
    private Position position;
    private Position positionDepart;
    private List<Color> Jetons;
    private String iconPath;

    public Joueur(String Path, Position positionDepart, Position position, List<Color> Jetons) {
        this.position = position;
        this.positionDepart = positionDepart;
        this.Jetons = Jetons;
        this.iconPath = Path; 
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Position getPositionDepart() {
        return positionDepart;
    }

    public List<Color> getJetons() {
        return Jetons;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void ajouterCarte(Color couleur) {
        Jetons.add(couleur);
    }

    public boolean aCarte(Color couleur) {
        return Jetons.contains(couleur);
    }

    public void retirerCarte(Color couleur) {
        Jetons.remove(couleur);
    }

    public List<Position> getCoupPossible(Grille grille) {
        List<Position> deplacementsPossibles = new ArrayList<>();
        for (Position voisin : grille.getVoisins(position.getX(), position.getY())) {
            Color couleurCase = grille.getCellColor(voisin.getX(), voisin.getY()); 
            if (Jetons.contains(couleurCase)) {
                deplacementsPossibles.add(voisin);
            }
        }
        return deplacementsPossibles;
    }

    public boolean deplacerVers(Position nouvellePosition, Grille grille) {
        Color couleur = grille.getCellColor(nouvellePosition.getX(), nouvellePosition.getY());
        if (Jetons.contains(couleur)) {
            this.position = nouvellePosition;
            Jetons.remove(couleur);
            return true;
        }
        return false;
    }

}
