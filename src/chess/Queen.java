package chess;

import java.awt.Color;
import java.awt.Point;

class Queen extends Piece {

    private Color[][] image = new Color[PIECE_SIZE][PIECE_SIZE];
    private final Piece[][] board;
    private final boolean isWhite;
    private boolean hasMoved;

    Queen(boolean isWhite, Piece[][] board) {
        this.isWhite = isWhite;
        this.board = board;
        final int[][] pixels =
                {
                        {0, 1, 0, 0, 1, 0},
                        {0, 0, 1, 1, 0, 0},
                        {1, 0, 1, 1, 0, 1},
                        {1, 0, 1, 1, 0, 1},
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
        final Bishop bishop = new Bishop(isWhite, board);
        final Rook rook = new Rook(isWhite, board);
        if (bishop.isEachCoordinateDeltaSame(start, end)) {
            return bishop.isBishopActionLegal(start, end);
        }
        return rook.isActionLegal(start, end);
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
