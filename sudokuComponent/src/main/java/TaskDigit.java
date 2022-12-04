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
        System.out.println("Queue: "+sudoku.getPool().getQueue().size());
        Dictionary<String, String> solution = searchWithDigit(values, digit);
        if(solution != null){
            try {
                sudoku.getSolDicSemaphore().acquire();
                sudoku.addPossibleSolution(solution);
                sudoku.getSolDicSemaphore().release();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        System.out.println("Task finished");
    }

    private Dictionary<String, String> searchWithDigit(Dictionary<String, String> valuesP, String digitP) {
        Dictionary<String, String> result = sudoku.search(sudoku.assign(sudoku.makeACopyOfValues(valuesP), square, digitP));
        if (result != null) {
            return result; //Solved
        }
        return null; //Simulates a false
    }
}
