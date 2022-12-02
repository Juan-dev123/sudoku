import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.LinkedList;
import java.util.Queue;

public class SudokuComponent {

    public static void main(String[] args){
        test();
    }

    //BORRARRRR
    public static void test(){
        Sudoku sudoku = new Sudoku();

        String test_grid1 = "070020050003000000000030900000000000400000090000170003005000007300086000001000000";
        String test_grid2 = "4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4......";
        String test_grid3 = "85...24..72......9..4.........1.7..23.5...9...4...........8..7..17..........36.4.";

        sudoku.tempSolve(test_grid3);
        Queue<Dictionary<String, String>> solutionsDic = sudoku.getSolutionsDic();

        //System.out.println(sudoku.display(sudoku.getValues()));



        //System.out.println(sudoku.solve(test_grid1));

    }

}
