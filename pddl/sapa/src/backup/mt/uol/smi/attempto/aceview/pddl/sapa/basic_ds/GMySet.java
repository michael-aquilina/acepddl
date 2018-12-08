/*
   Author: Minh B. Do (Arizona State University)
 */
package mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds;

import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.GMResDB;

/**
 * GMySet: Class to store the grounded "Set" structure, which is an action
 * effect related to changing the value of a continuous function. The "Set"
 * contains of 3 parts, the function in the left hand side, the assigment
 * indication (increase/decrease etc.) and the GMathForm structure as the right
 * hand side
 * 
 * @author Minh B. Do
 * @author J. Daniel Benton
 */
public class GMySet {
	int leftSide; // Ground func ID of left side
	int assign; // "=" : 0; "-=" : 1; "+=" : 2; "*=" : 3; "/=" : 4;
	GMathForm rightSide;
	private boolean constant = false;

	public GMySet() {
		leftSide = 0;
		assign = 0;
		rightSide = new GMathForm();
	}

	public GMySet(int left, int assg, GMathForm right) {
		leftSide = left;
		assign = assg;
		// rightSide = new GMathForm(right);
		rightSide = right;
	}

	/**
	 * Called to make this set's rhs constant/static if all of its functions are.
	 * 
	 * @param mr
	 *            The initial state's GMResDB.
	 * @author J. Benton
	 */
	public void setConstant(GMResDB mr, boolean[] constants) {
		try {
			rightSide.valueCheckStatics(mr, constants);
			constant = true;
		} catch (Exception e) {
			constant = false;
			// ignore the exception because it just means that this "set"
			// has no constant values in it.
		}
	}

	/** Set the "head" of the assigment to a specific ground function's ID */
	public void setLeftSide(int l) {
		leftSide = l;
	}

	public int getLeftSide() {
		return leftSide;
	}

	/** Set the assignment (0: "="; "-=" : 1; "+=" : 2; "*=" : 3; "/=" : 4;) */
	public void setAssign(int assg) {
		assign = assg;
	}

	public int getAssign() {
		return assign;
	}

	/**
	 * Set the "tail" (right side) of this assignment formula to a specific
	 * GMathForm
	 */
	public void setRightSide(GMathForm m) {
		// rightSide = new GMathForm(m);
		rightSide = m;
	}

	// IMPROVE Allow right side to be all on calls to it... ?? maybe
	public GMathForm getRightSide() {
		return rightSide;
		// return new GMathForm(rightSide); // maybe we'll need to uncomment this, but I
		// don't think so
	}

	public String toString() {
		String s = "(" + assign + " ";

		s += leftSide + " " + rightSide.toString() + ")";

		return s;
	}
}
