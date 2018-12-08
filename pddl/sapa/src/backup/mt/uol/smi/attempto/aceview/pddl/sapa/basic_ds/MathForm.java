/*************************************************
    Author: Minh B. Do (ASU - binhminh@asu.edu)
**************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds;

/**
 * MathForm: Represent the abstract template class parsed from the domain file.
 * Used to make ground math form (stored in GMathForm). Structurally similar to
 * GMathForm. Refer to that class for member functions.
 */
public class MathForm {
	int type; // 0: function, 1: float value, 2: time (#t), 3: Non Primititive,
	// 4: Constants (Note: Constant will be changed to *float value* after
	// grounding)
	// 5: = ?duration (this mathform is equal to the (MathForm) representing action
	// duration
	float value; // If type=1.
	Function element; // If type=0.

	char operator; // +, -, *, /

	MathForm left;
	MathForm right;

	public MathForm() {
		type = 0;
	}

	public void setType(int b) {
		type = b;
	}

	public int getType() {
		return type;
	}

	public void setValue(float f) {
		value = f;
	}

	public float getValue() {
		return value;
	}

	public void setElement(Function e) {
		element = e;
	}

	public Function getElement() {
		return element;
	}

	public void setOperator(char c) {
		operator = c;
	}

	public char getOperator() {
		return operator;
	}

	public void setLeft(MathForm m) {
		left = m;
	}

	public MathForm getLeft() {
		return left;
	}

	public void setRight(MathForm m) {
		right = m;
	}

	public MathForm getRight() {
		return right;
	}

	public String toString() {
		if (type == 0)
			return element.toString();

		if (type == 1)
			return (new Float(value)).toString();

		if (type == 2)
			return "#t";

		return "(" + operator + " " + left.toString() + " " + right.toString() + ")";
	}
}
