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
        int result = 0;
        Box[] boxes = n.boxes;
        
        for(int i = 0; i < goals.length; i++) {
        	int closestScore = -1;
        	Box reservedBox = null;
        	for(int j = 0; j < boxes.length; j++) {
        		Box box = boxes[j];
        		if(goals[i].c != Character.toLowerCase(box.c) || boxes[j] == reservedBox) {
        			continue;
        		}
        		
        		//Manhattan distance
        		int agentToBox = Math.abs(n.agentCol - box.col) + Math.abs(n.agentRow - box.row);
        		int boxToGoal = Math.abs(goals[i].col - box.col) + Math.abs(goals[i].row - box.row);
        		
        		if(boxToGoal == 0) {
        			closestScore = 0;
        			break;
        		}
        		else if(closestScore == -1 || agentToBox + boxToGoal < closestScore) {
        			reservedBox = box;
        			closestScore = agentToBox + boxToGoal;
        		}
        	}
        	result += closestScore;
        }
        //System.err.println(result);
        return result;
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
