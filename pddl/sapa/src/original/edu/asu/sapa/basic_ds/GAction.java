/************************************************************************
   Author: Minh B. Do - Arizona State University
*************************************************************************/
package edu.asu.sapa.basic_ds;

/**
 * GAction: Represent the grounded action with preconditions are grounded
 * predicates (Integer) and grounded "Test" (math comparisons). Effects are also
 * grounded predicates and grounded "Set" (math assignments).
 */
public class GAction {
	int id = -1; // Unique ID for a ground action

	boolean d_type; // The duration is CONSTANT (T), or dynamic (F)
	float d_static; // Value of duration if CONSTANT
	GMathForm d_dynamic;
	public boolean duration_constant = false;

	boolean c_type; // The cost is CONSTANT (T), or dynamic (F)
	float c_static; // Value of cost if CONSTANT
	GMathForm c_dynamic;
	public boolean cost_constant = false;

	int[] preconds; // Predicate list represent preconditions
	GTest[] testList; // preconds for metric source (list of functions)

	int[] deleteList, addList;
	GMySet[] setList; // effects for metric resource

	int[] preTime, testTime, addTime, deleteTime, setTime; // Time (start/end) each
	// precondition, test, add or delete occurs. Can be changed to "boolean" now.
	int nPre, nTest, nAdd, nDel, nSet;
	int nInsPre; // BM: Added July 23, 2003 to support the addition of "at end" preconds
	// InsPre: Preconditions that need to be true at the begining of action ("at
	// start"
	// or "over all"). Thus, excluding "at end" condition.

	public GAction() {

	}

	/** Constructor */
	public GAction(int numPrecond, int numTest, int numDel, int numAdd, int numSet) {
		nPre = numPrecond;
		nInsPre = numPrecond;
		nTest = numTest;
		nAdd = numAdd;
		nDel = numDel;
		nSet = numSet;

		d_type = true;
		d_static = 0;

		c_type = true;
		c_static = 1;

		preconds = new int[numPrecond];
		preTime = new int[numPrecond];

		testList = new GTest[numTest];
		testTime = new int[numTest];

		addList = new int[numAdd];
		addTime = new int[numAdd];

		deleteList = new int[numDel];
		setList = new GMySet[numSet];

		deleteTime = new int[numDel];
		setTime = new int[numSet];
	}

	/**
	 * Unique ID of this ground action. Which is also the index in the ground action
	 * list
	 */
	public void setID(int i) {
		id = i;
	}

	public int getID() {
		return id;
	}

	/**
	 * Duration type of this action. Whether it's fixed (static) duration or a
	 * dynamic one (represented by a GMathForm data structure)
	 */
	public void setDType(boolean t) {
		d_type = t;
	}

	/**
	 * Reset the precondition array. Intended to be called only while grounding
	 * actions after static preconditions are removed from this action. June 10,
	 * 2004
	 * 
	 * @author J. Benton
	 */
	public void resetPreconditionArray() {
		int[] precondsList = new int[nPre];
		int[] precondsTimeList = new int[nPre];

		System.arraycopy(preconds, 0, precondsList, 0, nPre);
		preconds = precondsList;
		System.arraycopy(preTime, 0, precondsTimeList, 0, nPre);
		preTime = precondsTimeList;
	}

	public boolean getDType() {
		return d_type;
	}

	public void setDStatic(float f) {
		d_static = f;
	}

	public float getDStatic() {
		return d_static;
	}

	public void setDDynamic(GMathForm m) {
		// d_dynamic = new GMathForm(m);
		d_dynamic = m;
	}

	public GMathForm getDDynamic() {
		// return new GMathForm(d_dynamic);
		return d_dynamic;
	}

	/** The cost of this action. Represented similar to the duration */
	public void setCType(boolean t) {
		c_type = t;
	}

	public boolean getCType() {
		return c_type;
	}

	public void setCStatic(float f) {
		c_static = f;
	}

	public float getCStatic() {
		return c_static;
	}

	public void setCDynamic(GMathForm m) {
		// c_dynamic = new GMathForm(m);
		c_dynamic = m;
	}

	public GMathForm getCDynamic() {
		// return new GMathForm(c_dynamic);
		return c_dynamic;
	}

	/** Add a new (pre)condition to the action specification */
	public void addPrecond(int pre, int index) {
		preconds[index] = pre;
	}

	/**
	 * remove a precondition from the action specification (used for removing static
	 * values) after grounding
	 * 
	 * @param index
	 *            Index of the precondition to remove.
	 */
	public void removePrecond(int index) {
		nPre--;
		// only decreases if we are removing an instant precondition (i.e. "over all"
		// or "at start"
		if (preTime[index] < 2)
			nInsPre--;
		int precondsLength = preconds.length - 1;
		// remove the precondition.
		for (int i = index; i < precondsLength; i++) {
			preconds[i] = preconds[i + 1];
			preTime[i] = preTime[i + 1];
		}
	}

	public int numPrecond() {
		return nPre;
	}

	public int numInsPrecond() {
		return nInsPre;
	}

	/**
	 * Get a (pre)condition given by its index in the list of all (pre)conditions
	 */
	public int getPrecond(int index) {
		return preconds[index];
	}

	/**
	 * Get the index of a given (pre)condition. Mostly used for checking if a given
	 * predicate is a (pre)condition of a given action or not
	 */
	public int indexPrecond(int pre) {
		for (int i = 0; i < nPre; i++)
			if (pre == preconds[i])
				return i;

		return -1;
	}

	/**
	 * For durations of (pre)conditions Complies with PDDL2.1 level 3: 0 = Instance
	 * pre need to be true at start time; 1 = whole true for the whole duration of
	 * action (st,et); 2: Only need to be true "at end"
	 */
	public void putPreTime(int time, int index) {
		preTime[index] = time;

		if (time > 1) {
			nInsPre--;
		}

	}

	public int getPreTime(int index) {
		return preTime[index];
	}

	public int getPreTimeObj(int pre) {
		for (int i = 0; i < nPre; i++)
			if (pre == preconds[i])
				return preTime[i];

		return -1;
	}

	/** Add new positive predicate effect (0: st; 1: et) */
	public void putAdd(int pred, int index) {
		addList[index] = pred;
	}

	public int numAdd() {
		return nAdd;
	}

	public int getAdd(int index) {
		return addList[index];
	}

	public void putAddTimeEffect(int time, int index) {
		addTime[index] = time;
	}

	public int getAddTimeEffect(int index) {
		return addTime[index];
	}

	public int getAddTimeEffectObj(int pred) {
		for (int i = 0; i < nAdd; i++)
			if (addList[i] == pred)
				return addTime[i];

		return -1;
	}

	public int indexAdd(int pred) {
		for (int i = 0; i < nAdd; i++)
			if (addList[i] == pred)
				return i;

		return -1;
	}

	/** Add new negative predicate effect (0:st; 1:et) */
	public void putDelete(int pred, int index) {
		deleteList[index] = pred;
	}

	public int numDelete() {
		return nDel;
	}

	public int getDelete(int index) {
		return deleteList[index];
	}

	public void putDeleteTimeEffect(int time, int index) {
		deleteTime[index] = time;
	}

	public int getDeleteTimeEffect(int index) {
		return deleteTime[index];
	}

	public int getDeleteTimeEffectObj(int pred) {
		for (int i = 0; i < nDel; i++)
			if (deleteList[i] == pred)
				return deleteTime[i];

		return -1;
	}

	public int indexDelete(int pred) {
		for (int i = 0; i < nDel; i++)
			if (deleteList[i] == pred)
				return i;

		return -1;
	}

	/** Add new Test, which is handled similar to predicate precondition */
	public void putTest(GTest aTest, int index) {
		testList[index] = aTest;
	}

	public int numTest() {
		return nTest;
	}

	public GTest getTest(int index) {
		return testList[index];
	}

	public void putTestTime(int time, int index) {
		testTime[index] = time;
	}

	public int getTestTime(int index) {
		return testTime[index];
	}

	public int indexTest(int resID) {
		for (int i = 0; i < nTest; i++)
			if (testList[i].getLeftSide() == resID)
				return i;

		return -1;
	}

	/**
	 * Add a new "Set", which is a effect that change the value of some continuous
	 * function
	 */
	public void putSet(GMySet aSet, int index) {
		setList[index] = aSet;
	}

	public int numSet() {
		return nSet;
	}

	public GMySet getSet(int index) {
		return setList[index];
	}

	// Specify the time moment in which an effect occur: 0: start time; 1: end time;
	public void putSetTimeEffect(int time, int index) {
		setTime[index] = time;
	}

	public int getSetTimeEffect(int index) {
		return setTime[index];
	}

	public int indexSet(int resID) {
		for (int i = 0; i < nSet; i++)
			if (setList[i].getLeftSide() == resID)
				return i;

		return -1;
	}

	/** Function used to shortly print the information about this ground action */
	public String toString() {
		String s = new String();
		Object obj;
		int i;

		s += "(ID: " + getID() + "  ";

		s += ":duration ";
		if (d_type)
			s += getDStatic();
		else
			s += getDDynamic();
		s += "\n";

		s += ":precond ";
		for (i = 0; i < nPre; i++)
			s += "(" + preconds[i] + "," + preTime[i] + ") ";
		s += "\n";

		s += ":effect ";
		for (i = 0; i < nAdd; i++)
			s += "+(" + addList[i] + "," + addTime[i] + ") ";
		for (i = 0; i < nDel; i++)
			s += "-(" + deleteList[i] + "," + deleteTime[i] + ") ";
		s += "\n";

		return s;
	}
}
