/*******************************************************************
    Author: Minh B. Do (Arizona State Univ. - binhminh@asu.edu)
********************************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds;

import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.*;

/**
 * This class can be used for the tree structure that encode the math formula
 * used in Test/Set/Dynamic Duration/Cost. the basic math formula can be a float
 * value, or a function value. The complex structure contain one operator (e.g.
 * +,-,*,/) and the "left" and "right" elements, which each in turn is a
 * GMathForm. Similar to MathForm but specialized for Grounded Functions
 */
public class GMathForm {
	int type; // 0: function, 1: value, 2: time (#t), 3: Non Primititive
	float value; // If type=1.
	int element; // If type=0. This one is the GroundFunction ID

	char operator; // +, -, *, /

	GMathForm left;
	GMathForm right;

	public GMathForm() {
		type = 0;
	}

	public GMathForm(GMathForm g) {
		type = g.getType();
		value = g.getValue();
		element = g.getElement();
		operator = g.getOperator();

		if (type == 3) {
			// left = new GMathForm(g.getLeft());
			// right = new GMathForm(g.getRight());
			left = g.getLeft();
			right = g.getRight();
		}
	}

	/**
	 * Set the "Type" of this math formula. 0: function, 1: float value, 2: #t or
	 * intermediate time point (not used for level 3, PDDL21), 3: Non primitive or
	 * complex math formula
	 */
	public void setType(int b) {
		type = b;
	}

	public int getType() {
		return type;
	}

	/** Set the static value of this GMathForm if the type is type = 1 */
	public void setValue(float f) {
		value = f;
	}

	public float getValue() {
		return value;
	}

	/**
	 * Set the "element" of this GMathForm to the Function's ID if the type of this
	 * GMathForm is 0
	 */
	public void setElement(int e) {
		element = e;
	}

	public int getElement() {
		return element;
	}

	/** Set the operator (+,-,*,/) if this GMathForm is non-primitive */
	public void setOperator(char c) {
		operator = c;
	}

	public char getOperator() {
		return operator;
	}

	public void setLeft(GMathForm m) {
		// left = new GMathForm(m);
		left = m;
	}

	// IMPROVE If this is a value, return the value instead of the whole GMathform
	// object
	// IMPROVE first check if we even need to make a copy or not.
	public GMathForm getLeft() {
		// return new GMathForm(left);
		return left;
	}

	// IMPROVE same as above
	public void setRight(GMathForm m) {
		// right = new GMathForm(m);
		right = m;
	}

	// IMPROVE same as above
	public GMathForm getRight() {
		// return new GMathForm(right);
		return right;
	}

	/**
	 * Function to evaluate and return the value of this math formula given the
	 * current values of all continuous functions specified in the passed in GMResDB
	 */
	public float value(GMResDB mr, float time) { // #t = time. Currently: time = act's dur if called from GMResDB
		// If it's grounded function, need to look at DB
		if (type == 0) {
			// Need to look up DB for latest updated value
			return mr.getValue(element);
		}

		// Just a normal Primitive value
		if (type == 1) {
			return value;
		}

		// If it's time (spended from the start-point of an action)
		if (type == 2) {
			// Need to look at time-control module later
			return time;
		}

		switch (operator) {
		case '+':
			return left.value(mr, time) + right.value(mr, time);
		case '-':
			return left.value(mr, time) - right.value(mr, time);
		case '*':
			return left.value(mr, time) * right.value(mr, time);
		case '/':
			return left.value(mr, time) / right.value(mr, time);
		default:
			return 0;
		}
	}

	/**
	 * Function to evaluate and return the value of this math formula given the
	 * current values... used to evaluate static/constant functions during the
	 * grounding process. This is intended to be called only while grounding. When
	 * this method is called, we are assuming that no states but the intial state
	 * exist.
	 * 
	 * @param constants
	 *            An array of booleans, each element specified by function ID. False
	 *            if constant, true if not.
	 * @author J. Benton (adapted from above value() method by Minh)
	 */
	public float valueCheckStatics(GMResDB mr, boolean[] constants) throws CannotEvaluateException {
		// true if this formula evaluates to a constant value
		boolean iAmConstant = true;
		// If it's grounded function, need to check if its a constant
		if (type == 0) {
			// if this grounded formula evaluates to a function
			// that remains constant throughout the problem solving procedure...
			if (!constants[element]) { // true = not constant, false = constant
				// Need to look up DB for the initial value
				value = mr.getValue(element);
				type = 1; // 1 = value (not formula, so this is "constant")
				return value;
			} else {
				throw new CannotEvaluateException();
			}
		}

		// Just a normal Primitive value
		if (type == 1) {
			return value;
		}

		// Note to code-readers: This is sort of a poor way
		// to use exceptions and needs to be refactored.
		// Idea is: If the exception is thrown, the formula called is not constant.
		// In this way, everything that can be evaluated to a constant will be...
		// (so we can then remove all static/constant functions from the GMResDB)
		CannotEvaluateException ex = null;
		float leftValue;
		float rightValue;
		try {
			leftValue = left.valueCheckStatics(mr, constants);
		} catch (CannotEvaluateException e) {
			ex = e;
			iAmConstant = false;
			leftValue = (float) 0.0;
		}
		try {
			rightValue = right.valueCheckStatics(mr, constants);
		} catch (CannotEvaluateException e) {
			ex = e;
			iAmConstant = false;
			rightValue = (float) 0.0;
		}

		if (iAmConstant) {
			switch (operator) {
			case '+':
				value = leftValue + rightValue;
				break;
			case '-':
				value = leftValue - rightValue;
				break;
			case '*':
				value = leftValue * rightValue;
				break;
			case '/':
				value = leftValue / rightValue;
				break;
			default:
				return 0;
			}
			left = null; // for the garbage collector to pick up
			right = null; // this is garbage too
			type = 1; // 1 = value
			return value;
		} else {
			throw ex;
		}
	}

	/** Function to print this GMathForm. Mostly for debugging */
	public String toString() {
		if (type == 0)
			return ("funcID(" + element + ")");

		if (type == 1)
			return (new Float(value)).toString();

		if (type == 2)
			return "#t";

		return "(" + operator + " " + left.toString() + " " + right.toString() + ")";
	}
}
