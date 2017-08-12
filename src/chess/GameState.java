package chess;

import java.awt.Point;

class GameState {

    private Piece[][] board;
    private Chess chess;
    private boolean isWhiteTurn = true;
    private Piece moving;
    private Point from;

    GameState(Piece[][] board, Chess chess) {
        this.board = board;
        this.chess = chess;
    }

    void clicked(int x, int y) {
        if (moving == null) {
            final Piece toMove = board[y][x];
            if (toMove != null && toMove.isWhite() == isWhiteTurn) {
                moving = toMove;
                from = new Point(x, y);
                return;
            }
            moving = null;
            from = null;
            return;
        }
        final Point to = new Point(x, y);
        if (moving.isActionLegal(from, to)) {
            isWhiteTurn = !isWhiteTurn;
            flipBoard();
        }
        moving = null;
        from = null;
    }

    private void flipBoard() {
        final int BOARD_SIZE = board.length;
        for (int i = 0; i < BOARD_SIZE / 2; i++) {
            final Piece[] tempSlice = board[BOARD_SIZE - i - 1];
            board[BOARD_SIZE - i - 1] = board[i];
            board[i] = tempSlice;
        }
        chess.refreshPixels();
    }
}
