package chess;

import java.awt.Color;
import java.awt.Point;

/**
 * The pawn can only move one square forward, but can move two on its first move. It eats on a diagonal.
 */
class Pawn extends Piece {

    private Color[][] image = new Color[PIECE_SIZE][PIECE_SIZE];
    private final Piece[][] board;
    private final boolean isWhite;
    private boolean hasMoved;

    Pawn(boolean isWhite, Piece[][] board) {
        this.isWhite = isWhite;
        this.board = board;
        final int[][] pixels =
                {
                        {0, 0, 1, 1, 0, 0},
                        {0, 1, 1, 1, 1, 0},
                        {0, 1, 1, 1, 1, 0},
                        {0, 0, 1, 1, 0, 0},
                        {0, 1, 1, 1, 1, 0},
                        {1, 1, 1, 1, 1, 1}
                };
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
        return ((board[y2][x2] == null && x2 == x1)
                && ((y2 == y1 - 1) || (y2 == y1 - 2 && board[y1 - 1][x1] == null && !hasMoved)))
                || (board[y2][x2] != null && board[y2][x2].isWhite() != isWhite && y2 == y1 - 1 && delta(x2, x1) == 1);
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
