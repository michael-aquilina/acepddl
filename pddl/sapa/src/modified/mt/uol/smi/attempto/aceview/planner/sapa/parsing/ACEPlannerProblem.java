/********************************************************************
   Author: Minh B. Do (Arizona State University - binhminh@asu.edu)
*********************************************************************/
package mt.uol.smi.attempto.aceview.planner.sapa.parsing;

import mt.uol.smi.attempto.aceview.planner.sapa.complex_ds.ACEPlannerPredDB;

/**
 * This class represent the problem structure that we parsed from the problem
 * file in PDDL2.1
 */
public class ACEPlannerProblem {
	String name; // Name of the problem
	String domain; // Name of the domain used for this problem

	ACEPlannerPredDB init_state;
	ACEPlannerPredDB goal_state;

	// Flag to state if there are deadline goal constraints for this problem
	boolean dlConst = false;

	public ACEPlannerProblem() {
		name = new String();
		goal_state = new ACEPlannerPredDB();
		init_state = new ACEPlannerPredDB();
	}

	/** Set the name of this problem */
	public void setName(String n) {
		name = n;
	}

	public String getName() {
		return name;
	}

	/** Domain name used for this problem */
	public void setDomain(String n) {
		domain = n;
	}

	public String getDomain() {
		return domain;
	}

	public ACEPlannerPredDB getGoalState() {
		return goal_state;
	}

	public void setGoalState(ACEPlannerPredDB goal_state) {
		this.goal_state = goal_state;
	}

	public ACEPlannerPredDB getInitState() {
		return init_state;
	}

	public void setInitState(ACEPlannerPredDB init_state) {
		this.init_state = init_state;
	}

	
	
}
