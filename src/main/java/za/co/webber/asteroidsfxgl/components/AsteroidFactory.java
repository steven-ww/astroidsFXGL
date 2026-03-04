package za.co.webber.asteroidsfxgl.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import java.util.concurrent.ThreadLocalRandom;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import za.co.webber.asteroidsfxgl.EntityType;

public class AsteroidFactory implements EntityFactory {

  // Four distinct jagged polygon outlines for visual variety
  private static final double[][] SHAPE_VARIANTS = {
    {
      -26, -10, -20, -22, -8, -28, 6, -26, 18, -20, 28, -8, 26, 4, 26, 16, 16, 24, 4, 26, -8, 24,
      -16, 18, -22, 10, -30, 2, -28, -6, -24, -14
    },
    {-18, -24, -4, -28, 14, -24, 24, -14, 28, 2, 22, 18, 10, 26, -6, 24, -20, 16, -28, 4, -24, -10},
    {
      -22, -16, -10, -26, 8, -28, 22, -18, 28, -4, 24, 14, 14, 26, -4, 22, -18, 14, -26, 0, -20, -12
    },
    {-24, -8, -16, -24, 0, -28, 16, -22, 26, -10, 28, 6, 20, 20, 6, 26, -10, 22, -24, 12, -28, -2}
  };

  private static Polygon createAsteroidShape(double scale) {
    int variant = ThreadLocalRandom.current().nextInt(SHAPE_VARIANTS.length);
    Polygon p = new Polygon(SHAPE_VARIANTS[variant]);
    p.setFill(Color.TRANSPARENT);
    p.setStroke(Color.WHITE);
    p.setStrokeWidth(2);

    p.setScaleX(scale);
    p.setScaleY(scale);

    return p;
  }

  private static Polygon createAsteroidForSize(AsteroidSize size) {
    return switch (size) {
      case LARGE -> createAsteroidShape(1.0);
      case MEDIUM -> createAsteroidShape(0.6);
      case SMALL -> createAsteroidShape(0.35);
    };
  }

  @Spawns("asteroid")
  public Entity newAsteroid(SpawnData data) {
    AsteroidSize size = data.hasKey("size") ? data.get("size") : AsteroidSize.LARGE;
    Polygon rock = createAsteroidForSize(size);

    return FXGL.entityBuilder(data)
        .type(EntityType.ASTEROID)
        .view(rock)
        .bbox(new HitBox("ASTEROID", BoundingShape.circle(size.getRadius())))
        .with(new CollidableComponent(true))
        .with(new AsteroidComponent(size))
        .build();
  }
}
