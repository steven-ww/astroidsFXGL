package za.co.webber.asteroidsfxgl.components;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polyline;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ShipShapeTest {

  private static boolean javaFxAvailable = false;

  @BeforeAll
  static void initJavaFx() {
    // JavaFX toolkit must be initialised before creating Node objects.
    // In headless CI environments this may not be possible.
    try {
      javafx.application.Platform.startup(() -> {});
      javaFxAvailable = true;
    } catch (IllegalStateException e) {
      // Already initialised — that's fine
      javaFxAvailable = true;
    } catch (Exception ignored) {
      // Headless environment — tests will be skipped
    }
  }

  private static void requireJavaFx() {
    assumeTrue(javaFxAvailable, "JavaFX toolkit not available (headless environment)");
  }

  @Test
  void createShipReturnsNonNullPath() {
    requireJavaFx();
    Path ship = ShipShape.createShip(1.0);
    assertNotNull(ship);
    assertFalse(ship.getElements().isEmpty());
  }

  @Test
  void createShipScalesCorrectly() {
    requireJavaFx();
    Path ship = ShipShape.createShip(2.0);
    assertEquals(2.0, ship.getScaleX());
    assertEquals(2.0, ship.getScaleY());
  }

  @Test
  void createShipDefaultScaleIsOne() {
    requireJavaFx();
    Path ship = ShipShape.createShip(1.0);
    assertEquals(1.0, ship.getScaleX());
    assertEquals(1.0, ship.getScaleY());
  }

  @Test
  void createFlameReturnsHiddenPolyline() {
    requireJavaFx();
    Polyline flame = ShipShape.createFlame();
    assertNotNull(flame);
    assertFalse(flame.isVisible());
  }

  @Test
  void createExplosionFragmentsReturnsThreeLines() {
    requireJavaFx();
    List<Line> fragments = ShipShape.createExplosionFragments();
    assertEquals(3, fragments.size());
    for (Line l : fragments) {
      assertNotNull(l);
      assertEquals(2.0, l.getStrokeWidth());
    }
  }

  @Test
  void createMiniShipIconIsScaled() {
    requireJavaFx();
    Path icon = ShipShape.createMiniShipIcon(0.5);
    assertNotNull(icon);
    assertEquals(0.5, icon.getScaleX());
    assertEquals(0.5, icon.getScaleY());
  }
}
