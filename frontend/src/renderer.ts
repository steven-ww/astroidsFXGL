import { WIDTH, HEIGHT, GLOW_BLUR, GLOW_COLOR, STAR_COUNT } from "./constants.js";
import { Player, type ShipFragment } from "./entities/player.js";
import { Asteroid, ASTEROID_POLYGON } from "./entities/asteroid.js";
import { Bullet } from "./entities/bullet.js";
import type { Particle } from "./entities/particle.js";
import type { Star, ScoreEntry } from "./types.js";

export class Renderer {
  private ctx: CanvasRenderingContext2D;
  private stars: Star[] = [];
  screenFlash = 0; // 0–1, decays each frame

  constructor(private canvas: HTMLCanvasElement) {
    canvas.width = WIDTH;
    canvas.height = HEIGHT;
    const ctx = canvas.getContext("2d");
    if (!ctx) throw new Error("Cannot get 2D context");
    this.ctx = ctx;
    this.initStars();
  }

  private initStars(): void {
    this.stars = [];
    for (let i = 0; i < STAR_COUNT; i++) {
      this.stars.push({
        x: Math.random() * WIDTH,
        y: Math.random() * HEIGHT,
        brightness: 0.3 + Math.random() * 0.7,
        size: 0.5 + Math.random() * 1.5,
      });
    }
  }

  clear(): void {
    const ctx = this.ctx;
    ctx.fillStyle = "#000";
    ctx.fillRect(0, 0, WIDTH, HEIGHT);
  }

  drawStars(): void {
    const ctx = this.ctx;
    for (const s of this.stars) {
      ctx.fillStyle = `rgba(200, 220, 255, ${s.brightness * 0.6})`;
      ctx.beginPath();
      ctx.arc(s.x, s.y, s.size, 0, Math.PI * 2);
      ctx.fill();
    }
  }

  drawScreenFlash(dt: number): void {
    if (this.screenFlash > 0) {
      const ctx = this.ctx;
      ctx.fillStyle = `rgba(255, 255, 255, ${this.screenFlash * 0.3})`;
      ctx.fillRect(0, 0, WIDTH, HEIGHT);
      this.screenFlash = Math.max(0, this.screenFlash - dt * 3);
    }
  }

  private enableGlow(color = GLOW_COLOR, blur = GLOW_BLUR): void {
    this.ctx.shadowColor = color;
    this.ctx.shadowBlur = blur;
  }

  private disableGlow(): void {
    this.ctx.shadowColor = "transparent";
    this.ctx.shadowBlur = 0;
  }

  drawPlayer(player: Player): void {
    const ctx = this.ctx;

    // Draw fragments (even if exploded)
    for (const frag of player.fragments) {
      this.drawFragment(frag);
    }

    if (player.exploded) return;

    ctx.save();
    ctx.translate(player.x, player.y);
    ctx.rotate((player.rotation * Math.PI) / 180);
    ctx.globalAlpha = player.opacity;

    this.enableGlow("rgba(100, 200, 255, 0.9)", 10);

    // Ship body — A-shape
    ctx.strokeStyle = "#fff";
    ctx.lineWidth = 2;
    ctx.lineCap = "round";

    // Left leg
    ctx.beginPath();
    ctx.moveTo(0, -12);
    ctx.lineTo(-8, 10);
    ctx.stroke();

    // Right leg
    ctx.beginPath();
    ctx.moveTo(0, -12);
    ctx.lineTo(8, 10);
    ctx.stroke();

    // Crossbar
    ctx.beginPath();
    ctx.moveTo(-7, 7);
    ctx.lineTo(7, 7);
    ctx.stroke();

    // Thrust flame
    if (player.thrusting) {
      this.enableGlow("rgba(255, 150, 50, 0.9)", 14);
      const flicker = 0.8 + Math.random() * 0.4;
      ctx.strokeStyle = `rgba(255, ${140 + Math.random() * 60}, 50, ${flicker})`;
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.moveTo(-3, 12);
      ctx.lineTo(0, 12 + 6 * flicker);
      ctx.lineTo(3, 12);
      ctx.stroke();
    }

    this.disableGlow();
    ctx.globalAlpha = 1;
    ctx.restore();
  }

  private drawFragment(frag: ShipFragment): void {
    const ctx = this.ctx;
    const alpha = 1 - Math.min(1, frag.life / frag.maxLife);

    ctx.save();
    ctx.translate(frag.x, frag.y);
    ctx.rotate((frag.rotation * Math.PI) / 180);
    ctx.globalAlpha = alpha;

    this.enableGlow("rgba(100, 200, 255, 0.6)", 6);
    ctx.strokeStyle = "#fff";
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(frag.line[0][0], frag.line[0][1]);
    ctx.lineTo(frag.line[1][0], frag.line[1][1]);
    ctx.stroke();

    this.disableGlow();
    ctx.globalAlpha = 1;
    ctx.restore();
  }

  drawAsteroid(asteroid: Asteroid): void {
    const ctx = this.ctx;

    ctx.save();
    ctx.translate(asteroid.x, asteroid.y);
    ctx.rotate((asteroid.rotation * Math.PI) / 180);
    ctx.scale(asteroid.scale, asteroid.scale);

    this.enableGlow("rgba(180, 200, 255, 0.6)", 6);

    ctx.strokeStyle = "#fff";
    ctx.lineWidth = 2;
    ctx.fillStyle = "transparent";

    ctx.beginPath();
    const verts = ASTEROID_POLYGON;
    ctx.moveTo(verts[0][0], verts[0][1]);
    for (let i = 1; i < verts.length; i++) {
      ctx.lineTo(verts[i][0], verts[i][1]);
    }
    ctx.closePath();
    ctx.stroke();

    this.disableGlow();
    ctx.restore();
  }

  drawBullet(bullet: Bullet): void {
    const ctx = this.ctx;

    ctx.save();
    ctx.translate(bullet.x, bullet.y);
    ctx.rotate((bullet.rotation * Math.PI) / 180);

    this.enableGlow("rgba(255, 255, 200, 0.9)", 10);

    ctx.strokeStyle = "#fff";
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(0, 0);
    ctx.lineTo(0, -4);
    ctx.stroke();

    this.disableGlow();
    ctx.restore();
  }

  drawParticles(particles: Particle[]): void {
    const ctx = this.ctx;
    this.enableGlow("rgba(255, 200, 100, 0.8)", 8);

    for (const p of particles) {
      const alpha = 1 - Math.min(1, p.life / p.maxLife);
      ctx.fillStyle = `rgba(255, ${180 + Math.random() * 75}, ${80 + Math.random() * 50}, ${alpha})`;
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.size * (1 - p.life / p.maxLife * 0.5), 0, Math.PI * 2);
      ctx.fill();
    }

    this.disableGlow();
  }

  // ── HUD ──

  drawScore(score: number): void {
    const ctx = this.ctx;
    this.enableGlow("rgba(255, 255, 255, 0.5)", 4);
    ctx.fillStyle = "#fff";
    ctx.font = "24px monospace";
    ctx.textAlign = "left";
    ctx.fillText(String(score).padStart(2, "0"), 20, 35);
    this.disableGlow();
  }

  drawHighScore(highScore: number): void {
    const ctx = this.ctx;
    this.enableGlow("rgba(255, 255, 255, 0.5)", 4);
    ctx.fillStyle = "#fff";
    ctx.font = "24px monospace";
    ctx.textAlign = "center";
    ctx.fillText(String(highScore).padStart(2, "0"), WIDTH / 2, 35);
    this.disableGlow();
  }

  drawLives(lives: number): void {
    const ctx = this.ctx;
    this.enableGlow("rgba(100, 200, 255, 0.5)", 4);

    // Draw mini ships for remaining lives (exclude current)
    for (let i = 0; i < lives - 1; i++) {
      ctx.save();
      ctx.translate(20 + i * 18, 55);
      ctx.scale(0.6, 0.6);

      ctx.strokeStyle = "#fff";
      ctx.lineWidth = 1.5;
      ctx.lineCap = "round";

      ctx.beginPath();
      ctx.moveTo(0, -12);
      ctx.lineTo(-8, 10);
      ctx.stroke();

      ctx.beginPath();
      ctx.moveTo(0, -12);
      ctx.lineTo(8, 10);
      ctx.stroke();

      ctx.beginPath();
      ctx.moveTo(-7, 7);
      ctx.lineTo(7, 7);
      ctx.stroke();

      ctx.restore();
    }

    this.disableGlow();
  }

  drawGameOver(): void {
    const ctx = this.ctx;
    this.enableGlow("rgba(255, 100, 100, 0.8)", 12);

    ctx.fillStyle = "#fff";
    ctx.font = "48px monospace";
    ctx.textAlign = "center";
    ctx.fillText("GAME OVER", WIDTH / 2, HEIGHT / 2 - 50);

    this.disableGlow();
  }

  drawLeaderboard(scores: ScoreEntry[]): void {
    const ctx = this.ctx;
    this.enableGlow("rgba(255, 255, 200, 0.5)", 6);

    ctx.fillStyle = "#fff";
    ctx.font = "32px monospace";
    ctx.textAlign = "center";
    ctx.fillText("HIGH SCORES", WIDTH / 2, 150);

    ctx.font = "20px monospace";
    for (let i = 0; i < scores.length && i < 10; i++) {
      const entry = scores[i];
      const text = `${entry.name.padEnd(3)}  ${entry.score}`;
      ctx.fillText(text, WIDTH / 2, 200 + i * 30);
    }

    this.disableGlow();
  }

  drawPressSpace(): void {
    const ctx = this.ctx;
    this.enableGlow("rgba(255, 255, 255, 0.5)", 4);

    ctx.fillStyle = "#fff";
    ctx.font = "24px monospace";
    ctx.textAlign = "center";
    ctx.fillText("PRESS SPACE TO START", WIDTH / 2, 600);

    this.disableGlow();
  }

  drawNamePrompt(currentName: string): void {
    const ctx = this.ctx;
    this.enableGlow("rgba(255, 255, 100, 0.7)", 8);

    ctx.fillStyle = "#fff";
    ctx.font = "28px monospace";
    ctx.textAlign = "center";
    ctx.fillText("NEW HIGH SCORE!", WIDTH / 2, HEIGHT / 2 - 40);

    ctx.font = "22px monospace";
    ctx.fillText("Enter 3 characters:", WIDTH / 2, HEIGHT / 2);

    ctx.font = "36px monospace";
    const display = currentName.padEnd(3, "_");
    ctx.fillText(display, WIDTH / 2, HEIGHT / 2 + 50);

    ctx.font = "16px monospace";
    ctx.fillText("Press ENTER to confirm", WIDTH / 2, HEIGHT / 2 + 90);

    this.disableGlow();
  }
}
