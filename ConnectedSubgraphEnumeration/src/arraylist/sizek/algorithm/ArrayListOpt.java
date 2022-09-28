package arraylist.sizek.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * HashSet operation class: union, difference, intersection.
 *
 */
public class ArrayListOpt {
  
	//The operation of Intersect
	public static ArrayList intersect(ArrayList l1,ArrayList l2) {
		ArrayList result =(ArrayList)l1.clone();
		result.retainAll(l2);
		
		return result;
	}
	
	//The operation of Union
	public static ArrayList union(ArrayList l1,ArrayList l2) {
		ArrayList result =(ArrayList)l1.clone();
		result.addAll(l2);
		
		return result;
		
	}
	
	//The operation of Diff
	public static ArrayList diff(ArrayList l1,ArrayList l2) {
		ArrayList result =(ArrayList)l1.clone();
		result.removeAll(l2);
		
		return result;
	}

  
  
}
