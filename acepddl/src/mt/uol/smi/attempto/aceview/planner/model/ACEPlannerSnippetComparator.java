package mt.uol.smi.attempto.aceview.planner.model;

import java.util.Comparator;

public class ACEPlannerSnippetComparator implements Comparator<ACEPlannerSnippet> {
	@Override
	public int compare(ACEPlannerSnippet o1, ACEPlannerSnippet o2) {
		if(o1 == null || o2 == null
				|| o1.getAceSnippet() == null || o2.getAceSnippet() == null
				|| o1.getAceSnippet().toString() == null || o2.getAceSnippet().toString() == null) {
			return 0;
		}
		return o1.getAceSnippet().toString().compareTo(o2.getAceSnippet().toString());
	} 
}
