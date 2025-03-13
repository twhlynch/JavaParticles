package Particles;

import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

enum MouseState {
	None,
	Left,
	Right
}

public class Particles extends JFrame implements KeyListener {
	private static final int MAX_PARTICLES = 1000000;
	private static final int SPAWN_COUNT = 100;
	private static final Bounds defaultBounds = new Bounds(0.0f, 0.0f, 800.0f, 800.0f);
	private static final boolean DO_COLLISION = true;
	private static final Vector2 GRAVITY = new Vector2(0.0f, 0.02f);
	private boolean debug = false;
	private Particle[] particles = new Particle[MAX_PARTICLES];
	private int count = 0;
	private JPanel panel;
	private BufferedImage buffer;
	private MouseState mouseState = MouseState.None;
	private Vector2 mousePosition = new Vector2();
	private long lastFrameTime = System.nanoTime();

	public Particles() {
		setTitle("Particle Simulation");
		setSize((int)defaultBounds.w, (int)defaultBounds.h);
		setUndecorated(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);

		buffer = new BufferedImage((int)defaultBounds.w, (int)defaultBounds.h, BufferedImage.TYPE_INT_ARGB);

		panel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				Graphics2D g2d = buffer.createGraphics();

				g2d.setColor(Color.BLACK);
				g2d.fillRect((int)defaultBounds.x, (int)defaultBounds.y, (int)defaultBounds.w, (int)defaultBounds.h);

				// draw particles
				for (int i = 0; i < count; i++) {
					Particle p = particles[i];
					Vector2 position = p.getPosition();
					g2d.setColor(Color.WHITE);
					if (p.hit) g2d.setColor(Color.RED);
               g2d.drawLine((int)position.x, (int)position.y, (int)position.x, (int)position.y);
				}
				g2d.setColor(Color.WHITE);

				// draw tree
				if (debug) {
					Tree tree = new Tree(defaultBounds);
					for (int i = 0; i < count; i++) {
						Particle p = particles[i];
						tree.insert(p);
					}
					tree.draw(g2d);
				}

				g2d.drawString("Particles: " + count, 10, 20);

				long currentFrameTime = System.nanoTime();
				float timeSince = (float)(currentFrameTime - lastFrameTime);
				g2d.drawString(String.format("fps: %.2f", 1000.0f / (timeSince / 1000000.0f)), 10, 40);
				lastFrameTime = currentFrameTime;

				g2d.dispose();
				g.drawImage(buffer, 0, 0, this);

				for (int i = 0; i < count; i++) {
					particles[i].hit = false;
				}
			}
		};

		panel.addMouseListener(
			new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
					mousePosition.set(e.getX(), e.getY());
               if (SwingUtilities.isLeftMouseButton(e)) {
                  mouseState = MouseState.Left;
               } else if (SwingUtilities.isRightMouseButton(e)) {
                  mouseState = MouseState.Right;
               }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
					mousePosition.set(e.getX(), e.getY());
               mouseState = MouseState.None;
            }
         }
		);

		panel.addMouseMotionListener(
			new MouseAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					mousePosition.set(e.getX(), e.getY());
            }
				public void mouseDragged(MouseEvent e) {
					mousePosition.set(e.getX(), e.getY());
            }
			}
		);

		add(panel);

		Timer timer = new Timer(0,
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (mouseState == MouseState.Left) {
						addParticle(mousePosition, true);
					} else if (mouseState == MouseState.Right) {
						for (int i = 0; i < count; i++) {
							Particle p = particles[i];
							Vector2 position = p.getPosition();

							float dx = position.x - mousePosition.x;
							float dy = position.y - mousePosition.y;
							float distance = (float)Math.sqrt(dx * dx + dy * dy);

							if (distance > 200.0f) continue;

							float force = Math.min(20.0f / (distance + 0.000001f), 1.0f);
							float angle = (float)Math.atan2(dy, dx);

							float fx = (float)Math.cos(angle) * force;
							float fy = (float)Math.sin(angle) * force;

							p.updateGravity(new Vector2(-fx, -fy));
						}
					}
					updateParticles();
					panel.repaint();
				}
			}
		);
		timer.start();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_R) {
			count = 0;
			particles = new Particle[MAX_PARTICLES];
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			System.exit(0);
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			debug = !debug;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

	public void updateParticles() {
		Tree tree = new Tree(defaultBounds);
		if (DO_COLLISION) {
			for (int i = 0; i < count; i++) {
				Particle p = particles[i];
				tree.insert(p);
			}
		}

		for (int i = 0; i < count; i++) {
			Particle p = particles[i];
			Vector2 position = p.getPosition();

			if (DO_COLLISION) {
				ArrayList<Particle> neighbors = tree.query(new Bounds(position.x, position.y, 1.0f, 1.0f));

				for (Particle other : neighbors) {
					if (other == p) continue;

					Vector2 otherPosition = other.getPosition();

					float dx = position.x - otherPosition.x;
					float dy = position.y - otherPosition.y;
					float distance = Math.abs(dx - dx) + Math.abs(dy - dy);
					
					float force = 0.05f * distance;
					float angle = (float)Math.atan2(dy, dx);

					float fx = (float)Math.cos(angle) * force;
					float fy = (float)Math.sin(angle) * force;

					p.addVelocity(fx, fy);
					other.addVelocity(-fx, -fy);

					p.hit = true;
					other.hit = true;
				}
			}

			p.update();
			p.updateGravity(GRAVITY);
			p.boundsCheck(defaultBounds);
		}
	}

	public void addParticle(Vector2 basePosition, boolean isMoving) {
		for (int i = 0; i < SPAWN_COUNT; i++) {
			if (count >= MAX_PARTICLES) return;

			Vector2 position = new Vector2(basePosition.x, basePosition.y);
			Vector2 velocity = new Vector2(0.0f, 0.0f);

			float angle = (float)Math.random() * (float)Math.PI * 2.0f;
			float d = (float)Math.random() * 30.0f;
			float x = (float)Math.cos(angle) * d;
			float y = (float)Math.sin(angle) * d;
			position.x += x;
			position.y += y;

			if (isMoving) {
				velocity.x = x / 30.0f;
				velocity.y = y / 30.0f;
			}

			particles[count++] = new Particle(position, velocity);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			Particles example = new Particles();
			example.setVisible(true);
		});
	}
}