import java.io.*;
import java.util.*;
import java.util.concurrent.*;

//Adapted from http://norvig.com/sudoku.html and http://pankaj-k.net/sudoku/sudoku.js
public class Sudoku {

    public static final String PATH_SOLUTIONS = "sudokuComponent/src/main/resources/solutions.txt";
    public static final String PATH_FINAL = "sudokuComponent/src/main/resources/solutionsFinal.txt";
    static final int MAX_THREADS = 12;

    private final String digits = "123456789";
    private final String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
    private final String[] cols = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private final List<String> squares;
    private final List<ArrayList<String>> unitList;
    private final Dictionary<String, ArrayList<ArrayList<String>>> units;
    private final Dictionary<String, ArrayList<String>> peers;

    private final Dictionary<String, String> values;

    private final Queue<Dictionary<String, String>> solutionsDic;

    private final ThreadPoolExecutor pool;

    private final Semaphore poolSemaphore;

    private final Semaphore solDicSemaphore;

    private final Semaphore messageSemaphore;

    private int tasks;


    public Sudoku() {
        squares = cross(rows, cols);
        unitList = new ArrayList<>();
        fillUnitList();
        units = new Hashtable<>();
        fillUnits();
        peers = new Hashtable<>();
        fillPeers();
        values = new Hashtable<>();
        solutionsDic = new LinkedList<>();
        pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS);
        poolSemaphore = new Semaphore(1);
        solDicSemaphore = new Semaphore(1);
        messageSemaphore = new Semaphore(1);
        tasks = 0;
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


    public boolean parseGrid(String[][] grid) {

        for (String square : squares) {
            //To start, every square can be any digit; then assign values from the grid.
            values.put(square, digits);
        }
        try {
            for (int i = 0, k = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[i].length; j++, k++) {
                    String value = grid[i][j];
                    //If the value in the square is between 1 and 9 and the value can''t be assigned then return false
                    if (digits.contains(value) && assign(values, squares.get(k), value) == null) {
                        return false; //Fail if we can't assign the digit to the square.
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }

    /**
     * Eliminate all the other values (except digit) from values.get(square) and propagate.
     *
     * @return Null if a contradiction is detected
     */
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

    /**
     * Eliminate digit from values.get(square), propagate when values or places <= 2
     *
     * @return False if a contradiction is detected
     */
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

    public void solve(String[][] grid) {
        boolean allIsGood = parseGrid(grid);
        try {
            PrintWriter printWriter = new PrintWriter(PATH_SOLUTIONS);
            if (allIsGood) {
                writeText(printWriter, "");
                    findAllSolutions(printWriter);
                    pool.shutdown();
                    waitForPool(tasks);
            }
            printWriter.close();
            organizeFile();
        } catch (FileNotFoundException e) {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void findAllSolutions(PrintWriter printWriter) {
        System.out.println("Finding all solutions");
        Runnable task = new TaskDigit(values, this, printWriter);
        task.run();
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

    private void parseSolutionToString(PrintWriter printWriter) {
        Runnable task = new TaskDictionary(this, printWriter);
        tasks++;
        pool.execute(task);
    }

    private String getNumberOfSolutions() {

        if (tasks == 0) {
            return "There is no solution for this sudoku\n";
        }
        if (tasks == 1) {
            return "There is 1 solution for this sudoku\n";
        } else {
            return "There are " + tasks + " solutions for this sudoku\n";
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

    public boolean search(Dictionary<String, String> valuesP, PrintWriter printWriter) {
        //"Using depth-first search and propagation, try all possible values."

        if (valuesP == null) {
            return false; //Failed earlier
        }

        //Search the square with the minimum number of digits
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
                parseSolutionToString(printWriter);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return true; //Solved
        }

        boolean atLeastOneSolution = false;
        for (int i = 0; i < valuesP.get(minSquare).length(); i++) {
            boolean result = search(assign(makeACopyOfValues(valuesP), minSquare, String.valueOf(valuesP.get(minSquare).charAt(i))), printWriter);
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

    private boolean isMember(String item, ArrayList<String> list) {
        return list.contains(item);
    }

    public Queue<Dictionary<String, String>> getSolutionsDic() {
        return solutionsDic;
    }

    public List<String> getSquares() {
        return squares;
    }

    public Semaphore getSolDicSemaphore() {
        return solDicSemaphore;
    }
}