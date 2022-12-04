import java.io.PrintWriter;

public class TaskGrid implements Runnable {

    private Sudoku sudoku;

    private String text;

    private PrintWriter printWriter;

    public TaskGrid(Sudoku sudokuP, String textP, PrintWriter printWriterP){
        sudoku = sudokuP;
        text = textP;
        printWriter = printWriterP;
    }

    @Override
    public void run() {
//        String grid = convertStringToGrid(text);
        sudoku.addGrid(text, printWriter);
    }

    private String convertStringToGrid(String representationOfGrid){
        String grid = "";
        for (int i = 0, j = 1, k = 1, l = 1; i < representationOfGrid.length(); i++, j++, k++){
            if(l == 4){
                grid += "------+------+------\n";
                l = 1;
            }
            if(j == 3){
                if(k == 9){
                    grid += representationOfGrid.charAt(i) + "\n";
                    k = 0;
                    l++;
                }else{
                    grid += representationOfGrid.charAt(i) + " | ";
                }
                j = 0;
            }else {
                grid += representationOfGrid.charAt(i) + " ";
            }
        }
        return grid;
    }
}
