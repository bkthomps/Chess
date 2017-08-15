package chess;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Chess {

    static final String GAME_TITLE = "Chess";
    private static final JFrame frame = new JFrame(GAME_TITLE);

    private static final int BOARD_SIZE = 8;
    private static final int PIXELS_PER_SQUARE = 8;
    private static final int PIXELS_ON_BOARD = BOARD_SIZE * PIXELS_PER_SQUARE;

    private Piece[][] board = new Piece[BOARD_SIZE][BOARD_SIZE];
    private final Color[][] pixels = new Color[PIXELS_ON_BOARD][PIXELS_ON_BOARD];
    private GameState state;

    public static void main(String[] args) {
        final Chess chess = new Chess();
        chess.configureGUI();
        chess.resetBoard();
        chess.refreshPixels();
        chess.state = new GameState(chess.board, chess);
    }

    private void configureGUI() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.add(new GridPane());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void resetBoard() {
        board[0][0] = new Rook(false, board);
        board[0][1] = new Knight(false, board);
        board[0][2] = new Bishop(false, board);
        board[0][3] = new King(false, board);
        board[0][4] = new Queen(false, board);
        board[0][5] = new Bishop(false, board);
        board[0][6] = new Knight(false, board);
        board[0][7] = new Rook(false, board);
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = new Pawn(false, board);
            board[6][i] = new Pawn(true, board);
        }
        board[7][0] = new Rook(true, board);
        board[7][1] = new Knight(true, board);
        board[7][2] = new Bishop(true, board);
        board[7][3] = new Queen(true, board);
        board[7][4] = new King(true, board);
        board[7][5] = new Bishop(true, board);
        board[7][6] = new Knight(true, board);
        board[7][7] = new Rook(true, board);
    }

    void refreshPixels() {
        setBackGround();
        setPieces();
    }

    private void setBackGround() {
        final Color darkBrown = new Color(160, 80, 0);
        final Color lightBrown = new Color(200, 100, 0);
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                final Color usedColor = ((i + j) % 2 == 0) ? darkBrown : lightBrown;
                fillInSubSection(usedColor, j * PIXELS_PER_SQUARE, i * PIXELS_PER_SQUARE);
            }
        }
    }

    private void setPieces() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null) {
                    final Color[][] image = board[i][j].getImage();
                    setPieceColor(j * PIXELS_PER_SQUARE, i * PIXELS_PER_SQUARE, image);
                }
            }
        }
    }

    private void setPieceColor(int x, int y, Color[][] image) {
        for (int i = 1; i < PIXELS_PER_SQUARE - 1; i++) {
            for (int j = 1; j < PIXELS_PER_SQUARE - 1; j++) {
                if (image[i - 1][j - 1] != null) {
                    pixels[i + y][j + x] = image[i - 1][j - 1];
                }
            }
        }
    }

    private void fillInSubSection(Color color, int x, int y) {
        for (int i = 0; i < PIXELS_PER_SQUARE; i++) {
            for (int j = 0; j < PIXELS_PER_SQUARE; j++) {
                pixels[i + y][j + x] = color;
            }
        }
    }

    private class GridPane extends JPanel {

        private final List<Rectangle> cells;

        GridPane() {
            cells = new ArrayList<>(PIXELS_ON_BOARD * PIXELS_ON_BOARD);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    final int horizontalClickPosition = (e.getX()) / (getWidth() / PIXELS_PER_SQUARE);
                    final int verticalClickPosition = (e.getY()) / (getHeight() / PIXELS_PER_SQUARE);
                    state.clicked(horizontalClickPosition, verticalClickPosition);
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            final int guiDisplay;
            if (screenSize.getWidth() < screenSize.getHeight()) {
                guiDisplay = (int) (screenSize.getWidth() * 0.8);
            } else {
                guiDisplay = (int) (screenSize.getHeight() * 0.8);
            }
            final int multiplier = guiDisplay / PIXELS_ON_BOARD;
            return new Dimension(multiplier * PIXELS_ON_BOARD, multiplier * PIXELS_ON_BOARD);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            final Graphics2D g2d = (Graphics2D) g.create();
            final int cellWidth = getWidth() / PIXELS_ON_BOARD;
            final int cellHeight = getHeight() / PIXELS_ON_BOARD;
            if (cells.isEmpty()) {
                for (int row = 0; row < PIXELS_ON_BOARD; row++) {
                    for (int col = 0; col < PIXELS_ON_BOARD; col++) {
                        Rectangle cell = new Rectangle(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
                        cells.add(cell);
                    }
                }
            }

            for (int i = 0; i < PIXELS_ON_BOARD; i++) {
                for (int j = 0; j < PIXELS_ON_BOARD; j++) {
                    g2d.setColor(pixels[i][j]);
                    final Rectangle cell = cells.get(j + i * PIXELS_ON_BOARD);
                    g2d.fill(cell);
                    repaint();
                }
            }
        }
    }
}
