package za.co.webber.asteroidsfxgl.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polyline;
import za.co.webber.asteroidsfxgl.EntityType;
import za.co.webber.asteroidsfxgl.GameConfig;

public class PlayerFactory implements EntityFactory {

  @Spawns("player")
  public Entity newPlayer(SpawnData data) {
    Path ship = ShipShape.createShip(1.0);
    Polyline flame = ShipShape.createFlame();

    return FXGL.entityBuilder(data)
        .type(EntityType.PLAYER)
        .view(ship)
        .view(flame)
        .bbox(new HitBox("PLAYER", BoundingShape.circle(GameConfig.PLAYER_COLLISION_RADIUS)))
        .rotationOrigin(0, 0)
        .with(new CollidableComponent(true))
        .with(new PlayerComponent(flame))
        .build();
  }
}
