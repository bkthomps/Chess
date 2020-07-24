package chess;

/**
 * The king may only move to any of the eight adjacent squares, unless there is a friendly piece. If
 * the tile which it moves to is an enemy piece, it captures it. If the king is in check, it must
 * move, or another piece must move in such a way that the check is no longer in effect. If the king
 * is in check without the possibility of another piece moving to prevent the check, and the king
 * can only move to other squares which are also in check, it is a checkmate and the opponent wins.
 * If a similar situation occurs, except that the king is not in check on the current square, but
 * would be in check in any adjacent squares, then this is a stalemate, and it is a draw, meaning
 * there are no winners.
 */
final class King extends Piece {
    private static final int[][] pixels = {
            {0, 0, 1, 1, 0, 0},
            {1, 0, 1, 1, 0, 1},
            {1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 0}
    };

    King(boolean isWhite) {
        super(isWhite, pixels);
    }

    @Override
    boolean isActionLegal(Point start, Point end) {
        return Math.abs(end.x() - start.x()) + Math.abs(end.y() - start.y()) != 0
                && Math.abs(end.x() - start.x()) <= 1 && Math.abs(end.y() - start.y()) <= 1
                && canMoveToLocation(end) && isNoAdjacentKing(end) && !isKingInCheck(end);
    }

    private boolean isNoAdjacentKing(Point point) {
        return isKingNotAt(point.x() - 1, point.y() - 1) && isKingNotAt(point.x(), point.y() - 1) && isKingNotAt(point.x() + 1, point.y() - 1)
                && isKingNotAt(point.x() - 1, point.y()) && isKingNotAt(point.x() + 1, point.y())
                && isKingNotAt(point.x() - 1, point.y() + 1) && isKingNotAt(point.x(), point.y() + 1) && isKingNotAt(point.x() + 1, point.y() + 1);
    }

    private boolean isKingNotAt(int x, int y) {
        if (x < 0 || x >= Board.BOARD_WIDTH || y < 0 || y >= Board.BOARD_LENGTH) {
            return true;
        }
        var point = Point.instance(x, y);
        var item = Board.getBoard(point);
        return !(item instanceof King && item.isWhite() != isWhite());
    }

    boolean isKingInCheck(Point point) {
        return isCheckFromDiagonalLine(point, -1, -1) || isCheckFromDiagonalLine(point, -1, 1)
                || isCheckFromDiagonalLine(point, 1, -1) || isCheckFromDiagonalLine(point, 1, 1)
                || isCheckFromStraightLine(point, 0, -1) || isCheckFromStraightLine(point, 0, 1)
                || isCheckFromStraightLine(point, -1, 0) || isCheckFromStraightLine(point, 1, 0)
                || isCheckDueToPawn(point) || isCheckDueToKnight(point);
    }

    private boolean isCheckFromDiagonalLine(Point point, int xScale, int yScale) {
        point = Point.instance(point.x() + xScale, point.y() + yScale);
        while (isInGridBounds(point)) {
            var piece = Board.getBoard(point);
            if ((piece instanceof Bishop || piece instanceof Queen) && piece.isWhite() != isWhite()) {
                return true;
            }
            if (piece != null) {
                return false;
            }
            point = Point.instance(point.x() + xScale, point.y() + yScale);
        }
        return false;
    }

    private boolean isCheckFromStraightLine(Point point, int xScale, int yScale) {
        point = Point.instance(point.x() + xScale, point.y() + yScale);
        while (isInGridBounds(point)) {
            var piece = Board.getBoard(point);
            if ((piece instanceof Rook || piece instanceof Queen) && piece.isWhite() != isWhite()) {
                return true;
            }
            if (piece != null) {
                return false;
            }
            point = Point.instance(point.x() + xScale, point.y() + yScale);
        }
        return false;
    }

    private boolean isCheckDueToPawn(Point point) {
        return isEnemyPawnAt(point.x() - 1, point.y() - 1) || isEnemyPawnAt(point.x() + 1, point.y() - 1);
    }

    private boolean isEnemyPawnAt(int x, int y) {
        if (!isInGridBounds(x, y)) {
            return false;
        }
        var point = Point.instance(x, y);
        var piece = Board.getBoard(point);
        return piece instanceof Pawn && piece.isWhite() != isWhite();
    }

    private boolean isCheckDueToKnight(Point point) {
        return (isEnemyKnightAt(point.x() - 2, point.y() - 1)) || (isEnemyKnightAt(point.x() - 1, point.y() - 2))
                || (isEnemyKnightAt(point.x() - 2, point.y() + 1)) || (isEnemyKnightAt(point.x() - 1, point.y() + 2))
                || (isEnemyKnightAt(point.x() + 2, point.y() - 1)) || (isEnemyKnightAt(point.x() + 1, point.y() - 2))
                || (isEnemyKnightAt(point.x() + 2, point.y() + 1)) || (isEnemyKnightAt(point.x() + 1, point.y() + 2));
    }

    private boolean isEnemyKnightAt(int x, int y) {
        if (!isInGridBounds(x, y)) {
            return false;
        }
        var point = Point.instance(x, y);
        var piece = Board.getBoard(point);
        return piece instanceof Knight && piece.isWhite() != isWhite();
    }

    private boolean isInGridBounds(Point p) {
        return isInGridBounds(p.x(), p.y());
    }

    private boolean isInGridBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < Board.BOARD_WIDTH && y < Board.BOARD_LENGTH;
    }
}
