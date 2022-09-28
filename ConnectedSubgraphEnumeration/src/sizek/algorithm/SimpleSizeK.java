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
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * @author: Shanshan Wang, Chenglong Xiao
 * @description: The algorithm works in a bottom-up way. This version is implemented mainly using ArrayList, and BitSet (or boolean array) is used to ensure that the check whether a vertex is in the set in O(1) time.
 *               Note that, only in this version, O(n+m) space is fully guaranteed.
 *               The algorithm was introduced in the papers: A Faster Algorithm for Detecting Network Motifs,Enumerating Connected Induced Subgraphs: Improved
 *               Delay and Experimental Comparison.
 */

public class SimpleSizeK extends SubgraphEnumerationAlgorithm{
	
	private ArrayList<Integer> temp = new ArrayList();

	public SimpleSizeK(ArrayList<Integer>[] g, ArrayList<ArrayList<Integer>> cs, int cardinality, int s, long maximalTime,
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
			   ArrayList<Integer> c = (ArrayList)i.next();
			   // hashset is used to ensure the sequence of vertices (increasing order)
			   HashSet<Integer> vertices = new HashSet(c);
			   int forbiddenVerticesNumber = 0;
			   for(int j : vertices) {
				    // store the subgraph using ArrayList
				    ArrayList<Integer> subgraph = new ArrayList();
					subgraph.add(j);
					ArrayList<Integer> neighbors_subgraph = new ArrayList();
					BitSet neighbors_subgraph_bitset = new BitSet();
	                // initialize the neighbors of the subgraph
		            neighbors_subgraph.add(j);
		            // the bitset is used to ensure that the check of existence of a vertex can be done in O(1) time
		            neighbors_subgraph_bitset.set(j);
		            // initialize the exclusive neighbors of the subgraph (linkedlist), the linkedlist is required to ensure the visiting sequence of vertices.
		            LinkedList<Integer> ex_neighbors = new LinkedList<>();
		            for(int k: graph[j]) {
		            	if(k>j) {
		            	   ex_neighbors.add(k);
		            	   neighbors_subgraph.add(k);
		            	   neighbors_subgraph_bitset.set(k);
		            	}
		            }
		            extendSubgraph(subgraph, ex_neighbors, neighbors_subgraph, neighbors_subgraph_bitset,forbiddenVerticesNumber, j);
		            forbiddenVerticesNumber++;
					// k-component rule
					if((size-forbiddenVerticesNumber)<k) {
						break;
					}
			   }
		 } 
		 if(pw!=null)
		       pw.close();
	}

	public  boolean extendSubgraph(ArrayList<Integer> subgraph,  LinkedList<Integer> ex_neighbors, ArrayList<Integer> neighbors_subgraph, BitSet neighbors_subgraph_bitset, int forbiddenVerticesNumber, int max_index) {
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
		// a pointer to indicate the current end of temp
		int tp = temp.size()-1;
		
		while(ex_neighbors.size()>0) {
			// choose the last vertex
			Integer i = ex_neighbors.getLast();
			// remove the last vertex from the list
			ex_neighbors.removeLast();
			// pen is a pointer to indicate the current end of ex_neighbors
			int pen = ex_neighbors.size()-1;
			// update the subgraph
			subgraph.add(i);
			// temp is a global array to temporarily store the visited neighbors
		 	temp.add(i);
			// update the exclusive neighbors
		    // update the neighbors of subgraph, pns is a pointer to indicate the current end of neighbors_subgraph
			int pns = neighbors_subgraph.size()-1;
			for(Integer k: graph[i]) {
                    if(k>max_index&&!neighbors_subgraph_bitset.get(k)) {
                    	ex_neighbors.add(k);
                    	neighbors_subgraph.add(k);
                    	neighbors_subgraph_bitset.set(k);
                    }
			}
			if(extendSubgraph(subgraph,ex_neighbors,neighbors_subgraph,neighbors_subgraph_bitset,forbiddenVerticesNumber,max_index)) {
					// restore the subgraph
				    subgraph.remove(i);
				    // restore the exclusive neighbors
					int enl = ex_neighbors.size()-1;
					for(int j=enl; j>pen; j--) {
						ex_neighbors.remove(j);
					}
					// restore the neighbors of subgraph
					int ns = neighbors_subgraph.size()-1;
					for(int j=ns; j>pns; j--) {
						neighbors_subgraph_bitset.set(neighbors_subgraph.remove(j), false);
						//neighbors_subgraph.remove(j);
					}
					hasInLeaf = true;
					forbiddenVerticesNumber++;
			}
			else {
				    // restore the subgraph
				    subgraph.remove(i);
				    // restore the exclusive neighbors
					int enl = ex_neighbors.size()-1;
					for(int j=enl; j>pen; j--) {
						ex_neighbors.remove(j);
					}
					// restore the neighbors of subgraph
					int ns = neighbors_subgraph.size()-1;
					for(int j=ns; j>pns; j--) {
						neighbors_subgraph_bitset.set(neighbors_subgraph.remove(j), false);
						//neighbors_subgraph.remove(j);
					}	
					forbiddenVerticesNumber++;
					break;
			}
						
			// the k-component rule
			if((size-forbiddenVerticesNumber)<k)
					break;
		}
		// restore the exclusive neighbors before returning the parent search node
		int fs = temp.size()-1;
		for(int i=fs; i>tp; i--) {
			Integer deletedNeighbor = temp.get(i);
		    temp.remove(i);
		    ex_neighbors.addLast(deletedNeighbor);
		}	
		return hasInLeaf;
	}
}
