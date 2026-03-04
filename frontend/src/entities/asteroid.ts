import {
  WIDTH,
  HEIGHT,
  ASTEROID_BASE_MIN_SPEED,
  ASTEROID_BASE_MAX_SPEED,
  ASTEROID_ANGLE_JITTER,
  ASTEROID_MAX_SPIN,
  ASTEROID_SIZES,
} from "../constants.js";
import type { AsteroidSize, Entity } from "../types.js";

/** Raw polygon vertices for the asteroid shape (unscaled) */
export const ASTEROID_POLYGON: [number, number][] = [
  [-26, -10],
  [-20, -22],
  [-8, -28],
  [6, -26],
  [18, -20],
  [28, -8],
  [26, 4],
  [26, 16],
  [16, 24],
  [4, 26],
  [-8, 24],
  [-16, 18],
  [-22, 10],
  [-30, 2],
  [-28, -6],
  [-24, -14],
];

function rnd(min: number, max: number): number {
  return min + Math.random() * (max - min);
}

function rotate(
  vx: number,
  vy: number,
  angleRad: number
): { x: number; y: number } {
  const c = Math.cos(angleRad);
  const s = Math.sin(angleRad);
  return { x: vx * c - vy * s, y: vx * s + vy * c };
}

export class Asteroid implements Entity {
  x: number;
  y: number;
  rotation = 0;
  alive = true;

  size: AsteroidSize;
  radius: number;
  scale: number;
  wrapMargin: number;

  vx: number;
  vy: number;
  spin: number;

  constructor(x: number, y: number, size: AsteroidSize) {
    this.x = x;
    this.y = y;
    this.size = size;

    const sizeInfo = ASTEROID_SIZES[size];
    this.radius = sizeInfo.radius;
    this.scale = sizeInfo.scale;
    this.wrapMargin = sizeInfo.wrapMargin;

    // Compute velocity toward screen center with jitter
    const cx = WIDTH / 2;
    const cy = HEIGHT / 2;
    const dx = cx - x;
    const dy = cy - y;
    const dist = Math.sqrt(dx * dx + dy * dy) || 1;
    const dirX = dx / dist;
    const dirY = dy / dist;

    const jitterRad = ((rnd(-ASTEROID_ANGLE_JITTER, ASTEROID_ANGLE_JITTER)) * Math.PI) / 180;
    const jittered = rotate(dirX, dirY, jitterRad);

    const speed = rnd(
      ASTEROID_BASE_MIN_SPEED * sizeInfo.speedMult,
      ASTEROID_BASE_MAX_SPEED * sizeInfo.speedMult
    );
    this.vx = jittered.x * speed;
    this.vy = jittered.y * speed;

    this.spin = rnd(-ASTEROID_MAX_SPIN, ASTEROID_MAX_SPIN);
  }

  update(dt: number): void {
    this.x += this.vx * dt;
    this.y += this.vy * dt;
    this.rotation += this.spin * dt;

    // Screen wrap with margin
    const m = this.wrapMargin;
    if (this.x < -m) this.x = WIDTH + m;
    else if (this.x > WIDTH + m) this.x = -m;
    if (this.y < -m) this.y = HEIGHT + m;
    else if (this.y > HEIGHT + m) this.y = -m;
  }
}
