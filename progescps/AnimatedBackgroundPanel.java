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

    private final List<Particle> particles;
    private final Timer timer;

    public AnimatedBackgroundPanel() {
        particles = new ArrayList<>();
        Random random = new Random();

        // Create a set of particles
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle(random));
        }

        // Timer to update and repaint the animation
        timer = new Timer(40, new ActionListener() {
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
    public void stop() {
        timer.stop();
    }

    private void updateParticles() {
        int width = getWidth();
        int height = getHeight();
        for (Particle p : particles) {
            p.update(width, height);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Dark background
        g2d.setColor(new Color(25, 25, 35));
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
        double x, y;
        double vx, vy;
        int size;
        float alpha;
        Color color;
        private final Random random;

        Particle(Random random) {
            this.random = random;
            this.x = random.nextInt(800);
            this.y = random.nextInt(600);
            this.vx = -0.5 + random.nextDouble(); // Slow movement
            this.vy = -0.5 + random.nextDouble();
            this.size = random.nextInt(3) + 2; // Small sizes
            this.alpha = random.nextFloat() * 0.5f; // Subtle alpha
            this.color = new Color(200, 200, 255, random.nextInt(150) + 50);
        }

        void update(int width, int height) {
            x += vx;
            y += vy;

            // Reset particle if it goes off-screen
            if (x < -size || x > width || y < -size || y > height) {
                x = random.nextInt(width);
                y = height + random.nextInt(50);
            }
        }
    }
}