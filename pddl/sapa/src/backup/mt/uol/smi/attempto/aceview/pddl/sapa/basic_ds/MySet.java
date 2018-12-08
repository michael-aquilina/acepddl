/********************************************************************
   Author: Minh B. Do (Arizona State University - binhminh@asu.edu)
*********************************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds;

/**
 * MySet: Represent the "Set" template from the domain file. Used to ground and
 * store action's effects related to continuous function in GMySet. Refer to
 * GMySet for detail descriptions on components of this class.
 */
public class MySet {
	Function leftSide;
	String assign;
	MathForm rightSide;

	public MySet() {
		leftSide = new Function();
		assign = new String();
		rightSide = new MathForm();
	}

	public MySet(Function left, String ass, MathForm right) {
		leftSide = left;
		assign = ass;
		rightSide = right;
	}

	public void setLeftSide(Function l) {
		leftSide = l;
	}

	public Function getLeftSide() {
		return leftSide;
	}

	public void setAssign(String s) {
		assign = s;
	}

	public String getAssign() {
		return assign;
	}

	public void setRightSide(MathForm m) {
		rightSide = m;
	}

	public MathForm getRightSide() {
		return rightSide;
	}

	public String toString() {
		String s = "(" + assign + " ";

		s += leftSide.toString() + " " + rightSide.toString() + ")";

		return s;
	}
}
