import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.List;

public class TaskDictionary implements Runnable {

    private final Sudoku sudoku;

    private final PrintWriter printWriter;

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
                String grid = convertStringToGrid(solutionText);
                sudoku.writeText(printWriter,grid);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    private String dictionaryToString(Dictionary<String, String> solution, List<String> squares){
        StringBuilder dictionary = new StringBuilder();
        for (String square : squares) {
            dictionary.append(solution.get(square));
        }
        return dictionary.toString();
    }

    private String convertStringToGrid(String representationOfGrid){
        StringBuilder grid = new StringBuilder();
        for (int i = 0, j = 1, k = 1, l = 1; i < representationOfGrid.length(); i++, j++, k++){
            if(l == 4){
                grid.append("------+------+------\n");
                l = 1;
            }
            grid.append(representationOfGrid.charAt(i));
            if(j == 3){
                if(k == 9){
                    grid.append("\n");
                    k = 0;
                    l++;
                }else{
                    grid.append(" | ");
                }
                j = 0;
            }else {
                grid.append(" ");
            }
        }
        return grid.toString();
    }
}
