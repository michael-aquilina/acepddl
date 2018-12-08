/**********************************************************************
    Author: Minh B. Do - Arizona State University
***********************************************************************/

package mt.uol.smi.attempto.aceview.pddl.sapa.rmtpg;

import mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds.GMathForm;

/**
 * ActLink: Class to represent an Action, and links with facts which are its
 * preconds or effects. This class is similar to FactLink class and is much
 * simpler than GAction class. It is used in the relaxed bi-level planning graph
 */
public class ActLink {
	int MAXCOSTCHANGE = 50; // constant to indicate how many time cost
	// function can change
	int actID; // Id of this fact

	// if this action is dynamic,
	// we save the mathform so that it can be used to discover
	// whether we can apply it at a particular time point.
	GMathForm gMathForm;

	int[] preconds; // List of factIDs for the preconditions of this action

	// joint arrays
	int[] effects; // List of factIDs for the effects of this action
	float[] eTimes; // List of the time each effect occurs.

	public int numPrec, numEffect;

	// joint arrays
	float[] time_point; // time point at which this action can be executed
	// cost to achieve preconditions of the actions
	float[] cost; // cost at time point (from above array)
	int index; // size of array

	float dur; // duration of this action
	// given in the domain description (static) -- not in PDDL 2.1
	// for actions in PDDL 2.1 cost = 1.
	float exec_cost; // execution cost of this action

	/**
	 * Constructor using action ID, duration, number of preconditions, number of
	 * effects and the execution cost
	 * 
	 * @param id
	 *            ID for this action
	 * @param d
	 *            duration of this action
	 * @param np
	 *            number of preconditions for this action
	 * @param ne
	 *            number of effects for this action
	 * @param e_cost
	 *            exec_cost for this action
	 */
	public ActLink(int id, float d, int np, int ne, float e_cost) {
		actID = id;
		dur = d;

		numPrec = np;
		numEffect = ne;

		preconds = new int[numPrec];
		effects = new int[numEffect];
		eTimes = new float[numEffect];

		time_point = new float[MAXCOSTCHANGE];
		cost = new float[MAXCOSTCHANGE];
		index = 0;
		exec_cost = e_cost;
	}

	/**
	 * Constructor using action ID, duration, number of preconditions, number of
	 * effects and the execution cost
	 * 
	 * @param id
	 *            ID for this action
	 * @param mathform
	 *            If this action is dynamic, this contains a mathform. (implies d =
	 *            0)
	 * @param np
	 *            number of preconditions for this action
	 * @param ne
	 *            number of effects for this action
	 * @param e_cost
	 *            exec_cost for this action
	 */
	public ActLink(int id, GMathForm mathform, int np, int ne, float e_cost) {
		actID = id;
		dur = 0;

		gMathForm = mathform;
		numPrec = np;
		numEffect = ne;

		preconds = new int[numPrec];
		effects = new int[numEffect];
		eTimes = new float[numEffect];

		time_point = new float[MAXCOSTCHANGE];
		cost = new float[MAXCOSTCHANGE];
		index = 0;
		exec_cost = e_cost;

		// TODO Calculate possible durations for this action from the gMathForm obj
		// IMPROVE Collect actions to use in this location.

		// We must have a collection of all actions that modify resources
		// on which the duration is dependent.

		// If there are no actions that modify resources for which this action's
		// duration depends upon, then do not perform the calculation and instead
		// make this action's duration equal to whatever the mathform calculation for
		// the initial values of the resources are.

		// We must "know when to stop" by giving a threshold on the number of
		// applications that a particular action has -- in particular, those
		// actions whose application consistently increases or decreases a
		// resource. (i.e. all actions, because all have "increase" or "decrease"
		// when dealing with resources in PDDL 2.2)
		// We can do as above or assume a minimum value of 0 for all
		// resources. (maybe unnecessary)

	}

	/** Add a fact (predicate) which is precondition of this action */
	public void addPrecond(int fact, int index) {
		preconds[index] = fact;
	}

	public int numPrecond() {
		return numPrec;
	}

	public int getPrecond(int index) {
		return preconds[index];
	}

	/**
	 * Add an (positive) effect and the time at which it occurs. time = 0 -> "st",
	 * time = 1 -> "et"
	 */
	public void addEffect(int fact, int time, int index) {
		effects[index] = fact;
		if (time == 0)
			eTimes[index] = 0;
		else
			eTimes[index] = dur;
	}

	/*
	 * public int numEffect() { return numEffect; }
	 */

	public int getEffect(int index) {
		return effects[index];
	}

	/** Get an effect time according to the index of a given effect */
	public float getEffectTime(int index) {
		return eTimes[index];
	}

	/** Get an effect time according to an effect's predicate ID */
	public float getETime(int effectID) {
		for (int i = 0; i < numEffect; i++)
			if (effects[i] == effectID)
				return eTimes[i];

		// sanity check
		System.out.println("Error in ActLink.getETime(): fact " + effectID + " is not an effect of action " + actID);
		System.exit(1);
		return 0;
	}

	/** Get the duration of action */
	public float getDur() {
		return dur;
	}

	/**
	 * To Activate/Deactivate and action. Indicate whether or not it's in the
	 * relaxed planning graph or not Deactivate when "resetting" relaxed planning
	 * graph
	 */
	public void deactivate() {
		index = 0;
	}

	/**
	 * True if this is in the planning graph.
	 * 
	 * @return
	 */
	public boolean isIn() {
		// index == 0 then can't achieve yet
		if (index > 0)
			return true;
		else
			return false;
	}

	/**
	 * Update the cost function. Specify that this action can be executed with cost
	 * "c" at from time "t"
	 */
	public void addCost(float t, float c) {
		// If we add cost the first time
		if (index < 1) {
			time_point[index] = t;
			cost[index] = c;
			index++;
			return;
		}

		// If it's not the first time we add the cost
		if (t > time_point[index - 1]) {
			// assures that cost function is always decreasing
			if (c < cost[index - 1]) {
				time_point[index] = t;
				cost[index] = c;
				index++;
			}
		} else if (t == time_point[index - 1]) {
			if (c < cost[index - 1])
				cost[index - 1] = c;
		} else {
			System.out.println("Add cost at wrong time in ActLink.java");
			System.exit(1);
		}

		// Double check if we reach the limit of the cost function
		if (index >= MAXCOSTCHANGE) {
			System.out.println("Reach the limit for storing in cost function.");
			System.exit(1);
		}
	}

	/** Get the cost to execute an action at a specific time point */
	public float getCost(float time) {
		// sanity check
		// If there is nothing in the cost vector or
		// *time* is smaller than the first update
		if ((index < 1) || (time < time_point[0])) {
			System.out.println("Error in ActLink.getCost(): time = " + time);
			System.exit(1);
		}

		// Normal cases
		// infinity = -1
		// always the case for no deadlines
		if (time < 0) // Just want a best cost
			return cost[index - 1];
		// if deadlines are in the domain
		for (int i = 1; i < index; i++)
			if (time_point[i] > time)
				return cost[i - 1];

		// if no deadline
		return cost[index - 1];
	}

	/**
	 * Get the minimal time t value to execute action that the cost is still the
	 * same with when it execute at t_max > t
	 */
	public float getMinTime(float tmax) {
		if ((index < 1) || (tmax < time_point[0])) {
			if (index < 1)
				System.out.println(
						"Error in ActLink.getMinTime(): cost = infty. tmax = " + tmax + " -- ActID = " + actID);
			else
				System.out.println("Error in ActLink.getMinTime(): tmax < time_point[0]. " + "tmax = " + tmax
						+ " time_point[0] = " + time_point[0] + " -- ActID = " + actID);
			System.exit(1);
		}

		for (int i = 1; i < index; i++)
			if (time_point[i] > tmax)
				return time_point[i - 1];

		return time_point[index - 1];
	}

	/** Get the execution cost of this action */
	public float getExecCost() {
		return exec_cost;
	}
}
