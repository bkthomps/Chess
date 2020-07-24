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
        return Board.getBoard(point) == null || Board.getBoard(point).isWhite() != isWhite();
    }

    final boolean wouldNotPutAlliedKingIntoCheck(Point start, Point end) {
        var backup = Board.getBoard(end);
        Board.setBoard(end, this);
        Board.setBoard(start, null);
        boolean isAllowed = !Board.getAlliedKing(isWhite()).isKingInCheck(Board.locateAlliedKing(isWhite()));
        Board.setBoard(start, this);
        Board.setBoard(end, backup);
        return isAllowed;
    }
}
