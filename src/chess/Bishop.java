package chess;

import java.awt.Color;
import java.awt.Point;

class Bishop extends Piece {

    private Color[][] image = new Color[PIECE_SIZE][PIECE_SIZE];
    private final Piece[][] board;
    private final boolean isWhite;
    private boolean hasMoved;

    Bishop(boolean isWhite, Piece[][] board) {
        this.isWhite = isWhite;
        this.board = board;
        final int[][] pixels =
                {
                        {0, 0, 1, 1, 0, 0},
                        {0, 0, 1, 1, 0, 0},
                        {0, 1, 1, 1, 1, 0},
                        {0, 1, 1, 1, 1, 0},
                        {0, 0, 1, 1, 0, 0},
                        {1, 1, 1, 1, 1, 1}
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
        return isEachCoordinateDeltaSame(start, end) && isBishopActionLegal(start, end);
    }

    boolean isEachCoordinateDeltaSame(Point start, Point end) {
        return delta(end.getY(), start.getY()) == delta(end.getX(), start.getX());
    }

    boolean isBishopActionLegal(Point start, Point end) {
        final int x1 = (int) start.getX(), x2 = (int) end.getX();
        final int y1 = (int) start.getY(), y2 = (int) end.getY();
        final int xSign = (x2 > x1) ? 1 : -1;
        final int ySign = (y2 > y1) ? 1 : -1;
        int x = x1;
        int y = y1;
        for (int i = x1 + 1; i < x2; i++) {
            x += xSign;
            y += ySign;
            if (board[y][x] != null) {
                return false;
            }
        }
        return canMoveOnto(x2, y2, board, isWhite);
    }

    @Override
    boolean hasMoved() {
        return hasMoved;
    }

    @Override
    void setMove() {
        hasMoved = true;
    }
}
