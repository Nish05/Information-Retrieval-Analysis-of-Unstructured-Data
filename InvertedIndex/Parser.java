/**
 * Parser.java
 * 
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
/**
 * This program builds an inverted index.
 * The documents are split into tokens. The stop words are removed 
 * from the tokens. The tokens are stemmed and final terms are generated.
 * A list of documents for each term is maintained.
 * 
 * @author Nisha Bhanushali
 *
 */
public class Parser {

	String myDocs[];
	ArrayList<String> noStopList; 
	ArrayList<String> termList=new ArrayList<String>();
	ArrayList<ArrayList<Integer>> docLists=new ArrayList<ArrayList<Integer>>();
	ArrayList<Integer> docFreq=new ArrayList<Integer>();
	TreeMap<Integer,LinkedList<String>> termFreq;
	ArrayList<Integer> docList;
	String[] stopList;
	/**
	 * The constructor does text processing by tokenizing 
	 * the documents, removing the stop words, stemming
	 * the documents and then generating the termlist.
	 *  
	 * @param folderName      Name of the folder containing all the files
	 * @param stopListFile    Name of the file containing all the stop words
	 */
	public Parser(String folderName,String stopListFile){
		int j=0;
		//Assigns unique ID to each file
		File folder=new File(folderName);
		File[] filenames=folder.listFiles();
		myDocs=new String[filenames.length-1];
		System.out.println("*******************************************");
		System.out.println();
		System.out.println("List of files ");
		System.out.println();
		System.out.println(" ID |"+"     Filename ");
		System.out.println("-----------------------");
		for(int i=0;i<filenames.length;i++){
			// The finder automatically generates .DS_Store file which is 
			//unwanted. To avoid errors, the if condition is written
			if(!filenames[i].getName().equals(".DS_Store")){
				myDocs[j]=filenames[i].getName();
				System.out.println(" "+j+"  | "+filenames[i].getName());
				j++;
			}
		}
		System.out.println();
		//Storing the list of stop words in the array from the 
		//given file of stop words
		stopList=generateStopList(stopListFile);
		Arrays.sort(stopList);
		//Performing tokenization of each document
		for(int i=0;i<myDocs.length;i++){
			String[] tokens=parse(folderName+"/"+myDocs[i]);
			noStopList=new ArrayList<String>();

			for(String token:tokens){
				int result=searchStopWord(token);
				if(result==-1){
					//Stemming of the tokens
					String term=stemWord(token);
					if(!termList.contains(term)){
						termList.add(term);
						docList=new ArrayList<Integer>();
						docList.add(new Integer(i));
						docLists.add(docList);
						docFreq.add(new Integer(1));
					}
					else{//if a term already exists
						int index=termList.indexOf(term);
						int value=docFreq.get(index).intValue();

						docList=docLists.get(index);
						if(!docList.contains(new Integer(i))){
							docList.add(new Integer(i));
							docLists.set(index,docList);
							value=value+1;
							docFreq.set(index,new Integer(value));
						}
					}
				}

			}

		}
	}
	/**
	 * Generates an array of words from the file of stop words
	 * @param stopListFile       Name of the file which contains all the stop words
	 * @return array of stop words
	 */
	public String[] generateStopList(String stopListFile) {
		ArrayList<String> stopwords=new ArrayList<String>();

		try {
			int count=0;
			BufferedReader reader=new BufferedReader(new FileReader(stopListFile));
			//Checks the total number of stop words in the document

			while(reader.readLine()!=null){
				stopwords.add(reader.readLine());
			}
			stopList=new String[stopwords.size()];
			while(count!=stopwords.size()){
				stopList[count]=stopwords.get(count);	
				count++;
			}
			System.out.println();
		} catch (IOException e) {

			e.printStackTrace();
		}

		return stopList;
	}
	/**
	 * This method performs stemming of the token
	 * @param token      token to be stemmed
	 * @return stemmed token
	 */
	public String stemWord(String token) {
		Stemmer st=new Stemmer();
		st.add(token.toCharArray(),token.length());
		st.stem();
		return st.toString();
	}
	/**
	 * Performs binary search to search whether the token is 
	 * stop word or not.
	 * @param token   token which is searched
	 * @return -1 or position of the token in stop word list
	 */
	public int searchStopWord(String token) {
		int lo=0;
		int high=stopList.length-1;
		while(lo<high){
			int mid=(lo+high)/2;
			if(token.compareTo(stopList[mid])<0) high=mid-1;
			else if(token.compareTo(stopList[mid])>0) lo=mid+1;
			else return mid;

		}
		return -1;


	}
	/**
	 * Removes all the delimiters from the file
	 * @param fileName  file to be processed
	 * @return  processed file
	 */
	public String[] parse(String fileName) {
		String allLines="";
		String currentLine;
		String[] tokens=null;
		try {
			BufferedReader reader=new BufferedReader(new FileReader(fileName));
			while((currentLine=reader.readLine())!=null){
				allLines+=currentLine;
			}
			tokens=allLines.split("[ .,?!:;$%()\"--']+");

		} catch (IOException e) {
			e.printStackTrace();
		}
		return tokens;
	}
	/**
	 * CHecks whether the keyword is present in the termlist
	 * @param query                    keyword to be searched
	 * @return null                    if not present
	 *         list of documents       if present
	 */
	public ArrayList<Integer> search(String query){
		int index=termList.indexOf(query);
		if(index<0)
			return null;
		return docLists.get(index);
	}
	/**
	 * Processes the query in the increasing order of the
	 * document frequency of each keyword
	 * @param   query1            query to be processed
	 * @param   operator          AND/OR to be performed between the keywords
	 * @return  list of documents generated
	 */
	public ArrayList<Integer> search(String[] query1, String operator){
		String[] query=insertInMap(query1);
		ArrayList<Integer> result=search(query[0]);
		int value=97;

		int termId=1;
		while(termId<query.length){
			ArrayList<Integer> result1=search(query[termId]);
			if( operator.equals("AND") && result!=null && result1!=null){
				result=merge(result,result1,operator);	
			}
			else if(operator.equals("OR") && (result!=null || result1!=null)){
				result=merge(result,result1,operator);	
			}
			termId++;
		}
		return result;
	}
	/**
	 * To arrange the termlist such that the term with the smallest list is 
	 * processed first.
	 * @param   query1    query to be processed
	 * @return  query in the increasing order of the document frequency
	 */
	public String[] insertInMap(String[] query1) {
		termFreq=new TreeMap<Integer,LinkedList<String>>();
		LinkedList<String> term;
		String[] query;
		int index;
		int freq;
		for(int i=0;i<query1.length;i++){
			index=termList.indexOf(query1[i]);
			if(index==-1){
				freq=0;
			}
			else{
				freq=docFreq.get(index);
			}
			term=termFreq.get(freq);
			if(term==null){
				term=new LinkedList<String>();
				term.add(query1[i]);
				termFreq.put(freq, term);
			}
			else{
				term.add(query1[i]);
				termFreq.put(freq, term);
			}

		}
		int i=0;
		query=new String[query1.length];
		Set<Integer> keys = termFreq.keySet();
		System.out.println("The order of processing is : ");
		System.out.println("Freq      Keyword");
		System.out.println("-----------------------------");
		for(Integer key: keys){
			term=termFreq.get(key);
			for(int j=0;j<term.size();j++){
				query[i]=term.get(j);
				System.out.println(key.intValue()+"         "+query[i]);
				i++;
			}
		}
		return query;
	}
	/**
	 * Performs merge operation on two posting lists at a time. The merge algorithm
	 * is different for AND and OR 
	 * @param  l1             list 1
	 * @param  l2             list 2
	 * @param  operator       operation to be performed
	 * @return final mergedlist 
	 */
	public ArrayList<Integer> merge(ArrayList<Integer> l1, ArrayList<Integer> l2,
			String operator) {
		ArrayList<Integer> mergedList=new ArrayList<Integer>();
		int id1=0;
		int id2=0;
		//Merge algorithm to perform AND between the keyword
		if(operator.equals("AND")){
			while(id1<l1.size() && id2<l2.size()){
				if(l1.get(id1).intValue()==l2.get(id2).intValue()){
					mergedList.add(l1.get(id1));
					id1++;
					id2++;
				}
				else if(l1.get(id1)<l2.get(id2)){
					id1++;
				}
				else{
					id2++;
				}
			}
		}
		//Merge operation to perform OR between the keywords
		else{
			if(l1!=null && l2!=null){
				while(id1<l1.size() && id2<l2.size()){
					if(l1.get(id1).intValue()==l2.get(id2).intValue()){
						mergedList.add(l1.get(id1));
						id1++;
						id2++;
					}
					else if(l1.get(id1)<l2.get(id2)){
						mergedList.add(l1.get(id1));
						id1++;
					}
					else{
						mergedList.add(l2.get(id2));
						id2++;
					}
				}
			} 
			else if(l2==null){
				while(id1<l1.size()){
					mergedList.add(l1.get(id1));
					id1++;
				}
			}else if(l1==null){
				while(id2<l2.size()){
					mergedList.add(l2.get(id2));
					id2++;
				}
			}

		}
		return mergedList;
	}

	/**
	 * This method is the implementation of the test cases with 
	 * single keyword
	 */
	public void singleKeyWord() {
		String keyword;
		ArrayList<Integer> result;
		String query;
		System.out.println();
		System.out.println("Test Case 1 :");
		System.out.println("Keyword : flicking");
		keyword="flicking";
		query=stemWord(keyword);
		result=search(query);
		printResult(result);
		System.out.println();
		System.out.println("Test Case 2 : ");
		System.out.println("Keyword : Hotel");
		keyword="Hotel";
		query=stemWord(keyword);
		search(query);
		result=search(query);
		printResult(result);

	}
	/**
	 * This method is the implementation of the test cases with 
	 * two keyword and AND operation
	 */

	public void twoKeyWordAND() {
		String[] keywords=new String[2];
		System.out.println();
		System.out.println("Test Case 1 :");
		System.out.println("Keywords : as you");
		keywords[0]="as";
		keywords[1]="you";
		computeResult(keywords,"AND");
		System.out.println();
		System.out.println("Test Case 2 : ");
		System.out.println("Keyword : 123 learning");
		keywords[0]="123";
		keywords[1]="learning";
		computeResult(keywords,"AND");

	}
	/**
	 * This method is the implementation of the test cases with 
	 * two keywords and OR operation
	 */
	public void twoKeyWordOR(){
		String[] keywords=new String[2];
		System.out.println();
		System.out.println("Test Case 1 :");
		System.out.println("Keywords : actors Hotel");
		keywords[0]="actors";
		keywords[1]="Hotel";
		computeResult(keywords,"OR");
		System.out.println();
		System.out.println("Test Case 2 : ");
		System.out.println("Keyword : these movie");
		keywords[0]="these";
		keywords[1]="movie";
		computeResult(keywords,"OR");
	}
	/**
	 * This method is the implementation of the test cases with 
	 * three keywords and AND operation
	 */
	public void threeKeyWord() {
		String[] keywords=new String[3];
		System.out.println();
		System.out.println("Test Case 1 :");
		System.out.println("Keywords : as guessing video");
		keywords[0]="as";
		keywords[1]="guessing";
		keywords[2]="video";
		computeResult(keywords,"AND");
		System.out.println();
		System.out.println("Test Case 2 : ");
		System.out.println("Keyword : equal ending across");
		keywords[0]="equal";
		keywords[1]="ending";
		keywords[2]="across";
		computeResult(keywords,"AND");

	}
	/**
	 * This method is the implementation of the test cases with 
	 * four keywords and AND operation
	 */
	public void FourKeyWord() {
		String[] keywords=new String[4];
		System.out.println();
		System.out.println("Test Case 1 :");
		System.out.println("Keywords : as learning through videos");
		keywords[0]="as";
		keywords[1]="learning";
		keywords[2]="video";
		keywords[3]="through";
		computeResult(keywords,"AND");
		System.out.println();
		System.out.println("Test Case 2 : ");
		System.out.println("Keyword : here guess turn edges");
		keywords[0]="here";
		keywords[1]="guess";
		keywords[2]="turn";
		keywords[3]="edges";
		computeResult(keywords,"AND");

	}
	/**
	 * Retrieves all the documents relevant to the query searched and prints
	 * all the documents
	 * @param keywords     keywords to be searched
	 * @param operator     operation to be performed
	 */
	public void computeResult(String keywords[],String operator){
		ArrayList<Integer> result;
		String[] query=new String[keywords.length];
		for(int i=0;i<keywords.length;i++){
			String result1=stemWord(keywords[i]);
			query[i]=result1;
		}
		result=search(query,operator);
		printResult(result);
	}

	public static void main(String[] args)throws IOException{
		BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
		Parser p = new Parser(args[0],args[1]);
		System.out.println(p);
		int choice;
		do{
			System.out.println("*******************************************");
			System.out.println();
			System.out.println("Menu");
			System.out.println("1 User-defined Input");
			System.out.println("2 Pre-defined Input(Test cases)");
			System.out.println("3 Exit");
			System.out.println("Please enter your choice");
			choice=Integer.parseInt(reader.readLine());
			switch(choice){
			case 1: 
				System.out.println("Enter the keywords to be searched :");
				String input=reader.readLine();
				String[] query=input.split("[ .,?!:;$%()\"--']+");
				System.out.println("Enter the operation to be performed AND/OR");
				String operator=reader.readLine().toUpperCase();
				System.out.println("The operator is : "+operator);
				p.computeResult(query,operator);
				System.out.println("Press 3 to exit the search");
				break;
			case 2:
				System.out.println("The test cases for searching are as follows :");
				System.out.println();
				System.out.println("*******************************************");
				System.out.println();
				System.out.println("Test case with 1 keyword input : ");
				p.singleKeyWord();
				System.out.println();
				System.out.println("*******************************************");
				System.out.println();
				System.out.println("Test case with 2 keywords input and 'AND' operator : ");
				p.twoKeyWordAND();
				System.out.println();
				System.out.println("*******************************************");
				System.out.println();
				System.out.println("Test case with 2 keywords input and 'OR' operator :");
				p.twoKeyWordOR();
				System.out.println();
				System.out.println("*******************************************");
				System.out.println();
				System.out.println("Test case with 3 keywords input and 'AND' operator : ");
				p.threeKeyWord();
				System.out.println();
				System.out.println("*******************************************");
				System.out.println();
				System.out.println("Multiple keyword input is : ");
				p.FourKeyWord();
				System.out.println();
				System.out.println("*******************************************");
				System.out.println();
				System.out.println("Press 3 to exit the search");
				break;
			}
		}while(choice!=3);
	}
	/**
	 * This method prints the matrix of inverted index
	 */
	public String toString(){
		System.out.println("*******************************************");
		System.out.println("The inverted index is : ");
		System.out.println();
		String matrixString=new String();
		for(int i=0;i<termList.size();i++){
			matrixString+=String.format("%-15s", termList.get(i));
			docList=docLists.get(i);
			for(int j=0;j<docList.size();j++)
				matrixString+=docList.get(j)+"\t";
			matrixString+="\n";
		}
		return matrixString;
	}
	/**
	 * Prints all the relevant documents for a query search
	 * @param result      final documents to be printed
	 */
	public void printResult(ArrayList<Integer> result){
		if(result!=null){
			if(result.size()!=0){
				System.out.println("The relevant documents are : ");

				for(Integer i: result){
					System.out.println(myDocs[i]);
				}
			}
			else{
				System.out.println("No Match!.....");
			}
		}else{
			System.out.println("No Match!.....");
		}

	}
}
