package fr.jpnco.simula.samples.trafficlight.states;

/**
 * The three states a traffic light band can be in. A band is {@link #GREEN} for most of its turn,
 * turns {@link #ORANGE} for a few seconds just before the other band takes over, and is {@link
 * #RED} while the other band is active. A vehicle may cross an intersection only when the band it
 * uses is {@link #GREEN}.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-002, FR-004.
 */
public enum LightState {

  /** The band may flow. */
  GREEN,

  /** The band is about to turn red; a vehicle must stop at the intersection. */
  ORANGE,

  /** The band is stopped; the other band may flow. */
  RED,
}
