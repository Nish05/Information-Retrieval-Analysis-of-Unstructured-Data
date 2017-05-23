import java.util.ArrayList;

public class PositionalIndex {
	String[] myDocs;
	ArrayList<String> termList;
	ArrayList<ArrayList<DocId>> docLists;

	/**
	 * Construct a positional index 
	 * @param docs List of input strings or file names
	 * 
	 */
	public PositionalIndex(String[] docs)
	{
		myDocs = docs;
		termList = new ArrayList<String>();
		docLists = new ArrayList<ArrayList<DocId>>();
		ArrayList<DocId> docList;
		for(int i=0;i<myDocs.length;i++){
			String[] tokens = myDocs[i].split(" ");
			String token;
			for(int j=0;j<tokens.length;j++){
				token = tokens[j];
				if(!termList.contains(token)){
					termList.add(token);
					docList = new ArrayList<DocId>();
					DocId doid = new DocId(i,j);
					docList.add(doid);
					docLists.add(docList);
				}
				else{ //existing term
					int index = termList.indexOf(token);
					docList = docLists.get(index);
					int k=0;
					boolean match = false;
					//search the postings for a document id, if match, insert a new position
					//number to the document id
					for(DocId doid:docList)
					{
						if(doid.docId==i)
						{
							doid.insertPosition(j);
							docList.set(k, doid);
							match = true;
							break;
						}
						k++;
					}
					//if no match, add a new document id along with the position number
					if(!match)
					{
						DocId doid = new DocId(i,j);
						docList.add(doid);
					}
				}
			}
		}
	}

	/**
	 * Return the string representation of a positional index
	 */
	public String toString()
	{
		String matrixString = new String();
		ArrayList<DocId> docList;
		for(int i=0;i<termList.size();i++){
			matrixString += String.format("%-15s", termList.get(i));
			docList = docLists.get(i);
			for(int j=0;j<docList.size();j++)
			{
				matrixString += docList.get(j)+ "\t";
			}
			matrixString += "\n";
		}
		return matrixString;
	}

	/**
	 * 
	 * @param l1 first postings
	 * @param l2 second postings
	 * @return merged result of two postings
	 */
	public ArrayList<DocId> intersect(ArrayList<DocId> l1, ArrayList<DocId> l2)
	{
		ArrayList<DocId> mergedList = new ArrayList<DocId>();
		int id1=0, id2=0;
		while(id1<l1.size()&&id2<l2.size()){
			//if both terms appear in the same document
			
			if(l1.get(id1).docId==l2.get(id2).docId){
				//get the position information for both terms
				ArrayList<Integer> pp1 = l1.get(id1).positionList;
				ArrayList<Integer> pp2 = l2.get(id2).positionList;
				int pid1 =0, pid2=0;
				while(pid1<pp1.size()){
					boolean match = false;
					while(pid2<pp2.size()){
						//if the two terms appear together, we find a match
						if(Math.abs(pp1.get(pid1)-pp2.get(pid2))<=1){
							match = true;
							mergedList.add(l2.get(id2));
							break;
						}
						else if(pp2.get(pid2)>pp1.get(pid1))
							break;
						pid2++;
					}
					if(match) //if a match if found, the search for the current document can be stopped
						break;
					pid1++;
				}
				id1++;
				id2++;
			}
			else if(l1.get(id1).docId<l2.get(id2).docId)
				id1++;
			else
				id2++;
		}	
		
		return mergedList;
	}
	/**
	 * 
	 * @param query a phrase query that consists of any number of terms in the sequential order
	 * @return ids of documents that contain the phrase
	 */
	public ArrayList<DocId> phraseQuery(String[] query)
	{	
		ArrayList<DocId> l1;
		ArrayList<DocId> l2;
		ArrayList<DocId> intermediate;
		l1 = docLists.get(termList.indexOf(query[0]));
		l2 = docLists.get(termList.indexOf(query[1]));
		intermediate=intersect(l1,l2);
		
		if(query.length>2){
			for(int i=2;i<query.length;i++){
				l1=intermediate;
				l2 = docLists.get(termList.indexOf(query[i]));	
				intermediate=intersect(l1,l2);			
			}
		}
		
		return intermediate;
		//TASK3: TO BE COMPLETED
	}


	public static void main(String[] args)
	{
		String[] docs = {"new home sales top forecasts",
				"home sales rise in july",
				"increase in home sales in july",
				"july new home sales rise"
		};
		PositionalIndex pi = new PositionalIndex(docs);
		System.out.println("******************************************");
		System.out.println();
		System.out.println("     Positional Index    ");
		System.out.println();
		System.out.print(pi);
		//TestCase1
		System.out.println();
		System.out.println("******************************************");
		System.out.println();
		System.out.println("Test Case 1");
		System.out.println("Query: home sales");
		String[] query={"home","sales"};
		System.out.println();
		System.out.println("Retrieved Documents: ");
		ArrayList<DocId> result=pi.phraseQuery(query);
		if(result!=null){
			for(DocId doid: result){
				System.out.println(docs[doid.docId]);
			}
		}
		System.out.println();
		System.out.println("******************************************");
		System.out.println();
		//TestCase2
		System.out.println("Test Case 2");
		System.out.println("Query: new home sales");
		String[] query1={"new","home","sales"};
		System.out.println();
		System.out.println("Retrieved Documents: ");
		ArrayList<DocId> result1=pi.phraseQuery(query1);
		if(result1!=null){
			for(DocId doid: result1){
				System.out.println(docs[doid.docId]);
			}
		}
		System.out.println();
		System.out.println("******************************************");
		System.out.println();
		//TestCase3
		System.out.println("Test Case 3");
		System.out.println("Query: home sales in july");
		String[] query2={"home","sales","in","july"};
		System.out.println();
		System.out.println("Retrieved Documents: ");
		ArrayList<DocId> result2=pi.phraseQuery(query2);
		if(result2!=null){
			for(DocId doid: result2){
				System.out.println(docs[doid.docId]);
			}
		}
		System.out.println();
		System.out.println("******************************************");
		System.out.println();
		//TestCase4
		System.out.println("Test Case 4");
		System.out.println("Query: july new home sales rise");
		String[] query3={"july","new","home","sales","rise"};
		System.out.println();
		System.out.println("Retrieved Documents: ");
		ArrayList<DocId> result3=pi.phraseQuery(query3);
		if(result3!=null){
			for(DocId doid: result3){
				System.out.println(docs[doid.docId]);
			}
		}
		System.out.println();
		System.out.println("******************************************");
		System.out.println();
		//TASK4: TO BE COMPLETED: design and test phrase queries with 2-5 terms
	}
}
/**
 * 
 * @author qyuvks
 * Document id class that contains the document id and the position list
 */
class DocId {
	int docId;
	ArrayList<Integer> positionList; 
	public DocId(int did){
		docId=did;
		positionList=new ArrayList<Integer>();
	}
	public DocId(int did, int position){
		docId=did;
		positionList=new ArrayList<Integer>();
		positionList.add(new Integer(position));
	}
	public void insertPosition(int position){
		positionList.add(new Integer(position));
	}

	public String toString(){
		String docString=docId+":<";
		for(Integer i:positionList){
			docString+=i+",";
		}
		docString=docString.substring(0, docString.length()-1)+">";
		return docString;
	}
}


