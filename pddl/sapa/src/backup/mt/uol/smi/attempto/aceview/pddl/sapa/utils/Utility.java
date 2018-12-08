/****************************************************
    Utility.java: Contains supporting functions to
    help doing dirty jobs for other classes

    Author: Minh B. Do - Arizona State University
****************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.utils;

import mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds.*;
import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.*;
import mt.uol.smi.attempto.aceview.pddl.sapa.parsing.*;

import java.util.*;

public class Utility {
	ArrayList aArrayList = new ArrayList();
	int index;
	Predicate gPred;

	/*
	 * Set of ground actions, and goals with goals time for easier use in the
	 * heuristics calculation functions
	 */
	ArrayList groundActions; // list in GAction class
	ArrayList goals;
	Hashtable goalTime;

	/*
	 * Structure to store the metric-resource related structures. Specifically, the
	 * maximum level for each resource that we can increase, also the time needed to
	 * increase from 0 to that maximum level
	 */
	ArrayList lhsResource; // list of resource-ID for appear in the lhs (can be change)
	float[] maxValue; // Maximum values can be increased to for each resource
	float[] maxResCost; // Cost for action that increase the most for each resource type
	float[] maxDuration; // Time duration needed to increase to the max resource value.
	boolean[] staticDur; // Indicate the action that give that maxValue has static
	// duration or dynamic (that linearly depend on the continuous amount) --> ADD
	// LATER

	/* Functions to return some structures for goals */
	public ArrayList getSortedGoals() {
		return goals;
	}

	public Hashtable getGoalTime() {
		return goalTime;
	}

	/** Function to set the ground actions */
	public void initialize(ArrayList gActions, ArrayList theGoals, Hashtable gTime) {
		groundActions = new ArrayList(gActions);

		// Sort the goals
		ArrayList tempGoals = new ArrayList(theGoals);

		goals = new ArrayList();
		goalTime = new Hashtable(gTime);

		float min;
		Float time;
		int i, index = 0;

		// Sorting the goals according to the increasing goal-time order
		while (tempGoals.size() > 0) {
			min = 1000000;
			for (i = 0; i < tempGoals.size(); i++) {
				if ((time = (Float) goalTime.get(tempGoals.get(i))) != null) {
					if (time.floatValue() < min) {
						min = time.floatValue();
						index = i;
					}
				} else {
					System.out.println("Wrong in finding goal time!!! (utility.initiaze()");
					System.exit(0);
				}
			}
			goals.add(tempGoals.remove(index));
		}
	}

	/* Functions to initialize the max-resource level DS */
	public void setLhs(ArrayList lhs) {
		// lhsResource = new ArrayList(lhs);
		lhsResource = lhs;
		int lhsSize = lhs.size() + 1;
		maxValue = new float[lhsSize];
		maxResCost = new float[lhsSize];
		maxDuration = new float[lhsSize];
	}

	public void maxResPreprocess(GMResDB mresDB) {
		int i, j, index, oper;
		GAction action;
		GMySet ms;
		Integer fID;
		float value, v;

		float dur, cost;

		// First set the values of all target lhs function value to 0
		for (i = 0; i < lhsResource.size(); i++) {
			mresDB.setValue(((Integer) lhsResource.get(i)).intValue(), 0);
			maxValue[i] = 0;
			maxResCost[i] = 0;
			maxDuration[i] = 0;
		}

		// Go through all ground actions that can increase that func values.
		for (i = 0; i < groundActions.size(); i++) {
			action = (GAction) groundActions.get(i);

			if (action.numSet() < 1)
				continue;

			// Get the duration of the action that increase the maximum amount of res
			if (action.getDType())
				dur = action.getDStatic();
			else
				dur = action.getDDynamic().value(mresDB, 0);

			// Get the cost of that action
			if (action.getCType())
				cost = action.getCStatic();
			else
				cost = action.getCDynamic().value(mresDB, 0);

			for (j = 0; j < action.numSet(); j++) {
				ms = (GMySet) action.getSet(j);

				// If not assign the new higher value
				oper = ms.getAssign();

				// Function ID of the function that can be changed
				fID = new Integer(ms.getLeftSide());

				index = lhsResource.indexOf(fID);
				value = ms.getRightSide().value(mresDB, dur);

				// Continue to update the maxValue & maxDuration array's values
				if ((oper == 0 || oper == 2) && (value > maxValue[index])) {
					maxValue[index] = value;
					maxDuration[index] = dur;
					maxResCost[index] = cost;
				}
			}
		}

		/*** Print test ***/
		for (i = 0; i < lhsResource.size(); i++) {
			System.out.println(";; ResID: " + (Integer) lhsResource.get(i) + " MaxValue: " + maxValue[i]
					+ " MaxDuration: " + maxDuration[i]);
		}
	}

	/**
	 * Function to get the set of the function name that appear at the lefthand side
	 * of some assignment (so their values may be modified). This helps when we have
	 * to ground all functions.
	 */
	public ArrayList getLHS(Domain aDomain) {
		ArrayList lhs = new ArrayList();

		for (int i = 0; i < aDomain.numAction(); i++) {
			Action act = aDomain.getAction(i);

			for (int j = 0; j < act.numSet(); j++) {
				MySet ms = act.getSet(j);
				String s = (ms.getLeftSide()).getName();

				if (!lhs.contains(s))
					lhs.add(s);
			}
		}

		return lhs;
	}

	/*
	 * Function to adjust the relaxed plan according to the resource usage of
	 * actions int the domains and the amount of resource available at the inititial
	 * state. Note: propOption = True then cost of the reajustment will be
	 * propositional to the (non-integer) cost of the adjustment actions need to be
	 * added to the relaxed plan. False: first count the *integer* number of actions
	 * need to be added.
	 */
	public float resourceAdjustment(int[] solution, int sSize, GMResDB mresDB, boolean propOption) {
		int i, j, index, oper, actIndex;
		Integer resID;
		float dur, adjustment = 0;

		float[] resLevel = new float[lhsResource.size()];
		GMySet ms;
		Integer fID;
		float value, v;
		GAction aAction;

		/*
		 * Set the initial resource level to the available resource in the state
		 * evaluated
		 */
		for (i = 0; i < lhsResource.size(); i++) {
			resID = (Integer) lhsResource.get(i);
			resLevel[i] = mresDB.getValue(resID.intValue());
		}

		/* Go through the actions in the solution */
		for (i = 0; i < sSize; i++) {
			actIndex = solution[i];
			aAction = (GAction) groundActions.get(actIndex);
			if (aAction.numSet() < 1)
				continue;

			for (j = 0; j < aAction.numSet(); j++) {
				ms = (GMySet) aAction.getSet(j);

				// If not assign the new higher value
				oper = ms.getAssign();
				if ((oper != 1) && (oper != 2))
					continue;

				if (aAction.getDType())
					dur = aAction.getDStatic();
				else
					dur = aAction.getDDynamic().value(mresDB, 0);

				// Function ID of the function that can be changed
				resID = new Integer(ms.getLeftSide());
				index = lhsResource.indexOf(resID);

				// Evaluate the right-side of the "Set" operation, which is a GMathForm
				value = ms.getRightSide().value(mresDB, dur);

				// Check the amount changed
				if (oper == 1)
					resLevel[index] -= value;
				else
					resLevel[index] += value;
			}
		}

		// Addjust the *number of actions* need to be added to the solution
		for (i = 0; i < lhsResource.size(); i++)
			if (resLevel[i] < 0) {
				if (propOption) {
					adjustment -= ((float) resLevel[i] / maxValue[i]) * maxResCost[i];
				} else {
					adjustment -= (new Float((float) resLevel[i] / maxValue[i])).intValue() * maxResCost[i];
				}
			}

		return adjustment;
	}

	/*
	 * Check if the predicate set P at a given time t is consistent with all the
	 * goal's deadline (if some goal G has deadline ti < t, and G is not achieved in
	 * P)
	 */
	public boolean checkConsistentGoalDeadline(GPredDB gp, float time) {
		ArrayList tempGoals = new ArrayList(goals);
		Integer aPred;

		for (int i = 0; i < gp.numPred(); i++) {
			aPred = gp.getPred(i);

			// Remove all goals already achieved.
			if (tempGoals.contains(aPred))
				tempGoals.remove(tempGoals.indexOf(aPred));
		}

		// Check if current time is higher than the earliest goal that is not achieved
		if (time > ((Float) goalTime.get(tempGoals.get(0))).floatValue())
			return false;
		else
			return true;
	}

	public ArrayList getLhsResource() {
		return lhsResource;
	}
}
