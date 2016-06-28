package game;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

@Test
public class PhantomUpdaterTest {
    PhantomUpdater phantomUpdater;

    @BeforeMethod
    void init() {
        phantomUpdater = new PhantomUpdater(AbstractBestMoveFinderTest.createTestGameParameters());
    }

    @Test
    void test() {
        check(
                asList(),
                asList(buster(0, 0, 0).build()),
                asList(buster(1, 1, 0).build()),
                asList(buster(1, 1, 0).build())
        );
    }

    @Test
    void test2() {
        check(
                asList(),
                asList(
                        buster(0, 0, 0).build(),
                        buster(0, 0, 1).build()
                ),
                asList(buster(1, 1, 1).build()),
                asList(
                        buster(0, 0, 0).build(),
                        buster(1, 1, 1).build()
                )
        );
    }

    @Test
    void testMoveToBase() {
        check(
                asList(),
                asList(
                        buster(50, 25, 0).carryingGhost().build()
                ),
                asList(),
                asList(
                        buster(50, 27, 0).carryingGhost().build()
                ),
                new Point(50, 50)
        );
    }

    @Test
    void removeIfYouHaveVision() {
        check(
                asList(buster(50, 34, 1).build()),
                asList(
                        buster(50, 25, 0).carryingGhost().build()
                ),
                asList(),
                asList(),
                new Point(50, 50)
        );
    }

    @Test
    void removeIfReleasedGhost() {
        check(
                asList(),
                asList(
                        buster(50, 46, 0).carryingGhost().build()
                ),
                asList(),
                asList(),
                new Point(50, 50)
        );
    }

    // --- utils

    private void check(List<Buster> allies, List<Buster> phantomEnemies, List<Buster> enemies, List<Buster> expected) {
        check(allies, phantomEnemies, enemies, expected, new Point(0, 0));
    }

    private void check(List<Buster> allies, List<Buster> phantomEnemies, List<Buster> enemies, List<Buster> expected, Point enemyBase) {
        assertEquals(
                phantomUpdater.updatePhantomEnemies(allies, phantomEnemies, enemies, enemyBase),
                expected
        );
    }

    private BusterBuilder buster(int x, int y, int id) {
        return new BusterBuilder(x, y, id);
    }
}