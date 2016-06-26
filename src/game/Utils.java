package game;

import static java.lang.Math.*;

public class Utils {
    static final int W = 16001;
    static final int H = 9001;
    static final int MAX_BUST_RANGE = 1760;
    static final int MIN_BUST_RANGE = 900;
    static final int RELEASE_RANGE = 1600;
    static final int FOG_RANGE = 2200;
    static final int STUN_RANGE = 1760;
    static final int STUN_COOLDOWN = 20;
    static final int STUN_DURATION = 10;
    static final int MOVE_DIST = 800;

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
        int ry = roundTo(fromY + dy, toY);
        return new Point(rx, ry);
    }

    private static int roundTo(double x, int toX) {
        if (toX > x) {
            return (int)Math.ceil(x);
        } else {
            return (int)Math.floor(x);
        }
    }
}
