import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

//Adapted from http://norvig.com/sudoku.html and http://pankaj-k.net/sudoku/sudoku.js
public class Sudoku{

    private final String digits = "123456789";
    private final String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
    private final String[] cols = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private final List<String> squares;
    private final List<ArrayList<String>> unitList;
    private Dictionary<String, ArrayList<ArrayList<String>>> units;
    private Dictionary<String, ArrayList<String>> peers;

    private Dictionary<String, String> values;

    public Sudoku(){
        squares = cross(rows, cols);
        unitList = new ArrayList<>();
        fillUnitlist();
        units = new Hashtable<>();
        fillUnits();
        peers = new Hashtable<>();
        fillPeers();
        values = new Hashtable<>();
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

    private void fillUnitlist(){
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
        //for s in sqaures
        for (int i = 0; i < squares.size(); i++) {
            String square = squares.get(i);
            units.put(square, new ArrayList<>());
            //u for u in unitList QUITARRR
            for (int j = 0; j < unitList.size(); j++) {
                if(isMember(square, unitList.get(j))){
                    units.get(square).add(unitList.get(j));
                }
            }
        }
    }

    private void fillPeers(){
        //for s in squares QUITARR
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
        //for var s in squares QUITARRR
        for (int i = 0; i < squares.size(); i++) {
            //To start, every square can be any digit; then assign values from the grid.
            values.put(squares.get(i), digits);
        }
        //for var s in squares QUITARRR
        for (int i = 0; i < squares.size(); i++) {
            if(digits.indexOf(grid2.charAt(i)) >= 0 && assign(values, squares.get(i), String.valueOf(grid2.charAt(i))) == null){
                return false; //Fail if we can't assign the digit to the square.
            }
        }
        return true;
    }

    /**
     * Eliminate all the other values (except digit) from values.get(square) and propagate.
     * @return False if a contradiction is detected
     */
    private Dictionary<String, String> assign(Dictionary<String, String> valuesP, String square, String digit){
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
            return null; //Contradiction: removed last value
        } else if (valuesP.get(square).length() == 1) { // If there is only one value left in square, remove it from peers
            boolean result = true;
            for(int i = 0; i < peers.get(square).size(); i++){
                result = result && (eliminate(valuesP, peers.get(square).get(i), valuesP.get(square)) != null ? true : false);
            }
            if(!result){
                return null;
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
                return null; //Contradiction: no place for this value
            } else if (dPlaces.size() == 1) {
                //the digit can only be in one place in unit, assign it there
                if(assign(valuesP, dPlaces.get(0), digit) == null){
                    return null;
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

    private Dictionary<String, String> search(Dictionary<String, String> valuesP){
        //"Using depth-first search and propagation, try all possible values."
        /**
        boolean solved = true;
        for (int i = 0; i < squares.size(); i++) {
            if (values.get(squares.get(i)).length() != 1){
                solved = false;
            }
        }
        if (solved){
            return true;
        }**/
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
        if(max == 1){
            return valuesP; //Solved
        }
        for (int i = 0; i < valuesP.get(minSquare).length(); i++) {
            Dictionary<String, String> result = search(assign(makeACopyOfValues(valuesP), minSquare, String.valueOf(valuesP.get(minSquare).charAt(i))));
            if(result != null){
                return result;
            }
        }
        return null;
    }

    private Dictionary<String, String> makeACopyOfValues(Dictionary<String, String> valuesP){
        Dictionary<String, String> valuesCopy = new Hashtable<>();
        for (int i = 0; i < squares.size(); i++) {
            valuesCopy.put(squares.get(i), valuesP.get(squares.get(i)));
        }
        return valuesCopy;
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
}