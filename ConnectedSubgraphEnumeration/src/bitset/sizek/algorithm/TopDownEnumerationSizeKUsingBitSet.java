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
import java.util.LinkedList;
import java.util.Stack;
import java.util.StringTokenizer;
/**
 * @author: Shanshan Wang, Chenglong Xiao
 * @description: The algorithm works in a top-down way. This version is implemented mainly using BitSet.
 *               Compared to HashSet and ArrayList£¬ BitSet is more efficient when performing set operations£¨union,intersect,etc.£©. 
 */
public class TopDownEnumerationSizeKUsingBitSet extends SubgraphEnumerationAlgorithmUsingBitSet {

	private final int NIL = -1;
	private int time = 0; // the counter to record the discovery time of each vertex

	public TopDownEnumerationSizeKUsingBitSet(BitSet[] g, ArrayList<BitSet> c, int cardinality, int s, long maximalTime, String path) {
		graph = g;
		components = c;
		k = cardinality;
		size = s;
		maxTime = maximalTime;
		outputFilePath = path;
	}

	public void enumerate() {
		// get the start time of the enumeration
		startTime = System.currentTimeMillis();
		// initialize the printer
		if (outputFilePath != null)
			outputGraphToFile();
		// enumerate connected subgraphs from each connected components
		Iterator i = components.iterator();
		while (i.hasNext()) {
			BitSet component = (BitSet) i.next();
			BitSet mustInculdedVertices = new BitSet();
			BitSet[] subgraphWithEdges = new BitSet[size];
			// initialize the adjacent relationship for each vertex of the component
			for (int k = component.nextSetBit(0); k >= 0; k = component.nextSetBit(k + 1))
				subgraphWithEdges[k] = BitSetOpt.intersect(graph[k], component);
			topDownEnumeration(subgraphWithEdges, component, mustInculdedVertices);
		}
		if (pw != null)
			pw.close();
	}

	public void topDownEnumeration(BitSet[] subgraphWithEdges, BitSet subgraph, BitSet mustInculdedVertices) {
		// print the subgraph of cardinality k
		if (subgraph.cardinality() == k) {
			count++;
			if (pw != null)
				pw.println(subgraph);
			return;
		}
		// if the running time exceeds the given maximal time, then exits the algorithm.
		long currentTime = System.currentTimeMillis();
		if((currentTime-startTime)/1000>=maxTime) {
				System.out.println("Running time exceeds the given maximal running time!  (" + maxTime +" seconds)");
				System.out.println("The number of enumerated subgraphs: " + count);
				pw.close();
				System.exit(0);
		}
		
		BitSet articulationPoints = new BitSet();
		// find all the articulation points in X
		findArticulationPoints(subgraphWithEdges, subgraph, articulationPoints);
        // get the candidates that to be deleted
		BitSet candidates = BitSetOpt.difference(subgraph, BitSetOpt.union(mustInculdedVertices, articulationPoints));
		BitSet newMustInculdedVertices = (BitSet) mustInculdedVertices.clone();
		for (int i = candidates.nextSetBit(0); i >= 0; i = candidates.nextSetBit(i + 1)) {
			// update the subgraph
			subgraph.set(i, false);
			// update the matrix representation of the subgraph
			for (int j = subgraphWithEdges[i].nextSetBit(0); j >= 0; j = subgraphWithEdges[i].nextSetBit(j + 1)) {
				subgraphWithEdges[j].set(i, false);
			}
			topDownEnumeration(subgraphWithEdges, subgraph, newMustInculdedVertices);
			// restore the subgraph
			subgraph.set(i, true);
			// restore the matrix representation of the subgraph
			for (int j = subgraphWithEdges[i].nextSetBit(0); j >= 0; j = subgraphWithEdges[i].nextSetBit(j + 1)) {
				subgraphWithEdges[j].set(i, true);
			}
			newMustInculdedVertices.set(i, true);
			// pruning the search space by looking at the number of must included vertices (look-ahead rule)
			if (newMustInculdedVertices.cardinality() == k) {
				if (isConnectedSubgraph(newMustInculdedVertices)) {
					count++;
					if (pw != null)
						pw.println(newMustInculdedVertices);
				}
				break;
			}
		}

	}
    // check the connectivity of subgraph
	public boolean isConnectedSubgraph(BitSet subgraph) {
		BitSet visited = new BitSet();
		int i = subgraph.nextSetBit(0);
		traverse(visited, subgraph, i);
		if (visited.cardinality() == subgraph.cardinality())
			return true;
		else
			return false;
	}
    // Depth-first Search of subgraph
	public void traverse(BitSet visited, BitSet subgraph, int index) {
		visited.set(index, true);
		BitSet adjacentVertices = BitSetOpt.intersect(graph[index], subgraph);
		for (int i = adjacentVertices.nextSetBit(0); i >= 0; i = adjacentVertices.nextSetBit(i + 1)) {
			if (!visited.get(i)) {
				traverse(visited, subgraph, i);
			}
		}
	}

	public void findArticulationPoints(BitSet[] adj, BitSet subgraph, BitSet articulationPoints) {

    	// Mark all the vertices as not visited
		boolean visited[] = new boolean[size];
		int disc[] = new int[size];
		int low[] = new int[size];
		int parent[] = new int[size];
		time = 0;

		// Initialize parent and visited, and ap(articulation point) arrays
		for (int i = 0; i < size; i++) {
			parent[i] = NIL;
			visited[i] = false;
		}

		// Call the recursive helper function to find articulation points in DFS tree rooted with vertex 'i'
		int i = subgraph.nextSetBit(0);
		APUtil(i, visited, disc, low, parent, adj, time, articulationPoints);
	}

	private void APUtil(int u, boolean visited[], int disc[], int low[], int parent[], BitSet[] adj, int time,
			BitSet articulationPoints) {

		// Count of children in DFS Tree
		int children = 0;

		// Mark the current node as visited
		visited[u] = true;

		// Initialize discovery time and low value
		disc[u] = low[u] = ++time;

		// Go through all vertices adjacent to this
		for (int v = adj[u].nextSetBit(0); v >= 0; v = adj[u].nextSetBit(v + 1)) {
			// v is current adjacent of u
			// If v is not visited yet, then make it a child of u
			// in DFS tree and recur for it
			if (!visited[v]) {
				children++;
				parent[v] = u;
				APUtil(v, visited, disc, low, parent, adj, time, articulationPoints);

				// Check if the subtree rooted with v has a connection to
				// one of the ancestors of u
				low[u] = Math.min(low[u], low[v]);

				// u is an articulation point in following cases
				// (1) u is root of DFS tree and has two or more chilren.
				if (parent[u] == NIL && children > 1) {
					articulationPoints.set(u, true);
				}

				// (2) If u is not root and low value of one of its child
				// is more than discovery value of u.
				if (parent[u] != NIL && low[v] >= disc[u]) {
					articulationPoints.set(u, true);
				}
			}
			// Update low value of u for parent function calls.
			else if (v != parent[u])
				low[u] = Math.min(low[u], disc[v]);
		}
	}

}
