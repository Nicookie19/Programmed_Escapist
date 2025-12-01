package progescps;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A panel with an animated background of floating particles.
 */
public class AnimatedBackgroundPanel extends JPanel {

    private static final int PARTICLE_COUNT = 250;
    private static final int TIMER_DELAY = 30; // Approx 25 FPS
    private static final Color BACKGROUND_COLOR = new Color(25, 25, 35);

    private final List<Particle> particles;
    private final Timer timer;

    /**
     * Constructs the AnimatedBackgroundPanel, initializing particles and the animation timer.
     */
    public AnimatedBackgroundPanel() {
        particles = new ArrayList<>();
        
        // A single Random instance to be shared
        final Random random = new Random();
        
        // Pre-defined colors for particles
        final Color[] starColors = {
            new Color(200, 200, 255, random.nextInt(150) + 50), // Bluish-white
            new Color(255, 255, 255, random.nextInt(150) + 50), // White
            new Color(255, 255, 224, random.nextInt(150) + 50)  // Pale Yellow
        };

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new Particle(random, starColors));
        }

        // Timer to update and repaint the animation
        timer = new Timer(TIMER_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateParticles();
                repaint();
            }
        });
        timer.start();
    }

    /**
     * Stops the animation timer.
     */
    /**
     * Stops the animation timer.
     */
    public void stop() {
        timer.stop();
    }

    /**
     * Updates the position of each particle in the animation.
     */
    private void updateParticles() {
        int width = getWidth();
        int height = getHeight();
        for (Particle p : particles) {
            p.update(width, height);
        }
    }

    /**
     * Paints the background and particles on the panel.
     *
     * @param g The Graphics context used for painting.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Use pre-defined background color
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Set rendering hints for quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw each particle
        for (Particle p : particles) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p.alpha));
            g2d.setColor(p.color);
            g2d.fillOval((int) p.x, (int) p.y, p.size, p.size);
        }

        g2d.dispose();
    }

    /**
     * Represents a single particle in the animation.
     */
    private static class Particle {
        private static final double MIN_VELOCITY = -0.5;
        private static final double VELOCITY_RANGE = 1.0;
        private static final int MIN_SIZE = 2;
        private static final int SIZE_RANGE = 3;
        private static final float MAX_INITIAL_ALPHA = 0.5f;
        private static final float TWINKLE_FACTOR = 0.1f;
        private static final float MIN_ALPHA = 0.1f;
        private static final float MAX_ALPHA = 0.7f;
        double x, y;
        double vx, vy;
        int size;
        float alpha;
        Color color;
        private final Random random;

        /**
         * Constructs a Particle with random properties.
         *
         * @param random     The Random object used for generating random values.
         * @param starColors The array of Color objects to choose from for the particle's color.
         */
        Particle(Random random, Color[] starColors) {
            this.random = random;
            // Initialize with placeholder values, will be reset in update
            this.x = -1;
            this.y = -1;
            this.vx = MIN_VELOCITY + random.nextDouble() * VELOCITY_RANGE;
            this.vy = MIN_VELOCITY + random.nextDouble() * VELOCITY_RANGE;
            this.size = random.nextInt(SIZE_RANGE) + MIN_SIZE;
            this.alpha = random.nextFloat() * MAX_INITIAL_ALPHA;
            this.color = starColors[random.nextInt(starColors.length)];
        }

        /**
         * Updates the particle's position and handles its respawn when it goes off-screen.
         *
         * @param width  The width of the panel.
         * @param height The height of the panel.
         */
        void update(int width, int height) {
            if (width == 0 || height == 0) return; // Avoid division by zero or issues on init

            // Initialize particle position if it's the first time
            if (x == -1 && y == -1) {
                x = random.nextInt(width);
                y = random.nextInt(height);
            }

            x += vx;
            y += vy;

            // Add a twinkling effect
            alpha += (random.nextFloat() - 0.5f) * TWINKLE_FACTOR;
            alpha = Math.max(MIN_ALPHA, Math.min(MAX_ALPHA, alpha));

            // Reset particle if it goes off-screen, making it appear from a random edge
            if (x < -size || x > width || y < -size || y > height) {
                int edge = random.nextInt(4);
                switch (edge) {
                    case 0: // Left edge
                        x = -size; y = random.nextInt(height);
                        break;
                    case 1: // Right edge
                        x = width + size; y = random.nextInt(height);
                        break;
                    case 2: // Top edge
                        x = random.nextInt(width); y = -size;
                        break;
                    case 3: // Bottom edge
                        x = random.nextInt(width); y = height + size;
                        break;
                }
            }
        }
    }
}