package chess;

import java.awt.Point;

class GameState {

    private Piece[][] board;
    private Chess chess;
    private boolean isWhiteTurn = true;
    private Piece moving;
    private Point from;

    GameState(Piece[][] board, Chess chess) {
        this.board = board;
        this.chess = chess;
    }

    void clicked(int x, int y) {
        if (moving == null) {
            final Piece toMove = board[y][x];
            if (toMove != null && toMove.isWhite() == isWhiteTurn) {
                moving = toMove;
                from = new Point(x, y);
                return;
            }
            moving = null;
            from = null;
            return;
        }
        final Point to = new Point(x, y);
        if (canCastle(to)) {
            isWhiteTurn = !isWhiteTurn;
            flipBoard();
        } else if (moving.isActionLegal(from, to)) {
            move(from, to, moving);
            isWhiteTurn = !isWhiteTurn;
            flipBoard();
        }
        moving = null;
        from = null;
    }

    private boolean canCastle(Point to) {
        final int x1 = (int) from.getX(), x2 = (int) to.getX();
        final int y1 = (int) from.getY(), y2 = (int) to.getY();
        final Piece[] slice = board[7];
        if (y1 != 7 || y2 != 7) {
            return false;
        }
        if (isWhiteTurn && x1 == 4 && x2 == 0) {
            if (hasMoved(slice[0]) && slice[1] == null && slice[2] == null && slice[3] == null && hasMoved(slice[4])) {
                setMove(4, 2);
                setMove(0, 3);
                return true;
            }
        } else if (isWhiteTurn && x1 == 4 && x2 == 7) {
            if (hasMoved(slice[4]) && slice[5] == null && slice[6] == null && hasMoved(slice[7])) {
                setMove(4, 6);
                setMove(7, 5);
                return true;
            }
        } else if (!isWhiteTurn && x1 == 3 && x2 == 0) {
            if (hasMoved(slice[0]) && slice[1] == null && slice[2] == null && hasMoved(slice[3])) {
                setMove(3, 1);
                setMove(0, 2);
                return true;
            }
        } else if (!isWhiteTurn && x1 == 3 && x2 == 7) {
            if (hasMoved(slice[3]) && slice[4] == null && slice[5] == null && slice[6] == null && hasMoved(slice[7])) {
                setMove(3, 5);
                setMove(7, 4);
                return true;
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
        board[(int) start.getY()][(int) start.getX()] = null;
        board[(int) end.getY()][(int) end.getX()] = me;
        me.setMove();
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
}
