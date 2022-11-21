import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

//Adapted from http://norvig.com/sudoku.html and http://pankaj-k.net/sudoku/sudoku.js
public class Sudoku{

    private final String digits = "123456789";
    private final String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
    private final String[] cols = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private final ArrayList<String> squares;
    private final ArrayList<ArrayList<String>> unitlist;
    private Dictionary<String, ArrayList<ArrayList<String>>> units;
    private Dictionary<String, ArrayList<String>> peers;

    private Dictionary<String, String> values;

    public Sudoku(){
        squares = cross(rows, cols);
        unitlist = new ArrayList<>();
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
            unitlist.add(cross(rows, temp));
        }
        for (int i = 0; i < rows.length; i++) {
            String[] temp = {rows[i]};
            unitlist.add(cross(temp, cols));
        }
        String[][] rrows = {{"A","B","C"}, {"D","E","F"}, {"G","H","I"}};
        String[][] ccols = {{"1", "2", "3"}, {"4","5","6"}, {"7","8","9"}};

        for (int i = 0; i < rrows.length; i++) {
            for (int j = 0; j < ccols.length; j++) {
                unitlist.add(cross(rrows[i], ccols[j]));
            }
        }

        /**
        String[][] rs = {{"ABC"}, {"DEF"}, {"GHI"}};
        String[][] cs = {{"123"}, {"456"}, {"789"}};

        for (int i = 0; i < rs.length; i++) {
            for (int j = 0; j < cs.length; j++) {
                unitlist.add(cross(rs[i], cs[j]));
            }
        }**/
    }

    private void fillUnits(){
        //for s in sqaures
        for (int i = 0; i < squares.size(); i++) {
            String s = squares.get(i);
            units.put(s, new ArrayList<>());
            //u for u in unitlist QUITARRR
            for (int j = 0; j < unitlist.size(); j++) {
                if(isMember(s, unitlist.get(j))){
                    units.get(s).add(unitlist.get(j));
                }
            }
        }
    }

    private void fillPeers(){
        //for s in squares QUITARR
        for (int i = 0; i < squares.size(); i++) {
            String s = squares.get(i);
            ArrayList<String> tempPeers = new ArrayList<>();
            for (int j = 0; j < units.get(s).size(); j++) {
                for (int k = 0; k < units.get(s).get(j).size(); k++) {
                    String tempMiniSquare = units.get(s).get(j).get(k);
                    if(!isMember(tempMiniSquare, tempPeers) && !tempMiniSquare.equals(s)){
                        tempPeers.add(tempMiniSquare);
                    }
                }
            }
            peers.put(s, tempPeers);
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
            if(digits.indexOf(grid2.charAt(i)) >= 0 && !assign(squares.get(i), String.valueOf(grid2.charAt(i)))){
                return false; //Fail if we can't assign the digit to the square.
            }
        }
        return true;
    }

    /**
     * Eliminate all the other values (except digit) from values.get(square) and propagate.
     * @return False if a contradiction is detected
     */
    private boolean assign(String square, String digit){
        boolean result = true;
        String other_values = values.get(square).replace(digit, "");
        for(int i = 0; i < other_values.length(); i++){
            String tempDigit = String.valueOf(other_values.charAt(i));
            result = result && (eliminate(square, tempDigit) ? true : false);
        }
        return result;
    }

    /**
     * Eliminate digit from values.get(square), propagate when values or places <= 2
     * @param square
     * @param digit
     * @return False if a contradiction is detected
     */
    private boolean eliminate(String square, String digit){
        if(!values.get(square).contains(digit)){
            return true; //The digit was already eliminated
        }
        String tempDigits = values.get(square).replace(digit, "");
        values.put(square, tempDigits);
        // If a square is reduced to one value d2, then eliminate d2 from the peers.
        if (values.get(square).length() == 0){
            return false; //Contradiction: removed last value
        } else if (values.get(square).length() == 1) { // If there is only one value left in square, remove it from peers
            boolean result = true;
            for(int i = 0; i < peers.get(square).size(); i++){
                result = result && (eliminate(peers.get(square).get(i), values.get(square)) ? true : false);
            }
            if(!result){
                return false;
            }
        }
        // If a unit is reduced to only one place for a value d, then put it there.
        for (int u = 0; u < units.get(square).size(); u++) {
            ArrayList<String> dplaces = new ArrayList<>();
            for (int s = 0; s < units.get(square).get(u).size(); s++) {
                String square2 = units.get(square).get(u).get(s);
                if(values.get(square2).indexOf(digit) != -1){
                    dplaces.add(square2);
                }
            }

            if (dplaces.size() == 0){
                return false; //Contradiction: no place for this value
            } else if (dplaces.size() == 1) {
                //the digit can only be in one place in unit, assign it there
                if(!assign(dplaces.get(0), digit)){
                    return false;
                }
            }
        }
        return true;
    }

    public String display(){
        String grid = "";
        for (int i = 0, j = 1, k = 1, l = 1; i < squares.size(); i++, j++, k++) {
            if(l == 4){
                grid += "------+------+------\n";
                l = 1;
            }
            if(j == 3){
                if(k == 9){
                    grid += values.get(squares.get(i)) + "\n";
                    k = 0;
                    l ++;
                }else{
                    grid += values.get(squares.get(i)) + " |";
                }
                j = 0;
            }else{
                grid += values.get(squares.get(i)) + " ";
            }
        }
        return grid;
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