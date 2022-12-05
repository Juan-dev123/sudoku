import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

public class TaskDictionary implements Runnable {

    private Sudoku sudoku;

    private PrintWriter printWriter;

    public TaskDictionary(Sudoku sudokuP, PrintWriter printWriterP){
        sudoku = sudokuP;
        printWriter = printWriterP;
    }

    @Override
    public void run() {
        try {
            sudoku.getSolDicSemaphore().acquire();
            Dictionary<String, String> solutionGrid = sudoku.getSolutionsDic().poll();
            sudoku.getSolDicSemaphore().release();
            if(solutionGrid != null){
                String solutionText = dictionaryToString(solutionGrid, sudoku.getSquares());
                sudoku.writeText(printWriter,solutionText);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    private String dictionaryToString(Dictionary<String, String> solution, List<String> squares){
        String dictionary = "";
        for (int i = 0; i < squares.size(); i++) {
            dictionary += solution.get(squares.get(i));
        }
        return dictionary;
    }
}
