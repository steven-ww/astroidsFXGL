package za.co.webber.asteroidsfxgl.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import za.co.webber.asteroidsfxgl.EntityType;
import za.co.webber.asteroidsfxgl.GameConfig;

public class BulletFactory {

  public static Entity spawnBullet(Point2D position, double rotation, Point2D shipVelocity) {
    Point2D direction =
        new Point2D(
            Math.cos(Math.toRadians(rotation - 90)), Math.sin(Math.toRadians(rotation - 90)));

    Line bulletView = new Line(0, 0, 0, -4);
    bulletView.setStroke(Color.WHITE);
    bulletView.setStrokeWidth(2);

    Entity bullet =
        FXGL.entityBuilder()
            .type(EntityType.BULLET)
            .at(position)
            .view(bulletView)
            .bbox(new HitBox("BULLET", BoundingShape.circle(GameConfig.BULLET_COLLISION_RADIUS)))
            .with(new CollidableComponent(true))
            .with(new BulletComponent(direction, shipVelocity))
            .build();

    bullet.setRotation(rotation);
    return bullet;
  }
}
