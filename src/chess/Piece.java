package chess;

import java.awt.Color;
import java.awt.Point;

/**
 * The chess board contains pieces which may be moved, and have their own abilities.
 */
abstract class Piece {

    private static final int PIECE_SIZE = 6;
    private final Color[][] image = new Color[PIECE_SIZE][PIECE_SIZE];
    private boolean hasMoved;

    /**
     * Determines whether the piece is white or not.
     *
     * @return true if the piece is white
     */
    abstract boolean isWhite();

    /**
     * Determines if the action is legal.
     *
     * @param start the original position
     * @param end   the final position
     * @return true if the action is legal
     */
    abstract boolean isActionLegal(Point start, Point end);

    /**
     * Determines if the piece has moved.
     *
     * @return true if the piece has moved
     */
    final boolean hasMoved() {
        return hasMoved;
    }

    /**
     * Sets the piece to be moved.
     */
    final void setMove() {
        hasMoved = true;
    }

    /**
     * Gets the color representation of the piece.
     *
     * @return the color representation of the piece
     */
    final Color[][] getImage() {
        return image;
    }

    /**
     * Determines if a piece can move onto a grid location.
     *
     * @param point location to check
     * @return true if the piece can move onto the location
     */
    final boolean canMoveOnto(Point point) {
        return Chess.getBoard(point) == null || Chess.getBoard(point).isWhite() != isWhite();
    }

    /**
     * Sets the color of each piece displayed on the board based on which squares contain pieces and if they are white
     * or black.
     *
     * @param pixels the pixels on the board
     * @throws IllegalStateException if invalid pixel value on the pixel board
     */
    final void getColor(int[][] pixels) {
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels.length; j++) {
                final int pixel = pixels[i][j];
                if (pixel == 0) {
                    image[i][j] = null;
                } else if (pixel == 1 && isWhite()) {
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
     * @return true if moving the piece would put the king in check
     */
    final boolean wouldNotPutKingIntoCheck(Point start, Point end) {
        final Piece backup = Chess.getBoard(end);
        Chess.setBoard(end, this);
        Chess.setBoard(start, null);
        final Point kingPoint = GameState.locateKing(isWhite());
        final King king = (King) Chess.getBoard(kingPoint);
        final boolean isAllowed = !king.isCheck(kingPoint);
        Chess.setBoard(start, this);
        Chess.setBoard(end, backup);
        return isAllowed;
    }
}
