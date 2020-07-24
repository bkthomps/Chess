package chess.backend;

import java.util.Objects;

/**
 * An immutable point class.
 */
public final class Point {
    private static final Point[][] pool = new Point[Board.BOARD_LENGTH + 2][Board.BOARD_WIDTH + 2];
    private final int x;
    private final int y;

    private Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Point instance(int x, int y) {
        if (pool[y + 1][x + 1] == null) {
            pool[y + 1][x + 1] = new Point(x, y);
        }
        return pool[y + 1][x + 1];
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Point)) {
            return false;
        }
        var point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
