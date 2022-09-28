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
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * @author: Shanshan Wang, Chenglong Xiao
 * @description: The algorithm works in a bottom-up way. This version is implemented mainly using HashSet.
 *               The algorithm was introduced in the paper: A Faster Algorithm for Detecting Network Motifs, Enumerating Connected Induced Subgraphs: Improved
 *               Delay and Experimental Comparison.
 */

public class SimpleSizeKUsingHashSet extends SubgraphEnumerationAlgorithmUsingHashSet{

	public SimpleSizeKUsingHashSet(HashSet<Integer>[] g, ArrayList<HashSet<Integer>> cs, int cardinality, int s, long maximalTime,
			String path) {
		graph = g;
		components = cs;
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
			   HashSet<Integer> c = (HashSet)i.next();
			   int forbiddenVerticesNumber = 0;
			   for(int j : c) {
				    // store the subgraph using hashset
				    HashSet<Integer> subgraph = new HashSet();
					subgraph.add(j);
					HashSet<Integer> neighbors_subgraph = new HashSet();
	                // initialize the neighbors of the subgraph
		            neighbors_subgraph.add(j);
		            // initialize the exclusive neighbors of the subgraph (linkedlist), the linkedlist is required to ensure the visiting sequence of vertices.
		            LinkedList<Integer> ex_neighbors_list = new LinkedList<>();
		            for(int k: graph[j]) {
		            	if(k>j) {
		            	   ex_neighbors_list.add(k);
		            	   neighbors_subgraph.add(k);
		            	}
		            }
		            extendSubgraph(subgraph, ex_neighbors_list, neighbors_subgraph, forbiddenVerticesNumber, j);
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

	public  boolean extendSubgraph(HashSet<Integer> subgraph,  LinkedList<Integer> ex_neighbors_list, HashSet<Integer> neighbors_subgraph, int forbiddenVerticesNumber, int max_index) {
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
		
		HashSet<Integer> new_neighbors_subgraphs = null;
		LinkedList<Integer> new_ex_neighbors_list = null;
				
		Iterator iter = ex_neighbors_list.iterator();
		while(iter.hasNext()) {
			// choose the last vertex from the list
			int i = ex_neighbors_list.getLast();
			// remove the last vertex from the list
			ex_neighbors_list.removeLast();
			// update the subgraph
			subgraph.add(i);
			// get the exclusive neighbors of vertex i
			HashSet<Integer> neighbors_i = new HashSet();
			for(int k: graph[i])
                    if(k>max_index)
			            neighbors_i.add(k);
			HashSet<Integer> new_neighbors_i = HashSetOpt.difference(neighbors_i, neighbors_subgraph);
			//new_neighbors_i.remove(i);
			// add each new exclusive neighbor to the list
			new_ex_neighbors_list = (LinkedList<Integer>) ex_neighbors_list.clone();
			for(int j: new_neighbors_i) {
					new_ex_neighbors_list.add(j);
			}
			// update the neighbors of subgraph	
			new_neighbors_subgraphs = HashSetOpt.union(neighbors_subgraph, neighbors_i);
			if(extendSubgraph(subgraph,new_ex_neighbors_list,new_neighbors_subgraphs,forbiddenVerticesNumber,max_index)) {
					subgraph.remove(i);
					hasInLeaf = true;
			}
			else {
				    subgraph.remove(i);
					return hasInLeaf;
			}
			forbiddenVerticesNumber++;
			// the k component rule
			if((size-forbiddenVerticesNumber)<k)
					return hasInLeaf;
		}
			
		return hasInLeaf;
	}
	

	
}
