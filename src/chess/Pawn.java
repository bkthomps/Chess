package chess;

import java.awt.Point;

/**
 * The pawn can only move one square forward, but can move two on its first move. The pawn may not
 * move forward if there is a piece in the way. The pawn captures one tile diagonally.
 */
final class Pawn extends Piece {
    private final boolean isWhite;

    Pawn(boolean isWhite) {
        this.isWhite = isWhite;
        int[][] pixels = {
                {0, 0, 1, 1, 0, 0},
                {0, 1, 1, 1, 1, 0},
                {0, 1, 1, 1, 1, 0},
                {0, 0, 1, 1, 0, 0},
                {0, 1, 1, 1, 1, 0},
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
        boolean isForward = Chess.getBoard(end) == null && end.x == start.x;
        boolean isOneBlock = end.y == start.y - 1;
        var oneBelowStart = new Point(start.x, start.y - 1);
        boolean isJump = end.y == start.y - 2 && Chess.getBoard(oneBelowStart) == null && !hasMoved();
        boolean isForwardAllowed = isForward && (isOneBlock || isJump);
        boolean isCapture = Chess.getBoard(end) != null
                && Chess.getBoard(end).isWhite() != isWhite
                && end.y == start.y - 1 && Math.abs(end.x - start.x) == 1;
        return (isForwardAllowed || isCapture) && wouldNotPutAlliedKingIntoCheck(start, end);
    }
}
