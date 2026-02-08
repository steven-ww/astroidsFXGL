package za.co.webber.asteroidsfxgl.hud;

import com.almasb.fxgl.dsl.FXGL;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class HudDisplay {

  private static final int HUD_MARGIN = 20;

  public static Path createMiniShip(double scale) {
    Path miniShip =
        new Path(
            new MoveTo(0, -12),
            new LineTo(-8, 10),
            new MoveTo(0, -12),
            new LineTo(8, 10),
            new MoveTo(-7, 7),
            new LineTo(7, 7));

    miniShip.setStroke(Color.WHITE);
    miniShip.setStrokeWidth(1.0);
    miniShip.setFill(null);

    miniShip.setScaleX(scale);
    miniShip.setScaleY(scale);

    return miniShip;
  }

  public static void drawLives(int lives) {
    var ui = FXGL.getGameScene();

    // Clear old icons (important when lives change)
    List<Node> toRemove =
        ui.getUINodes().stream().filter(n -> "LIFE".equals(n.getUserData())).toList();

    toRemove.forEach(ui::removeUINode);

    for (int i = 0; i < lives - 1; i++) { // do NOT show current ship
      Path miniShip = createMiniShip(0.6);

      miniShip.setTranslateX(HUD_MARGIN + i * 15);
      miniShip.setTranslateY(HUD_MARGIN + 40); // Pushed down slightly

      miniShip.setUserData("LIFE"); // tag for easy removal
      ui.addUINode(miniShip);
    }
  }

  public static void drawScore(int score) {
    var ui = FXGL.getGameScene();

    // Clear old score display
    List<Node> toRemove =
        ui.getUINodes().stream().filter(n -> "SCORE".equals(n.getUserData())).toList();

    toRemove.forEach(ui::removeUINode);

    // Create score text in classic arcade style
    Text scoreText = new Text(String.format("%02d", score));
    scoreText.setFill(Color.WHITE);
    scoreText.setFont(Font.font("Monospaced", 24));

    // Position in upper left above the lives, like the original game
    scoreText.setTranslateX(HUD_MARGIN);
    scoreText.setTranslateY(HUD_MARGIN + 15);

    scoreText.setUserData("SCORE");
    ui.addUINode(scoreText);
  }

  public static void drawHighScore() {
    var ui = FXGL.getGameScene();

    // Clear old score display
    List<Node> toRemove =
        ui.getUINodes().stream().filter(n -> "HIGHSCORE".equals(n.getUserData())).toList();

    toRemove.forEach(ui::removeUINode);

    // Clear Game Over / Leaderboard if they exist
    ui.getUINodes().stream()
        .filter(
            n -> "GAME_OVER_UI".equals(n.getUserData()) || "LEADERBOARD_UI".equals(n.getUserData()))
        .toList()
        .forEach(ui::removeUINode);

    // Create score text in classic arcade style
    Text scoreText = new Text(String.format("%02d", FXGL.geti("highScore")));
    scoreText.setFill(Color.WHITE);
    scoreText.setFont(Font.font("Monospaced", 24));

    // Position in center
    double textWidth = scoreText.getLayoutBounds().getWidth();
    scoreText.setTranslateX(1280 / 2.0 - textWidth / 2.0);
    scoreText.setTranslateY(HUD_MARGIN + 15);

    scoreText.setUserData("HIGHSCORE");
    ui.addUINode(scoreText);
  }

  public static void showGameOver() {
    var ui = FXGL.getGameScene();

    Text gameOverText = new Text("GAME OVER");
    gameOverText.setFill(Color.WHITE);
    gameOverText.setFont(Font.font("Monospaced", 48));

    double textWidth = gameOverText.getLayoutBounds().getWidth();
    gameOverText.setTranslateX(1280 / 2.0 - textWidth / 2.0);
    gameOverText.setTranslateY(720 / 2.0 - 50);

    gameOverText.setUserData("GAME_OVER_UI");
    ui.addUINode(gameOverText);
  }

  public static void showLeaderboard(List<String> scores) {
    var ui = FXGL.getGameScene();

    // Remove any existing game over UI if we are transitioning to leaderboard
    ui.getUINodes().stream()
        .filter(n -> "GAME_OVER_UI".equals(n.getUserData()))
        .toList()
        .forEach(ui::removeUINode);

    Text title = new Text("HIGH SCORES");
    title.setFill(Color.WHITE);
    title.setFont(Font.font("Monospaced", 32));
    double titleWidth = title.getLayoutBounds().getWidth();
    title.setTranslateX(1280 / 2.0 - titleWidth / 2.0);
    title.setTranslateY(150);
    title.setUserData("LEADERBOARD_UI");
    ui.addUINode(title);

    for (int i = 0; i < scores.size(); i++) {
      String text = scores.get(i);

      Text scoreText = new Text(text);
      scoreText.setFill(Color.WHITE);
      scoreText.setFont(Font.font("Monospaced", 20));
      double sw = scoreText.getLayoutBounds().getWidth();
      scoreText.setTranslateX(1280 / 2.0 - sw / 2.0);
      scoreText.setTranslateY(200 + i * 30);
      scoreText.setUserData("LEADERBOARD_UI");
      ui.addUINode(scoreText);
    }

    Text restartText = new Text("PRESS SPACE TO START");
    restartText.setFill(Color.WHITE);
    restartText.setFont(Font.font("Monospaced", 24));
    double rw = restartText.getLayoutBounds().getWidth();
    restartText.setTranslateX(1280 / 2.0 - rw / 2.0);
    restartText.setTranslateY(600);
    restartText.setUserData("LEADERBOARD_UI");
    ui.addUINode(restartText);
  }
}
