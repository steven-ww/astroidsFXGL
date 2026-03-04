import {
  WIDTH,
  HEIGHT,
  PLAYER_TURN_SPEED,
  PLAYER_THRUST,
  PLAYER_COLLISION_RADIUS,
  INVINCIBILITY_DURATION,
  INVINCIBILITY_BLINK_FREQ,
} from "../constants.js";
import type { Vec2, Entity } from "../types.js";

export interface ShipFragment {
  x: number;
  y: number;
  rotation: number;
  vx: number;
  vy: number;
  spin: number;
  life: number;
  maxLife: number;
  /** Line segment endpoints in local space: [[x1,y1],[x2,y2]] */
  line: [[number, number], [number, number]];
}

export class Player implements Entity {
  x: number;
  y: number;
  rotation = 0; // degrees, 0 = up
  radius = PLAYER_COLLISION_RADIUS;
  alive = true;

  vx = 0;
  vy = 0;

  thrusting = false;
  exploded = false;
  invincible = false;
  invincibilityTimer = 0;
  opacity = 1;

  /** Ship fragments during explosion */
  fragments: ShipFragment[] = [];

  /** Whether the player is waiting to respawn */
  respawnTimer = 0;

  constructor(x: number, y: number) {
    this.x = x;
    this.y = y;
  }

  turnLeft(dt: number): void {
    if (this.exploded) return;
    this.rotation -= PLAYER_TURN_SPEED * dt;
  }

  turnRight(dt: number): void {
    if (this.exploded) return;
    this.rotation += PLAYER_TURN_SPEED * dt;
  }

  thrustOn(dt: number): void {
    if (this.exploded) return;
    const rad = ((this.rotation - 90) * Math.PI) / 180;
    this.vx += Math.cos(rad) * PLAYER_THRUST * dt;
    this.vy += Math.sin(rad) * PLAYER_THRUST * dt;
    this.thrusting = true;
  }

  thrustOff(): void {
    this.thrusting = false;
  }

  getVelocity(): Vec2 {
    return { x: this.vx, y: this.vy };
  }

  /** Position of the ship's nose tip, offset forward by `distance` pixels */
  getNosePosition(distance: number): Vec2 {
    const rad = ((this.rotation - 90) * Math.PI) / 180;
    return {
      x: this.x + Math.cos(rad) * distance,
      y: this.y + Math.sin(rad) * distance,
    };
  }

  explode(): void {
    if (this.exploded) return;
    this.exploded = true;
    this.thrusting = false;

    const rad = (this.rotation * Math.PI) / 180;
    const cos = Math.cos(rad);
    const sin = Math.sin(rad);

    // Three line fragments matching the A-shape
    const fragDefs: {
      line: [[number, number], [number, number]];
      localVx: number;
      localVy: number;
      spin: number;
    }[] = [
      {
        line: [
          [0, -12],
          [-8, 10],
        ],
        localVx: -90,
        localVy: 0,
        spin: 40,
      },
      {
        line: [
          [0, -12],
          [8, 10],
        ],
        localVx: 90,
        localVy: 0,
        spin: -40,
      },
      {
        line: [
          [-7, 7],
          [7, 7],
        ],
        localVx: 0,
        localVy: 90,
        spin: 0,
      },
    ];

    this.fragments = fragDefs.map((fd) => ({
      x: this.x,
      y: this.y,
      rotation: this.rotation,
      vx: fd.localVx * cos - fd.localVy * sin,
      vy: fd.localVx * sin + fd.localVy * cos,
      spin: fd.spin,
      life: 0,
      maxLife: 1.5,
      line: fd.line,
    }));
  }

  respawn(x: number, y: number): void {
    this.x = x;
    this.y = y;
    this.rotation = 0;
    this.vx = 0;
    this.vy = 0;
    this.exploded = false;
    this.thrusting = false;
    this.invincible = true;
    this.invincibilityTimer = 0;
    this.opacity = 1;
    this.fragments = [];
    this.alive = true;
  }

  update(dt: number): void {
    // Update fragments
    for (const f of this.fragments) {
      f.x += f.vx * dt;
      f.y += f.vy * dt;
      f.rotation += f.spin * dt;
      f.life += dt;
    }
    this.fragments = this.fragments.filter((f) => f.life < f.maxLife);

    if (this.exploded) return;

    // Invincibility
    if (this.invincible) {
      this.invincibilityTimer += dt;
      if (this.invincibilityTimer >= INVINCIBILITY_DURATION) {
        this.invincible = false;
        this.invincibilityTimer = 0;
        this.opacity = 1;
      } else {
        const blink =
          Math.sin(
            this.invincibilityTimer * INVINCIBILITY_BLINK_FREQ * Math.PI * 2
          ) > 0;
        this.opacity = blink ? 1.0 : 0.3;
      }
    }

    // Movement
    this.x += this.vx;
    this.y += this.vy;

    // Screen wrap
    if (this.x < 0) this.x = WIDTH;
    else if (this.x > WIDTH) this.x = 0;
    if (this.y < 0) this.y = HEIGHT;
    else if (this.y > HEIGHT) this.y = 0;
  }
}
