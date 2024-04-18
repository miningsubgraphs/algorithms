package arraylist.sizek.algorithm;


import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author: Chenglong Xiao, Chengyong Mao, Shanshan Wang
 * @description: The algorithm works in a bottom-up way. This version is implemented mainly using List, and boolean arrays are used to ensure that the check whether a vertex is in the set in O(1) time.
 *               Note that, in this version, O(n+m) space is fully guaranteed.
 *               The delay of the algorithm is O(k\Delta).
 */
public class KDeltaDelayEnumeration extends SubgraphEnumerationAlgorithmUsingArrayList {

    private ArrayList<Integer> T;
    boolean[] TB;

    public KDeltaDelayEnumeration(ArrayList<Integer>[] g, ArrayList<ArrayList<Integer>> cs, int cardinality, int s, long maximalTime,
                                  String path) {
        graph = g;
        components = cs;
        k = cardinality;
        size = s - 1;
        maxTime = maximalTime;
        outputFilePath = path;
    }

    public void enumerate() {
        // get the start time of the enumeration
        startTime = System.currentTimeMillis();
        // initialize the printer
        if (outputFilePath != null)
            outputGraphToFile();
        // enumerate connected subgraph of order k from each connected components
        for (ArrayList<Integer> component : components) {
            // store the subgraph using List
            ArrayList<Integer> S = new ArrayList<>();
            // record the existence of the vertices in S+N+F
            boolean[] B = new boolean[size + 1];
            // store the traversed vertices before solution is found
            T = new ArrayList<>();
            TB = new boolean[size + 1];
            // store the all forbidden vertices including two parts: 
            //[0, F.size£¨£©-fNumber) are the neighbor vertices that can not be included.
            //[F.size£¨£©-fNumber, F¡£size()-1] are the neighbor vertices that can be opened to visit one by one. 
            ArrayList<Integer> F = new ArrayList<>();
            // store the all neighbor vertices to be opened when a k-subgraph is found
            ArrayList<Integer> N = new ArrayList<>();
            // component records the vertices in terms of Depth-First Search, 
            // we visit the vertex in component in the reverse order such that the remain subgraphs that excluding the visited vertices before v  are connected and the sizes are >= k. 
            for (int i = component.size()-1; i>=0; i--) {
            	Integer v = component.get(i);
                S.add(v);
                B[v] = true;
                int fNumber = 0;
                // add the vertices to forbiddenVertices as the set of vertices to be opened one by one
                for (Integer u : graph[v]) {
                    if (!B[u]) {
                        F.add(u);
                        B[u] = true;
                        fNumber++;
                    }
                }
                // expand the subgraph by adding neighbor vertices
                extendSubgraph(S,  F,N, B, fNumber);
                // restore the neighbors as the child recursion moved all the F[N.size()-fIndex,N.size()-1] to N
                while (fNumber>0) {
                	B[N.remove(N.size()-1)] = false;
                    fNumber--;
                }
                // mark vertex v as a closed vertex as all the subgraphs containing v are already enumerated
                F.add(v);
                // restore the subgraph
                S.remove(S.size() - 1);
                // if the number of remain vertices <k, no need to enumerate
                if(size-F.size()<k) {
					break;
				}
            }
        }
        if (pw != null)
            pw.close();
    }

    private boolean extendSubgraph(ArrayList<Integer> S, ArrayList<Integer> F,ArrayList<Integer> N, boolean[] B,int fNumber) {
        // a tag indicating whether an interesting leaf exists
        boolean hasInLeaf = false;
        // a subgraph of size is found
        if (S.size() == k) {
            ++count;
            if (pw != null) {
                pw.println(S);
            }
            return true;
        } else if (S.size() + T.size() == k) {
            ++count;
            if (pw != null) {
                pw.print(S);
                pw.println(T);
            }
            // clear the set traversedVertices
            for (int i = T.size() - 1; i >= 0; i--) {
                TB[T.remove(i)] = false;
            }
            hasInLeaf = true;
        }
        // if the running time exceeds the given maximal time, then exits the algorithm.
        long currentTime = System.currentTimeMillis();
        if ((currentTime - startTime) / 1000 >= maxTime) {
            System.out.println("Running time exceeds the given maximal running time!  (" + maxTime + " seconds)");
            System.out.println("The number of enumerated subgraphs: " + count);
            pw.close();
            System.exit(0);
        }
        // F(F.size()-fNumber,F.size()-1] are the vertices can be opened one by one
        for (int i = 0; i < fNumber; i++) {
            int u = F.remove(F.size()-1);
            S.add(u);
            // the number of vertices added to F
            int fCount = 0;
            // the number of vertices added to N
            int nCount = 0;
            // when the size of subgraph is smaller than k, we add its neighbor to F or N
            if(S.size()<k) {
              for (int v : graph[u]) {
                if (!B[v]) {
                    if (!TB[v]) {
                        F.add(v);
                        B[v] = true;
                        fCount++;
                    } else {
                        N.add(v);
                        B[v] = true;
                        nCount++;
                    }
                }
              }
            }
            // moveCount records the number of vertices moved from N to F
            int moveCount = 0;
            if (hasInLeaf&&S.size()<k) {
                // move all the vertices from N to F
                while(!N.isEmpty()) {
                	Integer v = N.remove(N.size()-1);
                	F.add(v);
                	moveCount++;
                }
            }
            // newFIndex: mark the new FIndex
            int newFNumber = fCount+moveCount;
            if (extendSubgraph(S, F, N, B, newFNumber)) {
                hasInLeaf = true;
            } else {
                T.add(u);
                TB[u] = true;
            }
            int numberOfNeighbors = fCount+nCount;
            // remove the added neighbor vertices of u
            while (numberOfNeighbors > 0) {
            	B[N.remove(N.size()-1)] = false;
                numberOfNeighbors--;
            }
            // add u to N
            N.add(u);
            // restore S
            S.remove(S.size() - 1);
        }
        return hasInLeaf;
    }
}
