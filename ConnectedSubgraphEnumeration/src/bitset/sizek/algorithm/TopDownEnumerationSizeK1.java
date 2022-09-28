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



public class TopDownEnumerationSizeK1 extends SubgraphEnumerationAlgorithmUsingBitSet{

	private final int NIL = -1; 
	private int time = 0; // the counter to record the discovery time of each vertex
	
	public TopDownEnumerationSizeK1(BitSet[] g, ArrayList<BitSet> c, int cardinality, int s,
			String path) {
		graph = g;
		components = c;
		k = cardinality;
		size = s;
		outputFilePath = path;
	}
	

	
	public  void enumerate() {
		 // initialize the printer
		 if(outputFilePath!=null)
		 outputGraphToFile();
		 // enumerate connected subgraphs from each connected components
		 Iterator i = components.iterator();
		 while(i.hasNext()) {
			   BitSet C = (BitSet)i.next();
			   BitSet F = new BitSet();
			   topDownEnumeration(C, F, -1);
		 } 
		 if(pw!=null)
		       pw.close();
	}

	public boolean topDownEnumeration(BitSet subgraph, BitSet F, int root) {
		if(subgraph.cardinality()==k) {
			count ++;
			// output the subgraph to the hardware disk
			if(pw!=null)
			    pw.println(subgraph);
			return true;
		}
		BitSet A = new BitSet();
		// find all the vertices that are not articulation points in subgraph
		int[] Y = AP(subgraph, A, root);
		// a tag indicating whether an interesting leaf is found 
        boolean hasIntLeaf = false;
		BitSet F1 = (BitSet)F.clone();
		 
		for (int i =0; i < Y.length; i ++) {
			// set the first deleted vertex as a fixed root
			if(i>0&&root==-1)
				root = Y[0];
			if(!F1.get(Y[i])) {
				subgraph.set(Y[i], false);
				if(topDownEnumeration(subgraph, F1, root)) {
					subgraph.set(Y[i], true);
					hasIntLeaf=true;
				}
				else {
					subgraph.set(Y[i], true);
					return hasIntLeaf;
				}
				F1.set(Y[i], true);
				// pruning the search space by looking at the number of must included vertices
				if(F1.cardinality()>k)
					return hasIntLeaf;
			}
		}
		return hasIntLeaf;

	}
	
	private int[] AP( BitSet X, BitSet A, int root) {
		// rebuild the graph 'X' 
		BitSet[] adj= new BitSet[size];
		for(int k = X.nextSetBit(0); k>=0; k=X.nextSetBit(k+1))
			   adj[k]=BitSetOpt.intersect(graph[k], X);

		// Mark all the vertices as not visited
		boolean visited[] = new boolean[size];
		int disc[] = new int[size];
		int low[] = new int[size];
		int parent[] = new int[size];
		int nA [] = new int[size];
		time = 0;

		// Initialize parent and visited, and ap(articulation point) arrays
		for (int i = 0; i < size; i++) {
			parent[i] = NIL;
			visited[i] = false;
			nA[i] = NIL;
			
		}

		// Call the recursive helper function to find articulation points in DFS tree rooted with vertex 'root'
		if(root ==-1)
			root = X.nextSetBit(0);
		APUtil(root, visited, disc, low, parent, adj,  A, nA);
		int[] nonAP = new int[X.cardinality()-A.cardinality()]; 
		int j =0;
	    for(int i = 0; i<size; i++)
	    	if(nA[i]!=-1) {
	    		nonAP[j] = nA[i];
	    		j++;
	    	}
	    		
		return nonAP;
	}

	private void APUtil(int u, boolean visited[], int disc[], int low[], int parent[], BitSet[] adj, 
			BitSet A, int nA[]) {

		// Count of children in DFS Tree
		int children = 0;

		// Mark the current node as visited
		visited[u] = true;

		// Initialize discovery time and low value
		time++;
		disc[u] = time;
		low[u] = time;

		// Go through all vertices adjacent to this
		for(int v = adj[u].nextSetBit(0);v>=0;v=adj[u].nextSetBit(v+1)) {
			// v is current adjacent of u
			// If v is not visited yet, then make it a child of u
			// in DFS tree and recur for it
			if (!visited[v]) {
				children++;
				parent[v] = u;
				APUtil(v, visited, disc, low, parent, adj, A, nA);

				// Check if the subtree rooted with v has a connection to
				// one of the ancestors of u
				low[u] = Math.min(low[u], low[v]);

				// u is an articulation point in following cases

				// (1) u is root of DFS tree and has two or more chilren.
				if (parent[u] == NIL && children > 1) {
					A.set(u, true);
				}

				// (2) If u is not root and low value of one of its child
				// is more than discovery value of u.
				if (parent[u] != NIL && low[v] >= disc[u]) {
					A.set(u, true);
				}
			}

			// Update low value of u for parent function calls.
			else if (v != parent[u])
				low[u] = Math.min(low[u], disc[v]);
		}
		
		// record the non articulation points according to the discovery time
		if(!A.get(u))
		   nA[disc[u]-1] = u ;
	}


}
