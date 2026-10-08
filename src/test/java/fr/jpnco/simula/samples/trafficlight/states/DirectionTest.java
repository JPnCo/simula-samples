package fr.jpnco.simula.samples.trafficlight.states;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DirectionTest {

  @Test
  void deltas_match_cardinal_directions() {
    assertEquals(-1, Direction.NORTH.rowDelta());
    assertEquals(0, Direction.NORTH.colDelta());
    assertEquals(1, Direction.SOUTH.rowDelta());
    assertEquals(0, Direction.SOUTH.colDelta());
    assertEquals(0, Direction.EAST.rowDelta());
    assertEquals(1, Direction.EAST.colDelta());
    assertEquals(0, Direction.WEST.rowDelta());
    assertEquals(-1, Direction.WEST.colDelta());
  }

  @Test
  void vertical_directions_are_north_and_south() {
    assertTrue(Direction.NORTH.isVertical());
    assertTrue(Direction.SOUTH.isVertical());
    assertFalse(Direction.EAST.isVertical());
    assertFalse(Direction.WEST.isVertical());
  }

  @Test
  void turnRight_rotates_clockwise() {
    assertEquals(Direction.EAST, Direction.NORTH.turnRight());
    assertEquals(Direction.SOUTH, Direction.EAST.turnRight());
    assertEquals(Direction.WEST, Direction.SOUTH.turnRight());
    assertEquals(Direction.NORTH, Direction.WEST.turnRight());
  }

  @Test
  void turnLeft_rotates_counterclockwise() {
    assertEquals(Direction.WEST, Direction.NORTH.turnLeft());
    assertEquals(Direction.NORTH, Direction.EAST.turnLeft());
    assertEquals(Direction.EAST, Direction.SOUTH.turnLeft());
    assertEquals(Direction.SOUTH, Direction.WEST.turnLeft());
  }

  @Test
  void enum_has_exactly_four_directions() {
    assertEquals(4, Direction.values().length);
  }
}
