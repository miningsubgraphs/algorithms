package bitset.sizek.algorithm;

import java.util.BitSet;

/**
 * BitSet operation class: union, minus, intersection.
 *
 */
public class BitSetOpt {
  
  public static BitSet union(BitSet a, BitSet b){
	  BitSet result = (BitSet) a.clone();
	  result.or(b);
	  return result;
  }
  public static void unionLight(BitSet a, BitSet b){
	  a.or(b);
  }
  public static BitSet difference(BitSet a, BitSet b){
	  BitSet result = (BitSet) a.clone();
	  result.andNot(b);
	  return result;
  }
  
  public static void differenceLight(BitSet a, BitSet b){
	  a.andNot(b);
  }
  
  public static BitSet intersect(BitSet a, BitSet b){
	  BitSet result = (BitSet) a.clone();
	  result.and(b);
	  return result;
  }
  
  public static void intersectLight(BitSet a, BitSet b){
	  a.and(b);
  }
  
  
  public static boolean intersects(BitSet a, BitSet b){
	  return a.intersects(b);
  }
  
  public static BitSet Xor(BitSet a, BitSet b){
	  BitSet result = (BitSet) a.clone();
	  result.xor(b);
	  return result;
  }
}
