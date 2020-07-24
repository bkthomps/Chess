package chess;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Keeps track of the logic of the board, which contains information associated with the pieces on
 * the board, as well as historic information such as the history of moves which have been taken.
 */
final class Board {
    static final int BOARD_LENGTH = 8;
    static final int BOARD_WIDTH = 8;

    private static final Piece[][] board = new Piece[BOARD_LENGTH][BOARD_WIDTH];
    private static final King whiteKing = new King(true);
    private static final King blackKing = new King(false);
    private static Point whiteKingLocation;
    private static Point blackKingLocation;

    private boolean isWhiteTurn = true;
    private Point enPassant;

    private final List<Piece[][]> boardHistory = new ArrayList<>();
    private final List<Boolean> canCastleHistory = new ArrayList<>();
    private final List<Point> enPassantHistory = new ArrayList<>();
    private int drawCounter;

    Board() {
        setNonPawnRow(0, false);
        setPawnRow(1, false);
        setPawnRow(BOARD_LENGTH - 2, true);
        setNonPawnRow(BOARD_LENGTH - 1, true);
    }

    private void setNonPawnRow(int index, boolean isWhite) {
        board[index][0] = new Rook(isWhite);
        board[index][1] = new Knight(isWhite);
        board[index][2] = new Bishop(isWhite);
        board[index][3] = new Queen(isWhite);
        if (isWhite) {
            board[index][4] = whiteKing;
            whiteKingLocation = Point.instance(4, index);
        } else {
            board[index][4] = blackKing;
            blackKingLocation = Point.instance(4, index);
        }
        board[index][5] = new Bishop(isWhite);
        board[index][6] = new Knight(isWhite);
        board[index][7] = new Rook(isWhite);
    }

    private void setPawnRow(int index, boolean isWhite) {
        for (int i = 0; i < BOARD_WIDTH; i++) {
            board[index][i] = new Pawn(isWhite);
        }
    }

    void queenSideCastle() {
        performCastling(Point.instance(4, 7), Point.instance(2, 7));
        if (isWhiteTurn) {
            whiteKingLocation = Point.instance(2, 7);
        } else {
            blackKingLocation = Point.instance(2, 7);
        }
        performCastling(Point.instance(0, 7), Point.instance(3, 7));
        flipBoard();
        enPassant = null;
        enPassantHistory.add(null);
    }

    void kingSideCastle() {
        performCastling(Point.instance(4, 7), Point.instance(6, 7));
        if (isWhiteTurn) {
            whiteKingLocation = Point.instance(6, 7);
        } else {
            blackKingLocation = Point.instance(6, 7);
        }
        performCastling(Point.instance(7, 7), Point.instance(5, 7));
        flipBoard();
        enPassant = null;
        enPassantHistory.add(null);
    }

    private void performCastling(Point from, Point to) {
        setBoard(to, getBoard(from));
        setBoard(from, null);
        getBoard(to).setMove();
    }

    void enPassant(Piece moving, Point from) {
        var squareAboveEnemy = Point.instance(enPassant.x(), enPassant.y() + 1);
        movePiece(moving, from, enPassant);
        setBoard(squareAboveEnemy, null);
        enPassant = null;
        enPassantHistory.add(null);
        flipBoard();
        checkIfGameOver();
    }

    void doMove(Piece piece, Point from, Point to) {
        if (piece instanceof King) {
            if (isWhiteTurn) {
                whiteKingLocation = to;
            } else {
                blackKingLocation = to;
            }
        }
        recordEnPassantHistory(piece, from, to);
        movePiece(piece, from, to);
        flipBoard();
        checkIfGameOver();
    }

    private void movePiece(Piece piece, Point start, Point end) {
        if (getBoard(end) != null || piece instanceof Pawn) {
            drawCounter = 0;
            boardHistory.clear();
            enPassantHistory.clear();
            canCastleHistory.clear();
        } else {
            drawCounter++;
            var boardCopy = new Piece[BOARD_LENGTH][BOARD_WIDTH];
            for (int i = 0; i < BOARD_LENGTH; i++) {
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    var point = Point.instance(j, i);
                    boardCopy[i][j] = getBoard(point);
                }
            }
            boardHistory.add(boardCopy);
            canCastleHistory.add(getAlliedKing(isWhiteTurn).hasMoved());
        }
        rawMove(piece, start, end);
        piece.setMove();
    }

    private void flipBoard() {
        isWhiteTurn = !isWhiteTurn;
        whiteKingLocation = Point.instance(whiteKingLocation.x(), BOARD_LENGTH - 1 - whiteKingLocation.y());
        blackKingLocation = Point.instance(blackKingLocation.x(), BOARD_LENGTH - 1 - blackKingLocation.y());
        for (int i = 0; i < BOARD_LENGTH / 2; i++) {
            var tempSlice = board[BOARD_LENGTH - i - 1];
            board[BOARD_LENGTH - i - 1] = board[i];
            board[i] = tempSlice;
        }
    }

    private void recordEnPassantHistory(Piece moving, Point from, Point to) {
        if (moving instanceof Pawn && from.y() - to.y() == 2) {
            enPassant = Point.instance(to.x(), to.y() - 2);
        } else {
            enPassant = null;
        }
        enPassantHistory.add(enPassant);
    }

    /**
     * The game may be over by checkmate or by draw. There are 4 types of draws:
     * <p> 1. Stalemate
     * <p> 2. 50 moves without pawn move or piece capture
     * <p> 3. Board repeated 3 times
     * <p> 4. Insufficient mating material
     */
    private void checkIfGameOver() {
        if (isGameOverDueToCheckmate(getAlliedKing(isWhiteTurn), locateAlliedKing(isWhiteTurn))) {
            var text = Frontend.RESOURCE.getString(isWhiteTurn ? "blackWins" : "whiteWins");
            endGameWithNotification(text);
        } else if (isGameOverDueToStalemate(getAlliedKing(isWhiteTurn), locateAlliedKing(isWhiteTurn))) {
            var text = Frontend.RESOURCE.getString("stalemate");
            endGameWithNotification(text);
        }
        endGameIfNonStalemateDraw();
        warnIfKingInCheck(getAlliedKing(isWhiteTurn), locateAlliedKing(isWhiteTurn));
    }

    private boolean isGameOverDueToCheckmate(King king, Point point) {
        return king.isKingInCheck(point) && isMoveImpossible(king, point);
    }

    private boolean isGameOverDueToStalemate(King king, Point point) {
        return !king.isKingInCheck(point) && isMoveImpossible(king, point);
    }

    private void endGameWithNotification(String text) {
        String[] options = {Frontend.RESOURCE.getString("acknowledge")};
        Frontend.displayDialogText(text, options);
        System.exit(0);
    }

    private void warnIfKingInCheck(King king, Point location) {
        if (king.isKingInCheck(location)) {
            var text = Frontend.RESOURCE.getString("inCheck");
            String[] options = {Frontend.RESOURCE.getString("acknowledge")};
            Frontend.displayDialogText(text, options);
        }
    }

    /**
     * @throws IllegalStateException if king is not at the specified area
     */
    private boolean isMoveImpossible(King king, Point point) {
        if (!(getBoard(point) instanceof King)) {
            throw new IllegalStateException("King not where specified!");
        }
        for (int i = 0; i < BOARD_LENGTH; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                var start = Point.instance(j, i);
                var piece = getBoard(start);
                if (piece != null && piece.isWhite() == isWhiteTurn) {
                    for (int k = 0; k < BOARD_LENGTH; k++) {
                        for (int l = 0; l < BOARD_WIDTH; l++) {
                            var end = Point.instance(l, k);
                            var save = getBoard(end);
                            if (piece.isActionLegal(start, end)) {
                                rawMove(piece, start, end);
                                var kingLocation = locateAlliedKing(isWhiteTurn);
                                boolean isNotInCheck = !king.isKingInCheck(kingLocation);
                                rawMove(piece, end, start);
                                setBoard(end, save);
                                if (isNotInCheck) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Moves the piece without setting the piece to moved state. Used
     * when checking the board and not actually moving pieces on it.
     */
    private void rawMove(Piece piece, Point start, Point end) {
        setBoard(start, null);
        setBoard(end, piece);
    }

    private void endGameIfNonStalemateDraw() {
        endGameIfTooManyMoves();
        endGameIfTooManyBoardRepetitions();
        endGameIfInsufficientMatingMaterial();
    }

    /**
     * Draw if 50 moves without pawn move or piece capture.
     *
     * @throws IllegalStateException if somehow went over the draw counter
     */
    private void endGameIfTooManyMoves() {
        int maxMovePerSide = 50;
        int maxUnproductiveMoves = 2 * maxMovePerSide;
        if (drawCounter == maxUnproductiveMoves) {
            var text = Frontend.RESOURCE.getString("draw") + ' '
                    + maxMovePerSide + ' ' + Frontend.RESOURCE.getString("noCapture");
            endGameWithNotification(text);
        } else if (drawCounter > maxUnproductiveMoves) {
            throw new IllegalStateException("drawCounter > " + maxUnproductiveMoves);
        }
    }

    /**
     * Draw if board repeated 3 times.
     */
    private void endGameIfTooManyBoardRepetitions() {
        if (isTooManyBoardRepetitions()) {
            var text = Frontend.RESOURCE.getString("boardRepeat");
            endGameWithNotification(text);
        }
    }

    /**
     * @return true if the board has repeated 3 times
     * @throws IllegalStateException if castle or en passant history size is invalid
     */
    private boolean isTooManyBoardRepetitions() {
        int historySize = boardHistory.size();
        Point enPassantBackup = null;
        if (historySize == enPassantHistory.size() - 1) {
            enPassantBackup = enPassantHistory.get(enPassantHistory.size() - 1);
            enPassantHistory.remove(enPassantHistory.size() - 1);
        }
        if (historySize != canCastleHistory.size() || historySize != enPassantHistory.size()) {
            throw new IllegalStateException("History lists are not same size!");
        }
        for (int i = 0; i < historySize; i++) {
            int count = 0;
            for (int j = 0; j < historySize; j++) {
                if (areBoardsEqual(boardHistory.get(i), boardHistory.get(j))
                        && canCastleHistory.get(i).equals(canCastleHistory.get(j))
                        && ((enPassantHistory.get(i) == null && enPassantHistory.get(j) == null)
                        || (enPassantHistory.get(i) != null && enPassantHistory.get(j) != null
                        && enPassantHistory.get(i).equals(enPassantHistory.get(j))))) {
                    count++;
                }
                if (count == 3) {
                    return true;
                }
            }
        }
        if (enPassantBackup != null) {
            enPassantHistory.add(enPassantBackup);
        }
        return false;
    }

    /**
     * Mating material is any pieces which could force a checkmate.
     */
    private void endGameIfInsufficientMatingMaterial() {
        var ally = new ArrayList<Piece>();
        var enemy = new ArrayList<Piece>();
        findPieces(ally, enemy);
        if (isInsufficientMatingMaterial(ally, enemy)) {
            var text = Frontend.RESOURCE.getString("insufficientPieces");
            endGameWithNotification(text);
        }
    }

    private void findPieces(List<Piece> ally, List<Piece> enemy) {
        for (int i = 0; i < BOARD_LENGTH; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                var point = Point.instance(j, i);
                var piece = getBoard(point);
                if (piece != null && !(piece instanceof King)) {
                    if (piece.isWhite() == isWhiteTurn) {
                        ally.add(piece);
                    } else {
                        enemy.add(piece);
                    }
                }
            }
        }
    }

    /**
     * @return is lone King against:
     * lone King, or King and Knight, or King and Bishop, or King and two Knights
     */
    private boolean isInsufficientMatingMaterial(List<Piece> ally, List<Piece> enemy) {
        if (ally.size() != 0 && enemy.size() != 0) {
            return false;
        }
        var group = (ally.size() == 0) ? enemy : ally;
        return group.size() == 0
                || (group.size() == 1 && (group.get(0) instanceof Knight || group.get(0) instanceof Bishop))
                || (group.size() == 2 && group.get(0) instanceof Knight && group.get(1) instanceof Knight);
    }

    Color[][] getPieceImage(int x, int y) {
        if (board[y][x] == null) {
            return null;
        }
        return board[y][x].getPieceImage();
    }

    boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    static King getAlliedKing(boolean isWhiteTurn) {
        return isWhiteTurn ? whiteKing : blackKing;
    }

    static Point locateAlliedKing(boolean isWhiteTurn) {
        return isWhiteTurn ? whiteKingLocation : blackKingLocation;
    }

    Piece getAlliedPieceAt(Point point) {
        var piece = board[point.y()][point.x()];
        if (piece == null || piece.isWhite() != isWhiteTurn) {
            return null;
        }
        return piece;
    }

    boolean areBoardsEqual(Piece[][] boardOne, Piece[][] boardTwo) {
        for (int i = 0; i < BOARD_LENGTH; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (boardOne[i][j] != boardTwo[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    static Piece getBoard(Point point) {
        return board[point.y()][point.x()];
    }

    static void setBoard(Point point, Piece piece) {
        board[point.y()][point.x()] = piece;
    }

    Move[][] availableMoves(Piece moving, Point from) {
        var moves = new Move[BOARD_LENGTH][BOARD_WIDTH];
        for (var slice : moves) {
            Arrays.fill(slice, Move.NONE);
        }
        for (int i = 0; i < BOARD_LENGTH; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                var checkAt = Point.instance(j, i);
                if (moving instanceof King) {
                    var backup = getBoard(from);
                    setBoard(from, null);
                    if (moving.isActionLegal(from, checkAt)) {
                        moves[i][j] = Move.NORMAL;
                    }
                    setBoard(from, backup);
                } else if (moving.isActionLegal(from, checkAt)) {
                    if (moving instanceof Pawn && checkAt.y() == 0) {
                        moves[i][j] = Move.PAWN_PROMOTION;
                    } else {
                        moves[i][j] = Move.NORMAL;
                    }
                }
            }
        }
        if (canQueenSideCastle(from)) {
            moves[BOARD_LENGTH - 1][0] = Move.QUEEN_SIDE_CASTLE;
        }
        if (canKingSideCastle(from)) {
            moves[BOARD_LENGTH - 1][BOARD_WIDTH - 1] = Move.KING_SIDE_CASTLE;
        }
        if (enPassant != null) {
            boolean canCaptureEnPassant =
                    from.y() == enPassant.y() + 1 && Math.abs(from.x() - enPassant.x()) == 1;
            if (canCaptureEnPassant && canPerformEnPassant(moving, enPassant)) {
                moves[enPassant.y()][enPassant.x()] = Move.EN_PASSANT;
            }
        }
        return moves;
    }

    private boolean canQueenSideCastle(Point from) {
        var king = new King(isWhiteTurn);
        boolean isLockedOnKing = from.x() == 4 && from.y() == 7;
        boolean isClearPath = hasPieceMoved(getBoard(Point.instance(0, 7)))
                && getBoard(Point.instance(1, 7)) == null && getBoard(Point.instance(2, 7)) == null
                && getBoard(Point.instance(3, 7)) == null && hasPieceMoved(getBoard(Point.instance(4, 7)));
        boolean isNotPassingThroughCheck = !king.isKingInCheck(Point.instance(4, 7))
                && !king.isKingInCheck(Point.instance(3, 7)) && !king.isKingInCheck(Point.instance(2, 7));
        return isLockedOnKing && isClearPath && isNotPassingThroughCheck;
    }

    private boolean canKingSideCastle(Point from) {
        var king = new King(isWhiteTurn);
        boolean isLockedOnKing = from.x() == 4 && from.y() == 7;
        boolean isClearPath = hasPieceMoved(getBoard(Point.instance(4, 7))) && getBoard(Point.instance(5, 7)) == null
                && getBoard(Point.instance(6, 7)) == null && hasPieceMoved(getBoard(Point.instance(7, 7)));
        boolean isNotPassingThroughCheck = !king.isKingInCheck(Point.instance(4, 7))
                && !king.isKingInCheck(Point.instance(5, 7)) && !king.isKingInCheck(Point.instance(6, 7));
        return isLockedOnKing && isClearPath && isNotPassingThroughCheck;
    }

    private boolean hasPieceMoved(Piece piece) {
        return piece != null && !piece.hasMoved();
    }

    private boolean canPerformEnPassant(Piece moving, Point to) {
        var squareAboveEnemy = Point.instance(to.x(), to.y() + 1);
        return to.equals(enPassant) && moving instanceof Pawn
                && moving.isWhite() != getBoard(squareAboveEnemy).isWhite();
    }
}
