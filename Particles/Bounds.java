package Particles;

public class Bounds {
	public float x, y, w, h;

	public Bounds(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public boolean contains(Vector2 position) {
		return this.x <= position.x &&
			position.x <= this.x + this.w &&
			this.y <= position.y &&
			position.y <= this.y + this.h;
	}

	public boolean intersects(Bounds other) {
		return this.x + this.w >= other.x &&
			other.x + other.w >= this.x &&
			this.y + this.h >= other.y &&
			other.y + other.h >= this.y;
	}
}
