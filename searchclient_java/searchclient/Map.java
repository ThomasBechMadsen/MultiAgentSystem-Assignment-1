package searchclient;

public class Map {
    public int max_row;// = 70;
    public int max_col;// = 70;
    public boolean[][] walls;
    public char[][] goals;
    
    public Map(int rows, int cols) {
    	max_row = rows;
    	max_col = cols;
    	walls = new boolean[max_row][max_col];
    	goals = new char[max_row][max_col];
    }
    
}
