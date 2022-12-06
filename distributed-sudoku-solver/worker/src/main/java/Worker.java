import com.zeroc.Ice.Util;

public class Worker {

    public static void main(String[] args){
        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args,"config.server"))
        {
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Solver");
            com.zeroc.Ice.Object object = new SolverI();
            String identity = communicator.getProperties().getProperty("Identity");
            adapter.add(object, Util.stringToIdentity(identity));
            //adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("SimpleSolver"));
            adapter.activate();
            System.out.println("Server running");
            communicator.waitForShutdown();
        }
    }
}
