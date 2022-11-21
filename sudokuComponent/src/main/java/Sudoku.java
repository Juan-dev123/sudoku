import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class Sudoku{

    private String digits = "123456789";
    private String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
    private String[] cols = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private ArrayList<String> squares;
    private ArrayList<ArrayList<String>> unitlist;
    private Dictionary<String, ArrayList<ArrayList<String>>> units;
    private Dictionary<String, ArrayList<String>> peers;

    public Sudoku(){
        squares = cross(rows, cols);
        unitlist = new ArrayList<>();
        fillUnitlist();
        units = new Hashtable<>();
        fillUnits();
        peers = new Hashtable<>();
        fillPeers();
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