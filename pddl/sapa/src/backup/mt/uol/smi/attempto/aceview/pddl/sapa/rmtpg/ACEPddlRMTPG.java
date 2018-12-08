/************************************************************
   Author: Minh B. Do -- Arizona State University
*************************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.rmtpg;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import mt.uol.smi.attempto.aceview.pddl.model.ACEPlannerActionListModel;
import mt.uol.smi.attempto.aceview.pddl.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.pddl.model.ACEPlannerConditionModel;
import mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds.Event;
import mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds.GAction;
import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.ACEPlannerPredDB;
import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.ACEPlannerStateInfo;
import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.GPredDB;
import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.StateInfo;

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
	/* Constants */
	int RPMAXSIZE = 500;

	public final static boolean statistics = false;
	public static ArrayList recordationOfEventSize;

	/* List of FactLink & ActLink to make up a bi-level graph */
	ACEPlannerFactLink[] factLevel;
	ACEPlannerActLink[] actLevel;

	int numAction;
	int numFact;

	EventQueue eventQueue = new EventQueue(); // List of *CostEvent* sorted by time

	ACEPlannerPredDB tempGoals; // To hold the set of goals in process. Sorted in the increasing
	// order of time to achieve. Used when propagate the cost in RMTPG.

	float[] actionZeros; // for initializing int arrays with native arraycopy
	int[] initialNumPredNotSat;
	int[] numPredNotSat; // For efficient growing the graph
	// this is the number of precondition still not satisfied for
	// each action, graduatlly reduced to 0.
	float[] maxActionCost, sumActionCost; // Costs of actions according to the set
	// of its preconditions that are satisfied until now. Will
	// be updated according to "max", "sum" or "combo" rule.

	ACEPlannerPredDB goals; // Set of goals in the initial problem specification
	ACEPlannerPredDB remainGoals; // Used when extracting the direct heuristics. Also used to build sortedGoals.
	ACEPlannerPredDB sortedGoals = new ACEPlannerPredDB(); // Goals sorted in decreasing cost order
	Hashtable goalTime;
	float latestGoalDeadline = 0;

	ArrayList spedFact = new ArrayList();
	Hashtable spedFactTime = new Hashtable(); // time that this fact becomes true?
	int initStateSize = 0;

	// Datastructures for the relaxed plan
	int[] relaxedPlan = new int[RPMAXSIZE];
	int rpIndex = 0;
	int[] haPEffects = new int[RPMAXSIZE / 5];
	int hapeIndex = 0;

	ArrayList<ACEPlannerActionListModel> readyActions = new ArrayList<ACEPlannerActionListModel>(); // List of action ready to be
	// "put" into the RMTPG.

	// Options to run the propagation
	boolean deadlineFlag = false;
	int costPropOption = 1;
	boolean relaxedPlanOption = true;
	int goalCostOption = 1;
	boolean haFlag = false;
	boolean haneFlag = false;
	int lookaheadOption = 1;

	// float currentTime;

	// BM: July 23, 2003. Storing actions that have no instant condition (only "at
	// end" condition)
	int[] noCondActs = new int[100];
	int numNCA = 0;

	// For dynamic goals (pending preconditions)
	ACEPlannerPredDB dGoal = new ACEPlannerPredDB();
	ArrayList dGoalTime = new ArrayList();

	public ACEPlannerRMTPG() {
		// if we are collecting statistics, initialize statistic-recording variables.
		if (statistics) {
			recordationOfEventSize = new ArrayList(100);
		}
	}

	/**
	 * Function to set up the running options
	 */
	public void optionSetting(boolean dlf, int cpo, boolean rpo, int gco, boolean haf, boolean hanef, int lao) {
		deadlineFlag = dlf;
		costPropOption = cpo;
		relaxedPlanOption = rpo;
		goalCostOption = gco;
		haFlag = haf;
		haneFlag = hanef;
		lookaheadOption = lao;
	}

	public void resetHAOption(boolean haf, boolean hanef) {
		haFlag = haf;
		haneFlag = hanef;
	}

	public void resetLAOption(int lao) {
		lookaheadOption = lao;
	}

	/**
	 * Function to initialize the data structures related to goals. will be called
	 * from *Utility* and *goals* is SORTED according to the INCREASING order of
	 * DEADLINE values.
	 */
	public void initGoals(ACEPlannerPredDB g) {
		goals = new ACEPlannerPredDB(g);
	}

	public float getLatestGoalTime() {
		return latestGoalDeadline;
	}

	public List<ACEPlannerActionModel> applicableActions(ACEPlannerStateInfo state, ArrayList<ACEPlannerActionModel> gActions) {
		ArrayList<ACEPlannerActionModel> actionList = new ArrayList<ACEPlannerActionModel>();

		for (ACEPlannerActionModel possibleAction: gActions) {
			if (state.applicable(possibleAction)) {
				actionList.add(possibleAction);
			}
		}
		return actionList;
	}
}