package chess;

import java.awt.Color;
import java.awt.Point;

/**
 * The king may move only one square which surrounds it. Meaning it can move into any of its eight neighbors. If the
 * king is threatened, it must be moved. This is called a check. If the king cannot move without going into check and is
 * currently in check, it is a checkmate, and the opponent wins. If the king cannot move without going into check and is
 * not currently in check, it is a stalemate, and is a draw. The king cannot move into check.
 */
class King extends Piece {

    private Color[][] image = new Color[PIECE_SIZE][PIECE_SIZE];
    private final Piece[][] board;
    private final boolean isWhite;
    private boolean hasMoved;

    King(boolean isWhite, Piece[][] board) {
        this.isWhite = isWhite;
        this.board = board;
        final int[][] pixels =
                {
                        {0, 0, 1, 1, 0, 0},
                        {1, 0, 1, 1, 0, 1},
                        {1, 1, 1, 1, 1, 1},
                        {1, 1, 1, 1, 1, 1},
                        {0, 1, 1, 1, 1, 0},
                        {0, 1, 1, 1, 1, 0}
                };
        getColor(image, pixels, isWhite);
    }

    @Override
    public Color[][] getImage() {
        return image;
    }

    @Override
    public boolean isWhite() {
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
        final int x1 = (int) start.getX(), x2 = (int) end.getX();
        final int y1 = (int) start.getY(), y2 = (int) end.getY();
        return delta(x2, x1) + delta(y2, y1) != 0 && delta(x2, x1) <= 1 && delta(y2, y1) <= 1
                && canMoveOnto(x2, y2, board, isWhite) && !isCheck(x2, y2);
    }

    @Override
    boolean hasMoved() {
        return hasMoved;
    }

    @Override
    void setMove() {
        hasMoved = true;
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
            if (board[y][x] != null && (board[y][x].getClass() == Bishop.class
                    || board[y][x].getClass() == Queen.class) && board[y][x].isWhite() != isWhite) {
                return true;
            }
            if (board[y][x] != null) {
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
            if (board[y][x] != null && (board[y][x].getClass() == Rook.class
                    || board[y][x].getClass() == Queen.class) && board[y][x].isWhite() != isWhite) {
                return true;
            }
            if (board[y][x] != null) {
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
        return x >= 0 && y >= 0 && x < board.length && y < board.length;
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
     * Determines if the king is in check due to one side a pawn can eat from.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return if the king is in check due to one side a pawn can eat from
     */
    private boolean isEnemyPawn(int x, int y) {
        return x > 0 && y > 0 && x < board.length - 1 && y < board.length - 1 && board[y][x] != null
                && board[y][x].getClass() == Pawn.class && board[y][x].isWhite() != isWhite;
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
     * Determine if the king is in check due to one spot a knight can eat from.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return if the king is in check due to one spot a knight can eat from
     */
    private boolean isEnemyKnight(int x, int y) {
        return x > 0 && y > 0 && x < board.length - 1 && y < board.length - 1 && board[y][x] != null
                && board[y][x].getClass() == Knight.class && board[y][x].isWhite() != isWhite;
    }
}
