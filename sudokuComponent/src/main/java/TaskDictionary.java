import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

public class TaskDictionary implements Runnable {

    private Sudoku sudoku;

    public TaskDictionary(Sudoku sudokuP){
        sudoku = sudokuP;
    }

    @Override
    public void run() {
        try {
            sudoku.getSolDicSemaphore().acquire();
            Dictionary<String, String> possibleSolution = sudoku.getSolutionsDic().poll();
            sudoku.getSolDicSemaphore().release();
            if(possibleSolution != null){
                sudoku.getSolStrSemaphore().acquire();
                sudoku.addPossibleSolution(dictionaryToString(possibleSolution, sudoku.getSquares()));
                sudoku.getSolStrSemaphore().release();
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
