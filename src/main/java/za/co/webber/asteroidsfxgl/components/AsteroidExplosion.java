package za.co.webber.asteroidsfxgl.components;

import com.almasb.fxgl.dsl.FXGL;
import java.util.concurrent.ThreadLocalRandom;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import za.co.webber.asteroidsfxgl.EntityType;
import za.co.webber.asteroidsfxgl.GameConfig;

/**
 * Spawns short line-segment fragments that radiate outward when an asteroid is destroyed, mimicking
 * the classic arcade explosion effect.
 */
public final class AsteroidExplosion {

  private AsteroidExplosion() {}

  /** Spawn debris fragments at the given position for the given asteroid size. */
  public static void explode(double x, double y, AsteroidSize size) {
    int count = fragmentCount(size);
    double lifetime = fragmentLifetime(size);
    double speed = GameConfig.ASTEROID_EXPLOSION_SPEED;

    double angleStep = 360.0 / count;
    ThreadLocalRandom rng = ThreadLocalRandom.current();

    for (int i = 0; i < count; i++) {
      double angle = Math.toRadians(i * angleStep + rng.nextDouble(-15, 15));
      double len = 4 + rng.nextDouble(6); // fragment line length 4-10 px

      Line frag = new Line(0, 0, 0, -len);
      frag.setStroke(Color.WHITE);
      frag.setStrokeWidth(1.5);

      double vx = Math.cos(angle) * speed * rng.nextDouble(0.6, 1.0);
      double vy = Math.sin(angle) * speed * rng.nextDouble(0.6, 1.0);
      double spin = rng.nextDouble(-120, 120);

      FXGL.entityBuilder()
          .type(EntityType.ASTEROID)
          .at(x, y)
          .view(frag)
          .with(new DriftAndFadeComponent(new Point2D(vx, vy), spin, lifetime))
          .buildAndAttach();
    }
  }

  private static int fragmentCount(AsteroidSize size) {
    return switch (size) {
      case LARGE -> GameConfig.ASTEROID_EXPLOSION_FRAGMENTS_LARGE;
      case MEDIUM -> GameConfig.ASTEROID_EXPLOSION_FRAGMENTS_MEDIUM;
      case SMALL -> GameConfig.ASTEROID_EXPLOSION_FRAGMENTS_SMALL;
    };
  }

  private static double fragmentLifetime(AsteroidSize size) {
    return switch (size) {
      case LARGE -> GameConfig.ASTEROID_EXPLOSION_LIFETIME_LARGE;
      case MEDIUM -> GameConfig.ASTEROID_EXPLOSION_LIFETIME_MEDIUM;
      case SMALL -> GameConfig.ASTEROID_EXPLOSION_LIFETIME_SMALL;
    };
  }
}
