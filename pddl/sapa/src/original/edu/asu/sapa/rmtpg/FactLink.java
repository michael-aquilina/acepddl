/**********************************************************************
    Author: Minh B. Do - Arizona State University
***********************************************************************/

package edu.asu.sapa.rmtpg;

import java.util.*;

/**
 * FactLink: Class to represent a fact, and links with actions which link to
 * that fact (has a fact as its precond or effects)
 */
public class FactLink {
	int MAXCOSTCHANGE = 50; // constant to indicate how many time cost
	// function can change

	int factID; // Id of this fact

	// Later try more sophisticated way to use array[] instead of ArrayList
	ArrayList spedActs; // List of ActIDs that this fact supports
	ArrayList spActs; // List of ActIDs that support this fact.

	float[] time_point; // time point of the cost function
	float[] cost; // cost of this fact
	int[] cost_action; // Action that support a given cost
	int index; // Index of how many values in the cost table.

	public FactLink(int id) {
		factID = id;

		spedActs = new ArrayList();
		spActs = new ArrayList();

		time_point = new float[MAXCOSTCHANGE];
		cost = new float[MAXCOSTCHANGE];
		cost_action = new int[MAXCOSTCHANGE];
		index = 0;
	}

	/* For supported action (fact is precond of) */
	// act instanceof Integer representing the action ID
	public void addSpedAct(Object act) {
		spedActs.add(act);
	}

	// size of actions that this fact supports
	public int numSpedAct() {
		return spedActs.size();
	}

	public Integer getSpedAct(int index) {
		// IMPROVE Remove this cast.
		return (Integer) spedActs.get(index);
	}

	/* For actions support that fact (fact is effect of) */
	public void addSpAct(Object act) {
		spActs.add(act);
	}

	public int numSpAct() {
		return spActs.size();
	}

	public Integer getSpAct(int index) {
		return (Integer) spActs.get(index);
	}

	public void deactivate() {
		index = 0;
	}

	public boolean isIn() {
		if (index > 0)
			return true;
		else
			return false;
	}

	/* Manage the cost & supporting action */
	public void addCost(float time, float c, int ca) {
		// If we add the first time
		if (index < 1) {
			time_point[index] = time;
			cost[index] = c;
			cost_action[index] = ca;
			index++;
			return;
		}

		// If we do not add the first time
		if (time > time_point[index - 1]) {
			if (c < cost[index - 1]) {
				time_point[index] = time;
				cost[index] = c;
				cost_action[index] = ca;
				index++;
			}
		} else if (time == time_point[index - 1]) {
			if (c < cost[index - 1]) {
				cost[index - 1] = c;
				cost_action[index - 1] = ca;
			}
		} else {
			System.out.println("Add new cost at wrong time in FactLink.java");
			System.exit(1);
		}

		// Double check if we reach the limit of the cost function
		if (index >= MAXCOSTCHANGE) {
			System.out.println("Reach the limit for storing in cost function.");
			System.exit(1);
		}
	}

	/*
	 * Function to selectively add random cost at time point t and cost c but not at
	 * the time point that is higher than all others considered
	 */
	public void selectiveAddCost(float time, float c, int ca) {
		int i, j, k;

		// Find the right time point
		for (i = 0; i < index; i++)
			if (time < time_point[i])
				break;

		if (i > 0) {
			if (c > cost[i - 1])
				return;
		}

		if (i == index) {
			time_point[index] = time;
			cost[index] = c;
			cost_action[index] = ca;
			index++;
			return;
		}

		// Adjust the cost function
		for (j = i; j < index; j++)
			if (cost[j] < c)
				break;

		if (j == index) {
			time_point[i] = time;
			cost[i] = c;
			cost_action[i] = ca;
			index = i + 1;
			return;
		}

		if (j == i) {
			for (k = index; k > i; k--) {
				time_point[k] = time_point[k - 1];
				cost[k] = cost[k - 1];
				cost_action[k] = cost_action[k - 1];
			}

			time_point[i] = time;
			cost[i] = c;
			cost_action[i] = ca;
			index++;
		} else {
			time_point[i] = time;
			cost[i] = c;
			cost_action[i] = ca;

			for (k = j; k < index; k++) {
				time_point[++i] = time_point[k];
				cost[i] = cost[k];
				cost_action[i] = cost_action[i];
			}

			index = i + 1;
		}
	}

	public float getCost(float time) {
		// If there is nothing in the cost vector or
		// *time* is smaller than the first update
		if (index < 1) {
			System.out.println("Error in FactLink.getCost(): cost = infinite -- factID = " + factID);
			System.exit(1);
		}

		// If we just want a best cost
		if (time < 0)
			return cost[index - 1];

		// If giving the wrong time point
		if (time < time_point[0]) {
			System.out.println("Error in FactLink.getCost(): 0 < time = " + time + " < time_point[0] = " + time_point[0]
					+ " -- factID = " + factID);
			System.exit(1);
		}

		// Normal cases
		for (int i = 1; i < index; i++)
			if (time_point[i] > time)
				return cost[i - 1];

		return cost[index - 1];
	}

	public int getCostAction(float time) {
		// If there is nothing in the cost vector or
		// *time* is smaller than the first update
		if (index < 1) {
			System.out.println("Error in FactLink.getCostAction() - cost = infinite. FactID = " + factID);
			System.exit(1);
		}

		// If just want action that gives the best cost
		if (time < 0)
			return cost_action[index - 1];

		if (time < time_point[0]) {
			System.out.println("Error in FactLink.getCostAction(): 0 < time < time_point[0] " + " time = " + time
					+ " time_point[0] = " + time_point[0] + " cost[0] = " + cost[0] + " -- FactID = " + factID);
			System.exit(1);
		}

		// Normal cases
		for (int i = 1; i < index; i++)
			if (time_point[i] > time)
				return cost_action[i - 1];

		return cost_action[index - 1];
	}

	/**
	 * BM: Added July 23, 2003 to support "at end" condition This function check if
	 * this fact is achievable at a given time
	 */
	public boolean achievable(float time) {
		if (index < 1)
			return false;

		if (time_point[0] > time)
			return false;

		return true;
	}
}
