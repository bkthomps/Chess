package chess;

import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of the chess board and the pieces which exist on it.
 */
final class GameState {
    private final Chess chess;
    private boolean isWhiteTurn;
    private Piece moving;
    private Point from;
    private boolean isInCheck;
    private Point enPassant;
    private int drawCounter;
    private final List<Piece[][]> boardHistory = new ArrayList<>();
    private final List<Point> enPassantHistory = new ArrayList<>();
    private final List<Boolean> canCastleHistory = new ArrayList<>();

    GameState(Chess chess, boolean isWhiteTurn) {
        this.chess = chess;
        this.isWhiteTurn = isWhiteTurn;
        final String text = Chess.resource.getString("startupInformation");
        final String[] options = {Chess.resource.getString("acknowledge")};
        displayDialogText(text, options);
    }

    void handleClick(int x, int y) {
        if (moving == null) {
            lockOntoPiece(new Point(x, y));
            return;
        }
        chess.refreshPixels();
        final Point to = new Point(x, y);
        if (isInCheck) {
            doMoveInCheckIfLegal(to);
        } else if (castleIfPossible(to)) {
            isWhiteTurn = !isWhiteTurn;
            chess.flipBoard();
            enPassant = null;
            enPassantHistory.add(null);
        } else if (canPerformEnPassant(to)) {
            doEnPassant();
        } else if (moving.isActionLegal(from, to)) {
            recordEnPassantHistory(to);
            doMove(to);
        }
        moving = null;
        from = null;
    }

    static Point locateAlliedKing(boolean isWhiteTurn) {
        Point king = null;
        for (int i = 0; i < Chess.BOARD_SIZE; i++) {
            for (int j = 0; j < Chess.BOARD_SIZE; j++) {
                final Point point = new Point(j, i);
                final Piece item = Chess.getBoard(point);
                if (item instanceof King && item.isWhite() == isWhiteTurn) {
                    if (king != null) {
                        throw new IllegalStateException("Multiple Kings!");
                    }
                    king = point;
                }
            }
        }
        if (king != null) {
            return king;
        }
        throw new IllegalStateException("No King!");
    }

    private void lockOntoPiece(Point point) {
        final Piece toMove = Chess.getBoard(point);
        if (toMove != null && toMove.isWhite() == isWhiteTurn) {
            moving = toMove;
            from = point;
            highlightLegalMoves();
            return;
        }
        moving = null;
        from = null;
    }

    private void highlightLegalMoves() {
        final Color darkGreen = new Color(0, 100, 40);
        final Color lightGreen = new Color(0, 140, 50);
        for (int i = 0; i < Chess.BOARD_SIZE; i++) {
            for (int j = 0; j < Chess.BOARD_SIZE; j++) {
                final Point checkAt = new Point(j, i);
                final Color usedColor = ((i + j) % 2 == 0 ^ !isWhiteTurn) ? lightGreen : darkGreen;
                if (moving instanceof King) {
                    final Piece backup = Chess.getBoard(from);
                    Chess.setBoard(from, null);
                    if (moving.isActionLegal(from, checkAt)) {
                        chess.drawTileBackgroundGUI(usedColor, j, i);
                    }
                    Chess.setBoard(from, backup);
                } else if (moving.isActionLegal(from, checkAt)) {
                    chess.drawTileBackgroundGUI(usedColor, j, i);
                }
            }
        }
        if (canQueenSideCastle()) {
            chess.drawTileBackgroundGUI(isWhiteTurn ? darkGreen : lightGreen, 0, Chess.BOARD_SIZE - 1);
        }
        if (canKingSideCastle()) {
            chess.drawTileBackgroundGUI(isWhiteTurn ? lightGreen : darkGreen, Chess.BOARD_SIZE - 1, Chess.BOARD_SIZE - 1);
        }
        if (enPassant != null) {
            final boolean canCaptureEnPassant = from.y == enPassant.y + 1
                    && Math.abs(from.x - enPassant.x) == 1;
            if (canCaptureEnPassant && canPerformEnPassant(enPassant)) {
                final Color usedColor = ((enPassant.y + enPassant.x) % 2 == 0 ^ !isWhiteTurn) ? lightGreen : darkGreen;
                chess.drawTileBackgroundGUI(usedColor, enPassant.x, enPassant.y);
            }
        }
        chess.drawAllPiecesGUI();
    }

    private void doMoveInCheckIfLegal(Point to) {
        if (moving.isActionLegal(from, to)) {
            recordEnPassantHistory(to);
            movePiece(moving, from, to);
            final Point location = locateAlliedKing(isWhiteTurn);
            final King king = new King(isWhiteTurn);
            if (!king.isKingInCheck(location)) {
                isWhiteTurn = !isWhiteTurn;
                chess.flipBoard();
                checkIfGameOver();
                isInCheck = false;
            } else {
                movePiece(moving, to, from);
            }
        }
    }

    /**
     * Moves the piece. If it is a pawn moving into a promotion square, ask the
     * user what to promote the pawn to and promote the pawn based on user input.
     *
     * @throws IllegalStateException if somehow the user ignored piece promotion
     */
    private void doMove(Point to) {
        if (to.y == 0 && moving instanceof Pawn) {
            final String text = Chess.resource.getString("pawnPromotionOption");
            final String[] options = {
                    Chess.resource.getString("queen"),
                    Chess.resource.getString("knight"),
                    Chess.resource.getString("rook"),
                    Chess.resource.getString("bishop"),
            };
            int promotion = -1;
            while (promotion < 0) {
                promotion = displayDialogText(text, options);
            }
            final Piece piece;
            switch (promotion) {
                case 0:
                    piece = new Queen(moving.isWhite());
                    break;
                case 1:
                    piece = new Knight(moving.isWhite());
                    break;
                case 2:
                    piece = new Rook(moving.isWhite());
                    break;
                case 3:
                    piece = new Bishop(moving.isWhite());
                    break;
                default:
                    throw new IllegalStateException("Pawn promotion is mandatory.");
            }
            movePiece(piece, from, to);
        } else {
            movePiece(moving, from, to);
        }
        isWhiteTurn = !isWhiteTurn;
        chess.flipBoard();
        checkIfGameOver();
    }

    private boolean canPerformEnPassant(Point to) {
        final Point squareAboveEnemy = new Point(to.x, to.y + 1);
        return to.equals(enPassant) && moving instanceof Pawn
                && moving.isWhite() != Chess.getBoard(squareAboveEnemy).isWhite();
    }

    /**
     * En passant is a move which lets a pawn capture a pawn which just moved
     * two squares as if it only moved one square immediately after it happens.
     */
    private void doEnPassant() {
        final Point squareAboveEnemy = new Point(enPassant.x, enPassant.y + 1);
        movePiece(moving, from, enPassant);
        Chess.setBoard(squareAboveEnemy, null);
        enPassant = null;
        enPassantHistory.add(null);
        isWhiteTurn = !isWhiteTurn;
        chess.flipBoard();
        checkIfGameOver();
    }

    private void recordEnPassantHistory(Point to) {
        if (moving instanceof Pawn && from.y - to.y == 2) {
            enPassant = new Point(to.x, to.y - 2);
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
        final Point location = locateAlliedKing(isWhiteTurn);
        final King king = new King(isWhiteTurn);
        if (isGameOverDueToCheckmate(king, location)) {
            final String text = Chess.resource.getString(isWhiteTurn ? "blackWins" : "whiteWins");
            endGameWithNotification(text);
        } else if (isGameOverDueToStalemate(king, location)) {
            final String text = Chess.resource.getString("stalemate");
            endGameWithNotification(text);
        }
        endGameIfNonStalemateDraw();
        warnIfKingInCheck(king, location);
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
        if (!(Chess.getBoard(point) instanceof King)) {
            throw new IllegalStateException("King not where specified!");
        }
        for (int i = 0; i < Chess.BOARD_SIZE; i++) {
            for (int j = 0; j < Chess.BOARD_SIZE; j++) {
                final Point start = new Point(j, i);
                final Piece me = Chess.getBoard(start);
                if (me != null && me.isWhite() == isWhiteTurn) {
                    for (int k = 0; k < Chess.BOARD_SIZE; k++) {
                        for (int l = 0; l < Chess.BOARD_SIZE; l++) {
                            final Point end = new Point(l, k);
                            final Piece save = Chess.getBoard(end);
                            if (me.isActionLegal(start, end)) {
                                rawMove(me, start, end);
                                final Point kingLocation = locateAlliedKing(isWhiteTurn);
                                final boolean isNotInCheck = !king.isKingInCheck(kingLocation);
                                rawMove(me, end, start);
                                Chess.setBoard(end, save);
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
        final int MAX_MOVE_PER_SIDE = 50;
        final int MAX_UNPRODUCTIVE_MOVES = 2 * MAX_MOVE_PER_SIDE;
        if (drawCounter == MAX_UNPRODUCTIVE_MOVES) {
            final String text = Chess.resource.getString("draw") + ' '
                    + MAX_MOVE_PER_SIDE + ' ' + Chess.resource.getString("noCapture");
            endGameWithNotification(text);
        } else if (drawCounter > MAX_UNPRODUCTIVE_MOVES) {
            throw new IllegalStateException("drawCounter > " + MAX_UNPRODUCTIVE_MOVES);
        }
    }

    /**
     * Draw if board repeated 3 times.
     */
    private void endGameIfTooManyBoardRepetitions() {
        if (isTooManyBoardRepetitions()) {
            final String text = Chess.resource.getString("boardRepeat");
            endGameWithNotification(text);
        }
    }

    /**
     * @return true if the board has repeated 3 times
     * @throws IllegalStateException if castle or en passant history size is invalid
     */
    private boolean isTooManyBoardRepetitions() {
        final int historySize = boardHistory.size();
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
                if (areBoardEqual(boardHistory.get(i), boardHistory.get(j))
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

    private boolean areBoardEqual(Piece[][] boardOne, Piece[][] boardTwo) {
        for (int i = 0; i < Chess.BOARD_SIZE; i++) {
            for (int j = 0; j < Chess.BOARD_SIZE; j++) {
                if (boardOne[i][j] != boardTwo[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Mating material is any pieces which could force a checkmate.
     */
    private void endGameIfInsufficientMatingMaterial() {
        final List<Piece> ally = new ArrayList<>();
        final List<Piece> enemy = new ArrayList<>();
        findPieces(ally, enemy);
        if (isInsufficientMatingMaterial(ally, enemy)) {
            final String text = Chess.resource.getString("insufficientPieces");
            endGameWithNotification(text);
        }
    }

    private void findPieces(List<Piece> ally, List<Piece> enemy) {
        for (int i = 0; i < Chess.BOARD_SIZE; i++) {
            for (int j = 0; j < Chess.BOARD_SIZE; j++) {
                final Point point = new Point(j, i);
                final Piece me = Chess.getBoard(point);
                if (me != null && !(me instanceof King)) {
                    if (me.isWhite() == isWhiteTurn) {
                        ally.add(me);
                    } else {
                        enemy.add(me);
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
        final List<Piece> group = (ally.size() == 0) ? enemy : ally;
        return group.size() == 0
                || (group.size() == 1 && (group.get(0) instanceof Knight || group.get(0) instanceof Bishop))
                || (group.size() == 2 && group.get(0) instanceof Knight && group.get(1) instanceof Knight);
    }

    private void endGameWithNotification(String text) {
        final String[] options = {Chess.resource.getString("acknowledge")};
        displayDialogText(text, options);
        System.exit(0);
    }

    private void warnIfKingInCheck(King king, Point location) {
        if (king.isKingInCheck(location)) {
            isInCheck = true;
            final String text = Chess.resource.getString("inCheck");
            final String[] options = {Chess.resource.getString("acknowledge")};
            displayDialogText(text, options);
        }
    }

    private boolean castleIfPossible(Point to) {
        if (to.y != Chess.BOARD_SIZE - 1) {
            return false;
        }
        if (to.x == 0 && canQueenSideCastle()) {
            performCastling(new Point(4, 7), new Point(2, 7));
            performCastling(new Point(0, 7), new Point(3, 7));
            return true;
        } else if (to.x == 7 && canKingSideCastle()) {
            performCastling(new Point(4, 7), new Point(6, 7));
            performCastling(new Point(7, 7), new Point(5, 7));
            return true;
        }
        return false;
    }

    private boolean canQueenSideCastle() {
        final King king = new King(isWhiteTurn);
        final boolean isLockedOnKing = from.x == 4 && from.y == 7;
        final boolean isClearPath = hasPieceMoved(Chess.getBoard(new Point(0, 7)))
                && Chess.getBoard(new Point(1, 7)) == null && Chess.getBoard(new Point(2, 7)) == null
                && Chess.getBoard(new Point(3, 7)) == null && hasPieceMoved(Chess.getBoard(new Point(4, 7)));
        final boolean isNotPassingThroughCheck = !king.isKingInCheck(new Point(4, 7))
                && !king.isKingInCheck(new Point(3, 7)) && !king.isKingInCheck(new Point(2, 7));
        return isLockedOnKing && isClearPath && isNotPassingThroughCheck;
    }

    private boolean canKingSideCastle() {
        final King king = new King(isWhiteTurn);
        final boolean isLockedOnKing = from.x == 4 && from.y == 7;
        final boolean isClearPath = hasPieceMoved(Chess.getBoard(new Point(4, 7))) && Chess.getBoard(new Point(5, 7)) == null
                && Chess.getBoard(new Point(6, 7)) == null && hasPieceMoved(Chess.getBoard(new Point(7, 7)));
        final boolean isNotPassingThroughCheck = !king.isKingInCheck(new Point(4, 7))
                && !king.isKingInCheck(new Point(5, 7)) && !king.isKingInCheck(new Point(6, 7));
        return isLockedOnKing && isClearPath && isNotPassingThroughCheck;
    }

    private boolean hasPieceMoved(Piece me) {
        return me != null && !me.hasMoved();
    }

    private void performCastling(Point from, Point to) {
        Chess.setBoard(to, Chess.getBoard(from));
        Chess.setBoard(from, null);
        Chess.getBoard(to).setMove();
    }

    private void movePiece(Piece me, Point start, Point end) {
        if (Chess.getBoard(end) != null || me instanceof Pawn) {
            drawCounter = 0;
            boardHistory.clear();
            enPassantHistory.clear();
            canCastleHistory.clear();
        } else {
            drawCounter++;
            final Piece[][] boardCopy = new Piece[Chess.BOARD_SIZE][Chess.BOARD_SIZE];
            for (int i = 0; i < Chess.BOARD_SIZE; i++) {
                for (int j = 0; j < Chess.BOARD_SIZE; j++) {
                    final Point point = new Point(j, i);
                    boardCopy[i][j] = Chess.getBoard(point);
                }
            }
            boardHistory.add(boardCopy);
            final Point kingLocation = locateAlliedKing(isWhiteTurn);
            canCastleHistory.add(Chess.getBoard(kingLocation).hasMoved());
        }
        rawMove(me, start, end);
        me.setMove();
    }

    /**
     * Moves the piece without setting the piece to moved state. Used
     * when checking the board and not actually moving pieces on it.
     */
    private void rawMove(Piece me, Point start, Point end) {
        Chess.setBoard(start, null);
        Chess.setBoard(end, me);
    }

    private int displayDialogText(String text, String[] options) {
        return JOptionPane.showOptionDialog(null, text, Chess.GAME_TITLE, JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    }
}
