package chess;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the game state which occurs after each move.
 */
public enum GameStatus {
    ONGOING("", ContinueStatus.CONTINUE),
    IN_CHECK("inCheck", ContinueStatus.WARN),
    WHITE_WINS("whiteWins", ContinueStatus.TERMINATE),
    BLACK_WINS("blackWins", ContinueStatus.TERMINATE),
    STALEMATE("stalemate", ContinueStatus.TERMINATE),
    TOO_MANY_MOVES("tooManyMoves", ContinueStatus.TERMINATE),
    TOO_MANY_REPETITIONS("boardRepeat", ContinueStatus.TERMINATE),
    INSUFFICIENT_MATING("insufficientPieces", ContinueStatus.TERMINATE);

    private enum ContinueStatus {
        CONTINUE,
        WARN,
        TERMINATE
    }

    private final String text;
    private final ContinueStatus continueStatus;

    GameStatus(String text, ContinueStatus continueStatus) {
        this.text = text;
        this.continueStatus = continueStatus;
    }

    String textCode() {
        return text;
    }

    boolean mustWarnUser() {
        return continueStatus != ContinueStatus.CONTINUE;
    }

    boolean isGameOver() {
        return continueStatus == ContinueStatus.TERMINATE;
    }
}
