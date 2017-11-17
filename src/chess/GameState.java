package chess;

import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of the state of the chess board and the pieces on it.
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
        final String text = "Click on a piece to move, and\nthen a location to move it to.";
        final String[] options = {"OK"};
        customText(text, options);
    }

    /**
     * Handles a click event.
     *
     * @param x the x-coordinate of the click
     * @param y the y-coordinate of the click
     */
    void clicked(int x, int y) {
        if (moving == null) {
            lockOntoPiece(new Point(x, y));
            return;
        }
        chess.refreshPixels();
        final Point to = new Point(x, y);
        if (isInCheck) {
            doMoveInCheck(to);
        } else if (castleIfPossible(to)) {
            isWhiteTurn = !isWhiteTurn;
            chess.flipBoard();
            enPassant = null;
            enPassantHistory.add(null);
        } else if (isEnPassantLegal(to)) {
            doEnPassant();
        } else if (moving.isActionLegal(from, to)) {
            checkEnPassant(to);
            doMove(to);
        }
        moving = null;
        from = null;
    }

    /**
     * Determines the location of the allied king.
     *
     * @param isWhiteTurn the color of the allied king
     * @return location of the allied king
     * @throws IllegalStateException if there is no allied king or more than one allied king
     */
    static Point locateKing(boolean isWhiteTurn) {
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

    /**
     * Determines the piece which was clicked to operate on.
     *
     * @param point the location to lock on to
     */
    private void lockOntoPiece(Point point) {
        final Piece toMove = Chess.getBoard(point);
        if (toMove != null && toMove.isWhite() == isWhiteTurn) {
            moving = toMove;
            from = point;
            letUserKnowLegalMoves();
            return;
        }
        moving = null;
        from = null;
    }

    /**
     * Creates marks on the board letting the user know where the piece can be moved.
     */
    private void letUserKnowLegalMoves() {
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
                        chess.fillInSubSection(usedColor, j, i);
                    }
                    Chess.setBoard(from, backup);
                } else if (moving.isActionLegal(from, checkAt)) {
                    chess.fillInSubSection(usedColor, j, i);
                }
            }
        }
        if (canQueenSideCastle()) {
            chess.fillInSubSection(isWhiteTurn ? darkGreen : lightGreen, 0, Chess.BOARD_SIZE - 1);
        }
        if (canKingSideCastle()) {
            chess.fillInSubSection(isWhiteTurn ? lightGreen : darkGreen, Chess.BOARD_SIZE - 1, Chess.BOARD_SIZE - 1);
        }
        if (enPassant != null) {
            final boolean canCaptureEnPassant = from.y == enPassant.y + 1
                    && Math.abs(from.x - enPassant.x) == 1;
            if (canCaptureEnPassant && isEnPassantLegal(enPassant)) {
                final Color usedColor = ((enPassant.y + enPassant.x) % 2 == 0 ^ !isWhiteTurn) ? lightGreen : darkGreen;
                chess.fillInSubSection(usedColor, enPassant.x, enPassant.y);
            }
        }
        chess.setPieces();
    }

    /**
     * Determines if the move to make when the king is in check is legal.
     *
     * @param to where to move the king to
     */
    private void doMoveInCheck(Point to) {
        if (moving.isActionLegal(from, to)) {
            checkEnPassant(to);
            move(moving, from, to);
            final Point location = locateKing(isWhiteTurn);
            final King king = new King(isWhiteTurn);
            if (!king.isCheck(location)) {
                isWhiteTurn = !isWhiteTurn;
                chess.flipBoard();
                checkEndgame();
                isInCheck = false;
            } else {
                move(moving, to, from);
            }
        }
    }

    /**
     * Moves the piece. If it is a pawn moving into a promotion square, ask the user what to promote the pawn to and
     * promote the pawn based on user input.
     *
     * @param to where to move to
     */
    private void doMove(Point to) {
        if (to.y == 0 && moving instanceof Pawn) {
            final String text = "What would you like to promote your pawn to?";
            final String[] options = {"QUEEN", "KNIGHT", "ROOK", "BISHOP"};
            final int promotion = customText(text, options);
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
                    piece = moving;
                    break;
            }
            move(piece, from, to);
        } else {
            move(moving, from, to);
        }
        isWhiteTurn = !isWhiteTurn;
        chess.flipBoard();
        checkEndgame();
    }

    /**
     * Determines if en passant may be used.
     *
     * @param to where to move the piece to
     * @return true if en passant is legal
     */
    private boolean isEnPassantLegal(Point to) {
        final Point squareAboveEnemy = new Point(to.x, to.y + 1);
        return to.equals(enPassant) && moving instanceof Pawn
                && moving.isWhite() != Chess.getBoard(squareAboveEnemy).isWhite();
    }

    /**
     * Performs en passant. En passant is a move which lets a pawn capture a pawn which just moved two squares as if it
     * only moved one square immediately after it happens.
     */
    private void doEnPassant() {
        final Point squareAboveEnemy = new Point(enPassant.x, enPassant.y + 1);
        move(moving, from, enPassant);
        Chess.setBoard(squareAboveEnemy, null);
        enPassant = null;
        enPassantHistory.add(null);
        isWhiteTurn = !isWhiteTurn;
        chess.flipBoard();
        checkEndgame();
    }

    /**
     * Keeps a record that en passant may be used at this location.
     *
     * @param to location to move to
     */
    private void checkEnPassant(Point to) {
        if (moving instanceof Pawn && from.y - to.y == 2) {
            enPassant = new Point(to.x, to.y - 2);
        } else {
            enPassant = null;
        }
        enPassantHistory.add(enPassant);
    }

    /**
     * Checks if the game is over. It may be over by checkmate or by draw.
     * <p> There are 4 types of draws:
     * <p> 1. Stalemate
     * <p> 2. 50 moves without pawn move or piece capture
     * <p> 3. Board repeated 3 times
     * <p> 4. Insufficient mating material
     */
    private void checkEndgame() {
        final Point location = locateKing(isWhiteTurn);
        final King king = new King(isWhiteTurn);
        if (isCheckmate(king, location)) {
            final String team = isWhiteTurn ? "Black" : "White";
            final String text = "Checkmate! " + team + " wins!";
            finishGame(text);
        } else if (isStalemate(king, location)) {
            final String text = "Draw! Because of stalemate!";
            finishGame(text);
        }
        otherDraw();
        warnIfCheck(king, location);
    }

    /**
     * Determines if the game is over by checkmate.
     *
     * @param king  the king
     * @param point the location of the king
     * @return true if the game is over by checkmate
     */
    private boolean isCheckmate(King king, Point point) {
        return king.isCheck(point) && isMoveImpossible(king, point);
    }

    /**
     * Determines if the game is over by stalemate.
     *
     * @param king  the king
     * @param point the location of the king
     * @return true if the game is over by stalemate
     */
    private boolean isStalemate(King king, Point point) {
        return !king.isCheck(point) && isMoveImpossible(king, point);
    }

    /**
     * Determines if any move can be done which results in king not being in check.
     *
     * @param king  the king
     * @param point the location of the king
     * @return true if any move can be done which results in king not being in check
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
                                final Point kingLocation = locateKing(isWhiteTurn);
                                final boolean isNotInCheck = !king.isCheck(kingLocation);
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

    /**
     * Determines if there is a non-stalemate draw.
     */
    private void otherDraw() {
        determineIfTooManyMoves();
        determineIfTooManyBoardRepetitions();
        determineIfInsufficientMatingMaterial();
    }

    /**
     * Draw if 50 moves without pawn move or piece capture.
     *
     * @throws IllegalStateException if somehow went over the draw counter
     */
    private void determineIfTooManyMoves() {
        final int MAX_MOVE_PER_SIDE = 50;
        final int MAX_UNPRODUCTIVE_MOVES = 2 * MAX_MOVE_PER_SIDE;
        if (drawCounter == MAX_UNPRODUCTIVE_MOVES) {
            final String text = "Draw! " + MAX_MOVE_PER_SIDE + " moves without pawn move or piece capture!";
            finishGame(text);
        } else if (drawCounter > MAX_UNPRODUCTIVE_MOVES) {
            throw new IllegalStateException("drawCounter > " + MAX_UNPRODUCTIVE_MOVES);
        }
    }

    /**
     * Draw if board repeated 3 times.
     */
    private void determineIfTooManyBoardRepetitions() {
        if (isTooManyBoardRepetitions()) {
            final String text = "Draw! Board repeated 3 times!";
            finishGame(text);
        }
    }

    /**
     * Determines if the board has repeated 3 times.
     *
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
                if (isSameBoard(boardHistory.get(i), boardHistory.get(j))
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
     * Determines if two specified boards are the same.
     *
     * @param boardOne the first board
     * @param boardTwo the second board
     * @return true if two boards are the same
     */
    private boolean isSameBoard(Piece[][] boardOne, Piece[][] boardTwo) {
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
     * Draw if insufficient mating material. Mating material is any pieces which could force a checkmate.
     */
    private void determineIfInsufficientMatingMaterial() {
        final List<Piece> ally = new ArrayList<>();
        final List<Piece> enemy = new ArrayList<>();
        findPieces(ally, enemy);
        if (isInsufficientMating(ally, enemy)) {
            final String text = "Draw! Not enough pieces to cause a checkmate!";
            finishGame(text);
        }
    }

    /**
     * Finds pieces on the board.
     *
     * @param ally  the ally pieces
     * @param enemy the enemy pieces
     */
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
     * Determines if insufficient mating material.
     *
     * @param ally  pieces allied with the King not including the King
     * @param enemy pieces enemy to the King not including the King
     * @return is lone King against: lone King, or King and Knight, or King and Bishop, or King and two Knights
     */
    private boolean isInsufficientMating(List<Piece> ally, List<Piece> enemy) {
        if (ally.size() != 0 && enemy.size() != 0) {
            return false;
        }
        final List<Piece> group = (ally.size() == 0) ? enemy : ally;
        return group.size() == 0
                || (group.size() == 1 && (group.get(0) instanceof Knight || group.get(0) instanceof Bishop))
                || (group.size() == 2 && group.get(0) instanceof Knight && group.get(1) instanceof Knight);
    }

    /**
     * Gives a user a message and terminates the program.
     *
     * @param text the text to display to the user
     */
    private void finishGame(String text) {
        final String[] options = {"OK"};
        customText(text, options);
        System.exit(0);
    }

    /**
     * Warns the user if the king is in check.
     *
     * @param king     the king which is in check
     * @param location the location of the king in check
     */
    private void warnIfCheck(King king, Point location) {
        if (king.isCheck(location)) {
            isInCheck = true;
            final String text = "Warning: you are in check.\nYou must get out of check.";
            final String[] options = {"OK"};
            customText(text, options);
        }
    }

    /**
     * Determines if the king can castle, and if so, castle.
     *
     * @param to the location to move to
     * @return true if the king can castle
     */
    private boolean castleIfPossible(Point to) {
        if (to.y != Chess.BOARD_SIZE - 1) {
            return false;
        }
        if (to.x == 0 && canQueenSideCastle()) {
            moveCastle(new Point(4, 7), new Point(2, 7));
            moveCastle(new Point(0, 7), new Point(3, 7));
            return true;
        } else if (to.x == 7 && canKingSideCastle()) {
            moveCastle(new Point(4, 7), new Point(6, 7));
            moveCastle(new Point(7, 7), new Point(5, 7));
            return true;
        }
        return false;
    }

    /**
     * Determines if can queen side castle.
     *
     * @return true if queen side castle is legal
     */
    private boolean canQueenSideCastle() {
        final King king = new King(isWhiteTurn);
        final boolean isLockedOnKing = from.x == 4 && from.y == 7;
        final boolean isClearPath = hasMoved(Chess.getBoard(new Point(0, 7)))
                && Chess.getBoard(new Point(1, 7)) == null && Chess.getBoard(new Point(2, 7)) == null
                && Chess.getBoard(new Point(3, 7)) == null && hasMoved(Chess.getBoard(new Point(4, 7)));
        final boolean isNotPassingThroughCheck = !king.isCheck(new Point(4, 7))
                && !king.isCheck(new Point(3, 7)) && !king.isCheck(new Point(2, 7));
        return isLockedOnKing && isClearPath && isNotPassingThroughCheck;
    }

    /**
     * Determines if can king side castle.
     *
     * @return true if king side castle is legal
     */
    private boolean canKingSideCastle() {
        final King king = new King(isWhiteTurn);
        final boolean isLockedOnKing = from.x == 4 && from.y == 7;
        final boolean isClearPath = hasMoved(Chess.getBoard(new Point(4, 7))) && Chess.getBoard(new Point(5, 7)) == null
                && Chess.getBoard(new Point(6, 7)) == null && hasMoved(Chess.getBoard(new Point(7, 7)));
        final boolean isNotPassingThroughCheck = !king.isCheck(new Point(4, 7))
                && !king.isCheck(new Point(5, 7)) && !king.isCheck(new Point(6, 7));
        return isLockedOnKing && isClearPath && isNotPassingThroughCheck;
    }

    /**
     * Determines if the piece has moved.
     *
     * @param me the piece
     * @return true if the piece has moved
     */
    private boolean hasMoved(Piece me) {
        return me != null && !me.hasMoved();
    }

    /**
     * Moves the piece when castling.
     *
     * @param from location to move from
     * @param to   location to move to
     */
    private void moveCastle(Point from, Point to) {
        Chess.setBoard(to, Chess.getBoard(from));
        Chess.setBoard(from, null);
        Chess.getBoard(to).setMove();
    }

    /**
     * Moves the piece.
     *
     * @param me    the piece to move
     * @param start the location to move from
     * @param end   the location to move to
     */
    private void move(Piece me, Point start, Point end) {
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
            final Point kingLocation = locateKing(isWhiteTurn);
            canCastleHistory.add(Chess.getBoard(kingLocation).hasMoved());
        }
        rawMove(me, start, end);
        me.setMove();
    }

    /**
     * Moves the piece without setting the piece to moved state. Used when checking the board and not actually moving
     * pieces on it.
     *
     * @param me    the piece to move
     * @param start the location to move from
     * @param end   the location to move to
     */
    private void rawMove(Piece me, Point start, Point end) {
        Chess.setBoard(start, null);
        Chess.setBoard(end, me);
    }

    /**
     * Displays text to the user using a dialog box.
     *
     * @param text    what to display
     * @param options the options the user can click
     * @return the option the user picked
     */
    private int customText(String text, String[] options) {
        return JOptionPane.showOptionDialog(null, text, Chess.GAME_TITLE, JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    }
}
