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
        checkEnemies(
                asList(),
                asList(buster(0, 0, 0).build()),
                asList(buster(1, 1, 0).build()),
                asList(buster(1, 1, 0).build())
        );
    }

    @Test
    void test2() {
        checkEnemies(
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
        checkEnemies(
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
        checkEnemies(
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
        checkEnemies(
                asList(),
                asList(
                        buster(50, 46, 0).carryingGhost().build()
                ),
                asList(),
                asList(),
                new Point(50, 50)
        );
    }

    @Test
    void testGhosts() {
        List<Ghost> ghosts = asList(ghost(0, 0, 0));
        List<Ghost> phantomGhosts = asList();
        List<Buster> allies = asList();
        List<Buster> enemies = asList();
        List<Ghost> expected = asList(ghost(0, 0, 0));
        checkGhosts(
                ghosts,
                phantomGhosts,
                allies,
                enemies,
                expected
        );
    }

    @Test
    void testGhosts2() {
        List<Ghost> ghosts = asList();
        List<Ghost> phantomGhosts = asList(ghost(0, 0, 0));
        List<Buster> allies = asList();
        List<Buster> enemies = asList();
        List<Ghost> expected = asList(ghost(0, 0, 0));
        checkGhosts(
                ghosts,
                phantomGhosts,
                allies,
                enemies,
                expected
        );
    }

    @Test
    void testGhosts3() {
        List<Ghost> ghosts = asList(ghost(1, 1, 0));
        List<Ghost> phantomGhosts = asList(ghost(0, 0, 0));
        List<Buster> allies = asList();
        List<Buster> enemies = asList();
        List<Ghost> expected = asList(ghost(1, 1, 0));
        checkGhosts(
                ghosts,
                phantomGhosts,
                allies,
                enemies,
                expected
        );
    }

    @Test
    void testGhostsVision() {
        List<Ghost> ghosts = asList();
        List<Ghost> phantomGhosts = asList(ghost(0, 0, 0));
        List<Buster> allies = asList(buster(0, 6, 0).build());
        List<Buster> enemies = asList();
        List<Ghost> expected = asList();
        checkGhosts(
                ghosts,
                phantomGhosts,
                allies,
                enemies,
                expected
        );
    }

    @Test
    void testGhostsMove() {
        List<Ghost> ghosts = asList();
        List<Ghost> phantomGhosts = asList(ghost(10, 10, 0));
        List<Buster> allies = asList(buster(4, 9, 0).build());
        List<Buster> enemies = asList(buster(4, 11, 1).build());
        List<Ghost> expected = asList(ghost(11, 10, 0));
        checkGhosts(
                ghosts,
                phantomGhosts,
                allies,
                enemies,
                expected
        );
    }

    @Test
    void testBusterCarriesGhost() {
        List<Ghost> ghosts = asList();
        List<Ghost> phantomGhosts = asList(ghost(10, 10, 0), ghost(10, 10, 1), ghost(10, 10, 2));
        List<Buster> allies = asList(buster(0, 0, 0).carryingGhost(0).build());
        List<Buster> enemies = asList(buster(0, 0, 1).carryingGhost(1).build());
        List<Ghost> expected = asList(ghost(10, 10, 2));
        checkGhosts(
                ghosts,
                phantomGhosts,
                allies,
                enemies,
                expected
        );
    }

    @Test
    void stunnedEnemyDropsGhost() {
        assertEquals(
                phantomUpdater.dropGhostFromStunnedEnemy(
                        buster(0, 10, 0).build(),
                        buster(0, 11, 0).carryingGhost(3).build()
                ),
                ghost(0, 13, 3)
        );
    }

    // --- utils

    private Ghost ghost(int x, int y, int id) {
        return new Ghost(id, x, y, 0, 0);
    }

    private void checkGhosts(List<Ghost> ghosts, List<Ghost> phantomGhosts, List<Buster> allies, List<Buster> enemies, List<Ghost> expected) {
        assertEquals(
                phantomUpdater.updatePhantomGhosts(ghosts, phantomGhosts, allies, enemies),
                expected
        );
    }

    private void checkEnemies(List<Buster> allies, List<Buster> phantomEnemies, List<Buster> enemies, List<Buster> expected) {
        checkEnemies(allies, phantomEnemies, enemies, expected, new Point(0, 0));
    }

    private void checkEnemies(List<Buster> allies, List<Buster> phantomEnemies, List<Buster> enemies, List<Buster> expected, Point enemyBase) {
        assertEquals(
                phantomUpdater.updatePhantomEnemies(allies, phantomEnemies, enemies, enemyBase),
                expected
        );
    }

    private BusterBuilder buster(int x, int y, int id) {
        return new BusterBuilder(x, y, id);
    }
}