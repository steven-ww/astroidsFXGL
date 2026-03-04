package za.co.webber.asteroidsfxgl.components;

import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
import za.co.webber.asteroidsfxgl.GameConfig;

public class BulletComponent extends Component {

  private double life = 0;
  private final Point2D velocity;

  public BulletComponent(Point2D direction, Point2D shipVelocity) {
    this.velocity = direction.normalize().multiply(GameConfig.BULLET_SPEED).add(shipVelocity);
  }

  @Override
  public void onUpdate(double tpf) {
    entity.translate(velocity.multiply(tpf));

    if (entity.getX() < 0) {
      entity.setX(GameConfig.SCREEN_WIDTH);
    } else if (entity.getX() > GameConfig.SCREEN_WIDTH) {
      entity.setX(0);
    }

    if (entity.getY() < 0) {
      entity.setY(GameConfig.SCREEN_HEIGHT);
    } else if (entity.getY() > GameConfig.SCREEN_HEIGHT) {
      entity.setY(0);
    }

    life += tpf;
    if (life > GameConfig.BULLET_LIFETIME) {
      entity.removeFromWorld();
    }
  }
}
