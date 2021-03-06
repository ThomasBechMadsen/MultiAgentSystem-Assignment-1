package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import searchclient.State.Box;

public class Map {
    public int max_row;// = 70;
    public int max_col;// = 70;
    // Size goals can only be 'a'..'z' we make use of 0xFF for walls
    private char[][] tile;
    private final char kWall = 0xFF;
    
    public int goals = 0;
    
    public Map(int rows, int cols) {
    	max_row = rows;
    	max_col = cols;
    	tile = new char[max_row][max_col];
    }
    
    public void setWall(int row, int col)
    {
    	tile[row][col] = kWall;
    }
    
    public boolean hasWall(int row, int col)
    {
    	return tile[row][col] == kWall;
    }
    
    public char getGoal(int row, int col)
    {
    	return tile[row][col];
    }
    
    public void setGoal(int row, int col, char c)
    {
    	tile[row][col] = c;
    }
    
    public Goal[] getAllGoals() {
    	ArrayList<Goal> goals = new ArrayList<Goal>();
    	for (int row = 0; row < max_row; row++) {
            for (int col = 0; col < max_col; col++) {
            	if (getGoal(row, col) >= 'a' && getGoal(row, col) <= 'z') {
                    goals.add(new Goal(tile[row][col], col, row));
                }
            }
        }
    	return goals.toArray(new Goal[0]);
    }
    
    public StringBuilder GetMapLayout() {
    	StringBuilder s = new StringBuilder();
    	for (int row = 0; row < max_row; row++) {
            for (int col = 0; col < max_col; col++) {
            	if (getGoal(row, col) >= 'a' && getGoal(row, col) <= 'z') {
                    s.append(getGoal(row, col));
                } else if (hasWall(row, col)) {
                    s.append("+");
                } else {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
    	return s;
    }

    @Override
    public String toString()
    {
        return String.format("Map with size [%d;%d]", max_row, max_col); 
    }
    
    public static class Goal{
		public char c;
		public int col;
		public int row;
		
		public Goal(char chr, int col, int row) {
			this.c = chr;
			this.col = col;
			this.row = row;
		} 
    }
}