package chess;

import java.awt.Point;

/**
 * The pawn can only move one square forward, but can move two on its first move. The pawn captures diagonally.
 */
final class Pawn extends Piece {

    private final boolean isWhite;

    Pawn(boolean isWhite) {
        this.isWhite = isWhite;
        final int[][] pixels =
                {
                        {0, 0, 1, 1, 0, 0},
                        {0, 1, 1, 1, 1, 0},
                        {0, 1, 1, 1, 1, 0},
                        {0, 0, 1, 1, 0, 0},
                        {0, 1, 1, 1, 1, 0},
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
        final boolean isForward = Chess.getBoard(end) == null && end.x == start.x;
        final boolean isOneBlock = end.y == start.y - 1;
        final Point oneBelowStart = new Point(start.x, start.y - 1);
        final boolean isJump = end.y == start.y - 2 && Chess.getBoard(oneBelowStart) == null && !hasMoved();
        final boolean isForwardAllowed = isForward && (isOneBlock || isJump);
        final boolean isCapture = Chess.getBoard(end) != null
                && Chess.getBoard(end).isWhite() != isWhite
                && end.y == start.y - 1 && Math.abs(end.x - start.x) == 1;
        return (isForwardAllowed || isCapture) && wouldNotPutKingIntoCheck(start, end);
    }
}
