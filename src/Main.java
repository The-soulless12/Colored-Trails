import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("COLORED TRAILS");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 20, 20, 20)); // Les marges de l'écran d'accueil

        Grille grid = new Grille(5, 7);
        int a = new Random().nextInt(13) + 1, b; while((b = new Random().nextInt(13) + 1) == a);
        Joueur joueur1 = createRandomJoueur("Images/agent" + a + ".png"), joueur2 = createRandomJoueur("Images/agent" + b + ".png");
        
        // Vérification que les positions ne sont pas identiques
        while (joueur1.getPositionDepart().getX() == joueur2.getPositionDepart().getX() && 
               joueur1.getPositionDepart().getY() == joueur2.getPositionDepart().getY()) {
            joueur2 = createRandomJoueur("Images/agent2.png");
        }
        grid.ajouterJoueur(joueur1);  
        grid.ajouterJoueur(joueur2);

        Color buttonColor = grid.getRandomColorUsed();
        Color lightBackground = lightenColor(buttonColor, 0.6f);
        mainPanel.setBackground(lightBackground);
        mainPanel.setOpaque(true);

        // Ruban 01 : Settings and Logout
        JPanel ribbon1 = new JPanel();
        ribbon1.setPreferredSize(new Dimension(600, 40)); 
        ribbon1.setOpaque(false);
        ribbon1.setLayout(new BorderLayout());
        // Settings button
        ImageIcon icon = new ImageIcon("Images/settings.png");
        Image image = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(image);
        JButton cornerButton = new JButton(scaledIcon);
        cornerButton.setPreferredSize(new Dimension(30, 30));
        cornerButton.setFocusPainted(false);
        cornerButton.setContentAreaFilled(false);
        cornerButton.setBorderPainted(false);
        cornerButton.setMargin(new Insets(0, 0, 0, 0));
        cornerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Fenetre Parametres
        cornerButton.addActionListener(e -> {
            JDialog settingsDialog = new JDialog(frame, "Paramètres", true);
            settingsDialog.setSize(500, 300);
            settingsDialog.setLayout(new BorderLayout());
            String[] columnNames = {"Agent", "Numéro Agent", "Position Départ", "Nombre de Cartes"};
            Object[][] data = new Object[grid.getJoueurs().size()][4];

            int i = 0;
            for (Joueur joueur : grid.getJoueurs()) {
                ImageIcon iconJoueur = new ImageIcon(new ImageIcon(joueur.getIconPath()).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
                data[i][0] = iconJoueur;
                data[i][1] = i+1;
                data[i][2] = "(" + joueur.getPositionDepart().getX() + ", " + joueur.getPositionDepart().getY() + ")";
                data[i][3] = joueur.getJetons().size();
                i++;
            }

            JTable table = new JTable(new javax.swing.table.DefaultTableModel(data, columnNames) {
                @Override
                public Class<?> getColumnClass(int column) {
                    return column == 0 ? ImageIcon.class : Object.class;
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 0; // Seule la colonne image est éditable
                }
            });
            table.setFont(new Font("Monospaced", Font.PLAIN, 12));
            table.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 12));
            table.getTableHeader().setBackground(buttonColor);
            table.setRowHeight(30); 

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            for (int col = 1; col < table.getColumnCount(); col++) {
                table.getColumnModel().getColumn(col).setCellRenderer(centerRenderer);
            }

            JScrollPane scrollPane = new JScrollPane(table);
            settingsDialog.add(scrollPane, BorderLayout.CENTER);

            JButton validateButton = new JButton("Fermer");
            JPanel bottomPanel = new JPanel();
            bottomPanel.add(validateButton);
            settingsDialog.add(bottomPanel, BorderLayout.SOUTH);

            validateButton.addActionListener(ev -> settingsDialog.dispose());
            settingsDialog.setLocationRelativeTo(frame);
            settingsDialog.setVisible(true);
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        rightPanel.setOpaque(false);
        rightPanel.add(cornerButton);
        ribbon1.add(rightPanel, BorderLayout.EAST);
        
        // Logout button
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        leftPanel.setOpaque(false);
        ImageIcon logoutIcon = new ImageIcon("Images/logout.png");
        Image logoutImg = logoutIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon logoutscaledIcon = new ImageIcon(logoutImg);

        ImageIcon logoutIcon2 = new ImageIcon("Images/logout2.png");
        Image logoutImg2 = logoutIcon2.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon logoutscaledIcon2 = new ImageIcon(logoutImg2);

        JButton logoutButton = new JButton(logoutscaledIcon);
        logoutButton.setPreferredSize(new Dimension(30, 30));
        logoutButton.setFocusPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setMargin(new Insets(0, 0, 0, 0));
        logoutButton.addActionListener(e -> frame.dispose());
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutButton.setIcon(logoutscaledIcon2);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutButton.setIcon(logoutscaledIcon);
            }
        });
        leftPanel.add(logoutButton);
        ribbon1.add(leftPanel, BorderLayout.WEST);

        // Ruban 02 : Start button
        JPanel ribbon2 = new JPanel();
        ribbon2.setPreferredSize(new Dimension(600, 40));
        ribbon2.setOpaque(false);
        ribbon2.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        RoundedButton startButton = new RoundedButton("Start", buttonColor);
        startButton.setFont(new Font("Monospaced", Font.BOLD, 25));
        startButton.setPreferredSize(new Dimension(120, 40));
        startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ribbon2.add(startButton);

        // Ruban 03 : Espace vide
        JPanel ribbon3 = new JPanel();
        ribbon3.setPreferredSize(new Dimension(600, 20));
        ribbon3.setOpaque(false);

        mainPanel.add(ribbon1);
        mainPanel.add(ribbon2);
        mainPanel.add(ribbon3);
        mainPanel.add(grid);

        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // On affiche les joueurs après que tout est visible
        SwingUtilities.invokeLater(() -> {
            grid.dessinerJoueurs();
        });
    }

    public static Color lightenColor(Color color, float factor) {
        int r = Math.min(255, (int)(color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int)(color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int)(color.getBlue() + (255 - color.getBlue()) * factor));
        return new Color(r, g, b);
    }

    public static Joueur createRandomJoueur(String iconPath) {
        Random rand = new Random();
        int x = rand.nextInt(5);  
        int y = rand.nextInt(7);  

        Position positionDepart = new Position(x, y);
        Position positionBut = new Position(rand.nextInt(5), rand.nextInt(7));
        List<Color> jetons = new ArrayList<>();
         // Ajouter une couleur à la liste -------------------------------- BOUCLE WA9IL psq machi ga3 same
        jetons.add(Color.RED); 

        Joueur joueur = new Joueur(iconPath, positionDepart, positionBut, jetons);  

        return joueur;
    }

    static class RoundedButton extends JButton {
        private final Color backgroundColor;

        public RoundedButton(String text, Color bgColor) {
            super(text.toUpperCase());
            this.backgroundColor = bgColor;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setForeground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int shadowOffset = 3;
            g2.setColor(new Color(0, 0, 0, 60)); 
            g2.fillRoundRect(shadowOffset, shadowOffset, getWidth() - shadowOffset, getHeight() - shadowOffset, 20, 20);

            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - shadowOffset, getHeight() - shadowOffset, 20, 20);

            g2.setFont(getFont());
            FontMetrics metrics = g2.getFontMetrics();
            int x = (getWidth() - shadowOffset - metrics.stringWidth(getText())) / 2;
            int y = ((getHeight() - shadowOffset - metrics.getHeight()) / 2) + metrics.getAscent();
            g2.setColor(getForeground());
            g2.drawString(getText(), x, y);

            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            
        }
    }
}