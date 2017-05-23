
import java.util.*;

/**
 * Document clustering
 * @author Nisha Bhanushali
 *
 */
public class Clustering {
	int numClass;
	String[] myDocs;
	ArrayList<String> termList;
	ArrayList<Doc> docLists;
	ArrayList<Integer> docList;
	ArrayList<Double> termFreq;
	Doc[] centroids;
	Doc[] last_Centroids;
	Doc[] current_Centroids;
	//Hierarchical clustering algorithm
	ArrayList<Integer> I;
	int iteration=0;
	//Declare attributes here

	/**
	 * Constructor for attribute initialization
	 * @param numC number of clusters
	 */
	public Clustering(int numC)
	{
		//TO BE COMPLETED
		numClass=numC;
		//List of document objects
		docLists = new ArrayList<Doc>();
		//Document Number
		docList=new ArrayList<Integer>();
		centroids=new Doc[numClass];
		last_Centroids=new Doc[numClass];
		current_Centroids=new Doc[numClass];
		
		//Hierarchical Clustering algorithm
		I=new ArrayList<Integer>();
		

	}

	public Clustering() {
	
		//List of document objects
		docLists = new ArrayList<Doc>();
		//Document Number
		docList=new ArrayList<Integer>();
		//Hierarchical Clustering algorithm
		I=new ArrayList<Integer>();
	}

	/**
	 * Load the documents to build the vector representations
	 * @param docs
	 */
	public void preprocess(String[] docs){
		//TO BE COMPLETED
		myDocs = docs;

		for(int i=0;i<myDocs.length;i++)
		{
			String[] tokens = myDocs[i].split(" ");
			String token;
			docList.add(i);
			termList = new ArrayList<String>();
			termFreq = new ArrayList<Double>();
			for(int j=0;j<tokens.length;j++){
				token = tokens[j];
				if(!termList.contains(token))
				{
					termList.add(token);
					termFreq.add(1.0);
				}
				else
				{
					int index = termList.indexOf(token);
					double value=termFreq.get(index);
					value=value+1.0;
					termFreq.set(index,value);
				}
			}

			for(int k=0;k<termFreq.size();k++){
				double count=termFreq.get(k).doubleValue()/tokens.length;
				termFreq.set(k, count);
			}
			Doc doc=new Doc(termList,termFreq);
			doc.docId=i;
			docLists.add(doc);
		}

	}

	/**
	 * Compute cosine similarity between the documents
	 */
	public double cosineSimilarity(Doc doc1,Doc doc2){
		double num=0.0;
		double denom1=0;
		double denom2=0;
		double freq1=0;
		double freq2=0;
		ArrayList<String> termList1=doc1.termlist;
		ArrayList<String> termList2=doc2.termlist;

		ArrayList<Double> termFreq1=doc1.termFreq;
		ArrayList<Double> termFreq2=doc2.termFreq;

		for(String term : termList1){
			if(termList2.contains(term)){
				freq1=termFreq1.get(termList1.indexOf(term));
				freq2=termFreq2.get(termList2.indexOf(term));
				num+=freq1*freq2;
				denom1+=Math.pow(freq1, 2);
				denom2 += Math.pow(freq2, 2);
			}
			else{
				freq1=termFreq1.get(termList1.indexOf(term));
				denom1+=Math.pow(freq1, 2);
			}
		}
		for(String term:termList2){
			if(!termList1.contains(term)){
				freq2=termFreq2.get(termList2.indexOf(term));
				denom2 += Math.pow(freq2, 2);
			}
		}
		denom1 = Math.sqrt(denom1);
		denom2 = Math.sqrt(denom2);
		double result = num / (denom1 * denom2);

		return result;
	}


	/**
	 * Cluster the documents
	 * For kmeans clustering, use the first and the ninth documents as the initial centroids
	 */
	public void cluster(){
		//TO BE COMPLETED
		
		
		//computing the first cluster
		double cossim1=0;
		
		HashMap<Doc,ArrayList<Integer>> clusters=new HashMap<Doc,ArrayList<Integer>>();
		ArrayList<Integer> clusterDocs;
				for(int i=0;i<centroids.length;i++){
					System.out.println("Enter initial centroid");
					Scanner sc=new Scanner(System.in);
					centroids[i]=docLists.get(sc.nextInt());
					clusterDocs=new ArrayList<Integer>();
					clusters.put(centroids[i], clusterDocs);
				}

		for(int i=0;i<centroids.length;i++){
			current_Centroids[i]=centroids[i];
		}

		int maxId=0;
		for(int i=1;i<docLists.size()-1;i++){
			double max=-1;
			Doc document=docLists.get(i);
			for(int k=0;k<centroids.length;k++){
				cossim1=cosineSimilarity(document,centroids[k]);
				if(cossim1>max){
					max=cossim1;
					maxId=k;
				}

			}

			if(!clusters.containsKey(centroids[maxId])){
				clusterDocs=new ArrayList<Integer>();
				clusterDocs.add(i);
				clusters.put(centroids[maxId], clusterDocs);
			}
			else{
				clusterDocs=clusters.get(centroids[maxId]);
				clusterDocs.add(i);
				clusters.put(centroids[maxId], clusterDocs);
			}
		}
		
		
		//printing cluster
		printCluster(clusters);
		int count1=0;
		boolean flag=false;
		while(flag==false){
			
			//Storing the current centroids
			for(int i=0;i<current_Centroids.length;i++){
				last_Centroids[i]=current_Centroids[i];
			}
			
			//Recomputing centroid			
			for(int i=0;i<centroids.length;i++){
				centroids[i]=new Doc(new ArrayList<String>(),new ArrayList<Double>());
			}
			Set<Doc> keys = clusters.keySet();
			int j=0;
			for(Doc key:keys){
				ArrayList<Integer> docList=clusters.get(key);
				for(Integer docsId:docList){

					Doc doc=docLists.get(docsId);
					ArrayList<String> terms=doc.termlist;
					for(String term: terms){
						if(key.termlist.contains(term)){
							int index=terms.indexOf(term);
							double count=doc.termFreq.get(index);
							int index2=key.termlist.indexOf(term);
							double count2=key.termFreq.get(index2);
							count2+=count;
							key.termFreq.set(index2,count2);

						}
						else{
							int index=terms.indexOf(term);
							double count=doc.termFreq.get(index);
							key.termlist.add(term);
							key.termFreq.add(count);
						}
					}
				}
				for(int i=0;i<key.termFreq.size();i++){
					double count=key.termFreq.get(i).doubleValue()/docList.size();
					key.termFreq.set(i, count);
				}
				centroids[j]=key;
				centroids[j].docId=-1;   // centroids are not a part of document so assigning -1 to it
				j++;
			}	

			//assign the document to the cluster which is more similar
			keys=clusters.keySet();
			clusters.clear();
			j=0;
			for(Doc key:keys){
				key=centroids[j];
				j++;
			}
			for(int i=0;i<centroids.length;i++){
				current_Centroids[i]=centroids[i];
			}
			maxId=0;
			for(int i=0;i<docLists.size();i++){
				double max=-1;
				Doc document=docLists.get(i);
				for(int k=0;k<centroids.length;k++){
					cossim1=cosineSimilarity(document,centroids[k]);
					if(cossim1>max){
						max=cossim1;
						maxId=k;
					}

				}
				if(!clusters.containsKey(centroids[maxId])){
					clusterDocs=new ArrayList<Integer>();
					clusterDocs.add(i);
					clusters.put(centroids[maxId], clusterDocs);
				}
				else{
					clusterDocs=clusters.get(centroids[maxId]);
					clusterDocs.add(i);
					clusters.put(centroids[maxId], clusterDocs);
				}

			}
			
			//Comparing the current and last centroid values
			for(int i=0;i<current_Centroids.length;i++){
				Doc doc1=current_Centroids[i];
				Doc doc2=current_Centroids[i];
				ArrayList<String> termlist1=doc1.termlist;
				ArrayList<String> termlist2=doc2.termlist;
				ArrayList<Double> termFreq1=doc1.termFreq;
				ArrayList<Double> termFreq2=doc2.termFreq;
				 for(String term: termlist1){
					 if(termlist2.contains(term)){
						 int index1=termlist1.indexOf(term);
						 int index2=termlist2.indexOf(term);
						 Double freq1=termFreq1.get(index1);
						 Double freq2=termFreq2.get(index2);
						 if(freq1!=freq2){
							 flag=false;
							 break;
						 }
						 else{
							 flag=true;
						 }
					 }
					 else{
						 flag=false;
						 break;
					 }
				 }
				 if(flag==false){
					 break;
				 }
			}
			
			count1++;
			printCluster(clusters);
		}
	}

	//To print the vector space model
	public String toString()
	{
		String matrixString = new String();
		Doc doc;
		ArrayList<String> termList;
		ArrayList<Double> termFreq;
		for(int i=0;i<docList.size();i++){

			doc = docLists.get(i);
			termList=doc.termlist;
			termFreq=doc.termFreq;
			matrixString+="\t";
			matrixString += String.format("%-15s", docList.get(i));
			for(int j=0;j<termList.size();j++)
			{
				matrixString +=String.format("%-15s",termList.get(j)+" : "+termFreq.get(j)+"  ");
			}
			matrixString += "\n";
		}
		return matrixString;
	}

/**
 * Draws the dendrogram for the given documents
 * @return    the dendrogram
 */
	public ArrayList<ArrayList<String>> simpleHAC() {
		ArrayList<Doc_HAC> documents=new ArrayList<Doc_HAC>();
		ArrayList<ArrayList<String>> A=new ArrayList<ArrayList<String>>();
		int k=1;
		
			//Computing the initial similarity between the documents
			for(int i=0;i<docList.size();i++){
				ArrayList<Integer> mergedList=new ArrayList<Integer>();
				ArrayList<Double> cosine=new ArrayList<Double>();
				Doc_HAC doc=new Doc_HAC(mergedList,cosine);
				doc.termlist=docLists.get(i).termlist;
				doc.termScore=docLists.get(i).termFreq;
				doc.docId=i;
				Doc doc1=docLists.get(i);
				I.add(1);
				for(int j=0;j<docList.size();j++){				
					if(i==j){
						doc.cosine.add(-1.0);
					}else{
						Doc doc2=docLists.get(j);
						double cossim=cosineSimilarity(doc1,doc2);
						doc.cosine.add(cossim);

					}
				}
		//		System.out.println(doc);
				documents.add(doc);
			}

			while(k<docList.size()){
				
				
			//Merging the documents into the same cluster
			
			double maxcosim=0.0;
			int maxId_i=0;
			int maxId_j=0;
			//Finding the most similar documents
			for(int i=0;i<documents.size();i++){
				Doc_HAC doc=documents.get(i);
				ArrayList<Double> cosim=doc.cosine;

				for(int j=0;j<cosim.size();j++){
					double current=cosim.get(j);
					if(current>maxcosim && I.get(j)==1 && I.get(i)==1 && i!=j){
						maxcosim=current;
						maxId_i=i;
						maxId_j=j;
					}
				}		
			}
			
			//Adding the merged cluster to the list of merged clusters
			ArrayList<String> merging=new ArrayList<String>();
			merging.add(""+maxId_i);
			merging.add(""+maxId_j);
			A.add(merging);
			Doc_HAC doc=documents.get(maxId_i);
			doc.merge.add(maxId_j);
			doc.count++;
			mergingDoc(maxId_i,maxId_j,documents,doc.count);
			I.set(maxId_j,0);
			
			
			//Finding out the cosine similarity between all the documents
			Doc_HAC doc1=documents.get(maxId_i);
			doc1.cosine.clear();
			I.add(1);
			for(int j=0;j<docList.size();j++){				
				if(maxId_i==j || I.get(maxId_i)==0){
					doc.cosine.add(-1.0);
				}else{
					Doc doc2=docLists.get(j);
					double cossim=cosineSimilarity(doc1,doc2);
					doc1.cosine.add(cossim);

				}
			}
			
			doc1=documents.get(maxId_j);
			doc1.cosine.clear();
			I.add(1);
			for(int j=0;j<docList.size();j++){				
				if(maxId_j==j || I.get(maxId_j)==0){
					doc.cosine.add(-1.0);
				}else{
					Doc doc2=docLists.get(j);
					double cossim=cosineSimilarity(doc1,doc2);
					doc1.cosine.add(cossim);

				}
			}
			k++;
		}
			return A;
	}
	/**
	 * Computes the cosine similarity betweet the documents 
	 * @param doc1
	 * @param doc2
	 * @return
	 */
	public double cosineSimilarity(Doc_HAC doc1, Doc doc2) {
		double num=0.0;
		double denom1=0;
		double denom2=0;
		double freq1=0;
		double freq2=0;
		ArrayList<String> termList1=doc1.termlist;
		ArrayList<String> termList2=doc2.termlist;
		ArrayList<Double> termFreq1=doc1.termFreq;
		ArrayList<Double> termFreq2=doc2.termFreq;

		for(String term : termList1){
			if(termList2.contains(term)){
				freq1=termFreq1.get(termList1.indexOf(term));
				freq2=termFreq2.get(termList2.indexOf(term));
				num+=freq1*freq2;
				denom1+=Math.pow(freq1, 2);
				denom2 += Math.pow(freq2, 2);
			}
			else{
				freq1=termFreq1.get(termList1.indexOf(term));
				denom1+=Math.pow(freq1, 2);
			}
		}
		for(String term:termList2){
			if(!termList1.contains(term)){
				freq2=termFreq2.get(termList2.indexOf(term));
				denom2 += Math.pow(freq2, 2);
			}
		}
		denom1 = Math.sqrt(denom1);
		denom2 = Math.sqrt(denom2);
		double result = num / (denom1 * denom2);

		return result;
	}
/**
 * Performs the merging of the documents
 * @param maxId_i        cluster i
 * @param maxId_j        cluster m
 * @param documents      list of documents
 * @param docCount
 */
	public void mergingDoc(int maxId_i, int maxId_j, ArrayList<Doc_HAC> documents,int docCount) {
		Doc_HAC doc1=documents.get(maxId_i);
		Doc_HAC doc2=documents.get(maxId_j);
		ArrayList<String> termList1=doc1.termlist;
		ArrayList<Double> termFreq1=doc1.termScore;

		ArrayList<String> termList2=doc2.termlist;
		ArrayList<Double> termFreq2=doc2.termScore;
		for(String term: termList2){
			if(termList1.contains(term)){
				int index=termList2.indexOf(term);
				double count=termFreq2.get(index);
				int index2=termList1.indexOf(term);
				double count2=termFreq1.get(index2);
				count2+=count;
				termFreq1.set(index2,count2);

			}
			else{
				int index=termList2.indexOf(term);
				double count=termFreq2.get(index);
				doc1.termlist.add(term);
				termFreq1.add(count);
			}
		}
		
		doc1.termFreq.clear();
		for(Double score: termFreq1){
			doc1.termFreq.add(score.doubleValue()/docCount);
		}
	}

/**
 * Prints the clusters 
 * @param clusters
 */
	public void printCluster(HashMap<Doc, ArrayList<Integer>> clusters){
		Set<Doc> keys = clusters.keySet();
		int i=0;
		System.out.println("Iteration : "+iteration);
		for(Doc doc:keys){
			System.out.println("Cluster "+i+" ");
			ArrayList<Integer> document=clusters.get(doc);
			for(Integer docs : document){
				System.out.print(docs.intValue()+"   ");
			}
			System.out.println();
			i++;
		}
		iteration++;
		System.out.println();
	}
	/**
	 * Prints the merging output of the dendrogram
	 * @param l
	 */
	public void dendogram( ArrayList<ArrayList<String >> l){
	    ArrayList<String> prev = l.get(0);
	    System.out.println(prev.toString());
	    for (int i = 1; i <l.size()  ; i++) {
	        ArrayList<String> current = l.get(i);
	        String x = current.get(0);
	        String y = current.get(1);
	        // terminating condition
	        if(prev.contains(x) && prev.contains(y)){
	            int index1 = prev.indexOf(x);
	            int index2 = prev.indexOf(y);
	            if(index1 < index2){
	                if (index1 !=0){
	                    prev.add(index1,y);
	                }else{
	                    prev.add(0,y);
	                }
	                prev.remove(index2+1);
	            }else{
	                if (index2 !=0){
	                    prev.add(index2,x);
	                }else{
	                    prev.add(0,x);
	                }
	                prev.remove(index1+1);

	            }
	        }else if (prev.contains(x)){
	            //append
	            int index = prev.indexOf(x);
	            prev.add(index+1,y);

	        }else if (prev.contains(y)){
	            // prepend
	            int index = prev.indexOf(y);
	            if (index !=0){
	                prev.add(index,x);
	            }else{
	                prev.add(0,x);
	            }
	        }else{
	            // prepend
	            prev.add(0,x);
	            prev.add(1,y);
	        }
	        System.out.println(prev.toString());
	    }
	}

	public static void main(String[] args){
		String[] docs = {"hot chocolate cocoa beans",
				"cocoa ghana africa",
				"beans harvest ghana",
				"cocoa butter",
				"butter truffles",
				"sweet chocolate can",
				"brazil sweet sugar can",
				"sugar can brazil",
				"sweet cake icing",
				"cake black forest"
		};
		Clustering c = new Clustering(2);
		System.out.println("K-Means Clustering");
		c.preprocess(docs);
	//	System.out.println(c);
		System.out.println();
		c.cluster();
		
		System.out.println("Hierarchical Agglomerative Clustering");
		Clustering c1=new Clustering();
		c1.preprocess(docs);
		System.out.println();
		ArrayList<ArrayList<String>>result=c1.simpleHAC();
		c1.dendogram(result);
		
	}

}

/**
 * 
 * @author qyuvks
 * Document class for the vector representation of a document
 */
class Doc{
	//TO BE COMPLETED
	int docId;

	ArrayList<String> termlist;

	ArrayList<Double> termFreq;
	public Doc(ArrayList<String> termList2, ArrayList<Double> termFreq2) {
		termlist=termList2;
		termFreq=termFreq2;

	}
	public String toString(){
		String docString=new String();
		docString+=docId+" : ";
		return docString;
	}
}
class Doc_HAC{
	int docId;
	int count=0;
	ArrayList<Integer> merge;
	ArrayList<Double> cosine;
	ArrayList<String> termlist;
	ArrayList<Double> termFreq;
	ArrayList<Double> termScore;
	public Doc_HAC(ArrayList<Integer> mergedList, ArrayList<Double> cossim){
		merge=mergedList;
		cosine=cossim;
		termlist=new ArrayList<String>();
		termFreq=new ArrayList<Double>();
		termScore=new ArrayList<Double>();
	}
	public String toString(){
		String docString=new String();
		docString+=String.format("%-15s",docId)+"\t";
		for(int i=0;i<cosine.size();i++){
			docString+=i+":"+cosine.get(i)+"  ";
		}
		docString+="\n";
		return docString;
	}
}
