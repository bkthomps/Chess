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
        setPieceImage(pixels);
    }

    @Override
    boolean isWhite() {
        return isWhite;
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
        return canMoveToLocation(end);
    }
}
