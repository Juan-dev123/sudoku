import java.util.ArrayList;
import java.util.Dictionary;

public class TaskDigit implements Runnable{

    private Dictionary<String, String> values;
    private String digit;

    private String square;

    private Sudoku sudoku;

    public TaskDigit(Dictionary<String, String> valuesP, char digitP, String squareP, Sudoku sudokuP){
        values = valuesP;
        digit = String.valueOf(digitP);
        square = squareP;
        sudoku = sudokuP;

    }

    @Override
    public void run(){
        System.out.println("Running...");
        ArrayList<Dictionary<String, String>> solutions = searchWithDigit(values, digit);
        if(solutions != null){
            try {
                sudoku.getSolDicSemaphore().acquire();
                for (int i = 0; i < solutions.size(); i++) {
                    sudoku.addPossibleSolution(solutions.get(i));
                }
                sudoku.getSolDicSemaphore().release();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private ArrayList<Dictionary<String, String>> searchWithDigit(Dictionary<String, String> valuesP, String digitP) {
        ArrayList<Dictionary<String, String>> solutions = new ArrayList<>();
        ArrayList<Dictionary<String, String>> result = sudoku.search(sudoku.assign(sudoku.makeACopyOfValues(valuesP), square, digitP), solutions);
        return result;
    }
}
