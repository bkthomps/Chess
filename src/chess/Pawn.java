package chess;

import java.awt.Point;

/**
 * The pawn can only move one square forward, but can move two on its first move. It eats on a diagonal.
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

    /**
     * Determines if the action is legal. It is legal is it moves one square or two squares on its first turn to move.
     * To eat, it moves one square diagonally.
     *
     * @param start the beginning position
     * @param end   the final position
     * @return if the action is legal
     */
    @Override
    boolean isActionLegal(Point start, Point end) {
        final boolean isForward = Chess.board[end.y][end.x] == null && end.x == start.x;
        final boolean isOneBlock = end.y == start.y - 1;
        final boolean isJump = end.y == start.y - 2 && Chess.board[start.y - 1][start.x] == null && !hasMoved();
        final boolean isForwardAllowed = isForward && (isOneBlock || isJump);
        final boolean isCapture = Chess.board[end.y][end.x] != null
                && Chess.board[end.y][end.x].isWhite() != isWhite
                && end.y == start.y - 1 && Math.abs(end.x - start.x) == 1;
        return (isForwardAllowed || isCapture) && wouldNotPutKingIntoCheck(start, end);
    }
}
