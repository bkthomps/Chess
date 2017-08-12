package chess;

class Board {
    private static final int BOARD_SIZE = 8;
    private Piece[][] board = new Piece[BOARD_SIZE][BOARD_SIZE];

    void resetBoard() {
        board[0][0] = new Rook(false, board);
        board[0][1] = new Knight(false, board);
        board[0][2] = new Bishop(false, board);
        board[0][3] = new King(false, board);
        board[0][4] = new Queen(false, board);
        board[0][5] = new Bishop(false, board);
        board[0][6] = new Knight(false, board);
        board[0][7] = new Rook(false, board);
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = new Pawn(false, board);
            board[6][i] = new Pawn(true, board);
        }
        board[7][0] = new Rook(true, board);
        board[7][1] = new Knight(true, board);
        board[7][2] = new Bishop(true, board);
        board[7][3] = new Queen(true, board);
        board[7][4] = new King(true, board);
        board[7][5] = new Bishop(true, board);
        board[7][6] = new Knight(true, board);
        board[7][7] = new Rook(true, board);
    }

    Piece[][] getBoard() {
        return board;
    }
}
