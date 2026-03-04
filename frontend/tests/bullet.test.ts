import { describe, it, expect } from "vitest";
import { Bullet } from "../src/entities/bullet.js";
import { WIDTH, HEIGHT, BULLET_LIFETIME } from "../src/constants.js";

describe("Bullet", () => {
  it("should move in direction of rotation", () => {
    // rotation=0 means ship points up, bullet should move upward (negative y)
    const b = new Bullet(640, 360, 0, { x: 0, y: 0 });
    b.update(1 / 60);
    expect(b.y).toBeLessThan(360);
  });

  it("inherits ship velocity", () => {
    const b = new Bullet(640, 360, 0, { x: 100, y: 0 });
    // vx should include ship velocity
    expect(b.vx).toBeGreaterThan(0);
  });

  it("wraps from right to left", () => {
    const b = new Bullet(WIDTH + 1, 360, 90, { x: 0, y: 0 });
    b.update(1 / 60);
    // After wrapping and a tiny step, should be near left edge
    expect(b.x).toBeLessThan(WIDTH);
  });

  it("expires after lifetime", () => {
    const b = new Bullet(640, 360, 0, { x: 0, y: 0 });
    expect(b.alive).toBe(true);
    b.update(BULLET_LIFETIME + 0.1);
    expect(b.alive).toBe(false);
  });

  it("stays alive before lifetime expires", () => {
    const b = new Bullet(640, 360, 0, { x: 0, y: 0 });
    b.update(BULLET_LIFETIME - 0.1);
    expect(b.alive).toBe(true);
  });
});
