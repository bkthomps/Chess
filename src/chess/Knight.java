package chess;

import java.awt.Point;

/**
 * The knight is a chess piece which must travel in an L-shape. It may move two spaces horizontally
 * and one space vertically, or vice versa. It can also travel through pieces. If the destination
 * piece is an enemy piece, it captures it. If the destination piece is a friendly piece, it may not
 * perform the move.
 */
final class Knight extends Piece {
    private static final int[][] pixels = {
            {0, 1, 0, 0, 0, 0},
            {1, 1, 1, 0, 0, 0},
            {1, 1, 1, 1, 0, 0},
            {0, 0, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1}
    };

    Knight(boolean isWhite) {
        super(isWhite, pixels);
    }

    @Override
    boolean isActionLegal(Point start, Point end) {
        boolean isMoveL = (Math.abs(end.x - start.x) == 2 && Math.abs(end.y - start.y) == 1)
                || (Math.abs(end.x - start.x) == 1 && Math.abs(end.y - start.y) == 2);
        return isMoveL && canMoveToLocation(end) && wouldNotPutAlliedKingIntoCheck(start, end);
    }
}
