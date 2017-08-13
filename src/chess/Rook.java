package chess;

import java.awt.Color;
import java.awt.Point;

class Rook extends Piece {

    private Color[][] image = new Color[PIECE_SIZE][PIECE_SIZE];
    private final Piece[][] board;
    private final boolean isWhite;

    Rook(boolean isWhite, Piece[][] board) {
        this.isWhite = isWhite;
        this.board = board;
        final int[][] pixels =
                {
                        {1, 0, 1, 1, 0, 1},
                        {1, 1, 1, 1, 1, 1},
                        {0, 1, 1, 1, 1, 0},
                        {0, 1, 1, 1, 1, 0},
                        {0, 1, 1, 1, 1, 0},
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
        final int x1 = (int) start.getX(), x2 = (int) end.getX();
        final int y1 = (int) start.getY(), y2 = (int) end.getY();
        if ((x1 == x2 && y1 == y2) || (x1 != x2 && y1 != y2)) {
            return false;
        }
        if (x1 == x2) {
            final int one = (y1 < y2) ? y1 : y2;
            final int two = (y1 < y2) ? y2 : y1;
            for (int i = one + 1; i < two; i++) {
                if (board[i][x1] != null) {
                    return false;
                }
            }
        } else {
            final int one = (x1 < x2) ? x1 : x2;
            final int two = (x1 < x2) ? x2 : x1;
            for (int i = one + 1; i < two; i++) {
                if (board[y1][i] != null) {
                    return false;
                }
            }

        }
        return canMoveOnto(x2, y2, board, isWhite);
    }
}
