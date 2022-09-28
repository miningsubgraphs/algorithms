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
import java.util.Stack;
import java.util.StringTokenizer;



public class TopDownEnumerationSizeK2 extends SubgraphEnumerationAlgorithmUsingBitSet{

	private final int NIL = -1; 
	private int time = 0; // the counter to record the discovery time of each vertex
	
	public TopDownEnumerationSizeK2(BitSet[] g, ArrayList<BitSet> c, int cardinality, int s,
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
		 // enumerate connected subgraphs from each connected component
		 Iterator i = components.iterator();
		 while(i.hasNext()) {
			   BitSet C = (BitSet)i.next();
			   BitSet F = new BitSet();
			   BitSet[] subgraphWithEdges = new BitSet[size];
			   for(int k = C.nextSetBit(0); k>=0; k=C.nextSetBit(k+1))
				   subgraphWithEdges[k]=BitSetOpt.intersect(graph[k], C);
			   topDownEnumeration(subgraphWithEdges,C, F, -1);
		 } 
		 if(pw!=null)
		       pw.close();
	}

	public boolean topDownEnumeration(BitSet[] subgraphWithEdges, BitSet subgraph, BitSet F, int root) {
		if(subgraph.cardinality()==k) {
			count ++;
			// output the subgraph to the hardware disk
			if(pw!=null)
			    pw.println(subgraph);
			return true;
		}
		
		BitSet A = new BitSet();
		// find all the vertices that are not articulation points in subgraph
		int[] Y = AP(subgraphWithEdges,subgraph, A, root);
		// a tag indicating whether an interesting leaf is found 
        boolean hasIntLeaf = false;
		BitSet F1 = (BitSet)F.clone();
		 
		for (int i =0; i < Y.length; i ++) {
			// set the first deleted vertex as a fixed root
			if(i>0&&root==-1)
				root = Y[0];
			if(!F1.get(Y[i])) {
				subgraph.set(Y[i], false);
				// update the matrix representation of the subgraph
				for(int j = subgraphWithEdges[Y[i]].nextSetBit(0);j>=0;j=subgraphWithEdges[Y[i]].nextSetBit(j+1)) {
					subgraphWithEdges[j].set(Y[i], false);
				}
				if(topDownEnumeration(subgraphWithEdges,subgraph, F1, root)) {
					subgraph.set(Y[i], true);
					// restore the matrix representation of the subgraph
					for(int j = subgraphWithEdges[Y[i]].nextSetBit(0);j>=0;j=subgraphWithEdges[Y[i]].nextSetBit(j+1)) {
						subgraphWithEdges[j].set(Y[i], true);
					}
					hasIntLeaf=true;
				}
				else {
					subgraph.set(Y[i], true);
					// restore the matrix representation of the subgraph
					for(int j = subgraphWithEdges[Y[i]].nextSetBit(0);j>=0;j=subgraphWithEdges[Y[i]].nextSetBit(j+1)) {
						subgraphWithEdges[j].set(Y[i], true);
					}
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
	
	public  boolean isConnectedSubgraph(BitSet G){
		BitSet visited = new BitSet();
		int i = G.nextSetBit(0);
        traverse(visited, G, i);
        if(visited.cardinality()==G.cardinality())
        	return true;
        else 
        	return false;
	}
	
	public  void traverse(BitSet visited, BitSet G, int index) {
		visited.set(index, true);
		BitSet adjacentVertices = BitSetOpt.intersect(G, graph[index]);
		for(int i = adjacentVertices.nextSetBit(0);i>=0;i=adjacentVertices.nextSetBit(i+1)) {
			if(!visited.get(i)) {
				traverse(visited,G,i);
			}
		}
	}
	
	
	private int[] AP( BitSet[] adj, BitSet X, BitSet A, int root) {
	
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
		APUtil(root, X.cardinality(), visited, disc, low, parent, adj,  A, nA);
		int[] nonAP = new int[X.cardinality()-A.cardinality()]; 
		int j =0;
	    for(int i = 0; i<size; i++)
	    	if(nA[i]!=-1) {
	    		nonAP[j] = nA[i];
	    		j++;
	    	}
	    		
		return nonAP;
	}

	private void APUtil(int u, int number, boolean visited[], int disc[], int low[], int parent[], BitSet[] adj, 
			BitSet A, int nA[]) {

		// Mark the current node as visited
		visited[u] = true;

		// Initialize discovery time and low value
		time++;
		disc[u] = time;
		low[u] = time;
		
		 //new code
		 int[] stack = new int[number];
		 int[] children = new int[size];
		 int top =-1;
		 int returnPoint =-1;
		 stack[++top] = u;
	     while(top!=-1){
	    	    boolean end = false;
	    	    int currentVertex = stack[top];
	    	    if(adj[currentVertex].nextSetBit(returnPoint+1)<0)
            		end = true;
	            for(int v = adj[currentVertex].nextSetBit(returnPoint+1);v>=0;v=adj[currentVertex].nextSetBit(v+1)) {
	            	if(adj[currentVertex].nextSetBit(v+1)<0)
	            		end = true;
	            	if(!visited[v]) {
	            		returnPoint = -1;
	            		time++;
	            		disc[v] = time;
	            		low[v] = time;
	            		children[currentVertex]++;
	            		parent[v]=currentVertex;
	            		visited[v]=true;
	            		stack[++top]=v;
	            		end = false;
	            		break;
	            	}
	            	// Update low value of u for parent function calls.
	    			else if (v != parent[currentVertex])
	    				low[currentVertex] = Math.min(low[currentVertex], disc[v]);
	            	
	            }
	           
	            if(end==true) {
	            	returnPoint = currentVertex;
	            	if(parent[currentVertex]!=-1) {
	            	// Check if the subtree rooted with v has a connection to
					// one of the ancestors of u
					low[parent[currentVertex]] = Math.min(low[parent[currentVertex]], low[currentVertex]);

					// u is an articulation point in following cases

					// (1) u is root of DFS tree and has two or more children.
					if (parent[parent[currentVertex]] == NIL && children[parent[currentVertex]] > 1) {
						A.set(parent[currentVertex], true);
					}

					// (2) If u is not root and low value of one of its child
					// is more than discovery value of u.
					if (parent[parent[currentVertex]] != NIL && low[currentVertex] >= disc[parent[currentVertex]]) {
						A.set(parent[currentVertex], true);
					}
	            	}
					// record the non articulation points according to the discovery time
					if(!A.get(currentVertex))
					   nA[disc[currentVertex]-1] = currentVertex ;
	            	
	            	top--;
	            }
	            
	      }

	}

}
