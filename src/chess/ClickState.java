package chess;

import chess.backend.Move;
import chess.backend.Piece;
import chess.backend.Point;

/**
 * Keeps track of what the user has clicked on. There are two modes: the first one in which the user
 * has not clicked on anything, and nothing is highlighted, and the second one where the cursor is
 * fixed on a piece and its possible moves are highlighted.
 */
final class ClickState {
    private static final ClickState nullInstance = new ClickState(null, null, null);
    private final Piece moving;
    private final Point from;
    private final Move[][] moves;

    private ClickState(Piece moving, Point from, Move[][] moves) {
        this.moving = moving;
        this.from = from;
        this.moves = moves;
    }

    static ClickState firstClickInstance() {
        return nullInstance;
    }

    static ClickState secondClickInstance(Piece moving, Point from, Move[][] moves) {
        if (moving == null || from == null || moves == null) {
            throw new IllegalArgumentException("Second click must not contain null");
        }
        return new ClickState(moving, from, moves);
    }

    boolean isFirstClick() {
        return moving == null;
    }

    Move getMove(Point point) {
        if (isFirstClick()) {
            throw new IllegalStateException("Must only call this method on second click");
        }
        return moves[point.y()][point.x()];
    }

    Piece getMoving() {
        if (isFirstClick()) {
            throw new IllegalStateException("Must only call this method on second click");
        }
        return moving;
    }

    Point getFrom() {
        if (isFirstClick()) {
            throw new IllegalStateException("Must only call this method on second click");
        }
        return from;
    }
}
