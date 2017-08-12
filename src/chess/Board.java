package chess;

class Board {
    private static final int BOARD_SIZE = 8;
    private Piece[][] board = new Piece[BOARD_SIZE][BOARD_SIZE];

    void resetBoard() {
        board[0][0] = new Rook(false);
        board[0][1] = new Knight(false);
        board[0][2] = new Bishop(false);
        board[0][3] = new King(false);
        board[0][4] = new Queen(false);
        board[0][5] = new Bishop(false);
        board[0][6] = new Knight(false);
        board[0][7] = new Rook(false);
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = new Pawn(false);
            board[6][i] = new Pawn(true);
        }
        board[7][0] = new Rook(true);
        board[7][1] = new Knight(true);
        board[7][2] = new Bishop(true);
        board[7][3] = new Queen(true);
        board[7][4] = new King(true);
        board[7][5] = new Bishop(true);
        board[7][6] = new Knight(true);
        board[7][7] = new Rook(true);
    }

    Piece[][] getBoard() {
        return board;
    }
}
