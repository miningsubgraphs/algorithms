package sizek.algorithm;

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
import java.util.LinkedList;
import java.util.StringTokenizer;
/**
 * @author: Shanshan Wang, Chenglong Xiao
 * @description: The algorithm works in a bottom-up way. This version is implemented mainly using ArrayList, and BitSet (or boolean array) is used to ensure that the check whether a vertex is in the set in O(1) time.
 *               Note that, only in this version, O(n+m) space is fully guaranteed.
 *               The algorithm is a variant of previous algorithm presented in : A Faster Algorithm for Detecting Network Motifs, Enumerating Connected Induced Subgraphs: Improved
 *               Delay and Experimental Comparison.
 */
public class ImprovedSimpleSizeK extends SubgraphEnumerationAlgorithm {

	public ImprovedSimpleSizeK(ArrayList<Integer>[] g, ArrayList<ArrayList<Integer>> arrayList,
			int cardinality, int s, long maximalTime, String path) {
		graph = g;
		components = arrayList;
		k = cardinality;
		size = s-1;
		maxTime = maximalTime;
		outputFilePath = path;
	}

	public void enumerate() {
		// get the start time of the enumeration
		startTime = System.currentTimeMillis();
		// initialize the printer
		if (outputFilePath != null)
			outputGraphToFile();
		// enumerate connected subgraphs of order k from each connected components
		Iterator iterator1 = components.iterator();
		while (iterator1.hasNext()) {
			ArrayList<Integer> c = (ArrayList<Integer>) iterator1.next();
			ArrayList<Integer> forbiddenVertices = new ArrayList<Integer>();
			BitSet forbiddenVerticesBitSet = new BitSet();
			for (int j : c) {
				// store the subgraph using BitSet
				ArrayList<Integer> subgraph = new ArrayList<Integer>();
				BitSet subgraphBitSet = new BitSet();
				subgraph.add(j);
				subgraphBitSet.set(j, true);
				// the neighbors of current subgraph
				LinkedList<Integer> neighbors = new LinkedList<Integer>();
				BitSet neighborsBitSet = new BitSet();
				for(int k: graph[j]) {
					if(!forbiddenVerticesBitSet.get(k)) {
					  neighbors.add(k);
					  neighborsBitSet.set(k, true);
					}
				}
				extendSubgraph(subgraph, neighbors, forbiddenVertices,subgraphBitSet,neighborsBitSet,forbiddenVerticesBitSet);
				// the set of visited vertices that marked as forbidden vertices
				forbiddenVertices.add(j);
				// the BitSet is used to check if a vertex in the forbiddenVertices set in O(1) time
				forbiddenVerticesBitSet.set(j, true);
				// k-component rule
				if(size-forbiddenVertices.size()<k) {
					break;
				}
			}
		}
		if (pw != null)
			pw.close();
	}

	public boolean extendSubgraph(ArrayList<Integer> subgraph, LinkedList<Integer> neighbors,
			ArrayList<Integer> forbiddenVertices,BitSet subgraphBitSet,BitSet neighborsBitSet, BitSet forbiddenVerticesBitSet) {
		if (subgraph.size() == k) {
			count++;
			// output the subgraph to the hardware disk
			if (pw != null)
				pw.println(subgraph);
			return true;
		}
		// if the running time exceeds the given maximal time, then exits the algorithm.
		long currentTime = System.currentTimeMillis();
		if ((currentTime - startTime) / 1000 >= maxTime) {
			System.out.println("Running time exceeds the given maximal running time!  (" + maxTime + " seconds)");
			System.out.println("The number of enumerated subgraphs: " + count);
			pw.close();
			System.exit(0);
		}
		// a tag indicating whether an interesting leaf exists
		boolean hasInLeaf = false;
		// the pointer to indicate  the last index  of the current forbidden vertices set
		int pf = forbiddenVertices.size()-1;
        // add each neighbor to extend the subgraph
		while (neighbors.size()>0) {
			    Integer i = neighbors.getLast();
				// update the subgraph
				subgraph.add(i);
				subgraphBitSet.set(i);
				// the pointer to indicate  the last index  of the current neighbors set
				neighborsBitSet.set(neighbors.getLast(),false);
				neighbors.removeLast();
				int pn = neighbors.size()-1;
			    // update the neighbors
				for(Integer j:graph[i])
					if(!subgraphBitSet.get(j)&&!neighborsBitSet.get(j)&&!forbiddenVerticesBitSet.get(j)) {
						neighbors.add(j);
						neighborsBitSet.set(j);
					}
				if (extendSubgraph(subgraph, neighbors, forbiddenVertices,subgraphBitSet,neighborsBitSet,forbiddenVerticesBitSet)) {
					// restore the subgraph
					subgraphBitSet.set(i, false);
					subgraph.remove(subgraph.size()-1);
					// restore the neighbors
					int ns = neighbors.size()-1;
					for(int j=ns; j>pn; j--) {
						neighborsBitSet.set(neighbors.get(j), false);
						neighbors.remove(j);
					}
    				// update the forbidden vertices
					forbiddenVertices.add(i);
					forbiddenVerticesBitSet.set(i);
					hasInLeaf = true;
				} else {
					// restore the subgraph
					subgraphBitSet.set(i, false);
					subgraph.remove(subgraph.size()-1);
					// restore the neighbors
					int ns = neighbors.size()-1;
					for(int j=ns; j>pn; j--) {
						neighborsBitSet.set(neighbors.get(j), false);
						neighbors.remove(j);
					}
					// update the forbidden vertices
					forbiddenVertices.add(i);
					forbiddenVerticesBitSet.set(i);
					break;
				}
				// k-component rule
				if (size - forbiddenVertices.size() < k) {
					break;
				}
		}
		
		// restore the forbidden vertices set and neighbor vertices set
		int fs = forbiddenVertices.size()-1;
		for(int i=fs; i>pf; i--) {
			Integer deletedNeighbor = forbiddenVertices.get(i);
			forbiddenVertices.remove(i);
			forbiddenVerticesBitSet.set(deletedNeighbor, false);
			neighbors.add(deletedNeighbor);
			neighborsBitSet.set(deletedNeighbor);
		}
		return hasInLeaf;
	}
}
