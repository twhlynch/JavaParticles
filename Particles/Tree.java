package Particles;

import java.util.ArrayList;

public class Tree {
	private static final int MAX_LEAFS = 4;

	private Bounds bounds;
	private Particle[] particles = new Particle[MAX_LEAFS];
	private int count;
	private boolean divided;
	private Tree NE, NW, SE, SW;

	public Tree(Bounds bounds) {
		this.bounds = bounds;
	}

	private void subdivide() {
		float x = this.bounds.x;
		float y = this.bounds.y;
		float w = this.bounds.w / 2.0f;
		float h = this.bounds.h / 2.0f;

		this.NE = new Tree(new Bounds(x + w, y, w, h));
		this.NW = new Tree(new Bounds(x, y, w, h));
		this.SE = new Tree(new Bounds(x + w, y + h, w, h));
		this.SW = new Tree(new Bounds(x, y + h, w, h));

		this.divided = true;
	}

	public boolean insert(Particle p) {
		if (!this.bounds.contains(p.getPosition())) {
			return false;
      }

		if (count < MAX_LEAFS) {
			this.particles[this.count++] = p;
			return true;
		}

      if (!this.divided) {
         this.subdivide();
      }

      if (this.NE.insert(p) || this.NW.insert(p) || 
			this.SE.insert(p) || this.SW.insert(p)) {
			return true;
		}

		return false;
	}

	public ArrayList<Particle> query(Bounds bounds) {
		if (!this.bounds.intersects(bounds)) {
			return new ArrayList<>();
      }

		ArrayList<Particle> results = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			Particle p = this.particles[i];
         if (bounds.contains(p.getPosition())) {
            results.add(p);
         }
		}

		if (this.divided) {
			results.addAll(this.NE.query(bounds));
         results.addAll(this.NW.query(bounds));
         results.addAll(this.SE.query(bounds));
         results.addAll(this.SW.query(bounds));
		}

		return results;
	}
}
