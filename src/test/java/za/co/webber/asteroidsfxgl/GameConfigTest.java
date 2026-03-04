package za.co.webber.asteroidsfxgl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GameConfigTest {

  @Test
  void screenDimensionsArePositive() {
    assertTrue(GameConfig.SCREEN_WIDTH > 0);
    assertTrue(GameConfig.SCREEN_HEIGHT > 0);
  }

  @Test
  void playerConstantsArePositive() {
    assertTrue(GameConfig.PLAYER_ROTATION_SPEED > 0);
    assertTrue(GameConfig.PLAYER_THRUST > 0);
    assertTrue(GameConfig.INVINCIBILITY_DURATION > 0);
    assertTrue(GameConfig.PLAYER_COLLISION_RADIUS > 0);
    assertTrue(GameConfig.RESPAWN_DELAY_SECONDS > 0);
  }

  @Test
  void bulletConstantsArePositive() {
    assertTrue(GameConfig.BULLET_SPEED > 0);
    assertTrue(GameConfig.BULLET_LIFETIME > 0);
    assertTrue(GameConfig.BULLET_COLLISION_RADIUS > 0);
    assertTrue(GameConfig.BULLET_SPAWN_OFFSET > 0);
  }

  @Test
  void asteroidSpeedMultipliersIncreaseWithSmallerSize() {
    assertTrue(GameConfig.ASTEROID_SPEED_MULT_SMALL > GameConfig.ASTEROID_SPEED_MULT_MEDIUM);
    assertTrue(GameConfig.ASTEROID_SPEED_MULT_MEDIUM > GameConfig.ASTEROID_SPEED_MULT_LARGE);
  }

  @Test
  void scoreValuesIncreaseWithSmallerSize() {
    assertTrue(GameConfig.SCORE_SMALL_ASTEROID > GameConfig.SCORE_MEDIUM_ASTEROID);
    assertTrue(GameConfig.SCORE_MEDIUM_ASTEROID > GameConfig.SCORE_LARGE_ASTEROID);
  }

  @Test
  void maxAsteroidsIsPositive() {
    assertTrue(GameConfig.MAX_ASTEROIDS > 0);
  }
}
