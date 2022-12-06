
module Demo
{
    interface Printer
    {
        void printString(string s);
    }

    interface Callback{
    	void addSolution(string solution);
    	void notifyEndOfTask();
    }

    interface Solver{
        void findSolutions(string grid, Callback* cl);
    }

}