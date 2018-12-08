package mt.uol.smi.attempto.aceview.planner.model;

import java.util.Comparator;

import ch.uzh.ifi.attempto.aceview.ACESnippet;

public class ACESnippetComparator implements Comparator<ACESnippet> {
	@Override
	public int compare(ACESnippet o1, ACESnippet o2) {
		if(o1 == null || o2 == null
				|| o1.toString() == null || o2.toString() == null) {
			return 0;
		}
		return o1.toString().compareTo(o2.toString());
	} 
}
