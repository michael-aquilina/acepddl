/*************************************************************
   Author: Minh B. Do - Arizona State University
**************************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds;

import java.util.List;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACETextImpl;
import mt.uol.smi.attempto.aceview.pddl.model.ACEPlannerSnippet;

/**
 * GPredDB: Class to manage the set of predicates that are true at a given time
 * point.
 */

public class ACEPlannerPredDB extends ACETextImpl {
	
	public ACEPlannerPredDB() {
		super();
	}
	public ACEPlannerPredDB(ACEPlannerPredDB  predDB) {
		super();
		for(ACESnippet snippet: predDB.getSnippets()) {
			this.add(snippet);
		}			
	}	
	public ACEPlannerPredDB(List<ACEPlannerSnippet>  snippets) {
		super();
		for(ACEPlannerSnippet snippet: snippets) {
			this.add(snippet.getAceSnippet());
		}			
	}
}
