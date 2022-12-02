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
        System.out.println("Queue: "+sudoku.getPool().getQueue().size());
        System.out.println("TaskDictionary running");
        Dictionary<String, String> possibleSolution = sudoku.getSolutionsDic().poll();
        if(possibleSolution != null){
            sudoku.addPossibleSolution(dictionaryToString(possibleSolution, sudoku.getSquares()));
            Runnable task = new TaskString(sudoku);
            sudoku.addTaskToPool(task);
        }
        System.out.println("TaskDictionary finished");
    }

    private String dictionaryToString(Dictionary<String, String> solution, List<String> squares){
        String dictionary = "";
        for (int i = 0; i < squares.size(); i++) {
            dictionary += solution.get(squares.get(i));
        }
        return dictionary;
    }
}
