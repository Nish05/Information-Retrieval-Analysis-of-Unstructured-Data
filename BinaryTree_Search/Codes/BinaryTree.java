import java.util.*;

/**
 * 
 * @author Nisha Bhanushali
 * a node in a binary search tree
 */
class BTNode{
	BTNode left, right;
	String term;
	ArrayList<Integer> docLists;

	/**
	 * Create a tree node using a term and a document list
	 * @param term the term in the node
	 * @param docList the ids of the documents that contain the term
	 */
	public BTNode(String term, ArrayList<Integer> docList)
	{
		this.term = term;
		this.docLists = docList;
	}
	public BTNode() {
	}
	public String toString(){
		String docString=term+": <";
		for(Integer i:docLists){
			docString+=i+",";
		}
		docString=docString.substring(0, docString.length()-1)+">";
		return docString;
	}
}

/**
 *
 * Binary search tree structure to store the term dictionary
 */
public class BinaryTree {
	ArrayList<BTNode> nodes;
	/**
	 * insert a node to a subtree 
	 * @param node root node of a subtree
	 * @param iNode the node to be inserted into the subtree
	 */
	public void add(BTNode node, BTNode iNode)
	{
		if(iNode!=null){

			if(iNode.term.compareTo(node.term)<0)
			{
				if(node.left!=null){
					add(node.left,iNode);
				}
				else{
					System.out.println("Inserted " + iNode.term + 
							" to left node " + node.term);
					node.left = iNode;
				}
			}
			else if(iNode.term.compareTo(node.term) > 0){
				if(node.right!=null){
					add(node.right,iNode);
				}
				else{
					System.out.println("Inserted " + iNode.term +
							" to right of node " + node.term);
					node.right = iNode;
				}
			}
		}
	}

	/**
	 * Search a term in a subtree
	 * @param n root node of a subtree
	 * @param key a query term
	 * @return tree nodes with term that match the query term or null if no match
	 */
	public BTNode search(BTNode n, String key)
	{
		if(n==null) return null;
		if (n.term.equals(key)) return n;
		else if (n.term.compareTo(key)>0) return search(n.left,key);
		else return search(n.right,key);
	}

	/**
	 * Do a wildcard search in a subtree
	 * @param n the root node of a subtree
	 * @param key a wild card term, e.g., ho (terms like home will be returned)
	 * @return tree nodes that match the wild card
	 */
	public ArrayList<BTNode> wildCardSearch(BTNode n, String key)
	{
		if(n==null) {
			return nodes;
		}
		int endindex=key.length();
		if(n.term.length()>=key.length()){
			if (n.term.substring(0,endindex).equals(key)){ 
				if(nodes==null){
					nodes=new ArrayList<BTNode>();
					nodes.add(n);
				}
				else{
					nodes.add(n);
				}
			}
		}
		wildCardSearch(n.left,key);
		wildCardSearch(n.right,key);

		return nodes;
	}

	/**
	 * Print the inverted index based on the increasing order of the terms in a subtree
	 * @param node the root node of the subtree
	 */
	public void printInOrder(BTNode node)
	{
		if(node!=null){
			printInOrder(node.left);
			System.out.println(node);
			printInOrder(node.right);
		}
	}

}

