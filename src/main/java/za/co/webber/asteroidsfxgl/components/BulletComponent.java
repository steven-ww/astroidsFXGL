package za.co.webber.asteroidsfxgl.components;

import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

public class BulletComponent extends Component {

  private static final double SPEED = 500;
  private static final double LIFETIME = 1.2;

  private double life = 0;

  private final Point2D velocity;

  public BulletComponent(Point2D direction, Point2D shipVelocity) {
    // Bullet moves forward + inherits ship movement
    this.velocity = direction.normalize().multiply(SPEED).add(shipVelocity);
  }

  @Override
  public void onUpdate(double tpf) {
    // Move bullet
    entity.translate(velocity.multiply(tpf));

    // Wrap around screen edges (mirror player's wrapping behavior)
    double w = com.almasb.fxgl.dsl.FXGL.getAppWidth();
    double h = com.almasb.fxgl.dsl.FXGL.getAppHeight();

    if (entity.getX() < 0) {
      entity.setX(w);
    } else if (entity.getX() > w) {
      entity.setX(0);
    }

    if (entity.getY() < 0) {
      entity.setY(h);
    } else if (entity.getY() > h) {
      entity.setY(0);
    }

    // Lifetime handling
    life += tpf;
    if (life > LIFETIME) {
      entity.removeFromWorld();
    }
  }

  @Override
  public void onRemoved() {
    // Decrement global bullet counter safely when this bullet is removed (collision, timeout, etc.)
    try {
      int count = com.almasb.fxgl.dsl.FXGL.geti("bulletCount");
      if (count > 0) {
        com.almasb.fxgl.dsl.FXGL.inc("bulletCount", -1);
      }
    } catch (Exception ignored) {
      // If bulletCount not present for some reason, ignore silently
    }
  }
}
