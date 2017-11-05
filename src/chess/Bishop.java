package chess;

import java.awt.Color;
import java.awt.Point;

/**
 * The bishop is the chess piece which can travel as many squares as it wants diagonally, as long as it does not travel
 * through anything.
 */
final class Bishop extends Piece {

    private final Color[][] image = new Color[PIECE_SIZE][PIECE_SIZE];
    private final boolean isWhite;
    private boolean hasMoved;

    Bishop(boolean isWhite) {
        this.isWhite = isWhite;
        final int[][] pixels =
                {
                        {0, 0, 1, 1, 0, 0},
                        {0, 0, 1, 1, 0, 0},
                        {0, 1, 1, 1, 1, 0},
                        {0, 1, 1, 1, 1, 0},
                        {0, 0, 1, 1, 0, 0},
                        {1, 1, 1, 1, 1, 1}
                };
        getColor(pixels);
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
     * Determine if the action is legal.
     *
     * @param start the original position
     * @param end   the final position
     * @return if the action is legal
     */
    @Override
    boolean isActionLegal(Point start, Point end) {
        return isEachCoordinateDeltaSame(start, end) && isBishopActionLegal(start, end)
                && wouldNotPutKingIntoCheck(start, end);
    }

    /**
     * Checks to see if the change in x is the same as the change in y.
     *
     * @param start the original position
     * @param end   the final position
     * @return if the change in x is the same as the change in y
     */
    boolean isEachCoordinateDeltaSame(Point start, Point end) {
        return delta(end.getY(), start.getY()) == delta(end.getX(), start.getX());
    }

    /**
     * Bishop can capture enemy and can travel as long as nothing is in its way.
     *
     * @param start the original position
     * @param end   the final position
     * @return if bishop action is legal
     */
    boolean isBishopActionLegal(Point start, Point end) {
        final int x1 = (int) start.getX(), x2 = (int) end.getX();
        final int y1 = (int) start.getY(), y2 = (int) end.getY();
        final int min = Math.min(x1, x2);
        final int max = Math.max(x1, x2);
        int x = x1;
        int y = y1;
        for (int i = min + 1; i < max; i++) {
            x += Math.signum(x2 - x1);
            y += Math.signum(y2 - y1);
            if (Chess.board[y][x] != null) {
                return false;
            }
        }
        return canMoveOnto(x2, y2);
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
