package sizek.algorithm;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;

public abstract class SubgraphEnumerationAlgorithm {
	protected int size = 0; // define the size of the given graph
	protected ArrayList<Integer>[] graph; // the arraylist representation of the given graph
	protected ArrayList<ArrayList<Integer>> components;// the connected components in the graph
	protected long count = 0; // count the number of enumerated subgraphs
	protected int k = 0; // the cardinality of the enumerated subgraphs
	protected String outputFilePath = null; // the file path to output the subgraphs
	protected PrintWriter pw; // printer to print out the subgraphs
	protected long  maxTime; // the maximal running time
	protected long startTime; // the start time of the algorithm
	
	
	public abstract void  enumerate();
	
	public long getCount() {
		return count;
	}
	
	public  void outputGraphToFile() {	
		try {
			pw = new PrintWriter(new FileWriter(outputFilePath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
