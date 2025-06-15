import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Grille extends JPanel {

    private static final int CELL_SIZE = 70;
    private static final Color BORDER_COLOR = new Color(100, 100, 100, 100);
    private static final Color[] pastelColors = generatePastelColors(5);
    private RoundedCellPanel[][] cells;
    private List<Joueur> joueurs;

    public Grille(int rows, int cols) {
        super();
        setOpaque(false);

        RoundedPanel gridContainer = new RoundedPanel(30, new Color(240, 240, 240));
        gridContainer.setLayout(new GridLayout(rows, cols, 10, 10));
        gridContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        cells = new RoundedCellPanel[rows][cols];
        joueurs = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Color fill = pastelColors[rand.nextInt(pastelColors.length)];
                RoundedCellPanel cell = new RoundedCellPanel(fill);
                cells[i][j] = cell;
                gridContainer.add(cell);
            }
        }

        setLayout(new BorderLayout());
        add(gridContainer, BorderLayout.CENTER);
        
        setPreferredSize(new Dimension(cols * (CELL_SIZE + 10) + 40, rows * (CELL_SIZE + 10) + 40));
    }

    private static Color[] generatePastelColors(int count) {
        Color[] colors = new Color[count];
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            int r = rand.nextInt(128) + 127;
            int g = rand.nextInt(128) + 127;
            int b = rand.nextInt(128) + 127;
            colors[i] = new Color(r, g, b);
        }
        return colors;
    }

    public void ajouterJoueur(Joueur joueur) {
        joueurs.add(joueur);
        
        if (isVisible()) {
            dessinerJoueurs();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
    
    // public void dessinerJoueurs() {
    //     // On vide toutes les cases
    //     for (int i = 0; i < cells.length; i++) {
    //         for (int j = 0; j < cells[0].length; j++) {
    //             cells[i][j].removeAll();
    //         }
    //     }

    //     for (Joueur joueur : joueurs) {
    //         Position pos = joueur.getPosition();
    //         if (pos == null) continue;
            
    //         try {
    //             if (pos.getX() >= 0 && pos.getX() < cells.length && 
    //                 pos.getY() >= 0 && pos.getY() < cells[0].length) {
                    
    //                 RoundedCellPanel cell = cells[pos.getX()][pos.getY()];
    //                 ImageIcon icon = new ImageIcon(joueur.getIconPath());
    //                 Image scaledImg = icon.getImage().getScaledInstance(
    //                     CELL_SIZE - 10, CELL_SIZE - 10, Image.SCALE_SMOOTH);
    //                 ImageIcon scaledIcon = new ImageIcon(scaledImg);
    //                 JLabel agentLabel = new JLabel(scaledIcon);
    //                 agentLabel.setSize(CELL_SIZE - 10, CELL_SIZE - 10);
    //                 cell.removeAll();
    //                 cell.setLayout(new GridBagLayout());
    //                 cell.add(agentLabel);
                    
    //             } else {
    //                 System.err.println("Position invalide: " + pos);
    //             }
    //         } catch (Exception e) {
    //             System.err.println("Erreur lors de l'affichage du joueur à " + pos + ": " + e.getMessage());
    //             e.printStackTrace();
    //         }
    //     }
    // }

    public void dessinerJoueurs() {
    // On vide toutes les cases
    for (int i = 0; i < cells.length; i++) {
        for (int j = 0; j < cells[0].length; j++) {
            cells[i][j].removeAll();
        }
    }

    // Créer une Map pour grouper les joueurs par position
    Map<Position, List<Joueur>> joueursParPosition = new HashMap<>();
    for (Joueur joueur : joueurs) {
        Position pos = joueur.getPosition();
        if (pos == null) continue;
        joueursParPosition.computeIfAbsent(pos, k -> new ArrayList<>()).add(joueur);
    }

    // Dessiner les joueurs
    for (Map.Entry<Position, List<Joueur>> entry : joueursParPosition.entrySet()) {
        Position pos = entry.getKey();
        List<Joueur> joueursCase = entry.getValue();
        
        if (pos.getX() >= 0 && pos.getX() < cells.length && 
            pos.getY() >= 0 && pos.getY() < cells[0].length) {
            
            RoundedCellPanel cell = cells[pos.getX()][pos.getY()];
            cell.removeAll();
            cell.setLayout(null); // On utilise null layout pour positionner précisément

            if (joueursCase.size() == 1) {
                // Un seul joueur : taille normale au centre
                Joueur joueur = joueursCase.get(0);
                ImageIcon icon = new ImageIcon(joueur.getIconPath());
                Image scaledImg = icon.getImage().getScaledInstance(
                    CELL_SIZE - 10, CELL_SIZE - 10, Image.SCALE_SMOOTH);
                JLabel agentLabel = new JLabel(new ImageIcon(scaledImg));
                agentLabel.setBounds(5, 5, CELL_SIZE - 10, CELL_SIZE - 10);
                cell.add(agentLabel);
            } else {
                // Plusieurs joueurs : taille réduite
                int petiteTaille = (CELL_SIZE - 5) / 2;
                
                // Premier joueur en haut à gauche
                if (joueursCase.size() >= 1) {
                    Joueur joueur1 = joueursCase.get(0);
                    ImageIcon icon1 = new ImageIcon(joueur1.getIconPath());
                    Image scaledImg1 = icon1.getImage().getScaledInstance(
                        petiteTaille, petiteTaille, Image.SCALE_SMOOTH);
                    JLabel label1 = new JLabel(new ImageIcon(scaledImg1));
                    label1.setBounds(5, 5, petiteTaille, petiteTaille);
                    cell.add(label1);
                }

                // Deuxième joueur en bas à droite
                if (joueursCase.size() >= 2) {
                    Joueur joueur2 = joueursCase.get(1);
                    ImageIcon icon2 = new ImageIcon(joueur2.getIconPath());
                    Image scaledImg2 = icon2.getImage().getScaledInstance(
                        petiteTaille, petiteTaille, Image.SCALE_SMOOTH);
                    JLabel label2 = new JLabel(new ImageIcon(scaledImg2));
                    label2.setBounds(CELL_SIZE - petiteTaille - 10, 
                                   CELL_SIZE - petiteTaille - 10, 
                                   petiteTaille, petiteTaille);
                    cell.add(label2);
                }

                // Troisième joueur en haut à droite
                if (joueursCase.size() >= 3) {
                    Joueur joueur3 = joueursCase.get(2);
                    ImageIcon icon3 = new ImageIcon(joueur3.getIconPath());
                    Image scaledImg3 = icon3.getImage().getScaledInstance(
                        petiteTaille, petiteTaille, Image.SCALE_SMOOTH);
                    JLabel label3 = new JLabel(new ImageIcon(scaledImg3));
                    label3.setBounds(CELL_SIZE - petiteTaille - 10, 5, 
                                   petiteTaille, petiteTaille);
                    cell.add(label3);
                }

                // Quatrième joueur en bas à gauche
                if (joueursCase.size() >= 4) {
                    Joueur joueur4 = joueursCase.get(3);
                    ImageIcon icon4 = new ImageIcon(joueur4.getIconPath());
                    Image scaledImg4 = icon4.getImage().getScaledInstance(
                        petiteTaille, petiteTaille, Image.SCALE_SMOOTH);
                    JLabel label4 = new JLabel(new ImageIcon(scaledImg4));
                    label4.setBounds(5, CELL_SIZE - petiteTaille - 10, 
                                   petiteTaille, petiteTaille);
                    cell.add(label4);
                }
            }
            cell.revalidate();
            cell.repaint();
        }
    }
}

    static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color backgroundColor;

        public RoundedPanel(int arc, Color bg) {
            this.arc = arc;
            this.backgroundColor = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(new Color(180, 180, 180));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
        }
    }

    static class RoundedCellPanel extends JPanel {
        private final Color fillColor;

        public RoundedCellPanel(Color fillColor) {
            this.fillColor = fillColor;
            setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
            setOpaque(false);
            setLayout(new GridBagLayout());
        }

        public Color getFillColor() {
            return fillColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 20;
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            g2.setColor(BORDER_COLOR);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

            g2.dispose();
        }
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    public static Color[] getPastelcolors() {
        return pastelColors;
    }

        public Color getCellColor(int x, int y) {
        return cells[x][y].getFillColor();
    }

    public RoundedCellPanel getCell(int x, int y) {
        return cells[x][y];
    }

    public Color getRandomColorUsed() {
        Random rand = new Random();
        return pastelColors[rand.nextInt(pastelColors.length)];
    }
}