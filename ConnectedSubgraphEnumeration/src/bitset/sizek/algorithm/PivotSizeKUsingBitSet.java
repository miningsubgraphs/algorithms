package bitset.sizek.algorithm;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
/**
 * @author: Shanshan Wang, Chenglong Xiao
 * @description: The algorithm works in a bottom-up way. This version is implemented mainly using BitSet.
 *               Compared to HashSet and ArrayList£¬ BitSet is more efficient when performing set operations£¨union,intersect,etc.£©. 
 *               The algorithm is a variant of previous algorithm presented in : Finding dense subgraphs of sparse graphs. 
 */
public class PivotSizeKUsingBitSet extends SubgraphEnumerationAlgorithmUsingBitSet{

	 public PivotSizeKUsingBitSet(BitSet[] g, ArrayList<BitSet> c, int cardinality, int s, long maximalTime,
				String path) {
			graph = g;
			components = c;
			k = cardinality;
			size = s-1;
			maxTime = maximalTime;
			outputFilePath = path;
		}
	
	@Override
	public void enumerate() {
		// TODO Auto-generated method stub
		 // get the start time of the enumeration
		 startTime = System.currentTimeMillis();
		 // initialize the printer
		 if(outputFilePath!=null)
		 outputGraphToFile();
		 // enumerate connected subgraphs of order k from each connected components
		 Iterator i = components.iterator();
		 while(i.hasNext()) {
			   BitSet c = (BitSet)i.next();
			   int forbiddenVerticesNumber = 0;
			   for(int j = c.nextSetBit(0);j>=0;j=c.nextSetBit(j+1)) {
				   BitSet P = new BitSet();
					P.set(j, true);
					BitSet S = new BitSet();
					BitSet F = new BitSet();
					pivot(P, S, F, j);
			   }
		 } 
		 if(pw!=null)
		       pw.close();
	}
	
	public  void pivot(BitSet P, BitSet S, BitSet F, int max_index) {
		if(P.cardinality()+S.cardinality()==k) {
			count ++;
			// output the subgraph to the hardware disk
			if(pw!=null)
			    pw.println(BitSetOpt.union(P, S));
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
		
		BitSet new_p = null;
		BitSet new_s = null;
		BitSet new_f = null;
		new_s =(BitSet) S.clone();
		new_p = (BitSet)P.clone();
		new_f = (BitSet)F.clone();
		for (int i = P.nextSetBit(0); i >= 0; i = P.nextSetBit(i + 1)) {
			for(int j = graph[i].nextSetBit(0);j>=0;j=graph[i].nextSetBit(j+1)) {
				if(j<=max_index)
					continue;
				if(new_f.get(j)||new_p.get(j)||new_s.get(j))
					continue;
				BitSet temp =(BitSet) new_p.clone();
				temp.set(j, true);
				pivot(temp,new_s,new_f,max_index);
				new_f.set(j,true);
				// the k-component rule
				if((size-new_f.size())<k)
					break;
			}
			new_p.set(i, false);
			new_s.set(i, true);
         
		}
	}

}
