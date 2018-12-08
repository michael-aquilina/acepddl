/*********************************************************
         Author: Minh B. Do - Arizona State University.
*********************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.lp_interface;

import java.util.*;

/**
 * Constraint to guarantee that the resource-related precond of actions are
 * satisfied: Init_r + \Sigma U^r_{A_i} (A_i < A_j) > K Note: One instance of
 * this class may represent a set of LP constraints.
 */
public class ResPreCons {
	// Define some constant
	final static int EQUAL = 0;
	final static int SMALLER = 1;
	final static int S_EQUAL = 2; // Smaller or Equal
	final static int LARGER = 3;
	final static int L_EQUAL = 4; // Larger or Equal

	int resID; // ID of the (lhs) resource involved with this ResPreCons instance
	float initLevel = 0;

	public ArrayList changeRes = new ArrayList(); // actions that change value of this res
	public ArrayList checkRes = new ArrayList(); // actions that check (precond) on res's value
	public ArrayList changeAmount = new ArrayList(); // change amount can be < 0.
	public ArrayList checkAmount = new ArrayList();
	public ArrayList checkType = new ArrayList(); // >; <; >=; <=; =

	public ResPreCons() {
	}

	public ResPreCons(int rID) {
		resID = rID;
	}

	public void setID(int id) {
		resID = id;
	}

	public int getID() {
		return resID;
	}

	public void setInitLevel(float init) {
		initLevel = init;
	}

	public float getInitLevel() {
		return initLevel;
	}

	public int numChangeAct() {
		return changeRes.size();
	}

	public void addChangeAct(int aID, float cAmount) {
		changeRes.add(new Integer(aID));
		changeAmount.add(new Float(cAmount));
	}

	public int getChangeActID(int index) {
		return ((Integer) changeRes.get(index)).intValue();
	}

	public float getChangeAmount(int index) {
		return ((Float) changeAmount.get(index)).floatValue();
	}

	public void addCheckAct(int aID, float cAmount, int cType) {
		if (checkRes.contains(new Integer(aID)))
			return;

		checkRes.add(new Integer(aID));
		checkAmount.add(new Float(cAmount));
		checkType.add(new Integer(cType));
	}

	public int numCheckAct() {
		return checkRes.size();
	}

	public int getCheckAct(int index) {
		return ((Integer) checkRes.get(index)).intValue();
	}

	public float getCheckAmount(int index) {
		return ((Float) checkAmount.get(index)).floatValue();
	}

	public int getCheckType(int index) {
		return ((Integer) checkType.get(index)).intValue();
	}
}
