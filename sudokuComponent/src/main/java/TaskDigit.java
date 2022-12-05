import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Dictionary;

public class TaskDigit implements Runnable{

    private Dictionary<String, String> values;

    private Sudoku sudoku;

    private PrintWriter printWriter;

    public TaskDigit(Dictionary<String, String> valuesP, Sudoku sudokuP, PrintWriter printWriterP){
        values = valuesP;
        sudoku = sudokuP;
        printWriter = printWriterP;

    }

    @Override
    public void run(){
        System.out.println("Running...");
        sudoku.search(values, printWriter);
    }

}
