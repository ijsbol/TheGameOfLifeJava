import java.awt.Canvas;
import java.awt.Graphics;
import javax.swing.Timer;

import javafx.scene.control.Cell;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.util.Random;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class App extends JPanel {

    // Game settings
    private static final int CELL_SIZE_IN_PIXELS = 1;
    private static final Color DEAD_CELL_COLOUR = Color.BLACK;
    private static final Color ALIVE_CELL_COLOUR = Color.WHITE;

    // Window settings
    private static final int WINDOW_WIDTH_IN_CELLS = 800;
    private static final int WINDOW_HEIGHT_IN_CELLS = 800;

    // Extra information
    private static final int MAX_FRAME_RATE = 60;
    private static final boolean RANDOM_START = true;

    /* :: Window adjustments ::
     * For some reason JFrame includes the window bounding box as part
     * of the canvas width / height; this makes it so that not all of the
     * cells can be displayed at once. These adjust the size of the
     * window so that all of the cells fit into the view area.
     */
    private static final int WINDOW_WIDTH_ADJUSTMENT = 6;
    private static final int WINDOW_HEIGHT_ADJUSTMENT = 28;

    private boolean[][] board = new boolean[WINDOW_WIDTH_IN_CELLS][WINDOW_HEIGHT_IN_CELLS];

    private List<Integer> updated_cell_locations;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Game of Life");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        int screenWidth  = (WINDOW_WIDTH_IN_CELLS  * CELL_SIZE_IN_PIXELS) + WINDOW_WIDTH_ADJUSTMENT;
        int screenHeight = (WINDOW_HEIGHT_IN_CELLS * CELL_SIZE_IN_PIXELS) + WINDOW_HEIGHT_ADJUSTMENT;

        Dimension dimension = new Dimension(
            screenWidth + CELL_SIZE_IN_PIXELS,
            screenHeight + CELL_SIZE_IN_PIXELS
        );

        frame.setMaximumSize(dimension);
        frame.setMinimumSize(dimension);

        App canvas = new App();

        canvas.setSize(screenWidth, screenHeight);

        canvas.generate_initial_board();
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);

        canvas.createRepaintTimer(frame);

    }

    private void generate_initial_board() {
        Boolean[] array = {true, false};
        List<Boolean> choices = Arrays.asList(array);
        Random random = new Random();
        for (int y = 0; y < WINDOW_HEIGHT_IN_CELLS; y++) {
            for (int x = 0; x < WINDOW_WIDTH_IN_CELLS; x++) {
                if (RANDOM_START) {
                    this.board[y][x] = choices.get(random.nextInt(choices.size()));
                } else {
                    this.board[y][x] = false;
                }
            }
        }
    }

    private int getLiveSurroudningCells(int x, int y) {
        Integer[] offsets = {-1, 0, 1};

        int liveSurroudningCells = 0;

        for (int delta_y: offsets) {
            for (int delta_x: offsets) {
                if (delta_x == 0 && delta_y == 0) {}
                else {
                    int cell_x = x + delta_x;
                    int cell_y = y + delta_y;
                    
                    if (cell_x >= WINDOW_WIDTH_IN_CELLS) {
                        cell_x -= WINDOW_WIDTH_IN_CELLS;
                    } else if (cell_x < 0) {
                        cell_x += WINDOW_WIDTH_IN_CELLS;
                    } if (cell_y >= WINDOW_HEIGHT_IN_CELLS) {
                        cell_y -= WINDOW_HEIGHT_IN_CELLS;
                    } else if (cell_y < 0) {
                        cell_y += WINDOW_HEIGHT_IN_CELLS;
                    }

                    if (this.board[cell_y][cell_x]) {
                        liveSurroudningCells += 1;
                    }
                }
            }
        }

        return liveSurroudningCells;
    }

    private void permutate() {
        // boolean[][] temp_board = new boolean[WINDOW_WIDTH_IN_CELLS][WINDOW_HEIGHT_IN_CELLS];
        this.updated_cell_locations = new ArrayList<>();

        for (int y = 0; y < WINDOW_HEIGHT_IN_CELLS; y++) {
            for (int x = 0; x < WINDOW_WIDTH_IN_CELLS; x++) {
                int liveSurroundingCellCount = this.getLiveSurroudningCells(x, y);

                if (this.board[y][x]) {
                    // Current cell is live.
                    if (liveSurroundingCellCount < 2) {
                        // A live cell dies if it has fewer than two live neighbors.
                        this.updated_cell_locations.add(x);
                        this.updated_cell_locations.add(y);
                    } else if (liveSurroundingCellCount == 2 || liveSurroundingCellCount == 3) {
                        // A live cell with two or three live neighbors lives on to the next generation.

                    } else if (liveSurroundingCellCount > 3) {
                        // A live cell with more than three live neighbors dies.
                        this.updated_cell_locations.add(x);
                        this.updated_cell_locations.add(y);
                    }
                } else {
                    // Current cell is dead
                    if (liveSurroundingCellCount == 3) {
                        // A dead cell will be brought back to live if it has exactly three live neighbors.
                        this.updated_cell_locations.add(x);
                        this.updated_cell_locations.add(y);
                    }
                }
                
            }
        }

        for (int index = 0; index < this.updated_cell_locations.size(); index+=2) {
            int x = this.updated_cell_locations.get(index);
            int y = this.updated_cell_locations.get(index + 1);
            if (this.board[y][x]) {
                this.board[y][x] = false;
            } else {
                this.board[y][x] = true;
            }
        }
    }

    private void createRepaintTimer(JFrame frame) {
        final Timer timer = new Timer(10, null);

        timer.addActionListener(e -> {
            if (!frame.isVisible()) {
                timer.stop();
            } else {
                this.permutate();
                frame.repaint();
                frame.pack();
            }
        });

        timer.start();
    }

    private void initial_paint(Graphics graphicsContext) {
        for (int y = 0; y < WINDOW_HEIGHT_IN_CELLS; y++) {
            for (int x = 0; x < WINDOW_WIDTH_IN_CELLS; x++) {
                if (board[y][x]) {
                    graphicsContext.setColor(ALIVE_CELL_COLOUR);
                } else {
                    graphicsContext.setColor(DEAD_CELL_COLOUR);
                }
                graphicsContext.fillRect(
                    (x * CELL_SIZE_IN_PIXELS),
                    (y * CELL_SIZE_IN_PIXELS),
                    CELL_SIZE_IN_PIXELS,
                    CELL_SIZE_IN_PIXELS
                );
            }
        }
    }

    private void partial_frame_update_paint(Graphics graphicsContext) {
        for (int index = 0; index < this.updated_cell_locations.size(); index += 2) {
            int x = this.updated_cell_locations.get(index);
            int y = this.updated_cell_locations.get(index + 1);

            if (board[y][x]) {
                graphicsContext.setColor(ALIVE_CELL_COLOUR);
            } else {
                graphicsContext.setColor(DEAD_CELL_COLOUR);
            }

            graphicsContext.fillRect(
                (x * CELL_SIZE_IN_PIXELS),
                (y * CELL_SIZE_IN_PIXELS),
                CELL_SIZE_IN_PIXELS,
                CELL_SIZE_IN_PIXELS
            );
        }
    }

    public void paintComponent(Graphics graphicsContext) {
        if (updated_cell_locations == null) {
            this.initial_paint(graphicsContext);
        } else {
            this.partial_frame_update_paint(graphicsContext);
        }

    }
}