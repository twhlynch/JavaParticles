package Particles;

public class Vector2 {
	public float x = 0, y = 0;
	public Vector2(float x, float y) {
      this.x = x;
      this.y = y;
   }
   public Vector2() {
      this.x = this.y = 0;
   }
   public void set(float x, float y) {
      this.x = x;
      this.y = y;
   }
   public void add(Vector2 other) {
      this.x += other.x;
      this.y += other.y;
   } 
   public void add(float val) {
      this.x += val;
      this.y += val;
   } 
   public void multiply(float val) {
      this.x *= val;
      this.y *= val;
   } 
}