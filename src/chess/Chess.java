package chess;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The starting point of the program which contains the front end component.
 */
final class Chess {
    static final ResourceBundle resource = ResourceBundle.getBundle("chess.i18n", Locale.getDefault());
    static final String GAME_TITLE = resource.getString("gameTitle");
    private final JFrame frame = new JFrame(GAME_TITLE);

    static final int BOARD_SIZE = 8;
    private static final int PIXELS_PER_SQUARE = 8;
    private static final int PIXELS_ON_BOARD = BOARD_SIZE * PIXELS_PER_SQUARE;

    private static final Piece[][] board = new Piece[BOARD_SIZE][BOARD_SIZE];
    private final Color[][] pixels = new Color[PIXELS_ON_BOARD][PIXELS_ON_BOARD];
    private final GameState state;
    private boolean isWhiteTurn = true;

    public static void main(String[] args) {
        new Chess();
    }

    private Chess() {
        initializeGUI();
        setupPiecesOnBoard();
        refreshPixels();
        state = new GameState(this, isWhiteTurn);
    }

    static void setBoard(Point point, Piece piece) {
        board[point.y][point.x] = piece;
    }

    static Piece getBoard(Point point) {
        return board[point.y][point.x];
    }

    void flipBoard() {
        isWhiteTurn = !isWhiteTurn;
        for (int i = 0; i < BOARD_SIZE / 2; i++) {
            var tempSlice = board[BOARD_SIZE - i - 1];
            board[BOARD_SIZE - i - 1] = board[i];
            board[i] = tempSlice;
        }
        refreshPixels();
    }

    private void initializeGUI() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.add(new GridPane());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void setupPiecesOnBoard() {
        board[0][0] = new Rook(!isWhiteTurn);
        board[0][1] = new Knight(!isWhiteTurn);
        board[0][2] = new Bishop(!isWhiteTurn);
        board[0][3] = new Queen(!isWhiteTurn);
        board[0][4] = new King(!isWhiteTurn);
        board[0][5] = new Bishop(!isWhiteTurn);
        board[0][6] = new Knight(!isWhiteTurn);
        board[0][7] = new Rook(!isWhiteTurn);
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = new Pawn(!isWhiteTurn);
            board[6][i] = new Pawn(isWhiteTurn);
        }
        board[7][0] = new Rook(isWhiteTurn);
        board[7][1] = new Knight(isWhiteTurn);
        board[7][2] = new Bishop(isWhiteTurn);
        board[7][3] = new Queen(isWhiteTurn);
        board[7][4] = new King(isWhiteTurn);
        board[7][5] = new Bishop(isWhiteTurn);
        board[7][6] = new Knight(isWhiteTurn);
        board[7][7] = new Rook(isWhiteTurn);
    }

    void refreshPixels() {
        drawBackgroundGUI();
        drawAllPiecesGUI();
    }

    private void drawBackgroundGUI() {
        var darkBrown = new Color(160, 80, 0);
        var lightBrown = new Color(200, 100, 0);
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                var usedColor = ((i + j) % 2 == 0 ^ !isWhiteTurn) ? lightBrown : darkBrown;
                drawTileBackgroundGUI(usedColor, j, i);
            }
        }
    }

    void drawAllPiecesGUI() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null) {
                    var image = board[i][j].getPieceImage();
                    drawPieceGUI(j * PIXELS_PER_SQUARE, i * PIXELS_PER_SQUARE, image);
                }
            }
        }
    }

    private void drawPieceGUI(int x, int y, Color[][] image) {
        for (int i = 1; i < PIXELS_PER_SQUARE - 1; i++) {
            for (int j = 1; j < PIXELS_PER_SQUARE - 1; j++) {
                if (image[i - 1][j - 1] != null) {
                    pixels[i + y][j + x] = image[i - 1][j - 1];
                }
            }
        }
    }

    void drawTileBackgroundGUI(Color color, int x, int y) {
        for (int i = 0; i < PIXELS_PER_SQUARE; i++) {
            for (int j = 0; j < PIXELS_PER_SQUARE; j++) {
                pixels[i + y * PIXELS_PER_SQUARE][j + x * PIXELS_PER_SQUARE] = color;
            }
        }
    }

    /**
     * Manages the square graphics on the graphical user interface.
     */
    private class GridPane extends JPanel {
        private final List<Rectangle> cells;

        GridPane() {
            cells = new ArrayList<>(PIXELS_ON_BOARD * PIXELS_ON_BOARD);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (state != null) {
                        int horizontalClickPosition = (e.getX()) / (getWidth() / PIXELS_PER_SQUARE);
                        int verticalClickPosition = (e.getY()) / (getHeight() / PIXELS_PER_SQUARE);
                        state.handleClick(horizontalClickPosition, verticalClickPosition);
                    }
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int guiDisplay;
            if (screenSize.getWidth() < screenSize.getHeight()) {
                guiDisplay = (int) (screenSize.getWidth() * 0.8);
            } else {
                guiDisplay = (int) (screenSize.getHeight() * 0.8);
            }
            int multiplier = guiDisplay / PIXELS_ON_BOARD;
            return new Dimension(multiplier * PIXELS_ON_BOARD, multiplier * PIXELS_ON_BOARD);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            var g2d = (Graphics2D) g.create();
            int cellWidth = getWidth() / PIXELS_ON_BOARD;
            int cellHeight = getHeight() / PIXELS_ON_BOARD;
            if (cells.isEmpty()) {
                for (int row = 0; row < PIXELS_ON_BOARD; row++) {
                    for (int col = 0; col < PIXELS_ON_BOARD; col++) {
                        var cell = new Rectangle(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
                        cells.add(cell);
                    }
                }
            }

            for (int i = 0; i < PIXELS_ON_BOARD; i++) {
                for (int j = 0; j < PIXELS_ON_BOARD; j++) {
                    g2d.setColor(pixels[i][j]);
                    var cell = cells.get(j + i * PIXELS_ON_BOARD);
                    g2d.fill(cell);
                    repaint();
                }
            }
        }
    }
}
