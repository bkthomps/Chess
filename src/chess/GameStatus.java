package chess;

/**
 * Represents the game state which occurs after each move.
 */
public enum GameStatus {
    ONGOING,
    IN_CHECK,
    WHITE_WINS,
    BLACK_WINS,
    STALEMATE,
    TOO_MANY_MOVES,
    TOO_MANY_REPETITIONS,
    INSUFFICIENT_MATING
}
