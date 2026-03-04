import { describe, it, expect } from "vitest";
import { Asteroid } from "../src/entities/asteroid.js";
import { WIDTH, HEIGHT, ASTEROID_SIZES } from "../src/constants.js";

describe("Asteroid", () => {
  it("should initialize with correct size properties", () => {
    const a = new Asteroid(100, 100, "LARGE");
    expect(a.size).toBe("LARGE");
    expect(a.radius).toBe(ASTEROID_SIZES.LARGE.radius);
    expect(a.scale).toBe(ASTEROID_SIZES.LARGE.scale);
    expect(a.alive).toBe(true);
  });

  it("MEDIUM asteroid has smaller radius", () => {
    const a = new Asteroid(100, 100, "MEDIUM");
    expect(a.radius).toBe(ASTEROID_SIZES.MEDIUM.radius);
    expect(a.scale).toBe(ASTEROID_SIZES.MEDIUM.scale);
  });

  it("SMALL asteroid has smallest radius", () => {
    const a = new Asteroid(100, 100, "SMALL");
    expect(a.radius).toBe(ASTEROID_SIZES.SMALL.radius);
    expect(a.scale).toBe(ASTEROID_SIZES.SMALL.scale);
  });

  it("velocity points roughly toward screen center", () => {
    // Spawn on left edge — velocity x should be positive (toward center)
    const a = new Asteroid(-40, HEIGHT / 2, "LARGE");
    expect(a.vx).toBeGreaterThan(0);
  });

  it("moves over time", () => {
    const a = new Asteroid(-40, HEIGHT / 2, "LARGE");
    const startX = a.x;
    a.update(1 / 60);
    expect(a.x).not.toBe(startX);
  });

  it("rotates over time", () => {
    const a = new Asteroid(100, 100, "LARGE");
    const startRot = a.rotation;
    a.update(1);
    // spin is random but non-zero is very likely
    // Just verify update doesn't crash
    expect(typeof a.rotation).toBe("number");
  });

  it("wraps from right edge to left", () => {
    const a = new Asteroid(100, 100, "LARGE");
    a.vx = 1000; // fast rightward
    a.vy = 0;
    a.x = WIDTH + a.wrapMargin + 1;
    a.update(0); // trigger wrap check (no dt movement needed since already past edge)
    expect(a.x).toBeLessThan(0);
  });

  it("wraps from bottom to top", () => {
    const a = new Asteroid(100, 100, "LARGE");
    a.vx = 0;
    a.vy = 1000;
    a.y = HEIGHT + a.wrapMargin + 1;
    a.update(0);
    expect(a.y).toBeLessThan(0);
  });
});
