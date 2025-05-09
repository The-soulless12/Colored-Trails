import jade.core.Agent;
import java.util.*;

public class Joueur extends Agent {
    private String iconPath;
    private Position positionDepart;
    private Position positionBut;
    private List<String> jetons; 
    private int score = 0;
    private int toursBloqué = 0;

    @Override
    protected void setup() {
        Object[] args = getArguments();

        if (args != null && args.length >= 3) {
            iconPath = (String) args[0];
            positionDepart = (Position) args[1];
            positionBut = (Position) args[2];
            jetons = new ArrayList<>(Arrays.asList((String[]) args[3]));
        } else {
            System.err.println("Arguments manquants ou incorrects !");
            doDelete(); 
            return;
        }

        System.out.println(getLocalName() + " prêt. Départ = " + positionDepart + ", But = " + positionBut);
        System.out.println("Jetons initiaux : " + jetons);
    }

    public Joueur(String iconPath, Position positionDepart, Position positionBut, List<String> jetons) {
        this.iconPath = iconPath;
        this.positionDepart = positionDepart;
        this.positionBut = positionBut;
        this.jetons = jetons;
    }

    public List<String> getJetons() {
        return jetons;
    }

    public void ajouterJeton(String couleur) {
        jetons.add(couleur);
    }

    public boolean retirerJeton(String couleur) {
        return jetons.remove(couleur);
    }

    public Position getPositionDepart() {
        return positionDepart;
    }

    public Position getPositionBut() {
        return positionBut;
    }

    public int getScore() {
        return score;
    }

    public void ajouterScore(int points) {
        score += points;
    }

    public void incrementerToursBloqué() {
        toursBloqué++;
    }

    public int getToursBloqué() {
        return toursBloqué;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public void reinitialiser() {
        this.score = 0;
        this.toursBloqué = 0;
        this.jetons.clear();
        System.out.println("Le joueur " + getLocalName() + " a été réinitialisé.");
    }
}
