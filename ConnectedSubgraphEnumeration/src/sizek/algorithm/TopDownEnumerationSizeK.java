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
import java.util.Stack;
import java.util.StringTokenizer;
/**
 * @author: Shanshan Wang, Chenglong Xiao
 * @description: The algorithm works in a top-down way. This version is implemented mainly using ArrayList, and BitSet (or boolean array) is used to ensure that the check whether a vertex is in the set in O(1) time.
 *               The graph is stored in ArrayList.
 *               Note that, only in this version, O(n+m) space is fully guaranteed.
 */
public class TopDownEnumerationSizeK extends SubgraphEnumerationAlgorithm {

	private final int NIL = -1;
	private int time = 0; // the counter to record the discovery time of each vertex, it is required for
							// finding articulation points

	public TopDownEnumerationSizeK(ArrayList<Integer>[] g, ArrayList<ArrayList<Integer>> c,
			int cardinality, int s, long maximalTime, String path) {
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

			// the current component
			ArrayList<Integer> component = (ArrayList) i.next();
			// if k equals to the size of the graph (component), print it
			if (k == size - 1) {
				count++;
				pw.println(component);
				break;
			}
			// the set of must included vertices
			ArrayList<Integer> mustInculdedVertices = new ArrayList();
			// the adjacent list of the current subgraph
			ArrayList<Integer>[] subgraphWithEdges = new ArrayList[size];
			// a set to store the vertices removed from the deletableVertices, this set is
			// required when restore the deletableVertices set
			ArrayList<Integer> A = new ArrayList();
			// a set to store the vertices added to the deletableVertices, this set is
			// required when restore the deletableVertices set
			ArrayList<Integer> B = new ArrayList();
			// the set of vertices that can be deleted (not articulation point)
			BitSet deletableVertices = new BitSet();
			BitSet mustIncludedVerticesBitSet = new BitSet();
			// the subgraph
			BitSet subgraph = new BitSet();
			// initialize the adjacent relationship for each vertex of the component
			for (Integer j : component) {
				subgraphWithEdges[j] = ArrayListOperation.intersect(graph[j], component);
				deletableVertices.set(j, true);
				subgraph.set(j, true);
			}
			findNonArticulationPoints(subgraphWithEdges, subgraph, deletableVertices, A, B);
			topDownEnumeration(subgraphWithEdges, subgraph, deletableVertices, A, B, mustInculdedVertices,mustIncludedVerticesBitSet);
		}
		if (pw != null)
			pw.close();
	}

	public void topDownEnumeration(ArrayList<Integer>[] subgraphWithEdges, BitSet subgraph, BitSet deletableVertices,
			ArrayList<Integer> A, ArrayList<Integer> B, ArrayList<Integer> mustInculdedVertices, BitSet mustIncludedVerticesBitSet) {
		
		// print the subgraph of cardinality k
		if (subgraph.cardinality() == k) {
			count++;
			if (pw != null)
				pw.println(subgraph);
			return;
		}
		// if the running time exceeds the given maximal time, then exits the algorithm.
		long currentTime = System.currentTimeMillis();
		if ((currentTime - startTime) / 1000 >= maxTime) {
			System.out.println("Running time exceeds the given maximal running time!  (" + maxTime + " seconds)");
			System.out.println("The number of enumerated subgraphs: " + count);
			pw.close();
			System.exit(0);
		}

		// a pointer to indicate the current end of mustIncludedVertices
		int pm = mustInculdedVertices.size() - 1;
		for (Integer i = deletableVertices.nextSetBit(0); i >= 0; i = deletableVertices.nextSetBit(i + 1)) {
			if (!mustIncludedVerticesBitSet.get(i)) {
				// update the subgraph
				subgraph.set(i, false);
				
				// update the adjacent list representation of the subgraph
				if(subgraph.cardinality()>k)
				  for (Integer j : subgraphWithEdges[i]) {
					subgraphWithEdges[j].remove(i);
				  }
				// pa is a pointer to indicate the current end of A
				int pa = A.size() - 1;
				// pb is a pointer to indicate the current end of B
				int pb = B.size() - 1;
				// delete i from deletableVertices
				deletableVertices.set(i, false);
				if(subgraph.cardinality()>k)
				        findNonArticulationPoints(subgraphWithEdges, subgraph, deletableVertices, A, B);
				topDownEnumeration(subgraphWithEdges, subgraph, deletableVertices, A, B, mustInculdedVertices,mustIncludedVerticesBitSet);
				// restore the subgraph
				subgraph.set(i, true);
				// restore the adjacent list representation of the subgraph
				if(subgraph.cardinality()>k+1)
				  for (Integer j : subgraphWithEdges[i]) {
					subgraphWithEdges[j].add(i);
				  }
				// restore the deletableVertices, A and B
				deletableVertices.set(i, true);
				int j = A.size() - 1;
				while (j > pa) {
					deletableVertices.set(A.get(j), true);
					A.remove(j);
					j--;
				}
				int l = B.size() - 1;
				while (l > pb) {
					deletableVertices.set(B.get(l), false);
					B.remove(l);
					l--;
				}
				// add vertex i to mustIncludedVertices
				mustInculdedVertices.add(i);
				mustIncludedVerticesBitSet.set(i);
				// pruning the search space by looking at the number of must included vertices (look-ahead rule)
				if (mustInculdedVertices.size() > k) {
					if (isConnectedSubgraph(mustInculdedVertices)) {
						count++;
						if (pw != null)
							pw.println(mustInculdedVertices);
					}
					break;
				}
			}
		}
		// restore the mustIncludedVertices set when returning the parent search node
		for (int i = mustInculdedVertices.size() - 1; i > pm; i--) {
			mustIncludedVerticesBitSet.set(mustInculdedVertices.get(i), false);
			mustInculdedVertices.remove(i);
		}

	}

	// check the connectivity of subgraph
	public boolean isConnectedSubgraph(ArrayList<Integer> subgraph) {
		ArrayList<Integer> visited = new ArrayList();
		Integer i = subgraph.get(0);
		traverse(visited, subgraph, i);
		if (visited.size() == subgraph.size())
			return true;
		else
			return false;
	}

	// Depth-first Search of subgraph
	public void traverse(ArrayList<Integer> visited, ArrayList<Integer> subgraph, Integer index) {
		visited.add(index);
		ArrayList<Integer> adjacentVertices = ArrayListOperation.intersect(graph[index], subgraph);
		for (Integer i : adjacentVertices) {
			if (!visited.contains(i)) {
				traverse(visited, subgraph, i);
			}
		}
	}

	public void findNonArticulationPoints(ArrayList<Integer>[] adj, BitSet subgraph, BitSet deletableVertices,
			ArrayList<Integer> A, ArrayList<Integer> B) {

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

		// Call the recursive helper function to find articulation points in DFS tree
		// rooted with vertex 'i'
		int i = subgraph.nextSetBit(0);
		APUtil(i, visited, disc, low, parent, adj, time, deletableVertices, A, B);
	}

	private void APUtil(int u, boolean visited[], int disc[], int low[], int parent[], ArrayList<Integer>[] adj,
			int time, BitSet deletableVertices, ArrayList<Integer> A, ArrayList<Integer> B) {

		// Count of children in DFS Tree
		int children = 0;

		// Mark the current node as visited
		visited[u] = true;

		// Initialize discovery time and low value
		disc[u] = low[u] = ++time;

		// a tag to indicate whether the vertex is changed from an articulation point to
		// a non-articulation point
		boolean isAP = false;

		// Go through all vertices adjacent to this
		for (int v : adj[u]) {
			// v is current adjacent of u
			// If v is not visited yet, then make it a child of u
			// in DFS tree and recur for it
			if (!visited[v]) {
				children++;
				parent[v] = u;
				APUtil(v, visited, disc, low, parent, adj, time, deletableVertices, A, B);

				// Check if the subtree rooted with v has a connection to
				// one of the ancestors of u
				low[u] = Math.min(low[u], low[v]);

				// u is an articulation point in following cases
				// (1) u is root of DFS tree and has two or more chilren.
				if (parent[u] == NIL && children > 1) {
					if (deletableVertices.get(u)) {
						A.add(u);
						deletableVertices.set(u, false);
					}
					isAP = true;
				}

				// (2) If u is not root and low value of one of its child
				// is more than discovery value of u.
				if (parent[u] != NIL && low[v] >= disc[u]) {
					if (deletableVertices.get(u)) {
						A.add(u);
						deletableVertices.set(u, false);
					}
					isAP = true;
				}
			}
			// Update low value of u for parent function calls.
			else if (v != parent[u])
				low[u] = Math.min(low[u], disc[v]);
		}
		// check if u was an articulation point but now is a non-articulation point
		if (isAP == false && !deletableVertices.get(u)) {
			deletableVertices.set(u, true);
			B.add(u);
		}

	}

}
