import java.awt.Color;

class Offre {
    private String demandeur;
    private String proposeur;
    private Color couleurDemandee; // couleur que le demandeur veut
    private Color couleurDemandeeRetour;  // couleur que le proposeur donne

    public Offre(String demandeur, String proposeur, Color couleurDemandee, Color couleurDemandeeRetour) {
        this.demandeur = demandeur;
        this.proposeur = proposeur;
        this.couleurDemandee = couleurDemandee;
        this.couleurDemandeeRetour = couleurDemandeeRetour;
    }

    public String getDemandeur() { return demandeur; }
    public String getProposeur() { return proposeur; }
    public Color getCouleurDemandee() { return couleurDemandee; }
    public Color getCouleurDemandeeRetour() { return couleurDemandeeRetour; }
}
