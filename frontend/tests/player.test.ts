import { describe, it, expect } from "vitest";
import { Player } from "../src/entities/player.js";
import { WIDTH, HEIGHT } from "../src/constants.js";

describe("Player", () => {
  it("should initialize at given position", () => {
    const p = new Player(100, 200);
    expect(p.x).toBe(100);
    expect(p.y).toBe(200);
    expect(p.rotation).toBe(0);
    expect(p.alive).toBe(true);
    expect(p.exploded).toBe(false);
  });

  it("should not move without thrust", () => {
    const p = new Player(640, 360);
    p.update(1 / 60);
    expect(p.x).toBe(640);
    expect(p.y).toBe(360);
  });

  it("turnRight increases rotation", () => {
    const p = new Player(0, 0);
    const before = p.rotation;
    p.turnRight(1 / 60);
    expect(p.rotation).toBeGreaterThan(before);
  });

  it("turnLeft decreases rotation", () => {
    const p = new Player(0, 0);
    const before = p.rotation;
    p.turnLeft(1 / 60);
    expect(p.rotation).toBeLessThan(before);
  });

  it("thrustOn adds velocity and sets thrusting flag", () => {
    const p = new Player(640, 360);
    p.thrustOn(1 / 60);
    expect(p.thrusting).toBe(true);
    // With rotation=0, thrust should add negative y velocity (upward)
    expect(p.vy).toBeLessThan(0);
  });

  it("thrustOff clears thrusting flag", () => {
    const p = new Player(0, 0);
    p.thrustOn(1 / 60);
    p.thrustOff();
    expect(p.thrusting).toBe(false);
  });

  it("wraps from right edge to left", () => {
    const p = new Player(WIDTH + 1, 360);
    p.update(1 / 60);
    expect(p.x).toBe(0);
  });

  it("wraps from left edge to right", () => {
    const p = new Player(-1, 360);
    p.update(1 / 60);
    expect(p.x).toBe(WIDTH);
  });

  it("wraps from bottom to top", () => {
    const p = new Player(640, HEIGHT + 1);
    p.update(1 / 60);
    expect(p.y).toBe(0);
  });

  it("wraps from top to bottom", () => {
    const p = new Player(640, -1);
    p.update(1 / 60);
    expect(p.y).toBe(HEIGHT);
  });

  it("explode sets exploded flag and creates fragments", () => {
    const p = new Player(640, 360);
    p.explode();
    expect(p.exploded).toBe(true);
    expect(p.fragments.length).toBe(3);
  });

  it("explode does nothing if already exploded", () => {
    const p = new Player(640, 360);
    p.explode();
    const fragCount = p.fragments.length;
    p.explode();
    expect(p.fragments.length).toBe(fragCount);
  });

  it("does not rotate when exploded", () => {
    const p = new Player(0, 0);
    p.explode();
    const rot = p.rotation;
    p.turnLeft(1 / 60);
    expect(p.rotation).toBe(rot);
  });

  it("does not thrust when exploded", () => {
    const p = new Player(0, 0);
    p.explode();
    p.thrustOn(1 / 60);
    expect(p.vx).toBe(0);
    expect(p.vy).toBe(0);
  });

  it("respawn resets state and enables invincibility", () => {
    const p = new Player(640, 360);
    p.explode();
    p.respawn(100, 200);
    expect(p.x).toBe(100);
    expect(p.y).toBe(200);
    expect(p.exploded).toBe(false);
    expect(p.invincible).toBe(true);
    expect(p.vx).toBe(0);
    expect(p.vy).toBe(0);
    expect(p.fragments.length).toBe(0);
  });

  it("invincibility ends after duration", () => {
    const p = new Player(640, 360);
    p.invincible = true;
    p.invincibilityTimer = 0;
    // Simulate 3+ seconds
    p.update(3.1);
    expect(p.invincible).toBe(false);
    expect(p.opacity).toBe(1);
  });

  it("getNosePosition returns correct offset for rotation=0", () => {
    const p = new Player(640, 360);
    const nose = p.getNosePosition(14);
    expect(nose.x).toBeCloseTo(640, 1);
    expect(nose.y).toBeCloseTo(346, 1); // 360 - 14
  });

  it("getVelocity returns current velocity", () => {
    const p = new Player(0, 0);
    p.vx = 5;
    p.vy = -3;
    const v = p.getVelocity();
    expect(v.x).toBe(5);
    expect(v.y).toBe(-3);
  });

  it("fragments drift and expire", () => {
    const p = new Player(640, 360);
    p.explode();
    expect(p.fragments.length).toBe(3);
    // Simulate enough time for fragments to expire (1.5s)
    for (let i = 0; i < 100; i++) p.update(0.02);
    expect(p.fragments.length).toBe(0);
  });
});
