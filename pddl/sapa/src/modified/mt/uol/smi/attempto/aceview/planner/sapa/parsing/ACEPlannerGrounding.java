/**************************************************************************
   Author: Minh B. Do - Arizona State Univ.
***************************************************************************/
package mt.uol.smi.attempto.aceview.planner.sapa.parsing;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLDataFactory;

import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import mt.uol.smi.attempto.aceview.planner.sapa.complex_ds.ACEPlannerStateManager;

/**
 * Grounding: Class to help to ground Predicate, Function, Actions, StateManager
 * etc. according to the templates in the domain file and the object instances
 * in the problem file.
 */
public class ACEPlannerGrounding {

	ACEPlannerProblem aProb;
	ACEPlannerDomain aDomain;

	public ACEPlannerGrounding() {
	}

	/**
	 * Get the StateManager object according to domain/problem specification.
	 * Specifically, ground all predicates, actions, read in the initial/goal state.
	 */
	public ACEPlannerStateManager getStateManager(ACEPlannerDomain aD, ACEPlannerProblem aP,
			OWLModelManager modelManager, OWLDataFactory dataFactory, TokenMapper aceLexicon) {
		aDomain = aD;
		aProb = aP;

		ACEPlannerStateManager pm = new ACEPlannerStateManager(modelManager, dataFactory,aceLexicon);

		pm.setInitPredDB(aP.getInitState());

		pm.setGoalPredDB(aP.getGoalState());

		return pm;
	}
}
