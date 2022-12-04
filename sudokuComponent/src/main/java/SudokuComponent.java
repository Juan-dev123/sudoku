import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SudokuComponent {

    public static void main(String[] args){
        String[][] sudokuArray = readSudoku("generator/src/main/resources/inputSudoku.txt");
        Sudoku sudoku = new Sudoku();
        sudoku.solve(sudokuArray);
    }

    private static String[][] readSudoku(String path){
        String[][] sudoku = new String[9][9];
        try {
            BufferedReader bfReader = new BufferedReader(new FileReader(path));
            String currentLine;
            for (int i = 0; i < sudoku.length; i++) {
                currentLine = bfReader.readLine();
                String[] row = currentLine.split(",", -1);
                for (int j = 0; j < row.length; j++) {
                    if (row[j].equals("")){
                        row[j] = "0";
                    }
                }
                sudoku[i] = row;
            }
            bfReader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sudoku;
    }
}
