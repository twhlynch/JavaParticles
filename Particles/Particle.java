package Particles;

public class Particle {
	private Vector2 position;
	private Vector2 velocity;
	public boolean hit;

	public Particle(Vector2 position, Vector2 velocity) {
		this.position = position;
		this.velocity = velocity;
	}

	public void update() {
		position.x += velocity.x;
      position.y += velocity.y;
		velocity.x *= 0.995f;
		velocity.y *= 0.995f;
   }

	public void updateGravity(Vector2 G) {
		velocity.x += G.x;
		velocity.y += G.y;
	}

	public void boundsCheck(Bounds bounds) {
		if (position.x < 0.0f && velocity.x < 0.0f) {
			velocity.x = -velocity.x * 0.9f;
			position.x = 0.0f;
		} else if (position.x > bounds.w && velocity.x > 0.0f) {
			velocity.x = -velocity.x * 0.9f;
			position.x = bounds.w;
		}

		if (position.y < 0.0f && velocity.y < 0.0f) {
			velocity.y = -velocity.y * 0.9f;
			position.y = 0.0f;
		} else if (position.y > bounds.h && velocity.y > 0.0f) {
			velocity.y = -velocity.y * 0.9f;
			position.y = bounds.h;
		}
	}

	public Vector2 getPosition() {
      return position;
   }

	public void addVelocity(Vector2 velocity) {
		this.velocity.x += velocity.x;
      this.velocity.y += velocity.y;
	}
}