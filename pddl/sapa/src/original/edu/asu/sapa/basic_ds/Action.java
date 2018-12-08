/*********************************************************************
   Author: Minh B. Do (Arizona State University - binhminh@asu.edu)
**********************************************************************/
package edu.asu.sapa.basic_ds;

import java.util.*;

/**
 * Action: Abstract class to store the action structure specified in the domain
 * file. Will be used as the action template to ground actions using object
 * instances from the problem file
 */
public class Action {
	String name;

	boolean d_type; // The duration is CONSTANT (T), or dynamic (F)
	float d_static; // Value of duration if CONSTANT
	MathForm d_dynamic;

	boolean c_type = true; // The cost is constant (T), or dynamic (F)
	float c_static = 1;
	MathForm c_dynamic;

	ArrayList parameters; // List of all objects used in this action
	Hashtable para_map; // Map each objec with its type: obj->type

	ArrayList preconds; // Predicate list represent preconditions
	ArrayList testList; // preconds for metric source (list of functions)

	ArrayList deleteList;
	ArrayList addList;
	ArrayList setList; // effects for metric resource

	ArrayList addTime, deleteTime, setTime, preTime, testTime; // Integer ArrayList
	// all at "st" = 0 and "et" = 1. Include all *add*, *delete*, and *set* effects
	// and *precond* and *tests* for preconditions (1: means "over all" for
	// preconds).
	// BM: July 20, 2003. Modify the preTime list to accommodate the "at end"
	// precondition (will represent it by value 2 in preTime)

	public Action() {
		name = new String();
		d_type = true;
		d_static = 0;

		parameters = new ArrayList();
		para_map = new Hashtable();

		preconds = new ArrayList();
		testList = new ArrayList();

		addList = new ArrayList();
		deleteList = new ArrayList();
		setList = new ArrayList();

		preTime = new ArrayList();
		testTime = new ArrayList();
		addTime = new ArrayList();
		deleteTime = new ArrayList();
		setTime = new ArrayList();
	}

	/** Set the name of this action template */
	public void setName(String s) {
		name = s;
	}

	/** Get the name of this action **/
	public String getName() {
		return name;
	}

	/**
	 * Set action duration type, whether it's static (T) or dynamic in the form of a
	 * mathematic formula (F)
	 */
	public void setDType(boolean durType) {
		d_type = durType;
	}

	/** Get the type (static/dynamic) of this action's duration */
	public boolean getDType() {
		return d_type;
	}

	public void setDStatic(float constantDur) {
		d_static = constantDur;
	}

	public float getDStatic() {
		return d_static;
	}

	public void setDDynamic(MathForm m) {
		d_dynamic = m;
	}

	public MathForm getDDynamic() {
		return d_dynamic;
	}

	/** Action cost type. Similar to action's duration. */
	public void setCType(boolean c) {
		c_type = c;
	}

	public boolean getCType() {
		return c_type;
	}

	public void setCStatic(float cs) {
		c_static = cs;
	}

	public float getCStatic() {
		return c_static;
	}

	public void setCDynamic(MathForm m) {
		c_dynamic = m;
	}

	public MathForm getCDynamic() {
		return c_dynamic;
	}

	/** Add a set of parameters and their (same) type of this action */
	public void putPara(ArrayList paras, String type) {
		String para;

		for (int i = 0; i < paras.size(); i++) {
			para = (String) paras.get(i);
			parameters.add(para);
			para_map.put(para, type);
		}
	}

	public int numPara() {
		return parameters.size();
	}

	public String getPara(int index) {
		return (String) parameters.get(index);
	}

	public ArrayList getAllPara() {
		return parameters;
	}

	public String getParaType(Object obj) {
		return (String) para_map.get(obj);
	}

	/** Add a predicate (pre)condition */
	public void putPrecond(Predicate pre, Integer time) {
		// Check if the same predicate is assigned as precondtion.
		// If it's the case, then we will only change the "time"
		// setting of that precondition to "over all" (strongest) condition
		int i;
		for (i = 0; i < preconds.size(); i++) {
			if (pre.equals((Predicate) preconds.get(i))) {
				System.out.println("Predicate: " + pre.toString() + "is originally timed " + (Integer) preTime.get(i)
						+ " and is newly timed " + time + ". Changed to over_all condition!!");
				preTime.set(i, new Integer(1));
				return;
			}
		}

		preconds.add(pre);
		preTime.add(time);
	}

	public int numPrecond() {
		return preconds.size();
	}

	public Predicate getPrecond(int index) {
		return (Predicate) preconds.get(index);
	}

	/** Duration that this (pre)condition should hold (at start/end/over-all) */
	public Integer getPrecondTime(int index) {
		return (Integer) preTime.get(index);
	}

	public Integer getTestTime(int index) {
		return (Integer) testTime.get(index);
	}

	/** Add a "Test", which is (pre)condition related to the continuous functions */
	public void putTest(Test pre, Integer time) {
		testList.add(pre);
		testTime.add(time);
	}

	public int numTest() {
		return testList.size();
	}

	public Test getTest(int index) {
		return (Test) testList.get(index);
	}

	/** Add an positive (add) predicate effect */
	public void putAdd(Object a) {
		addList.add(a);
	}

	public int numAdd() {
		return addList.size();
	}

	public Predicate getAdd(int index) {
		return (Predicate) addList.get(index);
	}

	/** Add an negative (delete) predicate effect */
	public void putDelete(Object a) {
		deleteList.add(a);
	}

	public int numDelete() {
		return deleteList.size();
	}

	public Predicate getDelete(int index) {
		return (Predicate) deleteList.get(index);
	}

	/** Specify the time instance at which an effect occurs */
	public void putAddTimeEffect(Integer time) {
		addTime.add(time);
	}

	public void putDeleteTimeEffect(Integer time) {
		deleteTime.add(time);
	}

	public void putSetTimeEffect(Integer time) {
		setTime.add(time);
	}

	public Integer getAddTimeEffect(int index) {
		return (Integer) addTime.get(index);
	}

	public Integer getDeleteTimeEffect(int index) {
		return (Integer) deleteTime.get(index);
	}

	public Integer getSetTimeEffect(int index) {
		return (Integer) setTime.get(index);
	}

	/** "Set" is an effect that change the value of some continuous function */
	public void putSet(Object a) {
		setList.add(a);
	}

	public int numSet() {
		return setList.size();
	}

	public MySet getSet(int index) {
		return (MySet) setList.get(index);
	}

	public String toString() {
		String s = new String();
		Object obj;
		int i;

		s += "(" + getName() + "\n";
		s += ":parameters (";
		for (i = 0; i < numPara(); i++) {
			obj = getPara(i);
			s += (String) obj;
			s += "-" + (String) getParaType(obj) + " ";
		}
		s += ")\n";

		s += ":duration ";
		if (d_type)
			s += getDStatic();
		else
			s += getDDynamic();
		s += "\n";

		s += ":cost ";
		if (c_type)
			s += getCStatic();
		else
			s += getCDynamic();
		s += "\n";

		s += ":precondition ";
		for (i = 0; i < numPrecond(); i++) {
			obj = getPrecond(i);
			s += (Predicate) obj + "-" + (Integer) getPrecondTime(i);
		}
		for (i = 0; i < numTest(); i++) {
			obj = getTest(i);
			s += (Test) obj + "-" + (Integer) getTestTime(i) + " ";
		}
		s += "\n";

		s += ":effect ";
		for (i = 0; i < numDelete(); i++) {
			obj = getDelete(i);
			s += "(not " + (Predicate) obj + ")-" + (Integer) getDeleteTimeEffect(i) + " ";
		}
		for (i = 0; i < numAdd(); i++) {
			obj = getAdd(i);
			s += (Predicate) obj + "-" + (Integer) getAddTimeEffect(i) + " ";
		}
		for (i = 0; i < numSet(); i++) {
			obj = getSet(i);
			s += (MySet) obj + "-" + (Integer) getSetTimeEffect(i) + " ";
		}
		s += ")\n";

		return s;
	}
}
