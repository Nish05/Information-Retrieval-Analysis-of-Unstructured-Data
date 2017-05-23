import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class NBClassifier {
	String[] trainingDocs;
	String[] testingDocs;
	int[] trainingLabels;
	int[] testingLabels;
	int numClasses;
	ArrayList<Integer> trainlabels=new ArrayList<Integer>();
	ArrayList<Integer> testlabels=new ArrayList<Integer>();
	int[] classCounts; //number of docs per class
	String[] classStrings; //concatenated string for a given class
	int[] classTokenCounts; //total number of tokens per class
	HashMap<String,Double>[] condProb;
	HashSet<String> vocabulary; //entire vocabuary
	ArrayList<String> trainfilenames=new ArrayList<String>();
	ArrayList<String> testfilenames=new ArrayList<String>();
	int count=0;
	String filename;
	String[] stopList;
	
	/**
	 * Build a Naive Bayes classifier using a training document set
	 * @param trainDataFolder the training document folder
	 * @throws IOException 
	 */
	public NBClassifier(String trainDataFolder) throws IOException
	{
		preprocess(trainDataFolder);
		stopList=generateStopList();
		numClasses=2;
		classCounts = new int[numClasses];
		classStrings = new String[numClasses];
		classTokenCounts = new int[numClasses];
		condProb = new HashMap[numClasses];
		vocabulary = new HashSet<String>();
		for(int i=0;i<numClasses;i++){
			classStrings[i] = "";
			condProb[i] = new HashMap<String,Double>();
		}
		for(int i=0;i<trainingLabels.length;i++){
			classCounts[trainingLabels[i]]++;
			classStrings[trainingLabels[i]] += (trainingDocs[i] + " ");
		}
		for(int i=0;i<numClasses;i++){
			String[] tokens = classStrings[i].split("[ .,?!:;$%()\"--'/]+");
			
			classTokenCounts[i] = tokens.length;
			//collecting the counts
			for(String token:tokens){
				int result1=removeStopWord(token);
				if(result1==-1){
					token=stemWord(token);
					vocabulary.add(token);
					if(condProb[i].containsKey(token)){
						double count = condProb[i].get(token);
						condProb[i].put(token, count+1);
					}
					else
						condProb[i].put(token, 1.0);
				}
			}
		}
		//computing the class conditional probability
		for(int i=0;i<numClasses;i++){
			Iterator<Map.Entry<String, Double>> iterator = condProb[i].entrySet().iterator();
			int vSize = vocabulary.size();
			while(iterator.hasNext())
			{
				Map.Entry<String, Double> entry = iterator.next();
				String token = entry.getKey();
				Double count = entry.getValue();
				count = (count+1)/(classTokenCounts[i]+vSize);
				condProb[i].put(token, count);
			}
	
		}

	}
	
	/**
	 * Classify a test doc
	 * @param doc test doc
	 * @return class label
	 */
	public int classify(String doc){
		int label = 0;
		int vSize = vocabulary.size();
		double[] score = new double[numClasses];
		for(int i=0;i<score.length;i++){
			score[i] = Math.log(classCounts[i]*1.0/trainingDocs.length);
		}
		String[] tokens = doc.split("[ .,?!:;$%()\"--'/]+");
		for(int i=0;i<numClasses;i++){
			for(String token: tokens){
				int result1=removeStopWord(token);
				if(result1==-1){
					token=stemWord(token);
					if(condProb[i].containsKey(token))
						score[i] += Math.log(condProb[i].get(token));
					else
						score[i] += Math.log(1.0/(classTokenCounts[i]+vSize));
				}
			}
		}
		double maxScore = score[0];
		for(int i=0;i<score.length;i++){
			if(score[i]>maxScore)
				label = i;
		}

		return label;
	}
	/**
	 * Extracts the stop words from the file and stores it in the arraylist
	 * @return arraylist of stop words
	 * @throws IOException
	 */
	private String[] generateStopList() throws IOException {
		BufferedReader read=new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter the path of stop word folder");
		String stopListFile=read.readLine();
		ArrayList<String> stopwords=new ArrayList<String>();
		String currentline="";

		try {
			int count=0;
			BufferedReader reader=new BufferedReader(new FileReader(stopListFile));
			
			//Checks the total number of stop words in the document

			while((currentline=reader.readLine())!=null){
				stopwords.add(currentline);
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
	 * Checks whether the token is a stop word or not
	 * @param token
	 * @return if present return position else -1
	 */
	public int removeStopWord(String token) {
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
	 * Load the training documents
	 * @param trainDataFolder
	 */
	public void preprocess(String trainDataFolder)
	{
		File folder=new File(trainDataFolder);

		fileList(folder,"train");
		trainingDocs=new String[trainfilenames.size()];
		trainingLabels=new int[trainlabels.size()];
		for(int i=0;i<trainfilenames.size();i++){
			trainingDocs[i]=parse(trainfilenames.get(i));
			trainingLabels[i]=trainlabels.get(i);
		}
	}
	
	/**
	 * Stores the files of the training and testing data set in the array
	 * and assigns labels to those files 
	 * @param folder   
	 * @param folder_type
	 */
	private void fileList(File folder,String folder_type) {
		if(folder_type.equals("train")){
			listFile1(folder);
		}
		else{
			listFile2(folder);
		}


	}
	private void listFile1(File folder) {
		for(File file : folder.listFiles()){
			if(file.isDirectory()){
				listFile1(file);
			}
			else{
				if(file.isFile()){
					if(!file.getName().equals(".DS_Store")){
						if(folder.getName().equals("neg")){
							trainlabels.add(new Integer(1));
						}
						else{
							trainlabels.add(new Integer(0));
						}
						trainfilenames.add(file.getAbsolutePath());
					}
				}
			}
		}
	}
	private void listFile2(File folder) {
		for(File file : folder.listFiles()){
			if(file.isDirectory()){
				listFile2(file);
			}
			else{
				if(file.isFile()){
					if(!file.getName().equals(".DS_Store")){
						if(folder.getName().equals("neg")){
							testlabels.add(new Integer(1));
						}
						else{
							testlabels.add(new Integer(0));
						}
						testfilenames.add(file.getAbsolutePath());
					}
				}
			}
		}

	}
	/**
	 * Reads all the lines of the file
	 * @param fileName     the file to be parsed
	 * @return the file content concatenated into one string
	 */
	public String parse(String fileName) {
		String allLines="";
		String currentLine;

		try {
			BufferedReader reader=new BufferedReader(new FileReader(fileName));
			while((currentLine=reader.readLine())!=null){
				allLines+=currentLine;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return allLines;
	}
	/**
	 *  Classify a set of testing documents and report the accuracy
	 * @param testDataFolder fold that contains the testing documents
	 * @return classification accuracy
	 */
	public double classifyAll(String testDataFolder)
	{
		File folder=new File(testDataFolder);

		fileList(folder,"test");
		int true_classify=0;
		double accuracy;
		testingDocs=new String[testfilenames.size()];
		testingLabels=new int[testlabels.size()];
		for(int i=0;i<testfilenames.size();i++){
			testingDocs[i]=parse(testfilenames.get(i));
			testingLabels[i]=testlabels.get(i).intValue();
		}
		for(int i=0;i<testingDocs.length;i++){
			int label=classify(testingDocs[i]);
			if(label==testingLabels[i]){
				true_classify=true_classify+1;
			}
		}
	//	System.out.println("True classify is : "+true_classify);
		System.out.println("Correctly classified "+true_classify+" out of "+testingDocs.length);
		accuracy=(true_classify*1.0)/testingDocs.length;
		return accuracy;
	}


	public static void main(String[] args)throws IOException
	{		
		BufferedReader read=new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter the path of the training folder");
		String path=read.readLine();
		System.out.println("Enter the path of the testing folder");
		String path1=read.readLine();
		NBClassifier nb=new NBClassifier(path);
		double accuracy=nb.classifyAll(path1);
		System.out.println("Accuracy : "+accuracy);
	}
}
