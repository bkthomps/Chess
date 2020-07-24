package chess;

import java.awt.Color;

/**
 * The chess board contains pieces which may be moved, and have their own abilities.
 */
abstract class Piece {
    private static final int PIECE_SIZE = 6;
    private final Color[][] image = new Color[PIECE_SIZE][PIECE_SIZE];
    private final boolean isWhite;
    private boolean hasMoved;

    Piece(boolean isWhite, int[][] pixels) {
        this.isWhite = isWhite;
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels.length; j++) {
                if (pixels[i][j] != 0) {
                    image[i][j] = isWhite() ? Color.WHITE : Color.BLACK;
                }
            }
        }
    }

    abstract boolean isActionLegal(Point start, Point end);

    final boolean isWhite() {
        return isWhite;
    }

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
        return Game.getBoard(point) == null || Game.getBoard(point).isWhite() != isWhite();
    }

    final boolean wouldNotPutAlliedKingIntoCheck(Point start, Point end) {
        var backup = Game.getBoard(end);
        Game.setBoard(end, this);
        Game.setBoard(start, null);
        boolean isAllowed = !Game.getAlliedKing(isWhite()).isKingInCheck(Game.locateAlliedKing(isWhite()));
        Game.setBoard(start, this);
        Game.setBoard(end, backup);
        return isAllowed;
    }
}
