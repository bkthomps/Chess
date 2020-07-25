package chess.backend;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Keeps track of the game, which means the board and its pieces, and the board history.
 */
public final class Game {
    private final Board board = new Board();
    private final Map<Ply, Integer> history = new HashMap<>();
    private int drawCounter;
    private Point enPassant;

    public void queenSideCastle() {
        var king = Point.instance(Board.KING_X_COORD, Board.BOARD_LENGTH - 1);
        var rook = Point.instance(0, Board.BOARD_LENGTH - 1);
        performCastling(king, Point.instance(Board.KING_X_COORD - 2, Board.BOARD_LENGTH - 1));
        performCastling(rook, Point.instance(Board.KING_X_COORD - 1, Board.BOARD_LENGTH - 1));
        finishCastling(Point.instance(Board.KING_X_COORD - 2, Board.BOARD_LENGTH - 1));
    }

    public void kingSideCastle() {
        var king = Point.instance(Board.KING_X_COORD, Board.BOARD_LENGTH - 1);
        var rook = Point.instance(Board.BOARD_WIDTH - 1, Board.BOARD_LENGTH - 1);
        performCastling(king, Point.instance(Board.KING_X_COORD + 2, Board.BOARD_LENGTH - 1));
        performCastling(rook, Point.instance(Board.KING_X_COORD + 1, Board.BOARD_LENGTH - 1));
        finishCastling(Point.instance(Board.KING_X_COORD + 2, Board.BOARD_LENGTH - 1));
    }

    private void performCastling(Point from, Point to) {
        board.setBoard(to, board.getBoard(from));
        board.setBoard(from, null);
        board.getBoard(to).setMove();
    }

    private void finishCastling(Point point) {
        board.moveKing(point);
        enPassant = null;
        history.clear();
        board.flip();
    }

    public GameStatus enPassant(Piece moving, Point from) {
        var squareAboveEnemy = Point.instance(enPassant.x(), enPassant.y() + 1);
        movePiece(moving, from, enPassant);
        board.setBoard(squareAboveEnemy, null);
        board.flip();
        return gameOverState(0);
    }

    public GameStatus pawnPromotion(PromotionPiece promotion, Point from, Point to) {
        return doMove(board.getPromotionPiece(promotion), from, to);
    }

    public GameStatus normalMove(Piece moving, Point from, Point to) {
        return doMove(moving, from, to);
    }

    private GameStatus doMove(Piece piece, Point from, Point to) {
        if (piece instanceof King) {
            board.moveKing(to);
        }
        int repetitionCount = movePiece(piece, from, to);
        board.flip();
        return gameOverState(repetitionCount);
    }

    private int movePiece(Piece piece, Point start, Point end) {
        int count = 0;
        if (piece instanceof Pawn && start.y() - end.y() == 2) {
            enPassant = Point.instance(end.x(), Board.BOARD_LENGTH - 1 - end.y() - 1);
        } else {
            enPassant = null;
        }
        if (board.getBoard(end) != null || piece instanceof Pawn) {
            drawCounter = 0;
            history.clear();
        } else {
            drawCounter++;
            var gameState = new Ply(board, enPassant);
            count = history.getOrDefault(gameState, 0) + 1;
            history.put(gameState, count);
        }
        rawMove(piece, start, end);
        piece.setMove();
        return count;
    }

    public Color[][] getPieceImage(Point point) {
        return board.getPieceImage(point);
    }

    public Piece getAlliedPieceAt(Point point) {
        return board.getAlliedPieceAt(point);
    }

    public boolean isLightTile(Point point) {
        return board.isLightTile(point);
    }

    /**
     * The game may be over by checkmate or by draw. There are 4 types of draws:
     * <p> 1. Stalemate
     * <p> 2. 50 moves without pawn move or piece capture
     * <p> 3. Board repeated 3 times
     * <p> 4. Insufficient mating material
     */
    private GameStatus gameOverState(int repetitionCount) {
        if (isGameOverDueToCheckmate()) {
            return board.getAlliedKing().isWhite() ? GameStatus.BLACK_WINS : GameStatus.WHITE_WINS;
        }
        if (isGameOverDueToStalemate()) {
            return GameStatus.STALEMATE;
        }
        if (isTooManyMoves()) {
            return GameStatus.TOO_MANY_MOVES;
        }
        if (isTooManyBoardRepetitions(repetitionCount)) {
            return GameStatus.TOO_MANY_REPETITIONS;
        }
        if (isInsufficientMatingMaterial()) {
            return GameStatus.INSUFFICIENT_MATING;
        }
        if (board.getAlliedKing().isKingInCheck(board.locateAlliedKing())) {
            return GameStatus.IN_CHECK;
        }
        return GameStatus.ONGOING;
    }

    private boolean isGameOverDueToCheckmate() {
        var king = board.getAlliedKing();
        var kingLocation = board.locateAlliedKing();
        return king.isKingInCheck(kingLocation) && isMoveImpossible();
    }

    private boolean isGameOverDueToStalemate() {
        var king = board.getAlliedKing();
        var kingLocation = board.locateAlliedKing();
        return !king.isKingInCheck(kingLocation) && isMoveImpossible();
    }

    private boolean isMoveImpossible() {
        var king = board.getAlliedKing();
        for (int i = 0; i < Board.BOARD_LENGTH; i++) {
            for (int j = 0; j < Board.BOARD_WIDTH; j++) {
                var start = Point.instance(j, i);
                var piece = board.getBoard(start);
                if (piece != null && board.isAlly(piece)) {
                    for (int k = 0; k < Board.BOARD_LENGTH; k++) {
                        for (int l = 0; l < Board.BOARD_WIDTH; l++) {
                            var end = Point.instance(l, k);
                            var save = board.getBoard(end);
                            if (piece.isActionLegal(start, end)) {
                                rawMove(piece, start, end);
                                var kingLocation = board.locateAlliedKing();
                                boolean isNotInCheck = !king.isKingInCheck(kingLocation);
                                rawMove(piece, end, start);
                                board.setBoard(end, save);
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
        board.setBoard(start, null);
        board.setBoard(end, piece);
    }

    /**
     * Draw if 50 moves without pawn move or piece capture.
     */
    private boolean isTooManyMoves() {
        int maxMovePerSide = 50;
        int maxUnproductiveMoves = 2 * maxMovePerSide;
        return drawCounter >= maxUnproductiveMoves;
    }

    /**
     * Draw if board repeated 3 times.
     */
    private boolean isTooManyBoardRepetitions(int repetitionCount) {
        int maxRepetitions = 3;
        return repetitionCount >= maxRepetitions;
    }

    /**
     * Mating material is any pieces which could force a checkmate.
     */
    private boolean isInsufficientMatingMaterial() {
        var ally = new ArrayList<Piece>();
        var enemy = new ArrayList<Piece>();
        findPieces(ally, enemy);
        return isInsufficientMatingMaterial(ally, enemy);
    }

    private void findPieces(List<Piece> ally, List<Piece> enemy) {
        for (int i = 0; i < Board.BOARD_LENGTH; i++) {
            for (int j = 0; j < Board.BOARD_WIDTH; j++) {
                var point = Point.instance(j, i);
                var piece = board.getBoard(point);
                if (piece != null && !(piece instanceof King)) {
                    if (board.isAlly(piece)) {
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
        var size = group.size();
        return size == 0
                || (size == 1 && (group.get(0) instanceof Knight || group.get(0) instanceof Bishop))
                || (size == 2 && group.get(0) instanceof Knight && group.get(1) instanceof Knight);
    }

    public Move[][] availableMoves(Piece moving, Point from) {
        var moves = new Move[Board.BOARD_LENGTH][Board.BOARD_WIDTH];
        for (var slice : moves) {
            Arrays.fill(slice, Move.NONE);
        }
        for (int i = 0; i < Board.BOARD_LENGTH; i++) {
            for (int j = 0; j < Board.BOARD_WIDTH; j++) {
                var checkAt = Point.instance(j, i);
                if (moving instanceof King) {
                    var backup = board.getBoard(from);
                    board.setBoard(from, null);
                    if (moving.isActionLegal(from, checkAt)) {
                        moves[i][j] = Move.NORMAL;
                    }
                    board.setBoard(from, backup);
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
            moves[Board.BOARD_LENGTH - 1][0] = Move.QUEEN_SIDE_CASTLE;
        }
        if (canKingSideCastle(from)) {
            moves[Board.BOARD_LENGTH - 1][Board.BOARD_WIDTH - 1] = Move.KING_SIDE_CASTLE;
        }
        if (canPerformEnPassant(moving, from)) {
            moves[enPassant.y()][enPassant.x()] = Move.EN_PASSANT;
        }
        return moves;
    }

    private boolean canQueenSideCastle(Point from) {
        return canCastle(from, 0);
    }

    private boolean canKingSideCastle(Point from) {
        return canCastle(from, Board.BOARD_WIDTH - 1);
    }

    private boolean canCastle(Point from, int xCoordRook) {
        if (!from.equals(Point.instance(Board.KING_X_COORD, Board.BOARD_LENGTH - 1))) {
            return false;
        }
        var king = board.getAlliedKing();
        var rook = board.getBoard(Point.instance(xCoordRook, Board.BOARD_LENGTH - 1));
        if (hasPieceMoved(king) || hasPieceMoved(rook)) {
            return false;
        }
        int min = Math.min(xCoordRook, board.locateAlliedKing().x());
        int max = Math.max(xCoordRook, board.locateAlliedKing().x());
        for (int x = min + 1; x < max; x++) {
            if (board.getBoard(Point.instance(x, Board.BOARD_LENGTH - 1)) != null) {
                return false;
            }
        }
        int direction = Integer.signum(xCoordRook - board.locateAlliedKing().x());
        for (int i = 0; i <= 2; i++) {
            var tile = Point.instance(Board.KING_X_COORD + i * direction, Board.BOARD_LENGTH - 1);
            if (king.isKingInCheck(tile)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasPieceMoved(Piece piece) {
        return piece == null || piece.hasMoved();
    }

    private boolean canPerformEnPassant(Piece moving, Point from) {
        if (enPassant == null || !(moving instanceof Pawn)) {
            return false;
        }
        var squareAbovePiece = Point.instance(enPassant.x(), enPassant.y() + 1);
        var isPieceAnEnemy = moving.isWhite() != board.getBoard(squareAbovePiece).isWhite();
        var diffX = Math.abs(from.x() - enPassant.x());
        return isPieceAnEnemy && from.y() == enPassant.y() + 1 && diffX == 1;
    }
}
