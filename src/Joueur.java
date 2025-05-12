import java.util.*;
import java.awt.Color;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Joueur extends Agent {
    private Position position;
    private Position positionArrivee;
    private List<Color> Jetons;
    private String iconPath;

    public Joueur(String iconPath, Position position, Position positionArrivee, List<Color> jetons) {
        this.position = position;
        this.positionArrivee = positionArrivee;
        Jetons = jetons;
        this.iconPath = iconPath;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Position getPositionArrivee() {
        return positionArrivee;
    }

    public void setPositionArrivee(Position positionArrivee) {
        this.positionArrivee = positionArrivee;
    }

    public List<Color> getJetons() {
        return Jetons;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
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

    public void addJeton(Color jeton) {
        this.Jetons.add(jeton);
    }

    public void move(Position newPosition) {
        this.position = newPosition;
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

    @Override
    protected void setup() {
        Object[] args = getArguments();

        if (args != null && args.length >= 4) {
            this.iconPath = (String) args[0];
            this.position = (Position) args[1];
            this.positionArrivee = (Position) args[2];

            @SuppressWarnings("unchecked")
            List<Color> couleurs = (List<Color>) args[3];
            this.Jetons = couleurs;
        } else {
            System.out.println(getLocalName() + " a reçu des arguments insuffisants.");
            this.Jetons = new ArrayList<>();
            this.position = new Position(0, 0);
            this.positionArrivee = new Position(0, 0);
        }

        System.out.println(getLocalName() + " prêt pour le jeu!");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println(getLocalName() + " a reçu : " + msg.getContent());

                    if (msg.getContent().equals("propose échange")) {
                        System.out.println("Échange proposé à " + getLocalName());
                    }

                } else {
                    block();
                }
            }
        });
    }
}
