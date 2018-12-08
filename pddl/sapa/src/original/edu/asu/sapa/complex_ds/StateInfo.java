/****************************************************************************
    Author: Minh B. Do (Arizona State Univ. - binhminh@asu.edu)
*****************************************************************************/
package edu.asu.sapa.complex_ds;

import edu.asu.sapa.basic_ds.*;

import java.util.*;

/**
 * StateInfo.java: Store the information about the current state such as : (1)
 * Set of predicates that is true & resource information (stored in a GState
 * structure) (2) Set of future events and the occuring time points. (3) Set of
 * actions leading to this state and their execution time. (4) Set of
 * predicates/functions that need to be protected and their end time points. (5)
 * Time at which we measure this state (6) Distance to the goal (heuristically
 * measured) (7) Other information such as total execution cost, or total
 * duration etc.
 */
public class StateInfo {
	private GState currentState;
	private float csTime; // Time of the *cutset* of the current state

	// public for speedup
	public float distance; // Distance future-state --> goal-state (based on some heu)

	public ArrayList actions; // The list of ground action ID leading to this state
	// (may be the same with its parent if we get to this SP by activate events)
	private ArrayList atime; // The starting-time of each action in the *actions* vector
	private ArrayList adur; // The durations of each action in the *actions* vector

	private float totalDuration;
	private float totalExecCost;

	// IMPROVE make the eventQueue a priority queue (sort of like in RMTPG)
	private ArrayList eventQueue; // Sorted future events according to their time

	private ArrayList proPreds; // List of predicates that need to be protected
	private ArrayList proPredTime; // Map each protected predicate with the end time instance

	/*
	 * !!! We can relaxed the use of proFuncs & proFuncTime later, they are not
	 * necessarily be as restricted as proPreds & proPredTime !!!
	 */
	ArrayList proFuncs; // List of functions that need to be protected
	ArrayList proFuncTime; // Map each protected function with the end time instance

	ArrayList pendingConds; // Pending (pre)condition ("at end" conditions of applied actions)
	ArrayList pendingCondTime; // BM: Added on July 22, 2003. Pending conditions are just like
	// normal goals, but they are dynamically added and different from state to
	// state.

	int[] haEffects;
	int numHAPEffects, totalHAEffects;

	public StateInfo(int numFunc) {
		currentState = new GState(numFunc);
		csTime = 0;

		actions = new ArrayList();
		atime = new ArrayList();
		adur = new ArrayList();
		totalDuration = 0;
		totalExecCost = 0;

		eventQueue = new ArrayList();

		proPreds = new ArrayList();
		proPredTime = new ArrayList();

		proFuncs = new ArrayList();
		proFuncTime = new ArrayList();

		pendingConds = new ArrayList();
		pendingCondTime = new ArrayList();
	}

	public StateInfo(StateInfo sp) {
		currentState = new GState(sp.currentState);
		csTime = sp.getCSTime();
		distance = sp.getDistance();

		actions = new ArrayList(sp.getActions());
		atime = new ArrayList(sp.getTime());
		adur = new ArrayList(sp.getADur());

		totalDuration = sp.totalDuration();
		totalExecCost = sp.totalExecCost();

		eventQueue = new ArrayList(sp.getAllEvents());

		proPreds = new ArrayList(sp.getAllPPreds());
		proPredTime = new ArrayList(sp.getAllPPTime());

		proFuncs = new ArrayList(sp.getAllPFuncs());
		proFuncTime = new ArrayList(sp.getAllPFTime());

		pendingConds = new ArrayList(sp.getAllPConds());
		pendingCondTime = new ArrayList(sp.getAllPCTime());

		numHAPEffects = sp.getNumHAPEffects();
		totalHAEffects = sp.getTotalHAEffects();
		haEffects = sp.getHAEffects();
	}

	/**
	 * Set the "State", which contains the predicates that are true and the values
	 * of all functions at the current time point
	 */
	public void setCurrentState(GState s) {
		currentState = new GState(s);
	}

	public void setEventQueue(ArrayList eq) {
		eventQueue = new ArrayList(eq);
	}

	/**
	 * 
	 * @return
	 */
	public GState getCurrentState() {
		// TODO Why is this the thing creating a new instance of the current state?
		// Maybe the caller should do that.
		// return new GState(currentState);
		return currentState;
	}

	public GPredDB getCurrentPredDB() {
		return currentState.getPredDB();
	}

	public GMResDB getCurrentMResDB() {
		return currentState.getMResDB();
	}

	/** Set the time instance of the current state */
	public void setCSTime(float time) {
		csTime = time;
	}

	public float getCSTime() {
		return csTime;
	}

	/**
	 * Set the estimated heuristic *distance* from the current state to the goal
	 */
	public void setDistance(float d) {
		distance = d;
	}

	public float getDistance() {
		return distance;
	}

	/** Number of actions from the initial state leading to this state */
	public int numAction() {
		return actions.size();
	}

	public ArrayList getActions() {
		return actions;
	}

	/** Get the set of time point at which actions leading to this state occur */
	public ArrayList getTime() {
		// IMPROVE remove "new" operator
		return new ArrayList(atime);
	}

	/** Durations of all actions leading to this state */
	public ArrayList getADur() {
		return new ArrayList(adur);
	}

	public float totalDuration() {
		return totalDuration;
	}

	/** Total execution cost of actions leading to this state */
	public float totalExecCost() {
		return totalExecCost;
	}

	/** Add an event to the StateQueue (sorted according to time) */
	public void addEvent(Event e) {
		int i;

		for (i = 0; i < eventQueue.size(); i++)
			if (((Event) eventQueue.get(i)).getTime() > e.getTime()) {
				eventQueue.add(i, e);
				return;
			}

		eventQueue.add(e);
	}

	/**
	 * When advance the clock, get the next time point at which (earliest) events
	 */
	public float getNextEventTime() {
		if (eventQueue.size() == 0)
			return 0;

		return ((Event) eventQueue.get(0)).getTime();
	}

	/**
	 * Get the set of events that occur at the (same) earliest time point from the
	 * current state
	 */
	private ArrayList getNextEvents() {
		ArrayList events = new ArrayList();
		float time = getNextEventTime(); // J. Benton June 13, 2004: Source of big bug
		// float time = getNextTime();
		int i;

		if (time == 0)
			return events;
		int queueSize = eventQueue.size();
		for (i = 0; i < queueSize; i++) {
			Event e = (Event) eventQueue.get(i);
			if (e.getTime() <= time)
				events.add(new Event(e));
			else
				break;
		}

		for (int j = 0; j < i; j++) {
			if (((Event) eventQueue.get(0)).getTime() <= time) {
				eventQueue.remove(0);
			}
		}

		return events;
	}

	/** Get all next events, used to generate duplicate states */
	public ArrayList getAllEvents() {
		return new ArrayList(eventQueue);
	}

	/**
	 * Add a protected predicates (over duration of time). Protected predicates are
	 * action (pre)conditions that need to hold for a duration.
	 */
	private void addProPred(Integer predID, float time) {
		int i;
		for (i = 0; i < proPredTime.size(); i++)
			if (((Float) proPredTime.get(i)).floatValue() >= time)
				break;

		proPreds.add(i, predID);
		proPredTime.add(i, new Float(time));
	}

	/**
	 * Remove protected predicates when we move forward pass the time of some
	 * protected pred.
	 */
	private void deleteProPred(float time) {
		while (true) {
			if (proPredTime.size() < 1)
				break;

			if (((Float) proPredTime.get(0)).floatValue() <= time) {
				proPredTime.remove(0);
				proPreds.remove(0);
			} else {
				break;
			}
		}
	}

	/** Add protected functions -- similar to protected predicates */
	private void addProFunc(Integer funcID, float time) {
		int i;
		for (i = 0; i < proFuncTime.size(); i++)
			if (((Float) proFuncTime.get(i)).floatValue() >= time)
				break;

		proFuncs.add(i, funcID);
		proFuncTime.add(i, new Float(time));
	}

	/** Remove protected functions when we move pass their end time points */
	private void deleteProFunc(float time) {
		while (true) {
			if (proFuncTime.size() < 1)
				break;

			if (((Float) proFuncTime.get(0)).floatValue() <= time) {
				proFuncTime.remove(0);
				proFuncs.remove(0);
			} else {
				break;
			}
		}
	}

	/** Add pending preconditions **/
	private void addPendingCond(Integer predID, float time) {
		int i;
		for (i = 0; i < pendingCondTime.size(); i++)
			if (((Float) pendingCondTime.get(i)).floatValue() >= time)
				break;

		pendingConds.add(i, predID);
		pendingCondTime.add(i, new Float(time));
	}

	/** Delete pending (pre)conditions **/
	private void deletePendingCond(float time) {
		while (true) {
			if (pendingCondTime.size() < 1)
				break;

			if (((Float) pendingCondTime.get(0)).floatValue() <= time) {
				pendingCondTime.remove(0);
				pendingConds.remove(0);
			} else {
				break;
			}
		}
	}

	public ArrayList getAllPConds() {
		return new ArrayList(pendingConds);
	}

	public ArrayList getAllPCTime() {
		return new ArrayList(pendingCondTime);
	}

	public int numPCond() {
		return pendingConds.size();
	}

	public int getPCond(int index) {
		return ((Integer) pendingConds.get(index)).intValue();
	}

	public float getPCTime(int index) {
		return ((Float) pendingCondTime.get(index)).floatValue();
	}

	private float getNextTime() {
		float nextTime = -1, tempTime;

		if (eventQueue.size() > 0)
			nextTime = ((Event) eventQueue.get(0)).getTime();

		if (proPreds.size() > 0) {
			tempTime = ((Float) proPredTime.get(0)).floatValue();
			if (nextTime < 0)
				nextTime = tempTime;
			else if (nextTime > tempTime)
				nextTime = tempTime;
		}

		if (proFuncs.size() > 0) {
			tempTime = ((Float) proFuncTime.get(0)).floatValue();
			if (nextTime < 0)
				nextTime = tempTime;
			else if (nextTime > tempTime)
				nextTime = tempTime;
		}

		return nextTime;
	}

	/**
	 * Violate pending (pre)conditions. Only used when advance-time (to "time"
	 * value)
	 **/
	private boolean violatePendingCond() {
		// Get the next event time based on the earliest time among "events",
		// proPredTime and proFuncTime
		float pcTime, nextTime = getNextTime();

		if (nextTime < 0)
			return false;

		for (int i = 0; i < pendingCondTime.size(); i++) {
			pcTime = ((Float) pendingCondTime.get(i)).floatValue();
			if (pcTime > nextTime)
				return false;
			if ((pcTime < nextTime) && (!currentState.containPred(pendingConds.get(i))))
				return true;
		}

		return false;
	}

	/** Check if one action's delete list violate the protected predicates */
	private boolean violateProPreds(GAction a) {
		Integer aDel;
		int timeFlag, index, i;
		float timeInst;

		float dur;
		if (a.getDType())
			dur = a.getDStatic();
		else
			dur = a.getDDynamic().value(currentState.getMResDB(), csTime);

		/*
		 * Check at the time *instance* that the delete effect will occur, will it
		 * violate with any protected predicate?
		 */
		for (i = 0; i < a.numDelete(); i++) {
			aDel = new Integer(a.getDelete(i));
			if (!proPreds.contains(aDel))
				continue;

			index = proPreds.indexOf(aDel);

			// timeFlag = a.getTimeEffect(aDel);
			timeFlag = a.getDeleteTimeEffect(i);

			if (timeFlag == 0) { // Delete at "st"
				return true;
			} else { // Delete at "et"
				if (((Float) proPredTime.get(index)).floatValue() > csTime + dur)
					return true;
			}
		}

		/**
		 * Check if (1) any "delete" event in the queue will violate with protected
		 * precond or "add" effect of an action, also if an (2) ADD event will violate
		 * with delete effects of an action
		 */
		Event e;
		Integer eventID;

		for (i = 0; i < eventQueue.size(); i++) {
			e = (Event) eventQueue.get(i);
			eventID = e.getPred();

			if (e.getNeg()) { // Negative event
				index = a.indexPrecond(eventID.intValue());
				if (index > 0) { // If event is one precondition
					if (a.getPreTime(index) > 0) { // Precond holds through whole dur
						if (csTime + dur > e.getTime()) { // Violate A's preserve precond
							return true;
						}
					}
				}

				index = a.indexAdd(eventID.intValue());
				if (index > 0) {
					if (a.getAddTimeEffect(index) > 0) {
						if ((csTime + dur) == e.getTime())
							return true;
					}
				}
			} else { // Positive event
				index = a.indexDelete(eventID.intValue());
				if (index > 0) {
					if (a.getDeleteTimeEffect(index) > 0) {
						if ((csTime + dur) == e.getTime())
							return true;
					}
				}
			}
		}

		return false;
	}

	/** Check if one action's "SET" list using some function that is protected */
	private boolean violateProFuncs(GAction a) {
		int funcID;

		for (int i = 0; i < a.numSet(); i++) {
			funcID = a.getSet(i).getLeftSide();

			// Check if this action try to modify some protected function
			if (proFuncs.contains(new Integer(funcID)))
				return true;
		}

		return false;
	}

	public ArrayList getAllPPreds() {
		return new ArrayList(proPreds);
	}

	public ArrayList getAllPPTime() {
		return new ArrayList(proPredTime);
	}

	public ArrayList getAllPFuncs() {
		return new ArrayList(proFuncs);
	}

	public ArrayList getAllPFTime() {
		return new ArrayList(proFuncTime);
	}

	/**
	 * Check if an action is applicable in a current state (assumes predicate
	 * preconditions have already been checked).
	 * 
	 * @param a
	 *            Action to be checked for applicability.
	 */
	public boolean applicable(GAction a) {
		float dur;

		// Added to rule out the zero duration actions
		if (a.getDType())
			dur = a.getDStatic();
		else
			dur = a.getDDynamic().value(currentState.getMResDB(), csTime);

		if (dur <= 0)
			return false;

		if ((!violateProPreds(a)) && (!violateProFuncs(a)) && currentState.applicable(a)) {
			return true;
		}

		return false;
	}

	/**
	 * Applying actions to a StateInfo. The action's effect will change the values
	 * of the set of current predicates that are true, update the values of
	 * functions and introduce new events, protected predicates and functions
	 */
	public void update(GAction a) {
		Event e;
		float dur;
		int i;
		Integer p; // Ground Predicate ID
		int t;

		if (a.getDType())
			dur = a.getDStatic();
		else
			dur = a.getDDynamic().value(currentState.getMResDB(), csTime);

		// Update the current state for *instant* effects ("st")
		currentState.update(a, csTime);
		totalDuration += dur;

		// Update total cost of actions leading to this state
		if (a.getCType())
			totalExecCost += a.getCStatic();
		else
			totalExecCost += a.getCDynamic().value(currentState.getMResDB(), csTime);

		// Add future events
		for (i = 0; i < a.numDelete(); i++) {
			p = new Integer(a.getDelete(i));
			t = a.getDeleteTimeEffect(i);

			if (t < 1) // Effect at "st"
				continue;

			// Instant delete effect at the end of an action
			e = new Event(p, true, csTime + dur);
			addEvent(e);
		}

		for (i = 0; i < a.numAdd(); i++) {
			p = new Integer(a.getAdd(i));
			t = a.getAddTimeEffect(i);

			if (t < 1)
				continue;

			// Instant add effect at the end of an action
			e = new Event(p, false, csTime + dur);
			addEvent(e);
		}

		// Protect all non-instant preconditions
		for (i = 0; i < a.numPrecond(); i++) {
			p = new Integer(a.getPrecond(i));
			t = a.getPreTime(i);

			if (t < 1)
				continue;

			if (t == 1) { // If we have to protect it until end of action
				addProPred(p, csTime + dur);
			} else { // At the pending precondition ("at end" condition)
				addPendingCond(p, csTime + dur);
			}
		}

		// Protect all resources used during the course of this action
		for (i = 0; i < a.numSet(); i++) {
			addProFunc(new Integer(a.getSet(i).getLeftSide()), csTime + dur);
		}

		/*** Add action A to the list ***/
		actions.add(new Integer(a.getID()));
		atime.add(new Float(csTime));
		adur.add(new Float(dur));
	}

	/**
	 * Check if there is some future event or remaining predicate/function that
	 * still need to be protected
	 */
	public boolean canMoveForward() {
		if (((eventQueue.size() > 0) || (proPreds.size() > 0) || (proFuncs.size() > 0)) && (!violatePendingCond()))
			return true;
		return false;
	}

	/**
	 * Move forward to the first event's time, and update the current-state
	 * according to earliest events
	 */
	public void moveForward() {
		float nextTime = getNextTime();

		csTime = nextTime;

		if ((eventQueue.size() > 0) && (nextTime >= getNextEventTime())) {
			ArrayList events = getNextEvents();
			currentState.update(events);
		}

		deleteProPred(csTime);
		deleteProFunc(csTime);
		deletePendingCond(csTime);
	}

	/**
	 * Function to get the gValue, which equals the total execution cost of actions
	 * in the partial plan. Used to guide the A* search
	 */
	public float gValue() {
		return totalExecCost;
	}

	/**
	 * Set up the set of helpful action's effects. Similar to the technique used in
	 * FF's helpful action technique
	 */
	public void setHaEffects(int[] effects, int numPEffects, ArrayList nEffects) {
		numHAPEffects = numPEffects;

		if (nEffects == null)
			totalHAEffects = numHAPEffects;
		else
			totalHAEffects = numHAPEffects + nEffects.size();

		haEffects = new int[totalHAEffects];

		int i;
		for (i = 0; i < numHAPEffects; i++)
			haEffects[i] = effects[i];

		if (nEffects != null) {
			for (i = 0; i < nEffects.size(); i++)
				haEffects[numHAPEffects + i] = ((Integer) nEffects.get(i)).intValue();
		}
	}

	public int[] getHAEffects() {
		return haEffects;
	}

	/** Number of "positive" helpful effects */
	public int getNumHAPEffects() {
		return numHAPEffects;
	}

	/** Total number of both "positive" and "negative" helpful effects */
	public int getTotalHAEffects() {
		return totalHAEffects;
	}

	/**
	 * Check if a given action is qualified if we choose to use the helpful action
	 * option
	 */
	public boolean haQualified(GAction act, boolean haneFlag) {
		int i, j, numAdd, factID;

		numAdd = act.numAdd();
		if (numAdd == 0)
			return true;

		for (i = 0; i < numAdd; i++) {
			factID = act.getAdd(i);

			for (j = 0; j < numHAPEffects; j++)
				if (factID == haEffects[j])
					return true;
		}

		// If we want to check the negative effects also
		if ((haneFlag == true) && (totalHAEffects > numHAPEffects)) {
			for (i = 0; i < act.numDelete(); i++) {
				factID = act.getDelete(i);

				for (j = numHAPEffects; j < totalHAEffects; j++)
					if (factID == haEffects[j])
						return true;
			}
		}

		return false;
	}
}
