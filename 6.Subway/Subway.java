import java.io.*;
import java.util.*;

public class Subway {
	private static final int TRANSFER_TIME = 5;
	// Key: Station ID, Value: Station node
	private static HashMap<String, Node> idTable = new HashMap<>();
	// Key: Station name, Value: List of station nodes (If transfer station, many nodes belong to the same name)
	private static HashMap<String, LinkedList<Node>> nameTable = new HashMap<>();
	
	
	public static void main(String args[]) {
		// Read data file and save node & edge information
		readData(args[0]);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				String input = br.readLine();
				if (input.compareTo("QUIT") == 0)
					break;
				
				// Initialize nodes
				clearNodes();
				// Find and print the shortest path from source station to destination station
				String nodes[] = input.split(" ");
				findShortestPath(nodes[0], nodes[1]);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/* Uses Dijkstra algorithm to find shortest path from source station to destination station */
	private static void findShortestPath(String srcStation, String dstStation) {
		// Both source and destination nodes may be transfer station
		List<Node> srcNodes = nameTable.get(srcStation);
		List<Node> dstNodes = nameTable.get(dstStation);
		
		// Priority queue to save unsettled nodes that are adjacent to current settled nodes
		PriorityQueue<Node> unsettled = new PriorityQueue<>();
		// Initialize nodes and priority queue
		for(Node node: srcNodes) {
			node.setDist(0);
		}
		unsettled.addAll(srcNodes);
		
		
		// Every |Node|/100 iterations, check if early termination is possible
		int iter = 1;
		int checkEvery = idTable.size() / 100 + 1;
		
		while(!unsettled.isEmpty()) {
			// Find the node that has minimum distance value, which will become the new settled node.
			Node minNode = unsettled.poll();
			minNode.setSettled(true);
			// Relaxation on adjacent nodes (if possible)
			for(Edge edge: minNode.getEdges()) {
				Node adjNode = edge.getEndNode();
				long newDist = minNode.getDist() + edge.getDist();
				if(adjNode.getDist() > newDist) {
					adjNode.setDist(newDist);
					adjNode.setPrev(minNode);
					// Update priority queue (distance value of the node) if relaxation occurred
					unsettled.remove(adjNode);
					unsettled.add(adjNode);
				}
			}
			
			// If all destination nodes are settled, early termination
			if(iter % checkEvery == 0) {
				boolean terminate = true;
				for(Node node: dstNodes) {
					if(!node.isSettled()) {
						terminate = false;
						break;
					}
				}
				if(terminate) break;
			}
			iter++;
		}
		
		printPath(dstNodes);
	}
	
	/* Print the calculated shortest path */
	private static void printPath(List<Node> dstNodes) {
		// Destination node that has minimum distance is the real destination of shortest path
		Node dstNode = Collections.min(dstNodes);
		StringBuilder sb = new StringBuilder();
		// Backtrack the path, from the destination node to the source node
		Node curr = dstNode;
		while(curr != null) {
			Node prev = curr.getPrev();
			// If transfer node, use [NAME] format
			if(prev != null && curr.getName().equals(prev.getName())) {
				sb.insert(0, String.format("[%s] ", curr.getName()));
				curr = prev.getPrev();
			}
			// If not transfer node, just print the name
			else {
				sb.insert(0, curr.getName()+" ");
				curr = prev;
			}
		}
		System.out.println(sb.toString().trim());
		System.out.println(dstNode.getDist());
	}
	
	/* Clear all nodes:
	 * Make distance value, settled value, previous node to the initial setting */
	private static void clearNodes() {
		for(Node node: idTable.values()) {
			node.setDist(Long.MAX_VALUE);
			node.setSettled(false);
			node.setPrev(null);
		}
	}
	
	/* Read the data file.
	 * Data file consists of 1.station information 2.Distance(time) between adjacent stations
	 * Create nodes based on 1, create edges based on 2 */
	private static void readData(String file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));

			String inputLine;
			// Node (station) information
			while((inputLine = br.readLine()) != null) {
				if(inputLine.isBlank()) break;
				
				String info[] = inputLine.split(" ");
				String id = info[0];
				String name = info[1];
				String line = info[2];
				Node newNode = new Node(id, name, line);
				
				// Add the node to the id table (key: id, value: node)
				idTable.put(id, newNode);
				// Add the node to the name table (key: name, value: list of nodes)
				LinkedList<Node> nodes = nameTable.get(name);
				// If node with that name doesn't exist in the name table, create new list
				if(nodes == null) {
					nodes = new LinkedList<>();
					nodes.add(newNode);
					nameTable.put(name, nodes);
				}
				// If exist, add the node to the list
				else {
					// Make two way edges from/to the already existing nodes in the list
					// These edges represent transferring
					for(Node node: nodes) {
						node.getEdges().addFirst(new Edge(newNode,TRANSFER_TIME));
						newNode.getEdges().addFirst(new Edge(node, TRANSFER_TIME));
					}
					nodes.addFirst(newNode);
				}
			}
			// Edge information
			while((inputLine = br.readLine()) != null) {
				String info[] = inputLine.split(" ");
				String startId = info[0];
				String endId = info[1];
				long dist = Long.parseLong(info[2]);
				
				Node startNode = idTable.get(startId);
				Node endNode = idTable.get(endId);
				startNode.getEdges().add(new Edge(endNode, dist));
			}
			
			br.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}

/* Node represents station.
 * For transfer stations, separate nodes are created for each line. */
class Node implements Comparable<Node> {
	private String id;
	private String name;
	private String line;
	private LinkedList<Edge> edges;	// List of outgoing edges from this node
	private long dist; // Distance value (current distance from the source node)
	private boolean settled; // Whether the shortest path to this node is computed and finalized
	private Node prev; // Node that precedes this node in the current path
	
	public Node(String id, String name, String line) {
		this.id = id;
		this.name = name;
		this.line = line;
		
		this.edges = new LinkedList<>();	
		this.dist = Long.MAX_VALUE; // Initialize to maximum value
		this.settled = false;
		this.prev = null;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public LinkedList<Edge> getEdges() {
		return edges;
	}

	public void setEdges(LinkedList<Edge> edges) {
		this.edges = edges;
	}

	public long getDist() {
		return dist;
	}

	public void setDist(long dist) {
		this.dist = dist;
	}

	public Node getPrev() {
		return prev;
	}

	public void setPrev(Node prev) {
		this.prev = prev;
	}
	
	public boolean isSettled() {
		return settled;
	}

	public void setSettled(boolean settled) {
		this.settled = settled;
	}	

	// Node that has smaller distance value is smaller.
	// Used for finding the minimum distance node from the unsettled nodes.
	@Override
	public int compareTo(Node other) {
		return Long.compare(this.dist, other.dist);
	}
}

/* Edge represents connection between stations */
class Edge {
	private Node endNode; // To which node this edge is headed to
	private long dist; // Length of the edge: how long it takes to move along this edge
	
	public Edge(Node node, long dist) {
		this.setEndNode(node);
		this.setDist(dist);
	}

	public long getDist() {
		return dist;
	}

	public void setDist(long dist) {
		this.dist = dist;
	}

	public Node getEndNode() {
		return endNode;
	}

	public void setEndNode(Node endNode) {
		this.endNode = endNode;
	}
}
