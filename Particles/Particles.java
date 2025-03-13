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
	private static final int SPAWN_COUNT = 10;
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
	private float particleScale = 2;

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
					particles[i].draw(g2d);
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
							Vector2 distanceVec = position.getDistance(mousePosition);

							float distance = (float)Math.abs(distanceVec.x) + (float)Math.abs(distanceVec.y);

							if (distance > 200.0f) continue;

							float force = Math.min(20.0f / (distance + 0.000001f) * p.scale, 1.0f);
							float angle = (float)Math.atan2(distanceVec.y, distanceVec.x);

							float fx = (float)Math.cos(angle) * force;
							float fy = (float)Math.sin(angle) * force;

							p.addVelocity(-fx, -fy);
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
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			particleScale += 1.0f;
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			particleScale = (float)Math.max(particleScale - 1.0f, 2.0f);
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
				ArrayList<Particle> neighbors = tree.query(new Bounds(position.x - p.scale, position.y - p.scale, p.scale * 2, p.scale * 2));

				for (Particle other : neighbors) {
					p.collide(other);
				}
			}

			p.update();
			p.addVelocity(GRAVITY);
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

			particles[count++] = new Particle(position, velocity, particleScale);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			Particles example = new Particles();
			example.setVisible(true);
		});
	}
}