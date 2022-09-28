package arraylist.sizek.algorithm;

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
 * @description: The algorithm works in a bottom-up way. This version is implemented mainly using ArrayList, and boolean arrays are used to ensure that the check whether a vertex is in the set in O(1) time.
 *               Note that, only in this version, O(n+m) space is fully guaranteed.
 *               The algorithm is a variant of previous algorithm presented in : A Faster Algorithm for Detecting Network Motifs, Enumerating Connected Induced Subgraphs: Improved
 *               Delay and Experimental Comparison.
 */
public class VariantSimpleSizeKUsingArrayList extends SubgraphEnumerationAlgorithmUsingArrayList {

	public VariantSimpleSizeKUsingArrayList(ArrayList<Integer>[] g, ArrayList<ArrayList<Integer>> arrayList,
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
			boolean[] forbiddenVerticesBooleanArray = new boolean[size+1];
			// store the subgraph using List
			ArrayList<Integer> subgraph = new ArrayList<Integer>();
			boolean[] subgraphBooleanArray = new boolean[size+1];
			// the neighbors of current subgraph
			ArrayList<Integer> neighbors = new ArrayList<Integer>();
			boolean[] neighborsBooleanArray = new boolean[size+1];
			for (int j : c) {
				
				subgraph.add(j);
				subgraphBooleanArray[j]= true;
				
				for(int k: graph[j]) {
					if(!forbiddenVerticesBooleanArray[k]) {
					  neighbors.add(k);
					  neighborsBooleanArray[k]=true;
					}
				}
				extendSubgraph(subgraph, neighbors, forbiddenVertices,subgraphBooleanArray,neighborsBooleanArray,forbiddenVerticesBooleanArray);
			    // restore the subgraph
				subgraphBooleanArray[subgraph.remove(subgraph.size()-1)] = false;
				// restore the neighbors
				for(int l = neighbors.size()-1;l>=0;l--)
					neighborsBooleanArray[neighbors.remove(l)] = false;
			    // the set of visited vertices that marked as forbidden vertices
				forbiddenVertices.add(j);
				// the boolean array is used to check if a vertex in the forbiddenVertices set in O(1) time
				forbiddenVerticesBooleanArray[j]= true;
				// k-component rule
				if(size-forbiddenVertices.size()<k) {
					break;
				}
			}
		}
		if (pw != null)
			pw.close();
	}

	public boolean extendSubgraph(ArrayList<Integer> subgraph, ArrayList<Integer> neighbors,
			ArrayList<Integer> forbiddenVertices,boolean[] subgraphBooleanArray,boolean[] neighborsBooleanArray, boolean[] forbiddenVerticesBooleanArray) {
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
			    Integer i = neighbors.remove(neighbors.size()-1);
				// update the subgraph
				subgraph.add(i);
				subgraphBooleanArray[i]=true;
				// the pointer to indicate  the last index  of the current neighbors set
				neighborsBooleanArray[i]=false;
				int pn = neighbors.size()-1;
			    // update the neighbors
				for(Integer j:graph[i])
					if(!subgraphBooleanArray[j]&&!neighborsBooleanArray[j]&&!forbiddenVerticesBooleanArray[j]) {
						neighbors.add(j);
						neighborsBooleanArray[j]=true;
					}
				if (extendSubgraph(subgraph, neighbors, forbiddenVertices,subgraphBooleanArray,neighborsBooleanArray,forbiddenVerticesBooleanArray)) {
					// restore the subgraph
					subgraphBooleanArray[subgraph.remove(subgraph.size()-1)]=false;
					
					// restore the neighbors
					int ns = neighbors.size()-1;
					for(int j=ns; j>pn; j--) {
						neighborsBooleanArray[neighbors.remove(neighbors.size()-1)]= false;
					}
    				// update the forbidden vertices
					forbiddenVertices.add(i);
					forbiddenVerticesBooleanArray[i]=true;
					hasInLeaf = true;
				} else {
					// restore the subgraph
					subgraphBooleanArray[subgraph.remove(subgraph.size()-1)]= false;
					// restore the neighbors
					int ns = neighbors.size()-1;
					for(int j=ns; j>pn; j--) {
						neighborsBooleanArray[neighbors.remove(neighbors.size()-1)]=false;
					}
					// update the forbidden vertices
					forbiddenVertices.add(i);
					forbiddenVerticesBooleanArray[i]=true;
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
			Integer deletedNeighbor = forbiddenVertices.remove(i);
			forbiddenVerticesBooleanArray[deletedNeighbor]=false;
			neighbors.add(deletedNeighbor);
			neighborsBooleanArray[deletedNeighbor]=true;
		}
		return hasInLeaf;
	}
}
