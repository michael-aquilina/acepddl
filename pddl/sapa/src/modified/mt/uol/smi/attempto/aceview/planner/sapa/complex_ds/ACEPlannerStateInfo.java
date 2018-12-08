/****************************************************************************
    Author: Minh B. Do (Arizona State Univ. - binhminh@asu.edu)
*****************************************************************************/
package mt.uol.smi.attempto.aceview.planner.sapa.complex_ds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLDataFactory;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSatisfiedActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSnippet;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSnippetComparator;
import mt.uol.smi.attempto.aceview.planner.model.ACESnippetComparator;

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
public class ACEPlannerStateInfo {
	private static final Logger logger = Logger.getLogger(ACEPlannerStateInfo.class);
	private ACEPlannerGState currentState;
	private float csTime; // Time of the *cutset* of the current state

	// public for speedup
	public float distance; // Distance future-state --> goal-state (based on some heu)

	public ArrayList<ACEPlannerSatisfiedActionModel> actions; // The list of ground action ID leading to this state
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

	public ACEPlannerStateInfo(OWLModelManager modelManager, OWLDataFactory dataFactory, TokenMapper aceLexicon) {
		currentState = new ACEPlannerGState(modelManager, dataFactory, aceLexicon);
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

	public ACEPlannerStateInfo(ACEPlannerStateInfo sp) {
		currentState = new ACEPlannerGState(sp.currentState);
		csTime = sp.getCSTime();
		distance = sp.getDistance();

		actions = new ArrayList<ACEPlannerSatisfiedActionModel>(sp.getActions());
		atime = new ArrayList(sp.getTime());
		adur = new ArrayList(sp.getADur());

		totalDuration = sp.totalDuration();
		totalExecCost = sp.totalExecCost();
	}

	/**
	 * Set the "State", which contains the predicates that are true and the values
	 * of all functions at the current time point
	 */
	public void setCurrentState(ACEPlannerGState s) {
		currentState = new ACEPlannerGState(s);
	}

	/**
	 * 
	 * @return
	 */
	public ACEPlannerGState getCurrentState() {
		// TODO Why is this the thing creating a new instance of the current state?
		// Maybe the caller should do that.
		// return new GState(currentState);
		return currentState;
	}

	public List<ACEPlannerSnippet> getSortedSnippets() {
		List<ACEPlannerSnippet> snippets = currentState.getAcetext().getSnippets();
		Collections.sort(snippets, new ACEPlannerSnippetComparator());
		return snippets;
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

	public List<ACEPlannerSatisfiedActionModel> getActions() {
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

	/**
	 * Check if an action is applicable in a current state (assumes predicate
	 * preconditions have already been checked).
	 * 
	 * @param a
	 *            Action to be checked for applicability.
	 */
	public List<ACEPlannerSatisfiedActionModel> applicable(ACEPlannerActionModel a) {

		return currentState.applicable(a);
	}

	/**
	 * Applying actions to a StateInfo. The action's effect will change the values
	 * of the set of current predicates that are true, update the values of
	 * functions and introduce new events, protected predicates and functions
	 */
	public void update(ACEPlannerSatisfiedActionModel a) {

		this.logger
				.trace("!+!+!+!+!+!+!+Updating state with action " + a.getAction().getActionName() + " +!+!+!+!+!+!+!");

		// Update the current state for *instant* effects ("st")
		currentState.update(a);
		this.logger.trace("!+!+!+!+!+!+!+Updating state finished " + a.getAction().getActionName() + " +!+!+!+!+!+!+!");
		/*** Add action A to the list ***/
		actions.add(new ACEPlannerSatisfiedActionModel(a));
	}
}
