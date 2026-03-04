package za.co.webber.asteroidsfxgl;

/** Centralised constants for gameplay tuning and display settings. */
public final class GameConfig {

  private GameConfig() {}

  // ── Screen ──────────────────────────────────────────────────────────────
  public static final int SCREEN_WIDTH = 1280;
  public static final int SCREEN_HEIGHT = 720;

  // ── Player ──────────────────────────────────────────────────────────────
  public static final double PLAYER_ROTATION_SPEED = 2.5;
  public static final double PLAYER_THRUST = 0.05;
  public static final double INVINCIBILITY_DURATION = 3.0;
  public static final double INVINCIBILITY_BLINK_FREQ = 8.0;
  public static final double INVINCIBILITY_OPACITY_LOW = 0.3;
  public static final double PLAYER_COLLISION_RADIUS = 10.0;
  public static final double RESPAWN_DELAY_SECONDS = 1.5;

  // ── Bullet ──────────────────────────────────────────────────────────────
  public static final double BULLET_SPEED = 500;
  public static final double BULLET_LIFETIME = 1.2;
  public static final double BULLET_COLLISION_RADIUS = 2.0;
  public static final double BULLET_SPAWN_OFFSET = 14;

  // ── Asteroid ────────────────────────────────────────────────────────────
  public static final int MAX_ASTEROIDS = 10;
  public static final double ASTEROID_BASE_MIN_SPEED = 60;
  public static final double ASTEROID_BASE_MAX_SPEED = 120;
  public static final double ASTEROID_SPAWN_MARGIN = 40;
  public static final double ASTEROID_ANGLE_JITTER = 25;
  public static final double ASTEROID_SPIN_MIN = -40;
  public static final double ASTEROID_SPIN_MAX = 40;

  // Speed multipliers per size
  public static final double ASTEROID_SPEED_MULT_LARGE = 1.0;
  public static final double ASTEROID_SPEED_MULT_MEDIUM = 1.4;
  public static final double ASTEROID_SPEED_MULT_SMALL = 1.9;

  // Wrap margins per size
  public static final double ASTEROID_WRAP_MARGIN_LARGE = 36.0;
  public static final double ASTEROID_WRAP_MARGIN_MEDIUM = 26.0;
  public static final double ASTEROID_WRAP_MARGIN_SMALL = 18.0;

  // ── Scoring ─────────────────────────────────────────────────────────────
  public static final int SCORE_LARGE_ASTEROID = 20;
  public static final int SCORE_MEDIUM_ASTEROID = 50;
  public static final int SCORE_SMALL_ASTEROID = 100;

  // ── Explosion fragments ─────────────────────────────────────────────────
  public static final double EXPLOSION_FRAGMENT_LIFETIME = 1.5;
  public static final double EXPLOSION_FRAGMENT_DRIFT = 90.0;
  public static final double EXPLOSION_FRAGMENT_SPIN = 40.0;

  // Asteroid explosion
  public static final int ASTEROID_EXPLOSION_FRAGMENTS_LARGE = 8;
  public static final int ASTEROID_EXPLOSION_FRAGMENTS_MEDIUM = 6;
  public static final int ASTEROID_EXPLOSION_FRAGMENTS_SMALL = 4;
  public static final double ASTEROID_EXPLOSION_SPEED = 120.0;
  public static final double ASTEROID_EXPLOSION_LIFETIME_LARGE = 1.0;
  public static final double ASTEROID_EXPLOSION_LIFETIME_MEDIUM = 0.7;
  public static final double ASTEROID_EXPLOSION_LIFETIME_SMALL = 0.5;

  // ── HUD ─────────────────────────────────────────────────────────────────
  public static final int HUD_MARGIN = 20;
  public static final double HUD_LIVES_ICON_SCALE = 0.6;
  public static final int HUD_LIVES_ICON_SPACING = 15;
  public static final int HUD_SCORE_FONT_SIZE = 24;
  public static final int HUD_GAME_OVER_FONT_SIZE = 48;
  public static final int HUD_TITLE_FONT_SIZE = 32;
  public static final int HUD_LEADERBOARD_FONT_SIZE = 20;
  public static final int HUD_RESTART_FONT_SIZE = 24;
}
