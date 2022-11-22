import java.util.ArrayList;

public class SudokuComponent {

    public static void main(String[] args){
        test();
    }

    //BORRARRRR
    public static void test(){
        Sudoku sudoku = new Sudoku();
        ArrayList<ArrayList<String>> units = sudoku.getUnits().get("C2");
        ArrayList<String> peers = sudoku.getPeers().get("C2");
        System.out.println("hi :)");

        String test_grid1 = "0ññ03k020600900305001001806400008102900700000008006708200002609500800203009005010300";
        boolean result = sudoku.parseGrid(test_grid1);


        String test_grid2 = "4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4......";
        String test_grid3 = "85...24..72......9..4.........1.7..23.5...9...4...........8..7..17..........36.4.";
        System.out.println(sudoku.solve(test_grid1));

    }

}
