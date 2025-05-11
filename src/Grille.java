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
    
    public void dessinerJoueurs() {
        for (Joueur joueur : joueurs) {
            Position pos = joueur.getPositionDepart();
            if (pos == null) continue;
            
            try {
                if (pos.getX() >= 0 && pos.getX() < cells.length && 
                    pos.getY() >= 0 && pos.getY() < cells[0].length) {
                    
                    RoundedCellPanel cell = cells[pos.getX()][pos.getY()];
                    ImageIcon icon = new ImageIcon(joueur.getIconPath());
                    Image scaledImg = icon.getImage().getScaledInstance(
                        CELL_SIZE - 10, CELL_SIZE - 10, Image.SCALE_SMOOTH);
                    ImageIcon scaledIcon = new ImageIcon(scaledImg);
                    JLabel agentLabel = new JLabel(scaledIcon);
                    agentLabel.setSize(CELL_SIZE - 10, CELL_SIZE - 10);
                    cell.removeAll();
                    cell.setLayout(new GridBagLayout());
                    cell.add(agentLabel);
                    
                } else {
                    System.err.println("Position invalide: " + pos);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'affichage du joueur à " + pos + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
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

    // Donne les voisins valides (haut, bas, gauche, droite)
    public List<Position> getVoisins(int x, int y) {
        List<Position> voisins = new ArrayList<>();
        int rows = cells.length;
        int cols = cells[0].length;

        int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            if (nx >= 0 && nx < rows && ny >= 0 && ny < cols) {
                voisins.add(new Position(nx, ny));
            }
        }

        return voisins;
    }

    // Retourne la couleur de la case à une position donnée
    public Color getCouleurCase(Position pos) {
        return cells[pos.getX()][pos.getY()].getFillColor();
    }

    // Retourne la couleur de la case de destination (ou d'un mouvement entre deux cases)
    public Color getCouleurCaseVers(Position from, Position to) {
        return getCouleurCase(to);
    }

}