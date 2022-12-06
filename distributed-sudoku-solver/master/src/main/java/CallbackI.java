import com.zeroc.Ice.Current;

public class CallbackI implements Demo.Callback{
    @Override
    public void addSolution(String solution, Current current) {
        Master.addSolution(solution);
    }

    @Override
    public void notifyEndOfTask(Current current) {
        Master.removeServerTask();
    }
}
