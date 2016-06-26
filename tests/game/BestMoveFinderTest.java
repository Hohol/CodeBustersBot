package game;

import org.testng.annotations.Test;

import static game.Move.*;
import static org.testng.Assert.*;

@Test
public class BestMoveFinderTest {
    @Test
    void testMoveWithAllowedRange() {
        assertEquals(Utils.moveToWithAllowedRange(0, 0, 0, 0, 10, 5), new Point(0, 0));
        assertEquals(Utils.moveToWithAllowedRange(1, 0, 0, 0, 10, 5), new Point(1, 0));
        assertEquals(Utils.moveToWithAllowedRange(10, 0, 0, 0, 10, 5), new Point(5, 0));
        assertEquals(Utils.moveToWithAllowedRange(1, 1, 2, 2, 1, 1), new Point(2, 2));
    }
}