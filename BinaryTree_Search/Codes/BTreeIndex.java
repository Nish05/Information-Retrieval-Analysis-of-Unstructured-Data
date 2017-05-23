import java.util.*;

public class BTreeIndex {
	String[] myDocs;
	BinaryTree termList;
	BTNode root;
	BTNode node;
	ArrayList<Integer> docList;
	/**
	 * Construct binary search tree to store the term dictionary 
	 * @param docs List of input strings
	 * 
	 */
	public BTreeIndex(String[] docs)
	{
		myDocs=docs;
		termList=new BinaryTree();
		for(int i=0;i<myDocs.length;i++){
			String[] tokens=myDocs[i].split(" ");
			Arrays.sort(tokens);
			// creating the middle term as root
			if(i==0){
			int start=0;
			int end=tokens.length-1;
			int mid=(start+end)/2;
			docList=new ArrayList<Integer>();
			docList.add(i);
			BTNode iNode=new BTNode(tokens[mid],docList);
			root=iNode;
			}
			for(int j=0;j<tokens.length;j++){
				String token=tokens[j];
				//If the term is not present in the termList
				if(termList.search(root,token)==null){
					docList=new ArrayList<Integer>();
					docList.add(i);
					BTNode iNode=new BTNode(token,docList);
					termList.add(root, iNode);
				}//If the term is already present in the termList
				else{
					BTNode node=termList.search(root, token);
					docList=node.docLists;
					if(!docList.contains(i)){
						node.docLists.add(i);
					}
				}
			}
		}
		System.out.println();
		System.out.println("************************************************");
		System.out.println();
		System.out.println("Binary Tree Index");
		System.out.println();
		termList.printInOrder(root);
	}


	/**
	 * Single keyword search
	 * @param query the query string
	 * @return doclists that contain the term
	 */
	public ArrayList<Integer> search(String query)
	{
		BTNode node = termList.search(root, query);
		if(node==null)
			return null;
		return node.docLists;
	}

	/**
	 * conjunctive query search
	 * @param query the set of query terms
	 * @return doclists that contain all the query terms
	 */
	public ArrayList<Integer> search(String[] query)
	{
		ArrayList<Integer> result = search(query[0]);
		int termId = 1;
		while(termId<query.length)
		{
			ArrayList<Integer> result1 = search(query[termId]);
			result = merge(result,result1);
			termId++;
		}		
		return result;
	}

	/**
	 * 
	 * @param wildcard the wildcard query, e.g., ho (so that home can be located)
	 * @return a list of ids of documents that contain terms matching the wild card
	 */
	public ArrayList<Integer> wildCardSearch(String wildcard)
	{
		ArrayList<BTNode> nodes=termList.wildCardSearch(root,wildcard);
		ArrayList<Integer> docList=new ArrayList<Integer>();
		if(nodes!=null){
			for(BTNode node: nodes){
				for(Integer docId:node.docLists){
					if(!docList.contains(docId)){
						docList.add(docId);
					}
				}
			}
		}
		return docList;
	}


	private ArrayList<Integer> merge(ArrayList<Integer> l1, ArrayList<Integer> l2)
	{
		ArrayList<Integer> mergedList = new ArrayList<Integer>();
		int id1 = 0, id2=0;
		while(id1<l1.size()&&id2<l2.size()){
			if(l1.get(id1).intValue()==l2.get(id2).intValue()){
				mergedList.add(l1.get(id1));
				id1++;
				id2++;
			}
			else if(l1.get(id1)<l2.get(id2))
				id1++;
			else
				id2++;
		}
		return mergedList;
	}


	/**
	 * Test cases
	 * @param args commandline input
	 */
	public static void main(String[] args)
	{
		String[] docs = {"new home sales top forecasts",
				"home sales rise in july",
				"increase in home sales in july",
				"july new home sales rise"
		};
		System.out.println("************************************************");
		System.out.println();
		System.out.println("Building Binary Tree");
		System.out.println();
		BTreeIndex btree=new BTreeIndex(docs);
		System.out.println();
		//Test Case to search a single term in the Binary Tree
		System.out.println("************************************************");
		System.out.println();
		System.out.println("Test Cases : Search of Term");
		System.out.println();
		System.out.println("Test Case 1");
		System.out.println("Query : home");
		ArrayList<Integer> doc1=btree.search("home");
		System.out.println("The relevant documents are : ");
		for(Integer i:doc1){
			System.out.println(docs[i.intValue()]+" ");
		}
		//Test Case to search a list of terms in the Binary Tree
		System.out.println();
		System.out.println("Test Case 2");
		System.out.println("Query : increase, in, home");
		String[] query={"increase","in","home"};
		ArrayList<Integer> doc2=btree.search(query);
		System.out.println("The relevant documents are : ");
		for(Integer i:doc2){
			System.out.println(docs[i.intValue()]+" ");
		}
		System.out.println();
		System.out.println("************************************************");
		//Test cases to perform wildcard search
		//Test Case 1
		System.out.println();
		System.out.println("Test Cases : WildCard Search ");
		System.out.println();
		System.out.println("Test Case 1");
		System.out.println("Query : ne");
		System.out.println("The relevant documents are : ");
		ArrayList<Integer> doc3=btree.wildCardSearch("ne");
		if(doc3.size()!=0){
			for(Integer i:doc3){
				System.out.println(docs[i.intValue()]+" ");
			}
		}else{
			System.out.println("No Match !.....");
		}
		btree.termList.nodes=null;
		//Test Case 2
		System.out.println();
		System.out.println("Test Case 2");
		System.out.println("Query : in");
		System.out.println("The relevant documents are : ");
		ArrayList<Integer> doc4=btree.wildCardSearch("in");
		if(doc4.size()!=0){
			for(Integer i:doc4){
				System.out.println(docs[i.intValue()]+" ");
			}
		}else{
			System.out.println("No Match !.....");
		}
		btree.termList.nodes=null;
		//Test Case 3
		System.out.println();
		System.out.println("Test Case 3");
		System.out.println("Query : jul");
		ArrayList<Integer> doc5=btree.wildCardSearch("jul");
		System.out.println("The relevant documents are : ");
		if(doc5.size()!=0){
			for(Integer i:doc5){
				System.out.println(docs[i.intValue()]+" ");
			}
		}else{
			System.out.println("No Match !.....");
		}
		btree.termList.nodes=null;
		//Test Case 4
		System.out.println();
		System.out.println("Test Case 4");
		System.out.println("Query : me");
		ArrayList<Integer> doc6=btree.wildCardSearch("me");
		System.out.println("The relevant documents are : ");
		if(doc6.size()!=0){
			for(Integer i:doc6){
				System.out.println(docs[i.intValue()]+" ");
			}
		}else{
			System.out.println("No Match !.....");
		}
		btree.termList.nodes=null;
		System.out.println();
		System.out.println("************************************************");


	}
}
