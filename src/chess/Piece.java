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

    final boolean canMoveOnto(int x, int y, Piece[][] board, boolean isWhite) {
        return board[y][x] == null || board[y][x].isWhite() != isWhite;
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
}
