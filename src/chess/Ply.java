package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * The state associated with a ply, which is an action taken by either white or black.
 */
final class Ply {
    private final Piece[][] board = new Piece[Board.BOARD_LENGTH][Board.BOARD_WIDTH];
    private final Point enPassant;
    private final int hash;

    Ply(Piece[][] board, Point enPassant) {
        for (int i = 0; i < Board.BOARD_LENGTH; i++) {
            this.board[i] = Arrays.copyOf(board[i], Board.BOARD_WIDTH);
        }
        this.enPassant = enPassant;
        hash = 31 * Objects.hash(this.enPassant) + Arrays.deepHashCode(this.board);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Ply)) {
            return false;
        }
        var history = (Ply) o;
        for (int i = 0; i < board.length; i++) {
            if (!Arrays.equals(board[i], history.board[i])) {
                return false;
            }
        }
        if (enPassant == null) {
            return history.enPassant == null;
        }
        return enPassant.equals(history.enPassant);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
