package chess;

import java.awt.Point;

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
    private final boolean isWhite;

    King(boolean isWhite) {
        this.isWhite = isWhite;
        final int[][] pixels = {
                {0, 0, 1, 1, 0, 0},
                {1, 0, 1, 1, 0, 1},
                {1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1},
                {0, 1, 1, 1, 1, 0},
                {0, 1, 1, 1, 1, 0}
        };
        setPieceImage(pixels);
    }

    @Override
    boolean isWhite() {
        return isWhite;
    }

    @Override
    boolean isActionLegal(Point start, Point end) {
        return Math.abs(end.x - start.x) + Math.abs(end.y - start.y) != 0
                && Math.abs(end.x - start.x) <= 1 && Math.abs(end.y - start.y) <= 1
                && canMoveToLocation(end) && isNoAdjacentKing(end) && !isCheck(end);
    }

    /**
     * Determines if there is no adjacent King.
     *
     * @param me the position to check for adjacent king
     * @return true if there is no adjacent king
     */
    private boolean isNoAdjacentKing(Point me) {
        return isKingNotAt(me.x - 1, me.y - 1) && isKingNotAt(me.x, me.y - 1) && isKingNotAt(me.x + 1, me.y - 1)
                && isKingNotAt(me.x - 1, me.y) && isKingNotAt(me.x + 1, me.y)
                && isKingNotAt(me.x - 1, me.y + 1) && isKingNotAt(me.x, me.y + 1) && isKingNotAt(me.x + 1, me.y + 1);
    }

    /**
     * Determines if there is no King at the specified location.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return true if there is no King at the specified location
     */
    private boolean isKingNotAt(int x, int y) {
        if (x < 0 || x >= Chess.BOARD_SIZE || y < 0 || y >= Chess.BOARD_SIZE) {
            return true;
        }
        final Point point = new Point(x, y);
        final Piece item = Chess.getBoard(point);
        final boolean isEnemyKing = item instanceof King && item.isWhite() != isWhite;
        return !isEnemyKing;
    }

    /**
     * Determines if the king is in check.
     *
     * @param point the location to check
     * @return true if the king is in check
     */
    boolean isCheck(Point point) {
        return isCheckDiagonal(point, -1, -1) || isCheckDiagonal(point, -1, 1)
                || isCheckDiagonal(point, 1, -1) || isCheckDiagonal(point, 1, 1)
                || isCheckStraight(point, 0, -1) || isCheckStraight(point, 0, 1)
                || isCheckStraight(point, -1, 0) || isCheckStraight(point, 1, 0)
                || isCheckPawn(point) || isCheckKnight(point);
    }

    /**
     * Determines if the king is in check due to a diagonal opponent, such as a bishop or a queen.
     *
     * @param point  the location to check
     * @param xScale the side to check on for the x-coordinate
     * @param yScale the side to check on for the y-coordinate
     * @return true if the king is in check due to a diagonal opponent
     */
    private boolean isCheckDiagonal(Point point, int xScale, int yScale) {
        final Point mutatingPoint = new Point(point);
        mutatingPoint.x += xScale;
        mutatingPoint.y += yScale;
        while (isInGridBounds(mutatingPoint)) {
            final Piece me = Chess.getBoard(mutatingPoint);
            if ((me instanceof Bishop || me instanceof Queen) && me.isWhite() != isWhite) {
                return true;
            }
            if (me != null) {
                return false;
            }
            mutatingPoint.x += xScale;
            mutatingPoint.y += yScale;
        }
        return false;
    }

    /**
     * Determines if the king is in check due to a straight opponent, such as a rook or a queen.
     *
     * @param point  the location to check
     * @param xScale the side to check on for the x-coordinate
     * @param yScale the side to check on for the y-coordinate
     * @return true if the king is in check due to a straight opponent
     */
    private boolean isCheckStraight(Point point, int xScale, int yScale) {
        final Point mutatingPoint = new Point(point);
        mutatingPoint.x += xScale;
        mutatingPoint.y += yScale;
        while (isInGridBounds(mutatingPoint)) {
            final Piece me = Chess.getBoard(mutatingPoint);
            if ((me instanceof Rook || me instanceof Queen) && me.isWhite() != isWhite) {
                return true;
            }
            if (me != null) {
                return false;
            }
            mutatingPoint.x += xScale;
            mutatingPoint.y += yScale;
        }
        return false;
    }

    /**
     * Determines if the king is in check due to a pawn.
     *
     * @param point the location to check
     * @return true if the king is in check due to a pawn
     */
    private boolean isCheckPawn(Point point) {
        return isEnemyPawn(point.x - 1, point.y - 1) || isEnemyPawn(point.x + 1, point.y - 1);
    }

    /**
     * Determines if the king is in check due to one side a pawn can capture from.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return true if the king is in check due to one side a pawn can capture from
     */
    private boolean isEnemyPawn(int x, int y) {
        if (!isInGridBounds(x, y)) {
            return false;
        }
        final Point point = new Point(x, y);
        final Piece me = Chess.getBoard(point);
        return me instanceof Pawn && me.isWhite() != isWhite;
    }

    /**
     * Determines if the king is in check due to a knight.
     *
     * @param point the location to check
     * @return true if the king is in check due to a knight
     */
    private boolean isCheckKnight(Point point) {
        return (isEnemyKnight(point.x - 2, point.y - 1)) || (isEnemyKnight(point.x - 1, point.y - 2))
                || (isEnemyKnight(point.x - 2, point.y + 1)) || (isEnemyKnight(point.x - 1, point.y + 2))
                || (isEnemyKnight(point.x + 2, point.y - 1)) || (isEnemyKnight(point.x + 1, point.y - 2))
                || (isEnemyKnight(point.x + 2, point.y + 1)) || (isEnemyKnight(point.x + 1, point.y + 2));
    }

    /**
     * Determines if the king is in check due to one spot a knight can capture from.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return true if the king is in check due to one spot a knight can capture from
     */
    private boolean isEnemyKnight(int x, int y) {
        if (!isInGridBounds(x, y)) {
            return false;
        }
        final Point point = new Point(x, y);
        final Piece me = Chess.getBoard(point);
        return me instanceof Knight && me.isWhite() != isWhite;
    }

    /**
     * Determines if the point is in bounds.
     *
     * @param p the point
     * @return true if the point is in bounds
     */
    private boolean isInGridBounds(Point p) {
        return isInGridBounds(p.x, p.y);
    }

    /**
     * Determines if the coordinates are in bounds.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return true if the coordinates are in bounds
     */
    private boolean isInGridBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < Chess.BOARD_SIZE && y < Chess.BOARD_SIZE;
    }
}
