package chess.backend;

import java.awt.Color;

/**
 * The chess board contains pieces which may be moved, and have their own abilities.
 */
public abstract class Piece {
    private static final int PIECE_SIZE = 6;
    private final Color[][] image = new Color[PIECE_SIZE][PIECE_SIZE];
    private final Board board;
    private final boolean isWhite;
    private boolean hasMoved;

    Piece(Board board, boolean isWhite, int[][] pixels) {
        this.board = board;
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

    final Color[][] getPieceImage() {
        return image;
    }

    final Board board() {
        return board;
    }

    final boolean isWhite() {
        return isWhite;
    }

    final boolean hasMoved() {
        return hasMoved;
    }

    final void setMove() {
        hasMoved = true;
    }

    final boolean canMoveToLocation(Point point) {
        return board.getBoard(point) == null || board.getBoard(point).isWhite() != isWhite();
    }

    final boolean wouldNotPutAlliedKingIntoCheck(Point start, Point end) {
        var backup = board.getBoard(end);
        board.setBoard(end, this);
        board.setBoard(start, null);
        boolean isAllowed = !board.getAlliedKing().isKingInCheck(board.locateAlliedKing());
        board.setBoard(start, this);
        board.setBoard(end, backup);
        return isAllowed;
    }
}
