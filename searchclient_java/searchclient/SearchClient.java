package searchclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import searchclient.State.Box;

public class SearchClient {
    public State initialState;

    public SearchClient(BufferedReader serverMessages) throws Exception {
        // Read lines specifying colors
        String line = serverMessages.readLine();
        if (line.matches("^[a-z]+:\\s*[0-9A-Z](\\s*,\\s*[0-9A-Z])*\\s*$")) {
            System.err.println("Error, client does not support colors.");
            System.exit(1);
        }
        
        ArrayList<String> input = new ArrayList<String>();

        int colSize = line.length();
        while (!line.equals("")) {
        	input.add(line);
            line = serverMessages.readLine();
            if(colSize < line.length())
            {
            	colSize = line.length();
            }
        }
        Map map = new Map(input.size(), colSize);
        State.map = map;
        this.initialState = new State(null);
        
        boolean agentFound = false;
        ArrayList<Box> boxes = new ArrayList<Box>();
        System.err.printf("Map size: (%d, %d)\n", input.size(), colSize);
        for(int row = 0; row < input.size(); row++) {
        	for (int col = 0; col < input.get(row).length(); col++) {
                char chr = input.get(row).charAt(col);

                if (chr == '+') { // Wall.
                    State.map.setWall(row, col);
                } else if ('0' <= chr && chr <= '9') { // Agent.
                    if (agentFound) {
                        System.err.println("Error, not a single agent level");
                        System.exit(1);
                    }
                    agentFound = true;
                    this.initialState.agentRow = row;
                    this.initialState.agentCol = col;
                } else if ('A' <= chr && chr <= 'Z') { // Box.
                	boxes.add(new State.Box(chr, col, row));
                } else if ('a' <= chr && chr <= 'z') { // Goal.
                	State.map.setGoal(row, col, chr);
                	State.map.goals++;
                } else if (chr == ' ') {
                    // Free space.
                } else {
                    System.err.println("Error, read invalid level character: " + (int) chr);
                    System.exit(1);
                }
            }
        }
        this.initialState.boxes = boxes.toArray(new Box[0]);
        System.err.println("Goals: " + State.map.goals);
        System.err.println(initialState);
    }

    public ArrayList<State> Search(Strategy strategy) {
        System.err.format("Search starting with strategy %s.\n", strategy.toString());
        strategy.addToFrontier(this.initialState);

        int iterations = 0;
        while (true) {
            if (iterations == 1000) {
                System.err.println(strategy.searchStatus());
                iterations = 0;
            }

            if (strategy.frontierIsEmpty()) {
                return null;
            }
            State leafState = strategy.getAndRemoveLeaf();
            //System.err.println(leafState.toString());
            //System.err.println("f(): " + ((Strategy.StrategyBestFirst) strategy).heuristic.f(leafState));

            if (leafState.isGoalState()) {
                return leafState.extractPlan();
            }
            strategy.addToExplored(leafState);
            try {
            	for (State n : leafState.getExpandedStates()) { // The list of expanded states is shuffled randomly; see State.java.
                    if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
                        strategy.addToFrontier(n);
                    }
                }
                //System.err.println("Frontier: " + ((Strategy.StrategyBestFirst) strategy).countFrontier());
                //System.err.println("Explored: " + ((Strategy.StrategyBestFirst) strategy).countExplored());
			} catch (Exception e) {
				System.err.println("Failed to expand state:\n" + leafState);
				throw e;
			}
            iterations++;
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));

        // Use stderr to print to console
        System.err.println("SearchClient initializing. I am sending this using the error output stream.");

        // Read level and create the initial state of the problem
        SearchClient client = new SearchClient(serverMessages);

        Strategy strategy;
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "-bfs":
                    strategy = new Strategy.StrategyBFS();
                    break;
                case "-dfs":
                    strategy = new Strategy.StrategyDFS();
                    break;
                case "-astar":
                    strategy = new Strategy.StrategyBestFirst(new Heuristic.AStar(client.initialState));
                    break;
                case "-wastar":
                    strategy = new Strategy.StrategyBestFirst(new Heuristic.WeightedAStar(client.initialState, 5));
                    break;
                case "-greedy":
                    strategy = new Strategy.StrategyBestFirst(new Heuristic.Greedy(client.initialState));
                    break;
                default:
                    strategy = new Strategy.StrategyBFS();
                    System.err.println("Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or -greedy to set the search strategy.");
            }
        } else {
            strategy = new Strategy.StrategyBFS();
            System.err.println("Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or -greedy to set the search strategy.");
        }

		ArrayList<State> solution;
        try {
            solution = client.Search(strategy);
        } catch (OutOfMemoryError ex) {
            System.err.println("Maximum memory usage exceeded.");
            solution = null;
        }

        if (solution == null) {
            System.err.println(strategy.searchStatus());
            System.err.println("Unable to solve level.");
            System.exit(0);
        } else {
            System.err.println("\nSummary for " + strategy.toString());
            System.err.println("Found solution of length " + solution.size());
            System.err.println(strategy.searchStatus());

            for (State n : solution) {
                String act = n.action.toString();
                //System.err.printf("Action: %s\n", act);
                //System.err.println(n);
                System.out.println(act);
                String response = serverMessages.readLine();
                if (response.contains("false")) {
                    System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, act);
                    System.err.format("%s was attempted in \n%s\n", act, n.toString());
                    break;
                }
            }
        }
    }
}
