package chess;

import java.awt.Point;

/**
 * The knight, sometimes called the horse, is the chess piece which must travel in an L-shape, moving either two spaces
 * horizontally and one vertically or vice-versa. It can jump over other pieces.
 */
final class Knight extends Piece {

    private final boolean isWhite;

    Knight(boolean isWhite) {
        this.isWhite = isWhite;
        final int[][] pixels =
                {
                        {0, 1, 0, 0, 0, 0},
                        {1, 1, 1, 0, 0, 0},
                        {1, 1, 1, 1, 0, 0},
                        {0, 0, 1, 1, 1, 0},
                        {0, 1, 1, 1, 1, 1},
                        {1, 1, 1, 1, 1, 1}
                };
        getColor(pixels);
    }

    @Override
    boolean isWhite() {
        return isWhite;
    }

    /**
     * Determines if the action is legal. The action is legal if it moves 2 spaces in one direction and 1 in the other.
     * It may capture any piece as it would move normally.
     *
     * @param start the beginning position
     * @param end   the final position
     * @return if the action is legal
     */
    @Override
    boolean isActionLegal(Point start, Point end) {
        final boolean isMoveL = (Math.abs(end.x - start.x) == 2 && Math.abs(end.y - start.y) == 1)
                || (Math.abs(end.x - start.x) == 1 && Math.abs(end.y - start.y) == 2);
        return isMoveL && canMoveOnto(end.x, end.y) && wouldNotPutKingIntoCheck(start, end);
    }
}
