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
import java.util.Stack;
import java.util.StringTokenizer;
/**
 * @author: Shanshan Wang, Chenglong Xiao
 * @description: The algorithm works in a top-down way. This version is implemented mainly using HashSet.
 */
public class TopDownEnumerationSizeKUsingHashSet extends SubgraphEnumerationAlgorithmUsingHashSet {

	private final int NIL = -1;
	private int time = 0; // the counter to record the discovery time of each vertex

	public TopDownEnumerationSizeKUsingHashSet(HashSet[] g, ArrayList<HashSet<Integer>> c, int cardinality, int s, long maximalTime, String path) {
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
			HashSet<Integer> component = (HashSet) i.next();
			HashSet<Integer> mustInculdedVertices = new HashSet();
			HashSet<Integer>[] subgraphWithEdges = new HashSet[size];
			// initialize the adjacent relationship for each vertex of the component
			for (int k : component)
				subgraphWithEdges[k] = HashSetOpt.intersect(graph[k], component);
			topDownEnumeration(subgraphWithEdges, component, mustInculdedVertices);
		}
		if (pw != null)
			pw.close();
	}

	public void topDownEnumeration(HashSet<Integer>[] subgraphWithEdges, HashSet<Integer> subgraph, HashSet<Integer> mustInculdedVertices) {
		// print the subgraph of cardinality k
		if (subgraph.size() == k) {
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
		
		HashSet<Integer> articulationPoints = new HashSet();
		// find all the articulation points in X
		findArticulationPoints(subgraphWithEdges, subgraph, articulationPoints);
        // get the candidates that to be deleted
		HashSet<Integer> candidates = HashSetOpt.difference(subgraph, HashSetOpt.union(mustInculdedVertices, articulationPoints));
		HashSet<Integer> newMustInculdedVertices = (HashSet<Integer>) mustInculdedVertices.clone();
		for (int i : candidates) {
			// update the subgraph
			subgraph.remove(i);
			// update the adjacent list representation of the subgraph
			for (int j : subgraphWithEdges[i]) {
				subgraphWithEdges[j].remove(i);
			}
			topDownEnumeration(subgraphWithEdges, subgraph, newMustInculdedVertices);
			// restore the subgraph
			subgraph.add(i);
			// restore the adjacent list representation of the subgraph
			for (int j : subgraphWithEdges[i]) {
				subgraphWithEdges[j].add(i);
			}
			newMustInculdedVertices.add(i);
			// pruning the search space by looking at the number of must included vertices (look-ahead rule)
			if (newMustInculdedVertices.size() == k) {
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
	public boolean isConnectedSubgraph(HashSet<Integer> subgraph) {
		HashSet<Integer> visited = new HashSet<Integer>();
		int i = subgraph.iterator().next();
		traverse(visited, subgraph, i);
		if (visited.size() == subgraph.size())
			return true;
		else
			return false;
	}
    // Depth-first Search of subgraph
	public void traverse(HashSet<Integer> visited, HashSet<Integer> subgraph, int index) {
		visited.add(index);
		HashSet<Integer> adjacentVertices = HashSetOpt.intersect(graph[index], subgraph);
		for (int i : adjacentVertices) {
			if (!visited.contains(i)) {
				traverse(visited, subgraph, i);
			}
		}
	}

	public void findArticulationPoints(HashSet<Integer>[] adj, HashSet<Integer> subgraph, HashSet<Integer> articulationPoints) {

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
		int i = subgraph.iterator().next();
		APUtil(i, visited, disc, low, parent, adj, time, articulationPoints);
	}

	private void APUtil(int u, boolean visited[], int disc[], int low[], int parent[], HashSet<Integer>[] adj, int time,
			HashSet<Integer> articulationPoints) {

		// Count of children in DFS Tree
		int children = 0;

		// Mark the current node as visited
		visited[u] = true;

		// Initialize discovery time and low value
		disc[u] = low[u] = ++time;

		// Go through all vertices adjacent to this
		for (int v : adj[u]) {
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
					articulationPoints.add(u);
				}

				// (2) If u is not root and low value of one of its child
				// is more than discovery value of u.
				if (parent[u] != NIL && low[v] >= disc[u]) {
					articulationPoints.add(u);
				}
			}
			// Update low value of u for parent function calls.
			else if (v != parent[u])
				low[u] = Math.min(low[u], disc[v]);
		}
	}

}
