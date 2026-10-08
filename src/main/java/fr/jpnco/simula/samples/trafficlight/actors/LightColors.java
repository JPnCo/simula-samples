package fr.jpnco.simula.samples.trafficlight.actors;

import fr.jpnco.simula.samples.trafficlight.states.LightState;
import java.awt.Color;

/**
 * Maps a {@link LightState} to the pixel {@link Color} used by the {@link TrafficLightGui}. The
 * mapping is extracted from the Swing painting code so it is independently testable.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-007, SC-004.
 */
public final class LightColors {

  /** The color of a green light circle. */
  private static final Color GREEN = new Color(34, 139, 34);

  /** The color of an orange light circle. */
  private static final Color ORANGE = new Color(255, 140, 0);

  /** The color of a red light circle. */
  private static final Color RED = new Color(220, 40, 40);

  /** The color of the ground between the road lanes. */
  public static final Color GROUND = new Color(24, 24, 28);

  /** The color of the road lane surface. */
  public static final Color ROAD = new Color(70, 70, 72);

  /** The color of a lane marking along the middle of a road. */
  public static final Color LANE_MARKING = new Color(200, 200, 200);

  /** The color of a vehicle. */
  public static final Color VEHICLE = new Color(255, 215, 0);

  /** Private constructor to prevent instantiation of this utility class. */
  private LightColors() {}

  /**
   * Returns the color of the given light state. Participates in: FR-007, SC-004.
   *
   * @param state the light state
   * @return the color to paint
   */
  public static Color of(final LightState state) {
    if (state == LightState.GREEN) {
      return GREEN;
    }
    if (state == LightState.ORANGE) {
      return ORANGE;
    }
    return RED;
  }
}
