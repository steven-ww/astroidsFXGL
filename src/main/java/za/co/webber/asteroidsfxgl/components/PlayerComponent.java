package za.co.webber.asteroidsfxgl.components;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polyline;
import za.co.webber.asteroidsfxgl.EntityType;
import za.co.webber.asteroidsfxgl.GameConfig;

public class PlayerComponent extends Component {

  private final Vec2 velocity = new Vec2(0, 0);
  private final Polyline thrustFlame;
  private boolean exploded = false;
  private boolean invincible = false;
  private double invincibilityTimer = 0;

  public PlayerComponent(Polyline thrustFlame) {
    this.thrustFlame = thrustFlame;
    this.thrustFlame.setVisible(false);
  }

  public Point2D getNosePosition(double distance) {
    Point2D localNose = new Point2D(0, -distance);
    double ang = Math.toRadians(entity.getRotation());
    Point2D rotated = rotate(localNose, ang);
    return entity.getPosition().add(rotated);
  }

  public Vec2 getVelocity() {
    return velocity;
  }

  public double getRotation() {
    return entity.getRotation();
  }

  public void turnLeft() {
    if (exploded) return;
    entity.rotateBy(GameConfig.PLAYER_ROTATION_SPEED);
  }

  public void turnRight() {
    if (exploded) return;
    entity.rotateBy(-GameConfig.PLAYER_ROTATION_SPEED);
  }

  public void thrustOn() {
    if (exploded) return;
    Vec2 thrust = Vec2.fromAngle(entity.getRotation() - 90).mulLocal(GameConfig.PLAYER_THRUST);
    velocity.set(velocity.add(thrust));
    thrustFlame.setVisible(true);
  }

  public void thrustOff() {
    thrustFlame.setVisible(false);
  }

  @Override
  public void onUpdate(double tpf) {
    if (exploded) return;

    if (invincible) {
      invincibilityTimer += tpf;
      if (invincibilityTimer >= GameConfig.INVINCIBILITY_DURATION) {
        invincible = false;
        invincibilityTimer = 0;
        entity.getViewComponent().setOpacity(1.0);
      } else {
        double phase = invincibilityTimer * GameConfig.INVINCIBILITY_BLINK_FREQ * Math.PI * 2;
        entity
            .getViewComponent()
            .setOpacity((Math.sin(phase) > 0) ? 1.0 : GameConfig.INVINCIBILITY_OPACITY_LOW);
      }
    }

    entity.translate(velocity);

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
  }

  public boolean isInvincible() {
    return invincible;
  }

  public void explode() {
    if (exploded) return;
    exploded = true;

    thrustFlame.setVisible(false);

    try {
      CollidableComponent cc = entity.getComponent(CollidableComponent.class);
      cc.setValue(false);
    } catch (Exception ignored) {
    }

    entity.getViewComponent().clearChildren();

    List<Line> fragments = ShipShape.createExplosionFragments();
    Line left = fragments.get(0);
    Line right = fragments.get(1);
    Line bar = fragments.get(2);

    double ang = Math.toRadians(entity.getRotation());
    double drift = GameConfig.EXPLOSION_FRAGMENT_DRIFT;
    double spin = GameConfig.EXPLOSION_FRAGMENT_SPIN;
    double life = GameConfig.EXPLOSION_FRAGMENT_LIFETIME;

    Point2D vLeft = rotate(new Point2D(-drift, 0), ang);
    Point2D vRight = rotate(new Point2D(drift, 0), ang);
    Point2D vBar = rotate(new Point2D(0, drift), ang);

    spawnFragment(left, vLeft, spin, life);
    spawnFragment(right, vRight, -spin, life);
    spawnFragment(bar, vBar, 0.0, life);
  }

  private static Point2D rotate(Point2D v, double ang) {
    double c = Math.cos(ang);
    double s = Math.sin(ang);
    return new Point2D(v.getX() * c - v.getY() * s, v.getX() * s + v.getY() * c);
  }

  private void spawnFragment(Node view, Point2D velocity, double spinDegPerSec, double life) {
    Entity frag =
        FXGL.entityBuilder()
            .type(EntityType.PLAYER)
            .at(entity.getX(), entity.getY())
            .view(view)
            .buildAndAttach();
    frag.setRotation(entity.getRotation());
    frag.addComponent(new DriftAndFadeComponent(velocity, spinDegPerSec, life));
  }

  public void respawn(double x, double y) {
    entity.getViewComponent().clearChildren();

    Path ship = ShipShape.createShip(1.0);
    entity.getViewComponent().addChild(ship);
    entity.getViewComponent().addChild(thrustFlame);

    entity.setPosition(x, y);
    entity.setRotation(0);
    velocity.set(0, 0);

    exploded = false;
    thrustFlame.setVisible(false);

    invincible = true;
    invincibilityTimer = 0;

    try {
      CollidableComponent cc = entity.getComponent(CollidableComponent.class);
      cc.setValue(true);
    } catch (Exception ignored) {
    }
  }
}
