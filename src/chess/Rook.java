package chess;

import java.awt.Point;

/**
 * The rook, sometimes called the castle, may move any amount of squares horizontally or vertically, but must not pass
 * through other pieces.
 */
final class Rook extends Piece {

    private final boolean isWhite;

    Rook(boolean isWhite) {
        this.isWhite = isWhite;
        final int[][] pixels =
                {
                        {1, 0, 1, 1, 0, 1},
                        {1, 1, 1, 1, 1, 1},
                        {0, 1, 1, 1, 1, 0},
                        {0, 1, 1, 1, 1, 0},
                        {0, 1, 1, 1, 1, 0},
                        {1, 1, 1, 1, 1, 1}
                };
        getColor(pixels);
    }

    @Override
    boolean isWhite() {
        return isWhite;
    }

    /**
     * Determines if the action is legal. It is legal if it moves without passing through other pieces, and eats just
     * as it would move normally.
     *
     * @param start the beginning position
     * @param end   the final position
     * @return if the action is legal
     */
    @Override
    boolean isActionLegal(Point start, Point end) {
        return isRookActionLegal(start, end) && wouldNotPutKingIntoCheck(start, end);
    }

    /**
     * Rook can capture enemy and can travel as long as nothing is in its way.
     *
     * @param start the original position
     * @param end   the final position
     * @return if rook action is legal
     */
    boolean isRookActionLegal(Point start, Point end) {
        if ((start.x == end.x && start.y == end.y) || (start.x != end.x && start.y != end.y)) {
            return false;
        }
        if (start.x == end.x) {
            final int min = Math.min(start.y, end.y);
            final int max = Math.max(start.y, end.y);
            for (int i = min + 1; i < max; i++) {
                final Point point = new Point(start.x, i);
                if (Chess.getBoard(point) != null) {
                    return false;
                }
            }
        } else {
            final int min = Math.min(start.x, end.x);
            final int max = Math.max(start.x, end.x);
            for (int i = min + 1; i < max; i++) {
                final Point point = new Point(i, start.y);
                if (Chess.getBoard(point) != null) {
                    return false;
                }
            }
        }
        return canMoveOnto(end);
    }
}
