/****************************************************************
    Author: Minh B. Do - Arizona State University
*****************************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds;

import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.GMResDB;

/**
 * GTest: Class to store the grounded Test structure which is used to check the
 * metric resource (pre)condition represented by a comparison on some continous
 * function. Like "Set", "Test" consists of 3 component, a ground function on
 * the left side, a comparator (e.g <, >, =) and the GMathForm represent a value
 * on the right side.
 */
public class GTest {
	int leftSide; // Ground Function ID of the left side
	public boolean lhsIsStatic;
	public float leftValue; // if left hand side is a static/constant
	// "==" : 0; "<" : 1; "<=" : 2; ">" : 3; ">=" : 4;
	int comparator;
	GMathForm rightSide;
	boolean constant;

	public GTest() {
		leftSide = 0;
		comparator = 0;
		rightSide = new GMathForm();
	}

	public GTest(int left, int c, GMathForm right) {
		leftSide = left;
		comparator = c;
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
			// ignore the exception because it just means that this "test"
			// has no constant values in it (on the right and side, at least).
		}

		if (!constants[leftSide]) { // if the left hand side is static
			leftValue = mr.getValue(leftSide);
			lhsIsStatic = true;
		}
	}

	/** Set the left side, which is an Integer represent a function's ID */
	public void setLeftSide(int l) {
		leftSide = l;
	}

	public int getLeftSide() {
		return leftSide;
	}

	/** Set the Comparator: "==" : 0; "<" : 1; "<=" : 2; ">" : 3; ">=" : 4; */
	public void setComparator(int i) {
		comparator = i;
	}

	public int getComparator() {
		return comparator;
	}

	/** Set the right hand side, which is a GMathForm represent one value */
	public void setRightSide(GMathForm m) {
		// rightSide = new GMathForm(m);
		rightSide = m;
	}

	public GMathForm getRightSide() {
		return rightSide;
	}

	/** Function to do short printing */
	public String toString() {
		String s = "(";

		s += comparator + " " + leftSide + " " + rightSide.toString() + ")";

		return s;
	}
}