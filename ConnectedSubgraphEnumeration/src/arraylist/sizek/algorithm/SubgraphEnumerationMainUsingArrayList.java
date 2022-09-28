package arraylist.sizek.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.StringTokenizer;

import arraylist.sizek.algorithm.SimpleForwardSizeKUsingArrayList;
import arraylist.sizek.algorithm.SimpleSizeKUsingArrayList;
import bitset.sizek.algorithm.VariantSimpleSizeKUsingBitSet;
import bitset.sizek.algorithm.RSSPSizeKUsingBitSet;
import bitset.sizek.algorithm.SubgraphEnumerationAlgorithmUsingBitSet;
import bitset.sizek.algorithm.TopDownEnumerationSizeKUsingBitSet;

import java.util.HashSet;
import java.util.Iterator;
/**
 * @author: Shanshan Wang, Chenglong Xiao
 * @description: This class is a main class, which takes the file as inputs, get the adjacent lists of the graph and calls the algorithm. 
 */
public class SubgraphEnumerationMainUsingArrayList {
	private int size;//the size of Graph
    private ArrayList<Integer>[] graph; // represents the given graph using ArrayList
    
    // read the file and store the graph using BitSets
    public  void readFileToGraph(String filePath) {
		File file = new File(filePath);	
		BufferedReader br = null;
		String str = null;
		try {
			br = new BufferedReader(new FileReader(file));
			int line = 0;
			while ((str = br.readLine()) != null) {
				line++;
				StringTokenizer st = new StringTokenizer(str);
				if(st.countTokens()==1) {		
					continue;
				}
				else if(line==2) {
					size = Integer.parseInt(st.nextToken())+1;
					graph = new ArrayList[size];
		            for (int i = 0; i < size; i++) {
			            graph[i] = new ArrayList<Integer>();
		            }
				}
				else if(line>2) {
				  int start = Integer.parseInt(st.nextToken());
				  int end = Integer.parseInt(st.nextToken());
				  graph[start].add(end);
				  graph[end].add(start);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    // get the connected components of graph
    public  ArrayList<ArrayList<Integer>> getComponents(){
		ArrayList<ArrayList<Integer>> components = new ArrayList<ArrayList<Integer>> ();
		ArrayList<Integer> visited = new ArrayList<Integer>();
		for(int i = 1;i<size-1;i++) {
			if(!visited.contains(i)) {
				ArrayList<Integer> C = new ArrayList<Integer>();
                traverse(visited,i, C);
                components.add(C);
			}
		}
		
		return components;
	}
    
    // traverse the graph in the DFS order
    public  void traverse(ArrayList<Integer>  visited, int index, ArrayList<Integer>  C) {
		visited.add(index);
		C.add(index);
		Iterator iterator = graph[index].iterator();
		while (iterator.hasNext()) {
			int i = (int) iterator.next();
			if(!visited.contains(i)) {
				traverse(visited,i,C);
			}
		}	
	}
    
    public ArrayList<Integer>[] getGraph() {
    	return graph;
    }
    
    public int getSize() {
    	return size;
    }
    
	public static void main(String[] args) {
		String benchmark = args[0];// the benchmark graph
		int k = Integer.parseInt(args[1]); // the cardinality of enumerated subgraphs
		long time = Long.parseLong(args[2]); // the maximal running time (in seconds)
		String algorithmName = args[3]; // the name of the enumeration algorithm
				
		String outputFilePath = "outputs/"+benchmark+"_"+k; // the path to store the enumerated subgraphs in the hardware disk
		SubgraphEnumerationMainUsingArrayList enumeration = new SubgraphEnumerationMainUsingArrayList();
		enumeration.readFileToGraph("graphs/"+benchmark);
		
		// call the selected algorithm to enumerate the subgraphs
		SubgraphEnumerationAlgorithmUsingArrayList algorithm  = null;
		if(algorithmName.equals("Simple"))
		      algorithm = new SimpleSizeKUsingArrayList(enumeration.getGraph(),enumeration.getComponents(),k,enumeration.getSize(),time, outputFilePath);
		else if(algorithmName.equals("SimpleForward"))
		      algorithm = new SimpleForwardSizeKUsingArrayList(enumeration.getGraph(),enumeration.getComponents(),k,enumeration.getSize(),time,outputFilePath);
		else if(algorithmName.equals("VSimple"))
		      algorithm = new VariantSimpleSizeKUsingArrayList(enumeration.getGraph(),enumeration.getComponents(),k,enumeration.getSize(),time,outputFilePath);
		else if(algorithmName.equals("TopDown"))
			algorithm = new TopDownEnumerationSizeKUsingArrayList(enumeration.getGraph(),enumeration.getComponents(),k,enumeration.getSize(),time,outputFilePath);
		long startTime = System.currentTimeMillis();
		algorithm.enumerate();
		System.out.println("the number of enumerated subgraphs: " + algorithm.getCount());
		long endTime = System.currentTimeMillis();
		System.out.println("the runtime of the enumeration algorithm " +":" +(endTime - startTime) + "ms");
		
	}
}
