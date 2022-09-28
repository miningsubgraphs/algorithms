package hashset.sizek.algorithm;

import java.util.HashSet;

/**
 * HashSet operation class: union, difference, intersection.
 *
 */
public class HashSetOpt {
  
  public static HashSet<Integer>  union(HashSet<Integer>  a, HashSet<Integer>  b){
	  HashSet<Integer>  result = (HashSet<Integer> ) a.clone();
	  result.addAll(b);
	  return result;
  }
  public static void unionLight(HashSet<Integer>  a, HashSet<Integer>  b){
	  a.addAll(b);
  }
  public static HashSet<Integer>  difference(HashSet<Integer>  a, HashSet<Integer>  b){
	  HashSet<Integer>  result = (HashSet<Integer> ) a.clone();
	  result.removeAll(b);
	  return result;
  }
  
  public static void differenceLight(HashSet<Integer> a, HashSet<Integer>  b){
	  a.removeAll(b);
  }
  
  public static HashSet<Integer>  intersect(HashSet<Integer>  a, HashSet<Integer>  b){
	  HashSet<Integer>  result = (HashSet<Integer> ) a.clone();
	  result.retainAll(b);
	  return result;
  }
  
  public static void intersectLight(HashSet<Integer>  a, HashSet<Integer>  b){
	  a.retainAll(b);
  }
  
  
}
