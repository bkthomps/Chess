package chess;

import chess.backend.Board;
import chess.backend.Game;
import chess.backend.GameStatus;
import chess.backend.Move;
import chess.backend.Piece;
import chess.backend.Point;
import chess.backend.PromotionPiece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Keeps track of all frontend components, including the main chess board, and user text displays.
 */
final class Frontend {
    private static final ResourceBundle RESOURCE =
            ResourceBundle.getBundle("chess.i18n", Locale.getDefault());
    private static final String GAME_TITLE = RESOURCE.getString("gameTitle");
    private static final int PIXELS_PER_SQUARE = 8;
    private static final int BOARD_PIXELS_LENGTH = Board.BOARD_LENGTH * PIXELS_PER_SQUARE;
    private static final int BOARD_PIXELS_WIDTH = Board.BOARD_WIDTH * PIXELS_PER_SQUARE;

    private final Color[][] pixels = new Color[BOARD_PIXELS_LENGTH][BOARD_PIXELS_WIDTH];
    private final JFrame frame = new JFrame(GAME_TITLE);
    private final Game game = new Game();

    private ClickState clickState = ClickState.firstClickInstance();
    private static boolean initDone;

    public static void main(String[] args) {
        var text = RESOURCE.getString("startupInformation");
        String[] options = {RESOURCE.getString("acknowledge")};
        new Frontend();
        displayDialogText(text, options);
        initDone = true;
    }

    private Frontend() {
        initializeGUI();
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

    private void refreshPixels() {
        drawBackgroundGUI();
        drawAllPiecesGUI();
    }

    private void drawBackgroundGUI() {
        var darkBrown = new Color(160, 80, 0);
        var lightBrown = new Color(200, 100, 0);
        for (int i = 0; i < Board.BOARD_LENGTH; i++) {
            for (int j = 0; j < Board.BOARD_WIDTH; j++) {
                var usedColor = game.isLightTile(Point.instance(i, j)) ? lightBrown : darkBrown;
                drawTileBackgroundGUI(usedColor, j, i);
            }
        }
    }

    private void drawTileBackgroundGUI(Color color, int x, int y) {
        for (int i = 0; i < PIXELS_PER_SQUARE; i++) {
            for (int j = 0; j < PIXELS_PER_SQUARE; j++) {
                pixels[i + y * PIXELS_PER_SQUARE][j + x * PIXELS_PER_SQUARE] = color;
            }
        }
    }

    private void drawAllPiecesGUI() {
        for (int i = 0; i < Board.BOARD_WIDTH; i++) {
            for (int j = 0; j < Board.BOARD_LENGTH; j++) {
                var image = game.getPieceImage(Point.instance(j, i));
                if (image != null) {
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

    private static int displayDialogText(String text, String[] options) {
        return JOptionPane.showOptionDialog(null, text, GAME_TITLE, JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    }

    private void handleClick(int x, int y) {
        if (clickState.isFirstClick()) {
            lockOntoPiece(Point.instance(x, y));
            return;
        }
        var status = performAction(x, y);
        clickState = ClickState.firstClickInstance();
        refreshPixels();
        if (status.mustWarnUser()) {
            String text = RESOURCE.getString(status.textCode());
            String[] options = {RESOURCE.getString("acknowledge")};
            Frontend.displayDialogText(text, options);
            if (status.isGameOver()) {
                System.exit(0);
            }
        }
    }

    private GameStatus performAction(int x, int y) {
        var point = Point.instance(x, y);
        switch (clickState.getMove(point)) {
            case QUEEN_SIDE_CASTLE:
                game.queenSideCastle();
                return GameStatus.ONGOING;
            case KING_SIDE_CASTLE:
                game.kingSideCastle();
                return GameStatus.ONGOING;
            case EN_PASSANT:
                return game.enPassant(clickState.getMoving(), clickState.getFrom());
            case PAWN_PROMOTION:
                return game.pawnPromotion(pawnPromotion(), clickState.getFrom(), point);
            case NORMAL:
                return game.normalMove(clickState.getMoving(), clickState.getFrom(), point);
            case NONE:
                return GameStatus.ONGOING;
            default:
                throw new IllegalStateException("Invalid move type");
        }
    }

    private PromotionPiece pawnPromotion() {
        var text = RESOURCE.getString("pawnPromotionOption");
        String[] options = {
                RESOURCE.getString("queen"),
                RESOURCE.getString("knight"),
                RESOURCE.getString("rook"),
                RESOURCE.getString("bishop"),
        };
        int promotion = -1;
        while (promotion < 0) {
            promotion = displayDialogText(text, options);
        }
        switch (promotion) {
            case 0:
                return PromotionPiece.QUEEN;
            case 1:
                return PromotionPiece.KNIGHT;
            case 2:
                return PromotionPiece.ROOK;
            case 3:
                return PromotionPiece.BISHOP;
            default:
                throw new IllegalStateException("Did not return promotion piece");
        }
    }

    private void lockOntoPiece(Point point) {
        var piece = game.getAlliedPieceAt(point);
        if (piece == null) {
            clickState = ClickState.firstClickInstance();
        } else {
            clickState = highlightLegalMoves(piece, point);
        }
    }

    private ClickState highlightLegalMoves(Piece moving, Point from) {
        var darkGreen = new Color(0, 100, 40);
        var lightGreen = new Color(0, 140, 50);
        var moves = game.availableMoves(moving, from);
        boolean areThereMoves = false;
        for (int i = 0; i < Board.BOARD_LENGTH; i++) {
            for (int j = 0; j < Board.BOARD_WIDTH; j++) {
                if (moves[i][j] != Move.NONE) {
                    areThereMoves = true;
                    var usedColor = game.isLightTile(Point.instance(i, j)) ? lightGreen : darkGreen;
                    drawTileBackgroundGUI(usedColor, j, i);
                }
            }
        }
        drawAllPiecesGUI();
        if (areThereMoves) {
            return ClickState.secondClickInstance(moving, from, moves);
        }
        return ClickState.firstClickInstance();
    }

    /**
     * Manages the square graphics on the graphical user interface.
     */
    private class GridPane extends JPanel {
        private final List<Rectangle> cells;

        GridPane() {
            cells = new ArrayList<>(BOARD_PIXELS_LENGTH * BOARD_PIXELS_WIDTH);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (initDone) {
                        int horizontalClickPosition = (e.getX()) / (getWidth() / PIXELS_PER_SQUARE);
                        int verticalClickPosition = (e.getY()) / (getHeight() / PIXELS_PER_SQUARE);
                        handleClick(horizontalClickPosition, verticalClickPosition);
                    }
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int multiplier;
            if (screenSize.getWidth() < screenSize.getHeight()) {
                multiplier = (int) (screenSize.getWidth() * 0.8) / BOARD_PIXELS_WIDTH;
            } else {
                multiplier = (int) (screenSize.getHeight() * 0.8) / BOARD_PIXELS_LENGTH;
            }
            return new Dimension(multiplier * BOARD_PIXELS_WIDTH, multiplier * BOARD_PIXELS_LENGTH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            var g2d = (Graphics2D) g.create();
            int width = getWidth() / BOARD_PIXELS_WIDTH;
            int height = getHeight() / BOARD_PIXELS_LENGTH;
            if (cells.isEmpty()) {
                for (int row = 0; row < BOARD_PIXELS_WIDTH; row++) {
                    for (int col = 0; col < BOARD_PIXELS_LENGTH; col++) {
                        var cell = new Rectangle(col * width, row * height, width, height);
                        cells.add(cell);
                    }
                }
            }
            for (int i = 0; i < BOARD_PIXELS_LENGTH; i++) {
                for (int j = 0; j < BOARD_PIXELS_WIDTH; j++) {
                    g2d.setColor(pixels[i][j]);
                    var cell = cells.get(j + i * BOARD_PIXELS_LENGTH);
                    g2d.fill(cell);
                    repaint();
                }
            }
        }
    }
}
