import com.zeroc.IceInternal.ThreadPool;

import java.util.*;
import java.util.concurrent.*;

//Adapted from http://norvig.com/sudoku.html and http://pankaj-k.net/sudoku/sudoku.js
public class Sudoku{

    static final int MAX_THREADS = 12;

    static final int MAX_QUEUE_SIZE = 100;
    private final String digits = "123456789";
    private final String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
    private final String[] cols = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private final List<String> squares;
    private final List<ArrayList<String>> unitList;
    private Dictionary<String, ArrayList<ArrayList<String>>> units;
    private Dictionary<String, ArrayList<String>> peers;

    private Dictionary<String, String> values;

    private Queue<Dictionary<String, String>> solutionsDic;

    private Queue<String> solutionsStr;

    private ArrayList<String> solutions;

    private ThreadPoolExecutor pool;

    private Semaphore poolSemaphore;

    private Semaphore solDicSemaphore;

    private Semaphore solStrSemaphore;

    private String outputMessage;

    private Semaphore messageSemaphore;



    public Sudoku(){
        squares = cross(rows, cols);
        unitList = new ArrayList<>();
        fillUnitList();
        units = new Hashtable<>();
        fillUnits();
        peers = new Hashtable<>();
        fillPeers();
        values = new Hashtable<>();
        solutionsDic = new LinkedList<>();
        solutionsStr = new LinkedList<>();
        solutions = new ArrayList<>();
        pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS);
        poolSemaphore = new Semaphore(1);
        solDicSemaphore = new Semaphore(1);
        solStrSemaphore = new Semaphore(1);
        messageSemaphore = new Semaphore(1);
        outputMessage = "";
    }

    private ArrayList<String> cross(String[] rowsP, String[] colsP){
        ArrayList<String> longRow = new ArrayList<>();
        for(int i = 0; i < rowsP.length; i++){
            for(int j = 0; j < colsP.length; j++){
                longRow.add(rowsP[i]+colsP[j]);
            }
        }
        return longRow;
    }

    private void fillUnitList(){
        //for c in cols
        for (int i = 0; i < cols.length; i++) {
            String[] temp = {cols[i]};
            unitList.add(cross(rows, temp));
        }
        for (int i = 0; i < rows.length; i++) {
            String[] temp = {rows[i]};
            unitList.add(cross(temp, cols));
        }
        String[][] rrows = {{"A","B","C"}, {"D","E","F"}, {"G","H","I"}};
        String[][] ccols = {{"1", "2", "3"}, {"4","5","6"}, {"7","8","9"}};

        for (int i = 0; i < rrows.length; i++) {
            for (int j = 0; j < ccols.length; j++) {
                unitList.add(cross(rrows[i], ccols[j]));
            }
        }
    }

    private void fillUnits(){
        for (int i = 0; i < squares.size(); i++) {
            String square = squares.get(i);
            units.put(square, new ArrayList<>());
            for (int j = 0; j < unitList.size(); j++) {
                if(isMember(square, unitList.get(j))){
                    units.get(square).add(unitList.get(j));
                }
            }
        }
    }

    private void fillPeers(){
        for (int i = 0; i < squares.size(); i++) {
            String square = squares.get(i);
            ArrayList<String> tempPeers = new ArrayList<>();
            for (int j = 0; j < units.get(square).size(); j++) {
                for (int k = 0; k < units.get(square).get(j).size(); k++) {
                    String tempMiniSquare = units.get(square).get(j).get(k);
                    if(!isMember(tempMiniSquare, tempPeers) && !tempMiniSquare.equals(square)){
                        tempPeers.add(tempMiniSquare);
                    }
                }
            }
            peers.put(square, tempPeers);
        }
    }

    public boolean parseGrid(String grid){
        String grid2 = "";
        for (int i = 0; i < grid.length(); i++) {
            if("0.,-123456789".indexOf(grid.charAt(i)) >= 0){
                grid2 += grid.charAt(i);
            }
        }
        for (int i = 0; i < squares.size(); i++) {
            //To start, every square can be any digit; then assign values from the grid.
            values.put(squares.get(i), digits);
        }
        for (int i = 0; i < squares.size(); i++) {
            //If the value in the square is between 1 and 9 and the value can''t be assigned then return false
            if(digits.indexOf(grid2.charAt(i)) >= 0 && assign(values, squares.get(i), String.valueOf(grid2.charAt(i))) == null){
                return false; //Fail if we can't assign the digit to the square.
            }
        }
        return true;
    }

    /**
     * Eliminate all the other values (except digit) from values.get(square) and propagate.
     * @return Null if a contradiction is detected
     */
    public Dictionary<String, String> assign(Dictionary<String, String> valuesP, String square, String digit){
        boolean result = true;
        String other_values = valuesP.get(square).replace(digit, "");
        for(int i = 0; i < other_values.length(); i++){
            String tempDigit = String.valueOf(other_values.charAt(i));
            result = result && (eliminate(valuesP, square, tempDigit) != null ? true : false);
        }
        if (result){
            return valuesP;
        }else {
            return null; //Simulate a false
        }
    }

    /**
     * Eliminate digit from values.get(square), propagate when values or places <= 2
     * @param square
     * @param digit
     * @return False if a contradiction is detected
     */
    private Dictionary<String, String> eliminate(Dictionary<String, String> valuesP, String square, String digit){
        if(!valuesP.get(square).contains(digit)){
            return valuesP; //The digit was already eliminated
        }
        String tempDigits = valuesP.get(square).replace(digit, "");
        valuesP.put(square, tempDigits);
        // If a square is reduced to one value d2, then eliminate d2 from the peers.
        if (valuesP.get(square).length() == 0){
            //The null simulates a false
            return null; //Contradiction: removed last value
        } else if (valuesP.get(square).length() == 1) { // If there is only one value left in square, remove it from peers
            boolean result = true;
            for(int i = 0; i < peers.get(square).size(); i++){
                result = result && (eliminate(valuesP, peers.get(square).get(i), valuesP.get(square)) != null ? true : false);
            }
            if(!result){
                return null; //Simulate a false
            }
        }
        // If a unit is reduced to only one place for a value d, then put it there.
        for (int u = 0; u < units.get(square).size(); u++) {
            ArrayList<String> dPlaces = new ArrayList<>();
            for (int s = 0; s < units.get(square).get(u).size(); s++) {
                String square2 = units.get(square).get(u).get(s);
                if(valuesP.get(square2).indexOf(digit) != -1){
                    dPlaces.add(square2);
                }
            }

            if (dPlaces.size() == 0){
                //The null simulates a false
                return null; //Contradiction: no place for this value
            } else if (dPlaces.size() == 1) {
                //the digit can only be in one place in unit, assign it there
                if(assign(valuesP, dPlaces.get(0), digit) == null){
                    return null; //Simulate a false
                }
            }
        }
        return valuesP;
    }

    public String display(Dictionary<String, String> valuesP){
        String grid = "";
        for (int i = 0, j = 1, k = 1, l = 1; i < squares.size(); i++, j++, k++) {
            if(l == 4){
                grid += "------+------+------\n";
                l = 1;
            }
            if(j == 3){
                if(k == 9){
                    grid += valuesP.get(squares.get(i)) + "\n";
                    k = 0;
                    l ++;
                }else{
                    grid += valuesP.get(squares.get(i)) + " |";
                }
                j = 0;
            }else{
                grid += valuesP.get(squares.get(i)) + " ";
            }
        }
        return grid;
    }

    public String solve(String grid){
        boolean allIsGood = parseGrid(grid);
        if(allIsGood){
            Dictionary<String, String> solution = search(values);
            return display(solution);
        }else{
            return "Error";
        }
    }

    public void tempSolve(String grid){
        boolean allIsGood = parseGrid(grid);
        if(allIsGood) {
            findAllSolutions();
            parseSolutionToString();
            checkUniqueSolution();
            makeGrids();
        }

    }
    private void findAllSolutions(){
        try {
            poolSemaphore.acquire();
            for (int i = 0; i < squares.size(); i++) {
                String tempSquare = squares.get(i);
                String tempDigits = values.get(tempSquare);
                if(tempDigits.length() > 1){
                    for (int j = 0; j < tempDigits.length(); j++) {
                        Runnable task = new TaskDigit(values, tempDigits.charAt(j), tempSquare, this);
                        pool.execute(task);
                    }
                }
            }
            poolSemaphore.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        pool.shutdown();
        waitForPool();
        System.out.println("jeje");
    }

    private void waitForPool(){
        try {
            poolSemaphore.acquire();
            while (pool.getQueue().size() > 0 || pool.getActiveCount() > 0){
                poolSemaphore.release();
                Thread.yield();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void parseSolutionToString(){
        try {
            poolSemaphore.acquire();
            pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS);
            int initialSize = solutionsDic.size();
            for (int i = 0; i < initialSize; i++) {
                Runnable task = new TaskDictionary(this);
                pool.execute(task);
            }
            poolSemaphore.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        pool.shutdown();
        waitForPool();
        System.out.println("jeje");
    }

    private void checkUniqueSolution(){
        //It would be faster if fork join is used
        int originalSize = solutionsStr.size();
        for (int i = 0; i < originalSize; i++) {
            String possibleSolution = solutionsStr.poll();
            if(!solutions.contains(possibleSolution)){
                solutions.add(possibleSolution);
            }
        }
        System.out.println("jeje");
    }

    private void makeGrids(){
        try {
            poolSemaphore.acquire();
            pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS);
            int numSolutions = solutions.size();
            if(numSolutions == 0){
                outputMessage += "There is no solution for this sudoku\n";
            }else{
                if(numSolutions == 1){
                    outputMessage += "There is 1 solution for this sudoku\n";
                }else{
                    outputMessage += "There are " + numSolutions + " solutions for this sudoku\n";
                }
                for (int i = 0; i < solutions.size(); i++) {
                    Runnable task = new TaskGrid(this, solutions.get(i));
                    pool.execute(task);
                }
            }
            poolSemaphore.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        pool.shutdown();
        waitForPool();
        System.out.println("jeje");
    }

    public void addGrid(String grid){
        try {
            messageSemaphore.acquire();
            outputMessage += grid+"\n";
            messageSemaphore.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Dictionary<String, String> search(Dictionary<String, String> valuesP){
        //"Using depth-first search and propagation, try all possible values."

        //Search the square with the minimum number of digits
        if (valuesP == null){
            return null;
        }
        int min = digits.length()+1;
        int max = 1;
        String minSquare = null;
        for (int i = 0; i < squares.size(); i++) {
            String tempSquare = squares.get(i);
            if(valuesP.get(tempSquare).length() > max){
                max = valuesP.get(tempSquare).length();
            }
            if(valuesP.get(tempSquare).length() > 1 && valuesP.get(tempSquare).length() < min){ //Maybe if are different squares with the min value then there are multiple solutions
                min = valuesP.get(tempSquare).length();
                minSquare = tempSquare;
            }
        }
        //If all the squares only have one digit
        if(max == 1){
            return valuesP; //Solved
        }
        for (int i = 0; i < valuesP.get(minSquare).length(); i++) {
            Dictionary<String, String> result = search(assign(makeACopyOfValues(valuesP), minSquare, String.valueOf(valuesP.get(minSquare).charAt(i))));
            if(result != null){
                return result;
            }
        }
        return null; //Simulates a false
    }

    public Dictionary<String, String> makeACopyOfValues(Dictionary<String, String> valuesP){
        Dictionary<String, String> valuesCopy = new Hashtable<>();
        for (int i = 0; i < squares.size(); i++) {
            valuesCopy.put(squares.get(i), valuesP.get(squares.get(i)));
        }
        return valuesCopy;
    }

    public void addPossibleSolution(Dictionary<String, String> solution){
        solutionsDic.add(solution);
    }

    public void addPossibleSolution(String solution){
        solutionsStr.add(solution);
    }

    public void addSolution(String solution){
        solutions.add(solution);
    }

    private boolean isMember (String item, ArrayList<String> list){
        return list.contains(item);
    }

    public Dictionary<String, ArrayList<ArrayList<String>>> getUnits() {
        return units;
    }

    public Dictionary<String, ArrayList<String>> getPeers() {
        return peers;
    }

    public Dictionary<String, String> getValues() {
        return values;
    }

    public Queue<Dictionary<String, String>> getSolutionsDic() {
        return solutionsDic;
    }

    public List<String> getSquares() {
        return squares;
    }

    public void addTaskToPool(Runnable task) {
        pool.execute(task);
    }

    private void waitPossibleSolutions(){
        while (solutionsDic.size() > 0){
            Thread.yield();
            System.out.println("waiting possible solutions");
            System.out.println(solutionsDic.size());
            System.out.println(pool.getQueue().size());
        }
    }

    private void waitSolutions(){
        while (solutionsStr.size() > 0){
            Thread.yield();
            System.out.println("waiting solutions");
            System.out.println(solutionsStr.size());
            System.out.println(pool.getQueue().size());
        }
    }

    public Queue<String> getSolutionsStr() {
        return solutionsStr;
    }

    public ArrayList<String> getSolutions() {
        return solutions;
    }

    public ThreadPoolExecutor getPool() {
        return pool;
    }

    public Semaphore getSolDicSemaphore() {
        return solDicSemaphore;
    }

    public Semaphore getSolStrSemaphore() {
        return solStrSemaphore;
    }
}