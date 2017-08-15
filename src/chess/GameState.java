package chess;

import javax.swing.JOptionPane;
import java.awt.Point;

class GameState {

    private Piece[][] board;
    private Chess chess;
    private boolean isWhiteTurn = true;
    private Piece moving;
    private Point from;
    private boolean isInCheck;
    private Point enPassant;

    GameState(Piece[][] board, Chess chess) {
        this.board = board;
        this.chess = chess;
    }

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
        } else if (isEnPassantLegal(to)) {
            doEnPassant();
        } else if (moving.isActionLegal(from, to)) {
            checkEnPassant(to);
            doMove(to);
        }
        moving = null;
        from = null;
    }

    private void lockOntoPiece(int x, int y) {
        final Piece toMove = board[y][x];
        if (toMove != null && toMove.isWhite() == isWhiteTurn) {
            moving = toMove;
            from = new Point(x, y);
            return;
        }
        moving = null;
        from = null;
    }

    private void doMoveInCheck(Point to) {
        if (moving.isActionLegal(from, to)) {
            checkEnPassant(to);
            move(from, to, moving);
            final Point location = locateKing();
            final King king = new King(isWhiteTurn, board);
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

    private void doMove(Point to) {
        if ((int) to.getY() == 0 && moving.getClass() == Pawn.class) {
            final String text = "What would you like to promote your pawn to?";
            final String[] options = {"QUEEN", "KNIGHT", "ROOK", "BISHOP"};
            final int promotion = customText(text, options);
            final Piece piece;
            switch (promotion) {
                case 0:
                    piece = new Queen(moving.isWhite(), board);
                    break;
                case 1:
                    piece = new Knight(moving.isWhite(), board);
                    break;
                case 2:
                    piece = new Rook(moving.isWhite(), board);
                    break;
                case 3:
                    piece = new Bishop(moving.isWhite(), board);
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

    private boolean isEnPassantLegal(Point to) {
        return enPassant != null && to.equals(enPassant) && moving.getClass() == Pawn.class
                && moving.isWhite() != board[(int) to.getY() + 1][(int) to.getX()].isWhite();
    }

    private void doEnPassant() {
        move(from, enPassant, moving);
        board[(int) enPassant.getY() + 1][(int) enPassant.getX()] = null;
        enPassant = null;
        isWhiteTurn = !isWhiteTurn;
        flipBoard();
        checkEndgame();
    }

    private void checkEnPassant(Point to) {
        if (moving.getClass() == Pawn.class && from.getY() - to.getY() == 2) {
            enPassant = new Point((int) to.getX(), (int) to.getY() - 2);
        } else {
            enPassant = null;
        }
    }

    private void checkEndgame() {
        final Point location = locateKing();
        final int x = (int) location.getX(), y = (int) location.getY();
        final King king = new King(isWhiteTurn, board);
        if (isCheckmate(king, x, y)) {
            final String team = (isWhiteTurn) ? "Black" : "White";
            final String text = "Checkmate! " + team + " wins!";
            finishGame(text);
        } else if (isStalemate(king, x, y)) {
            final String text = "Draw! Because of stalemate!";
            finishGame(text);
        } else if (false) {
            /*
             * TODO: if draw, 4 ways to get draw
             * 1. Stalemate: exact same as checkmate except king is not currently in check
             * 2. 50 moves without pawn move or piece capture, just use a counter
             * 3. Board repeated 3 times, just use an ArrayList and compare after every move and keep a counter, resetting
             *    the ArrayList when a pawn moves or a piece is capture, can use counter from draw type 2.
             * 4. Not enough pieces to cause a checkmate. 4 ways for this to happen
             *    a. king vs. king
             *    b. king and bishop vs. king
             *    c. king and knight vs king
             *    d. king and bishop vs. king and bishop, when bishops are on the same color
             */
        } else {
            warnIfCheck(king, location);
        }
    }

    private boolean isCheckmate(King king, int x, int y) {
        return king.isCheck(x, y) && !isMovePossible(king, x, y);
    }

    private boolean isStalemate(King king, int x, int y) {
        return !king.isCheck(x, y) && !isMovePossible(king, x, y);
    }

    private boolean isMovePossible(King king, int x, int y) {
        if (board[y][x].getClass() != King.class) {
            throw new IllegalStateException("King not where specified!");
        }
        final int boardLength = board.length;
        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                final Piece me = board[j][i];
                if (me != null && me.isWhite() == isWhiteTurn) {
                    for (int k = 0; k < boardLength; k++) {
                        for (int l = 0; l < boardLength; l++) {
                            final Point start = new Point(i, j);
                            final Point end = new Point(k, l);
                            if (me.isActionLegal(start, end)) {
                                rawMove(start, end, me);
                                final Point kingLocation = locateKing();
                                if (!king.isCheck((int) kingLocation.getX(), (int) kingLocation.getY())) {
                                    rawMove(end, start, me);
                                    return true;
                                }
                                rawMove(end, start, me);
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void finishGame(String text) {
        final String[] options = {"OK"};
        customText(text, options);
        System.exit(0);
    }

    private void warnIfCheck(King king, Point location) {
        if (king.isCheck((int) location.getX(), (int) location.getY())) {
            isInCheck = true;
            final String text = "Warning: you are in check.\nYou must get out of check.";
            final String[] options = {"OK"};
            customText(text, options);
        }
    }

    private Point locateKing() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                final Piece item = board[i][j];
                if (item != null && item.getClass() == King.class && item.isWhite() == isWhiteTurn) {
                    return new Point(j, i);
                }
            }
        }
        throw new IllegalStateException("No King!");
    }

    private boolean canCastle(Point to) {
        final int x1 = (int) from.getX(), x2 = (int) to.getX();
        final int y1 = (int) from.getY(), y2 = (int) to.getY();
        final Piece[] slice = board[7];
        if (y1 != 7 || y2 != 7) {
            return false;
        }
        final King king = new King(isWhiteTurn, board);
        if (x1 == 4 && x2 == 0) {
            if (hasMoved(slice[0]) && slice[1] == null && slice[2] == null && slice[3] == null && hasMoved(slice[4])) {
                if (!king.isCheck(4, 7) && !king.isCheck(3, 7) && !king.isCheck(2, 7)) {
                    setMove(4, 2);
                    setMove(0, 3);
                    return true;
                }
            }
        } else if (x1 == 4 && x2 == 7) {
            if (hasMoved(slice[4]) && slice[5] == null && slice[6] == null && hasMoved(slice[7])) {
                if (!king.isCheck(4, 7) && !king.isCheck(5, 7) && !king.isCheck(6, 7)) {
                    setMove(4, 6);
                    setMove(7, 5);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasMoved(Piece me) {
        return me != null && !me.hasMoved();
    }

    private void setMove(int from, int to) {
        board[7][to] = board[7][from];
        board[7][from] = null;
        board[7][to].setMove();
    }

    private void move(Point start, Point end, Piece me) {
        rawMove(start, end, me);
        me.setMove();
    }

    private void rawMove(Point start, Point end, Piece me) {
        board[(int) start.getY()][(int) start.getX()] = null;
        board[(int) end.getY()][(int) end.getX()] = me;
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

    private int customText(String text, String[] options) {
        return JOptionPane.showOptionDialog(null, text, Chess.GAME_TITLE, JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    }
}
