/************************************************************
   Author: Minh B. Do -- Arizona State University
*************************************************************/
package edu.asu.sapa.rmtpg;

import edu.asu.sapa.complex_ds.*;
import edu.asu.sapa.basic_ds.*;

import java.util.*;

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

public class RMTPG {
	/* Constants */
	int RPMAXSIZE = 500;

	public final static boolean statistics = false;
	public static ArrayList recordationOfEventSize;

	/* List of FactLink & ActLink to make up a bi-level graph */
	FactLink[] factLevel;
	ActLink[] actLevel;

	int numAction;
	int numFact;

	EventQueue eventQueue = new EventQueue(); // List of *CostEvent* sorted by time

	ArrayList tempGoals; // To hold the set of goals in process. Sorted in the increasing
	// order of time to achieve. Used when propagate the cost in RMTPG.

	float[] actionZeros; // for initializing int arrays with native arraycopy
	int[] initialNumPredNotSat;
	int[] numPredNotSat; // For efficient growing the graph
	// this is the number of precondition still not satisfied for
	// each action, graduatlly reduced to 0.
	float[] maxActionCost, sumActionCost; // Costs of actions according to the set
	// of its preconditions that are satisfied until now. Will
	// be updated according to "max", "sum" or "combo" rule.

	ArrayList goals; // Set of goals in the initial problem specification
	ArrayList remainGoals; // Used when extracting the direct heuristics. Also used to build sortedGoals.
	ArrayList sortedGoals = new ArrayList(); // Goals sorted in decreasing cost order
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

	ArrayList readyActions = new ArrayList(); // List of action ready to be
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
	ArrayList dGoal = new ArrayList();
	ArrayList dGoalTime = new ArrayList();

	public RMTPG() {
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
	public void initGoals(ArrayList g, Hashtable gt) {
		goals = new ArrayList(g);
		goalTime = new Hashtable(gt);

		latestGoalDeadline = ((Float) goalTime.get(g.get(g.size() - 1))).floatValue();
	}

	public float getLatestGoalTime() {
		return latestGoalDeadline;
	}

	/**
	 * Function to add a cost-event to the queue (at the right time point)
	 */
	private void addCostEvent(CostEvent ce) {
		int i, index;

		/*
		 * index = eventQueue.size(); for(i = 0; i < index; i++) { // IMPROVE remove
		 * cast. if(((CostEvent)eventQueue.get(i)).time > ce.time ) {
		 * eventQueue.add(i,ce); return; } }
		 */
		eventQueue.add(ce);
	}

	/**
	 * Reset all parameters for next round of building the RMTPG. Thus, reset the
	 * fact and action levels to "empty". IMPROVE Possible improving code: for a
	 * given state, apply an action to it will not change much so we may still keep
	 * the same state but just account for the changes instead of reset everything
	 * again possibilities - (-- the difference of two sets -- make only the small
	 * changes required for the new state -- or maybe copy the whole state structure
	 * into the planning graph, thereby eliminating the need to repeat states all
	 * together.)
	 */
	private void resetBiLevelGraph() {
		int i;

		System.arraycopy(initialNumPredNotSat, 0, numPredNotSat, 0, numAction);
		System.arraycopy(actionZeros, 0, maxActionCost, 0, numAction);
		System.arraycopy(actionZeros, 0, sumActionCost, 0, numAction);
		for (i = 0; i < numAction; i++) {
			actLevel[i].deactivate();
		}

		for (i = 0; i < numFact; i++) {
			factLevel[i].deactivate();
		}

		eventQueue.clear();
	}

	/****************************************************************************
	 * Set of functions to build the bi-level graph structure to help calculate the
	 * relaxed plan fast.
	 *****************************************************************************/
	/**
	 * Main function to build the bi-level graph. (numPred is returned from
	 * StateManager.numPreds()) Creates an action array and fact array used in the
	 * planning graph. IMPROVE groundActions -- casting required... turn into array
	 * of GActions.
	 */
	public void buildBiLevelGraph(int numPred, ArrayList groundActions) {
		int i, j, k, numAct = groundActions.size();
		GAction action;
		int predId;

		ActLink[] al;
		FactLink[] fl;

		float duration, cost;

		// TODO Begin creation of a dependency graph (click this to find out more)
		/*
		 * For each resource find all actions that modify it, create a collection of
		 * each of those actions.
		 */

		actLevel = new ActLink[numAct];
		factLevel = new FactLink[numPred];
		int actPlusOne = numAct + 1;
		numPredNotSat = new int[actPlusOne];
		maxActionCost = new float[actPlusOne];
		sumActionCost = new float[actPlusOne];
		// actionZeros is used to "zero out" with an arraycopy
		actionZeros = new float[actPlusOne];
		/*
		 * for (int l=0;l<actPlusOne;l++) { actionZeros[l] = 0; }
		 */
		numAction = numAct;
		numFact = numPred;

		for (i = 0; i < numPred; i++) {
			factLevel[i] = new FactLink(i);
		}

		/* Go through the list of all ground actions to set up the two levels */
		for (i = 0; i < numAct; i++) {
			action = (GAction) groundActions.get(i);
			// sanity check
			if (action.getID() != i) {
				System.out.println("Something wrong in buildBiLevelGraph");
				System.exit(1);
			}

			// is this action's duration constant (true) or dynamic (false)
			if (action.getDType() == true)
				duration = action.getDStatic(); // if this value is constant, then get const value
			else {
				// TODO put formula for duration at this time point (i.e. GMathForm)
				duration = 0; // if this value is dynamic, set duration = 0
			}

			// if this action's cost is constant (true) or dynamic (false)
			if (action.getCType() == true) {
				cost = action.getCStatic(); // get constant cost
			} else {
				cost = 0; // set cost = 0 (indicates cost is not constant)
			}
			// if this action has a dynamic duration, then the duration is given
			// by a GMathForm object
			if (action.getDType()) {
				// generate a new action with this value (index given to ActLink -- for plan
				// extraction?)
				// note that the mathform in this action will be null in this case
				actLevel[i] = new ActLink(i, duration, action.numInsPrecond(), action.numAdd(), cost);
			} else {
				// duration on action is dynamic, so put GMathForm into ActLink.
				actLevel[i] = new ActLink(i, action.getDDynamic(), action.numInsPrecond(), action.numAdd(), cost);
			}

			// BM: Added July 23, 2003 for "at end".
			// Store the actions that have no instant preconditions (is applicable in any
			// state)
			if (action.numInsPrecond() == 0) {
				noCondActs[numNCA++] = i;
			}

			int numPrecond = action.numPrecond();
			/*
			 * if (numPrecond != action.numInsPrecond()) { System.out.println(action);
			 * System.out.println("not equal- precond:" + numPrecond + " ins:" +
			 * action.numInsPrecond() + " id:" + action.getID()); }
			 */
			// Get all precond of an action and set up links
			for (j = 0; j < numPrecond; j++) {

				// BM: July 23, 2003. Ignore the "at end" conditions when building the RMTPG
				if (action.getPreTime(j) < 2) {
					predId = action.getPrecond(j); // id of precondition (correponds to fact link)
					actLevel[i].addPrecond(predId, j);
					factLevel[predId].addSpedAct(new Integer(i));
				}
			}

			int numAdds = action.numAdd();
			// Get all effects of an action and set up links
			for (j = 0; j < numAdds; j++) {
				predId = action.getAdd(j);
				actLevel[i].addEffect(predId, action.getAddTimeEffect(j), j);
				factLevel[predId].addSpAct(new Integer(i));
			}
		}

		// initialize initialNumPredNotSat (for action applicability for states and
		// RMTPG)
		initialNumPredNotSat = new int[numPredNotSat.length];
		for (i = 0; i < numAction; i++) {
			initialNumPredNotSat[i] = actLevel[i].numPrecond();
		}
	}

	/**
	 * Function to help costPropagation() by handling the effect of an action by:
	 * (1) Improve fact cost if possible; (2) Activate actions supported by that
	 * fact.
	 */
	private void effectHandler(int effectID, float factCost, float ctime, int aID) {
		int i, j, k, l, factID;
		Integer actID;
		float oldFactCost;
		boolean addedFlag;

		// If the effect of an action introduces a new fact into the fact level
		// IMPROVE LOOK AT HOW TO USE *NEWMAXCOST* IN THE LOOKAHEAD TO SIMPLIFY CODE
		// HERE
		if (!factLevel[effectID].isIn()) {
			factLevel[effectID].addCost(ctime, factCost, aID);

			k = factLevel[effectID].numSpedAct();
			for (j = 0; j < k; j++) {
				actID = factLevel[effectID].getSpedAct(j);

				// Ready to execute this action if all preconds are satisfied
				if (--numPredNotSat[actID.intValue()] == 0) {
					readyActions.add(actID);
				}

				// Update the cost of an action
				if ((costPropOption == 0) || (costPropOption == 2)) {
					if (maxActionCost[actID.intValue()] < factCost)
						maxActionCost[actID.intValue()] = factCost;
				}
				if ((costPropOption == 1) || (costPropOption == 2)) {
					sumActionCost[actID.intValue()] += factCost;
				}
			}

			// Remove the fact from the goals set (if it equals to a goal)
			tempGoals.remove(new Integer(effectID));
		} else { // If old fact (already in fact level) & cost is improved
			oldFactCost = factLevel[effectID].getCost(-1);
			if (factCost < oldFactCost) {
				factLevel[effectID].addCost(ctime, factCost, aID);

				// Go through list of actions that are supported by that fact
				k = factLevel[effectID].numSpedAct();
				for (j = 0; j < k; j++) {
					actID = factLevel[effectID].getSpedAct(j);

					// Check if we can improve the cost of an action
					addedFlag = false;
					if ((costPropOption == 0) || (costPropOption == 2)) {
						// "max" propagation, need to look at all preconds
						for (l = 0; l < actLevel[actID.intValue()].numPrecond(); l++) {
							factID = actLevel[actID.intValue()].getPrecond(l);

							if ((factID != effectID) && (factLevel[factID].isIn())
									&& (factLevel[factID].getCost(-1) > factCost))
								break;
						}
						// If we actually improve the cost of an action in "max"
						if (l >= actLevel[actID.intValue()].numPrecond()) {
							maxActionCost[actID.intValue()] = factCost;
							if (numPredNotSat[actID.intValue()] == 0) {
								readyActions.add(actID);
								addedFlag = true;
							}
						}
					}
					if ((costPropOption == 1) || (costPropOption == 2)) {
						// "sum" propagation is simpler
						sumActionCost[actID.intValue()] += (factCost - oldFactCost);
						if ((!addedFlag) && (numPredNotSat[actID.intValue()] == 0))
							readyActions.add(actID);
					}
				}
			}
		}
	}

	/**
	 * Propagate the cost information on the RMTPG + Option1: 0 - "max", 1 - "sum"
	 * or 2 - "combination" propagation + Option2 = k: k-lookahead (k = -1: Infinite
	 * lookahead)
	 *
	 * Whenever we add one fact, then update the list of precond for actions that
	 * that fact supports (reduce the list). Consider that action only when the
	 * number of unsatisfied precond reach zero. Then add instant effects and do the
	 * same thing. In this way, we don't have to go through the list of all actions
	 * one at a one but rather put actions in on-demand. This can save some time.
	 * The same happens when updating the cost of some fact and we update the costs
	 * of only actions that are supported by that fact (on-demand also).
	 *
	 * NOTE: The propagation procedure is the same regardless of the value of
	 * deadlineFlag (checked during this routine). Except for the infinite lookahead
	 * we do not care if currentTime is more than the latest deadline or not for
	 * deadlineFlag = True. BM: Added July 23, 2003. dGoals are dynamic goals
	 * representing the pending (at end) preconditions. We relaxed the condition
	 * that dynamic goals should be achieved with deadline now.
	 * 
	 * @param ctime
	 */
	public int costPropagation(GPredDB currentState, ArrayList events, float ctime, ArrayList pendingCond,
			ArrayList pendingCondTime) {
		int i, j, k, l, effectID, predIndex, addedAction, aID;
		float actionCost = 1, effectTime, factCost, oldFactCost, oldActionCost;
		Integer aPred, actID;
		Event e;
		CostEvent ce = new CostEvent(0, 0, 0, 0);
		boolean addedFlag;

		// currentTime = ctime;

		// Reset the graph
		// Clears costs, events
		resetBiLevelGraph();
		// generate new temporary goals.
		tempGoals = new ArrayList(goals);
		// generate new remaining goals from set of goals needed
		remainGoals = new ArrayList(goals);
		readyActions.clear();

		dGoal = new ArrayList(pendingCond);
		dGoalTime = new ArrayList(pendingCondTime);

		// If we want to extract the relaxed plan -> prepare set of supported facts
		if (relaxedPlanOption) {
			spedFact.clear();
			spedFactTime.clear();
		}

		initStateSize = currentState.numPred();

		// Setup so that all facts in the initial state are "in"
		for (i = 0; i < initStateSize; i++) {
			aPred = currentState.getPred(i); // moving through all predicates in the current state
			predIndex = aPred.intValue(); // get the "fact index" for this pred.
			factLevel[predIndex].addCost(ctime, 0, -1);

			// need to do this so that we can extract the relaxed plan
			if (relaxedPlanOption) {
				spedFact.add(aPred);
				spedFactTime.put(aPred, new Float(0));
			}

			// Remove all goals already achieved (try to only achieve remaining goals.)
			// -- we have already achieved some goals (during the course of the search),
			// and tempGoals and remainGoals contain all goals in the goal state.
			// so we remove those goals. (tempGoals and remainGoals are equal right now)
			if ((j = tempGoals.indexOf(aPred)) > -1) {
				tempGoals.remove(j);
				remainGoals.remove(j);
			}

			if ((j = dGoal.indexOf(aPred)) > -1) {
				dGoal.remove(j);
				dGoalTime.remove(j);
			}

			// Reduce the number of precond that need to be satisfied for
			// actions supported by this fact
			k = factLevel[predIndex].numSpedAct();

			// action link for the current action we're looking at--
			// we use to determine the resource level required for this
			// action.
			ActLink actLink;
			for (j = 0; j < k; j++) {
				actID = factLevel[predIndex].getSpedAct(j);
				actLink = actLevel[actID.intValue()];

				// Put the action in to the list of ready to exec if all precond sat
				// numPredNotSat[] is an array of values indicating how many
				// preconditions need to be satisified for a particular action (given
				// by index (which = action number)
				if (--numPredNotSat[actID.intValue()] == 0) {
					readyActions.add(actID); // action can be executed if all preconditions have been met
					// Do not need to update the cost because all preconds
					// have cost = 0
				}
			}
		}

		// BM: July 23, 2003. For actions having no instantaneous condition
		for (i = 0; i < numNCA; i++)
			readyActions.add(new Integer(noCondActs[i]));

		// Put all hanging *positive* events in the event queue

		// eventQueue.clear();
		for (i = 0; i < events.size(); i++) {
			e = (Event) events.get(i);
			if (!e.getNeg()) {
				addCostEvent(new CostEvent(e.getPred().intValue(), e.getTime(), 0, -1));
			}
		}

		// Check if all the goals are achieved
		if ((tempGoals.size() == 0) && (dGoal.size() == 0))
			return 0;

		// Check if current time is higher than the earliest goal that is not achieved
		// Only check just before advance time.
		if (deadlineFlag && (ctime > ((Float) goalTime.get(tempGoals.get(0))).floatValue()))
			return -1;

		/*******************************
		 * Start propagating the cost *
		 *******************************/
		while (true) {
			// Activate all actions that are ready
			while (!readyActions.isEmpty()) {
				// Note that the readyActions() may be new action, or may be action
				// that already present but now is improved in cost. New action
				// can be though of as improving cost from "infinity"
				actID = (Integer) readyActions.remove(0);
				aID = actID.intValue();

				// Set the cost for the action
				if (costPropOption == 0) // max
					actionCost = maxActionCost[aID];
				else if (costPropOption == 1) // sum
					actionCost = sumActionCost[aID];
				else if (costPropOption == 2) // combo
					actionCost = (float) 0.5 * (sumActionCost[aID] + maxActionCost[aID]);
				else {
					System.out.println("Unknown propagation flag");
					System.exit(1);
				}
				actLevel[actID.intValue()].addCost(ctime, actionCost);

				// Take care of all action's effects
				for (i = 0; i < actLevel[aID].numEffect; i++) {
					effectID = actLevel[aID].getEffect(i);
					effectTime = actLevel[aID].getEffectTime(i);
					factCost = actionCost + actLevel[aID].getExecCost();

					// If it's delay effect, then put in the event queue
					if (effectTime > 0) {
						addCostEvent(new CostEvent(effectID, ctime + effectTime, factCost, aID));
					} else { // Activate immediate effects
						effectHandler(effectID, factCost, ctime, aID);
					}
				}
			}

			/*
			 * Now we can not add any more actions (exhaused the readyActions) ==> we go to
			 * the eventQueue
			 */
			// First check if all the goal are presented (tempGoals is empty)
			if (tempGoals.size() == 0) {
				// Check *lookaheadOption* for a stopping criteria (k-lookahead)
				if (lookaheadOption == 0) { // 0-lookahead
					return 1;
				} else if (lookaheadOption < 0) { // infinite lookahead
					// Do nothing, just wait for the event queue become empty
				} else { // k-lookahead
					if (lookaheadOption == 1)
						oneLookahead();
					else
						twoLookahead();
					return 1;
				}
			}

			// Find the first event in the list and activate all events at the same
			// time point. NOTE that only consider event if it *IMPROVES FACT COST*
			// actions supported by improved cost facts will be considered to add
			// into the *readyActions* similar to the way we do it above
			addedFlag = false;
			while (eventQueue.size() > 0) {
				ce = eventQueue.remove();
				predIndex = ce.factID;

				if ((!factLevel[predIndex].isIn()) || (ce.cost < factLevel[predIndex].getCost(-1))) {
					addedFlag = true;
					break;
				}
			}

			// If no more "good" event in the event queue and there are still some
			// remaining goals. This node (search state) is the deadend
			if (!addedFlag) {
				if (tempGoals.size() > 0) {
					/*
					 * System.out.print("Can't achieve goals: "); for(int m = 0; m <
					 * tempGoals.size(); m++) { System.out.print(" " + (Integer) tempGoals.get(m));
					 * } System.out.println(".  Try to run with -dupa flag on !!");
					 */
					for (int x = 0; x < tempGoals.size(); x++) {
						System.out.print(tempGoals.get(x) + " ");
					}
					System.out.println();
					System.out.println(";; goals cannot be satisfied");
					return -1;
				} else
					return 1;
			}

			// First check if new time advanced to is bigger than the earliest goal
			// remaining. If it's the case, then abort and return *inconsistency*
			ctime = ce.time; // advance time

			if (deadlineFlag) {
				// Specially handled for the *infinite lookahead* option
				if (lookaheadOption < 0) {
					if ((tempGoals.size() == 0) && (ctime > latestGoalDeadline))
						return 1;

					if ((tempGoals.size() > 0) && (ctime > ((Float) goalTime.get(tempGoals.get(0))).floatValue())) {
						return -1;
					}
				} else {
					// Check if current time is bigger than the earliest deadline of
					// non-achieved goal
					if ((ctime > ((Float) goalTime.get(tempGoals.get(0))).floatValue()))
						return -1;
				}
			}

			// Update actions just *EXACTLY* like above (when activate immediate effects)
			// put actions in the readyActions array.
			while (true) {
				effectID = ce.factID;
				factCost = ce.cost;

				effectHandler(effectID, factCost, ctime, ce.actID);

				// Get all events from the list that occur at the same time with first event
				addedFlag = false;
				while (eventQueue.size() > 0) {
					ce = eventQueue.get();
					if (ce.time > ctime) {
						break;
					} else {
						if ((!factLevel[ce.factID].isIn()) || (ce.cost < factLevel[ce.factID].getCost(-1))) {
							addedFlag = true;
							eventQueue.remove();
							break;
						} else {
							eventQueue.remove();
						}
					}
				}

				// If there is no more "good" event occur at "ctime"
				if (!addedFlag)
					break;
			}

			// used for researching information about states (for choosing data structures
			// and algorithms). NOTE that if statistics == false, compiler will *NOT*
			// include
			// this code in the class file (when using Sun's javac 1.4.2_04)
			if (statistics) {
				// XXX RECORD queue size for study.
				if (eventQueue.size() > 0) {
					recordationOfEventSize.add(new Integer(eventQueue.size()));
				}
			}
		}
	}

	/**
	 * Function to get the heuristic values directly from the propagated costs in
	 * the RMTPG. The option decide if it's "sum/max/combo" of the final propagated
	 * values. Note that this option may be independent of the way we propagate the
	 * cost Thus, we can propagate using *max* and then "adjust" the value by *sum*
	 * the cost of goals.
	 */
	private float getDirectHeuristic() {
		float goalCost, totalCost = 0, maxCost = 0;
		int numGoal = remainGoals.size();
		Integer goalID;
		Float gTime;

		for (int i = 0; i < numGoal; i++) {
			goalID = (Integer) remainGoals.get(i);
			gTime = (Float) goalTime.get(goalID);

			if (!deadlineFlag) {
				goalCost = factLevel[goalID.intValue()].getCost(-1);
			} else {
				goalCost = factLevel[goalID.intValue()].getCost(gTime.floatValue());
			}
			if (goalCostOption == 0) { // Max cost
				if (goalCost > totalCost)
					totalCost = goalCost;
			} else if (goalCostOption == 1) {
				totalCost += goalCost;
			} else { // Combo propagation
				if (goalCost > maxCost)
					maxCost = goalCost;
				totalCost += goalCost;
			}
		}

		if (goalCostOption < 2)
			return totalCost;
		else
			return (float) 0.5 * (maxCost + totalCost);
	}

	/**
	 * Function to sort the goals according to the highest cost first so we can take
	 * care of the possible positive information
	 */
	private void sortTheGoals() {
		int i, j, index = 0;
		Integer goal;
		float cost;
		ArrayList goalCostVector = new ArrayList();

		sortedGoals.clear();
		// BM: Added July 23, 2003. Merge the remaining static and dynamic goals
		remainGoals.addAll(dGoal);

		for (i = 0; i < remainGoals.size(); i++) {
			goal = (Integer) remainGoals.get(i);
			if (!deadlineFlag)
				cost = factLevel[goal.intValue()].getCost(-1);
			else
				cost = factLevel[goal.intValue()].getCost(((Float) goalTime.get(goal)).floatValue());

			for (j = 0; j < i; j++)
				if (((Float) goalCostVector.get(j)).floatValue() < cost)
					break;

			goalCostVector.add(j, new Float(cost));
			sortedGoals.add(j, goal);
		}
	}

	/**
	 * Function to extract the relaxed plan and then sum up the costs of actions in
	 * the relaxed plan for the cost part of the heuristic value. OPTION: Has
	 * deadlines or not. No deadline mean faster extraction
	 */
	private float relaxedPlanHeuristic() {
		int i, j, index, aID, fID;
		float fTime = 0, minATime = 0, maxATime = 0, heuValue, factCost;
		// min,max time to execute an action
		// Those values are valid in dealine cases, and depend on the step cost function
		Integer goal, factID;
		Float gTime = new Float(0), factTime;
		Hashtable sortedGoalTime;

		sortedGoalTime = new Hashtable(goalTime);
		// BM: Added July 23, 2003. Merge the static and dynamic goal time
		for (i = 0; i < dGoal.size(); i++)
			sortedGoalTime.put(dGoal.get(i), dGoalTime.get(i));

		rpIndex = 0;
		sortTheGoals();

		while (true) {
			if (sortedGoals.isEmpty())
				break;

			// Select actions to support remaining goals not merged with *spedFacts*
			goal = (Integer) sortedGoals.remove(0);
			if (!deadlineFlag)
				aID = factLevel[goal.intValue()].getCostAction(-1);
			else {
				gTime = (Float) sortedGoalTime.get(goal);
				aID = factLevel[goal.intValue()].getCostAction(gTime.floatValue());
			}

			// If supported by the initial (current) state
			if (aID < 0)
				continue;

			// Handle the action
			relaxedPlan[rpIndex++] = aID;
			if (rpIndex >= RPMAXSIZE) {
				System.out.println("The relaxed plan reach size limit of " + RPMAXSIZE + " actions");
				System.exit(1);
			}

			// Did not commit to which time point we will execute this action
			// so we specify the range to execute the action which the cost is same
			if (deadlineFlag) {
				maxATime = gTime.floatValue() - actLevel[aID].getETime(goal.intValue());
				minATime = actLevel[aID].getMinTime(maxATime);
			}

			// Handle the effects of newly selected action
			for (i = 0; i < actLevel[aID].numEffect; i++) {
				fID = actLevel[aID].getEffect(i);
				if (deadlineFlag) {
					fTime = actLevel[aID].getEffectTime(i); // Relative to action's start time
				}

				// Merge with the current goal set
				if ((index = sortedGoals.indexOf(new Integer(fID))) > -1) {
					if (!deadlineFlag)
						sortedGoals.remove(index);
					else {
						gTime = (Float) sortedGoalTime.get(sortedGoals.get(index));

						// Check if in the time range of executable action
						// we can support the goals or not.
						if (gTime.floatValue() > minATime + fTime) {
							sortedGoals.remove(index);
							if (gTime.floatValue() < maxATime + fTime)
								maxATime = gTime.floatValue() - fTime;
						}
					}
				}

				// Add to the supported fact list for non-deadline case
				if (!deadlineFlag) {
					factID = new Integer(fID);
					if (!spedFact.contains(factID))
						spedFact.add(factID);
				}
			}

			// Add to the supported fact list in case of exist goal deadlines
			if (deadlineFlag) {
				for (i = 0; i < actLevel[aID].numEffect; i++) {
					factID = new Integer(actLevel[aID].getEffect(i));
					fTime = actLevel[aID].getEffectTime(i);
					factTime = new Float(fTime + maxATime);

					if (!spedFact.contains(factID)) {
						spedFact.add(factID);
						spedFactTime.put(factID, factTime);
					} else if (((Float) spedFactTime.get(factID)).floatValue() > factTime.floatValue()) {
						spedFactTime.put(factID, factTime);
					}
				}
			}

			// Now add the list of preconditions to the *sortedGoal* & sortedGoalTime
			for (i = 0; i < actLevel[aID].numPrecond(); i++) {
				fID = actLevel[aID].getPrecond(i);
				// Check if supported by the initial state
				if (deadlineFlag) {
					factCost = factLevel[fID].getCost(maxATime);
				} else {
					factCost = factLevel[fID].getCost(-1);
				}

				if (factCost < 0) {
					System.out.println("..Error in getRelaxedHeuristic(): Cost fact < 0...");
					System.exit(0);
				} else if (factCost == 0) { // Supported by initial state
					continue;
				}

				goal = new Integer(fID);

				if (!deadlineFlag) {
					if (!spedFact.contains(goal))
						sortedGoals.add(goal);
				} else {
					if ((!spedFact.contains(goal)) || ((Float) spedFactTime.get(goal)).floatValue() > maxATime) {
						if (!sortedGoals.contains(goal)) {
							sortedGoals.add(goal);
							sortedGoalTime.put(goal, new Float(maxATime));
						} else if (((Float) sortedGoalTime.get(goal)).floatValue() > maxATime) {
							sortedGoalTime.put(goal, new Float(maxATime));
						}
					}
				}
			}
		}

		// Now handle the relaxed plan to get the final heuristic value
		heuValue = 0;
		for (i = 0; i < rpIndex; i++) {
			aID = relaxedPlan[i];
			heuValue += actLevel[aID].getExecCost();
		}

		return heuValue;
	}

	/**
	 * Function to stop the search according to the k-lookahead criteria parts of
	 * code are copied from *costPropagation* function, but put here to make thing
	 * clear.
	 *
	 * IMPROVE We only support 1 or 2-lookahead now, but may implement the general
	 * approach later.
	 */
	private void oneLookahead() {
		int i, j, predIndex;
		CostEvent ce;

		CostEvent[] events = eventQueue.toArray();

		for (i = 0; i < eventQueue.size(); i++) {
			// ce = (CostEvent) eventQueue.get(i);
			ce = events[i];
			if (deadlineFlag && (ce.time > latestGoalDeadline))
				return;

			predIndex = ce.factID;

			// If it's a good event
			if ((!factLevel[predIndex].isIn()) || (ce.cost < factLevel[predIndex].getCost(-1))) {
				factLevel[predIndex].addCost(ce.time, ce.cost, ce.actID);
			}
		}
	}

	/**
	 * Two-lookahead, much more complicated than one-lookahead because we have to
	 * take care of the set of actions supported. costPropOption: "max", "sum" or
	 * "combo" propagation
	 */
	private void twoLookahead() {
		int i, j, k, predIndex, effectID, actID, eventQueueSize;
		CostEvent ce;
		boolean newFactFlag, costReduced;
		ArrayList secondEventQueue = new ArrayList();
		float factCost, oldFactCost = 0, effectTime, actionCost = 1;

		CostEvent[] events = eventQueue.toArray();

		eventQueueSize = events.length;
		for (i = 0; i < eventQueueSize; i++) {
			// ce = (CostEvent) eventQueue.get(i);
			ce = events[i];
			predIndex = ce.factID;

			// If it's a good event
			if (factLevel[predIndex].isIn())
				oldFactCost = factLevel[predIndex].getCost(-1);
			if ((!factLevel[predIndex].isIn()) || (ce.cost < oldFactCost)) {
				if (ce.time > latestGoalDeadline)
					break;

				factCost = ce.cost;
				if (!factLevel[predIndex].isIn()) {
					newFactFlag = true;
				} else {
					newFactFlag = false;
				}
				factLevel[predIndex].addCost(ce.time, factCost, ce.actID);

				// Take care of actions supported by this fact
				// If event introduces new fact

				// New fact -> update actions supported by this fact
				k = factLevel[predIndex].numSpedAct();
				for (j = 0; j < k; j++) {
					actID = ((Integer) factLevel[predIndex].getSpedAct(j)).intValue();
					if (newFactFlag)
						--numPredNotSat[actID];

					// Update the cost of an action
					costReduced = false;
					if ((costPropOption == 0) || (costPropOption == 2)) {
						if (newFactFlag && (maxActionCost[actID] < factCost)) {
							maxActionCost[actID] = factCost; // new fact cost
							costReduced = true;
						}

						// If not a new fact, see if the new reduced cost cause the reduction
						// of the cost
						if ((!newFactFlag) && (newMaxCost(actID))) {
							costReduced = true;
						}
					}
					if ((costPropOption == 1) || (costPropOption == 2)) {
						if (newFactFlag)
							sumActionCost[actID] += factCost;
						else
							sumActionCost[actID] -= (oldFactCost - factCost);

						costReduced = true;
					}

					if (costReduced && (numPredNotSat[actID] == 0)) {
						// Handle immediate effects of the action has cost improved here
						// Because it's 2-lookahead, we don't propagate the cost changes
						// to *other* actions

						// Set the cost for the action
						if (costPropOption == 0)
							actionCost = maxActionCost[actID];
						else if (costPropOption == 1)
							actionCost = sumActionCost[actID];
						else if (costPropOption == 2)
							actionCost = (float) 0.5 * (sumActionCost[actID] + maxActionCost[actID]);

						actLevel[actID].addCost(ce.time, actionCost);

						for (i = 0; i < actLevel[actID].numEffect; i++) {
							effectID = actLevel[actID].getEffect(i);
							effectTime = actLevel[actID].getEffectTime(i);
							factCost = actionCost + actLevel[actID].getExecCost();

							// If it's delay effect, then put in the event queue
							if (effectTime > 0) {
								secondEventQueue.add(new CostEvent(effectID, ce.time + effectTime, factCost, actID));
							}

							// Activate immediate effects
							if (effectTime == 0) {
								// If new fact
								if (!factLevel[effectID].isIn()) {
									factLevel[effectID].addCost(ce.time, factCost, actID);
								} else {
									oldFactCost = factLevel[effectID].getCost(-1);
									if (factCost < oldFactCost) {
										factLevel[effectID].addCost(ce.time, factCost, actID);
									}
								}
							}
						}
					}
				}
			}
		}

		// Now handle the events in the second event queue
		eventQueueSize = secondEventQueue.size();
		for (i = 0; i < eventQueueSize; i++) {
			ce = (CostEvent) secondEventQueue.get(i);
			predIndex = ce.factID;

			factLevel[predIndex].selectiveAddCost(ce.time, ce.cost, ce.actID);
		}
	}

	/**
	 * Function to assign a new cost for an action when we do "max" propagation when
	 * we reduce the cost of some precondition of that action. Return: T = we
	 * actually have a new maxActionCost[actID]; F = no new cost
	 */
	private boolean newMaxCost(int actID) {
		int i, j, n, factID;
		float factCost, maxCost = 0;

		n = actLevel[actID].numPrecond();
		for (i = 0; i < n; i++) {
			factID = actLevel[actID].getPrecond(i);

			if (!factLevel[factID].isIn())
				continue;
			factCost = factLevel[factID].getCost(-1);

			if (maxCost < factCost)
				maxCost = factCost;
		}

		if (maxActionCost[actID] > maxCost) {
			maxActionCost[actID] = maxCost;
			return true;
		}

		return false;
	}

	/**
	 * Main function to get the heuristic values according to the setup options
	 */
	public float getHeuristicValue() {
		if (relaxedPlanOption) {
			return relaxedPlanHeuristic();
		} else {
			return getDirectHeuristic();
		}
	}

	/**
	 * Function to get the relaxed plan in case we need to adjust it with resource
	 * information using the Utility class.
	 */
	public int[] getRelaxedPlan() {
		return relaxedPlan;
	}

	public int getRPSize() {
		return rpIndex;
	}

	/**
	 * Function to examine the relaxed solution to find the set of POSITIVE effects
	 * of the "first level" actions in the relaxed plan. Used for "helpful action"
	 * option.
	 */
	ArrayList haActs = new ArrayList(); // Store actions in the first level
	// we need haActs if we use "haneFlag" instead of "haFlag".
	// hapeEffects would store the positive effects of those action and is
	// enough for the "haFlag".

	public int[] collectHAPEffects() {
		int i, j, k, l, aID, factID, numPrecond, spFactIndex;

		if (haneFlag) {
			haActs.clear();
		}

		hapeIndex = 0;
		for (i = 0; i < rpIndex; i++) {
			aID = relaxedPlan[i];

			// Check if all of its preconditions are satisfied in currentState
			numPrecond = actLevel[aID].numPrecond();
			for (j = 0; j < numPrecond; j++) {
				factID = actLevel[aID].getPrecond(j);

				spFactIndex = spedFact.indexOf(new Integer(factID));

				if (spFactIndex >= initStateSize)
					break;

				if (spFactIndex < 0) {
					continue;
				}
			}

			/* If it's the action in the first level */
			if (j >= numPrecond) {
				// We can interleave the part to get the set of negative effects here also
				for (k = 0; k < actLevel[aID].numEffect; k++) {
					factID = actLevel[aID].getEffect(k);

					for (l = 0; l < hapeIndex; l++)
						if (factID == haPEffects[l])
							break;

					if (l >= hapeIndex)
						haPEffects[hapeIndex++] = factID;
				}

				// Get the set of helpful actions to prepare to get the set of negative effects.
				if (haneFlag) {
					haActs.add(new Integer(aID));
				}
			}
		}

		return haPEffects;
	}

	/** Number of positive effects in the first level */
	public int numHAPEffects() {
		return hapeIndex;
	}

	/** Set of actions in the first level */
	public ArrayList getHActs() {
		return haActs;
	}

	/*
	 * BM: Added July 23, 2003 to support pending "at end" condition - or
	 * "dynamic goals" (which is very rare). Check if a given predID is achievable
	 * at/before a given "time"
	 */
	public boolean achievable(int predID, float time) {
		return factLevel[predID].achievable(time);
	}

	/**
	 * Returns an array of action IDs of actions that are supproted in this state.
	 * 
	 * @param state
	 *            The state to check.
	 * @return An array of action IDs.
	 */
	public GAction[] applicableActions(StateInfo state, ArrayList gActions) {
		ArrayList actionList = new ArrayList();
		GPredDB initPreds = state.getCurrentPredDB();
		int numPreds = initPreds.numPred();
		Integer predIdx;

		// Reset the graph
		// Clears costs, events
		// resetBiLevelGraph();
		System.arraycopy(initialNumPredNotSat, 0, numPredNotSat, 0, initialNumPredNotSat.length);

		for (int i = 0; i < numPreds; i++) {
			predIdx = initPreds.getPred(i);
			FactLink fact = factLevel[predIdx.intValue()];
			int numSupportedActions = fact.numSpedAct();
			ActLink action;
			for (int j = 0; j < numSupportedActions; j++) {
				action = actLevel[fact.getSpedAct(j).intValue()];
				// are all the predicates supported for this action to execute?
				if (--numPredNotSat[action.actID] == 0) {
					GAction possibleAction = (GAction) gActions.get(action.actID);
					if (state.applicable(possibleAction)) {
						// the possible action is communicate_soil_data and current state contains
						// channel_free general
						actionList.add(possibleAction);
					}
				}
			}
		}
		return (GAction[]) actionList.toArray(new GAction[actionList.size()]);
	}
}

/**
 * This is a minimum-has-precedence priority queue for use during cost
 * propagation. It is implemented for efficiency with the following observations
 * in mind: 1. unless we are performing infinite look-ahead, insert will occur
 * more often than remove. In the case of infinite look-ahead, the number of
 * inserts will equal the number of removes. 2. Sapa is unable to determine the
 * nature of the event ordering for a particular domain (i.e. we do not know the
 * order of events inserts). 3. in most domains, for each state during the
 * course of our A* search, the number of events in the queue tends not to vary
 * wildly Many known priority queue algorithms have a trade-off between the
 * "remove" and "insert" methods (e.g. constant time on one gives N time on the
 * other). There are also known priority queue algorithms that give worst-case
 * of O(log n) on all operations. A heap priority queue provides this quality
 * and was chosen for this implementation.
 * 
 * @author J. Daniel Benton
 */
class EventQueue {
	private int size; // the size of this queue
	private int arraySize;
	private static final int DEFSIZE = 100;
	private static final int GROW_BY = 20;
	private CostEvent[] events;

	/**
	 * default constructor.
	 */
	public EventQueue() {
		events = new CostEvent[arraySize = DEFSIZE];
	}

	/**
	 * 
	 * @param size
	 *            Estimated size of the queue.
	 */
	public EventQueue(int size) {
		events = new CostEvent[arraySize = size];
	}

	/**
	 * A copy constructor. Used to get the ordered array. Not intended for other
	 * uses.
	 * 
	 * @param queue
	 */
	private EventQueue(EventQueue queue) {
		this.size = queue.size;
		events = new CostEvent[queue.events.length];
		arraySize = queue.arraySize;
		System.arraycopy(queue.events, 0, events, 0, queue.events.length);
	}

	/**
	 * Gives the size of this queue.
	 * 
	 * @returns The size of this queue.
	 * @see java.util.Collection#size()
	 */
	public int size() {
		return size;
	}

	/**
	 * We do not reinitialize or shrink the array because it has been observed that
	 * the queue does not change in size widely between states during the A* search.
	 */
	public void clear() {
		// clear events so garbage collector can do its duty
		for (int i = 0; i <= size; i++) {
			events[size] = null;
		}
		size = 0;
	}

	/**
	 * (Javadoc)
	 * 
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty() {
		if (size == 0)
			return true;
		return false;
	}

	/**
	 * Adds event into the queue at the appropriate spot.
	 * 
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(CostEvent ce) {
		if (arraySize - 1 == size) {
			growArray(size);
		}

		int i = ++size;
		// "swim" AKA percolate
		for (; i > 1 && ce.time < events[i / 2].time; i /= 2) {
			events[i] = events[i / 2];
		}
		events[i] = ce;

		return true;
	}

	/**
	 * Gives an ordered array from this priority queue. This is a slow method, and
	 * should not be used often.
	 * 
	 * @return An ordered array from this priority queue.
	 */
	public CostEvent[] toArray() {
		EventQueue queue = new EventQueue(this);
		int size = queue.events.length;
		CostEvent[] array = new CostEvent[size];
		for (int i = 0; i < size; i++) {
			array[i] = queue.remove();
		}
		return array;
	}

	/**
	 * 
	 * @param size
	 *            The current size of the arrays. Passed in for efficiency
	 *            (method-local parameters are almost always faster to access than
	 *            object-local variables)
	 */
	private void growArray(int size) {
		CostEvent[] newEvents;
		int[] newOrderArray;
		int arraySize;

		newEvents = new CostEvent[arraySize = size + GROW_BY + 1];

		System.arraycopy(events, 0, newEvents, 0, size + 1);
		events = newEvents;
		this.arraySize = arraySize;
	}

	/**
	 * Unimplemented for events. (Always returns false!)
	 * 
	 * @return True if this queue contains this element, false otherwise.
	 */
	public boolean contains(CostEvent ce) {
		return false;
	}

	/**
	 * Removes a CostEvent from the queue with the smallest time.
	 * 
	 * @return The removed CostEvent object.
	 */
	public CostEvent remove() {
		if (isEmpty())
			return null;

		CostEvent event = events[1];

		// "sink" AKA procolate down
		int i = 1, child = 0;
		// move last to beginning of heap array
		CostEvent down_event;
		try {
			down_event = events[i] = events[size--];
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(i + " and " + size + " and length of " + events.length);
			throw (ArrayIndexOutOfBoundsException) e;
		}
		for (; i * 2 <= size; i = child) {
			child = i * 2;
			if (child != size && events[child + 1].time < events[child].time) {
				child++;
			}
			if (events[child].time < down_event.time) {
				events[i] = events[child];
			} else {
				break;
			}
		}
		events[i] = down_event;

		return event;
	}

	/**
	 * Gets the current minimum-timed event.
	 */
	public CostEvent get() {
		return events[1];
	}

	/**
	 * Adds event in priority, where the priority is determined by the time point of
	 * the event.
	 * 
	 * @param ce
	 */
	public void addEvent(CostEvent ce) {
		add(ce);
	}

	public static void main(String[] args) {
		int numItems = 20;
		EventQueue h = new EventQueue(numItems);
		int i = 11;

		for (i = 11; i != 0; i = (i + 11) % numItems) {
			h.add(new CostEvent(0, (float) i, (float) i, 0));
		}

		CostEvent event;
		for (i = 1; i < numItems; i++) {
			if (((event = h.remove())).time != (float) i) {
				System.out.println("Oops! " + i + " and " + event.time);
			}
		}

		for (i = 11; i != 0; i = (i + 11) % numItems) {
			h.add(new CostEvent(0, (float) i, (float) i, 0));
		}

		// h.add( new CostEvent(0,(float)1.0,(float)1.0,0) );
		// i = 9999999;
		// h.add( new CostEvent(0,(float)i,(float)i,0) );

		for (i = 1; i <= numItems; i++) {
			event = h.remove();
			if (event != null) {
				if (event.time != (float) i) {
					System.out.println("Oops! " + i + " " + event.time);
				} // else System.out.println("ok");
			} else {
				System.out.println("event is null! i = " + i);
			}
		}
	}
}
