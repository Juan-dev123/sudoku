import java.util.ArrayList;

public class Controller {

    public static void main(String[] args){
        Sudoku sudo = new Sudoku();
        ArrayList<ArrayList<String>> units = sudo.getUnits().get("C2");
        ArrayList<String> peers = sudo.getPeers().get("C2");
        System.out.println("hi :)");
    }

}
