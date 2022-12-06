import java.io.PrintWriter;

public class TaskDictionary implements Runnable {

    private final Master sudoku;

    private final PrintWriter printWriter;

    public TaskDictionary(Master sudokuP, PrintWriter printWriterP){
        sudoku = sudokuP;
        printWriter = printWriterP;
    }

    @Override
    public void run() {
        try {
            sudoku.getSolutionsSemaphore().acquire();
            String solutionGrid = sudoku.getSolutions().poll();
            sudoku.getSolutionsSemaphore().release();
            if(solutionGrid != null){
                String grid = convertStringToGrid(solutionGrid);
                sudoku.writeText(printWriter,grid);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    private String convertStringToGrid(String representationOfGrid){
        StringBuilder grid = new StringBuilder();
        for (int i = 0, j = 1, k = 1, l = 1; i < representationOfGrid.length(); i++, j++, k++){
            if(l == 4){
                grid.append("------+-------+------\n");
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
