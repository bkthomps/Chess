package chess;

import java.awt.Point;

/**
 * The bishop is the chess piece which can travel as many squares as it wants diagonally, as long as it does not travel
 * through anything.
 */
final class Bishop extends Piece {

    private final boolean isWhite;

    Bishop(boolean isWhite) {
        this.isWhite = isWhite;
        final int[][] pixels =
                {
                        {0, 0, 1, 1, 0, 0},
                        {0, 0, 1, 1, 0, 0},
                        {0, 1, 1, 1, 1, 0},
                        {0, 1, 1, 1, 1, 0},
                        {0, 0, 1, 1, 0, 0},
                        {1, 1, 1, 1, 1, 1}
                };
        getColor(pixels);
    }

    @Override
    boolean isWhite() {
        return isWhite;
    }

    /**
     * Determine if the action is legal.
     *
     * @param start the original position
     * @param end   the final position
     * @return if the action is legal
     */
    @Override
    boolean isActionLegal(Point start, Point end) {
        return isEachCoordinateDeltaSame(start, end) && isBishopActionLegal(start, end)
                && wouldNotPutKingIntoCheck(start, end);
    }

    /**
     * Checks to see if the change in x is the same as the change in y.
     *
     * @param start the original position
     * @param end   the final position
     * @return if the change in x is the same as the change in y
     */
    boolean isEachCoordinateDeltaSame(Point start, Point end) {
        return Math.abs(end.y - start.y) == Math.abs(end.x - start.x);
    }

    /**
     * Bishop can capture enemy and can travel as long as nothing is in its way.
     *
     * @param start the original position
     * @param end   the final position
     * @return if bishop action is legal
     */
    boolean isBishopActionLegal(Point start, Point end) {
        final int min = Math.min(start.x, end.x);
        final int max = Math.max(start.x, end.x);
        int x = start.x;
        int y = start.y;
        for (int i = min + 1; i < max; i++) {
            x += Math.signum(end.x - start.x);
            y += Math.signum(end.y - start.y);
            if (Chess.board[y][x] != null) {
                return false;
            }
        }
        return canMoveOnto(end);
    }
}
