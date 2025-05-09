import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Colored Trails");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 20, 20)); // Les marges de l'écran d'accueil

        Grille grid = new Grille(5, 7);
        Color buttonColor = grid.getRandomColorUsed();
        Color lightBackground = lightenColor(buttonColor, 0.6f);
        mainPanel.setBackground(lightBackground);
        mainPanel.setOpaque(true);

        JPanel ribbon1 = new JPanel();
        ribbon1.setPreferredSize(new Dimension(600, 40)); 
        ribbon1.setOpaque(false);
        ribbon1.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0)); 

        ImageIcon icon = new ImageIcon("Images/settings.png");
        Image image = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(image);
        JButton cornerButton = new JButton(scaledIcon);
        cornerButton.setPreferredSize(new Dimension(30, 30));
        cornerButton.setFocusPainted(false);
        cornerButton.setContentAreaFilled(false);
        cornerButton.setBorderPainted(false);
        cornerButton.setMargin(new Insets(0, 0, 0, 0));
        ribbon1.add(cornerButton);

        JPanel ribbon2 = new JPanel();
        ribbon2.setPreferredSize(new Dimension(600, 40));
        ribbon2.setOpaque(false);
        ribbon2.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        RoundedButton startButton = new RoundedButton("Start", buttonColor);
        startButton.setFont(new Font("Monospaced", Font.BOLD, 25));
        startButton.setPreferredSize(new Dimension(120, 40));
        ribbon2.add(startButton);

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
    }

    public static Color lightenColor(Color color, float factor) {
        int r = Math.min(255, (int)(color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int)(color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int)(color.getBlue() + (255 - color.getBlue()) * factor));
        return new Color(r, g, b);
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
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            g2.setFont(getFont());

            FontMetrics metrics = g2.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(getText())) / 2;
            int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
            g2.setColor(getForeground());
            g2.drawString(getText(), x, y);

            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            
        }
    }
}
