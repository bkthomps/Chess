package chess;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Chess {

    private static final String GAME_TITLE = "Chess";
    private static final JFrame frame = new JFrame(GAME_TITLE);
    private static final int SQUARE_AMOUNT = 8;
    private static final int SQUARE_SIZE = 8;
    private static final int PIXEL_SIZE = SQUARE_AMOUNT * SQUARE_SIZE;
    private final Color[][] pixels = new Color[PIXEL_SIZE][PIXEL_SIZE];
    private Piece[][] board;

    public static void main(String[] args) {
        final Chess chess = new Chess();
        chess.configureGUI();
        final Board board = new Board();
        board.resetBoard();
        chess.board = board.getBoard();
        chess.refreshPixels();
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

    private void refreshPixels() {
        setBackGround();
        setPieces();
    }

    private void setBackGround() {
        final Color darkBrown = new Color(160, 80, 0);
        final Color lightBrown = new Color(200, 100, 0);
        for (int i = 0; i < SQUARE_AMOUNT; i++) {
            for (int j = 0; j < SQUARE_AMOUNT; j++) {
                final Color usedColor = ((i + j) % 2 == 0) ? darkBrown : lightBrown;
                fillInSubSection(usedColor, j * SQUARE_SIZE, i * SQUARE_SIZE);
            }
        }
    }

    private void setPieces() {
        for (int i = 0; i < SQUARE_AMOUNT; i++) {
            for (int j = 0; j < SQUARE_AMOUNT; j++) {
                if (board[i][j] != null) {
                    final Color[][] image = board[i][j].getImage();
                    setPieceColor(j * SQUARE_SIZE, i * SQUARE_SIZE, image);
                }
            }
        }
    }

    private void setPieceColor(int x, int y, Color[][] image) {
        for (int i = 1; i < SQUARE_SIZE - 1; i++) {
            for (int j = 1; j < SQUARE_SIZE - 1; j++) {
                if (image[i - 1][j - 1] != null) {
                    pixels[i + y][j + x] = image[i - 1][j - 1];
                }
            }
        }
    }

    private void fillInSubSection(Color color, int x, int y) {
        for (int i = 0; i < SQUARE_SIZE; i++) {
            for (int j = 0; j < SQUARE_SIZE; j++) {
                pixels[i + y][j + x] = color;
            }
        }
    }

    private class GridPane extends JPanel {

        private final List<Rectangle> cells = new ArrayList<>(PIXEL_SIZE * PIXEL_SIZE);

        @Override
        public Dimension getPreferredSize() {
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            final int guiDisplay;
            if (screenSize.getWidth() < screenSize.getHeight()) {
                guiDisplay = (int) (screenSize.getWidth() * 0.8);
            } else {
                guiDisplay = (int) (screenSize.getHeight() * 0.8);
            }
            final int multiplier = guiDisplay / PIXEL_SIZE;
            return new Dimension(multiplier * PIXEL_SIZE, multiplier * PIXEL_SIZE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            final Graphics2D g2d = (Graphics2D) g.create();
            final int cellWidth = getWidth() / PIXEL_SIZE;
            final int cellHeight = getHeight() / PIXEL_SIZE;
            if (cells.isEmpty()) {
                for (int row = 0; row < PIXEL_SIZE; row++) {
                    for (int col = 0; col < PIXEL_SIZE; col++) {
                        Rectangle cell = new Rectangle(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
                        cells.add(cell);
                    }
                }
            }

            for (int i = 0; i < PIXEL_SIZE; i++) {
                for (int j = 0; j < PIXEL_SIZE; j++) {
                    g2d.setColor(pixels[i][j]);
                    final Rectangle cell = cells.get(j + i * PIXEL_SIZE);
                    g2d.fill(cell);
                    repaint();
                }
            }
        }
    }
}
