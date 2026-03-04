import { Game } from "./game.js";

const canvas = document.getElementById("game") as HTMLCanvasElement;
if (!canvas) throw new Error("Canvas element #game not found");

const game = new Game(canvas);

let lastTime = performance.now();

function loop(now: number): void {
  const dt = Math.min((now - lastTime) / 1000, 0.05); // cap at 50ms to avoid spiral
  lastTime = now;

  game.update(dt);
  game.render(dt);

  requestAnimationFrame(loop);
}

requestAnimationFrame(loop);
