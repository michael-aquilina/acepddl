/*************************************************************
   Author: Minh B. Do - Arizona State University
**************************************************************/
package mt.uol.smi.attempto.aceview.planner.sapa.complex_ds;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACETextImpl;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSnippet;
import mt.uol.smi.attempto.aceview.planner.model.AbstractACEPlannerSnippetListModel;

/**
 * GPredDB: Class to manage the set of predicates that are true at a given time
 * point.
 */

public class ACEPlannerPredDB extends AbstractACEPlannerSnippetListModel{
	public ACEPlannerPredDB() {
		super();
	}
	
	public ACEPlannerPredDB(ACEPlannerPredDB  predDB) {
		super(predDB);					
	}		
}
