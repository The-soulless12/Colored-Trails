import java.util.*;
import java.awt.Color;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
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

    public Joueur() {
        super();
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

    // S'enregistrer auprès du Directory Facilitator (DF)
    private void registerWithDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        
        ServiceDescription sd = new ServiceDescription();
        sd.setType("colored-trails-player");
        sd.setName(getLocalName() + "-service");
        dfd.addServices(sd);
        
        try {
            DFService.register(this, dfd);
            System.out.println(getLocalName() + " s'est enregistré auprès du DF");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
    
    // Trouver tous les agents du jeu
    private AID[] findAllPlayers() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("colored-trails-player");
        template.addServices(sd);
        
        try {
            DFAgentDescription[] results = DFService.search(this, template);
            AID[] agents = new AID[results.length];
            for (int i = 0; i < results.length; i++) {
                agents[i] = results[i].getName();
            }
            return agents;
        } catch (FIPAException e) {
            e.printStackTrace();
            return new AID[0];
        }
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
            System.out.println(getLocalName() + " a reçu ses arguments !");
        } else {
            System.out.println(getLocalName() + " a reçu des arguments insuffisants.");
            this.Jetons = new ArrayList<>();
            this.position = new Position(0, 0);
            this.positionArrivee = new Position(0, 0);
        }

        System.out.println(getLocalName() + " prêt pour le jeu!");
        
        // S'enregistrer auprès du DF
        registerWithDF();
        
        // Comportement pour envoyer un salut à tous les agents au démarrage
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                // Petite pause pour s'assurer que tous les agents sont prêts et enregistrés
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // Trouver tous les agents joueurs
                AID[] allPlayers = findAllPlayers();
                
                // Créer et envoyer le message de salutation en broadcast
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                for (AID agent : allPlayers) {
                    // Ne pas s'envoyer le message à soi-même
                    if (!agent.equals(getAID())) {
                        msg.addReceiver(agent);
                    }
                }
                
                msg.setContent("Bonjour à tous ! Je suis " + getLocalName() + " et je suis prêt à jouer !");
                send(msg);
                System.out.println(getLocalName() + " a envoyé une salutation à tous les agents");
            }
        });

        // Comportement cyclique pour recevoir les messages
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println(getLocalName() + " a reçu un message de " + msg.getSender().getLocalName() + ": " + msg.getContent());

                    // Si c'est une proposition d'échange
                    if (msg.getContent().equals("propose échange")) {
                        System.out.println("Échange proposé à " + getLocalName());

                        // Envoie un message de réponse
                        ACLMessage replyMsg = new ACLMessage(ACLMessage.INFORM);
                        replyMsg.addReceiver(msg.getSender());
                        replyMsg.setContent("Je considère votre proposition d'échange.");
                        send(replyMsg);
                    }
                } else {
                    block();
                }
            }
        });
    }
    
    @Override
    protected void takeDown() {
        // Se désenregistrer du DF lorsque l'agent se termine
        try {
            DFService.deregister(this);
            System.out.println(getLocalName() + " s'est désenregistré du DF");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        
        System.out.println(getLocalName() + " a terminé.");
    }
}