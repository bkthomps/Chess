package chess;

import java.awt.Point;

/**
 * The bishop is a chess piece which may travel as many squares as it wants diagonally, with the
 * only rule being that it does not travel through another piece. If the destination tile has an
 * enemy piece, it will capture it. If the destination tile has a friendly piece, it may not move
 * to it.
 */
final class Bishop extends Piece {
    private static final int[][] pixels = {
            {0, 0, 1, 1, 0, 0},
            {0, 0, 1, 1, 0, 0},
            {0, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 0},
            {0, 0, 1, 1, 0, 0},
            {1, 1, 1, 1, 1, 1}
    };

    Bishop(boolean isWhite) {
        super(isWhite, pixels);
    }

    @Override
    boolean isActionLegal(Point start, Point end) {
        return isDiagonalLine(start, end) && isBishopActionLegal(start, end)
                && wouldNotPutAlliedKingIntoCheck(start, end);
    }

    boolean isDiagonalLine(Point start, Point end) {
        return Math.abs(end.y - start.y) == Math.abs(end.x - start.x);
    }

    boolean isBishopActionLegal(Point start, Point end) {
        int min = Math.min(start.x, end.x);
        int max = Math.max(start.x, end.x);
        var mutatingPoint = new Point(start);
        int xScale = Integer.signum(end.x - start.x);
        int yScale = Integer.signum(end.y - start.y);
        for (int i = min + 1; i < max; i++) {
            mutatingPoint.x += xScale;
            mutatingPoint.y += yScale;
            if (Board.getBoard(mutatingPoint) != null) {
                return false;
            }
        }
        return canMoveToLocation(end);
    }
}
