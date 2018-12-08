/********************************************************************
   Author: Minh B. Do (Arizona State University - binhminh@asu.edu)
*********************************************************************/
package edu.asu.sapa.complex_ds;

import edu.asu.sapa.basic_ds.*;

/**
 * GMResDB: Metric Resource Database. This class manages the set of values for
 * all continuous functions. All things related to the metric map are from June
 * 12, 2004 and done by J. Benton.
 * 
 * @author Binh Minh Do
 * @author J. Benton
 */
public class GMResDB {
	// MetricMap metricMap; // Map each FuncID with that function's value
	int numFunc;
	int numFuncInArray;
	float[] funcValues; // values of dynamic functions

	public GMResDB(int nFunc) {
		numFunc = nFunc;
		funcValues = new float[numFunc];
	}

	public GMResDB(GMResDB mr) {

		numFunc = mr.numFunc;
		int num = numFuncInArray = mr.numFuncInArray;
		funcValues = new float[num];
		System.arraycopy(mr.funcValues, 0, funcValues, 0, num);
		/*
		 * for(int i = 0; i < num; i++) funcValues[i] = mr.getValue(i);
		 */
	}

	public int numFunction() {
		return numFunc;
	}

	/** Set value for a particular function */
	public void setValue(int funcID, float value) {
		funcValues[MetricMap.getInstance().actualizeIndex(funcID)] = value;
	}

	/** Get the value of a particular function */
	public float getValue(int funcID) {
		return funcValues[MetricMap.getInstance().actualizeIndex(funcID)];
	}

	/**
	 * Update a function value according to the operator and the indicated
	 * assignment/increment/decreasement value oper = 0 (=); 1: (-=); 2: (+=); 3:
	 * (*=): 4: (/=)
	 */
	public void updateFuncValue(int funcID, float value, int oper) {
		float v = funcValues[MetricMap.getInstance().actualizeIndex(funcID)];

		if (oper == 0)
			v = value;

		if (oper == 1)
			v = v - value;

		if (oper == 2)
			v = v + value;

		if (oper == 3)
			v = v * value;

		if (oper == 4)
			v = (float) v / value;

		funcValues[MetricMap.getInstance().actualizeIndex(funcID)] = v;
	}

	/**
	 * Function to update the GMResDB if we apply some action. There are two choices
	 * here. One is *instance* access if we want to apply all changes *instantly* at
	 * the beginning of that action or we want to update it *continuously* during
	 * the course of action. For the PDDL21 Level 3, only the first option is used.
	 */
	public void update(GAction a, boolean instant) {
		GMySet ms;
		int fID;
		float value;

		float dur;

		if (a.numSet() <= 0)
			return;

		if (a.getDType())
			dur = a.getDStatic();
		else
			dur = a.getDDynamic().value(this, 0);

		for (int i = 0; i < a.numSet(); i++) {
			ms = (GMySet) a.getSet(i);
			fID = ms.getLeftSide();
			value = getMFValue(ms.getRightSide(), dur);

			// "getAssign" returns the operator value... this for loop
			// applies the formula.
			updateFuncValue(fID, value, ms.getAssign());
		}
	}

	/**
	 * Function to only update the resource values if the change is increase used in
	 * the heuristics function when we relax the reduction effects (similar to the
	 * delete effects of the predicate)
	 */
	public void relaxUpdate(GAction a) {
		GMySet ms;
		int fID;
		float value;
		int oper;

		float dur;

		if (a.numSet() <= 0)
			return;

		if (a.getDType())
			dur = a.getDStatic();
		else
			dur = a.getDDynamic().value(this, 0);

		for (int i = 0; i < a.numSet(); i++) {
			ms = (GMySet) a.getSet(i);

			// Don't care if we are reducing the resource level
			oper = ms.getAssign();
			if ((oper == 1))
				continue;

			fID = ms.getLeftSide();
			value = getMFValue(ms.getRightSide(), dur);

			int actualIdx = MetricMap.getInstance().actualizeIndex(fID);
			float v = funcValues[actualIdx];

			if (oper == 0 && v < value) {
				funcValues[actualIdx] = value;
			} else if (oper == 2) {
				funcValues[actualIdx] = v + value;
			} else if (oper == 3 && value > 1) {
				funcValues[actualIdx] = v * value;
				v = v * value;
			} else if (oper == 4 && value < 1) {
				funcValues[actualIdx] = v / value;
			}
		}

	}

	/**
	 * Function to return a value of a MathForm given the current values of all
	 * functions in the GMResDB.
	 */
	public float getMFValue(GMathForm mf, float time) {
		return mf.value(this, time); // Test, will NOT involve #t in test
	}

	/**
	 * Function to check if the metric-precond (tests) of an action are satisfied
	 * given the current state of the GMResDB.
	 */
	public boolean applicable(GAction action) {
		if (action.numTest() == 0)
			return true;

		GTest test;
		float left, right;
		for (int i = 0; i < action.numTest(); i++) {
			test = action.getTest(i);
			if (test.lhsIsStatic) {
				left = test.leftValue;
			} else {
				left = getValue(test.getLeftSide());
			}

			right = getMFValue(test.getRightSide(), 0);

			if (test.getComparator() == 0 && left != right)
				return false;

			if (test.getComparator() == 1 && left >= right)
				return false;

			if (test.getComparator() == 2 && left > right)
				return false;

			if (test.getComparator() == 3 && left <= right)
				return false;

			if (test.getComparator() == 4 && left < right)
				return false;

		}
		return true;
	}

	public void removeResources(boolean[] staticResources) {
		// static = false, dynamic = true
		MetricMap map = MetricMap.getInstance();
		int numFuncInArray = numFunc;
		for (int i = 0; i < numFunc; i++) {
			if (staticResources[i])
				numFuncInArray++;
		}

		float[] dynamicFuncValues = new float[numFuncInArray];

		this.numFuncInArray = numFuncInArray;

		int currentLoc = 0;
		for (int i = 0; i < numFunc; i++) {
			if (!staticResources[i]) { // static, so remove it
				map.removeResource(i);
			} else { // dynamic, so it should be here
				dynamicFuncValues[currentLoc++] = funcValues[i];
			}
		}

		funcValues = dynamicFuncValues;
	}

	/**
	 * Indicate that the number of functions will not increase (so only removal can
	 * occur).
	 * 
	 * @author J. Benton
	 * @version June 12, 2004
	 */
	public void solidify() {
		MetricMap.setNumResources(numFuncInArray = numFunc);
	}

	public String toString() {
		String s = "GMResDB: ";
		int idx;
		for (int i = 0; i < numFunc; i++) {
			idx = MetricMap.getInstance().actualizeIndex(i);
			if (idx < 0) {
				s += "(ID:" + i + ",V:static) ";
			} else {
				s += "(ID:" + i + ",V:" + funcValues[idx] + ") ";
			}
		}
		s += "\n";
		return s;
	}
}

/**
 * Maps a particular resource ID to its place in the resource array of the
 * metric resource database. (This is a singleton, so only one instance will be
 * created during the planning process.) It is important that before removal of
 * resources takes place that all compilation of "function-to-values" is
 * performed in the grounded action's "Test" and "Set".
 * 
 * @version June 12, 2004
 * @author J. Benton
 */
class MetricMap {

	private static MetricMap metricMap = null;
	private static int numResources; // 0
	private int[] resourceID;

	private MetricMap() {
		resourceID = new int[numResources];
		// initialize so that the map indicates that each resource
		// is its ID initially
		for (int i = 0; i < numResources; i++) {
			resourceID[i] = i;
		}
	}

	public static void setNumResources(int resources) {
		numResources = resources;
	}

	public int actualizeIndex(int idx) {
		if (resourceID[idx] < 0)
			System.out.println(";; trying to get " + idx);
		return resourceID[idx];
	}

	public void removeResource(int idx) {
		// from this point forth in the program,
		// if we try to access this value, we'll
		// get an ArrayIndexOutOfBoundsException.
		resourceID[idx] = -1;

		// decrease the actual index for each resource after this one
		// by one.
		for (int i = idx + 1; i < numResources; i++) {
			resourceID[i]--;
		}
	}

	public static MetricMap getInstance() {
		if (metricMap == null)
			metricMap = new MetricMap();

		return metricMap;
	}
}