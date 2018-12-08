/**************************************************************************
   Author: Minh B. Do - Arizona State Univ.
***************************************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.parsing;

import mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds.*;
import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.*;

import java.util.*;

import ch.uzh.ifi.attempto.aceview.ACEText;

/**
 * Grounding: Class to help to ground Predicate, Function, Actions, StateManager
 * etc. according to the templates in the domain file and the object instances
 * in the problem file.
 */
public class ACEPlannerGrounding {
	
	boolean dupaFlag = true; // Do we want to check duplicate parameters?
	// (two para map to the same object)

	// GAction instance;

	ACEPlannerProblem aProb;
	ACEPlannerDomain aDomain;

	public ACEPlannerGrounding(boolean dupa) {
		dupaFlag = dupa;
	}

	/** Check if "parent" is a supertype of "child" */
	public boolean checkSuperType(Domain domain, String parent, String child) {
		if (parent.equalsIgnoreCase(child))
			return true;

		String temp = child, ans;
		while ((ans = domain.getParent(temp)) != null) {
			if (parent.equalsIgnoreCase(ans))
				return true;
			temp = ans;
		}

		return false;
	}


	/**
	 * Get the StateManager object according to domain/problem specification.
	 * Specifically, ground all predicates, actions, read in the initial/goal state.
	 */
	public ACEPlannerStateManager getStateManager(ACEPlannerDomain aD, ACEPlannerProblem aP) {
		aDomain = aD;
		aProb = aP;

		ACEPlannerStateManager pm = new ACEPlannerStateManager(0);

		pm.setInitPredDB(aP.getInitState());

		pm.setGoalPredDB(aP.getGoalState());


		return pm;
	}	
}
