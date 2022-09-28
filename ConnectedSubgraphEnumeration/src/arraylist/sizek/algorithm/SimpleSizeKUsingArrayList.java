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
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * @author: Shanshan Wang, Chenglong Xiao
 * @description: The algorithm works in a bottom-up way. This version is implemented mainly using ArrayList, and boolean arrays are used to ensure that the check whether a vertex is in the set in O(1) time.
 *               Note that, only in this version, O(n+m) space is fully guaranteed.
 *               The algorithm was introduced in the papers: A Faster Algorithm for Detecting Network Motifs,Enumerating Connected Induced Subgraphs: Improved
 *               Delay and Experimental Comparison.
 */

public class SimpleSizeKUsingArrayList extends SubgraphEnumerationAlgorithmUsingArrayList{
	
	private ArrayList<Integer> temp = new ArrayList();
	
	public SimpleSizeKUsingArrayList(ArrayList<Integer>[] g, ArrayList<ArrayList<Integer>> cs, int cardinality, int s, long maximalTime,
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
			   // store the subgraph using LinkedList
			   ArrayList<Integer> subgraph = new ArrayList();
			   // store the neighbors of subgraph
			   ArrayList<Integer> neighbors_subgraph = new ArrayList();
			   boolean[] neighbors_subgraph_booleanarray = new boolean[size+1];
			   // store the exclusive neighbors of the subgraph, linked list is required, as the simple and simple forward will 
			   // selected vertex from the start or the end respectively. Using array list is inefficient for deleting vertex from the beginning of the list.
			   LinkedList<Integer> ex_neighbors = new LinkedList<>();
			   int forbiddenVerticesNumber = 0;
			   for(Integer j : vertices) {
				   
					subgraph.add(j);
	                // initialize the neighbors of the subgraph
		            neighbors_subgraph.add(j);
		            // the boolean array is used to ensure that the check of existence of a vertex can be done in O(1) time
		            neighbors_subgraph_booleanarray[j]=true;
		            // initialize the exclusive neighbors of the subgraph (linkedlist)
		            for(Integer v: graph[j]) {
		            	if(v>j) {
		            	   ex_neighbors.add(v);
		            	   neighbors_subgraph.add(v);
		            	   neighbors_subgraph_booleanarray[v]=true;
		            	}
		            }
		            extendSubgraph(subgraph,  ex_neighbors, neighbors_subgraph, neighbors_subgraph_booleanarray,forbiddenVerticesNumber, j);
		            // restore the subgraph
		            subgraph.remove(subgraph.size()-1);
		            
		            // restore the neighbors and exclusive neighbors of the subgraph
		            for(int l = ex_neighbors.size(); l>0; l--) {
		            	ex_neighbors.removeLast();
		            	neighbors_subgraph_booleanarray[neighbors_subgraph.remove(neighbors_subgraph.size()-1)] = false;
		            }
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

	public  boolean extendSubgraph(ArrayList<Integer> subgraph, LinkedList<Integer> ex_neighbors, ArrayList<Integer> neighbors_subgraph, boolean[] neighbors_subgraph_booleanarray, int forbiddenVerticesNumber, int max_index) {
		if(subgraph.size()==k) {
			count ++;
			// output the subgraph to the hardware disk
			if(pw!=null) {
			        pw.println(subgraph);
			}
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
                    if(k>max_index&&!neighbors_subgraph_booleanarray[k]) {
                    	ex_neighbors.add(k);
                    	neighbors_subgraph.add(k);
                    	neighbors_subgraph_booleanarray[k]=true;
                    }
			}
			if(extendSubgraph(subgraph, ex_neighbors,neighbors_subgraph,neighbors_subgraph_booleanarray,forbiddenVerticesNumber,max_index)) {
					// restore the subgraph
				    subgraph.remove(subgraph.size()-1);
				    // restore the exclusive neighbors
					int enl = ex_neighbors.size()-1;
					for(int j=enl; j>pen; j--) {
						ex_neighbors.removeLast();
					}
					// restore the neighbors of subgraph
					int ns = neighbors_subgraph.size()-1;
					for(int j=ns; j>pns; j--) {
						neighbors_subgraph_booleanarray[neighbors_subgraph.remove(neighbors_subgraph.size()-1)]= false;
						//neighbors_subgraph.remove(j);
					}
					hasInLeaf = true;
					forbiddenVerticesNumber++;
			}
			else {
				    // restore the subgraph
				    subgraph.remove(subgraph.size()-1);
				    // restore the exclusive neighbors
					int enl = ex_neighbors.size()-1;
					for(int j=enl; j>pen; j--) {
						ex_neighbors.removeLast();
					}
					// restore the neighbors of subgraph
					int ns = neighbors_subgraph.size()-1;
					for(int j=ns; j>pns; j--) {
						neighbors_subgraph_booleanarray[neighbors_subgraph.remove(neighbors_subgraph.size()-1)]= false;
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
			Integer deletedNeighbor =  temp.remove(i);
		    ex_neighbors.addLast(deletedNeighbor);
		}	
		return hasInLeaf;
	}
}
