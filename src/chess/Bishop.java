package chess;

import java.awt.Point;

/**
 * The bishop is a chess piece which may travel as many squares as it wants diagonally, with the
 * only rule being that it does not travel through another piece. If the destination tile has an
 * enemy piece, it will capture it. If the destination tile has a friendly piece, it may not move
 * to it.
 */
final class Bishop extends Piece {
    private final boolean isWhite;

    Bishop(boolean isWhite) {
        this.isWhite = isWhite;
        final int[][] pixels = {
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
     * @return true if the change in x is the same as the change in y
     */
    boolean isEachCoordinateDeltaSame(Point start, Point end) {
        return Math.abs(end.y - start.y) == Math.abs(end.x - start.x);
    }

    /**
     * Bishop can capture enemy and can travel as long as nothing is in its way.
     *
     * @param start the original position
     * @param end   the final position
     * @return true if bishop action is legal
     */
    boolean isBishopActionLegal(Point start, Point end) {
        final int min = Math.min(start.x, end.x);
        final int max = Math.max(start.x, end.x);
        final Point mutatingPoint = new Point(start);
        final int xScale = Integer.signum(end.x - start.x);
        final int yScale = Integer.signum(end.y - start.y);
        for (int i = min + 1; i < max; i++) {
            mutatingPoint.x += xScale;
            mutatingPoint.y += yScale;
            if (Chess.getBoard(mutatingPoint) != null) {
                return false;
            }
        }
        return canMoveOnto(end);
    }
}
