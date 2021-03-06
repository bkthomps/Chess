package chess.backend;

/**
 * The pawn can only move one square forward, but can move two on its first move. The pawn
 * may not move forward if there is a piece in the way. The pawn captures one tile diagonally.
 */
final class Pawn extends Piece {
    private static final int[][] pixels = {
            {0, 0, 1, 1, 0, 0},
            {0, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 0},
            {0, 0, 1, 1, 0, 0},
            {0, 1, 1, 1, 1, 0},
            {1, 1, 1, 1, 1, 1}
    };

    Pawn(Board board, boolean isWhite) {
        super(board, isWhite, pixels);
    }

    @Override
    boolean isActionLegal(Point start, Point end) {
        boolean isForward = board().getBoard(end) == null && end.x() == start.x();
        boolean isOneBlock = end.y() == start.y() - 1;
        var oneBelowStart = Point.instance(start.x(), start.y() - 1);
        boolean isJump =
                end.y() == start.y() - 2 && board().getBoard(oneBelowStart) == null && !hasMoved();
        boolean isForwardAllowed = isForward && (isOneBlock || isJump);
        boolean isCapture = board().getBoard(end) != null
                && board().getBoard(end).isWhite() != isWhite()
                && end.y() == start.y() - 1 && Math.abs(end.x() - start.x()) == 1;
        return (isForwardAllowed || isCapture) && wouldNotPutAlliedKingIntoCheck(start, end);
    }
}
