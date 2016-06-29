package game;

import java.util.List;

import static java.lang.Math.*;

public class Utils {

    public static double dist(Buster buster, Point p) {
        return dist(buster.x, buster.y, p.x, p.y);
    }

    public static double dist(Buster buster, Ghost p) {
        return dist(buster.x, buster.y, p.x, p.y);
    }

    public static double dist(int x1, int y1, int x2, int y2) {
        return sqrt(sqr(x1 - x2) + sqr(y1 - y2));
    }

    public static double dist(Buster a, Buster b) {
        return dist(a.x, a.y, b.x, b.y);
    }

    public static double dist(Point a, Point b) {
        return dist(a.x, a.y, b.x, b.y);
    }

    public static double dist(Point p, Ghost g) {
        return dist(p.x, p.y, g.x, g.y);
    }

    static double dist(Point p, Buster b) {
        return dist(p.x, p.y, b.x, b.y);
    }

    public static long sqr(int x) {
        return x * x;
    }

    public static Point moveToWithAllowedRange(int fromX, int fromY, int toX, int toY, int moveDist, int minRange) {
        double dist = dist(fromX, fromY, toX, toY);
        if (dist < minRange) {
            return new Point(fromX, fromY);
        }
        double needDist = dist - minRange;
        double w = needDist / dist;
        double dx = (toX - fromX) * w;
        double dy = (toY - fromY) * w;
        int rx = roundTo(fromX + dx, toX);
        //noinspection SuspiciousNameCombination
        int ry = roundTo(fromY + dy, toY);
        return new Point(rx, ry);
    }

    private static int roundTo(double x, int toX) {
        if (toX > x) {
            return (int) Math.ceil(x);
        } else {
            return (int) Math.floor(x);
        }
    }

    static Point runawayPoint(int scaryX, int scaryY, int x, int y, int moveRange) {
        if (x == scaryX && y == scaryY) {
            return new Point(x, y);
        }
        double alpha = atan2(y - scaryY, x - scaryX);
        double newX = x + moveRange * cos(alpha);
        double newY = y + moveRange * sin(alpha);
        return Point.round(newX, newY);
    }

    static Point getNewPosition(Buster buster, Move move, GameParameters gameParameters) {
        int fromX = buster.x;
        int fromY = buster.y;
        if (move.type != MoveType.MOVE) {
            return new Point(fromX, fromY);
        }
        return getNewPosition(fromX, fromY, move.x, move.y, gameParameters);
    }

    public static Point getNewPosition(int fromX, int fromY, int toX, int toY, GameParameters gameParameters) {
        double dist = dist(fromX, fromY, toX, toY);
        if (dist <= gameParameters.MOVE_RANGE) {
            return new Point(toX, toY);
        }
        double w = gameParameters.MOVE_RANGE / dist;
        double dx = (toX - fromX) * w;
        double dy = (toY - fromY) * w;
        return Point.round(fromX + dx, fromY + dy);
    }

    static Point getEnemyBase(Point myBase, GameParameters gameParameters) {
        return new Point(gameParameters.H - myBase.x, gameParameters.W - myBase.y);
    }

    static Buster getWithId(List<Buster> busters, int id) {
        for (Buster enemy : busters) {
            if (enemy.getId() == id) {
                return enemy;
            }
        }
        return null;
    }
}
