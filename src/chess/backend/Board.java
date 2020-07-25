package chess.backend;

import java.awt.Color;
import java.util.Arrays;

/**
 * Keeps track of the board and which player is currently moving.
 */
public final class Board {
    public static final int BOARD_LENGTH = 8;
    public static final int BOARD_WIDTH = 8;
    static final int KING_X_COORD = (BOARD_WIDTH - 1) / 2 + 1;

    private final Piece[][] board = new Piece[BOARD_LENGTH][BOARD_WIDTH];
    private final King whiteKing = new King(this, true);
    private final King blackKing = new King(this, false);
    private Point whiteKingLocation;
    private Point blackKingLocation;
    private boolean isWhiteTurn = true;

    Board() {
        setNonPawnRow(0, false);
        setPawnRow(1, false);
        setPawnRow(BOARD_LENGTH - 2, true);
        setNonPawnRow(BOARD_LENGTH - 1, true);
    }

    private void setNonPawnRow(int index, boolean isWhite) {
        board[index][0] = new Rook(this, isWhite);
        board[index][BOARD_WIDTH - 1] = new Rook(this, isWhite);
        board[index][1] = new Knight(this, isWhite);
        board[index][BOARD_WIDTH - 2] = new Knight(this, isWhite);
        board[index][2] = new Bishop(this, isWhite);
        board[index][BOARD_WIDTH - 3] = new Bishop(this, isWhite);
        board[index][(BOARD_WIDTH - 1) / 2] = new Queen(this, isWhite);
        if (isWhite) {
            board[index][KING_X_COORD] = whiteKing;
            whiteKingLocation = Point.instance(KING_X_COORD, index);
        } else {
            board[index][KING_X_COORD] = blackKing;
            blackKingLocation = Point.instance(KING_X_COORD, index);
        }
    }

    private void setPawnRow(int index, boolean isWhite) {
        for (int i = 0; i < BOARD_WIDTH; i++) {
            board[index][i] = new Pawn(this, isWhite);
        }
    }

    Color[][] getPieceImage(Point point) {
        if (board[point.y()][point.x()] == null) {
            return null;
        }
        return board[point.y()][point.x()].getPieceImage();
    }

    Piece[][] copyBoard() {
        var boardCopy = new Piece[BOARD_LENGTH][BOARD_WIDTH];
        for (int i = 0; i < BOARD_LENGTH; i++) {
            boardCopy[i] = Arrays.copyOf(board[i], BOARD_WIDTH);
        }
        return boardCopy;
    }

    void flip() {
        isWhiteTurn = !isWhiteTurn;
        var oppositeWhiteKingLocation = BOARD_LENGTH - 1 - whiteKingLocation.y();
        var oppositeBlackKingLocation = BOARD_LENGTH - 1 - blackKingLocation.y();
        whiteKingLocation = Point.instance(whiteKingLocation.x(), oppositeWhiteKingLocation);
        blackKingLocation = Point.instance(blackKingLocation.x(), oppositeBlackKingLocation);
        for (int i = 0; i < BOARD_LENGTH / 2; i++) {
            var tempSlice = board[BOARD_LENGTH - i - 1];
            board[BOARD_LENGTH - i - 1] = board[i];
            board[i] = tempSlice;
        }
    }

    void moveKing(Point point) {
        if (isWhiteTurn) {
            whiteKingLocation = point;
        } else {
            blackKingLocation = point;
        }
    }

    King getAlliedKing() {
        return isWhiteTurn ? whiteKing : blackKing;
    }

    Point locateAlliedKing() {
        return isWhiteTurn ? whiteKingLocation : blackKingLocation;
    }

    Piece getAlliedPieceAt(Point point) {
        var piece = board[point.y()][point.x()];
        if (piece == null || piece.isWhite() != isWhiteTurn) {
            return null;
        }
        return piece;
    }

    Piece getBoard(Point point) {
        return board[point.y()][point.x()];
    }

    void setBoard(Point point, Piece piece) {
        board[point.y()][point.x()] = piece;
    }

    boolean isAlly(Piece piece) {
        return isWhiteTurn == piece.isWhite();
    }

    boolean isLightTile(Point point) {
        return ((point.x() + point.y()) % 2 == 0 ^ !isWhiteTurn);
    }

    Piece getPromotionPiece(PromotionPiece promotion) {
        switch (promotion) {
            case QUEEN:
                return new Queen(this, isWhiteTurn);
            case KNIGHT:
                return new Knight(this, isWhiteTurn);
            case ROOK:
                return new Rook(this, isWhiteTurn);
            case BISHOP:
                return new Bishop(this, isWhiteTurn);
            default:
                throw new IllegalStateException("Invalid promotion piece");
        }
    }
}
