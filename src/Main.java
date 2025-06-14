import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import java.util.stream.Collectors;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.*;

public class Main {
    private static JTextArea communicationArea;
    
    public static void appendToCommunication(String message) {
        if (communicationArea != null) {
            SwingUtilities.invokeLater(() -> {
                String processedMessage = message;
                // Remplacer les descriptions de couleur par C1, C2, C3, etc.
                if (message.contains("java.awt.Color")) {
                    Color[] colors = Grille.getPastelcolors();
                    for (int i = 0; i < colors.length; i++) {
                        String colorStr = colors[i].toString();
                        processedMessage = processedMessage.replace(colorStr, "C" + (i + 1));
                    }
                }
                communicationArea.append(processedMessage + "\n");
                communicationArea.setCaretPosition(communicationArea.getDocument().getLength());
            });
        } 
    }

    public static void main(String[] args) {
        // Réduit les logs JADE à WARNING uniquement
        Logger jadeLogger = Logger.getLogger("jade");
        jadeLogger.setLevel(Level.WARNING);
        for (Handler handler : jadeLogger.getHandlers()) {
            handler.setLevel(Level.WARNING);
        }

        JFrame frame = new JFrame("COLORED TRAILS");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 20, 20, 20)); // Les marges de l'écran d'accueil

        // Création de la grille et des agents 
        Grille grid = new Grille(5, 7);
        Random rand = new Random();
        int a = rand.nextInt(13) + 1;
        int b;
        do { b = rand.nextInt(13) + 1; } while (b == a);
        int c;
        do { c = rand.nextInt(13) + 1; } while (c == a || c == b);

        List<Position> positionsOccupees = new ArrayList<>();

        createRandomJoueur("Images/agent" + a + ".png", grid, positionsOccupees);
        createRandomJoueur("Images/agent" + b + ".png", grid, positionsOccupees);
        createRandomJoueur("Images/agent" + c + ".png", grid, positionsOccupees);

        // Initialisation de la plateforme JADE
        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        ContainerController mainContainer = rt.createMainContainer(profile);
        List<AgentController> agentsControllers = new ArrayList<>();

        try {
            List<Joueur> joueurs = grid.getJoueurs();
            for (int i = 0; i < joueurs.size(); i++) {
                Joueur joueur = joueurs.get(i);
                AgentController agent = mainContainer.createNewAgent(
                    "Agent" + (i + 1),
                    "Joueur",
                    new Object[]{joueur.getIconPath(), joueur.getPosition(), joueur.getPositionArrivee(), joueur.getJetons(), grid}
                );
                agentsControllers.add(agent);
                agent.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Color buttonColor = grid.getRandomColorUsed();
        Color lightBackground = CouleurPastel(buttonColor, 0.6f);
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
            settingsDialog.setSize(600, 300);
            settingsDialog.setLayout(new BorderLayout());
            Color[] couleurs = Grille.getPastelcolors();
            String[] baseColumns = {"Agent", "N°", "Pos", "But", "Blocage", "Jetons"};
            String[] columnNames = Arrays.copyOf(baseColumns, baseColumns.length + couleurs.length);
            for (int i = 0; i < couleurs.length; i++) {
                columnNames[baseColumns.length + i] = "C" + (i + 1); // ou utilise un nom basé sur la couleur si tu veux
            }

            Object[][] data = new Object[grid.getJoueurs().size()][columnNames.length];
            int i = 0;
            for (Joueur joueur : grid.getJoueurs()) {
                ImageIcon iconJoueur = new ImageIcon(new ImageIcon(joueur.getIconPath()).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
                data[i][0] = iconJoueur;
                data[i][1] = i + 1;
                data[i][2] = "(" + joueur.getPosition().getX() + ", " + joueur.getPosition().getY() + ")";
                data[i][3] = "(" + joueur.getPositionArrivee().getX() + ", " + joueur.getPositionArrivee().getY() + ")";
                data[i][4] = joueur.getNombreBlocage();
                data[i][5] = joueur.getJetons().size();

                List<Color> jetons = joueur.getJetons();
                for (int j = 0; j < couleurs.length; j++) {
                    Color couleur = couleurs[j];
                    long count = jetons.stream().filter(color -> color.equals(couleur)).count();
                    data[i][6 + j] = count;
                }

                i++;
            }

            JTable table = new JTable(new javax.swing.table.DefaultTableModel(data, columnNames) {
                @Override
                public Class<?> getColumnClass(int column) {
                    return column == 0 ? ImageIcon.class : Object.class;
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; 
                }
            });
            table.setFont(new Font("Monospaced", Font.PLAIN, 12));
            table.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 12));
            TableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                            boolean isSelected, boolean hasFocus,
                                                            int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(
                            table, value, isSelected, hasFocus, row, column);
                    label.setFont(new Font("Monospaced", Font.BOLD, 12));
                    label.setHorizontalAlignment(SwingConstants.CENTER);

                    if (column < 6) {
                        label.setBackground(buttonColor);
                    } else {
                        int colorIndex = column - 6; // Index dans le tableau des couleurs
                        if (colorIndex < couleurs.length) {
                            label.setBackground(couleurs[colorIndex]);
                        } else {
                            label.setBackground(Color.LIGHT_GRAY);
                        }
                    }

                    label.setOpaque(true);
                    label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY)); // bordures fines
                    return label;
                }
            };
            for (int col = 0; col < table.getColumnCount(); col++) {
                table.getColumnModel().getColumn(col).setHeaderRenderer(headerRenderer);
            }
            table.setRowHeight(30); 

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            for (int col = 1; col < table.getColumnCount(); col++) {
                table.getColumnModel().getColumn(col).setCellRenderer(centerRenderer);
            }

            JScrollPane scrollPane = new JScrollPane(table);
            settingsDialog.add(scrollPane, BorderLayout.CENTER);

            RoundedButton validateButton = new RoundedButton("Fermer", buttonColor);
            validateButton.setFont(new Font("Monospaced", Font.PLAIN, 18));
            validateButton.setPreferredSize(new Dimension(100, 30));
            validateButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel bottomPanel = new JPanel();
            bottomPanel.setBackground(lightBackground); // Utilise la même couleur de fond que la fenêtre principale
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
        logoutButton.addActionListener(e -> {
            frame.dispose();         
            System.exit(0);          
        });
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

        // Communications button
        JPanel commPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        commPanel.setOpaque(false);
        ImageIcon commIcon = new ImageIcon("Images/communication.png");
        Image commImg = commIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon commScaledIcon = new ImageIcon(commImg);

        JButton commButton = new JButton(commScaledIcon);
        commButton.setPreferredSize(new Dimension(30, 30));
        commButton.setFocusPainted(false);
        commButton.setContentAreaFilled(false);
        commButton.setBorderPainted(false);
        commButton.setMargin(new Insets(0, 0, 0, 0));
        commButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        commButton.addActionListener(e -> {
            JDialog commDialog = new JDialog(frame, "Communication", true);
            commDialog.setSize(400, 450);
            commDialog.setLayout(new BorderLayout());            
            if (communicationArea == null) {
                communicationArea = new JTextArea();
                communicationArea.setEditable(false);
                communicationArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                communicationArea.setMargin(new Insets(10, 10, 10, 10));
                communicationArea.setBackground(Color.WHITE);
                communicationArea.setForeground(Color.BLACK);
            }            // Ajout de bordures de la même couleur que le bouton Start
            JScrollPane scrollPane = new JScrollPane(communicationArea);
            scrollPane.setBorder(BorderFactory.createLineBorder(buttonColor, 3));
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);  // Désactive la barre horizontale
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);  // Active la barre verticale
            
            // Configuration du word wrap
            communicationArea.setLineWrap(true);        
            communicationArea.setWrapStyleWord(true);  
            
            // Panel principal avec marge (avec un nom unique)
            JPanel textAreaPanel = new JPanel(new BorderLayout());
            textAreaPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            textAreaPanel.setBackground(lightBackground);
            textAreaPanel.add(scrollPane, BorderLayout.CENTER);
            commDialog.add(textAreaPanel, BorderLayout.CENTER);

            // Bouton Fermer
            RoundedButton closeButton = new RoundedButton("Fermer", buttonColor);
            closeButton.setFont(new Font("Monospaced", Font.PLAIN, 18));
            closeButton.setPreferredSize(new Dimension(100, 30));
            closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel bottomPanel = new JPanel();
            bottomPanel.setBackground(lightBackground);
            bottomPanel.add(closeButton);
            commDialog.add(bottomPanel, BorderLayout.SOUTH);

            // Action de fermeture
            closeButton.addActionListener(ev -> {
                commDialog.dispose();
            });
            
            // Gestionnaire de fenêtre pour garder la référence à communicationArea
            commDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    // On ne fait rien, on garde la référence à communicationArea
                }
            });
            
            commDialog.setLocationRelativeTo(frame);
            commDialog.setVisible(true);
        });

        commPanel.add(commButton);
        ribbon1.add(commPanel, BorderLayout.CENTER);

        Main.appendToCommunication("helloooo");

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
        boolean[] firstClick = {true}; 

        startButton.addActionListener(e -> {
            try {
                if (firstClick[0]) {
                    System.out.println("Le jeu commence !");
                    appendToCommunication("- Le jeu commence !");
                    List<String> names = new ArrayList<>();
                    for (int i = 0; i < agentsControllers.size(); i++) {
                        names.add("Agent" + (i + 1));
                    }
                    AgentController controller = mainContainer.createNewAgent("Master", "Master", new Object[]{names});
                    controller.start();
                    
                    startButton.setText("Next");
                    firstClick[0] = false;
                } else {
                    System.out.println("Un nouveau tour commence !");
                    appendToCommunication("-----------------------------------------");
                    List<String> names = new ArrayList<>();
                    for (int i = 0; i < agentsControllers.size(); i++) {
                        names.add("Agent" + (i + 1));
                    }
                    AgentController controller = mainContainer.createNewAgent("Master", "Master", new Object[]{names});
                    controller.start();
                }

                // Timer pour vérifier si tous les agents sont déconnectés
                javax.swing.Timer checkAgentsTimer = new javax.swing.Timer(1000, ev -> {
                    boolean allAgentsGone = true;
                    for (AgentController ac : agentsControllers) {
                        try {
                            ac.getState();  // Lance une exception si l'agent n'existe plus
                            allAgentsGone = false;
                            break;
                        } catch (Exception ex) {
                            // Agent n'existe plus
                        }
                    }
                    if (allAgentsGone) {
                        startButton.setText("FIN");
                        startButton.setEnabled(false);
                        ((javax.swing.Timer)ev.getSource()).stop();
                    }
                });
                checkAgentsTimer.start();
                        
                // AMÉLIORATION : Timer de rafraîchissement plus fréquent et plus long
                javax.swing.Timer refreshTimer = new javax.swing.Timer(100, ev -> {
                    rafraichirAffichage(grid);
                });
                refreshTimer.setRepeats(true);
                refreshTimer.start();
                
                // Arrêter le timer après 10 secondes
                new javax.swing.Timer(10000, ev -> {
                    refreshTimer.stop();
                }).start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

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

    public static void rafraichirAffichage(Grille grid) {
        SwingUtilities.invokeLater(() -> {
            try {
                grid.dessinerJoueurs(); // met à jour les positions des agents
                grid.repaint();         // redessine la grille
                grid.revalidate();      // s'assure que la grille est correctement validée
            } catch (Exception e) {
                System.err.println("Erreur lors du rafraîchissement : " + e.getMessage());
            }
        });
    }

    public static Color CouleurPastel(Color color, float factor) {
        int r = Math.min(255, (int)(color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int)(color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int)(color.getBlue() + (255 - color.getBlue()) * factor));
        return new Color(r, g, b);
    }

    public static Joueur createRandomJoueur(String iconPath, Grille grille, List<Position> positionsOccupees) {
        Random rand = new Random();
        Position position;

        // On génère une position non occupée
        do {
            int x = rand.nextInt(5);
            int y = rand.nextInt(7);
            position = new Position(x, y);
        } while (positionsOccupees.contains(position));
        positionsOccupees.add(position);

        Position positionBut;
        // Cette boucle existe pour éviter d'avoir des chemins supérieurs à 3 cases
        do {
            positionBut = new Position(rand.nextInt(5), rand.nextInt(7));
        } while ( Math.abs(position.getX() - positionBut.getX()) + Math.abs(position.getY() - positionBut.getY()) < 3);
        
        List<Color> jetons = new ArrayList<>();
        Joueur joueur = new Joueur(iconPath, position, positionBut, jetons, grille);

        List<CaseChemin> chemin = joueur.getChemin();
        int tailleChemin = chemin.size();
        int pourcentageBonus = 10;
        int bonus = (int) Math.ceil(tailleChemin * (pourcentageBonus / 100.0));
        int nombreJetons = tailleChemin + bonus;
        
        // On complique la tâche aux agents en ne leur donnant pas les jetons dont ils ont besoin
        Set<Color> couleursChemin = chemin.stream()
            .map(CaseChemin::getCouleur)
            .collect(Collectors.toSet());
        Color couleurExclue = couleursChemin.stream()
            .skip(rand.nextInt(couleursChemin.size()))
            .findFirst()
            .orElse(null);
        List<Color> couleursDisponibles = Arrays.stream(Grille.getPastelcolors())
            .filter(c -> !c.equals(couleurExclue))
            .collect(Collectors.toList());
        for (int i = 0; i < nombreJetons; i++) {
            Color randomColor = couleursDisponibles.get(rand.nextInt(couleursDisponibles.size()));
            jetons.add(randomColor);
        }
        joueur.setJetons(jetons);

        grille.ajouterJoueur(joueur);
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