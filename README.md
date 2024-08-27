# algorithms
1. The source code includes the algorithms implemented using ArrayList, BitSet and HashSet. The implementation with ArrayList is the fastest one.
2. To run the algorithm, the format of input parameters: input file name, the size k, the running time threshold, algorithm name . An example: inf-power.mtx 4939 600 TopDown
3. The algorithms were implemented recursively. To avoid stackoverflow issue, we may set VM arguments: -Xss10M. 
4. The input files are in the folder ''graphs'', and the output files are in folder ''outputs''.
5. SimpleForwardSizeKUsingArrayList,SimpleSizeKUsingArrayList,VariantSimpleSizeKUsingArrayList and TopDownEnumerationSizeKUsingArrayList correspond to the SimpleForward,Simple,VSimple and TopnDown algorithms in the paper titled Novel Algorithms for Efficient Mining of Connected Induced Subgraphs of a Given Cardinality.
6. KDeltaDelayEnumeration.java is the algorithm with a delay of $O(k\Delta)$, the algorithm is proposed in the paper titled An algorithm with a delay of $\mathcal{O}(k\Delta)$ for enumerating connected induced subgraphs of size $k$.
7. The main class is SubgraphEnumerationMainUsingArrayList.
8. All the algorithms were tested on Eclipse Version: 2020-09 (4.17.0), JavaSE-10(jdk-15.0.1).
9. Steps to run the algorithms: right click on SubgraphEnumerationMainUsingArrayList->run as->run configurations->arguments->program arguments: ca-sandi_auths.mtx 3 600 Simple-> click run button.
