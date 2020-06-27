import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class Matching {
	
	private static final int K=6; 
	private static final int TABLE_CAPA=100;
	private static HashTable<Coord> slots;
	
	public static void main(String args[]) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				String input = br.readLine();
				if (input.compareTo("QUIT") == 0)
					break;

				command(input);
			}
			catch (Exception e) {
				System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
			}
		}
	}

	private static void command(String input) throws IllegalArgumentException {
		char commandType = input.charAt(0);
		String arg = input.substring(2);
		
		if(commandType == '<') enterFile(arg);
		else if(commandType == '@') printSlot(arg);
		else if(commandType == '?') search(arg);
		else throw new IllegalArgumentException("Illegal command type.");
	}

	/* Reads a text file and save all the patterns (of length K) in a new hash table. */
	private static void enterFile(String file) {
		slots = new HashTable<Coord>(TABLE_CAPA);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			int lineNum = 1;
			// Coordinates are inserted in increasing order -> sorted.
			while((line = br.readLine()) != null) {
				for(int i=0; i < line.length()-K+1; i++) {
					// extract pattern and insert it with the corresponding Coordinate
					String pattern = line.substring(i, i+K);
					slots.insert(pattern, new Coord(lineNum, i+1));
				}
				lineNum++;
			}
			br.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/* Print every pattern in the specified slot of the hash table. */ 
	private static void printSlot(String slotNum) {
		AVLTree<Coord> slot = slots.get(Integer.parseInt(slotNum));
		if(slot == null) {
			System.out.println("EMPTY");
			return;
		}
		slot.print();
	}
	
	/* Print all Coordinates where the given string appears. */
	private static void search(String arg) {
		// Split the given string into patterns (K letters string).
		// Last pattern may be shorter than K letters
		int totalLen = arg.length();
		int lastPatternLen = totalLen % K;
		if(lastPatternLen == 0) lastPatternLen = K;
		int numPatterns = lastPatternLen==K ? totalLen/K 
											: totalLen/K + 1;
		StringBuilder result = new StringBuilder(); // to save the result Coordinates before printing them all.
		
		
		// Search each pattern and save the Coordinates in an array.
		// If any one of pattern isn't found, search failed.
		Coord[][] patternCoords = new Coord[numPatterns][];
		for(int i=0; i<numPatterns; i++) {
			// Last pattern may be shorter than K -> Make the last pattern K letters from the end.
			String pattern = i == numPatterns-1 ? arg.substring(totalLen-K, totalLen)
												: arg.substring(K*i, K*(i+1));
			LinkedList<Coord> coords = slots.search(pattern);
			if(coords == null) {
				searchFailed();
				return;
			}
			// Save the searched Coordinates in the array
			patternCoords[i] = coords.toArray(new Coord[0]);
		}
		
		// Find successive patterns that are in successive Coordinates.
		// Those are the places where the given string appears.
		for(Coord startCoord: patternCoords[0]) {
			boolean found = true;
			Coord currCoord = startCoord;
			
			for(int j=1; j<patternCoords.length; j++) {
				Coord[] nextCoords = patternCoords[j];
				
				// Successive patterns should have same X coordinates and
				// difference of Y coordinates should be K (except last pattern)
				int diffY = j == patternCoords.length-1 ? lastPatternLen : K;
				Coord targetCoord = new Coord(currCoord.getX(), currCoord.getY() + diffY);
				// Since the Coordinates were inserted in increasing order, it can be searched by binary search.
				if(!binarySearch(nextCoords, targetCoord)) {
					found = false;
					break;
				}
				currCoord = targetCoord;
			}
			if (found) result.append(startCoord.toString() + " ");
		}
		if(result.length() == 0) searchFailed();
		else System.out.println(result.substring(0, result.length()-1)); // exclude last space
	}
	
	/* Search the target Coordinate from the given list of Coordinates, using binary search. */
	private static boolean binarySearch(Coord[] coords, Coord target) {
		int start = 0, end = coords.length-1;
		int mid;
		while(start <= end) {
			mid = (start + end) / 2;
			if(coords[mid].compareTo(target) > 0) {
				end = mid - 1;
			}
			else if(coords[mid].compareTo(target) < 0) {
				start = mid + 1;
			}
			else {
				return true;
			}
		}
		return false;
	}
	
	private static void searchFailed() {
		System.out.println("(0, 0)");
	}
}



/* Hash table that has AVLTrees in each slot.*/
class HashTable<T> {
	
	private ArrayList<AVLTree<T>> table;
	
	public HashTable(int capacity) {
		table = new ArrayList<AVLTree<T>>(capacity);
		for(int i=0; i<capacity; i++) {
			table.add(null);
		}
	}
	
	/* Insert the given pattern and item to the table.*/
	public void insert(String pattern, T item) {
		int idx = hash(pattern);
		
		if(table.get(idx) == null) {
			table.set(idx, new AVLTree<T>(pattern, item));
		} else {
			table.get(idx).insert(pattern, item);
		}
	}

	/* Returns the slot in the given index.*/
	public AVLTree<T> get(int idx) {
		return table.get(idx);
	}
	
	/* Search the given pattern and returns the list of items.*/
	public LinkedList<T> search(String pattern) {
		int idx = hash(pattern);
		
		AVLTree<T> tree = table.get(idx);
		if(tree == null) return null;
		
		TreeNode<T> node = tree.search(pattern);
		if(node == null) return null;
		
		return node.getList();
	}
	
	/* Hash function: (Sum of the ASCII code of the characters) mod 100.*/
	private static int hash(String pattern) {
		int sum = 0;
		for(int i=0; i<pattern.length(); i++) {
			sum += pattern.charAt(i);
		}
		return sum % 100;
	}
	
}


class AVLTree<T> {
	private TreeNode<T> root;
	
	public AVLTree() {
		root = null;
	}
	
	public AVLTree(String pattern, T item) {
		root = null;
		insert(pattern, item);
	}
	
	public boolean isEmpty() {
		return root == null;
	}
	
	/* Insert given pattern and item to the tree.*/
	public void insert(String pattern, T item) {
		root = _insert(root, pattern, item);
	}
	
	/* Helper function for insert.*/
	private TreeNode<T> _insert(TreeNode<T> node, String pattern, T item){
		if(node == null) {
			return new TreeNode<T>(pattern, item);
		}
		// Insert right
		if(pattern.compareTo(node.getPattern()) > 0) {
			node.setRightChild(_insert(node.getRightChild(), pattern, item));
			
			// Needs Left rotation
			if(node.getRightHeight() > node.getLeftHeight() + 1) {
				// Before Left rotation, needs right rotation
				TreeNode<T> child = node.getRightChild();
				if(child.getRightHeight() < child.getLeftHeight()) {
					node.setRightChild(rotateRight(child));
				}
				return rotateLeft(node);
			}
			else {
				return node;
			}
		}
		// Insert left
		else if(pattern.compareTo(node.getPattern()) < 0) {
			node.setLeftChild(_insert(node.getLeftChild(), pattern, item));
			
			// Needs Right rotation
			if(node.getLeftHeight() > node.getRightHeight() + 1) {
				// Before Right rotation, needs left rotation
				TreeNode<T> child = node.getLeftChild();
				if(child.getLeftHeight() < child.getRightHeight()) {
					node.setLeftChild(rotateLeft(child));
				}
				return rotateRight(node);
			}
			else {
				return node;
			}
		}
		// Already exists: insert in the corresponding list
		else {
			node.add(item);
			return node;
		}
	}
	
	/* Rotate left: the right child goes up, and the given node becomes left child.*/
	public TreeNode<T> rotateLeft(TreeNode<T> node) {
		TreeNode<T> returnNode = node.getRightChild();
		node.setRightChild(returnNode.getLeftChild());
		returnNode.setLeftChild(node);
		
		return returnNode;
	}
	/* Rotate right: the left child goes up, and the given node becomes right child.*/
	public TreeNode<T> rotateRight(TreeNode<T> node) {
		TreeNode<T> returnNode = node.getLeftChild();
		node.setLeftChild(returnNode.getRightChild());
		returnNode.setRightChild(node);
		
		return returnNode;
	}
	
	/* Find the node that has the given pattern.*/
	public TreeNode<T> search(String pattern) {
		return _search(root, pattern);
	}
	
	/* Helper function for search.*/
	private TreeNode<T> _search(TreeNode<T> node, String pattern) {
		if(node == null) return null;
		// Pattern is greater: go to right child
		if(node.getPattern().compareTo(pattern) < 0) {
			return _search(node.getRightChild(), pattern);
		}
		// Pattern is smaller: go to left child
		else if(node.getPattern().compareTo(pattern) > 0) {
			return _search(node.getLeftChild(), pattern);
		}
		// Found
		else {
			return node;
		}
	}
	
	/* Print all the patterns in the nodes.*/
	public void print() {
		if(isEmpty()) {
			System.out.println("EMPTY");
		}
		else {
			StringBuilder sb = new StringBuilder();
			_print(root, sb);
			System.out.println(sb.substring(0, sb.length()-1)); // exclude last space
		}
	}
	
	/* Helper function for print: preorder traversal.*/
	private void _print(TreeNode<T> node, StringBuilder sb) {
		if(node == null) return;
		sb.append(node.getPattern() + " ");
		_print(node.getLeftChild(), sb);
		_print(node.getRightChild(), sb);
	}
	
}

class TreeNode<T>{
	private LinkedList<T> list; // linked list of items
	private String pattern; // key
	private TreeNode<T> leftChild;
	private TreeNode<T> rightChild;
	private int leftHeight;
	private int rightHeight;
	
	public TreeNode(String pattern, T item) {
		this.pattern = pattern;
		this.list = new LinkedList<T>();
		list.add(item);
		
		this.leftChild = null;
		this.rightChild = null;
		this.leftHeight = 0;
		this.rightHeight = 0;
	}

	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	public LinkedList<T> getList() {
		return list;
	}

	public void add(T item) {
		list.add(item);
	}
	
	public TreeNode<T> getLeftChild() {
		return leftChild;
	}
	public void setLeftChild(TreeNode<T> leftChild) {
		this.leftChild = leftChild;
		this.leftHeight = leftChild == null ? 0 : leftChild.getHeight(); // update height
	}
	public TreeNode<T> getRightChild() {
		return rightChild;
	}
	public void setRightChild(TreeNode<T> rightChild) {
		this.rightChild = rightChild;
		this.rightHeight = rightChild == null? 0 : rightChild.getHeight(); // update height

	}
	public int getLeftHeight() {
		return leftHeight;
	}

	public int getRightHeight() {
		return rightHeight;
	}
	
	public int getHeight() {
		return Math.max(leftHeight, rightHeight)+1;
	}
	
}

class Coord implements Comparable<Coord> {
	private int x;
	private int y;
	
	public Coord(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public String toString() {
		return String.format("(%d, %d)", x, y);
	}

	/* Comparing Coordinates: compare X first, then Y.*/
	@Override
	public int compareTo(Coord c) {
		if(this.getX() > c.getX()) return 1;
		else if(this.getX() < c.getX()) return -1;
		else {
			if(this.getY() > c.getY()) return 1;
			else if(this.getY() < c.getY()) return -1;
			else return 0;
		}
	}
}


