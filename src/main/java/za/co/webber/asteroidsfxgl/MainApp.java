package za.co.webber.asteroidsfxgl;

import static java.lang.Math.min;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import za.co.webber.asteroidsfxgl.components.AsteroidComponent;
import za.co.webber.asteroidsfxgl.components.AsteroidExplosion;
import za.co.webber.asteroidsfxgl.components.AsteroidFactory;
import za.co.webber.asteroidsfxgl.components.AsteroidSize;
import za.co.webber.asteroidsfxgl.components.BulletFactory;
import za.co.webber.asteroidsfxgl.components.PlayerComponent;
import za.co.webber.asteroidsfxgl.components.PlayerFactory;
import za.co.webber.asteroidsfxgl.hud.HudDisplay;

public class MainApp extends GameApplication {

  private PlayerComponent playerComp;
  private HudDisplay hud;

  @Override
  protected void initSettings(GameSettings settings) {
    settings.setTitle("Astroids FXGL");
    settings.setWidth(GameConfig.SCREEN_WIDTH);
    settings.setHeight(GameConfig.SCREEN_HEIGHT);
  }

  @Override
  protected void initUI() {
    FXGL.getGameScene().setBackgroundColor(Color.BLACK);
    hud = new HudDisplay();
  }

  @Override
  protected void initGame() {
    FXGL.set("isGameOver", false);
    FXGL.getGameWorld().addEntityFactory(new PlayerFactory());
    FXGL.getGameWorld().addEntityFactory(new AsteroidFactory());

    double cx = GameConfig.SCREEN_WIDTH / 2.0;
    double cy = GameConfig.SCREEN_HEIGHT / 2.0;
    Entity player = FXGL.spawn("player", cx, cy);
    playerComp = player.getComponent(PlayerComponent.class);

    hud.updateLives(FXGL.geti("lives"));
    hud.updateScore(FXGL.geti("score"));
    hud.updateHighScore(FXGL.geti("highScore"));
    hud.clearOverlays();

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
                if (pc.isInvincible()) {
                  return;
                }
                lifeLost(pc);
              }
            });

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

                AsteroidExplosion.explode(x, y, size);
                handleAsteroidDestroyed(size, x, y);
              }
            });
  }

  private void lifeLost(PlayerComponent playerComp) {
    playerComp.explode();
    FXGL.inc("lives", -1);
    hud.updateLives(FXGL.geti("lives"));

    if (FXGL.geti("lives") > 0) {
      FXGL.runOnce(
          () -> {
            double cx = GameConfig.SCREEN_WIDTH / 2.0;
            double cy = GameConfig.SCREEN_HEIGHT / 2.0;
            playerComp.respawn(cx, cy);
          },
          javafx.util.Duration.seconds(GameConfig.RESPAWN_DELAY_SECONDS));
    } else {
      gameOver();
    }
  }

  private void gameOver() {
    FXGL.set("isGameOver", true);
    FXGL.getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);

    hud.showGameOver();

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

                hud.showLeaderboard(
                    topTen.stream()
                        .map(sd -> String.format("%-3s  %d", sd.name(), sd.score()))
                        .collect(Collectors.toList()));
              });
    } else {
      FXGL.runOnce(
          () -> {
            hud.showLeaderboard(
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
        addScore(GameConfig.SCORE_LARGE_ASTEROID);
        spawnAsteroidChildren(AsteroidSize.MEDIUM, x, y, 2);
      }
      case MEDIUM -> {
        addScore(GameConfig.SCORE_MEDIUM_ASTEROID);
        spawnAsteroidChildren(AsteroidSize.SMALL, x, y, 2);
      }
      case SMALL -> addScore(GameConfig.SCORE_SMALL_ASTEROID);
    }
    if (FXGL.geti("asteroidCount") == 0) {
      FXGL.inc("level", 1);
      FXGL.inc("lives", 1);
      hud.updateLives(FXGL.geti("lives"));
      int currentLevel = FXGL.geti("level");
      spawnLevelAsteroids(min(currentLevel * 2 + 4, GameConfig.MAX_ASTEROIDS));
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
    hud.updateScore(FXGL.geti("score"));
    if (FXGL.geti("score") > FXGL.geti("highScore")) {
      FXGL.set("highScore", FXGL.geti("score"));
      hud.updateHighScore(FXGL.geti("highScore"));
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

                Point2D bulletSpawn = playerComp.getNosePosition(GameConfig.BULLET_SPAWN_OFFSET);
                double rotation = playerComp.getRotation();

                Vec2 shipVelocityVec = playerComp.getVelocity();
                Point2D shipVelocity = new Point2D(shipVelocityVec.x, shipVelocityVec.y);

                Entity bullet = BulletFactory.spawnBullet(bulletSpawn, rotation, shipVelocity);
                FXGL.getGameWorld().addEntity(bullet);
              }
            },
            KeyCode.SPACE);
  }

  @Override
  protected void initGameVars(Map<String, Object> vars) {
    vars.put("isGameOver", false);
    vars.put("pixelsMoved", 0);
    vars.put("lives", 3);
    vars.put("score", 0);
    vars.put("level", 0);
    vars.put("asteroidCount", 0);
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
    double w = GameConfig.SCREEN_WIDTH;
    double h = GameConfig.SCREEN_HEIGHT;
    double margin = GameConfig.ASTEROID_SPAWN_MARGIN;

    int edge = (int) (Math.random() * 4);
    double x;
    double y;
    switch (edge) {
      case 0 -> {
        x = -margin;
        y = Math.random() * h;
      }
      case 1 -> {
        x = w + margin;
        y = Math.random() * h;
      }
      case 2 -> {
        x = Math.random() * w;
        y = -margin;
      }
      default -> {
        x = Math.random() * w;
        y = h + margin;
      }
    }

    FXGL.spawn("asteroid", x, y);
  }

  void main(String[] args) {
    launch(args);
  }
}
