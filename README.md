# Chess
Chess simulation. Click on the piece to move, and the location to move it to. If the action is legal, it will be taken. The game is done once a King enters checkmate, or once there is a draw.

# Pawn
The pawn may move one square forward, or two squares forward on its first action, given that it does not move over any other pieces. The pawn may capture by moving one square diagonally forward.

# Bishop
The bishop may move or capture in any amount of squares diagonally, given that it does not move over any other pieces.

# Rook
The rook may move or capture in any amount of squares horizontally or vertically, given that it does not move over any other pieces.

# Knight
The knight may move one square vertically and two squares horizontally, or vice-versa. The knight may move over other pieces.

# Queen
The queen may move or capture in any amount of squares horizontally or vertically or diagonally, given that it does not move over any other pieces.

# King
The king may move to any square which is adjacent to it, given that it does not move into check.

# Pawn Promotion
If a pawn reaches the other end of the board, the user may pick whether to promote it to a queen, rook, knight, or bishop.

# Castling
If the rook and king have not yet moved, and there are no pieces between them, the king may move two squares towards the rook, and the rook may move to the adjacent square in which the king moved from, given that the king was not in check, will not move into check, and the path between where the king moved from and is moving to does not have any squares which are in check.

# En Passant
Immediately after a pawn moves two squares, a pawn may capture it as if it had only moved one square.

# Check
The king is said to be in check when the next move by the opponent would capture it.

# Checkmate
A checkmate occurs when the king is in check, and any action taken results in the king still being in check.

# Draw
There are four ways for a draw to occur:
1. Stalemate: occurs when the king is not in check, but any action taken results in the king being in check.
2. 50 move rule: 50 moves from both white and black occur without any pawns moving or any pieces being captured.
3. Threefold repetition: the same board repeats three times in a row, meaning the pieces are on the same squares, and the castling and en passant opportunities are the same.
4. Insufficient mating material: not enough pieces to cause a checkmate, which can occur from a lone king against lone king, or king and knight, or king and bishop, or king and two knights.

# Screenshots
![Start Of Game](/Images/StartOfGame.png?raw=true "Start Of Game")
![Middle Of Game](/Images/MiddleOfGame.png?raw=true "Middle Of Game")
