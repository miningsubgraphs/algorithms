
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
 *               The algorithm was introduced in the paper: Enumerating Connected Induced Subgraphs: Improved
 *               Delay and Experimental Comparison.
 */

public class SimpleForwardSizeKUsingBitSet extends SubgraphEnumerationAlgorithmUsingBitSet {

    
	public SimpleForwardSizeKUsingBitSet(BitSet[] g, ArrayList<BitSet> c, int cardinality, int s, long maximalTime,
			String path) {
		graph = g;
		components = c;
		k = cardinality;
		size = s-1;
		maxTime = maximalTime;
		outputFilePath = path;
	}

	public  void enumerate() {
		 // get the start time of the enumeration
		 startTime = System.currentTimeMillis();
		 // initialize the printer
		 if(outputFilePath!=null)
		 outputGraphToFile();
		 // enumerate connected subgraphs of order k from each connected components
		 Iterator i = components.iterator();
		 while(i.hasNext()) {
			   BitSet c = (BitSet)i.next();
			   int forbiddenVerticesNumber = 0;
			   for(int j = c.previousSetBit(c.length());j>=0;j=c.previousSetBit(j-1)) {
				    // store the subgraph using BitSet
				    BitSet subgraph = new BitSet();
					subgraph.set(j, true);
					BitSet ex_neighbors = new BitSet();
					BitSet neighbors_subgraph = new BitSet();
					// initialize the exclusive neighbors of the subgraph
					ex_neighbors =(BitSet) graph[j].clone();
					if(j<ex_neighbors.length())
					       ex_neighbors.set(j, ex_neighbors.length(), false);
	                // initialize the neighbors of the subgraph
		            neighbors_subgraph = (BitSet)ex_neighbors.clone();
		            neighbors_subgraph.set(j, true);
		            extendSubgraph(subgraph, ex_neighbors, neighbors_subgraph, forbiddenVerticesNumber, j);
		            forbiddenVerticesNumber++;
					// k component rule
					if(size-forbiddenVerticesNumber<k) {
						break;
					}
			   }
		 } 
		 if(pw!=null)
		       pw.close();
	}

	public  boolean extendSubgraph(BitSet subgraph, BitSet ex_neighbors, BitSet neighbors_subgraph,  int forbiddenVerticesNumber, int max_index) {
		if(subgraph.cardinality()==k) {
			count ++;
			// output the subgraph to the hardware disk
			if(pw!=null)
			    pw.println(subgraph);
			return true;
		}
		
		// if the running time exceeds the given maximal time, then exits the algorithm.
		long currentTime = System.currentTimeMillis();
		if((currentTime-startTime)/1000>=maxTime) {
				System.out.println("Running time exceeds the given maximal running time!  (" + maxTime +" seconds)");
				System.out.println("The number of enumerated subgraphs: " + count);
				pw.close();
				System.exit(0);
		}
		
		// a tag indicating whether an interesting leaf exists
		boolean hasInLeaf = false;
		BitSet new_ex_neighbors = null;
		BitSet new_neighbors_subgraphs = null;
		for (int i = ex_neighbors.previousSetBit(ex_neighbors.length()); i >= 0; i = ex_neighbors.previousSetBit(i - 1)) {
			subgraph.set(i, true);
			BitSet neighbors_i = (BitSet)graph[i].clone();
			if(max_index<neighbors_i.length())
			       neighbors_i.set(max_index,neighbors_i.length(), false);
			// update the exclusive neighbors
			new_ex_neighbors = (BitSet)ex_neighbors.clone();
			new_ex_neighbors.set(i, false);
			new_ex_neighbors.or(BitSetOpt.difference(neighbors_i, neighbors_subgraph));
            // update the neighbors of subgraphs	
			new_neighbors_subgraphs = BitSetOpt.union(neighbors_subgraph, neighbors_i);
			
			if(extendSubgraph(subgraph, new_ex_neighbors,new_neighbors_subgraphs,forbiddenVerticesNumber,max_index)) {
				// restore the subgraph
				subgraph.set(i, false);
				hasInLeaf = true;
				forbiddenVerticesNumber++;
			}
			else {
				// restore the subgraph
				subgraph.set(i, false);
				forbiddenVerticesNumber++;
				break;
			}
			ex_neighbors.set(i, false);
			// the k component rule
			if((size-forbiddenVerticesNumber)<k)
				break;

		}
		return hasInLeaf;
	}

	
}
