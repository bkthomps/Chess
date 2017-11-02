package chess;

import java.awt.Color;
import java.awt.Point;

/**
 * The knight, sometimes called the horse, is the chess piece which must travel in an L-shape, moving either 2 spaces
 * horizontally and 1 vertically or vice-versa. It can jump over other pieces.
 */
final class Knight extends Piece {

    private final Color[][] image = new Color[PIECE_SIZE][PIECE_SIZE];
    private final Piece[][] board;
    private final boolean isWhite;
    private boolean hasMoved;

    Knight(boolean isWhite, Piece[][] board) {
        this.isWhite = isWhite;
        this.board = board;
        final int[][] pixels =
                {
                        {0, 1, 0, 0, 0, 0},
                        {1, 1, 1, 0, 0, 0},
                        {1, 1, 1, 1, 0, 0},
                        {0, 0, 1, 1, 1, 0},
                        {0, 1, 1, 1, 1, 1},
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

    /**
     * Determines if the action is legal. The action is legal if it moves 2 spaces in one direction and 1 in the other.
     * It may eat any piece as it would move normally.
     *
     * @param start the beginning position
     * @param end   the final position
     * @return if the action is legal
     */
    @Override
    boolean isActionLegal(Point start, Point end) {
        final int x1 = (int) start.getX(), x2 = (int) end.getX();
        final int y1 = (int) start.getY(), y2 = (int) end.getY();
        return ((delta(x1, x2) == 2 && delta(y1, y2) == 1) || (delta(x1, x2) == 1 && delta(y1, y2) == 2)
                && canMoveOnto(x2, y2, board, isWhite))
                && GameState.wouldNotPutKingIntoCheck(this, start, end, board, isWhite);
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
