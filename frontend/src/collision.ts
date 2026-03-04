import type { Entity } from "./types.js";

/** Returns true if two circular entities overlap */
export function circlesCollide(a: Entity, b: Entity): boolean {
  const dx = a.x - b.x;
  const dy = a.y - b.y;
  const distSq = dx * dx + dy * dy;
  const radSum = a.radius + b.radius;
  return distSq < radSum * radSum;
}
