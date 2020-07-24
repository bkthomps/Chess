package chess;

import java.util.Arrays;
import java.util.Objects;

final class GameState {
    private final Piece[][] board = new Piece[Board.BOARD_LENGTH][Board.BOARD_WIDTH];
    private final Point enPassant;
    // FIXME: castling is not only prevented by the king having moved, but also the rook
    // FIXME: must not have been moved, so instead, just clear this board when the king or rook moves
    private final boolean canCastle;
    private final int hash;

    GameState(Piece[][] board, Point enPassant, boolean canCastle) {
        for (int i = 0; i < Board.BOARD_LENGTH; i++) {
            this.board[i] = Arrays.copyOf(board[i], Board.BOARD_WIDTH);
        }
        this.enPassant = enPassant;
        this.canCastle = canCastle;
        hash = 31 * Objects.hash(this.enPassant, this.canCastle) + Arrays.deepHashCode(this.board);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GameState)) {
            return false;
        }
        var history = (GameState) o;
        for (int i = 0; i < board.length; i++) {
            if (!Arrays.equals(board[i], history.board[i])) {
                return false;
            }
        }
        if (canCastle != history.canCastle) {
            return false;
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
