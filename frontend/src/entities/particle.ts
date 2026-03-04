import {
  EXPLOSION_PARTICLE_COUNT,
  EXPLOSION_PARTICLE_SPEED,
  EXPLOSION_PARTICLE_LIFETIME,
} from "../constants.js";

export interface Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  life: number;
  maxLife: number;
  size: number;
}

export function spawnExplosion(x: number, y: number): Particle[] {
  const particles: Particle[] = [];
  for (let i = 0; i < EXPLOSION_PARTICLE_COUNT; i++) {
    const angle = Math.random() * Math.PI * 2;
    const speed = Math.random() * EXPLOSION_PARTICLE_SPEED + 20;
    particles.push({
      x,
      y,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      life: 0,
      maxLife: EXPLOSION_PARTICLE_LIFETIME * (0.5 + Math.random() * 0.5),
      size: 1 + Math.random() * 2,
    });
  }
  return particles;
}

export function updateParticles(particles: Particle[], dt: number): Particle[] {
  for (const p of particles) {
    p.x += p.vx * dt;
    p.y += p.vy * dt;
    p.life += dt;
  }
  return particles.filter((p) => p.life < p.maxLife);
}
