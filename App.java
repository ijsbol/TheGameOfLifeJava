import java.awt.Graphics;
import java.awt.Point;

import javax.swing.Timer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.util.Random;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;

public class App extends JPanel implements KeyListener {

    // Game settings
    private static final int CELL_SIZE_IN_PIXELS = 1;
    private static final Color DEAD_CELL_COLOUR = Color.BLACK;
    private static final Color ALIVE_CELL_COLOUR = Color.WHITE;

    // Window settings
    private static final int WINDOW_WIDTH_IN_CELLS = 500;
    private static final int WINDOW_HEIGHT_IN_CELLS = 500;

    // Extra information
    private static final int MILLIESECONDS_BETWEEN_FRAMES = 10;

    /* :: Window adjustments ::
     * For some reason JFrame includes the window bounding box as part
     * of the canvas width / height; this makes it so that not all of the
     * cells can be displayed at once. These adjust the size of the
     * window so that all of the cells fit into the view area.
     */
    private static final int WINDOW_WIDTH_ADJUSTMENT = 6;
    private static final int WINDOW_HEIGHT_ADJUSTMENT = 28;

    // Don't mess with these :}
    private boolean currentlyUpdatingCells = false;
    private boolean[][] board = new boolean[WINDOW_WIDTH_IN_CELLS][WINDOW_HEIGHT_IN_CELLS];
    private List<Integer> updated_cell_locations = new ArrayList<>();
    private boolean randomUpdateState = false;
    private boolean initalCellMouseClickedAliveState;
    private boolean rainbowMode = false;
    private boolean neighbourDeterministicCellColouringMode = false;

    // neighbourDeterministicCellColouring variables.
    private Color[] neighbourDeterministicCellColouringColours = new Color[]{
        DEAD_CELL_COLOUR,           // 0 alive neighbours
        new Color(255, 115, 58),    // 1 alive neighbour
        new Color(255, 135, 56),    // 2 alive neighbours
        new Color(255, 154, 56),    // 3 alive neighbours
        new Color(255, 173, 60),    // 4 alive neighbours
        new Color(255, 173, 60),    // 5 alive neighbours
        new Color(255, 191, 67),    // 6 alive neighbours
        new Color(255, 209, 78),    // 7 alive neighbours
        new Color(255, 226, 92),    // 8 alive neighbours
    };

    // Rainbow mode variables
    private Map<String, Integer> cornerOne = Map.of("r", 255,  "g", 0,  "b", 0);
    private Map<String, Integer> cornerTwo = Map.of("r", 0,  "g", 255,  "b", 0);
    private Map<String, Integer> cornerThree = Map.of("r", 0,  "g", 0,  "b", 255);
    private Map<String, Integer> cornerFour = Map.of("r", 0,  "g", 255,  "b", 255);

    public App() {
        // Initilize the key listeners.
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                Point mousePoint = mouseEvent.getPoint();

                int cell_pos_x = mousePoint.x / CELL_SIZE_IN_PIXELS;
                int cell_pos_y = mousePoint.y / CELL_SIZE_IN_PIXELS;

                initalCellMouseClickedAliveState = board[cell_pos_y][cell_pos_x];

                // Invert the cell type.
                board[cell_pos_y][cell_pos_x] = !initalCellMouseClickedAliveState;

                // Add the new cell to the repaint frame update.
                updated_cell_locations.add(cell_pos_x);
                updated_cell_locations.add(cell_pos_y);

                // Repaint the frame.
                repaint();
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                Point mousePoint = mouseEvent.getPoint();
                int cell_pos_x = mousePoint.x / CELL_SIZE_IN_PIXELS;
                int cell_pos_y = mousePoint.y / CELL_SIZE_IN_PIXELS;

                // Set the cell alive state to the inverse of the first cell type clicked.
                board[cell_pos_y][cell_pos_x] = !initalCellMouseClickedAliveState;

                // Add the new cell to the repaint frame update.
                updated_cell_locations.add(cell_pos_x);
                updated_cell_locations.add(cell_pos_y);

                // Repaint the frame.
                repaint();
            }
        });
    }

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
        canvas.setBackground(DEAD_CELL_COLOUR);

        canvas.generate_initial_board();
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);

        canvas.createRepaintTimer(frame);

    }

    public void keyPressed(KeyEvent keyEvent) {
        if(keyEvent.getKeyCode() == KeyEvent.VK_SPACE) {
            // Continue cell permutations if space key pressed.
            this.currentlyUpdatingCells = true;
        } else if(keyEvent.getKeyCode() == KeyEvent.VK_TAB) {
            // Pause cell permutations if tab key pressed.
            this.currentlyUpdatingCells = false;
        } else if(keyEvent.getKeyCode() == KeyEvent.VK_R) {
            // Randomly replace all cells if the R key is pressed.
            this.randomUpdateState = true;
            generate_initial_board();
            repaint();
        } else if(keyEvent.getKeyCode() == KeyEvent.VK_S) {
            // Step through one generation if the S key is pressed.
            permutate();
            repaint();
        } else if(keyEvent.getKeyCode() == KeyEvent.VK_C) {
            // Clears the board.
            generate_initial_board();
            repaint();
        } else if(keyEvent.getKeyCode() == KeyEvent.VK_Q) {
            // Enables rainbow mode.
            this.rainbowMode = true;
            this.neighbourDeterministicCellColouringMode = false;
            repaint();
        } else if(keyEvent.getKeyCode() == KeyEvent.VK_W) {
            // Disabled rainbow mode.
            this.rainbowMode = false;
            repaint();
        } else if(keyEvent.getKeyCode() == KeyEvent.VK_O) {
            // Enables neighbour-deterministic cell colouring.
            this.neighbourDeterministicCellColouringMode = true;
            this.rainbowMode = false;
            repaint();
        } else if(keyEvent.getKeyCode() == KeyEvent.VK_P) {
            // Disables neighbour-deterministic cell colouring.
            this.neighbourDeterministicCellColouringMode = false;
            repaint();
        }
    }

    public void keyReleased(KeyEvent keyEvent) { }
    public void keyTyped(KeyEvent e) { }

    private void generate_initial_board() {
        // Generate the initial board.
        Boolean[] array = {true, false};
        List<Boolean> choices = Arrays.asList(array);
        Random random = new Random();
        for (int y = 0; y < WINDOW_HEIGHT_IN_CELLS; y++) {
            for (int x = 0; x < WINDOW_WIDTH_IN_CELLS; x++) {
                if (this.randomUpdateState) {
                    // If it's a random start, pick a random live/dead state.
                    this.board[y][x] = choices.get(random.nextInt(choices.size()));

                    // Add the new cell to the frame buffer.
                    updated_cell_locations.add(x);
                    updated_cell_locations.add(y);
                } else {
                    this.board[y][x] = false;
                }
            }
        }

        randomUpdateState = false;
    }

    private int getLiveSurroundingCells(int x, int y) {

        int y1 = (y+1) % WINDOW_HEIGHT_IN_CELLS;
        int x1 = ((x-1) + WINDOW_WIDTH_IN_CELLS) % WINDOW_WIDTH_IN_CELLS;

        int y2 = ((y-1) + WINDOW_HEIGHT_IN_CELLS) % WINDOW_HEIGHT_IN_CELLS;
        int x2 = (x+1) % WINDOW_WIDTH_IN_CELLS;

        int numberOfLiveSurroundingCells = (
            (this.board[y1][x1] ?1:0) + (this.board[y1][x] ?1:0) + (this.board[y1][x2] ?1:0) +
            (this.board[y ][x1] ?1:0) +                            (this.board[y ][x2] ?1:0) +
            (this.board[y2][x1] ?1:0) + (this.board[y2][x] ?1:0) + (this.board[y2][x2] ?1:0)
        );

        return numberOfLiveSurroundingCells;
    }

    private void permutate() {
        // Permutate the board by one generation.
        this.updated_cell_locations = new ArrayList<>();

        for (int y = 0; y < WINDOW_HEIGHT_IN_CELLS; y++) {
            for (int x = 0; x < WINDOW_WIDTH_IN_CELLS; x++) {
                int liveSurroundingCellCount = this.getLiveSurroundingCells(x, y);

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
        // Basic repaint timer.
        final Timer timer = new Timer(MILLIESECONDS_BETWEEN_FRAMES, null);

        frame.repaint();
        frame.setBackground(DEAD_CELL_COLOUR);
        frame.pack();

        timer.addActionListener(e -> {
            if (!frame.isVisible()) {
                timer.stop();
            } else {
                if (currentlyUpdatingCells) {
                    this.permutate();
                    frame.repaint();
                    frame.setBackground(DEAD_CELL_COLOUR);
                    frame.pack();
                }
            }
        });

        timer.start();
    }

    private Color convertXYToRGB(int x, int y) {
        double div = 1.0 / (WINDOW_WIDTH_IN_CELLS * WINDOW_HEIGHT_IN_CELLS);

        int r = (int) Math.min(255,
            (div * (
                cornerOne.get("r") * (WINDOW_WIDTH_IN_CELLS - x) * (WINDOW_HEIGHT_IN_CELLS - y)
                + cornerTwo.get("r") * x * (WINDOW_HEIGHT_IN_CELLS - y)
                + cornerThree.get("r") * (WINDOW_WIDTH_IN_CELLS - x) * y
                + cornerFour.get("r") * x * y
            ))
        );

        int g = (int) Math.min(255,
            (div * (
                cornerOne.get("g") * (WINDOW_WIDTH_IN_CELLS - x) * (WINDOW_HEIGHT_IN_CELLS - y)
                + cornerTwo.get("g") * x * (WINDOW_HEIGHT_IN_CELLS - y)
                + cornerThree.get("g") * (WINDOW_WIDTH_IN_CELLS - x) * y
                + cornerFour.get("g") * x * y
            ))
        );

        int b = (int) Math.min(255,
            (div * (
                cornerOne.get("b") * (WINDOW_WIDTH_IN_CELLS - x) * (WINDOW_HEIGHT_IN_CELLS - y)
                + cornerTwo.get("b") * x * (WINDOW_HEIGHT_IN_CELLS - y)
                + cornerThree.get("b") * (WINDOW_WIDTH_IN_CELLS - x) * y
                + cornerFour.get("b") * x * y
            ))
        );

        return new Color(r, g, b);
    }

    private void initial_paint(Graphics graphicsContext) {
        // The intial board paint (paints all cells at once)
        for (int y = 0; y < WINDOW_HEIGHT_IN_CELLS; y++) {
            for (int x = 0; x < WINDOW_WIDTH_IN_CELLS; x++) {
                if (board[y][x]) {
                    if (rainbowMode) {
                        Color color = convertXYToRGB(x, y);
                        graphicsContext.setColor(color);
                    } else {
                        graphicsContext.setColor(ALIVE_CELL_COLOUR);
                    }
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
        // Paints only the updated cells.
        for (int index = 0; index < this.updated_cell_locations.size(); index += 2) {
            int x = this.updated_cell_locations.get(index);
            int y = this.updated_cell_locations.get(index + 1);

            if (board[y][x] && !neighbourDeterministicCellColouringMode) {
                if (rainbowMode) {
                    Color color = convertXYToRGB(x, y);
                    graphicsContext.setColor(color);
                }
                else {
                    graphicsContext.setColor(ALIVE_CELL_COLOUR);
                }
            } else if (neighbourDeterministicCellColouringMode) {
                int aliveNeighbours = getLiveSurroundingCells(x, y);
                Color color = neighbourDeterministicCellColouringColours[aliveNeighbours];
                graphicsContext.setColor(color);
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
        // Called every update frame.
        if (updated_cell_locations == null) {
            this.initial_paint(graphicsContext);
        } else {
            this.partial_frame_update_paint(graphicsContext);
        }

    }
}
