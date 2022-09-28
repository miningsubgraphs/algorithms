package hashset.sizek.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * @author: Shanshan Wang, Chenglong Xiao
 * @description: The algorithm works in a bottom-up way. This version is implemented mainly using HashSet.
 *               The algorithm is a variant of previous algorithm presented in : A Faster Algorithm for Detecting Network Motifs, Enumerating Connected Induced Subgraphs: Improved
 *               Delay and Experimental Comparison.
 */

public class VariantSimpleSizeKUsingHashSet extends SubgraphEnumerationAlgorithmUsingHashSet{

   
    public VariantSimpleSizeKUsingHashSet(HashSet<Integer>[] g, ArrayList<HashSet<Integer>> arrayList, int cardinality, int s, long maximalTime,
			String path) {
		graph = g;
		components = arrayList;
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
	     Iterator iterator1 = components.iterator();
		 while(iterator1.hasNext()) {
			 HashSet<Integer> c = (HashSet<Integer>)iterator1.next();
			 HashSet<Integer> forbiddenVertices = new HashSet<Integer>();
			 for(int j: c) {
				   // store the subgraph using BitSet
				   HashSet<Integer> subgraph = new HashSet<Integer>();
				   subgraph.add(j);
				   // the neighbors of current subgraph
				   HashSet<Integer> neighbor = new HashSet<Integer>();
				   neighbor = HashSetOpt.difference(graph[j],forbiddenVertices);			
				   extendSubgraph(subgraph, neighbor, forbiddenVertices);
				   // the set of visited vertices that marked as forbidden vertices
				   forbiddenVertices.add(j);
			   }
		 } 
		 if(pw!=null)
		       pw.close();
	}

	public  boolean extendSubgraph(HashSet<Integer> subgraph, HashSet<Integer> neighbors, HashSet<Integer> forbiddenVertices) {
		if(subgraph.size()==k) {
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
		HashSet<Integer> newNeighbors = null;
		HashSet<Integer> newForbiddenVertices = (HashSet<Integer>) forbiddenVertices.clone();
		Iterator iter = neighbors.iterator();
		while(iter.hasNext()) {
			// choose a vertex
			Integer i = (Integer) iter.next();
			// remove the selected vertex from the list
			iter.remove();
			newNeighbors = (HashSet<Integer>) neighbors.clone();
			// update the subgraph
			subgraph.add(i);
			// update the neighbors (first implementation)
            /* newNeighbors.addAll(HashSetOpt.difference(graph[i],subgraph));
               newNeighbors.remove(i);
               newNeighbors.removeAll(newForbiddenVertices);*/
			// update the neighbors (second implementation) 
            newNeighbors.addAll(HashSetOpt.difference(HashSetOpt.difference(graph[i],subgraph),newForbiddenVertices));
			if(extendSubgraph(subgraph, newNeighbors, newForbiddenVertices)) {
				subgraph.remove(i);
				hasInLeaf = true;
			}
			else {
				subgraph.remove(i);
				return hasInLeaf;
			}
			// update the forbidden vertices
			newForbiddenVertices.add(i);
			if(size-newForbiddenVertices.size()<k) {
				return hasInLeaf;
			}
		}
		return hasInLeaf;
	}
}
