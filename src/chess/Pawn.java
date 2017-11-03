package chess;

import java.awt.Color;
import java.awt.Point;

/**
 * The pawn can only move one square forward, but can move two on its first move. It eats on a diagonal.
 */
final class Pawn extends Piece {

    private final Color[][] image;
    private final boolean isWhite;
    private boolean hasMoved;

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
        image = new Color[PIECE_SIZE][PIECE_SIZE];
        getColor(image, pixels, isWhite);
    }

    @Override
    public Color[][] getImage() {
        return image;
    }

    @Override
    public boolean isWhite() {
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
        final int x1 = (int) start.getX(), x2 = (int) end.getX();
        final int y1 = (int) start.getY(), y2 = (int) end.getY();
        final boolean isForward = Chess.board[y2][x2] == null && x2 == x1;
        final boolean isOneBlock = y2 == y1 - 1;
        final boolean isJump = y2 == y1 - 2 && Chess.board[y1 - 1][x1] == null && !hasMoved;
        final boolean isForwardAllowed = isForward && (isOneBlock || isJump);
        final boolean isCapture = Chess.board[y2][x2] != null
                && Chess.board[y2][x2].isWhite() != isWhite
                && y2 == y1 - 1 && delta(x2, x1) == 1;
        return (isForwardAllowed || isCapture) && wouldNotPutKingIntoCheck(start, end);
    }

    @Override
    boolean hasMoved() {
        return hasMoved;
    }

    @Override
    void setMove() {
        hasMoved = true;
    }
}
