package bitset.sizek.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.StringTokenizer;
/**
 * @author: Shanshan Wang, Chenglong Xiao
 * @description: This class is a main class, which takes the file as inputs, get the adjacent lists of the graph and calls the algorithm. 
 */
public class SubgraphEnumerationMainUsingBitSet {
	private int size;//the size of Graph
    private BitSet[] graph; // represents the given graph using BitSet
    
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
				if(line ==1)
					continue;
				else if(line==2) {
					// the size of the graph (increase the value by one as the index of the first bit in BitSet is 0, 0 is not used to represent vertex in the benchmarks)
					size = Integer.parseInt(st.nextToken())+1;
					graph = new BitSet[size];
					for (int i = 0; i < size; i++) {
						graph[i] = new BitSet();
					}
				}
				else if(line>2) {
					  int start = Integer.parseInt(st.nextToken());
					  int end = Integer.parseInt(st.nextToken());
					  graph[start ].set(end , true);
					  graph[end ].set(start , true);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    // get the connected components of graph
    public  ArrayList<BitSet> getComponents(){
		ArrayList<BitSet> components = new ArrayList();
		BitSet visited = new BitSet();
		for(int i = 1;i<size;i++) {
			if(visited.get(i)==false) {
			    BitSet C = new BitSet();
                traverse(visited,i, C);
                components.add(C);
			}
		}
		
		return components;
	}
    
    // traverse the graph in the DFS order
    public  void traverse(BitSet visited, int index, BitSet C) {
		visited.set(index, true);
		C.set(index, true);
		for(int i = graph[index].nextSetBit(0);i>=0;i=graph[index].nextSetBit(i+1)) {
			if(!visited.get(i)) {
				traverse(visited,i,C);
			}
		}
	}
    
    public BitSet[] getGraph() {
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
		SubgraphEnumerationMainUsingBitSet enumeration = new SubgraphEnumerationMainUsingBitSet();
		enumeration.readFileToGraph("graphs/"+benchmark);
		
		// call the selected algorithm to enumerate the subgraphs
		long startTime = System.currentTimeMillis();
		SubgraphEnumerationAlgorithmUsingBitSet algorithm = null;
		if(algorithmName.equals("Simple"))
		      algorithm = new SimpleSizeKUsingBitSet(enumeration.getGraph(),enumeration.getComponents(),k,enumeration.getSize(),time, outputFilePath);
		else if(algorithmName.equals("SimpleForward"))
		      algorithm = new SimpleForwardSizeKUsingBitSet(enumeration.getGraph(),enumeration.getComponents(),k,enumeration.getSize(),time,outputFilePath);
		else if(algorithmName.equals("VSimple"))
		      algorithm = new VariantSimpleSizeKUsingBitSet(enumeration.getGraph(),enumeration.getComponents(),k,enumeration.getSize(),time,outputFilePath);
		else if(algorithmName.equals("RSSP"))
		      algorithm = new RSSPSizeKUsingBitSet(enumeration.getGraph(),enumeration.getComponents(),k,enumeration.getSize(),time,outputFilePath);
		else if (algorithmName.equals("TopDown"))
		      algorithm = new TopDownEnumerationSizeKUsingBitSet(enumeration.getGraph(),enumeration.getComponents(),k,enumeration.getSize(),time,outputFilePath);
		else if (algorithmName.equals("Pivot"))
			  algorithm = new PivotSizeKUsingBitSet(enumeration.getGraph(),enumeration.getComponents(),k,enumeration.getSize(),time,outputFilePath);
		algorithm.enumerate();
		System.out.println("the number of enumerated subgraphs: " + algorithm.getCount());
		long endTime = System.currentTimeMillis();
		System.out.println("the runtime of the enumeration algorithm " +algorithmName+":" +(endTime - startTime) + "ms");
		
	}
}
