package za.co.webber.asteroidsfxgl.hud;

import com.almasb.fxgl.dsl.FXGL;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import za.co.webber.asteroidsfxgl.GameConfig;
import za.co.webber.asteroidsfxgl.components.ShipShape;

/**
 * Instance-based HUD that retains node references. Create once in initUI(), then call update
 * methods to change displayed values without tearing down and re-adding nodes.
 */
public class HudDisplay {

  private final Text scoreText;
  private final Text highScoreText;
  private final HBox livesBox;

  // Transient overlay nodes (game-over, leaderboard)
  private final List<Node> overlayNodes = new ArrayList<>();

  /** Creates the HUD and adds all permanent nodes to the FXGL game scene. */
  public HudDisplay() {
    var ui = FXGL.getGameScene();

    // ── Score (upper-left) ─────────────────────────────────────────────
    scoreText = new Text("00");
    scoreText.setFill(Color.WHITE);
    scoreText.setFont(Font.font("Monospaced", GameConfig.HUD_SCORE_FONT_SIZE));
    scoreText.setTranslateX(GameConfig.HUD_MARGIN);
    scoreText.setTranslateY(GameConfig.HUD_MARGIN + 15);
    ui.addUINode(scoreText);

    // ── High-score (top-center) ───────────────────────────────────────
    highScoreText = new Text("00");
    highScoreText.setFill(Color.WHITE);
    highScoreText.setFont(Font.font("Monospaced", GameConfig.HUD_SCORE_FONT_SIZE));
    highScoreText.setTranslateY(GameConfig.HUD_MARGIN + 15);
    ui.addUINode(highScoreText);

    // ── Lives row (small ship icons below score) ──────────────────────
    livesBox = new HBox(GameConfig.HUD_LIVES_ICON_SPACING);
    livesBox.setTranslateX(GameConfig.HUD_MARGIN);
    livesBox.setTranslateY(GameConfig.HUD_MARGIN + 25);
    ui.addUINode(livesBox);
  }

  /** Update the displayed score text. */
  public void updateScore(int score) {
    scoreText.setText(String.format("%02d", score));
  }

  /** Update the displayed high-score text and re-centre it. */
  public void updateHighScore(int highScore) {
    highScoreText.setText(String.format("%02d", highScore));
    double textWidth = highScoreText.getLayoutBounds().getWidth();
    highScoreText.setTranslateX(GameConfig.SCREEN_WIDTH / 2.0 - textWidth / 2.0);
  }

  /** Rebuild the row of small ship icons for the given number of lives (excludes current ship). */
  public void updateLives(int lives) {
    livesBox.getChildren().clear();
    for (int i = 0; i < lives - 1; i++) {
      Path icon = ShipShape.createMiniShipIcon(GameConfig.HUD_LIVES_ICON_SCALE);
      livesBox.getChildren().add(icon);
    }
  }

  /** Show "GAME OVER" overlay centred on screen. */
  public void showGameOver() {
    var ui = FXGL.getGameScene();

    Text gameOverText = new Text("GAME OVER");
    gameOverText.setFill(Color.WHITE);
    gameOverText.setFont(Font.font("Monospaced", GameConfig.HUD_GAME_OVER_FONT_SIZE));

    double tw = gameOverText.getLayoutBounds().getWidth();
    gameOverText.setTranslateX(GameConfig.SCREEN_WIDTH / 2.0 - tw / 2.0);
    gameOverText.setTranslateY(GameConfig.SCREEN_HEIGHT / 2.0 - 50);

    ui.addUINode(gameOverText);
    overlayNodes.add(gameOverText);
  }

  /** Show the high-score leaderboard overlay, replacing any game-over text. */
  public void showLeaderboard(List<String> scores) {
    clearOverlays();
    var ui = FXGL.getGameScene();

    Text title = new Text("HIGH SCORES");
    title.setFill(Color.WHITE);
    title.setFont(Font.font("Monospaced", GameConfig.HUD_TITLE_FONT_SIZE));
    double titleWidth = title.getLayoutBounds().getWidth();
    title.setTranslateX(GameConfig.SCREEN_WIDTH / 2.0 - titleWidth / 2.0);
    title.setTranslateY(150);
    ui.addUINode(title);
    overlayNodes.add(title);

    for (int i = 0; i < scores.size(); i++) {
      Text scoreEntry = new Text(scores.get(i));
      scoreEntry.setFill(Color.WHITE);
      scoreEntry.setFont(Font.font("Monospaced", GameConfig.HUD_LEADERBOARD_FONT_SIZE));
      double sw = scoreEntry.getLayoutBounds().getWidth();
      scoreEntry.setTranslateX(GameConfig.SCREEN_WIDTH / 2.0 - sw / 2.0);
      scoreEntry.setTranslateY(200 + i * 30);
      ui.addUINode(scoreEntry);
      overlayNodes.add(scoreEntry);
    }

    Text restartText = new Text("PRESS SPACE TO START");
    restartText.setFill(Color.WHITE);
    restartText.setFont(Font.font("Monospaced", GameConfig.HUD_RESTART_FONT_SIZE));
    double rw = restartText.getLayoutBounds().getWidth();
    restartText.setTranslateX(GameConfig.SCREEN_WIDTH / 2.0 - rw / 2.0);
    restartText.setTranslateY(600);
    ui.addUINode(restartText);
    overlayNodes.add(restartText);
  }

  /** Remove all transient overlay nodes (game-over, leaderboard). */
  public void clearOverlays() {
    var ui = FXGL.getGameScene();
    overlayNodes.forEach(ui::removeUINode);
    overlayNodes.clear();
  }
}
