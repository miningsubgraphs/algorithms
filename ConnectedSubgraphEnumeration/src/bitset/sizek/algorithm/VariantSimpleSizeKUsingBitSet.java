package bitset.sizek.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * @author: Shanshan Wang, Chenglong Xiao
 * @description: The algorithm works in a bottom-up way. This version is implemented mainly using BitSet.
 *               Compared to HashSet and ArrayList£¬ BitSet is more efficient when performing set operations£¨union,intersect,etc.£©. 
 *               The algorithm is a variant of previous algorithm presented in : A Faster Algorithm for Detecting Network Motifs, Enumerating Connected Induced Subgraphs: Improved
 *               Delay and Experimental Comparison.
 */

public class VariantSimpleSizeKUsingBitSet extends SubgraphEnumerationAlgorithmUsingBitSet{

   
    public VariantSimpleSizeKUsingBitSet(BitSet[] g, ArrayList<BitSet> c, int cardinality, int s, long maximalTime,
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
			   BitSet forbiddenVertices = new BitSet();
			   for(int j = c.nextSetBit(0);j>=0;j=c.nextSetBit(j+1)) {
				    // store the subgraph using BitSet
				    BitSet subgraph = new BitSet();
					subgraph.set(j, true);
					// the neighbors of current subgraph
					BitSet neighbors = new BitSet();
					neighbors = BitSetOpt.difference(graph[j],forbiddenVertices);			
					extendSubgraph(subgraph, neighbors, forbiddenVertices);
					// the set of visited vertices that marked as forbidden vertices
					forbiddenVertices.set(j, true);
					// k component rule
					if(size-forbiddenVertices.cardinality()<k) {
						break;
					}
			   }
		 } 
		 if(pw!=null)
		       pw.close();
	}

	public  boolean extendSubgraph(BitSet subgraph, BitSet neighbors, BitSet forbiddenVertices) {
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
		BitSet newNeighbors = null;
		BitSet newForbiddenVertices = (BitSet) forbiddenVertices.clone();
		for (int i = neighbors.nextSetBit(0); i >= 0; i = neighbors.nextSetBit(i + 1)) {
			newNeighbors = (BitSet) neighbors.clone();
			// update the subgraph
			subgraph.set(i, true);
			// update the neighbors (first implementation, a faster implementation)
			newNeighbors.or(graph[i]);
			newNeighbors.andNot(subgraph);
			newNeighbors.andNot(newForbiddenVertices);
			// update the neighbors (second implementation, a slightly slower implementation)
			/*newNeighbors.set(i,false);
			neighbors.set(i, false);
			newNeighbors.or(BitSetOpt.difference(BitSetOpt.difference(graph[i], subgraph),newForbiddenVertices));*/
			if(extendSubgraph(subgraph, newNeighbors, newForbiddenVertices)) {
				subgraph.set(i, false);
				hasInLeaf = true;
			}
			else {
				subgraph.set(i, false);
				break;
			}
			// update the forbidden vertices
			newForbiddenVertices.set(i, true);
			// k-component rule
			if(size-newForbiddenVertices.cardinality()<k) {
				break;
			}
		}
		return hasInLeaf;
	}
	

}
