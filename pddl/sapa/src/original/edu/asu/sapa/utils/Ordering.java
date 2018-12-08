/************************************************************************
     Author: Minh B. Do - Arizona State University
*************************************************************************/

package edu.asu.sapa.utils;

import java.util.*;

/**
 * Ordering: Datastructure to hold the set of orderings in p.o plan between a
 * specific action and other related actions in the plan.
 */
public class Ordering {
	int id;

	public ArrayList acts = new ArrayList(); // Set of action constrained to be before this act.
	public ArrayList durs = new ArrayList(); // durations from the starting point of a *before*
	// action until the effect that is the same with precond of this action
	// Thus, et^p_{A'} which match the precond p of this action A.

	public ArrayList relList = new ArrayList(); // Each element is a relation
	// (OrderRelation class) representing the ordering relation between another
	// action and this one. Consult the specification of OrderRelation.

	int mutexIndex = 0; // Index in the relList array where we start storing
	// the mutex relation (all causal link until that index)

	public ArrayList redOrder = new ArrayList(); // Each element is an *Integer*
	// represents the index of the mutex ordering relation in the "relList"
	// that is redundant (according to the logical relations between two actions)

	public Ordering(int i) {
		id = i;
	}

	/** Add a temporal ordering between two actions */
	public void addTemporalOrdering(int act, float dur) {
		float maxDur;
		int actIndex;
		Integer actID = new Integer(act);

		if (act == id)
			return;

		if ((actIndex = acts.indexOf(actID)) > -1) {
			maxDur = ((Float) durs.get(actIndex)).floatValue();

			if (maxDur < dur)
				durs.set(actIndex, new Float(dur));
		} else {
			acts.add(actID);
			durs.add(new Float(dur));
		}
	}

	/** Add a logical ordering between two actions */
	public void addLogicalOrdering(int act, int saTime, int eaTime, int rel, int objId) {
		relList.add(new OrderRelation(act, saTime, eaTime, rel, objId));
	}

	public OrderRelation getLogicalOrdering(int index) {
		return (OrderRelation) relList.get(index);
	}

	public int numLogicalOrdering() {
		return relList.size();
	}

	/**
	 * Function to get the supporting action for the "index"th precond
	 */
	public int getSAct(int index) {
		return ((OrderRelation) relList.get(index)).actIndex;
	}

	/**
	 * Remove the temporal ordering relation between the aID action and the action A
	 * associated with _this_ Ordering object (aID->A). Notice that temporal
	 * orderings between them guarantee that (1) A has no relation with aID or (2) A
	 * is constrained to occur after aID with duration durs[acts.indexOf(aID)] in
	 * the set of temporal orderings (of A).
	 */
	public float removeTemporalOrdering(int aId) {
		float maxDur = -1, dur;
		int i;

		Integer act = new Integer(aId);

		while ((i = acts.indexOf(act)) >= 0) {
			dur = ((Float) durs.get(i)).floatValue();
			if (dur > maxDur)
				maxDur = dur;

			acts.remove(i);
			durs.remove(i);
		}

		return maxDur;
	}

	public void setMutexRelIndex() {
		mutexIndex = relList.size();
	}

	public int getMutexIndex() {
		return mutexIndex;
	}

	/**
	 * Function to check if there is an ordering: aId->thisAct
	 */
	public boolean existOrdering(int aId) {
		for (int i = 0; i < relList.size(); i++) {
			if (((OrderRelation) relList.get(i)).actIndex == aId)
				return true;
		}

		return false;
	}

	/**
	 * Remove the unnecessary mutex orderings according to the logical relations
	 * between two actions. Return the number of mutex ordering removed. May want to
	 * extend to return separately the number of logical and resource mutexes
	 * removed. Note: Is not currently used.
	 */
	public int removeMutexOrdering(int aId) {
		OrderRelation or;
		int i, rmCount = 0;

		for (i = mutexIndex; i < relList.size(); i++) {
			or = (OrderRelation) relList.get(i);

			if (or.actIndex == aId) {
				redOrder.add(new Integer(i));
				rmCount++;
			}
		}

		return rmCount;
	}

	public int numBefore() {
		return acts.size();
	}

	public void printOrderings() {
		for (int i = 0; i < acts.size(); i++) {
			System.out.print("<" + (Integer) acts.get(i) + "," + (Float) durs.get(i) + "> ");
		}
	}
}
