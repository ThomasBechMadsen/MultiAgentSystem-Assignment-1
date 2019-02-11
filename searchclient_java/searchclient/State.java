package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class State {
	public static class Box{
		public char c;
		public int col;
		public int row;
		
		public Box(char chr, int col, int row) {
			this.c = chr;
			this.col = col;
			this.row = row;
		}
		
		@Override
		protected Object clone() {
			return new Box(c, col, row);
		}
		
		@Override
		public int hashCode() {
			return (c ^ col ^ row);
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == null)
			{
				return false;
			}
			else
			{
				Box other;
				try {
					other = (Box)obj;
				} catch (Exception e) {
					return false;
				}
				return other.c == c && other.col == col && other.row == row;
			}
		}
		
        
	}
    private static final Random RNG = new Random(1);
    
    public static Map map;

    public int agentRow;
    public int agentCol;

    // Arrays are indexed from the top-left of the level, with first index being row and second being column.
    // Row 0: (0,0) (0,1) (0,2) (0,3) ...
    // Row 1: (1,0) (1,1) (1,2) (1,3) ...
    // Row 2: (2,0) (2,1) (2,2) (2,3) ...
    // ...
    // (Start in the top left corner, first go down, then go right)
    // E.g. this.walls[2] is an array of booleans having size MAX_COL.
    // this.walls[row][col] is true if there's a wall at (row, col)
    //

    // List of where all the boxes in this state are located
    // We could have it in a 2D array, but that would have 
    // significant overhead for a limited amount of boxes. 
    public Box[] boxes;

    public State parent;
    public Command action;

    private int g;

    private int _hash = 0;

    public State(State parent) {
        this.parent = parent;
        if (parent == null) {
            this.g = 0;
        } else {
            this.g = parent.g() + 1;
        }
    }

    public int g() {
        return this.g;
    }

    public boolean isInitialState() {
        return this.parent == null;
    }

    public boolean isGoalState() {
    	for(int i = 0; i < boxes.length; ++i)
    	{
    		Box currentBox = boxes[i];
    		if(map.getGoal(currentBox.row, currentBox.col) != Character.toLowerCase(currentBox.c))
    		{
    			return false;
    		}
    	}
        return true;
    }

    public ArrayList<State> getExpandedStates() {
        ArrayList<State> expandedStates = new ArrayList<>(Command.EVERY.length);
        for (Command c : Command.EVERY) {
            // Determine applicability of action
            int newAgentRow = this.agentRow + Command.dirToRowChange(c.dir1);
            int newAgentCol = this.agentCol + Command.dirToColChange(c.dir1);

            if (c.actionType == Command.Type.Move) {
                // Check if there's a wall or box on the cell to which the agent is moving
                if (this.cellIsFree(newAgentRow, newAgentCol)) {
                    State n = this.ChildState();
                    n.action = c;
                    n.agentRow = newAgentRow;
                    n.agentCol = newAgentCol;
                    expandedStates.add(n);
                }
            } else if (c.actionType == Command.Type.Push) {
                // Make sure that there's actually a box to move
                if (this.boxAt(newAgentRow, newAgentCol)) {
                    int newBoxRow = newAgentRow + Command.dirToRowChange(c.dir2);
                    int newBoxCol = newAgentCol + Command.dirToColChange(c.dir2);
                    // .. and that new cell of box is free
                    if (this.cellIsFree(newBoxRow, newBoxCol)) {
                        State n = this.ChildState();
                        n.action = c;
                        n.agentRow = newAgentRow;
                        n.agentCol = newAgentCol;
                        // Update the specific box instance for new state
                        Box movedBox = n.getBox(newAgentRow, newAgentCol);
                        movedBox.col = newBoxCol;
                        movedBox.row = newBoxRow;
                        expandedStates.add(n);
                    }
                }
            } else if (c.actionType == Command.Type.Pull) {
                // Cell is free where agent is going
                if (this.cellIsFree(newAgentRow, newAgentCol)) {
                    int boxRow = this.agentRow + Command.dirToRowChange(c.dir2);
                    int boxCol = this.agentCol + Command.dirToColChange(c.dir2);
                    // .. and there's a box in "dir2" of the agent
                    if (this.boxAt(boxRow, boxCol)) {
                        State n = this.ChildState();
                        n.action = c;
                        n.agentRow = newAgentRow;
                        n.agentCol = newAgentCol;
                        // Update the specific box instance for new state
                        Box movedBox = n.getBox(boxRow, boxCol);
                        movedBox.col = agentCol;
                        movedBox.row = agentRow;
                        expandedStates.add(n);
                    }
                }
            }
        }
        Collections.shuffle(expandedStates, RNG);
        return expandedStates;
    }

    private boolean cellIsFree(int row, int col) {
        return !map.hasWall(row, col) && !boxAt(row, col);
    }
    
    private Box getBox(int row, int col)
    {
    	for (int i = 0; i < boxes.length; i++) {
    		Box currentBox = boxes[i];
    		if(currentBox.col == col && currentBox.row == row)
    		{
    			return currentBox;
    		}
		}
    	return null;
    }
    
    private boolean boxAt(int row, int col) {
    	return getBox(row, col) != null;
    }

    private State ChildState() {
        State copy = new State(this);
        copy.boxes = this.boxes.clone();
        for(int i = 0; i < this.boxes.length; ++i)
        {
            copy.boxes[i] = (State.Box)this.boxes[i].clone(); // Make a deep copy
        }
        return copy;
    }

    public ArrayList<State> extractPlan() {
		ArrayList<State> plan = new ArrayList<>();
        State n = this;
        while (!n.isInitialState()) {
            plan.add(n);
            n = n.parent;
        }
        Collections.reverse(plan);
        return plan;
    }

    @Override
    public int hashCode() {
        if (this._hash == 0) {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.agentCol;
            result = prime * result + this.agentRow;
            result = prime * result + Arrays.deepHashCode(this.boxes);
            this._hash = result;
        }
        return this._hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        State other = (State) obj;
        if (this.agentRow != other.agentRow || this.agentCol != other.agentCol)
            return false;
        return Arrays.deepEquals(this.boxes, other.boxes);
    }

    @Override
    public String toString() {
        // First get the "static" background map
    	StringBuilder maprender = map.GetMapLayout();
    	// Draw agent location
    	int agentOffset = agentRow * (map.max_col + 1) + agentCol;
    	maprender.setCharAt(agentOffset, '0');
    	
    	// Draw box locations
    	for (int i = 0; i < boxes.length; i++) {
    		int boxOffset = boxes[i].row * (map.max_col + 1) + boxes[i].col;
    		maprender.setCharAt(boxOffset, boxes[i].c);
		}
    	
        return maprender.toString();
    }
    
}
