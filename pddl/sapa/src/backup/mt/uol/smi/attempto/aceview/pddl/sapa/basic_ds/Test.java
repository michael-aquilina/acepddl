/****************************************************
   Author: Minh B. Do (Arizona State University)
*****************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds;

/**
 * Test: The template for "Test", a (pre)condition involving a comparison
 * related to a continuous function. Consult description for class GTest for
 * explanation on components.
 */
public class Test {
	Function leftSide;
	String comparator;
	MathForm rightSide;

	public Test() {
		leftSide = new Function();
		comparator = new String();
		rightSide = new MathForm();
	}

	public Test(Function left, String c, MathForm right) {
		leftSide = left;
		comparator = c;
		rightSide = right;
	}

	public void setLeftSide(Function l) {
		leftSide = l;
	}

	public Function getLeftSide() {
		return leftSide;
	}

	public void setComparator(String s) {
		comparator = s;
	}

	public String getComparator() {
		return comparator;
	}

	public void setRightSide(MathForm m) {
		rightSide = m;
	}

	public MathForm getRightSide() {
		return rightSide;
	}

	public String toString() {
		String s = "(";

		s += comparator + " " + leftSide.toString() + " " + rightSide.toString() + ")";

		return s;
	}
}
