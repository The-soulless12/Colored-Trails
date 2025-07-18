import java.util.*;
import java.awt.Color;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
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
    private List<Offre> offresRecues = new ArrayList<>();
    private Boolean enAttenteOffres;
    private Integer Loyaute;

    public Joueur(String iconPath, Position position, Position positionArrivee, List<Color> jetons, Grille grille) {
        this.position = position;
        this.positionArrivee = positionArrivee;
        Jetons = jetons;
        this.iconPath = iconPath;
        this.NombreBlocage = 0;
        this.grille = grille;
        this.calculerCheminVersBut();
        this.enAttenteOffres = false;
        this.Loyaute = 100;
    }

    public Joueur() {
        super();
    }

    private void traiterOffres() {
        // Si on a plusieurs offres, on choisit celle avec la meilleure loyauté
        if (offresRecues.size() > 1) {
            // Trier les offres par loyauté décroissante (plus loyaux en premier)
            offresRecues.sort((o1, o2) -> {
                // Récupérer la loyauté du proposeur via la grille
                Integer loyaute1 = getLoyauteJoueur(o1.getProposeur());
                Integer loyaute2 = getLoyauteJoueur(o2.getProposeur());
                return loyaute2.compareTo(loyaute1); // ordre décroissant
            });
        }

        for (Offre offre : offresRecues) {
            // Si on a le jeton demandé par le proposeur, on accepte
            if (Jetons.contains(offre.getCouleurDemandeeRetour())) {
                Jetons.remove(offre.getCouleurDemandeeRetour());

                // On envoie le jeton demandé à l'agent proposeur
                ACLMessage accept = new ACLMessage(ACLMessage.INFORM);
                accept.addReceiver(new AID(offre.getProposeur(), AID.ISLOCALNAME));
                accept.setContent("JETON:" + 
                        offre.getCouleurDemandee().getRGB() + ":" + 
                        offre.getCouleurDemandeeRetour().getRGB() + ":" + 
                        getLocalName() + ":" + 
                        offre.getProposeur());
                send(accept);
                
                Main.appendToCommunication(getLocalName() + " envoie " + offre.getCouleurDemandeeRetour() + " à " + offre.getProposeur() + " (en attente de " + offre.getCouleurDemandee() + ")");
                offresRecues.clear(); // On ne traite qu'une seule offre
                return;
            }
        }

        Main.appendToCommunication(getLocalName() + " n'a pu satisfaire aucune offre reçue.");
        offresRecues.clear();
    }

    public Color choisirCouleurAechanger(Color couleurDemandee) {
        double r = Math.random();

        if (r < 0.25) {
            // Stratégie 1 : couleur aléatoire différente ou non possédée
            for (Color c : Grille.getPastelcolors()) {
                if (!Jetons.contains(c) || !c.equals(couleurDemandee)) {
                    return c;
                }
            }
        } else if (r < 0.5) {
            // Stratégie 2 : couleur manquante dans le chemin
            setLoyaute(- 2); 
            Set<Color> couleursDuChemin = new HashSet<>();
            for (CaseChemin c : chemin) {
                couleursDuChemin.add(c.getCouleur());
            }
            for (Color c : couleursDuChemin) {
                if (!Jetons.contains(c)) {
                    return c;
                }
            }
        } else if (r < 0.75) {
            // Stratégie 3 : couleur la plus fréquente dans le chemin à venir
            setLoyaute(- 7);
            Map<Color, Integer> freq = new HashMap<>();
            for (CaseChemin c : chemin) {
                freq.put(c.getCouleur(), freq.getOrDefault(c.getCouleur(), 0) + 1);
            }

            Color maxColor = null;
            int maxCount = -1;
            for (Map.Entry<Color, Integer> entry : freq.entrySet()) {
                if (!Jetons.contains(entry.getKey()) && entry.getValue() > maxCount) {
                    maxColor = entry.getKey();
                    maxCount = entry.getValue();
                }
            }
            if (maxColor != null) return maxColor;
        } else {
            // Stratégie 4 : tentative d’arnaque, proposer une couleur qu'on n’a pas
            setLoyaute(- 10);
            for (Color c : Grille.getPastelcolors()) {
                if (!c.equals(couleurDemandee)) {
                    return c; 
                }
            }
        }
        return null;
    }

    public void effectuerUnPas(Boolean firsttime) {
        CaseChemin prochaineCase = chemin.get(0);
        Color couleurCase = prochaineCase.getCouleur();

        if (Jetons.contains(couleurCase)) {
            position = new Position(prochaineCase.getX(), prochaineCase.getY());
            Jetons.remove(couleurCase);
            chemin.remove(0);

            synchroniserPositionAvecGrille();

            Main.appendToCommunication(getLocalName() + " avance vers la position " + position);
        } else {
            if (firsttime == true) {
                Main.appendToCommunication(getLocalName() + " envoie un SOS demandant la couleur " + couleurCase);
                envoyerSOS(couleurCase);
            } else {
                // Cette condition c'est juste pour le cas ou après un transfert qui a échoué (on n'a pas eu notre jeton)
                Main.appendToCommunication(getLocalName() + " on m'a trahi, je suis bloqué R.I.P !");
            }
        }
    }

    private void synchroniserPositionAvecGrille() {
        if (grille != null) {
            // Trouver l'objet Joueur correspondant dans la grille et mettre à jour sa position
            for (Joueur joueurGrille : grille.getJoueurs()) {
                if (joueurGrille.getIconPath().equals(this.iconPath)) {
                    joueurGrille.setPosition(new Position(this.position.getX(), this.position.getY()));
                    break;
                }
            }
        }
    }

    public void envoyerSOS(Color couleurManquante) {
        AID[] joueurs = findAllPlayers();
        ACLMessage sos = new ACLMessage(ACLMessage.INFORM);
        sos.setContent("SOS:" + couleurManquante.getRGB());

        for (AID agent : joueurs) {
            if (!agent.equals(getAID())) {
                sos.addReceiver(agent);
            }
        }

        send(sos);
        this.enAttenteOffres = true;
    }

    public void jouerUnTour(Boolean firsttime) {
        Position positionAvant = new Position(position.getX(), position.getY());
        effectuerUnPas(firsttime);
        if (positionAvant.equals(position)) {
            setNombreBlocage(this.NombreBlocage + 1);
            Main.appendToCommunication(getLocalName() + " est resté bloqué, compteur = " + NombreBlocage);
        }

        // Vérification de la victoire
        if (chemin.isEmpty()) {
            Main.appendToCommunication(getLocalName() + " a atteint son but. Fin du jeu !");
            ACLMessage fin = new ACLMessage(ACLMessage.INFORM);
            fin.setContent("FIN:VICTOIRE:" + getLocalName());
            AID[] joueurs = findAllPlayers();
            for (AID agent : joueurs) {
                if (!agent.equals(getAID())) {
                    fin.addReceiver(agent);
                }
            }
            send(fin);
            doDelete();
            return;
        }
        // Vérification du blocage
        if (this.NombreBlocage >= 3) {
            Main.appendToCommunication(getLocalName() + " est bloqué 3 fois, fin du jeu !");
            ACLMessage fin = new ACLMessage(ACLMessage.INFORM);
            fin.setContent("FIN:BLOCAGE:" + getLocalName());
            AID[] joueurs = findAllPlayers();
            for (AID agent : joueurs) {
                if (!agent.equals(getAID())) {
                    fin.addReceiver(agent);
                }
            }
            send(fin);
            doDelete();
        }
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
        
        if (args != null && args.length >= 5) {
            this.iconPath = (String) args[0];
            this.position = (Position) args[1];
            this.positionArrivee = (Position) args[2];
            this.grille = (Grille) args[4];
            this.NombreBlocage = 0;
            this.chemin = new ArrayList<>();
            this.calculerCheminVersBut();
            this.enAttenteOffres = false;
            this.Loyaute = 100;

            @SuppressWarnings("unchecked")
            List<Color> couleurs = (List<Color>) args[3];
            this.Jetons = couleurs;
        } else {
            System.out.println(getLocalName() + " a reçu des arguments insuffisants.");
            this.Jetons = new ArrayList<>();
            this.position = new Position(0, 0);
            this.positionArrivee = new Position(0, 0);
        }

        Main.appendToCommunication(getLocalName() + " prêt pour le jeu!");
        
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
            }
        });

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getContent().equals("GO")) {
                        Boolean firsttime = true;
                        jouerUnTour(firsttime);
                    }
                    if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("SOS:")) {
                        int rgb = Integer.parseInt(msg.getContent().split(":")[1]);
                        Color couleurDemandee = new Color(rgb);
                        String demandeur = msg.getSender().getLocalName();
                        
                        // Si l'agent a la couleur demandée, alors il répond
                        if (Jetons.contains(couleurDemandee)) {
                            Color couleurDemandeeEnRetour = choisirCouleurAechanger(couleurDemandee);
                            
                            if (couleurDemandeeEnRetour != null) {
                                ACLMessage offre = new ACLMessage(ACLMessage.PROPOSE);
                                offre.setContent("OFFRE:" + couleurDemandee.getRGB() + ":" + couleurDemandeeEnRetour.getRGB() + " pour " + demandeur);
                                offre.addReceiver(new AID(demandeur, AID.ISLOCALNAME));
                                send(offre);
                                Main.appendToCommunication(getLocalName() + " propose : " + couleurDemandee + " contre " + couleurDemandeeEnRetour + " pour " + demandeur);
                            }
                        }
                        else {
                            Main.appendToCommunication(getLocalName() + " Je n'ai aucune offre à te faire " + demandeur);
                        }
                    }
                    if (msg.getPerformative() == ACLMessage.PROPOSE && msg.getContent().startsWith("OFFRE:")) {
                        String[] parts = msg.getContent().split(":| pour ");
                        Color couleurDemandee = new Color(Integer.parseInt(parts[1]));
                        Color couleurDemandeeEnRetour = new Color(Integer.parseInt(parts[2]));
                        String demandeur = parts[3];
                        String proposeur = msg.getSender().getLocalName();

                        if (demandeur.equals(getLocalName())) {
                            Offre offre = new Offre(demandeur, proposeur, couleurDemandee, couleurDemandeeEnRetour);
                            offresRecues.add(offre);
                            if (offresRecues.size() == 1) {
                                addBehaviour(new WakerBehaviour(myAgent, 500) { 
                                    @Override
                                    protected void onWake() {
                                        traiterOffres();
                                    }
                                });
                            }
                        }

                    }
                    if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("JETON:")) {
                        String[] parts = msg.getContent().split(":");
                        Color couleurReçue = new Color(Integer.parseInt(parts[2])); // La couleur demandée en retour
                        Color couleurAPromettre = new Color(Integer.parseInt(parts[1])); // La couleur demandée au début
                        String demandeur = parts[3];
                        String proposeur = parts[4];

                        if (proposeur.equals(getLocalName())) {
                            Jetons.add(couleurReçue); // On ajoute la couleur reçue
                            Main.appendToCommunication(getLocalName() + " a reçu " + couleurReçue + " de " + demandeur);

                            // On envoie maintenant la couleur promise (offerte dans l'échange)
                            if (Jetons.contains(couleurAPromettre)) {
                                Jetons.remove(couleurAPromettre);
                                ACLMessage retour = new ACLMessage(ACLMessage.INFORM);
                                retour.addReceiver(new AID(demandeur, AID.ISLOCALNAME));
                                retour.setContent("JETON_RETOUR:" + couleurAPromettre.getRGB() + ":" + getLocalName());
                                send(retour);

                                Main.appendToCommunication(getLocalName() + " envoie " + couleurAPromettre + " à " + demandeur + " pour conclure l'échange");
                            } else {
                                Main.appendToCommunication(getLocalName() + " devait envoyer " + couleurAPromettre + " mais ne l'a plus !");
                            }
                        }
                    }
                    if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("JETON_RETOUR:")) {
                        String[] parts = msg.getContent().split(":");
                        Color couleurReçue = new Color(Integer.parseInt(parts[1]));
                        String proposeur = parts[2];

                        Jetons.add(couleurReçue);
                        Main.appendToCommunication(getLocalName() + " a reçu " + couleurReçue + " de " + proposeur + ", il peut avancer !");
                        
                        Position positionAvant = new Position(position.getX(), position.getY());
                        Boolean firsttime = false; 
                        jouerUnTour(firsttime);

                        try { Thread.sleep(800); } catch (InterruptedException e) { e.printStackTrace(); }
                        verifierDeBlocage(positionAvant);
                    }
                    if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("FIN:")) {
                        Main.appendToCommunication("Fin du jeu reçue : " + msg.getContent());
                        doDelete();
                        return;
                    }
                } else {
                    block();
                }
            }
        });
    }

    public void verifierDeBlocage(Position positionAvant) {
        if (!positionAvant.equals(position)) {
            setNombreBlocage(this.NombreBlocage - 1);
            Main.appendToCommunication(getLocalName() + "a pu se débloquer, compteur = " + NombreBlocage);
        }
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
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void takeDown() {
        calculerPoints();
        super.takeDown();
    }

    private int calculerPoints() {
        int points = 0;
        
        // Bonus pour les jetons restants (5 points par jeton)
        points += Jetons.size() * 5;
        
        if (position.equals(positionArrivee)) {
            // Bonus pour avoir atteint le but (100 points)
            points += 100;
            Main.appendToCommunication(getLocalName() + " a atteint son but (+100 points)");
        } else {
            // Pénalité pour les cases manquantes (10 points par case)
            int casesManquantes = chemin.size(); // nombre de cases qu'il reste à parcourir
            int penalite = casesManquantes * 10;
            points -= penalite;
            Main.appendToCommunication(getLocalName() + " n'a pas atteint son but (-" + penalite + " points)");
        }
        
        // Afficher le détail des points
        Main.appendToCommunication(getLocalName() + " a " + Jetons.size() + " jetons restants (+" + (Jetons.size() * 5) + " points)");
        Main.appendToCommunication(getLocalName() + " obtient un total de " + points + " points");
        
        return points;
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
        this.NombreBlocage = nombreBlocage;
        if (grille != null) {
            for (Joueur joueurGrille : grille.getJoueurs()) {
                if (joueurGrille.getIconPath().equals(this.iconPath)) {
                    joueurGrille.NombreBlocage = nombreBlocage;
                    break;
                }
            }
        }
    }

    public void setLoyaute(Integer Loyaute) {
        this.Loyaute = this.Loyaute + Loyaute;
        // Synchroniser avec l'objet dans la grille
        if (grille != null) {
            for (Joueur joueurGrille : grille.getJoueurs()) {
                if (joueurGrille.getIconPath().equals(this.iconPath)) {
                    joueurGrille.Loyaute = this.Loyaute;
                    break;
                }
            }
        }
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

    public List<Offre> getOffresRecues() {
        return offresRecues;
    }

    public void setOffresRecues(List<Offre> offresRecues) {
        this.offresRecues = offresRecues;
    }

    public Boolean getEnAttenteOffres() {
        return enAttenteOffres;
    }

    public void setEnAttenteOffres(Boolean enAttenteOffres) {
        this.enAttenteOffres = enAttenteOffres;
    }
    
    public Integer getLoyaute() {
        return Loyaute;
    }

    private Integer getLoyauteJoueur(String nomJoueur) {
        if (grille != null) {
            for (Joueur joueur : grille.getJoueurs()) {
                if (joueur.getLocalName() != null && joueur.getLocalName().equals(nomJoueur)) {
                    return joueur.getLoyaute();
                }
            }
        }
        return 0; 
    }
}