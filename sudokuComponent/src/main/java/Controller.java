import java.util.ArrayList;

public class Controller {

    public static void main(String[] args){
        test();
    }

    //BORRARRRR
    public static void test(){
        Sudoku sudo = new Sudoku();
        String test_grid1 = "003020600900305001001806400008102900700000008006708200002609500800203009005010300";
        String test_grid2 = "4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4......";
        boolean result = sudo.parseGrid(test_grid2);
        System.out.println(result);
        if(result){
            System.out.println(sudo.display());
        }
        ArrayList<ArrayList<String>> units = sudo.getUnits().get("C2");
        ArrayList<String> peers = sudo.getPeers().get("C2");
        System.out.println("hi :)");

    }

}
