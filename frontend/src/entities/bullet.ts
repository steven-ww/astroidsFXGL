import { WIDTH, HEIGHT, BULLET_SPEED, BULLET_LIFETIME } from "../constants.js";
import type { Entity, Vec2 } from "../types.js";

export class Bullet implements Entity {
  x: number;
  y: number;
  rotation: number;
  radius = 2;
  alive = true;

  vx: number;
  vy: number;
  life = 0;

  constructor(
    x: number,
    y: number,
    rotationDeg: number,
    shipVelocity: Vec2
  ) {
    this.x = x;
    this.y = y;
    this.rotation = rotationDeg;

    const rad = ((rotationDeg - 90) * Math.PI) / 180;
    this.vx = Math.cos(rad) * BULLET_SPEED + shipVelocity.x;
    this.vy = Math.sin(rad) * BULLET_SPEED + shipVelocity.y;
  }

  update(dt: number): void {
    this.x += this.vx * dt;
    this.y += this.vy * dt;

    // Screen wrap
    if (this.x < 0) this.x = WIDTH;
    else if (this.x > WIDTH) this.x = 0;
    if (this.y < 0) this.y = HEIGHT;
    else if (this.y > HEIGHT) this.y = 0;

    this.life += dt;
    if (this.life > BULLET_LIFETIME) {
      this.alive = false;
    }
  }
}
