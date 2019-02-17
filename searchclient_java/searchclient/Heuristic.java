package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import searchclient.Map.Goal;
import searchclient.State.Box;

public abstract class Heuristic implements Comparator<State> {
	
	private Goal[] goals;
	
    public Heuristic(State initialState) {
    	goals = State.map.getAllGoals();
    }

    public int h(State n) {
    	if(n.cachedHeuristic == -1)
    	{
    		int result = 0;
            Box[] boxes = n.boxes;
        	ArrayList<Box> reservedBoxes = new ArrayList<Box>();
        	Box bestBox = null;
            for(int i = 0; i < goals.length; i++) {
            	int closestScore = Integer.MAX_VALUE;
            	for(int j = 0; j < boxes.length; j++) {
            		Box box = boxes[j];
            		if(goals[i].c != Character.toLowerCase(box.c) || reservedBoxes.contains(box)) {
            			continue;
            		}
            		
            		//Manhattan distance
            		int boxToGoal = Math.abs(goals[i].col - box.col) + Math.abs(goals[i].row - box.row);
            		
            		if(boxToGoal == 0) {
            			closestScore = 0;
            			bestBox = box;
            			break;
            		}
            		else if(boxToGoal < closestScore) {
            			bestBox = box;
            			closestScore = boxToGoal;
            		}
            	}
            	reservedBoxes.add(bestBox);
            	result += closestScore;
            }
            
            int closestBoxDistance = Integer.MAX_VALUE;
            for (Box box : reservedBoxes) {
            	int currentDistance = Math.abs(n.agentCol - box.col) + Math.abs(n.agentRow - box.row);
            	if(currentDistance < closestBoxDistance)
            	{
            		closestBoxDistance = currentDistance;
            	}
			}
            
            n.cachedHeuristic = result + closestBoxDistance;
    	}
        
    	return n.cachedHeuristic;
    }

    public abstract int f(State n);

    @Override
    public int compare(State n1, State n2) {
        return this.f(n1) - this.f(n2);
    }

    public static class AStar extends Heuristic {
        public AStar(State initialState) {
            super(initialState);
        }

        @Override
        public int f(State n) {
            return n.g() + this.h(n);
        }

        @Override
        public String toString() {
            return "A* evaluation";
        }
    }

    public static class WeightedAStar extends Heuristic {
        private int W;

        public WeightedAStar(State initialState, int W) {
            super(initialState);
            this.W = W;
        }

        @Override
        public int f(State n) {
            return n.g() + this.W * this.h(n);
        }

        @Override
        public String toString() {
            return String.format("WA*(%d) evaluation", this.W);
        }
    }

    public static class Greedy extends Heuristic {
        public Greedy(State initialState) {
            super(initialState);
        }

        @Override
        public int f(State n) {
            return this.h(n);
        }

        @Override
        public String toString() {
            return "Greedy evaluation";
        }
    }
}
