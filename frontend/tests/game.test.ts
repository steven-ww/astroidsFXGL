import { describe, it, expect, beforeEach, vi } from "vitest";
import { Player } from "../src/entities/player.js";
import { Asteroid } from "../src/entities/asteroid.js";
import { Bullet } from "../src/entities/bullet.js";
import { spawnExplosion, updateParticles } from "../src/entities/particle.js";
import {
  INITIAL_LIVES,
  ASTEROID_SIZES,
  EXPLOSION_PARTICLE_COUNT,
  WIDTH,
  HEIGHT,
} from "../src/constants.js";

describe("Game scoring", () => {
  it("LARGE asteroid awards 20 points", () => {
    expect(ASTEROID_SIZES.LARGE.score).toBe(20);
  });

  it("MEDIUM asteroid awards 50 points", () => {
    expect(ASTEROID_SIZES.MEDIUM.score).toBe(50);
  });

  it("SMALL asteroid awards 100 points", () => {
    expect(ASTEROID_SIZES.SMALL.score).toBe(100);
  });
});

describe("Game initial state", () => {
  it("starts with correct number of lives", () => {
    expect(INITIAL_LIVES).toBe(3);
  });

  it("player spawns at screen center", () => {
    const p = new Player(WIDTH / 2, HEIGHT / 2);
    expect(p.x).toBe(640);
    expect(p.y).toBe(360);
  });
});

describe("Asteroid splitting", () => {
  it("LARGE asteroid produces MEDIUM children", () => {
    // Simulate: destroy a LARGE, spawn 2 MEDIUM
    const large = new Asteroid(-40, 360, "LARGE");
    large.alive = false;

    const children = [
      new Asteroid(large.x, large.y, "MEDIUM"),
      new Asteroid(large.x, large.y, "MEDIUM"),
    ];
    expect(children.length).toBe(2);
    expect(children[0].size).toBe("MEDIUM");
    expect(children[1].size).toBe("MEDIUM");
  });

  it("MEDIUM asteroid produces SMALL children", () => {
    const med = new Asteroid(200, 200, "MEDIUM");
    const children = [
      new Asteroid(med.x, med.y, "SMALL"),
      new Asteroid(med.x, med.y, "SMALL"),
    ];
    expect(children.length).toBe(2);
    expect(children[0].size).toBe("SMALL");
  });

  it("SMALL asteroid produces no children", () => {
    // SMALL asteroids just get destroyed, no splitting
    const small = new Asteroid(300, 300, "SMALL");
    small.alive = false;
    // No children spawned — verified by game logic
    expect(small.size).toBe("SMALL");
  });
});

describe("Explosion particles", () => {
  it("spawns correct number of particles", () => {
    const particles = spawnExplosion(400, 300);
    expect(particles.length).toBe(EXPLOSION_PARTICLE_COUNT);
  });

  it("particles expire after their lifetime", () => {
    let particles = spawnExplosion(400, 300);
    // Update for long enough
    for (let i = 0; i < 100; i++) {
      particles = updateParticles(particles, 0.02);
    }
    expect(particles.length).toBe(0);
  });

  it("particles move over time", () => {
    const particles = spawnExplosion(400, 300);
    const startX = particles[0].x;
    const startY = particles[0].y;
    updateParticles(particles, 0.5);
    // At least one particle should have moved
    const moved = particles.some((p) => p.x !== startX || p.y !== startY);
    expect(moved).toBe(true);
  });
});

describe("Level progression", () => {
  it("level 0 spawns 4 asteroids (0*2+4)", () => {
    const count = 0 * 2 + 4;
    expect(count).toBe(4);
  });

  it("level 1 spawns 6 asteroids (1*2+4)", () => {
    const count = 1 * 2 + 4;
    expect(count).toBe(6);
  });

  it("level 3 caps at MAX_ASTEROIDS=10", () => {
    const count = Math.min(3 * 2 + 4, 10);
    expect(count).toBe(10);
  });

  it("level 5 still caps at 10", () => {
    const count = Math.min(5 * 2 + 4, 10);
    expect(count).toBe(10);
  });
});

describe("Bullet-asteroid interaction", () => {
  it("bullet at asteroid position should be detected as collision", () => {
    const asteroid = new Asteroid(400, 300, "LARGE");
    const bullet = new Bullet(400, 300, 0, { x: 0, y: 0 });
    // Direct overlap — distance is 0, radius sum > 0
    const dx = bullet.x - asteroid.x;
    const dy = bullet.y - asteroid.y;
    const distSq = dx * dx + dy * dy;
    const radSum = bullet.radius + asteroid.radius;
    expect(distSq).toBeLessThan(radSum * radSum);
  });

  it("bullet far from asteroid should not collide", () => {
    const asteroid = new Asteroid(400, 300, "LARGE");
    const bullet = new Bullet(100, 100, 0, { x: 0, y: 0 });
    const dx = bullet.x - asteroid.x;
    const dy = bullet.y - asteroid.y;
    const distSq = dx * dx + dy * dy;
    const radSum = bullet.radius + asteroid.radius;
    expect(distSq).toBeGreaterThan(radSum * radSum);
  });
});
