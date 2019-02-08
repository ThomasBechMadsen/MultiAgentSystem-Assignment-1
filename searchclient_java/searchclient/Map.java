package searchclient;

public class Map {
    public int max_row;// = 70;
    public int max_col;// = 70;
    private boolean[][] walls;
    private char[][] goals;
    
    public Map(int rows, int cols) {
    	max_row = rows;
    	max_col = cols;
    	walls = new boolean[max_row][max_col];
    	goals = new char[max_row][max_col];
    }
    
    public void setWall(int row, int col)
    {
    	walls[row][col] = true;
    }
    
    public boolean hasWall(int row, int col)
    {
    	return walls[row][col];
    }
    
    public char getGoal(int row, int col)
    {
    	return goals[row][col];
    }
    
    public void setGoal(int row, int col, char c)
    {
    	goals[row][col] = c;
    }
    
    public StringBuilder GetMapLayout() {
    	StringBuilder s = new StringBuilder();
    	for (int row = 0; row < max_row; row++) {
            if (!walls[row][0]) {
                break;
            }
            for (int col = 0; col < max_col; col++) {
                //TODO: Write boxes
            	if (goals[row][col] > 0) {
                    s.append(goals[row][col]);
                } else if (walls[row][col]) {
                    s.append("+");
                } else {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
    	return s;
    }
    
}
