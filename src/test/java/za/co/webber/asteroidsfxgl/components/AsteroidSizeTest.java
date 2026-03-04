package za.co.webber.asteroidsfxgl.components;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AsteroidSizeTest {

  @Test
  void allSizesHavePositiveRadius() {
    for (AsteroidSize size : AsteroidSize.values()) {
      assertTrue(size.getRadius() > 0, size.name() + " should have a positive radius");
    }
  }

  @Test
  void largerSizesHaveLargerRadius() {
    assertTrue(AsteroidSize.LARGE.getRadius() > AsteroidSize.MEDIUM.getRadius());
    assertTrue(AsteroidSize.MEDIUM.getRadius() > AsteroidSize.SMALL.getRadius());
  }

  @Test
  void exactlyThreeSizes() {
    assertEquals(3, AsteroidSize.values().length);
  }
}
