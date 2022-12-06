import Demo.SolverPrx;
import com.zeroc.Ice.*;
import com.zeroc.Ice.Exception;
import com.zeroc.IceGrid.QueryPrx;

import java.io.*;
import com.zeroc.Ice.Object;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

public class Master {

    public static final String PATH_SOLUTIONS = "master/src/main/resources/solutions.txt";
    public static final String PATH_FINAL = "master/src/main/resources/solutionsFinal.txt";
    public static final int MAX_THREADS = 12;
    private static Queue<String> solutions;
    private final ThreadPoolExecutor pool;
    private final Semaphore poolSemaphore;
    private static Semaphore solutionsSemaphore;
    private final Semaphore messageSemaphore;
    private static int tasks;

    private static int serverTasks;

    private static Semaphore serverTaskSemaphore;

    private static Master sudokuCom;

    private final List<String> squares;

    private final PrintWriter printWriter;

    public Master(List<String> squares){
        pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS);
        poolSemaphore = new Semaphore(1);
        solutionsSemaphore = new Semaphore(1);
        messageSemaphore = new Semaphore(1);
        serverTaskSemaphore = new Semaphore(1);
        tasks = 0;
        serverTasks = 0;
        this.squares = squares;
        solutions = new LinkedList<>();
        try {
            printWriter = new PrintWriter(PATH_SOLUTIONS);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args){
        List<String> extraArgs = new ArrayList<>();

        try(Communicator communicator = Util.initialize(args, "config.client", extraArgs))
        {
            long start = System.currentTimeMillis();
            
            String[][] sudokuArray = readSudoku();

            Sudoku sudoku = new Sudoku();
            sudokuCom = new Master(sudoku.getSquares());
            ArrayList<Dictionary<String, String>> possibleValues = sudoku.solve(sudokuArray);

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Callback", "default -p 8050");
            Object object = new CallbackI();
            ObjectPrx objPrx= adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("callback"));
            adapter.activate();
            Demo.CallbackPrx callPrx = Demo.CallbackPrx.uncheckedCast(objPrx);

            if(possibleValues!=null){
                for (Dictionary<String, String> possibleValue : possibleValues) {
                    if (possibleValue != null) {
                        String grid = sudokuCom.dictionaryToString(possibleValue);
                        SolverPrx solver = getSolver(communicator);
                        new Thread(() -> solver.findSolutions(grid, callPrx)).start();
                        sudokuCom.addServerTask();
                    }
                }
            }
            
            waitForServerTask();

            long finish = System.currentTimeMillis();

            System.out.println("It took " + (finish-start) + "ms, " + (finish-start)/1000 + "s, " + (finish-start)/60000 + "m or " + (finish-start)/3600000 + "h to solve.");

            sudokuCom.getPool().shutdown();
            sudokuCom.waitForPool(tasks);
            sudokuCom.getPrintWriter().close();
            sudokuCom.organizeFile();
        }
        
    }

    private static SolverPrx getSolver(Communicator communicator){
        try{
            return SolverPrx.checkedCast(communicator.stringToProxy("SimpleSolver"));
        }catch (Exception e){
            QueryPrx query = com.zeroc.IceGrid.QueryPrx.checkedCast(communicator.stringToProxy("DemoIceGrid/Query"));
            return SolverPrx.checkedCast(query.findObjectByType("::Demo::Solver"));
        }
    }

    private static void waitForServerTask(){
        boolean quit = false;
        while(!quit){
            try {
                serverTaskSemaphore.acquire();
                if(serverTasks == 0){
                    quit = true;
                }else {
                    Thread.yield();
                }
                serverTaskSemaphore.release();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String[][] readSudoku(){
        String[][] sudoku = new String[9][9];
        try {
            BufferedReader bfReader = new BufferedReader(new FileReader("generator/src/main/resources/inputSudoku.txt"));
            String currentLine;
            for (int i = 0; i < sudoku.length; i++) {
                currentLine = bfReader.readLine();
                String[] row = currentLine.split(",", -1);
                for (int j = 0; j < row.length; j++) {
                    if (row[j].equals("")){
                        row[j] = "0";
                    }
                }
                sudoku[i] = row;
            }
            bfReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sudoku;
    }

    private void waitForPool(int task) {
        try {
            poolSemaphore.acquire();
            while (pool.getQueue().size() > 0 || pool.getActiveCount() > 0 || pool.getCompletedTaskCount() < task) {
                Thread.yield();
            }
            poolSemaphore.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getNumberOfSolutions() {
        if (tasks == 0) {
            return "There is no solution for this sudoku\n";
        } else {
            if (tasks == 1) {
                return "There is 1 solution for this sudoku\n";
            } else {
                return "There are " + tasks + " solutions for this sudoku\n";
            }
        }
    }

    public void writeText(PrintWriter printWriter, String text) {
        try {
            messageSemaphore.acquire();
            printWriter.write(text + "\n");
            messageSemaphore.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void organizeFile() {
        try {
            System.out.println("Organizing file...");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(PATH_SOLUTIONS));
            PrintWriter printWriter = new PrintWriter(PATH_FINAL);
            printWriter.write(getNumberOfSolutions());
            String solution = bufferedReader.readLine();
            while (solution != null) {
                printWriter.write(solution + "\n");
                solution = bufferedReader.readLine();
            }
            printWriter.close();
            bufferedReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String dictionaryToString(Dictionary<String, String> possibleValues){
        StringBuilder sudoku = new StringBuilder();
        for (int i = 0; i < squares.size()-1; i++) {
            sudoku.append(possibleValues.get(squares.get(i))).append(",");
        }
        sudoku.append(possibleValues.get(squares.get(squares.size() - 1)));
        return sudoku.toString();
    }

    public static void addSolution(String solution){
        try {
            solutionsSemaphore.acquire();
            solutions.add(solution);
            solutionsSemaphore.release();
            parseSolutionToString(sudokuCom.getPrintWriter());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void parseSolutionToString(PrintWriter printWriter) {
        Runnable task = new TaskDictionary(sudokuCom, printWriter);
        sudokuCom.addOneTask();
        sudokuCom.getPool().execute(task);
    }

    private void addServerTask(){
        try {
            serverTaskSemaphore.acquire();
            serverTasks++;
            serverTaskSemaphore.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeServerTask(){
        try {
            serverTaskSemaphore.acquire();
            serverTasks--;
            serverTaskSemaphore.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void addOneTask(){
        tasks++;
    }

    public ThreadPoolExecutor getPool() {
        return pool;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public Semaphore getSolutionsSemaphore() {
        return solutionsSemaphore;
    }

    public Queue<String> getSolutions() {
        return solutions;
    }
}
