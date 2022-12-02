public class QueueAdministrator implements Runnable {

    private Sudoku sudoku;

    public QueueAdministrator(Sudoku sudokuP){
        sudoku = sudokuP;
    }

    @Override
    public void run() {
        addTaskToPool();
        sudoku.setRunningTasks(false);
    }

    private void waitForSpace(){
        while(sudoku.getPool().getQueue().size() == Sudoku.MAX_QUEUE_SIZE){
            Thread.yield();
            System.out.println("Me quede en 1");
            if(sudoku.getPool().getQueue().size() < Sudoku.MAX_QUEUE_SIZE){
                addTaskToPool();
            }
        }
    }

    private void addTaskToPool(){
        if(sudoku.getTasks().size() > 0){
            if(sudoku.getPool().getQueue().size() == Sudoku.MAX_QUEUE_SIZE){
                waitForSpace();
            }else{
                Runnable tempTask = sudoku.getTasks().poll();
                if(tempTask != null){
                    sudoku.getPool().execute(tempTask);
                    addTaskToPool();
                }
            }
        }
    }
}
