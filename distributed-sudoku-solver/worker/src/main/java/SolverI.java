import Demo.CallbackPrx;
import Demo.Solver;
import com.zeroc.Ice.Current;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

public class SolverI implements Solver {

    private final String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
    private final String[] cols = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};

    static final int MAX_THREADS = 12;
    private final List<String> squares;
    private final List<ArrayList<String>> unitList;
    private final Dictionary<String, ArrayList<ArrayList<String>>> units;
    private final Dictionary<String, ArrayList<String>> peers;
    private final Queue<Dictionary<String, String>> solutionsDic;
    private ThreadPoolExecutor pool;
    private final Semaphore poolSemaphore;
    private final Semaphore solDicSemaphore;
    private int tasks;

    private CallbackPrx callback;

    public SolverI(){
        squares = cross(rows, cols);
        unitList = new ArrayList<>();
        fillUnitList();
        units = new Hashtable<>();
        fillUnits();
        peers = new Hashtable<>();
        fillPeers();
        poolSemaphore = new Semaphore(1);
        solDicSemaphore = new Semaphore(1);
        solutionsDic = new LinkedList<>();
    }

    private ArrayList<String> cross(String[] rowsP, String[] colsP) {
        ArrayList<String> longRow = new ArrayList<>();
        for (String s : rowsP) {
            for (String value : colsP) {
                longRow.add(s + value);
            }
        }
        return longRow;
    }

    private void fillUnits() {
        for (String square : squares) {
            units.put(square, new ArrayList<>());
            for (ArrayList<String> strings : unitList) {
                if (isMember(square, strings)) {
                    units.get(square).add(strings);
                }
            }
        }
    }

    private void fillPeers() {
        for (String square : squares) {
            ArrayList<String> tempPeers = new ArrayList<>();
            for (int j = 0; j < units.get(square).size(); j++) {
                for (int k = 0; k < units.get(square).get(j).size(); k++) {
                    String tempMiniSquare = units.get(square).get(j).get(k);
                    if (!isMember(tempMiniSquare, tempPeers) && !tempMiniSquare.equals(square)) {
                        tempPeers.add(tempMiniSquare);
                    }
                }
            }
            peers.put(square, tempPeers);
        }
    }

    private void fillUnitList() {
        for (String col : cols) {
            String[] temp = {col};
            unitList.add(cross(rows, temp));
        }
        for (String row : rows) {
            String[] temp = {row};
            unitList.add(cross(temp, cols));
        }
        String[][] rrows = {{"A", "B", "C"}, {"D", "E", "F"}, {"G", "H", "I"}};
        String[][] ccols = {{"1", "2", "3"}, {"4", "5", "6"}, {"7", "8", "9"}};

        for (String[] rrow : rrows) {
            for (String[] ccol : ccols) {
                unitList.add(cross(rrow, ccol));
            }
        }
    }

    private boolean isMember(String item, ArrayList<String> list) {
        return list.contains(item);
    }

    @Override
    public void findSolutions(String grid, CallbackPrx cl, Current current) {
        pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS);
        tasks = 0;
        Dictionary<String, String> possibleValues = parseStringToDictionary(grid);
        this.callback = cl;
        System.out.println("Finding all solutions");
        Runnable task = new TaskDigit(possibleValues, this);
        task.run();
        pool.shutdown();
        waitForPool(tasks);
        cl.notifyEndOfTask();
    }

    private Dictionary<String, String> parseStringToDictionary(String grid){
        Dictionary<String, String> possibleValues = new Hashtable<>();
        String[] sudokuValues = grid.split(",", -1);
        for (int i = 0; i < squares.size(); i++) {
            possibleValues.put(squares.get(i), sudokuValues[i]);
        }
        return  possibleValues;
    }

    public Dictionary<String, String> assign(Dictionary<String, String> valuesP, String square, String digit) {
        boolean result = true;
        String other_values = valuesP.get(square).replace(digit, "");
        for (int i = 0; i < other_values.length(); i++) {
            String tempDigit = String.valueOf(other_values.charAt(i));
            result = result && (eliminate(valuesP, square, tempDigit) != null);
        }
        if (result) {
            return valuesP;
        } else {
            return null; //Simulate a false
        }
    }

    private Dictionary<String, String> eliminate(Dictionary<String, String> valuesP, String square, String digit) {
        if (!valuesP.get(square).contains(digit)) {
            return valuesP; //The digit was already eliminated
        }
        String tempDigits = valuesP.get(square).replace(digit, "");
        valuesP.put(square, tempDigits);
        // If a square is reduced to one value d2, then eliminate d2 from the peers.
        if (valuesP.get(square).length() == 0) {
            //The null simulates a false
            return null; //Contradiction: removed last value
        } else if (valuesP.get(square).length() == 1) { // If there is only one value left in square, remove it from peers
            boolean result = true;
            for (int i = 0; i < peers.get(square).size(); i++) {
                result = result && (eliminate(valuesP, peers.get(square).get(i), valuesP.get(square)) != null);
            }
            if (!result) {
                return null; //Simulate a false
            }
        }
        // If a unit is reduced to only one place for a value d, then put it there.
        for (int u = 0; u < units.get(square).size(); u++) {
            ArrayList<String> dPlaces = new ArrayList<>();
            for (int s = 0; s < units.get(square).get(u).size(); s++) {
                String square2 = units.get(square).get(u).get(s);
                if (valuesP.get(square2).contains(digit)) {
                    dPlaces.add(square2);
                }
            }

            if (dPlaces.size() == 0) {
                //The null simulates a false
                return null; //Contradiction: no place for this value
            } else if (dPlaces.size() == 1) {
                //the digit can only be in one place in unit, assign it there
                if (assign(valuesP, dPlaces.get(0), digit) == null) {
                    return null; //Simulate a false
                }
            }
        }
        return valuesP;
    }

    public boolean search(Dictionary<String, String> valuesP) {
        //"Using depth-first search and propagation, try all possible values."

        if (valuesP == null) {
            return false; //Failed earlier
        }

        //Search the square with the minimum number of digits
        String digits = "123456789";
        int min = digits.length() + 1;
        int max = 1;
        String minSquare = null;
        for (String tempSquare : squares) {
            if (valuesP.get(tempSquare).length() > max) {
                max = valuesP.get(tempSquare).length();
            }
            if (valuesP.get(tempSquare).length() > 1 && valuesP.get(tempSquare).length() < min) { //Maybe if are different squares with the min value then there are multiple solutions
                min = valuesP.get(tempSquare).length();
                minSquare = tempSquare;
            }
        }
        //If all the squares only have one digit
        if (max == 1) {
            try {
                solDicSemaphore.acquire();
                solutionsDic.add(valuesP);
                solDicSemaphore.release();
                sendSolution();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return true; //Solved
        }

        boolean atLeastOneSolution = false;
        for (int i = 0; i < valuesP.get(minSquare).length(); i++) {
            boolean result = search(assign(makeACopyOfValues(valuesP), minSquare, String.valueOf(valuesP.get(minSquare).charAt(i))));
            atLeastOneSolution = atLeastOneSolution || result;
        }
        return atLeastOneSolution; //There is at least one solution
//Simulates a false
    }

    public Dictionary<String, String> makeACopyOfValues(Dictionary<String, String> valuesP) {
        Dictionary<String, String> valuesCopy = new Hashtable<>();
        for (String square : squares) {
            valuesCopy.put(square, valuesP.get(square));
        }
        return valuesCopy;
    }

    private void sendSolution() {
        Runnable task = new TaskDictionary(this, callback);
        tasks++;
        pool.execute(task);
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

    public Semaphore getSolDicSemaphore() {
        return solDicSemaphore;
    }

    public Queue<Dictionary<String, String>> getSolutionsDic() {
        return solutionsDic;
    }

    public List<String> getSquares() {
        return squares;
    }
}
