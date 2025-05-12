import jade.core.Agent;

public class JoueurAgent extends Agent {
    private Joueur joueur;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " est prêt !");
    }

    public Joueur getJoueur() {
        return joueur;
    }
}
