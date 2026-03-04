import { describe, it, expect } from "vitest";
import { circlesCollide } from "../src/collision.js";

function makeEntity(x: number, y: number, radius: number) {
  return {
    x,
    y,
    radius,
    rotation: 0,
    alive: true,
    update: () => {},
  };
}

describe("circlesCollide", () => {
  it("returns true for overlapping circles", () => {
    const a = makeEntity(0, 0, 10);
    const b = makeEntity(15, 0, 10);
    expect(circlesCollide(a, b)).toBe(true);
  });

  it("returns false for non-overlapping circles", () => {
    const a = makeEntity(0, 0, 5);
    const b = makeEntity(20, 0, 5);
    expect(circlesCollide(a, b)).toBe(false);
  });

  it("returns true for touching circles (edge case)", () => {
    const a = makeEntity(0, 0, 10);
    const b = makeEntity(19.9, 0, 10);
    expect(circlesCollide(a, b)).toBe(true);
  });

  it("returns true for concentric circles", () => {
    const a = makeEntity(100, 100, 10);
    const b = makeEntity(100, 100, 5);
    expect(circlesCollide(a, b)).toBe(true);
  });

  it("works on diagonal distance", () => {
    // Distance = sqrt(10^2 + 10^2) ≈ 14.14, radii sum = 15
    const a = makeEntity(0, 0, 10);
    const b = makeEntity(10, 10, 5);
    expect(circlesCollide(a, b)).toBe(true);
  });

  it("returns false for diagonal distance too far", () => {
    // Distance = sqrt(20^2 + 20^2) ≈ 28.28, radii sum = 15
    const a = makeEntity(0, 0, 10);
    const b = makeEntity(20, 20, 5);
    expect(circlesCollide(a, b)).toBe(false);
  });
});
