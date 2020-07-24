package chess;

/**
 * The rook may move any amount of squares horizontally or vertically, but must not pass through
 * other pieces. The rook captures the piece it moves to if the destination tile has an enemy piece,
 * but may not move if the destination tile contains a friendly piece.
 */
final class Rook extends Piece {
    private static final int[][] pixels = {
            {1, 0, 1, 1, 0, 1},
            {1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 0},
            {1, 1, 1, 1, 1, 1}
    };

    Rook(boolean isWhite) {
        super(isWhite, pixels);
    }

    @Override
    boolean isActionLegal(Point start, Point end) {
        return isRookActionLegal(start, end) && wouldNotPutAlliedKingIntoCheck(start, end);
    }

    boolean isRookActionLegal(Point start, Point end) {
        if (start.equals(end) || (start.x() != end.x() && start.y() != end.y())) {
            return false;
        }
        if (start.x() == end.x()) {
            int min = Math.min(start.y(), end.y());
            int max = Math.max(start.y(), end.y());
            for (int i = min + 1; i < max; i++) {
                var point = Point.instance(start.x(), i);
                if (Board.getBoard(point) != null) {
                    return false;
                }
            }
        } else {
            int min = Math.min(start.x(), end.x());
            int max = Math.max(start.x(), end.x());
            for (int i = min + 1; i < max; i++) {
                var point = Point.instance(i, start.y());
                if (Board.getBoard(point) != null) {
                    return false;
                }
            }
        }
        return canMoveToLocation(end);
    }
}
