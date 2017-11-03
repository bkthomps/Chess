package chess;

import java.awt.Color;
import java.awt.Point;

/**
 * The chess board contains pieces which may be moved, and have their own abilities.
 */
abstract class Piece {

    static final int PIECE_SIZE = 6;

    abstract Color[][] getImage();

    abstract boolean isWhite();

    abstract boolean isActionLegal(Point start, Point end);

    abstract boolean hasMoved();

    abstract void setMove();

    final int delta(double start, double end) {
        return Math.abs((int) end - (int) start);
    }

    final boolean canMoveOnto(int x, int y, boolean isWhite) {
        return Chess.board[y][x] == null || Chess.board[y][x].isWhite() != isWhite;
    }

    /**
     * Set the color of each piece displayed on the board based on which squares contain pieces and if they are white or
     * black.
     *
     * @param image   the color representation of the board
     * @param pixels  the pixels on the board
     * @param isWhite if the piece is white
     * @throws IllegalStateException if invalid pixel value on the pixel board
     */
    final void getColor(Color[][] image, int[][] pixels, boolean isWhite) {
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels.length; j++) {
                final int pixel = pixels[i][j];
                if (pixel == 0) {
                    image[i][j] = null;
                } else if (pixel == 1 && isWhite) {
                    image[i][j] = Color.WHITE;
                } else if (pixel == 1) {
                    image[i][j] = Color.BLACK;
                } else {
                    throw new IllegalStateException("Set up pixel board wrong!");
                }
            }
        }
    }

    /**
     * Determines if moving the piece would put the allied king in check.
     *
     * @param start the start position
     * @param end   the end position
     * @return if moving the piece would put the king in check
     */
    final boolean wouldNotPutKingIntoCheck(Point start, Point end) {
        final int x1 = (int) start.getX(), x2 = (int) end.getX();
        final int y1 = (int) start.getY(), y2 = (int) end.getY();
        final Piece backup = Chess.board[y2][x2];
        Chess.board[y2][x2] = this;
        Chess.board[y1][x1] = null;
        final Point kingPoint = GameState.locateKing(this.isWhite());
        final int kingX = (int) kingPoint.getX();
        final int kingY = (int) kingPoint.getY();
        final King king = (King) Chess.board[kingY][kingX];
        final boolean isAllowed = !king.isCheck(kingX, kingY);
        Chess.board[y1][x1] = this;
        Chess.board[y2][x2] = backup;
        return isAllowed;
    }
}
