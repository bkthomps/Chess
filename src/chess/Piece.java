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

    abstract boolean isWhite();

    abstract boolean isActionLegal(Point start, Point end);

    final boolean hasMoved() {
        return hasMoved;
    }

    final void setMove() {
        hasMoved = true;
    }

    final Color[][] getPieceImage() {
        return image;
    }

    final boolean canMoveToLocation(Point point) {
        return Chess.getBoard(point) == null || Chess.getBoard(point).isWhite() != isWhite();
    }

    /**
     * @throws IllegalStateException if invalid pixel value on the pixel board
     */
    final void setPieceImage(int[][] pixels) {
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

    final boolean wouldNotPutAlliedKingIntoCheck(Point start, Point end) {
        final Piece backup = Chess.getBoard(end);
        Chess.setBoard(end, this);
        Chess.setBoard(start, null);
        final Point kingPoint = GameState.locateKing(isWhite());
        final King king = (King) Chess.getBoard(kingPoint);
        final boolean isAllowed = !king.isKingInCheck(kingPoint);
        Chess.setBoard(start, this);
        Chess.setBoard(end, backup);
        return isAllowed;
    }
}
