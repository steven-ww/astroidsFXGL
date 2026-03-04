import {
  WIDTH,
  HEIGHT,
  INITIAL_LIVES,
  RESPAWN_DELAY,
  MAX_ASTEROIDS,
  ASTEROID_SIZES,
} from "./constants.js";
import { Player } from "./entities/player.js";
import { Asteroid } from "./entities/asteroid.js";
import { Bullet } from "./entities/bullet.js";
import {
  type Particle,
  spawnExplosion,
  updateParticles,
} from "./entities/particle.js";
import { InputManager } from "./input.js";
import { Renderer } from "./renderer.js";
import { circlesCollide } from "./collision.js";
import type { AsteroidSize, ScoreEntry } from "./types.js";

export type GameState =
  | "playing"
  | "respawning"
  | "gameOver"
  | "enterName"
  | "leaderboard";

export class Game {
  player: Player;
  asteroids: Asteroid[] = [];
  bullets: Bullet[] = [];
  particles: Particle[] = [];

  lives = INITIAL_LIVES;
  score = 0;
  level = 0;
  highScore = 0;
  asteroidCount = 0;

  state: GameState = "playing";
  respawnTimer = 0;

  // High score entry
  nameInput = "";
  highScores: ScoreEntry[] = [];

  private input: InputManager;
  private renderer: Renderer;

  constructor(canvas: HTMLCanvasElement) {
    this.input = new InputManager();
    this.renderer = new Renderer(canvas);

    this.highScores = this.loadHighScores();
    this.highScore = this.highScores.length > 0 ? this.highScores[0].score : 0;

    this.player = new Player(WIDTH / 2, HEIGHT / 2);
    this.spawnLevelAsteroids(this.level * 2 + 4);

    // Listen for name entry keystrokes
    window.addEventListener("keydown", (e) => this.handleNameInput(e));
  }

  private handleNameInput(e: KeyboardEvent): void {
    if (this.state !== "enterName") return;

    if (e.key === "Enter" && this.nameInput.length > 0) {
      this.submitHighScore();
      return;
    }

    if (e.key === "Backspace") {
      this.nameInput = this.nameInput.slice(0, -1);
      return;
    }

    if (
      this.nameInput.length < 3 &&
      e.key.length === 1 &&
      /[a-zA-Z0-9]/.test(e.key)
    ) {
      this.nameInput += e.key.toUpperCase();
    }
  }

  private submitHighScore(): void {
    const name =
      this.nameInput.length === 0 ? "AAA" : this.nameInput.toUpperCase();
    this.highScores.push({ name, score: this.score });
    this.highScores.sort((a, b) => b.score - a.score);
    this.highScores = this.highScores.slice(0, 10);
    this.saveHighScores(this.highScores);
    this.state = "leaderboard";
  }

  // ── Spawning ──

  private spawnLevelAsteroids(count: number): void {
    for (let i = 0; i < count; i++) {
      this.spawnLargeAsteroidOffscreen();
      this.asteroidCount++;
    }
  }

  private spawnLargeAsteroidOffscreen(): void {
    const margin = 40;
    const edge = Math.floor(Math.random() * 4);
    let x: number, y: number;

    switch (edge) {
      case 0:
        x = -margin;
        y = Math.random() * HEIGHT;
        break;
      case 1:
        x = WIDTH + margin;
        y = Math.random() * HEIGHT;
        break;
      case 2:
        x = Math.random() * WIDTH;
        y = -margin;
        break;
      default:
        x = Math.random() * WIDTH;
        y = HEIGHT + margin;
        break;
    }

    this.asteroids.push(new Asteroid(x, y, "LARGE"));
  }

  private spawnAsteroidChildren(
    size: AsteroidSize,
    x: number,
    y: number,
    count: number
  ): void {
    for (let i = 0; i < count; i++) {
      this.asteroids.push(new Asteroid(x, y, size));
      this.asteroidCount++;
    }
  }

  // ── Game logic ──

  private handleAsteroidDestroyed(
    size: AsteroidSize,
    x: number,
    y: number
  ): void {
    this.asteroidCount--;

    const sizeInfo = ASTEROID_SIZES[size];
    this.addScore(sizeInfo.score);

    // Spawn children
    if (size === "LARGE") {
      this.spawnAsteroidChildren("MEDIUM", x, y, 2);
    } else if (size === "MEDIUM") {
      this.spawnAsteroidChildren("SMALL", x, y, 2);
    }

    // Particles
    this.particles.push(...spawnExplosion(x, y));

    // Check level complete
    if (this.asteroidCount === 0) {
      this.level++;
      this.lives++;
      const asteroidCount = Math.min(this.level * 2 + 4, MAX_ASTEROIDS);
      this.spawnLevelAsteroids(asteroidCount);
    }
  }

  private addScore(delta: number): void {
    this.score += delta;
    if (this.score > this.highScore) {
      this.highScore = this.score;
    }
  }

  private lifeLost(): void {
    this.player.explode();
    this.renderer.screenFlash = 1;
    this.lives--;

    if (this.lives > 0) {
      this.state = "respawning";
      this.respawnTimer = RESPAWN_DELAY;
    } else {
      this.state = "gameOver";
      // After a brief delay, check for high score
      setTimeout(() => {
        const isHighScore =
          this.highScores.length < 10 ||
          this.score >
            this.highScores[this.highScores.length - 1].score;

        if (isHighScore && this.score > 0) {
          this.nameInput = "";
          this.state = "enterName";
        } else {
          this.state = "leaderboard";
        }
      }, 2000);
    }
  }

  private restartGame(): void {
    this.player = new Player(WIDTH / 2, HEIGHT / 2);
    this.asteroids = [];
    this.bullets = [];
    this.particles = [];
    this.lives = INITIAL_LIVES;
    this.score = 0;
    this.level = 0;
    this.asteroidCount = 0;
    this.state = "playing";
    this.highScores = this.loadHighScores();
    this.highScore = this.highScores.length > 0 ? this.highScores[0].score : 0;
    this.spawnLevelAsteroids(this.level * 2 + 4);
  }

  // ── Update loop ──

  update(dt: number): void {
    const input = this.input;

    // Handle restart from leaderboard
    if (this.state === "leaderboard" && input.wasPressed("Space")) {
      this.restartGame();
      input.endFrame();
      return;
    }

    // Handle restart from game over (if no high score entry needed)
    if (this.state === "gameOver" && input.wasPressed("Space")) {
      this.restartGame();
      input.endFrame();
      return;
    }

    // Name entry is handled by keydown listener, skip normal input
    if (this.state === "enterName") {
      input.endFrame();
      return;
    }

    // Respawn timer
    if (this.state === "respawning") {
      this.respawnTimer -= dt;
      if (this.respawnTimer <= 0) {
        this.player.respawn(WIDTH / 2, HEIGHT / 2);
        this.state = "playing";
      }
    }

    // Player input (only when playing)
    if (this.state === "playing" && !this.player.exploded) {
      if (input.isDown("KeyA")) this.player.turnLeft(dt);
      if (input.isDown("KeyD")) this.player.turnRight(dt);
      if (input.isDown("KeyW")) {
        this.player.thrustOn(dt);
      } else {
        this.player.thrustOff();
      }

      if (input.wasPressed("Space")) {
        const nose = this.player.getNosePosition(14);
        const vel = this.player.getVelocity();
        this.bullets.push(
          new Bullet(nose.x, nose.y, this.player.rotation, vel)
        );
      }
    }

    // Update entities
    this.player.update(dt);

    for (const a of this.asteroids) a.update(dt);
    for (const b of this.bullets) b.update(dt);

    this.particles = updateParticles(this.particles, dt);

    // Remove dead bullets
    this.bullets = this.bullets.filter((b) => b.alive);

    // Collision: bullet ↔ asteroid
    for (const bullet of this.bullets) {
      if (!bullet.alive) continue;
      for (const asteroid of this.asteroids) {
        if (!asteroid.alive) continue;
        if (circlesCollide(bullet, asteroid)) {
          bullet.alive = false;
          asteroid.alive = false;
          this.handleAsteroidDestroyed(
            asteroid.size,
            asteroid.x,
            asteroid.y
          );
        }
      }
    }

    // Collision: player ↔ asteroid
    if (
      this.state === "playing" &&
      !this.player.exploded &&
      !this.player.invincible
    ) {
      for (const asteroid of this.asteroids) {
        if (!asteroid.alive) continue;
        if (circlesCollide(this.player, asteroid)) {
          this.lifeLost();
          break;
        }
      }
    }

    // Remove dead asteroids and bullets
    this.asteroids = this.asteroids.filter((a) => a.alive);
    this.bullets = this.bullets.filter((b) => b.alive);

    input.endFrame();
  }

  // ── Render ──

  render(dt: number): void {
    const r = this.renderer;

    r.clear();
    r.drawStars();

    // Entities
    for (const a of this.asteroids) r.drawAsteroid(a);
    for (const b of this.bullets) r.drawBullet(b);
    r.drawPlayer(this.player);
    r.drawParticles(this.particles);

    // HUD
    r.drawScore(this.score);
    r.drawHighScore(this.highScore);
    r.drawLives(this.lives);

    // Screen flash overlay
    r.drawScreenFlash(dt);

    // State-specific UI
    if (this.state === "gameOver") {
      r.drawGameOver();
    } else if (this.state === "enterName") {
      r.drawGameOver();
      r.drawNamePrompt(this.nameInput);
    } else if (this.state === "leaderboard") {
      r.drawLeaderboard(this.highScores);
      r.drawPressSpace();
    }
  }

  // ── High score persistence ──

  loadHighScores(): ScoreEntry[] {
    try {
      const raw = localStorage.getItem("asteroids_highscores");
      if (!raw) return [];
      const parsed = JSON.parse(raw) as ScoreEntry[];
      return parsed.sort((a, b) => b.score - a.score).slice(0, 10);
    } catch {
      return [];
    }
  }

  saveHighScores(scores: ScoreEntry[]): void {
    try {
      localStorage.setItem("asteroids_highscores", JSON.stringify(scores));
    } catch {
      // ignore storage errors
    }
  }
}
