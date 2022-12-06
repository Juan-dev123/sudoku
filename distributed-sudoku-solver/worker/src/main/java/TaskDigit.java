import java.util.Dictionary;

public class TaskDigit implements Runnable{

    private final Dictionary<String, String> values;

    private final SolverI solver;

    public TaskDigit(Dictionary<String, String> valuesP, SolverI solver){
        values = valuesP;
        this.solver = solver;

    }

    @Override
    public void run(){
        System.out.println("Running...");
        solver.search(values);
    }

}
