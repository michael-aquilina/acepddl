/***********************************************************
   Author: Minh B. Do - Arizona State University
***********************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.parsing;

import java.util.*;

import mt.uol.smi.attempto.aceview.pddl.model.ACEPlannerActionListModel;
import mt.uol.smi.attempto.aceview.pddl.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds.*;

/**
 * Domain: Storing Domain structure returned by parser from reading the domain
 * file in PDDL2.1.
 */
public class ACEPlannerDomain {
	String name;
	
	ACEPlannerActionListModel actions = new ACEPlannerActionListModel(); // List of all action

	/** For name of the domain */
	public void setName(String n) {
		name = n;
	}

	public String getName() {
		return name;
	}

	public ACEPlannerActionListModel getActions() {
		return actions;
	}

	public void setActions(ACEPlannerActionListModel actions) {
		this.actions = actions;
	}

}
