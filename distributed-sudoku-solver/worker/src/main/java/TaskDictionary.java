import Demo.CallbackPrx;

import java.util.Dictionary;
import java.util.List;

public class TaskDictionary implements Runnable {

    private final SolverI solver;
    private final CallbackPrx cl;

    public TaskDictionary(SolverI solverP, CallbackPrx cl){
        solver = solverP;
        this.cl = cl;
    }

    @Override
    public void run() {
        try {
            solver.getSolDicSemaphore().acquire();
            Dictionary<String, String> solutionGrid = solver.getSolutionsDic().poll();
            solver.getSolDicSemaphore().release();
            if(solutionGrid != null){
                String solutionText = dictionaryToString(solutionGrid, solver.getSquares());
                cl.addSolution(solutionText);
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
}
