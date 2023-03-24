package main.java;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Color;
import javax.swing.JFrame;

import java.util.Random;
import java.util.List;
import java.util.Arrays;

public class App extends Canvas {

    // Game settings
    public static final int CELL_SIZE_IN_PIXELS = 10;
    public static final Color DEAD_CELL_COLOUR = Color.BLACK;
    public static final Color ALIVE_CELL_COLOUR = Color.WHITE;

    // Window settings
    public static final int WINDOW_WIDTH_IN_CELLS = 50;
    public static final int WINDOW_HEIGHT_IN_CELLS = 50;

    // Extra information
    public static final boolean OUTLINE_CELLS = false;
    public static final int OUTLINE_SIZE_IN_PIXELS = 1;
    public static final int MAX_FRAME_RATE = 60;
    public static final boolean RANDOM_START = true;

    public static boolean[][] board = new boolean[WINDOW_WIDTH_IN_CELLS][WINDOW_HEIGHT_IN_CELLS];

    public static void generate_board() {
        Boolean[] array = {true, false};
        List<Boolean> choices = Arrays.asList(array);
        Random random = new Random();
        for (int y = 0; y < WINDOW_HEIGHT_IN_CELLS; y++) {
            for (int x = 0; x < WINDOW_WIDTH_IN_CELLS; x++) {
                if (RANDOM_START) {
                    board[y][x] = choices.get(random.nextInt(choices.size()));
                } else {
                    board[y][x] = false;
                }
            }
        }
    }

    public static boolean[] getCellsAround(int x, int y) {
        // for xc in -1..1
        // for yc in -1..1
        //     index = (x + xc, y + yc)

        Integer[] offsets = {-1, 0, 1};
        List<Integer> offsetsList = Arrays.asList(offsets);

        boolean[] cellsAroundHouse = new boolean[8];
        int index = 0;

        for (int delta_x = 0; delta_x > offsetsList.size(); delta_x++) {
            for (int delta_y = 0; delta_y > offsetsList.size(); delta_y++) {
                if (delta_x == 0 && delta_y == 0 ) {}
                else {
                    cellsAroundHouse[index] = board[x + delta_x][y + delta_y];
                    index++;
                }
            }
        }
        
        return cellsAroundHouse;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Game of Life");
        Canvas canvas = new App();

        int screenWidth = WINDOW_WIDTH_IN_CELLS * CELL_SIZE_IN_PIXELS;
        int screenHeight = WINDOW_HEIGHT_IN_CELLS * CELL_SIZE_IN_PIXELS;

        canvas.setSize(screenWidth, screenHeight);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);

        generate_board();
        
    }

    public void paint(Graphics graphicsContext) {
        for (int y = 0; y < WINDOW_HEIGHT_IN_CELLS; y++) {
            for (int x = 0; x < WINDOW_WIDTH_IN_CELLS; x++) {
                if (board[y][x]) {
                    graphicsContext.setColor(ALIVE_CELL_COLOUR);
                } else {
                    graphicsContext.setColor(DEAD_CELL_COLOUR);
                }
                graphicsContext.fillRect(
                    x*CELL_SIZE_IN_PIXELS,
                    y*CELL_SIZE_IN_PIXELS,
                    CELL_SIZE_IN_PIXELS,
                    CELL_SIZE_IN_PIXELS
                );
            }
        }

    }
}
