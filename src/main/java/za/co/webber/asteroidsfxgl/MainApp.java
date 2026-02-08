package za.co.webber.asteroidsfxgl;

import static java.lang.Math.min;
import static za.co.webber.asteroidsfxgl.hud.HudDisplay.drawHighScore;
import static za.co.webber.asteroidsfxgl.hud.HudDisplay.drawLives;
import static za.co.webber.asteroidsfxgl.hud.HudDisplay.drawScore;
import static za.co.webber.asteroidsfxgl.hud.HudDisplay.showGameOver;
import static za.co.webber.asteroidsfxgl.hud.HudDisplay.showLeaderboard;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
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
    FXGL.set("isGameOver", false);
    FXGL.getGameWorld().addEntityFactory(new PlayerFactory());
    FXGL.getGameWorld().addEntityFactory(new AsteroidFactory());
    Entity player = FXGL.spawn("player", 640, 360); // 640 360
    playerComp = player.getComponent(PlayerComponent.class);
    drawLives(FXGL.geti("lives"));
    drawScore(FXGL.geti("score"));
    drawHighScore();

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
      gameOver();
    }
  }

  private void gameOver() {
    FXGL.set("isGameOver", true);
    // Stop all game logic/entities if necessary
    FXGL.getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);

    showGameOver();

    int score = FXGL.geti("score");
    List<ScoreData> scores = getHighScores();

    boolean isHighScore = scores.size() < 10 || score > scores.get(scores.size() - 1).score();

    if (isHighScore) {
      FXGL.getDialogService()
          .showInputBox(
              "New High Score! Enter 3 characters:",
              (String name) -> {
                String entryName =
                    (name == null || name.trim().isEmpty()) ? "AAA" : name.toUpperCase();
                if (entryName.length() > 3) {
                  entryName = entryName.substring(0, 3);
                }

                scores.add(new ScoreData(entryName, score));
                scores.sort(Comparator.comparingInt(ScoreData::score).reversed());

                List<ScoreData> topTen = scores.stream().limit(10).collect(Collectors.toList());
                saveHighScores(topTen);

                showLeaderboard(
                    topTen.stream()
                        .map(sd -> String.format("%-3s  %d", sd.name(), sd.score()))
                        .collect(Collectors.toList()));
              });
    } else {
      FXGL.runOnce(
          () -> {
            showLeaderboard(
                scores.stream()
                    .limit(10)
                    .map(sd -> String.format("%-3s  %d", sd.name(), sd.score()))
                    .collect(Collectors.toList()));
          },
          javafx.util.Duration.seconds(2));
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
    if (FXGL.geti("score") > FXGL.geti("highScore")) {
      FXGL.set("highScore", FXGL.geti("score"));
      drawHighScore();
    }
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
                if (FXGL.getb("isGameOver")) {
                  FXGL.getGameController().startNewGame();
                  return;
                }

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
    vars.put("isGameOver", false);
    vars.put("pixelsMoved", 0);
    vars.put("lives", 3);
    vars.put("score", 0);
    vars.put("level", 0);
    vars.put("asteroidCount", 0);
    vars.put("bulletCount", 0);
    vars.put("highScore", getHighScore());
  }

  private int getHighScore() {
    List<ScoreData> scores = getHighScores();
    return scores.isEmpty() ? 0 : scores.get(0).score();
  }

  private record ScoreData(String name, int score) {}

  private List<ScoreData> getHighScores() {
    try {
      Path path = Path.of("highscore.txt");
      if (!Files.exists(path)) {
        return new ArrayList<>();
      }
      List<String> allLines = Files.readAllLines(path);
      return allLines.stream()
          .map(line -> line.split(","))
          .filter(split -> split.length == 2)
          .map(split -> new ScoreData(split[0], Integer.parseInt(split[1])))
          .sorted(Comparator.comparingInt(ScoreData::score).reversed())
          .collect(Collectors.toList());
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  private void saveHighScores(List<ScoreData> scores) {
    try {
      List<String> lines =
          scores.stream()
              .limit(10)
              .map(sd -> sd.name() + "," + sd.score())
              .collect(Collectors.toList());
      Files.write(Path.of("highscore.txt"), lines);
    } catch (Exception e) {
      e.printStackTrace();
    }
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
