import java.util.Dictionary;

public class TaskString implements Runnable {

    private Sudoku sudoku;

    public TaskString(Sudoku sudokuP){
        sudoku = sudokuP;
    }

    @Override
    public void run() {
        System.out.println("Queue: "+sudoku.getPool().getQueue().size());
        System.out.println("TaskString running");
        String possibleSolution = sudoku.getSolutionsStr().poll();
        if(possibleSolution != null){
            //If the solution has not been added before
            if(!sudoku.getSolutions().contains(possibleSolution)){
                sudoku.addSolution(possibleSolution);
            }
        }
        System.out.println("TaskString finished");
    }
}
