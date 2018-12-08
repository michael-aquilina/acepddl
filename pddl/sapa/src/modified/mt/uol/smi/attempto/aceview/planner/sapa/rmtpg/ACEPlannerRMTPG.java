/************************************************************
   Author: Minh B. Do -- Arizona State University
*************************************************************/
package mt.uol.smi.attempto.aceview.planner.sapa.rmtpg;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import mt.uol.smi.attempto.aceview.planner.sapa.ACEPlannerPlanner;
import mt.uol.smi.attempto.aceview.planner.sapa.complex_ds.ACEPlannerPredDB;
import mt.uol.smi.attempto.aceview.planner.sapa.complex_ds.ACEPlannerStateInfo;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerActionListModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSatisfiedActionModel;

/**
 * RMTPG.java: Holding the Relaxed Temporal Planning Graph and the code to
 * propagate the cost information over it.
 */

// NOTE by BM: Be careful that by July 23, 2003. We allow contradicting
// events to co-exist in the event queue. Therefore, in costPropagation()
// function, relaxed achievement of all goals no longer guarantee the
// real achievement of all goals (because there may exist negative events)
// that delete the goals later. Need to look back later if that case occur
// and cause the false termination of the search (by returning heuristic
// value equals to ZERO).

public class ACEPlannerRMTPG {
	private static final Logger logger = Logger.getLogger(ACEPlannerRMTPG.class);

	ACEPlannerPredDB tempGoals; // To hold the set of goals in process. Sorted in the increasing
	// order of time to achieve. Used when propagate the cost in RMTPG.

	// of its preconditions that are satisfied until now. Will
	// be updated according to "max", "sum" or "combo" rule.

	ACEPlannerPredDB goals; // Set of goals in the initial problem specification
	ACEPlannerPredDB remainGoals; // Used when extracting the direct heuristics. Also used to build sortedGoals.
	ACEPlannerPredDB sortedGoals = new ACEPlannerPredDB(); // Goals sorted in decreasing cost order

	// For dynamic goals (pending preconditions)
	ACEPlannerPredDB dGoal = new ACEPlannerPredDB();

	public ACEPlannerRMTPG() {

	}

	/**
	 * Function to initialize the data structures related to goals. will be called
	 * from *Utility* and *goals* is SORTED according to the INCREASING order of
	 * DEADLINE values.
	 */
	public void initGoals(ACEPlannerPredDB g) {
		goals = new ACEPlannerPredDB(g);
	}

	public List<ACEPlannerSatisfiedActionModel> applicableActions(ACEPlannerStateInfo state, List<ACEPlannerActionModel> gActions) {
		ArrayList<ACEPlannerSatisfiedActionModel> actionList = new ArrayList<ACEPlannerSatisfiedActionModel>();
		logger.debug("\nLooking to get applicable actions for state with snippets: "+ state.getCurrentState().getAcetext().getSnippets().size());
		
		for (ACEPlannerActionModel possibleAction: gActions) {
			List<ACEPlannerSatisfiedActionModel> satsified = state.applicable(possibleAction);
			if(satsified != null){
				actionList.addAll(satsified);
			}
		}
		logger.debug("Looked to get applicable actions for state with snippets: "+ state.getCurrentState().getAcetext().getText());
		logger.debug("Actions applicable for this state : ");
		for(ACEPlannerSatisfiedActionModel act: actionList) {
			logger.debug("\n" + act.toString());
		}
		return actionList;
	}
}