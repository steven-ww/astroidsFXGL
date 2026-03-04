// Screen dimensions (matches FXGL 1280×720)
export const WIDTH = 1280;
export const HEIGHT = 720;

// Player
export const PLAYER_TURN_SPEED = 150; // degrees per second (2.5 per frame at 60fps)
export const PLAYER_THRUST = 3; // acceleration per second (0.05 per frame at 60fps)
export const PLAYER_COLLISION_RADIUS = 10;
export const INVINCIBILITY_DURATION = 3.0; // seconds
export const INVINCIBILITY_BLINK_FREQ = 8.0; // blinks per second
export const RESPAWN_DELAY = 1.5; // seconds

// Asteroids
export const ASTEROID_BASE_MIN_SPEED = 60;
export const ASTEROID_BASE_MAX_SPEED = 120;
export const ASTEROID_ANGLE_JITTER = 25; // degrees
export const ASTEROID_MAX_SPIN = 40; // degrees per second
export const MAX_ASTEROIDS = 10;

export const ASTEROID_SIZES = {
  LARGE: { radius: 30, scale: 1.0, speedMult: 1.0, wrapMargin: 36, score: 20 },
  MEDIUM: { radius: 18, scale: 0.6, speedMult: 1.4, wrapMargin: 26, score: 50 },
  SMALL: { radius: 11, scale: 0.35, speedMult: 1.9, wrapMargin: 18, score: 100 },
} as const;

// Bullets
export const BULLET_SPEED = 500;
export const BULLET_LIFETIME = 1.2; // seconds

// Game
export const INITIAL_LIVES = 3;

// Particles
export const EXPLOSION_PARTICLE_COUNT = 20;
export const EXPLOSION_PARTICLE_SPEED = 100;
export const EXPLOSION_PARTICLE_LIFETIME = 1.0;

// Fragment explosion
export const FRAGMENT_SPEED = 90;
export const FRAGMENT_SPIN = 40;
export const FRAGMENT_LIFETIME = 1.5;

// Rendering
export const GLOW_BLUR = 8;
export const GLOW_COLOR = "rgba(100, 180, 255, 0.8)";
export const STAR_COUNT = 120;
