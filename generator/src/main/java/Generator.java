import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

public class Generator {

    private static final String DELIMITER = ",";
    private static final int TOTAL_CELLS = 81;
    private static final int ROWS_COLUMNS = 9;

    public static void main(String[] args) throws FileNotFoundException {
        //Integer.parseInt(args[0])
        String[][] inputGrid = generate(TOTAL_CELLS - 18);
        writeToTextFile(inputGrid);
    }

    private static String[][] generate(int emptySquares) {
        String[][] grid = new String[ROWS_COLUMNS][ROWS_COLUMNS];
        Random random = new Random();
        for (String[] square : grid) {
            for (int j = 0; j < square.length; j++)
                square[j] = String.valueOf(random.nextInt(ROWS_COLUMNS - 1) + 1);
        }
        return eraseCells(grid, emptySquares);
    }

    private static String[][] eraseCells(String[][] grid, int emptySquares) {
        Random random = new Random();
        for (int i = 0; i < emptySquares; i++) {
            int randomRow = random.nextInt(ROWS_COLUMNS);
            int randomColumn = random.nextInt(ROWS_COLUMNS);
            if (!grid[randomRow][randomColumn].isEmpty())
                grid[randomRow][randomColumn] = "";
            else
                i--;
        }
        return grid;
    }

    private static void writeToTextFile(String[][] grid) throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter("generator/src/main/resources/inputSudoku.txt");
        for (String[] square : grid) {
            for (int j = 0; j < square.length; j++) {
                if (j != 8)
                    printWriter.write(square[j] + DELIMITER);
                else
                    printWriter.write(square[j]);
            }
            printWriter.write("\n");
        }
        printWriter.close();
    }
}