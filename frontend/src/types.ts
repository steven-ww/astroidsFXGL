export interface Vec2 {
  x: number;
  y: number;
}

export type AsteroidSize = "LARGE" | "MEDIUM" | "SMALL";

export interface Entity {
  x: number;
  y: number;
  rotation: number;
  radius: number;
  alive: boolean;
  update(dt: number): void;
}

export interface Star {
  x: number;
  y: number;
  brightness: number;
  size: number;
}

export interface ScoreEntry {
  name: string;
  score: number;
}
