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
        performCastling(Point.instance(4, 7), Point.instance(2, 7));
        performCastling(Point.instance(0, 7), Point.instance(3, 7));
        finishCastling(Point.instance(2, 7));
    }

    public void kingSideCastle() {
        performCastling(Point.instance(4, 7), Point.instance(6, 7));
        performCastling(Point.instance(7, 7), Point.instance(5, 7));
        finishCastling(Point.instance(6, 7));
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
            enPassant = Point.instance(end.x(), end.y() - 2);
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
        if (isGameOverDueToCheckmate(board.getAlliedKing(), board.locateAlliedKing())) {
            return board.getAlliedKing().isWhite() ? GameStatus.BLACK_WINS : GameStatus.WHITE_WINS;
        }
        if (isGameOverDueToStalemate(board.getAlliedKing(), board.locateAlliedKing())) {
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

    private boolean isGameOverDueToCheckmate(King king, Point point) {
        return king.isKingInCheck(point) && isMoveImpossible(king, point);
    }

    private boolean isGameOverDueToStalemate(King king, Point point) {
        return !king.isKingInCheck(point) && isMoveImpossible(king, point);
    }

    /**
     * @throws IllegalStateException if king is not at the specified area
     */
    private boolean isMoveImpossible(King king, Point point) {
        if (!(board.getBoard(point) instanceof King)) {
            throw new IllegalStateException("King not where specified!");
        }
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
        var king = board.getAlliedKing();
        boolean isLockedOnKing = from.x() == 4 && from.y() == 7;
        boolean isClearPath = hasPieceMoved(board.getBoard(Point.instance(0, 7)))
                && board.getBoard(Point.instance(1, 7)) == null
                && board.getBoard(Point.instance(2, 7)) == null
                && board.getBoard(Point.instance(3, 7)) == null
                && hasPieceMoved(board.getBoard(Point.instance(4, 7)));
        boolean isNotPassingThroughCheck = !king.isKingInCheck(Point.instance(4, 7))
                && !king.isKingInCheck(Point.instance(3, 7))
                && !king.isKingInCheck(Point.instance(2, 7));
        return isLockedOnKing && isClearPath && isNotPassingThroughCheck;
    }

    private boolean canKingSideCastle(Point from) {
        var king = board.getAlliedKing();
        boolean isLockedOnKing = from.x() == 4 && from.y() == 7;
        boolean isClearPath = hasPieceMoved(board.getBoard(Point.instance(4, 7)))
                && board.getBoard(Point.instance(5, 7)) == null
                && board.getBoard(Point.instance(6, 7)) == null
                && hasPieceMoved(board.getBoard(Point.instance(7, 7)));
        boolean isNotPassingThroughCheck = !king.isKingInCheck(Point.instance(4, 7))
                && !king.isKingInCheck(Point.instance(5, 7))
                && !king.isKingInCheck(Point.instance(6, 7));
        return isLockedOnKing && isClearPath && isNotPassingThroughCheck;
    }

    private boolean hasPieceMoved(Piece piece) {
        return piece != null && !piece.hasMoved();
    }

    private boolean canPerformEnPassant(Piece moving, Point to) {
        var squareAboveEnemy = Point.instance(to.x(), to.y() + 1);
        return moving instanceof Pawn
                && moving.isWhite() != board.getBoard(squareAboveEnemy).isWhite();
    }
}
