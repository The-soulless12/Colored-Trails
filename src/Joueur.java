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
    private Grille grille;
    private Position position;
    private Position positionArrivee;
    private List<Color> Jetons;
    private String iconPath;
    private Integer NombreBlocage;
    private List<CaseChemin> chemin;

    public Joueur(String iconPath, Position position, Position positionArrivee, List<Color> jetons, Grille grille) {
        this.position = position;
        this.positionArrivee = positionArrivee;
        Jetons = jetons;
        this.iconPath = iconPath;
        this.NombreBlocage = 0;
        this.grille = grille;
        this.calculerCheminVersBut();
    }

    public Joueur() {
        super();
    }

    public void calculerCheminVersBut() {
        chemin = new ArrayList<>();
        int x = position.getX();
        int y = position.getY();
        int butX = positionArrivee.getX();
        int butY = positionArrivee.getY();

        while (x != butX && y != butY) {
            if (x < butX) x++;
            else if (x > butX) x--;

            if (y < butY) y++;
            else if (y > butY) y--;

            Color c = grille.getCellColor(x, y);
            chemin.add(new CaseChemin(x, y, c));
        }

        while (x != butX) {
            if (x < butX) x++;
            else x--;
            Color c = grille.getCellColor(x, y);
            chemin.add(new CaseChemin(x, y, c));
        }

        while (y != butY) {
            if (y < butY) y++;
            else y--;
            Color c = grille.getCellColor(x, y);
            chemin.add(new CaseChemin(x, y, c));
        }
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
        } else {
            System.out.println(getLocalName() + " a reçu des arguments insuffisants.");
            this.Jetons = new ArrayList<>();
            this.position = new Position(0, 0);
            this.positionArrivee = new Position(0, 0);
        }

        System.out.println(getLocalName() + " prêt pour le jeu!");
        
        registerWithDF();
        
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                AID[] allPlayers = findAllPlayers();
                
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                for (AID agent : allPlayers) {
                    if (!agent.equals(getAID())) {
                        msg.addReceiver(agent);
                    }
                }
                
                msg.setContent("Bonjour à tous ! Je suis " + getLocalName() + " et je suis prêt à jouer !");
                send(msg);
                System.out.println(getLocalName() + " a envoyé une salutation à tous les agents");
            }
        });

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println(getLocalName() + " a reçu un message de " + msg.getSender().getLocalName() + ": " + msg.getContent());

                    if (msg.getContent().equals("propose échange")) {
                        
                    }
                } else {
                    block();
                }
            }
        });
    }

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
    
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
            System.out.println(getLocalName() + " s'est désenregistré du DF");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        
        System.out.println(getLocalName() + " a terminé.");
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

    public Integer getNombreBlocage() {
        return NombreBlocage;
    }

    public void setNombreBlocage(Integer nombreBlocage) {
        NombreBlocage = nombreBlocage;
    }

    public List<CaseChemin> getChemin() {
        return chemin;
    }

    public void setChemin(List<CaseChemin> chemin) {
        this.chemin = chemin;
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

    public void setJetons(List<Color> jetons) {
        this.Jetons = jetons;
    }

    public void move(Position newPosition) {
        this.position = newPosition;
    }
}