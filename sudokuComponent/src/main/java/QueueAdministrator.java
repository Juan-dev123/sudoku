public class QueueAdministrator implements Runnable {

    private Sudoku sudoku;

    public QueueAdministrator(Sudoku sudokuP) {
        sudoku = sudokuP;
    }

    @Override
    public void run() {
        addTaskToPool();
        sudoku.setRunningTasks(false);
    }

    private void waitForSpace() {
        while (sudoku.getPool().getQueue().size() == Sudoku.MAX_QUEUE_SIZE) {
            Thread.yield();
            System.out.println("Me quede en 1");
            if (sudoku.getPool().getQueue().size() < Sudoku.MAX_QUEUE_SIZE) {
                addTaskToPool();
            }
        }
    }

    private void addTaskToPool() {
        try {
            if (sudoku.getTasks().size() > 0) {
                if (sudoku.getPool().getQueue().size() == Sudoku.MAX_QUEUE_SIZE) {
                    waitForSpace();
                } else {
                    sudoku.getSemaphoreTasks().acquire();
                    Runnable tempTask = sudoku.getTasks().poll();
                    sudoku.getSemaphoreTasks().release();
                    if (tempTask != null) {
                        sudoku.getPool().execute(tempTask);
                        addTaskToPool();
                    }
                }
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
/*    private void waitForSpace(){
        try {
            sudoku.getPoolSemaphore().acquire();
            while(sudoku.getPool().getQueue().size() == Sudoku.MAX_QUEUE_SIZE){
                Thread.yield();
                sudoku.getPoolSemaphore().release();
                System.out.println("Me quede en 1");
                sudoku.getPoolSemaphore().acquire();
                if(sudoku.getPool().getQueue().size() < Sudoku.MAX_QUEUE_SIZE){
                    addTaskToPool();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }*/

/*    private void addTaskToPool(){
        try {
            if(sudoku.getTasks().size() > 0){
                if(sudoku.getPool().getQueue().size() == Sudoku.MAX_QUEUE_SIZE){
                    sudoku.getPoolSemaphore().release();
                    waitForSpace();
                }else{
                    sudoku.getSemaphoreTasks().acquire();
                    Runnable tempTask = sudoku.getTasks().poll();
                    sudoku.getSemaphoreTasks().release();
                    if(tempTask != null){
                        sudoku.getPool().execute(tempTask);
                        addTaskToPool();
                    }
                }
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }*/

