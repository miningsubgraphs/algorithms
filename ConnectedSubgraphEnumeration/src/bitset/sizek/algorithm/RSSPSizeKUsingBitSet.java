package bitset.sizek.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.StringTokenizer;
/**
 * @author: Shanshan Wang, Chenglong Xiao
 * @description: The algorithm works in a bottom-up way. This version is implemented mainly using BitSet.
 *               Compared to HashSet and ArrayList£¬ BitSet is more efficient when performing set operations£¨union,intersect,etc.£©. 
 *               The algorithm is a variant of previous algorithm presented in : A linear delay algorithm for 
 *               enumerating all connected induced subgraphs.
 */
public class RSSPSizeKUsingBitSet extends SubgraphEnumerationAlgorithmUsingBitSet{

    private int distances[];
    private int subgraph[];

    
    public RSSPSizeKUsingBitSet(BitSet[] g, ArrayList<BitSet> c, int cardinality, int s, long maximalTime,
			String path) {
		graph = g;
		components = c;
		k = cardinality;
		size = s;
		maxTime = maximalTime;
		outputFilePath = path;
	}

	public  void enumerate() {
		 // get the start time of the enumeration
		 startTime = System.currentTimeMillis();
		 // initialize the printer
		 if(outputFilePath!=null)
			 outputGraphToFile();
		 // enumerate connected subgraphs from each connected components
		 Iterator iterator = components.iterator();
		 while(iterator.hasNext()) {
			   BitSet c = (BitSet)iterator.next();
			   for(int i = c.nextSetBit(0);i>=0;i=c.nextSetBit(i+1)) {
				    int[] subgraph =new int[size];
					subgraph[0] = i;
					// initialize the neighbors of vertex i
					BitSet neighbors = (BitSet)graph[i].clone();
					int[] distances = new int[size];
					// initialize the distances
					for(int j = 0; j<distances.length; j++)
						distances[j] = -1;
					distances[i]=0;
					// update the distance to each neighbors
					for(int j = neighbors.nextSetBit(0);j>=0;j=neighbors.nextSetBit(j+1)) {
						distances[j]=1;
					}
					int pointer = 0;
					// the subgraph stored in BitSet
					BitSet subgraphSet = new BitSet();
					subgraphSet.set(i, true);
					EnumerateCIS(subgraph, pointer, subgraphSet, neighbors, distances);
			   }
		 } 
		 if(pw!=null)
		       pw.close();
	}

	public  void EnumerateCIS(int[] subgraph, int pointer, BitSet subgraphSet, BitSet neighbors, int[] distances) {
		if(subgraphSet.cardinality()==k) {
			count ++;
			// output the subgraph to the hardware disk
			if(pw!=null)
			    pw.println(subgraphSet);
			return;
		}
		// if the running time exceeds the given maximal time, then exits the algorithm.
		long currentTime = System.currentTimeMillis();
		if((currentTime-startTime)/1000>=maxTime) {
				System.out.println("Running time exceeds the given maximal running time!  (" + maxTime +" seconds)");
				System.out.println("The number of enumerated subgraphs: " + count);
				pw.close();
				System.exit(0);
		}
        for(int i = neighbors.nextSetBit(0);i>=0;i=neighbors.nextSetBit(i+1)) {
        	// call isValidExtension to check each neighbor
        	if(isValidExtension(i,subgraph[0],subgraph[pointer],distances)) {
        		int[] new_subgraph = subgraph.clone();
        		BitSet newSubgraphSet = (BitSet)subgraphSet.clone();
        		// update the subgraph
        		new_subgraph[pointer+1]=i;
        		newSubgraphSet.set(i, true);
        		// update the neighbors
                BitSet new_neighbors = (BitSet)neighbors.clone();
                new_neighbors.or(graph[i]);
                new_neighbors.andNot(newSubgraphSet);
                // update the distance
                int[] new_distances = distances.clone();
                for(int j = graph[i].nextSetBit(0);j>=0;j=graph[i].nextSetBit(j+1)) {
                	if(new_distances[j]<0)
                		new_distances[j]= distances[new_subgraph[pointer+1]]+1;
                }
        		EnumerateCIS(new_subgraph,pointer+1,newSubgraphSet,new_neighbors,new_distances);
        	}
        }
	}

	// check if vertex v is valid
	public  boolean isValidExtension(int v, int first, int last, int[] distances) {
		if(v<first)
			return false;
		if(distances[v]>distances[last])
			return true;
		return distances[v]==distances[last]&&v>last;
	}
	
	
	
}
