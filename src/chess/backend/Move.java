package chess.backend;

/**
 * Specifies the type of move.
 */
public enum Move {
    NONE,
    QUEEN_SIDE_CASTLE,
    KING_SIDE_CASTLE,
    EN_PASSANT,
    PAWN_PROMOTION,
    NORMAL,
}
