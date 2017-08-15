package chess;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

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

    boolean isCheck(int x, int y) {
        return isCheckDiagonal(x, y, -1, -1) || isCheckDiagonal(x, y, -1, 1)
                || isCheckDiagonal(x, y, 1, -1) || isCheckDiagonal(x, y, 1, 1)
                || isCheckStraight(x, y, 0, -1) || isCheckStraight(x, y, 0, 1)
                || isCheckStraight(x, y, -1, 0) || isCheckStraight(x, y, 1, 0)
                || isCheckPawn(x, y) || isCheckKnight(x, y);
    }

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

    private boolean isInGridBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < board.length && y < board.length;
    }

    private boolean isCheckPawn(int xCheck, int yCheck) {
        return isEnemyPawn(xCheck - 1, yCheck - 1) || isEnemyPawn(xCheck + 1, yCheck - 1);
    }

    private boolean isEnemyPawn(int x, int y) {
        return x > 0 && y > 0 && x < board.length - 1 && y < board.length - 1 && board[y][x] != null
                && board[y][x].getClass() == Pawn.class && board[y][x].isWhite() != isWhite;
    }

    private boolean isCheckKnight(int xCheck, int yCheck) {
        return (isEnemyKnight(xCheck - 2, yCheck - 1)) || (isEnemyKnight(xCheck - 1, yCheck - 2))
                || (isEnemyKnight(xCheck - 2, yCheck + 1)) || (isEnemyKnight(xCheck - 1, yCheck + 2))
                || (isEnemyKnight(xCheck + 2, yCheck - 1)) || (isEnemyKnight(xCheck + 1, yCheck - 2))
                || (isEnemyKnight(xCheck + 2, yCheck + 1)) || (isEnemyKnight(xCheck + 1, yCheck + 2));
    }

    private boolean isEnemyKnight(int x, int y) {
        return x > 0 && y > 0 && x < board.length - 1 && y < board.length - 1 && board[y][x] != null
                && board[y][x].getClass() == Knight.class && board[y][x].isWhite() != isWhite;
    }
}
