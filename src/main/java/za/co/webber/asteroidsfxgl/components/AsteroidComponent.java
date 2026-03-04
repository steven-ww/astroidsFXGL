package za.co.webber.asteroidsfxgl.components;

import com.almasb.fxgl.entity.component.Component;
import java.util.concurrent.ThreadLocalRandom;
import javafx.geometry.Point2D;
import za.co.webber.asteroidsfxgl.GameConfig;

public class AsteroidComponent extends Component {

  private final AsteroidSize size;

  private Point2D velocity;
  private double spin;
  private double wrapMargin;

  public AsteroidComponent(AsteroidSize size) {
    this.size = size;
  }

  @Override
  public void onAdded() {
    double cx = GameConfig.SCREEN_WIDTH / 2.0;
    double cy = GameConfig.SCREEN_HEIGHT / 2.0;

    double dx = cx - entity.getX();
    double dy = cy - entity.getY();
    Point2D dirToCenter = new Point2D(dx, dy).normalize();

    double angleJitter = rnd(-GameConfig.ASTEROID_ANGLE_JITTER, GameConfig.ASTEROID_ANGLE_JITTER);
    Point2D jittered = rotate(dirToCenter, Math.toRadians(angleJitter));

    double speedMult =
        switch (size) {
          case LARGE -> GameConfig.ASTEROID_SPEED_MULT_LARGE;
          case MEDIUM -> GameConfig.ASTEROID_SPEED_MULT_MEDIUM;
          case SMALL -> GameConfig.ASTEROID_SPEED_MULT_SMALL;
        };

    double speed =
        rnd(
            GameConfig.ASTEROID_BASE_MIN_SPEED * speedMult,
            GameConfig.ASTEROID_BASE_MAX_SPEED * speedMult);
    velocity = jittered.multiply(speed);

    spin = rnd(GameConfig.ASTEROID_SPIN_MIN, GameConfig.ASTEROID_SPIN_MAX);

    wrapMargin =
        switch (size) {
          case LARGE -> GameConfig.ASTEROID_WRAP_MARGIN_LARGE;
          case MEDIUM -> GameConfig.ASTEROID_WRAP_MARGIN_MEDIUM;
          case SMALL -> GameConfig.ASTEROID_WRAP_MARGIN_SMALL;
        };
  }

  @Override
  public void onUpdate(double tpf) {
    entity.translate(velocity.getX() * tpf, velocity.getY() * tpf);
    entity.rotateBy(spin * tpf);
    wrapAround();
  }

  private void wrapAround() {
    double w = GameConfig.SCREEN_WIDTH;
    double h = GameConfig.SCREEN_HEIGHT;
    double x = entity.getX();
    double y = entity.getY();
    double m = wrapMargin;

    if (x < -m) entity.setX(w + m);
    else if (x > w + m) entity.setX(-m);

    if (y < -m) entity.setY(h + m);
    else if (y > h + m) entity.setY(-m);
  }

  private static double rnd(double min, double max) {
    return ThreadLocalRandom.current().nextDouble(min, max);
  }

  public AsteroidSize getSize() {
    return size;
  }

  private static Point2D rotate(Point2D v, double angleRad) {
    double cos = Math.cos(angleRad);
    double sin = Math.sin(angleRad);
    return new Point2D(v.getX() * cos - v.getY() * sin, v.getX() * sin + v.getY() * cos);
  }
}
