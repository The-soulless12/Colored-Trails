import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class Grille extends JPanel {

    private static final int CELL_SIZE = 70;
    private static final Color BORDER_COLOR = new Color(100, 100, 100, 100);
    private static final Color[] pastelColors = generatePastelColors(5);

    public Grille(int rows, int cols) {
        super();
        setLayout(new GridLayout(rows, cols, 10, 10));
        setOpaque(false);

        RoundedPanel gridContainer = new RoundedPanel(30, new Color(240, 240, 240));
        gridContainer.setLayout(new GridLayout(rows, cols, 10, 10));
        gridContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Random rand = new Random();
        for (int i = 0; i < rows * cols; i++) {
            Color fill = pastelColors[rand.nextInt(pastelColors.length)];
            gridContainer.add(new RoundedCellPanel(fill));
        }

        setLayout(new BorderLayout());
        add(gridContainer, BorderLayout.CENTER);
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
}
