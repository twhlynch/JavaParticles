package Particles;

import java.awt.*;

public class Particle {
	private Vector2 position;
	private Vector2 velocity;
	public boolean hit;
	public float scale;

	public Particle(Vector2 position, Vector2 velocity, float scale) {
		this.position = position;
		this.velocity = velocity;
		this.scale = scale;
	}

	public void update() {
		position.add(velocity);
		velocity.multiply(0.995f - 0.0001f * scale);
   }

	public void boundsCheck(Bounds bounds) {
		if (position.x < scale / 2 && velocity.x < 0.0f) {
			velocity.x = -velocity.x * 0.9f;
			position.x = scale / 2;
		} else if (position.x > bounds.w - scale / 2 && velocity.x > 0.0f) {
			velocity.x = -velocity.x * 0.9f;
			position.x = bounds.w;
		}

		if (position.y < scale / 2 && velocity.y < 0.0f) {
			velocity.y = -velocity.y * 0.9f;
			position.y = scale / 2;
		} else if (position.y > bounds.h - scale / 2 && velocity.y > 0.0f) {
			velocity.y = -velocity.y * 0.9f;
			position.y = bounds.h;
		}
	}

	public void draw(Graphics2D g2d) {
		if (this.scale < 2.0f) {
			g2d.drawLine((int)position.x, (int)position.y, (int)position.x, (int)position.y);
		} else {
			g2d.setColor(this.hit ? Color.RED : Color.WHITE);
			g2d.fillOval((int)(position.x - scale / 2), (int)(position.y - scale / 2), (int)(scale), (int)(scale));
		}
	}

	public void collide(Particle other) {
		if (other == this) return;

		Vector2 distance = this.position.getDistance(other.getPosition());

		float force = 0.005f * ((float)Math.abs(distance.x) + (float)Math.abs(distance.y));
		float angle = (float)Math.atan2(distance.y, distance.x);

		float fx = (float)Math.cos(angle) * force;
		float fy = (float)Math.sin(angle) * force;

		this.addVelocity(fx, fy);
		other.addVelocity(-fx, -fy);

		this.hit = true;
		other.hit = true;
	}

	public Vector2 getPosition() {
      return position;
   }

	public void addVelocity(Vector2 velocity) {
		this.velocity.add(velocity);
	}
	public void addVelocity(float x, float y) {
		this.velocity.x += x;
		this.velocity.y += y;
	}
}