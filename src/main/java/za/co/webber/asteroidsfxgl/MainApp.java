package za.co.webber.asteroidsfxgl;

import static java.lang.Math.min;
import static za.co.webber.asteroidsfxgl.hud.HudDisplay.drawLives;
import static za.co.webber.asteroidsfxgl.hud.HudDisplay.drawScore;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import java.util.Map;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import za.co.webber.asteroidsfxgl.components.AsteroidComponent;
import za.co.webber.asteroidsfxgl.components.AsteroidFactory;
import za.co.webber.asteroidsfxgl.components.AsteroidSize;
import za.co.webber.asteroidsfxgl.components.BulletFactory;
import za.co.webber.asteroidsfxgl.components.PlayerComponent;
import za.co.webber.asteroidsfxgl.components.PlayerFactory;

public class MainApp extends GameApplication {

  private PlayerComponent playerComp;
  private static final int MAX_ASTEROIDS = 10;
  private static final int MAX_BULLETS = 4;

  @Override
  protected void initSettings(GameSettings settings) {
    settings.setTitle("Astroids FXGL");
    settings.setWidth(1280);
    settings.setHeight(720);
  }

  @Override
  protected void initUI() {
    Text textPixels = new Text();
    textPixels.setTranslateX(50); // x = 50
    textPixels.setTranslateY(100); // y = 100
    textPixels.textProperty().bind(FXGL.getWorldProperties().intProperty("pixelsMoved").asString());

    FXGL.getGameScene().addUINode(textPixels); // add to the scene graph
    FXGL.getGameScene().setBackgroundColor(Color.BLACK);
  }

  @Override
  protected void initGame() {
    FXGL.getGameWorld().addEntityFactory(new PlayerFactory());
    FXGL.getGameWorld().addEntityFactory(new AsteroidFactory());
    Entity player = FXGL.spawn("player", 640, 360); // 640 360
    playerComp = player.getComponent(PlayerComponent.class);
    drawLives(FXGL.geti("lives"));
    drawScore(FXGL.geti("score"));

    spawnLevelAsteroids(FXGL.geti("level") * 2 + 4);
  }

  private void spawnLevelAsteroids(int count) {
    for (int i = 0; i < count; i++) {
      FXGL.inc("asteroidCount", 1);
      spawnLargeAsteroidOffscreen();
    }
  }

  @Override
  protected void initPhysics() {
    FXGL.getPhysicsWorld()
        .addCollisionHandler(
            new CollisionHandler(EntityType.PLAYER, EntityType.ASTEROID) {
              @Override
              protected void onCollisionBegin(Entity player, Entity asteroid) {
                PlayerComponent pc = player.getComponent(PlayerComponent.class);

                // Ignore collision during invincibility
                if (pc.isInvincible()) {
                  return;
                }

                lifeLost(pc);
              }
            });

    // Bullet hits asteroid: destroy both, split asteroid by size, and add score
    FXGL.getPhysicsWorld()
        .addCollisionHandler(
            new CollisionHandler(EntityType.BULLET, EntityType.ASTEROID) {
              @Override
              protected void onCollisionBegin(Entity bullet, Entity asteroid) {
                AsteroidComponent comp = asteroid.getComponent(AsteroidComponent.class);
                AsteroidSize size = comp.getSize();

                double x = asteroid.getX();
                double y = asteroid.getY();

                bullet.removeFromWorld();
                asteroid.removeFromWorld();

                handleAsteroidDestroyed(size, x, y);
              }
            });
  }

  private void lifeLost(PlayerComponent playerComp) {
    playerComp.explode();
    FXGL.inc("lives", -1);
    drawLives(FXGL.geti("lives"));

    if (FXGL.geti("lives") > 0) {
      // Respawn after explosion animation (1.5 seconds to match fragment lifetime)
      FXGL.runOnce(
          () -> {
            playerComp.respawn(640, 360); // Center of screen
          },
          javafx.util.Duration.seconds(1.5));
    } else {
      FXGL.getNotificationService().pushNotification("Game Over!");
    }
  }

  private void handleAsteroidDestroyed(AsteroidSize size, double x, double y) {
    FXGL.inc("asteroidCount", -1);
    switch (size) {
      case LARGE -> {
        addScore(20);
        spawnAsteroidChildren(AsteroidSize.MEDIUM, x, y, 2);
      }
      case MEDIUM -> {
        addScore(50);
        spawnAsteroidChildren(AsteroidSize.SMALL, x, y, 2);
      }
      case SMALL -> addScore(100);
    }
    if (FXGL.geti("asteroidCount") == 0) {
      FXGL.inc("level", 1);
      FXGL.inc("lives", 1);
      drawLives(FXGL.geti("lives"));
      int currentLevel = FXGL.geti("level");
      spawnLevelAsteroids(min(currentLevel * 2 + 4, MAX_ASTEROIDS));
    }
  }

  private void spawnAsteroidChildren(AsteroidSize childSize, double x, double y, int count) {
    for (int i = 0; i < count; i++) {
      FXGL.inc("asteroidCount", 1);
      SpawnData data = new SpawnData(x, y).put("size", childSize);
      FXGL.spawn("asteroid", data);
    }
  }

  private void addScore(int delta) {
    FXGL.inc("score", delta);
    drawScore(FXGL.geti("score"));
  }

  @Override
  protected void initInput() {
    Input input = FXGL.getInput();

    input.addAction(
        new UserAction("Turn Left") {
          @Override
          protected void onAction() {
            playerComp.turnLeft();
          }
        },
        KeyCode.D);

    input.addAction(
        new UserAction("Turn Right") {
          @Override
          protected void onAction() {
            playerComp.turnRight();
          }
        },
        KeyCode.A);

    input.addAction(
        new UserAction("Thrust") {
          @Override
          protected void onAction() {
            playerComp.thrustOn();
          }

          protected void onActionEnd() {
            playerComp.thrustOff();
          }
        },
        KeyCode.W);

    FXGL.getInput()
        .addAction(
            new UserAction("Shoot") {
              @Override
              protected void onActionBegin() {

                // Limit active player bullets to MAX_BULLETS
                try {
                  if (FXGL.geti("bulletCount") >= MAX_BULLETS) {
                    return;
                  }
                } catch (Exception ignored) {
                  // If bulletCount is not yet initialized, allow shooting (initGameVars will set
                  // it)
                }

                // Get ship position & rotation
                Point2D bulletSpawn = playerComp.getNosePosition(14);
                //        Point2D shipPos = playerComp.getCenter();
                double rotation = playerComp.getRotation();

                // Get ship velocity if you have one
                Vec2 shipVelocityVec = playerComp.getVelocity();
                Point2D shipVelocity = new Point2D(shipVelocityVec.x, shipVelocityVec.y);

                Entity bullet =
                    BulletFactory.spawnBullet(
                        //                shipPos,
                        bulletSpawn, rotation, shipVelocity);

                FXGL.getGameWorld().addEntity(bullet);
                // Increment bullet count when we successfully add a bullet
                FXGL.inc("bulletCount", 1);
              }
            },
            KeyCode.SPACE);
  }

  /**
   * Initializes game variables that are accessible globally via FXGL.get*() methods. These
   * variables can be used for cross-class access, UI data binding, and save/load functionality.
   *
   * @param vars Map to populate with initial game state variables
   */
  @Override
  protected void initGameVars(Map<String, Object> vars) {
    vars.put("pixelsMoved", 0);
    vars.put("lives", 3);
    vars.put("score", 0);
    vars.put("level", 0);
    vars.put("asteroidCount", 0);
    vars.put("bulletCount", 0);
  }

  private void spawnLargeAsteroidOffscreen() {
    double w = FXGL.getAppWidth();
    double h = FXGL.getAppHeight();
    double margin = 40; // spawn just beyond the edge

    int edge = (int) (Math.random() * 4); // 0=left,1=right,2=top,3=bottom
    double x;
    double y;
    switch (edge) {
      case 0: // left
        x = -margin;
        y = Math.random() * h;
        break;
      case 1: // right
        x = w + margin;
        y = Math.random() * h;
        break;
      case 2: // top
        x = Math.random() * w;
        y = -margin;
        break;
      default: // bottom
        x = Math.random() * w;
        y = h + margin;
        break;
    }

    FXGL.spawn("asteroid", x, y);
  }

  void main(String[] args) {
    launch(args);
  }
}
