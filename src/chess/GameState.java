package chess;

import javax.swing.JOptionPane;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of the state of the chess board and the pieces on it.
 */
final class GameState {

    private final Chess chess;
    private boolean isWhiteTurn = true;
    private Piece moving;
    private Point from;
    private boolean isInCheck;
    private Point enPassant;
    private int drawCounter;
    private final List<Piece[][]> boardHistory = new ArrayList<>();
    private final List<Point> enPassantHistory = new ArrayList<>();
    private final List<Boolean> canCastleHistory = new ArrayList<>();

    GameState(Chess chess) {
        this.chess = chess;
    }

    /**
     * Handles a click event.
     *
     * @param x the x-coordinate of the click
     * @param y the y-coordinate of the click
     */
    void clicked(int x, int y) {
        if (moving == null) {
            lockOntoPiece(x, y);
            return;
        }
        final Point to = new Point(x, y);
        if (isInCheck) {
            doMoveInCheck(to);
        } else if (canCastle(to)) {
            isWhiteTurn = !isWhiteTurn;
            flipBoard();
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
     * Determine location of king.
     *
     * @return location of king
     * @throws IllegalStateException if there is no allied king or more than one allied king
     */
    static Point locateKing(boolean isWhiteTurn) {
        Point king = null;
        for (int i = 0; i < Chess.board.length; i++) {
            for (int j = 0; j < Chess.board.length; j++) {
                final Piece item = Chess.board[i][j];
                if (item != null && item.getClass() == King.class && item.isWhite() == isWhiteTurn) {
                    if (king != null) {
                        throw new IllegalStateException("Multiple Kings!");
                    }
                    king = new Point(j, i);
                }
            }
        }
        if (king != null) {
            return king;
        }
        throw new IllegalStateException("No King!");
    }

    /**
     * Determine the piece which was clicked to operate on.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    private void lockOntoPiece(int x, int y) {
        final Piece toMove = Chess.board[y][x];
        if (toMove != null && toMove.isWhite() == isWhiteTurn) {
            moving = toMove;
            from = new Point(x, y);
            return;
        }
        moving = null;
        from = null;
    }

    /**
     * Determine if the move is legal to make when the king is in check.
     *
     * @param to where to move the king to
     */
    private void doMoveInCheck(Point to) {
        if (moving.isActionLegal(from, to)) {
            checkEnPassant(to);
            move(from, to, moving);
            final Point location = locateKing(isWhiteTurn);
            final King king = new King(isWhiteTurn);
            if (!king.isCheck((int) location.getX(), (int) location.getY())) {
                isWhiteTurn = !isWhiteTurn;
                flipBoard();
                checkEndgame();
                isInCheck = false;
            } else {
                move(to, from, moving);
            }
        }
    }

    /**
     * Move the piece. If it is a pawn moving into a promotion square, ask the user what to promote the pawn to and
     * promote the pawn based on user input.
     *
     * @param to where to move to
     */
    private void doMove(Point to) {
        if ((int) to.getY() == 0 && moving.getClass() == Pawn.class) {
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
            move(from, to, piece);
        } else {
            move(from, to, moving);
        }
        isWhiteTurn = !isWhiteTurn;
        flipBoard();
        checkEndgame();
    }

    /**
     * Determine if en passant may be used.
     *
     * @param to where to move the piece to
     * @return if en passant is legal
     */
    private boolean isEnPassantLegal(Point to) {
        return enPassant != null && to.equals(enPassant) && moving.getClass() == Pawn.class
                && moving.isWhite() != Chess.board[(int) to.getY() + 1][(int) to.getX()].isWhite();
    }

    /**
     * Perform en passant. En passant is a move which lets a pawn eat a pawn which just moved two squares as if it only
     * moved one square immediately after it happens.
     */
    private void doEnPassant() {
        move(from, enPassant, moving);
        Chess.board[(int) enPassant.getY() + 1][(int) enPassant.getX()] = null;
        enPassant = null;
        enPassantHistory.add(null);
        isWhiteTurn = !isWhiteTurn;
        flipBoard();
        checkEndgame();
    }

    /**
     * Keep a record that en passant may be used at this location.
     *
     * @param to location to move to
     */
    private void checkEnPassant(Point to) {
        if (moving.getClass() == Pawn.class && from.getY() - to.getY() == 2) {
            final Point p = new Point((int) to.getX(), (int) to.getY() - 2);
            enPassant = p;
            enPassantHistory.add(p);
        } else {
            enPassant = null;
            enPassantHistory.add(null);
        }
    }

    /**
     * Check if game is over. It may be over by checkmate or by draw. The are 4 types of draws.
     */
    private void checkEndgame() {
        final Point location = locateKing(isWhiteTurn);
        final int x = (int) location.getX(), y = (int) location.getY();
        final King king = new King(isWhiteTurn);
        if (isCheckmate(king, x, y)) {
            final String team = (isWhiteTurn) ? "Black" : "White";
            final String text = "Checkmate! " + team + " wins!";
            finishGame(text);
        } else if (isStalemate(king, x, y)) {
            final String text = "Draw! Because of stalemate!";
            finishGame(text);
        }
        otherDraw();
        warnIfCheck(king, location);
    }

    /**
     * Determine if the game is over by checkmate.
     *
     * @param king the king
     * @param x    the x-coordinate
     * @param y    the y-coordinate
     * @return if the game is over by checkmate
     */
    private boolean isCheckmate(King king, int x, int y) {
        return king.isCheck(x, y) && isMoveImpossible(king, x, y);
    }

    /**
     * Determine if the game is over by stalemate.
     *
     * @param king the king
     * @param x    the x-coordinate
     * @param y    the y-coordinate
     * @return if the game is over by stalemate
     */
    private boolean isStalemate(King king, int x, int y) {
        return !king.isCheck(x, y) && isMoveImpossible(king, x, y);
    }

    /**
     * Determine if any move can be done which results in king not being in check.
     *
     * @param king the king
     * @param x    the x-coordinate
     * @param y    the y-coordinate
     * @return if any move can be done which results in king not being in check
     * @throws IllegalStateException if king is not at the specified area
     */
    private boolean isMoveImpossible(King king, int x, int y) {
        if (Chess.board[y][x].getClass() != King.class) {
            throw new IllegalStateException("King not where specified!");
        }
        final int boardLength = Chess.board.length;
        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                final Piece me = Chess.board[j][i];
                if (me != null && me.isWhite() == isWhiteTurn) {
                    for (int k = 0; k < boardLength; k++) {
                        for (int l = 0; l < boardLength; l++) {
                            final Piece save = Chess.board[l][k];
                            final Point start = new Point(i, j);
                            final Point end = new Point(k, l);
                            if (me.isActionLegal(start, end)) {
                                rawMove(start, end, me);
                                final Point kingLocation = locateKing(isWhiteTurn);
                                if (!king.isCheck((int) kingLocation.getX(), (int) kingLocation.getY())) {
                                    rawMove(end, start, me);
                                    Chess.board[l][k] = save;
                                    return false;
                                }
                                rawMove(end, start, me);
                                Chess.board[l][k] = save;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * There are 4 types of draws:
     * 1. Stalemate
     * 2. 50 moves without pawn move or piece capture
     * 3. Board repeated 3 times
     * 4. Insufficient mating material
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
     * Determine if the board has repeated 3 times.
     *
     * @return if the board has repeated 3 times
     * @throws IllegalStateException if castle or en passant history size is invalid
     */
    private boolean isTooManyBoardRepetitions() {
        final int historySize = boardHistory.size();
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
        return false;
    }

    /**
     * Determine if two boards are the same.
     *
     * @param boardOne the first board
     * @param boardTwo the second board
     * @return if two boards are the same
     */
    private boolean isSameBoard(Piece[][] boardOne, Piece[][] boardTwo) {
        for (int i = 0; i < Chess.board.length; i++) {
            for (int j = 0; j < Chess.board.length; j++) {
                if (boardOne[j][i] != boardTwo[j][i]) {
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
     * Find pieces on the board.
     *
     * @param ally  the ally pieces
     * @param enemy the enemy pieces
     */
    private void findPieces(List<Piece> ally, List<Piece> enemy) {
        for (Piece[] slice : Chess.board) {
            for (Piece me : slice) {
                if (me == null || me.getClass() == King.class) {
                    continue;
                }
                if (me.isWhite() == isWhiteTurn) {
                    ally.add(me);
                } else {
                    enemy.add(me);
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
        return (group.size() == 0) || (group.size() == 1 && (group.get(0).getClass() == Knight.class
                || group.get(0).getClass() == Bishop.class)) || (group.size() == 2
                && group.get(0).getClass() == Knight.class && group.get(1).getClass() == Knight.class);
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
        if (king.isCheck((int) location.getX(), (int) location.getY())) {
            isInCheck = true;
            final String text = "Warning: you are in check.\nYou must get out of check.";
            final String[] options = {"OK"};
            customText(text, options);
        }
    }

    /**
     * Determine if the king can castle, and if so, castle.
     *
     * @param to the location to move to
     * @return if the king can castle
     */
    private boolean canCastle(Point to) {
        final int x1 = (int) from.getX(), x2 = (int) to.getX();
        final int y1 = (int) from.getY(), y2 = (int) to.getY();
        final Piece[] slice = Chess.board[7];
        if (y1 != 7 || y2 != 7) {
            return false;
        }
        final King king = new King(isWhiteTurn);
        if (x1 == 4 && x2 == 0) {
            if (hasMoved(slice[0]) && slice[1] == null && slice[2] == null && slice[3] == null && hasMoved(slice[4])) {
                if (!king.isCheck(4, 7) && !king.isCheck(3, 7) && !king.isCheck(2, 7)) {
                    moveCastle(4, 2);
                    moveCastle(0, 3);
                    return true;
                }
            }
        } else if (x1 == 4 && x2 == 7) {
            if (hasMoved(slice[4]) && slice[5] == null && slice[6] == null && hasMoved(slice[7])) {
                if (!king.isCheck(4, 7) && !king.isCheck(5, 7) && !king.isCheck(6, 7)) {
                    moveCastle(4, 6);
                    moveCastle(7, 5);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determine if the piece has moved.
     *
     * @param me the piece
     * @return if the piece has moved
     */
    private boolean hasMoved(Piece me) {
        return me != null && !me.hasMoved();
    }

    /**
     * Move the piece when castling.
     *
     * @param from location to move from
     * @param to   location to move to
     */
    private void moveCastle(int from, int to) {
        Chess.board[7][to] = Chess.board[7][from];
        Chess.board[7][from] = null;
        Chess.board[7][to].setMove();
    }

    /**
     * Move the piece.
     *
     * @param start the location to move from
     * @param end   the location to move to
     * @param me    the piece to move
     */
    private void move(Point start, Point end, Piece me) {
        if (Chess.board[(int) end.getY()][(int) end.getX()] != null || me.getClass() == Pawn.class) {
            drawCounter = 0;
            boardHistory.clear();
            enPassantHistory.clear();
            canCastleHistory.clear();
        } else {
            drawCounter++;
            final int boardLength = Chess.board.length;
            final Piece[][] boardCopy = new Piece[boardLength][boardLength];
            for (int i = 0; i < boardLength; i++) {
                for (int j = 0; j < boardLength; j++) {
                    boardCopy[j][i] = Chess.board[j][i];
                }
            }
            boardHistory.add(boardCopy);
            final Point kingLocation = locateKing(isWhiteTurn);
            canCastleHistory.add(Chess.board[(int) kingLocation.getY()][(int) kingLocation.getX()].hasMoved());
        }
        rawMove(start, end, me);
        me.setMove();
    }

    /**
     * Move the piece without setting the piece to moved state. Used when checking the board and not actually moving it.
     *
     * @param start the location to move from
     * @param end   the location to move to
     * @param me    the piece to move
     */
    private void rawMove(Point start, Point end, Piece me) {
        Chess.board[(int) start.getY()][(int) start.getX()] = null;
        Chess.board[(int) end.getY()][(int) end.getX()] = me;
    }

    /**
     * Flip the board so that the opposite player can play with the correct orientation.
     */
    private void flipBoard() {
        final int BOARD_SIZE = Chess.board.length;
        for (int i = 0; i < BOARD_SIZE / 2; i++) {
            final Piece[] tempSlice = Chess.board[BOARD_SIZE - i - 1];
            Chess.board[BOARD_SIZE - i - 1] = Chess.board[i];
            Chess.board[i] = tempSlice;
        }
        chess.refreshPixels();
    }

    /**
     * Display text to the user using a dialog box.
     *
     * @param text    what to display
     * @param options the options the user can click
     * @return the option choice the user picked
     */
    private int customText(String text, String[] options) {
        return JOptionPane.showOptionDialog(null, text, Chess.GAME_TITLE, JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    }
}
