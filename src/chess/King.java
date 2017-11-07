package chess;

import java.awt.Point;

/**
 * The king may move only one square which surrounds it. Meaning it can move into any of its eight neighbors. If the
 * king is threatened, it must be moved. This is called a check. If the king cannot move without going into check and is
 * currently in check, it is a checkmate, and the opponent wins. If the king cannot move without going into check and is
 * not currently in check, it is a stalemate, and is a draw. The king cannot move into check.
 */
final class King extends Piece {

    private final boolean isWhite;

    King(boolean isWhite) {
        this.isWhite = isWhite;
        final int[][] pixels =
                {
                        {0, 0, 1, 1, 0, 0},
                        {1, 0, 1, 1, 0, 1},
                        {1, 1, 1, 1, 1, 1},
                        {1, 1, 1, 1, 1, 1},
                        {0, 1, 1, 1, 1, 0},
                        {0, 1, 1, 1, 1, 0}
                };
        getColor(pixels);
    }

    @Override
    boolean isWhite() {
        return isWhite;
    }

    /**
     * Determines if the action is legal. It is legal if it moves only one square, and it does not move into check.
     *
     * @param start the beginning position
     * @param end   the final position
     * @return if the action is legal
     */
    @Override
    boolean isActionLegal(Point start, Point end) {
        return Math.abs(end.x - start.x) + Math.abs(end.y - start.y) != 0
                && Math.abs(end.x - start.x) <= 1 && Math.abs(end.y - start.y) <= 1
                && canMoveOnto(end) && isNoAdjacentKing(end) && !isCheck(end);
    }

    /**
     * Determines if there is no adjacent King.
     *
     * @param me the position to check for adjacent king
     * @return if there is no adjacent king
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
     * @return if there is no King at the specified location
     */
    private boolean isKingNotAt(int x, int y) {
        if (x < 0 || x >= Chess.BOARD_SIZE || y < 0 || y >= Chess.BOARD_SIZE) {
            return true;
        }
        final Point point = new Point(x, y);
        final Piece item = Chess.getBoard(point);
        final boolean isEnemyKing = item != null && item.getClass() == King.class && item.isWhite() != isWhite;
        return !isEnemyKing;
    }

    /**
     * Determines if the king is in check.
     *
     * @param point the location to check
     * @return if the king is in check
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
     * @return if the king is in check due to a diagonal opponent
     */
    private boolean isCheckDiagonal(Point point, int xScale, int yScale) {
        final Point mutatingPoint = new Point(point);
        mutatingPoint.x += xScale;
        mutatingPoint.y += yScale;
        while (isInGridBounds(mutatingPoint)) {
            final Piece me = Chess.getBoard(mutatingPoint);
            if (me != null && (me.getClass() == Bishop.class || me.getClass() == Queen.class)
                    && me.isWhite() != isWhite) {
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
     * @return if the king is in check due to a straight opponent
     */
    private boolean isCheckStraight(Point point, int xScale, int yScale) {
        final Point mutatingPoint = new Point(point);
        mutatingPoint.x += xScale;
        mutatingPoint.y += yScale;
        while (isInGridBounds(mutatingPoint)) {
            final Piece me = Chess.getBoard(mutatingPoint);
            if (me != null && (me.getClass() == Rook.class || me.getClass() == Queen.class)
                    && me.isWhite() != isWhite) {
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
     * Determines if the coordinates are in bounds.
     *
     * @param point the location to check
     * @return if the coordinates are in bounds
     */
    private boolean isInGridBounds(Point point) {
        return point.x >= 0 && point.y >= 0 && point.x < Chess.BOARD_SIZE && point.y < Chess.BOARD_SIZE;
    }

    /**
     * Determines if the king is in check due to a pawn.
     *
     * @param point the location to check
     * @return if the king is in check due to a pawn
     */
    private boolean isCheckPawn(Point point) {
        return isEnemyPawn(point.x - 1, point.y - 1) || isEnemyPawn(point.x + 1, point.y - 1);
    }

    /**
     * Determines if the king is in check due to one side a pawn can capture from.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return if the king is in check due to one side a pawn can capture from
     */
    private boolean isEnemyPawn(int x, int y) {
        if (isNotInInnerGridBounds(x, y)) {
            return false;
        }
        final Point point = new Point(x, y);
        final Piece me = Chess.getBoard(point);
        return me != null && me.getClass() == Pawn.class && me.isWhite() != isWhite;
    }

    /**
     * Determines if the king is in check due to a knight.
     *
     * @param point the location to check
     * @return if the king is in check due to a knight
     */
    private boolean isCheckKnight(Point point) {
        return (isEnemyKnight(point.x - 2, point.y - 1)) || (isEnemyKnight(point.x - 1, point.y - 2))
                || (isEnemyKnight(point.x - 2, point.y + 1)) || (isEnemyKnight(point.x - 1, point.y + 2))
                || (isEnemyKnight(point.x + 2, point.y - 1)) || (isEnemyKnight(point.x + 1, point.y - 2))
                || (isEnemyKnight(point.x + 2, point.y + 1)) || (isEnemyKnight(point.x + 1, point.y + 2));
    }

    /**
     * Determine if the king is in check due to one spot a knight can capture from.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return if the king is in check due to one spot a knight can capture from
     */
    private boolean isEnemyKnight(int x, int y) {
        if (isNotInInnerGridBounds(x, y)) {
            return false;
        }
        final Point point = new Point(x, y);
        final Piece me = Chess.getBoard(point);
        return me != null && me.getClass() == Knight.class && me.isWhite() != isWhite;
    }

    /**
     * Determines if the location is outside the inner grid. The inner grid being one smaller on each side than the
     * total grid.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return if the location is outside the inner grid
     */
    private boolean isNotInInnerGridBounds(int x, int y) {
        return x <= 0 || y <= 0 || x >= Chess.BOARD_SIZE - 1 || y >= Chess.BOARD_SIZE - 1;
    }
}
