package za.co.webber.asteroidsfxgl.components;

import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;

/**
 * Single source of truth for all ship-related visual geometry. Used by PlayerFactory,
 * PlayerComponent (respawn/explode), and HudDisplay so the shape is defined in exactly one place.
 */
public final class ShipShape {

  private ShipShape() {}

  // ── Core geometry constants ────────────────────────────────────────────
  private static final double NOSE_Y = -12;
  private static final double LEG_X = 8;
  private static final double LEG_Y = 10;
  private static final double BAR_X = 7;
  private static final double BAR_Y = 7;

  /** Creates the outlined A-shaped ship path at the given scale. */
  public static Path createShip(double scale) {
    Path ship =
        new Path(
            new MoveTo(0, NOSE_Y),
            new LineTo(-LEG_X, LEG_Y),
            new MoveTo(0, NOSE_Y),
            new LineTo(LEG_X, LEG_Y),
            new MoveTo(-BAR_X, BAR_Y),
            new LineTo(BAR_X, BAR_Y));

    ship.setFill(Color.TRANSPARENT);
    ship.setStroke(Color.WHITE);
    ship.setStrokeWidth(2);
    ship.setStrokeLineCap(StrokeLineCap.ROUND);

    if (scale != 1.0) {
      ship.setScaleX(scale);
      ship.setScaleY(scale);
    }

    return ship;
  }

  /** Creates the thrust-flame polyline (initially hidden). */
  public static Polyline createFlame() {
    Polyline flame = new Polyline(-3.0, 12.0, 0.0, 18.0, 3.0, 12.0);
    flame.setStroke(Color.WHITE);
    flame.setVisible(false);
    return flame;
  }

  /**
   * Returns the three line fragments used for the ship explosion effect. Order: left leg, right
   * leg, crossbar.
   */
  public static List<Line> createExplosionFragments() {
    Line left = new Line(0, NOSE_Y, -LEG_X, LEG_Y);
    Line right = new Line(0, NOSE_Y, LEG_X, LEG_Y);
    Line bar = new Line(-BAR_X, BAR_Y, BAR_X, BAR_Y);

    for (Line l : List.of(left, right, bar)) {
      l.setStroke(Color.WHITE);
      l.setStrokeWidth(2);
    }

    return List.of(left, right, bar);
  }

  /**
   * Creates a small ship icon for the HUD lives display. This is the same A-shape used as the main
   * ship but at a smaller scale and with a thinner stroke.
   */
  public static Path createMiniShipIcon(double scale) {
    Path miniShip =
        new Path(
            new MoveTo(0, NOSE_Y),
            new LineTo(-LEG_X, LEG_Y),
            new MoveTo(0, NOSE_Y),
            new LineTo(LEG_X, LEG_Y),
            new MoveTo(-BAR_X, BAR_Y),
            new LineTo(BAR_X, BAR_Y));

    miniShip.setStroke(Color.WHITE);
    miniShip.setStrokeWidth(1.0);
    miniShip.setFill(null);

    miniShip.setScaleX(scale);
    miniShip.setScaleY(scale);

    return miniShip;
  }
}
