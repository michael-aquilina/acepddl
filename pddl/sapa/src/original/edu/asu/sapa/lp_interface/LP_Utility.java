/*****************************************************************
    Author: Minh B. Do - Arizona State University
******************************************************************/
package edu.asu.sapa.lp_interface;

import edu.asu.sapa.basic_ds.*;
import edu.asu.sapa.complex_ds.*;

import java.util.*;

/**
 * LP_Utility.java: Provide some utility functions that support the MILP
 * encoding
 */

public class LP_Utility {
	public LP_Utility() {
	}

	/**
	 * Function to get the resource profile of actions in the p.c plan returned by
	 * Sapa
	 */
	public DResProfile getResProfile(ArrayList actions, ArrayList lhsResource, GMResDB initMResDB) {
		int i, j, k, resIndex;
		Integer resID;
		float rVal;

		DResProfile resPro = new DResProfile(actions.size());

		// Set up the initial values for all dynamic resource (lhsResource)
		for (i = 0; i < lhsResource.size(); i++) {
			resID = (Integer) lhsResource.get(i);
			rVal = initMResDB.getValue(resID.intValue());

			// resPro.addResValue(0, resID, rVal);
		}

		// Now going forward to monitor the resource levels before each action
		GAction anAct;
		GMySet aSet;
		ArrayList lhsRes, rhsRes;

		for (i = 0; i < actions.size(); i++) {
			anAct = (GAction) actions.get(i);
			lhsRes = getLHSRes(anAct);

			// Check if any test's rhs depends on the dynamic value (e.g. fuel >
			// dynamic_distance*consume_rate + 1)
			for (j = 0; j < anAct.numTest(); j++) {
				// Check the rhs to see if it involves a dynamic value
				rhsRes = getRHSRes(anAct.getTest(j).getRightSide());

				for (k = 0; k < rhsRes.size(); k++) {
					resID = (Integer) rhsRes.get(k);
					if (!lhsRes.contains(resID)) {
						System.out.println("LP_Utility.getResProfile: rhs is NOT subsumed by lhs.");
						System.exit(1);
					}

					// Put the dependency
					resPro.addResValue(i, resID, initMResDB.getValue(resID.intValue()));
				}
			}

			// Check if any set's rhs depends on the dynamic value (e.g fuel += capacity -
			// fuel_level)
			for (j = 0; j < anAct.numSet(); j++) {
				aSet = anAct.getSet(j);
				// Check the rhs to see if it involves a dynamic value
				rhsRes = getRHSRes(aSet.getRightSide());

				for (k = 0; k < rhsRes.size(); k++) {
					resID = (Integer) rhsRes.get(k);
					if (!lhsRes.contains(resID)) {
						System.out.println("LP_Utility.getResProfile: rhs is NOT subsumed by lhs.");
						System.exit(1);
					}

					// Put the dependency
					resPro.addResValue(i, resID, initMResDB.getValue(resID.intValue()));
				}

				// If the set type is assignment (=), then we need to put the dependent
				// value on the lhs also (i.e. x = y then the increment = (y-x) depends on both
				// x & y)
				if (aSet.getAssign() == 0)
					resPro.addResValue(i, new Integer(aSet.getLeftSide()), initMResDB.getValue(aSet.getLeftSide()));

			}

			// Update the rhsResource values due to the changes of aSet
			initMResDB.update(anAct, true);
		}

		return resPro;
	}

	/**
	 * Function to get the set of resource in the lhs of a given action
	 */
	private ArrayList getLHSRes(GAction act) {
		int i;
		ArrayList lhs = new ArrayList();

		for (i = 0; i < act.numTest(); i++)
			lhs.add(new Integer(act.getTest(i).getLeftSide()));

		for (i = 0; i < act.numSet(); i++)
			lhs.add(new Integer(act.getSet(i).getLeftSide()));

		return lhs;
	}

	/**
	 * Function to get the set of resource involved in a MathForm
	 */
	private ArrayList getRHSRes(GMathForm mf) {
		ArrayList aList = new ArrayList();
		int type = mf.getType();

		if ((type != 0) && (type != 3))
			return aList;

		if (type == 0) {
			aList.add(new Integer(mf.getElement()));
			return aList;
		}

		aList.addAll(getRHSRes(mf.getLeft()));
		aList.addAll(getRHSRes(mf.getRight()));
		return aList;
	}

}
