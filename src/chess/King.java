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
                && canMoveOnto(end.x, end.y) && isNoAdjacentKing(end) && !isCheck(end.x, end.y);
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
        if (x < 0 || x >= Chess.board.length || y < 0 || y >= Chess.board.length) {
            return true;
        }
        final Piece item = Chess.board[y][x];
        final boolean isEnemyKing = item != null && item.getClass() == King.class && item.isWhite() != isWhite;
        return !isEnemyKing;
    }

    /**
     * Determines if the king is in check.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return if the king is in check
     */
    boolean isCheck(int x, int y) {
        return isCheckDiagonal(x, y, -1, -1) || isCheckDiagonal(x, y, -1, 1)
                || isCheckDiagonal(x, y, 1, -1) || isCheckDiagonal(x, y, 1, 1)
                || isCheckStraight(x, y, 0, -1) || isCheckStraight(x, y, 0, 1)
                || isCheckStraight(x, y, -1, 0) || isCheckStraight(x, y, 1, 0)
                || isCheckPawn(x, y) || isCheckKnight(x, y);
    }

    /**
     * Determines if the king is in check due to a diagonal opponent, such as a bishop or a queen.
     *
     * @param xCheck the x-coordinate to check
     * @param yCheck the y-coordinate to check
     * @param xScale the side to check on for the x-coordinate
     * @param yScale the side to check on for the y-coordinate
     * @return if the king is in check due to a diagonal opponent
     */
    private boolean isCheckDiagonal(int xCheck, int yCheck, int xScale, int yScale) {
        int x = xCheck + xScale, y = yCheck + yScale;
        while (isInGridBounds(x, y)) {
            if (Chess.board[y][x] != null && (Chess.board[y][x].getClass() == Bishop.class
                    || Chess.board[y][x].getClass() == Queen.class) && Chess.board[y][x].isWhite() != isWhite) {
                return true;
            }
            if (Chess.board[y][x] != null) {
                return false;
            }
            x += xScale;
            y += yScale;
        }
        return false;
    }

    /**
     * Determines if the king is in check due to a straight opponent, such as a rook or a queen.
     *
     * @param xCheck the x-coordinate to check
     * @param yCheck the y-coordinate to check
     * @param xScale the side to check on for the x-coordinate
     * @param yScale the side to check on for the y-coordinate
     * @return if the king is in check due to a straight opponent
     */
    private boolean isCheckStraight(int xCheck, int yCheck, int xScale, int yScale) {
        int x = xCheck + xScale, y = yCheck + yScale;
        while (isInGridBounds(x, y)) {
            if (Chess.board[y][x] != null && (Chess.board[y][x].getClass() == Rook.class
                    || Chess.board[y][x].getClass() == Queen.class) && Chess.board[y][x].isWhite() != isWhite) {
                return true;
            }
            if (Chess.board[y][x] != null) {
                return false;
            }
            x += xScale;
            y += yScale;
        }
        return false;
    }

    /**
     * Determines if the coordinates are in bounds.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return if the coordinates are in bounds
     */
    private boolean isInGridBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < Chess.board.length && y < Chess.board.length;
    }

    /**
     * Determines if the king is in check due to a pawn.
     *
     * @param xCheck the x-coordinate
     * @param yCheck the y-coordinate
     * @return if the king is in check due to a pawn
     */
    private boolean isCheckPawn(int xCheck, int yCheck) {
        return isEnemyPawn(xCheck - 1, yCheck - 1) || isEnemyPawn(xCheck + 1, yCheck - 1);
    }

    /**
     * Determines if the king is in check due to one side a pawn can capture from.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return if the king is in check due to one side a pawn can capture from
     */
    private boolean isEnemyPawn(int x, int y) {
        return x > 0 && y > 0 && x < Chess.board.length - 1 && y < Chess.board.length - 1 && Chess.board[y][x] != null
                && Chess.board[y][x].getClass() == Pawn.class && Chess.board[y][x].isWhite() != isWhite;
    }

    /**
     * Determines if the king is in check due to a knight.
     *
     * @param xCheck the x-coordinate
     * @param yCheck the y-coordinate
     * @return if the king is in check due to a knight
     */
    private boolean isCheckKnight(int xCheck, int yCheck) {
        return (isEnemyKnight(xCheck - 2, yCheck - 1)) || (isEnemyKnight(xCheck - 1, yCheck - 2))
                || (isEnemyKnight(xCheck - 2, yCheck + 1)) || (isEnemyKnight(xCheck - 1, yCheck + 2))
                || (isEnemyKnight(xCheck + 2, yCheck - 1)) || (isEnemyKnight(xCheck + 1, yCheck - 2))
                || (isEnemyKnight(xCheck + 2, yCheck + 1)) || (isEnemyKnight(xCheck + 1, yCheck + 2));
    }

    /**
     * Determine if the king is in check due to one spot a knight can capture from.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return if the king is in check due to one spot a knight can capture from
     */
    private boolean isEnemyKnight(int x, int y) {
        return x > 0 && y > 0 && x < Chess.board.length - 1 && y < Chess.board.length - 1 && Chess.board[y][x] != null
                && Chess.board[y][x].getClass() == Knight.class && Chess.board[y][x].isWhite() != isWhite;
    }
}
